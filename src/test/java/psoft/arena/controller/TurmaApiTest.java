package psoft.arena.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurmaApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;
    @Autowired private EntityManager entityManager;

    private Map<String, Object> dados() {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("nome", "Projeto de Software");
        dados.put("codigo", "P1");
        dados.put("periodoLetivo", "2026.1");
        dados.put("professorResponsavel", "Ana");
        return dados;
    }

    private String json(Map<String, Object> dados) throws Exception {
        return mapper.writeValueAsString(dados);
    }

    private Number criar(Map<String, Object> dados) throws Exception {
        String resposta = mvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(json(dados)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    @Test
    void criarConsultarEListarTurmaAtiva() throws Exception {
        String resposta = mvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(json(dados())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Projeto de Software"))
                .andExpect(jsonPath("$.codigo").value("P1"))
                .andExpect(jsonPath("$.periodoLetivo").value("2026.1"))
                .andExpect(jsonPath("$.professorResponsavel").value("Ana"))
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(resposta, "$.id");
        entityManager.flush();
        entityManager.clear();

        mvc.perform(get("/turmas/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto de Software"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
        mvc.perform(get("/turmas")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void camposObrigatoriosInvalidosNaCriacaoENaEdicao() throws Exception {
        Number id = criar(dados());
        for (String campo : dados().keySet()) {
            for (String invalido : new String[] {null, "", "   "}) {
                Map<String, Object> corpo = dados();
                corpo.put(campo, invalido);
                mvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                        .andExpect(status().isBadRequest());
                mvc.perform(put("/turmas/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                        .andExpect(status().isBadRequest());
            }
            Map<String, Object> corpo = dados();
            corpo.remove(campo);
            mvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/turmas/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(get("/turmas/{id}", id)).andExpect(jsonPath("$.nome").value("Projeto de Software"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void periodoLetivoExigeQuatroDigitosEPontoSeguidosDeUmOuDois() throws Exception {
        Number id = criar(dados());
        for (String invalido : new String[] {
                "2026", "2026.0", "2026.3", "2026.12", "2026-1", "2026x1",
                "26.1", "20266.1", "abcd.1", " 2026.1", "2026.1 ", "2026.1\n"
        }) {
            Map<String, Object> corpo = dados();
            corpo.put("nome", "Não deve ser salvo");
            corpo.put("periodoLetivo", invalido);
            mvc.perform(post("/turmas").contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                    .andExpect(status().isBadRequest());
            mvc.perform(put("/turmas/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                    .andExpect(status().isBadRequest());
        }
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/turmas/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto de Software"))
                .andExpect(jsonPath("$.periodoLetivo").value("2026.1"));
    }

    @Test
    void aceitaAmbosSemestresNaCriacaoENaEdicao() throws Exception {
        for (String periodo : new String[] {"2026.1", "2026.2"}) {
            Map<String, Object> corpo = dados();
            corpo.put("periodoLetivo", periodo);
            Number id = criar(corpo);
            corpo.put("periodoLetivo", periodo.equals("2026.1") ? "2026.2" : "2026.1");
            mvc.perform(put("/turmas/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(corpo)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.periodoLetivo").value(corpo.get("periodoLetivo")));
        }
    }

    @Test
    void operacoesEmTurmaInexistenteRetornam404() throws Exception {
        mvc.perform(get("/turmas/999999999")).andExpect(status().isNotFound());
        mvc.perform(put("/turmas/999999999").contentType(MediaType.APPLICATION_JSON).content(json(dados())))
                .andExpect(status().isNotFound());
        mvc.perform(patch("/turmas/999999999/encerrar")).andExpect(status().isNotFound());
    }

    @Test
    void editarAtualizaDadosSemPermitirAlteracaoDeStatusPeloPayload() throws Exception {
        Number id = criar(dados());
        Map<String, Object> alterados = dados();
        alterados.put("nome", "Engenharia de Software");
        alterados.put("codigo", "P2");
        alterados.put("periodoLetivo", "2026.2");
        alterados.put("professorResponsavel", "Bruno");
        alterados.put("status", "ENCERRADA");
        mvc.perform(put("/turmas/{id}", id).contentType(MediaType.APPLICATION_JSON).content(json(alterados)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ATIVA"));
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/turmas/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Engenharia de Software"))
                .andExpect(jsonPath("$.codigo").value("P2"))
                .andExpect(jsonPath("$.periodoLetivo").value("2026.2"))
                .andExpect(jsonPath("$.professorResponsavel").value("Bruno"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
    }

    @Test
    void encerrarEIdempotentePreservaOutrasTurmasERejeitaEditarEncerrada() throws Exception {
        Number encerrada = criar(dados());
        Number ativa = criar(dados()); // Códigos repetidos são permitidos.
        mvc.perform(patch("/turmas/{id}/encerrar", encerrada)).andExpect(status().isOk());
        mvc.perform(patch("/turmas/{id}/encerrar", encerrada)).andExpect(status().isOk());
        Map<String, Object> alterados = dados();
        alterados.put("nome", "Turma encerrada editada");
        alterados.put("codigo", "P2");
        alterados.put("periodoLetivo", "2026.2");
        alterados.put("professorResponsavel", "Bruno");
        alterados.put("status", "ATIVA");
        mvc.perform(put("/turmas/{id}", encerrada).contentType(MediaType.APPLICATION_JSON).content(json(alterados)))
                .andExpect(status().isBadRequest());
        entityManager.flush();
        entityManager.clear();
        mvc.perform(get("/turmas/{id}", encerrada)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto de Software"))
                .andExpect(jsonPath("$.codigo").value("P1"))
                .andExpect(jsonPath("$.periodoLetivo").value("2026.1"))
                .andExpect(jsonPath("$.professorResponsavel").value("Ana"))
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
        mvc.perform(get("/turmas/{id}", ativa)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Projeto de Software"))
                .andExpect(jsonPath("$.status").value("ATIVA"));
        mvc.perform(get("/turmas")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.status == 'ENCERRADA')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.status == 'ATIVA')]", hasSize(1)));
    }
}
