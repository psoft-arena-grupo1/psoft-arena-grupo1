package psoft.arena.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
class AvaliacaoApiTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    @Autowired
    private SubmissaoRepository submissaoRepository;

    private Integer turmaId;
    private Integer atividadeId;

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private Integer criarId(String url, String corpo) throws Exception {
        String resposta = mvc.perform(json(post(url), corpo)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private void criarEstudante(String matricula) throws Exception {
        mvc.perform(json(post("/estudantes"),
                "{\"nome\":\"Aluno " + matricula + "\",\"matricula\":\"" + matricula + "\",\"email\":\"" + matricula
                        + "@email.com\"}"))
                .andExpect(status().isCreated());
    }

    private void matricularNaTurma(String matricula) throws Exception {
        mvc.perform(json(post("/turmas/" + turmaId + "/estudantes"), "{\"matricula\":\"" + matricula + "\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    private String corpo(String matricula, String codigo) {
        return "{\"matricula\":\"" + matricula + "\",\"codigoFonte\":\"" + codigo + "\"}";
    }

    private Integer submeter(String matricula) throws Exception {
        return criarId("/atividades/" + atividadeId + "/submissoes", corpo(matricula, CODIGO));
    }

    private Avaliacao criarAvaliacaoConcluida(Integer submissaoId) {
        Submissao submissao = submissaoRepository.findById(submissaoId.longValue()).orElseThrow();
        Avaliacao avaliacao = new Avaliacao(submissao);
        avaliacao.concluir(new BigDecimal("8.5"), "e1", "j1");
        return avaliacaoRepository.save(avaliacao);
    }

    @BeforeEach
    void setUp() throws Exception {
        turmaId = criarId("/turmas",
                "{\"nome\":\"POO\",\"codigo\":\"P1\",\"periodoLetivo\":\"2026.1\",\"professorResponsavel\":\"Ana\"}");
        Integer problemaId = criarId("/problemas",
                "{\"titulo\":\"Biblioteca\",\"descricao\":\"d\",\"requisitosFuncionais\":\"rf\",\"requisitosDeProjeto\":\"rp\"}");
        Integer estrategiaId = criarId("/estrategias",
                "{\"nome\":\"Smells\",\"tipo\":\"CODE_SMELLS\",\"descricao\":\"d\",\"prompt\":\"p\"}");
        Integer llmId = criarId("/llms",
                "{\"nome\":\"grok-3\",\"urlApi\":\"https://api.x.ai/v1\",\"chaveApi\":\"segredo-123\"}");
        criarEstudante("123");
        matricularNaTurma("123");
        atividadeId = criarId("/atividades", "{\"problemaId\":" + problemaId + ",\"turmaId\":" + turmaId
                + ",\"estrategiaId\":" + estrategiaId + ",\"llmPadraoId\":" + llmId
                + ",\"titulo\":\"Lab 1\",\"descricao\":\"d\",\"prazo\":\"2999-12-31T23:59:00\"}");
    }

    @Test
    void consultarAvaliacaoConcluidaRetornaStatusENota() throws Exception {
        Integer submissaoId = submeter("123");
        criarAvaliacaoConcluida(submissaoId);

        mvc.perform(get("/submissoes/" + submissaoId + "/avaliacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.submissaoId").value(submissaoId))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.nota").value(8.5))
                .andExpect(jsonPath("$.evidencias").value("e1"))
                .andExpect(jsonPath("$.justificativa").value("j1"))
                .andExpect(jsonPath("$.resultados", hasSize(0)));
    }

    @Test
    void consultarSubmissaoSemAvaliacaoRetorna404() throws Exception {
        Integer submissaoId = submeter("123");

        mvc.perform(get("/submissoes/" + submissaoId + "/avaliacao"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Avaliação não encontrada."));
    }
}
