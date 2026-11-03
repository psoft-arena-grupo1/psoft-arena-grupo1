package psoft.arena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.AtividadeRequest;
import psoft.arena.model.Atividade;
import psoft.arena.service.AtividadeService;

@RestController
@RequestMapping("/atividades")
@Tag(name = "Atividades", description = "Versão mínima, usada pelas regras da US01; completada na US06")
public class AtividadeController {

    private final AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma atividade para um problema ativo")
    public Atividade criar(@RequestBody AtividadeRequest dados) {
        return atividadeService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista as atividades")
    public List<Atividade> listar() {
        return atividadeService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma atividade")
    public Atividade buscar(@PathVariable Long id) {
        return atividadeService.buscar(id);
    }
}
