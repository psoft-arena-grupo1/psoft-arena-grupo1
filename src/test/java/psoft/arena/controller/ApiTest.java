package psoft.arena.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiTest {

    @Autowired
    private MockMvc mvc;

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder req, String corpo) {
        return req.contentType(MediaType.APPLICATION_JSON).content(corpo);
    }

    private Integer criarProblema() throws Exception {
        String resposta = mvc.perform(json(post("/problemas"),
                        "{\"titulo\":\"Biblioteca\",\"descricao\":\"d\",\"requisitosFuncionais\":\"rf\",\"requisitosDeProjeto\":\"rp\"}"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private Integer criarId(String url, String corpo) throws Exception {
        String resposta = mvc.perform(json(post(url), corpo)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    private String corpoAtividade(Integer problemaId) throws Exception {
        Integer turmaId = criarId("/turmas",
                "{\"nome\":\"POO\",\"codigo\":\"P1\",\"periodoLetivo\":\"2026.1\",\"professorResponsavel\":\"Ana\"}");
        Integer estrategiaId = criarId("/estrategias",
                "{\"nome\":\"Smells\",\"tipo\":\"CODE_SMELLS\",\"descricao\":\"d\",\"prompt\":\"p\"}");
        Integer llmId = criarId("/llms",
                "{\"nome\":\"grok-3\",\"urlApi\":\"https://api.x.ai/v1\",\"chaveApi\":\"segredo-123\"}");
        return "{\"problemaId\":" + problemaId + ",\"turmaId\":" + turmaId + ",\"estrategiaId\":" + estrategiaId
                + ",\"llmPadraoId\":" + llmId + ",\"titulo\":\"Lab 1\",\"descricao\":\"d\",\"prazo\":\"2999-12-31T23:59:00\"}";
    }

    // ---------- problemas ----------

    @Test
    void criarProblemaRetorna201ComStatusAtivo() throws Exception {
        mvc.perform(json(post("/problemas"),
                        "{\"titulo\":\"Biblioteca\",\"descricao\":\"d\",\"requisitosFuncionais\":\"rf\",\"requisitosDeProjeto\":\"rp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Biblioteca"))
                .andExpect(jsonPath("$.status").value("Ativo"));
    }

    @Test
    void criarProblemaComCampoVazioRetorna400ComMensagem() throws Exception {
        mvc.perform(json(post("/problemas"), "{\"titulo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Título, descrição, requisitos funcionais e requisitos de projeto são obrigatórios."));
    }

    @Test
    void buscarProblemaInexistenteRetorna404() throws Exception {
        mvc.perform(get("/problemas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Problema não encontrado."));
    }

    @Test
    void desativarERemoverProblema() throws Exception {
        Integer id = criarProblema();

        mvc.perform(patch("/problemas/" + id + "/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Inativo"));
        mvc.perform(delete("/problemas/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/problemas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void naoRemoverProblemaUtilizadoRetorna400() throws Exception {
        Integer id = criarProblema();
        mvc.perform(json(post("/atividades"), corpoAtividade(id))).andExpect(status().isCreated());

        mvc.perform(delete("/problemas/" + id)).andExpect(status().isBadRequest());
    }

    @Test
    void listarProblemasFiltrandoPorTitulo() throws Exception {
        criarProblema();

        mvc.perform(get("/problemas").param("titulo", "biblio")).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/problemas").param("titulo", "xyz")).andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- estudantes ----------

    @Test
    void criarEditarEConsultarEstudante() throws Exception {
        mvc.perform(json(post("/estudantes"), "{\"nome\":\"Maria\",\"matricula\":\"123\",\"email\":\"maria@email.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricula").value("123"));
        mvc.perform(json(put("/estudantes/123"), "{\"nome\":\"Maria Souza\",\"email\":\"ms@email.com\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/estudantes/123"))
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.email").value("ms@email.com"));
    }

    @Test
    void matriculaRepetidaRetorna400() throws Exception {
        String corpo = "{\"nome\":\"Maria\",\"matricula\":\"123\",\"email\":\"maria@email.com\"}";
        mvc.perform(json(post("/estudantes"), corpo)).andExpect(status().isCreated());

        mvc.perform(json(post("/estudantes"), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Já existe um estudante com essa matrícula."));
    }

    // ---------- estratégias ----------

    @Test
    void criarEstrategiaEAlterarPrompt() throws Exception {
        String resposta = mvc.perform(json(post("/estrategias"),
                        "{\"nome\":\"Smells\",\"tipo\":\"CODE_SMELLS\",\"descricao\":\"d\",\"prompt\":\"p1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CODE_SMELLS"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(resposta, "$.id");

        mvc.perform(json(patch("/estrategias/" + id + "/prompt"), "{\"prompt\":\"p2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prompt").value("p2"))
                .andExpect(jsonPath("$.nome").value("Smells"));
    }

    @Test
    void listarEstrategiasPorTipo() throws Exception {
        mvc.perform(json(post("/estrategias"), "{\"nome\":\"A\",\"tipo\":\"CODE_SMELLS\",\"descricao\":\"d\",\"prompt\":\"p\"}"));
        mvc.perform(json(post("/estrategias"), "{\"nome\":\"B\",\"tipo\":\"PADROES_DE_PROJETO\",\"descricao\":\"d\",\"prompt\":\"p\"}"));

        mvc.perform(get("/estrategias").param("tipo", "CODE_SMELLS"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("A"));
    }

    @Test
    void tipoDeEstrategiaInexistenteRetorna400() throws Exception {
        mvc.perform(json(post("/estrategias"), "{\"nome\":\"A\",\"tipo\":\"OUTRO\",\"descricao\":\"d\",\"prompt\":\"p\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarAtividadeRetorna201AbertaSemExporChaveDaApi() throws Exception {
        mvc.perform(json(post("/atividades"), corpoAtividade(criarProblema())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.llmPadrao.nome").value("grok-3"))
                .andExpect(jsonPath("$.llmPadrao.chaveApi").doesNotExist())
                .andExpect(content().string(not(containsString("segredo-123"))));
    }

    @Test
    void criarAtividadeSemLlmRetorna400() throws Exception {
        String corpo = corpoAtividade(criarProblema()).replaceAll(",\"llmPadraoId\":\\d+", "");

        mvc.perform(json(post("/atividades"), corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Turma, problema, estratégia de avaliação e LLM padrão são obrigatórios."));
    }

    @Test
    void encerrarAtividadeRetornaStatusConcluida() throws Exception {
        Integer id = criarId("/atividades", corpoAtividade(criarProblema()));

        mvc.perform(patch("/atividades/" + id + "/encerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void cadastrarLlmNaoRetornaChaveDaApi() throws Exception {
        Integer id = criarId("/llms", "{\"nome\":\"grok-3\",\"urlApi\":\"https://api.x.ai/v1\",\"chaveApi\":\"segredo-123\"}");

        mvc.perform(get("/llms/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("grok-3"))
                .andExpect(content().string(not(containsString("segredo-123"))));
    }

    @Test
    void cadastrarLlmSemChaveRetorna400() throws Exception {
        mvc.perform(json(post("/llms"), "{\"nome\":\"grok-3\",\"urlApi\":\"https://api.x.ai/v1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Nome do modelo, URL da API e chave de acesso são obrigatórios."));
    }
}
