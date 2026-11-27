package psoft.arena.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import psoft.arena.model.Avaliacao;
import psoft.arena.model.Submissao;
import psoft.arena.repository.AvaliacaoRepository;
import psoft.arena.repository.SubmissaoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReprocessamentoApiTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SubmissaoRepository submissaoRepository;
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Integer atividadeId;

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private Integer criarId(String url, String corpo) throws Exception {
        String resposta = mvc.perform(json(post(url), corpo)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private Long submeter() throws Exception {
        String resposta = mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"),
                "{\"matricula\":\"123\",\"codigoFonte\":\"" + CODIGO + "\"}").header("X-Matricula", "123"))
                .andReturn().getResponse().getContentAsString();
        return ((Integer) JsonPath.read(resposta, "$.id")).longValue();
    }

    private Long submissaoComFalha() throws Exception {
        Long id = submeter();
        Submissao submissao = submissaoRepository.findById(id).orElseThrow();
        submissao.encaminhar();
        submissao.iniciarProcessamento();
        Avaliacao avaliacao = new Avaliacao(submissao);
        avaliacao.falhar("O modelo grok-3 respondeu com erro HTTP 500.");
        avaliacaoRepository.save(avaliacao);
        submissao.falhar();
        submissaoRepository.flush();
        return id;
    }

    @BeforeEach
    void setUp() throws Exception {
        Integer turmaId = criarId("/turmas",
                "{\"nome\":\"POO\",\"codigo\":\"P1\",\"periodoLetivo\":\"2026.1\",\"professorResponsavel\":\"Ana\"}");
        Integer problemaId = criarId("/problemas",
                "{\"titulo\":\"Biblioteca\",\"descricao\":\"d\",\"requisitosFuncionais\":\"rf\",\"requisitosDeProjeto\":\"rp\"}");
        Integer estrategiaId = criarId("/estrategias",
                "{\"nome\":\"Smells\",\"tipo\":\"CODE_SMELLS\",\"descricao\":\"d\",\"prompt\":\"p\"}");
        Integer llmId = criarId("/llms",
                "{\"nome\":\"grok-3\",\"urlApi\":\"https://api.x.ai/v1\",\"chaveApi\":\"segredo-123\"}");
        mvc.perform(json(post("/estudantes"), "{\"nome\":\"Aluno\",\"matricula\":\"123\",\"email\":\"a@email.com\"}"));
        mvc.perform(json(post("/turmas/" + turmaId + "/estudantes"), "{\"matricula\":\"123\"}"));
        atividadeId = criarId("/atividades", "{\"problemaId\":" + problemaId + ",\"turmaId\":" + turmaId
                + ",\"estrategiaId\":" + estrategiaId + ",\"llmPadraoId\":" + llmId
                + ",\"titulo\":\"Lab 1\",\"descricao\":\"d\",\"prazo\":\"2999-12-31T23:59:00\"}");
    }

    @Test
    void reprocessarSubmissaoComFalhaVoltaParaEmFilaEPreservaOHistorico() throws Exception {
        Long id = submissaoComFalha();

        mvc.perform(post("/submissoes/" + id + "/reprocessamento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.status").value("EM_FILA"))
                .andExpect(jsonPath("$.historico", hasSize(5)))
                .andExpect(jsonPath("$.historico[3].statusNovo").value("FALHA"))
                .andExpect(jsonPath("$.historico[4].statusAnterior").value("FALHA"))
                .andExpect(jsonPath("$.historico[4].statusNovo").value("EM_FILA"))
                .andExpect(jsonPath("$.historico[4].motivo").value("Reprocessamento solicitado (tentativa 2)."));
    }

    @Test
    void reprocessamentoNaoCriaNovaSubmissaoEMantemOCodigoOriginal() throws Exception {
        Long id = submissaoComFalha();
        long antes = submissaoRepository.count();

        mvc.perform(post("/submissoes/" + id + "/reprocessamento")).andExpect(status().isOk());

        assertEquals(antes, submissaoRepository.count());
        assertEquals(CODIGO, submissaoRepository.findById(id).orElseThrow().getCodigoFonte());
    }

    @Test
    void reprocessarSubmissaoQueNaoFalhouRetorna409() throws Exception {
        Long id = submeter();

        mvc.perform(post("/submissoes/" + id + "/reprocessamento"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Não é possível reprocessar uma submissão no estado Submetida."));
    }

    @Test
    void reprocessarSubmissaoInexistenteRetorna404() throws Exception {
        mvc.perform(post("/submissoes/999999/reprocessamento"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Submissão não encontrada."));
    }

    @Test
    void tentativasDeAvaliacaoAnterioresContinuamConsultaveis() throws Exception {
        Long id = submissaoComFalha();
        mvc.perform(post("/submissoes/" + id + "/reprocessamento")).andExpect(status().isOk());
        Avaliacao novaTentativa = new Avaliacao(submissaoRepository.findById(id).orElseThrow());
        novaTentativa.concluir(new BigDecimal("8.0"), "e1", "j1");
        avaliacaoRepository.save(novaTentativa);

        mvc.perform(get("/submissoes/" + id + "/avaliacao/tentativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("FALHA"))
                .andExpect(jsonPath("$[1].status").value("CONCLUIDA"));
        mvc.perform(get("/submissoes/" + id + "/avaliacao"))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.nota").value(8.0));
    }
}
