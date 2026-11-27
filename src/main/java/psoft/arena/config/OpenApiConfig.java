package psoft.arena.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer cabecalhoEstudanteAutenticado() {
        return openApi -> openApi.getPaths().forEach((caminho, item) -> {
            if (caminho.matches("/estudantes/\\{matricula}/(atividades|submissoes).*")) {
                item.readOperations().forEach(operacao -> operacao.addParametersItem(new HeaderParameter()
                        .name(ControleAcessoEstudante.CABECALHO)
                        .required(true)
                        .description("Matrícula do estudante autenticado; deve ser igual à matrícula do caminho.")
                        .schema(new StringSchema())));
            }
        });
    }
}
