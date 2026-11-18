package psoft.arena.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubmissaoApiTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Autowired
    private MockMvc mvc;

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
    void submeterRetorna201ComStatusSubmetidaEMensagemDeRecebimento() throws Exception {
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", CODIGO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.atividadeId").value(atividadeId))
                .andExpect(jsonPath("$.matricula").value("123"))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.dataHora").value(notNullValue()))
                .andExpect(jsonPath("$.mensagem").value(containsString("recebida")));
    }

    @Test
    void cadaNovaSubmissaoGeraNovaVersao() throws Exception {
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", CODIGO)))
                .andExpect(jsonPath("$.versao").value(1));
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", CODIGO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.versao").value(2));
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", CODIGO)))
                .andExpect(jsonPath("$.versao").value(3));
    }

    @Test
    void versoesSaoContadasPorEstudante() throws Exception {
        criarEstudante("456");
        matricularNaTurma("456");

        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", CODIGO)))
                .andExpect(jsonPath("$.versao").value(1));
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("456", CODIGO)))
                .andExpect(jsonPath("$.versao").value(1));
    }

    @Test
    void estudanteForaDaTurmaRetorna403() throws Exception {
        criarEstudante("456");

        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("456", CODIGO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Somente estudantes da turma da atividade podem realizar submissões."));
    }

    @Test
    void atividadeConcluidaRetorna400() throws Exception {
        mvc.perform(patch("/atividades/" + atividadeId + "/encerrar")).andExpect(status().isOk());

        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", CODIGO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Atividades concluídas não aceitam novas submissões."));
    }

    @Test
    void atividadeInexistenteRetorna404() throws Exception {
        mvc.perform(json(post("/atividades/999999/submissoes"), corpo("123", CODIGO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Atividade não encontrada."));
    }

    @Test
    void estudanteInexistenteRetorna404() throws Exception {
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("999", CODIGO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Estudante não encontrado."));
    }

    @Test
    void codigoQueNaoEhJavaRetorna400() throws Exception {
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", "print('oi')")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A solução deve ser submetida em Java."));
    }

    @Test
    void codigoVazioRetorna400() throws Exception {
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), corpo("123", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O código-fonte da solução é obrigatório."));
    }

    @Test
    void matriculaAusenteRetorna400() throws Exception {
        mvc.perform(json(post("/atividades/" + atividadeId + "/submissoes"), "{\"codigoFonte\":\"" + CODIGO + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("A matrícula do estudante é obrigatória."));
    }

    // Acompanhamento da submissão pelo estudante.
    // Como a classe é @Transactional (sem commit), a fila não é acionada: a submissão fica em SUBMETIDA.

    @Test
    void consultarSubmissaoRetornaEstadoAtualEHistoricoInicial() throws Exception {
        Integer id = submeter("123");

        mvc.perform(get("/estudantes/123/submissoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.atividadeId").value(atividadeId))
                .andExpect(jsonPath("$.matricula").value("123"))
                .andExpect(jsonPath("$.versao").value(1))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].statusAnterior").doesNotExist())
                .andExpect(jsonPath("$.historico[0].statusNovo").value("SUBMETIDA"))
                .andExpect(jsonPath("$.historico[0].dataHora").value(notNullValue()));
    }

    @Test
    void listarSubmissoesDoEstudanteRetornaTodasAsVersoesComSeuEstado() throws Exception {
        submeter("123");
        submeter("123");

        mvc.perform(get("/estudantes/123/submissoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].versao", containsInAnyOrder(1, 2)))
                .andExpect(jsonPath("$[?(@.status != 'SUBMETIDA')]", hasSize(0)));
    }

    @Test
    void listaNaoIncluiSubmissoesDeOutrosEstudantes() throws Exception {
        criarEstudante("456");
        matricularNaTurma("456");
        submeter("123");
        submeter("456");

        mvc.perform(get("/estudantes/123/submissoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].matricula").value("123"));
    }

    @Test
    void estudanteSemSubmissoesRecebeListaVazia() throws Exception {
        criarEstudante("456");

        mvc.perform(get("/estudantes/456/submissoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void estudanteNaoConsultaSubmissaoDeOutroEstudante() throws Exception {
        criarEstudante("456");
        matricularNaTurma("456");
        Integer id = submeter("123");

        mvc.perform(get("/estudantes/456/submissoes/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Submissão não encontrada."));
    }

    @Test
    void consultarSubmissaoInexistenteRetorna404() throws Exception {
        mvc.perform(get("/estudantes/123/submissoes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Submissão não encontrada."));
    }

    @Test
    void consultarSubmissoesDeEstudanteInexistenteRetorna404() throws Exception {
        mvc.perform(get("/estudantes/999/submissoes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Estudante não encontrado."));
        mvc.perform(get("/estudantes/999/submissoes/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Estudante não encontrado."));
    }

    @Test
    void submissaoContinuaConsultavelDepoisQueAAtividadeEhConcluida() throws Exception {
        Integer id = submeter("123");
        mvc.perform(patch("/atividades/" + atividadeId + "/encerrar")).andExpect(status().isOk());

        mvc.perform(get("/estudantes/123/submissoes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMETIDA"));
    }
}