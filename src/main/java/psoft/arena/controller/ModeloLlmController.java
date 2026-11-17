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
import psoft.arena.dto.ModeloLlmRequest;
import psoft.arena.model.ModeloLlm;
import psoft.arena.service.ModeloLlmService;

@RestController
@RequestMapping("/llms")
@Tag(name = "Modelos de LLM", description = "US06 - Cadastro do LLM padrão das atividades")
public class ModeloLlmController {

    private final ModeloLlmService modeloLlmService;

    public ModeloLlmController(ModeloLlmService modeloLlmService) {
        this.modeloLlmService = modeloLlmService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um modelo de LLM; a chave da API nunca é retornada")
    public ModeloLlm criar(@RequestBody ModeloLlmRequest dados) {
        return modeloLlmService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista os modelos de LLM")
    public List<ModeloLlm> listar() {
        return modeloLlmService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um modelo de LLM")
    public ModeloLlm buscar(@PathVariable Long id) {
        return modeloLlmService.buscar(id);
    }
}
