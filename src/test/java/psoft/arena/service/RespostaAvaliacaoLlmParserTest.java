package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RespostaAvaliacaoLlmParserTest {

    private RespostaAvaliacaoLlmParser parser;

    @BeforeEach
    void setUp() {
        parser = new RespostaAvaliacaoLlmParser(new ObjectMapper());
    }

    @Test
    void interpretaJsonPuroValido() {
        RespostaAvaliacaoLlm resposta = parser.parse(
                "{\"nota\": 8.5, \"evidencias\": [\"e1\", \"e2\"], \"justificativa\": \"Boa solução\"}");

        assertEquals(0, resposta.nota().compareTo(new BigDecimal("8.5")));
        assertEquals(2, resposta.evidencias().size());
        assertEquals("e1", resposta.evidencias().get(0));
        assertEquals("e2", resposta.evidencias().get(1));
        assertEquals("Boa solução", resposta.justificativa());
    }

    @Test
    void interpretaJsonDentroDeCercaDeCodigo() {
        String conteudo = "```json\n{\"nota\": 7, \"evidencias\": [\"e1\"], \"justificativa\": \"ok\"}\n```";

        RespostaAvaliacaoLlm resposta = parser.parse(conteudo);

        assertEquals(0, resposta.nota().compareTo(new BigDecimal("7")));
        assertEquals(1, resposta.evidencias().size());
        assertEquals("e1", resposta.evidencias().get(0));
        assertEquals("ok", resposta.justificativa());
    }

    @Test
    void aceitaEvidenciasComoTextoUnico() {
        RespostaAvaliacaoLlm resposta = parser.parse(
                "{\"nota\": 5, \"evidencias\": \"evidência única\", \"justificativa\": \"j\"}");

        assertEquals(1, resposta.evidencias().size());
        assertEquals("evidência única", resposta.evidencias().get(0));
    }

    @Test
    void rejeitaArrayDeEvidenciasVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{\"nota\": 8, \"evidencias\": [], \"justificativa\": \"j\"}"));
    }

    @Test
    void rejeitaNotaAcimaDeDez() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{\"nota\": 11, \"evidencias\": [\"e\"], \"justificativa\": \"j\"}"));
    }

    @Test
    void rejeitaNotaNegativa() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{\"nota\": -0.5, \"evidencias\": [\"e\"], \"justificativa\": \"j\"}"));
    }

    @Test
    void rejeitaNotaAusente() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{\"evidencias\": [\"e\"], \"justificativa\": \"j\"}"));
    }

    @Test
    void rejeitaJustificativaVazia() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{\"nota\": 8, \"evidencias\": [\"e\"], \"justificativa\": \"  \"}"));
    }

    @Test
    void rejeitaJustificativaAusente() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("{\"nota\": 8, \"evidencias\": [\"e\"]}"));
    }

    @Test
    void rejeitaTextoSemJson() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("Não sei responder essa avaliação."));
    }
}
