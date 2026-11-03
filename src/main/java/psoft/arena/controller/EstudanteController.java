package psoft.arena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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
import psoft.arena.dto.EstudanteRequest;
import psoft.arena.dto.EstudanteUpdateRequest;
import psoft.arena.model.Estudante;
import psoft.arena.service.EstudanteService;

@RestController
@RequestMapping("/estudantes")
@Tag(name = "Estudantes", description = "US02 - Cadastro de estudantes")
public class EstudanteController {

    private final EstudanteService estudanteService;

    public EstudanteController(EstudanteService estudanteService) {
        this.estudanteService = estudanteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um estudante (matrícula única)")
    public Estudante criar(@RequestBody EstudanteRequest dados) {
        return estudanteService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista os estudantes, opcionalmente filtrando por trecho do nome")
    public List<Estudante> listar(@RequestParam(required = false) String nome) {
        return nome == null ? estudanteService.listar() : estudanteService.buscarPorNome(nome);
    }

    @GetMapping("/{matricula}")
    @Operation(summary = "Consulta um estudante pela matrícula")
    public Estudante buscar(@PathVariable String matricula) {
        return estudanteService.buscar(matricula);
    }

    @PutMapping("/{matricula}")
    @Operation(summary = "Edita nome e e-mail de um estudante")
    public Estudante atualizar(@PathVariable String matricula, @RequestBody EstudanteUpdateRequest dados) {
        return estudanteService.atualizar(matricula, dados);
    }
}
