# psoft-arena-grupo1

API REST do PSoft Arena (Spring Boot + H2 em memória).

## Rodar

```
./mvnw spring-boot:run
```

Swagger: http://localhost:8080/swagger-ui.html

## Testes

```
./mvnw test
```

## Pacotes

- `model`: entidades de domínio
- `repository`: acesso a dados (Spring Data JPA)
- `service`: regras de negócio
- `controller`: endpoints REST
- `dto`: corpos das requisições
- `exception`: erros e mapeamento para status HTTP
