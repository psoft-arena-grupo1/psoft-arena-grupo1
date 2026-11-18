package psoft.arena.controller;

import static org.hamcrest.Matchers.hasSize;
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
        mvc.perform(json(post("/atividades"), "{\"problemaId\":" + id + "}")).andExpect(status().isCreated());

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

}
