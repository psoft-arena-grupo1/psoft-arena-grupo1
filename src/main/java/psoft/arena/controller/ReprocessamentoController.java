package psoft.arena.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.AcompanhamentoSubmissaoResponse;
import psoft.arena.service.SubmissaoService;

@RestController
@RequestMapping("/submissoes/{submissaoId}/reprocessamento")
@Tag(name = "Reprocessamento", description = "US12 - Reprocessamento de submissões com falha")
public class ReprocessamentoController {

    private final SubmissaoService submissaoService;

    public ReprocessamentoController(SubmissaoService submissaoService) {
        this.submissaoService = submissaoService;
    }

    @PostMapping
    @Operation(summary = "Reprocessa uma submissão com status Falha: ela volta para Em fila com o mesmo código-fonte")
    public AcompanhamentoSubmissaoResponse reprocessar(@PathVariable Long submissaoId) {
        return submissaoService.reprocessar(submissaoId);
    }
}
