package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.AtividadeRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.repository.AtividadeRepository;

@Service
@Transactional
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;
    private final ProblemaService problemaService;
    private final TurmaService turmaService;
    private final EstrategiaAvaliacaoService estrategiaService;
    private final ModeloLlmService modeloLlmService;

    public AtividadeService(AtividadeRepository atividadeRepository, ProblemaService problemaService,
            TurmaService turmaService, EstrategiaAvaliacaoService estrategiaService, ModeloLlmService modeloLlmService) {
        this.atividadeRepository = atividadeRepository;
        this.problemaService = problemaService;
        this.turmaService = turmaService;
        this.estrategiaService = estrategiaService;
        this.modeloLlmService = modeloLlmService;
    }

    public Atividade criar(AtividadeRequest dados) {
        validarCampos(dados);
        return atividadeRepository.save(new Atividade(
                problemaService.buscar(dados.problemaId()),
                turmaService.buscar(dados.turmaId()),
                estrategiaService.buscar(dados.estrategiaId()),
                modeloLlmService.buscar(dados.llmPadraoId()),
                dados.titulo(), dados.descricao(), dados.prazo()));
    }

    public Atividade buscar(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
    }

    public List<Atividade> listar() {
        return atividadeRepository.findAll();
    }

    public Atividade encerrar(Long id) {
        Atividade atividade = buscar(id);
        atividade.encerrar();
        return atividade;
    }

    private void validarCampos(AtividadeRequest dados) {
        if (dados == null || dados.problemaId() == null || dados.turmaId() == null || dados.estrategiaId() == null
                || dados.llmPadraoId() == null) {
            throw new IllegalArgumentException("Turma, problema, estratégia de avaliação e LLM padrão são obrigatórios.");
        }
        if (vazio(dados.titulo()) || vazio(dados.descricao()) || dados.prazo() == null) {
            throw new IllegalArgumentException("Título, descrição e prazo para submissão são obrigatórios.");
        }
    }
}
