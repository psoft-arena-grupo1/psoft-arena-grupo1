package psoft.arena.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.AvaliacaoResponse;
import psoft.arena.service.AvaliacaoService;

@RestController
@RequestMapping("/submissoes/{submissaoId}/avaliacao")
@Tag(name = "Avaliações", description = "US10 - Avaliação automática de soluções")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    @Operation(summary = "Consulta a avaliação automática de uma submissão")
    public AvaliacaoResponse buscar(@PathVariable Long submissaoId) {
        return avaliacaoService.buscarPorSubmissao(submissaoId);
    }
}
