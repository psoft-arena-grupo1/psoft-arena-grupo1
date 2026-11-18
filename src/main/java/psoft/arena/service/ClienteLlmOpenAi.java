package psoft.arena.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import psoft.arena.model.ModeloLlm;

@Component
public class ClienteLlmOpenAi implements ClienteLlm {

    private static final String CAMINHO_COMPLETIONS = "chat/completions";

    private final RestClient.Builder restClientBuilder;
    private final RespostaAvaliacaoLlmParser parser;
    private final ObjectMapper objectMapper;

    public ClienteLlmOpenAi(RestClient.Builder restClientBuilder, RespostaAvaliacaoLlmParser parser,
            ObjectMapper objectMapper) {
        this.restClientBuilder = restClientBuilder;
        this.parser = parser;
        this.objectMapper = objectMapper;
    }

    @Override
    public RespostaAvaliacaoLlm avaliar(ModeloLlm modelo, String prompt) {
        try {
            RestClient cliente = restClientBuilder.clone()
                    .baseUrl(modelo.getUrlApi().replaceAll("/+$", ""))
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + modelo.getChaveApi())
                    .build();
            String corpoResposta = cliente.post()
                    .uri(CAMINHO_COMPLETIONS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpoRequisicao(modelo, prompt))
                    .retrieve()
                    .body(String.class);
            return parser.parse(extrairConteudo(corpoResposta, modelo));
        } catch (FalhaAvaliacaoException e) {
            throw e;
        } catch (RestClientResponseException e) {
            throw new FalhaAvaliacaoException("O modelo " + nome(modelo) + " respondeu com erro HTTP "
                    + e.getStatusCode().value() + ".", e);
        } catch (ResourceAccessException e) {
            throw new FalhaAvaliacaoException("Não foi possível acessar o modelo " + nome(modelo) + ".", e);
        } catch (RuntimeException e) {
            throw new FalhaAvaliacaoException("Falha ao avaliar com o modelo " + nome(modelo) + ": "
                    + e.getMessage(), e);
        }
    }

    private static Map<String, Object> corpoRequisicao(ModeloLlm modelo, String prompt) {
        return Map.of(
                "model", modelo.getNome(),
                "temperature", 0,
                "messages", List.of(Map.of("role", "user", "content", prompt)));
    }

    private String extrairConteudo(String corpoResposta, ModeloLlm modelo) {
        if (corpoResposta == null || corpoResposta.isBlank()) {
            throw new FalhaAvaliacaoException("O modelo " + nome(modelo) + " retornou uma resposta vazia.");
        }
        try {
            JsonNode conteudo = objectMapper.readTree(corpoResposta)
                    .path("choices").path(0).path("message").path("content");
            if (conteudo.isMissingNode() || conteudo.isNull() || conteudo.asText().isBlank()) {
                throw new FalhaAvaliacaoException("O modelo " + nome(modelo)
                        + " retornou uma resposta sem conteúdo de avaliação.");
            }
            return conteudo.asText();
        } catch (FalhaAvaliacaoException e) {
            throw e;
        } catch (Exception e) {
            throw new FalhaAvaliacaoException("A resposta do modelo " + nome(modelo)
                    + " não pôde ser interpretada como JSON.", e);
        }
    }

    private static String nome(ModeloLlm modelo) {
        return modelo == null || modelo.getNome() == null ? "desconhecido" : modelo.getNome();
    }
}
