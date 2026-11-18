package psoft.arena.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RespostaAvaliacaoLlmParser {

    private static final String ERRO_EVIDENCIAS_AUSENTES =
            "O campo 'evidencias' deve conter ao menos uma evidência textual.";

    private final ObjectMapper objectMapper;

    public RespostaAvaliacaoLlmParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public RespostaAvaliacaoLlm parse(String conteudo) {
        if (conteudo == null || conteudo.isBlank()) {
            throw new IllegalArgumentException("A resposta do modelo de LLM está vazia.");
        }
        JsonNode raiz = lerJson(extrairObjeto(conteudo));
        if (!raiz.isObject()) {
            throw new IllegalArgumentException("A resposta do modelo de LLM deve ser um objeto JSON.");
        }
        BigDecimal nota = lerNota(raiz.get("nota"));
        List<String> evidencias = lerEvidencias(raiz.get("evidencias"));
        String justificativa = lerJustificativa(raiz.get("justificativa"));
        return new RespostaAvaliacaoLlm(nota, evidencias, justificativa);
    }

    private String extrairObjeto(String conteudo) {
        int inicio = conteudo.indexOf('{');
        int fim = conteudo.lastIndexOf('}');
        if (inicio < 0 || fim <= inicio) {
            throw new IllegalArgumentException("A resposta do modelo de LLM não contém um objeto JSON válido.");
        }
        return conteudo.substring(inicio, fim + 1);
    }

    private JsonNode lerJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("A resposta do modelo de LLM não pôde ser interpretada como JSON.", e);
        }
    }

    private BigDecimal lerNota(JsonNode no) {
        if (no == null || no.isNull() || !no.isNumber()) {
            throw new IllegalArgumentException("O campo 'nota' deve ser um número entre 0 e 10.");
        }
        BigDecimal nota = no.decimalValue();
        if (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("O campo 'nota' deve estar entre 0 e 10.");
        }
        return nota;
    }

    private List<String> lerEvidencias(JsonNode no) {
        if (no == null || no.isNull()) {
            throw new IllegalArgumentException(ERRO_EVIDENCIAS_AUSENTES);
        }
        if (no.isTextual()) {
            String evidencia = no.asText().trim();
            if (evidencia.isEmpty()) {
                throw new IllegalArgumentException(ERRO_EVIDENCIAS_AUSENTES);
            }
            return Collections.singletonList(evidencia);
        }
        if (!no.isArray()) {
            throw new IllegalArgumentException("O campo 'evidencias' deve ser uma lista de textos ou um texto.");
        }
        if (no.isEmpty()) {
            throw new IllegalArgumentException(ERRO_EVIDENCIAS_AUSENTES);
        }
        List<String> evidencias = new ArrayList<>();
        for (JsonNode item : no) {
            if (item == null || !item.isTextual() || item.asText().isBlank()) {
                throw new IllegalArgumentException("O campo 'evidencias' deve conter apenas textos não vazios.");
            }
            evidencias.add(item.asText().trim());
        }
        return evidencias;
    }

    private String lerJustificativa(JsonNode no) {
        if (no == null || no.isNull() || !no.isTextual() || no.asText().isBlank()) {
            throw new IllegalArgumentException("O campo 'justificativa' deve ser um texto não vazio.");
        }
        return no.asText().trim();
    }
}
