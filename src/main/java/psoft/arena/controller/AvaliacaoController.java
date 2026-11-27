package psoft.arena.controller;

import java.util.List;

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
    @Operation(summary = "Consulta a avaliação automática mais recente de uma submissão")
    public AvaliacaoResponse buscar(@PathVariable Long submissaoId) {
        return avaliacaoService.buscarPorSubmissao(submissaoId);
    }

    @GetMapping("/tentativas")
    @Operation(summary = "US12 - Lista todas as tentativas de avaliação da submissão, da mais antiga para a mais recente")
    public List<AvaliacaoResponse> listarTentativas(@PathVariable Long submissaoId) {
        return avaliacaoService.listarTentativas(submissaoId);
    }
}
