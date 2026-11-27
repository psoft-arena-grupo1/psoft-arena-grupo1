package psoft.arena.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.config.ControleAcessoEstudante;
import psoft.arena.dto.SubmissaoRequest;
import psoft.arena.dto.SubmissaoResponse;
import psoft.arena.service.SubmissaoService;

@RestController
@RequestMapping("/atividades/{atividadeId}/submissoes")
@Tag(name = "Submissões", description = "US08 - Submissão de soluções")
public class SubmissaoController {

    private final SubmissaoService submissaoService;

    public SubmissaoController(SubmissaoService submissaoService) {
        this.submissaoService = submissaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submete uma solução em Java para uma atividade aberta; cada envio gera uma nova versão")
    public SubmissaoResponse submeter(@PathVariable Long atividadeId, @RequestBody SubmissaoRequest dados,
            @RequestHeader(name = ControleAcessoEstudante.CABECALHO, required = false) String matriculaAutenticada) {
        if (dados != null && dados.matricula() != null && !dados.matricula().isBlank()) {
            ControleAcessoEstudante.verificar(matriculaAutenticada, dados.matricula());
        }
        return SubmissaoResponse.de(submissaoService.submeter(atividadeId, dados));
    }
}