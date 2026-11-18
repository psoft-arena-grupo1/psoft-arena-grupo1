package psoft.arena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.ProblemaRequest;
import psoft.arena.model.Problema;
import psoft.arena.service.ProblemaService;

@RestController
@RequestMapping("/problemas")
@Tag(name = "Problemas", description = "US01 - Cadastro de problemas de projeto de software")
public class ProblemaController {

    private final ProblemaService problemaService;

    public ProblemaController(ProblemaService problemaService) {
        this.problemaService = problemaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um problema (criado com status Ativo)")
    public Problema criar(@RequestBody ProblemaRequest dados) {
        return problemaService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista os problemas, opcionalmente filtrando por trecho do título")
    public List<Problema> listar(@RequestParam(required = false) String titulo) {
        return titulo == null ? problemaService.listar() : problemaService.buscarPorTitulo(titulo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um problema")
    public Problema buscar(@PathVariable Long id) {
        return problemaService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita um problema")
    public Problema atualizar(@PathVariable Long id, @RequestBody ProblemaRequest dados) {
        return problemaService.atualizar(id, dados);
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativa um problema (status Inativo)")
    public Problema desativar(@PathVariable Long id) {
        return problemaService.desativar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um problema ainda não utilizado em atividades")
    public void remover(@PathVariable Long id) {
        problemaService.remover(id);
    }
}
