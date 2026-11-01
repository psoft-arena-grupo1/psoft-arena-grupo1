package psoft.arena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.TurmaRequest;
import psoft.arena.model.Turma;
import psoft.arena.service.TurmaService;

@RestController
@RequestMapping("/turmas")
@Tag(name = "Turmas", description = "US04 - Cadastro de turmas")
public class TurmaController {
    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma turma ativa")
    public Turma criar(@RequestBody TurmaRequest dados) {
        return turmaService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista todas as turmas, inclusive encerradas")
    public List<Turma> listar() {
        return turmaService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma turma")
    public Turma buscar(@PathVariable Long id) {
        return turmaService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita uma turma sem alterar seu status")
    public Turma atualizar(@PathVariable Long id, @RequestBody TurmaRequest dados) {
        return turmaService.atualizar(id, dados);
    }

    @PatchMapping("/{id}/encerrar")
    @Operation(summary = "Encerra uma turma")
    public Turma encerrar(@PathVariable Long id) {
        return turmaService.encerrar(id);
    }
}
