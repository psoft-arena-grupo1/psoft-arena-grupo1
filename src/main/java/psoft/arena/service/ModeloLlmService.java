package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.ModeloLlmRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.ModeloLlm;
import psoft.arena.repository.ModeloLlmRepository;

@Service
@Transactional
public class ModeloLlmService {

    private final ModeloLlmRepository modeloLlmRepository;

    public ModeloLlmService(ModeloLlmRepository modeloLlmRepository) {
        this.modeloLlmRepository = modeloLlmRepository;
    }

    public ModeloLlm criar(ModeloLlmRequest dados) {
        if (dados == null || vazio(dados.nome()) || vazio(dados.urlApi()) || vazio(dados.chaveApi())) {
            throw new IllegalArgumentException("Nome do modelo, URL da API e chave de acesso são obrigatórios.");
        }
        return modeloLlmRepository.save(new ModeloLlm(dados.nome(), dados.urlApi(), dados.chaveApi()));
    }

    public ModeloLlm buscar(Long id) {
        return modeloLlmRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Modelo de LLM não encontrado."));
    }

    public List<ModeloLlm> listar() {
        return modeloLlmRepository.findAll();
    }
}
