package psoft.arena;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "PSoft Arena API", version = "1.0",
        description = "Submissão e avaliação de soluções de problemas de projeto de software orientado a objetos."))
public class PsoftArenaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PsoftArenaApplication.class, args);
    }
}
