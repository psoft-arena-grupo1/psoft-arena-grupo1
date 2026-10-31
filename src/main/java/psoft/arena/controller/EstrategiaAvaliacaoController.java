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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.EstrategiaRequest;
import psoft.arena.dto.PromptRequest;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.service.EstrategiaAvaliacaoService;

@RestController
@RequestMapping("/estrategias")
@Tag(name = "Estratégias de Avaliação", description = "US03 - Cadastro de estratégias de avaliação")
public class EstrategiaAvaliacaoController {

    private final EstrategiaAvaliacaoService estrategiaService;

    public EstrategiaAvaliacaoController(EstrategiaAvaliacaoService estrategiaService) {
        this.estrategiaService = estrategiaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra uma estratégia de avaliação")
    public EstrategiaAvaliacao criar(@RequestBody EstrategiaRequest dados) {
        return estrategiaService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista as estratégias, opcionalmente filtrando por tipo")
    public List<EstrategiaAvaliacao> listar(@RequestParam(required = false) TipoEstrategia tipo) {
        return tipo == null ? estrategiaService.listar() : estrategiaService.listarPorTipo(tipo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma estratégia")
    public EstrategiaAvaliacao buscar(@PathVariable Long id) {
        return estrategiaService.buscar(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita nome, tipo, descrição e prompt de uma estratégia")
    public EstrategiaAvaliacao atualizar(@PathVariable Long id, @RequestBody EstrategiaRequest dados) {
        return estrategiaService.atualizar(id, dados);
    }

    @PatchMapping("/{id}/prompt")
    @Operation(summary = "Altera apenas o prompt de avaliação de uma estratégia")
    public EstrategiaAvaliacao alterarPrompt(@PathVariable Long id, @RequestBody PromptRequest dados) {
        return estrategiaService.alterarPrompt(id, dados.prompt());
    }
}
