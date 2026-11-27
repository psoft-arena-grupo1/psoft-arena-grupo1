package psoft.arena.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
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
class ResultadoAvaliacaoApiTest {

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

    private void criarEstudanteNaTurma(String matricula) throws Exception {
        mvc.perform(json(post("/estudantes"),
                "{\"nome\":\"Aluno " + matricula + "\",\"matricula\":\"" + matricula + "\",\"email\":\"" + matricula
                        + "@email.com\"}"))
                .andExpect(status().isCreated());
        mvc.perform(json(post("/turmas/" + turmaId + "/estudantes"), "{\"matricula\":\"" + matricula + "\"}"))
                .andExpect(status().is2xxSuccessful());
    }

    private Integer submeter(String matricula) throws Exception {
        String resposta = mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"),
                "{\"matricula\":\"" + matricula + "\",\"codigoFonte\":\"" + CODIGO + "\"}")
                .header("X-Matricula", matricula)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private Avaliacao novaAvaliacao(Integer submissaoId) {
        Submissao submissao = submissaoRepository.findById(submissaoId.longValue()).orElseThrow();
        return new Avaliacao(submissao);
    }

    private String urlResultado(String matricula, Integer submissaoId) {
        return "/estudantes/" + matricula + "/submissoes/" + submissaoId + "/resultado";
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
        criarEstudanteNaTurma("123");
        atividadeId = criarId("/atividades", "{\"problemaId\":" + problemaId + ",\"turmaId\":" + turmaId
                + ",\"estrategiaId\":" + estrategiaId + ",\"llmPadraoId\":" + llmId
                + ",\"titulo\":\"Lab 1\",\"descricao\":\"d\",\"prazo\":\"2999-12-31T23:59:00\"}");
    }

    @Test
    void estudanteConsultaOResultadoDaAvaliacaoConcluida() throws Exception {
        Integer submissaoId = submeter("123");
        Avaliacao avaliacao = novaAvaliacao(submissaoId);
        avaliacao.concluir(new BigDecimal("8.5"), "e1\ne2", "Boa separação de responsabilidades.");
        avaliacaoRepository.save(avaliacao);

        mvc.perform(get(urlResultado("123", submissaoId)).header("X-Matricula", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.submissaoId").value(submissaoId))
                .andExpect(jsonPath("$.atividadeId").value(atividadeId))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
                .andExpect(jsonPath("$.nota").value(8.5))
                .andExpect(jsonPath("$.justificativa").value("Boa separação de responsabilidades."))
                .andExpect(jsonPath("$.evidencias", contains("e1", "e2")));
    }

    @Test
    void resultadoNaoExpoeDetalhesInternosDaAvaliacao() throws Exception {
        Integer submissaoId = submeter("123");
        Avaliacao avaliacao = novaAvaliacao(submissaoId);
        avaliacao.concluir(new BigDecimal("8.5"), "e1", "j1");
        avaliacaoRepository.save(avaliacao);

        mvc.perform(get(urlResultado("123", submissaoId)).header("X-Matricula", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados").doesNotExist())
                .andExpect(jsonPath("$.motivoFalha").doesNotExist())
                .andExpect(jsonPath("$.chaveApi").doesNotExist());
    }

    @Test
    void cadaVersaoTemSeuProprioResultado() throws Exception {
        Integer primeira = submeter("123");
        Integer segunda = submeter("123");
        Avaliacao a1 = novaAvaliacao(primeira);
        a1.concluir(new BigDecimal("5"), "e", "j");
        avaliacaoRepository.save(a1);
        Avaliacao a2 = novaAvaliacao(segunda);
        a2.concluir(new BigDecimal("9"), "e", "j");
        avaliacaoRepository.save(a2);

        mvc.perform(get(urlResultado("123", primeira)).header("X-Matricula", "123"))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.nota").value(5));
        mvc.perform(get(urlResultado("123", segunda)).header("X-Matricula", "123"))
                .andExpect(jsonPath("$.versao").value(2))
                .andExpect(jsonPath("$.nota").value(9));
    }

    @Test
    void submissaoSemAvaliacaoRetorna404ComMensagemDeIndisponivel() throws Exception {
        Integer submissaoId = submeter("123");

        mvc.perform(get(urlResultado("123", submissaoId)).header("X-Matricula", "123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("O resultado da avaliação ainda não está disponível."));
    }

    @Test
    void avaliacaoEmProcessamentoRetorna404() throws Exception {
        Integer submissaoId = submeter("123");
        avaliacaoRepository.save(novaAvaliacao(submissaoId));

        mvc.perform(get(urlResultado("123", submissaoId)).header("X-Matricula", "123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("O resultado da avaliação ainda não está disponível."));
    }

    @Test
    void avaliacaoComFalhaRetorna404SemRevelarOMotivoInterno() throws Exception {
        Integer submissaoId = submeter("123");
        Avaliacao avaliacao = novaAvaliacao(submissaoId);
        avaliacao.falhar("Modelo grok-3: erro 500 da API");
        avaliacaoRepository.save(avaliacao);

        mvc.perform(get(urlResultado("123", submissaoId)).header("X-Matricula", "123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(containsString("não foi concluída")));
    }

    @Test
    void estudanteNaoVeOResultadoDeSubmissaoDeOutroEstudante() throws Exception {
        criarEstudanteNaTurma("456");
        Integer submissaoId = submeter("123");
        Avaliacao avaliacao = novaAvaliacao(submissaoId);
        avaliacao.concluir(new BigDecimal("8.5"), "e1", "j1");
        avaliacaoRepository.save(avaliacao);

        mvc.perform(get(urlResultado("456", submissaoId)).header("X-Matricula", "456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Submissão não encontrada."));
    }

    @Test
    void submissaoInexistenteRetorna404() throws Exception {
        mvc.perform(get(urlResultado("123", 999999)).header("X-Matricula", "123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Submissão não encontrada."));
    }

    @Test
    void estudanteInexistenteRetorna404() throws Exception {
        Integer submissaoId = submeter("123");

        mvc.perform(get(urlResultado("999", submissaoId)).header("X-Matricula", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Estudante não encontrado."));
    }
}