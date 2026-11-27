package psoft.arena.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.ResultadoAvaliacaoResponse;
import psoft.arena.service.ResultadoAvaliacaoService;

@RestController
@RequestMapping("/estudantes/{matricula}/submissoes/{submissaoId}/resultado")
@Tag(name = "Resultados", description = "US11 - Consulta do resultado da avaliação")
public class ResultadoAvaliacaoController {

    private final ResultadoAvaliacaoService resultadoService;

    public ResultadoAvaliacaoController(ResultadoAvaliacaoService resultadoService) {
        this.resultadoService = resultadoService;
    }

    @GetMapping
    @Operation(summary = "Consulta o resultado da avaliação de uma submissão do estudante (somente após a avaliação ser concluída)")
    public ResultadoAvaliacaoResponse buscar(@PathVariable String matricula, @PathVariable Long submissaoId) {
        return resultadoService.buscarDoEstudante(matricula, submissaoId);
    }
}