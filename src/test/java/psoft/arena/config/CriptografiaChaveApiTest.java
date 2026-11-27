package psoft.arena.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import psoft.arena.model.ModeloLlm;
import psoft.arena.repository.ModeloLlmRepository;

@SpringBootTest
@Transactional
class CriptografiaChaveApiTest {

    private static final String CHAVE_TESTE = "O8PlXWgQj4SHECDvqmIKsEnc/6w+nJCIf9iW7TWq7vg=";

    @Autowired
    private ModeloLlmRepository modeloLlmRepository;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CifradorChaveApi cifrador;

    @Test
    void chaveDaApiEhGravadaCifradaERecuperadaEmTextoPlano() {
        Long id = modeloLlmRepository.save(new ModeloLlm("grok-3", "https://api.x.ai/v1", "segredo-123")).getId();
        entityManager.flush();
        entityManager.clear();

        String armazenada = jdbc.queryForObject("select chave_api from modelo_llm where id = ?", String.class, id);
        assertFalse(armazenada.contains("segredo-123"));
        assertEquals("segredo-123", cifrador.decifrar(armazenada));
        assertEquals("segredo-123", modeloLlmRepository.findById(id).orElseThrow().getChaveApi());
    }

    @Test
    void cifrarMesmaChaveDuasVezesGeraValoresDiferentes() {
        assertNotEquals(cifrador.cifrar("segredo"), cifrador.cifrar("segredo"));
    }

    @Test
    void valorAdulteradoNaoEhDecifrado() {
        byte[] bytes = Base64.getDecoder().decode(cifrador.cifrar("segredo"));
        bytes[bytes.length - 1] ^= 1;
        String adulterado = Base64.getEncoder().encodeToString(bytes);
        assertThrows(IllegalStateException.class, () -> cifrador.decifrar(adulterado));
    }

    @Test
    void chaveDeCriptografiaComTamanhoInvalidoEhRejeitada() {
        assertThrows(IllegalStateException.class, () -> new CifradorChaveApi("c2VncmVkbw=="));
        assertEquals("x", new CifradorChaveApi(CHAVE_TESTE).decifrar(new CifradorChaveApi(CHAVE_TESTE).cifrar("x")));
    }
}
