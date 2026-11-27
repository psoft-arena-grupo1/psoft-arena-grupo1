package psoft.arena.config;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import psoft.arena.exception.AcessoNegadoException;
import psoft.arena.exception.NaoAutenticadoException;

@Component
public class ControleAcessoEstudante implements HandlerInterceptor {

    public static final String CABECALHO = "X-Matricula";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        @SuppressWarnings("unchecked")
        Map<String, String> variaveis = (Map<String, String>) request
                .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (variaveis != null && variaveis.containsKey("matricula")) {
            verificar(request.getHeader(CABECALHO), variaveis.get("matricula"));
        }
        return true;
    }

    public static void verificar(String matriculaAutenticada, String matriculaAlvo) {
        if (matriculaAutenticada == null || matriculaAutenticada.isBlank()) {
            throw new NaoAutenticadoException(
                    "Identifique o estudante autenticado pelo cabeçalho " + CABECALHO + ".");
        }
        if (matriculaAlvo == null || !matriculaAutenticada.strip().equals(matriculaAlvo.strip())) {
            throw new AcessoNegadoException("O estudante só pode acessar os próprios recursos.");
        }
    }
}
