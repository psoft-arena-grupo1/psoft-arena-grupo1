package psoft.arena.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.AvaliacaoResponse;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Avaliacao;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.ResultadoAvaliacaoModelo;
import psoft.arena.model.StatusAvaliacao;
import psoft.arena.model.Submissao;
import psoft.arena.repository.AvaliacaoRepository;
import psoft.arena.repository.ModeloLlmRepository;
import psoft.arena.repository.SubmissaoRepository;

@Service
@Transactional
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final SubmissaoRepository submissaoRepository;
    private final ModeloLlmRepository modeloLlmRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, SubmissaoRepository submissaoRepository,
            ModeloLlmRepository modeloLlmRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.submissaoRepository = submissaoRepository;
        this.modeloLlmRepository = modeloLlmRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Avaliacao iniciar(Long submissaoId) {
        Submissao submissao = submissaoRepository.findById(submissaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Submissão não encontrada."));
        if (avaliacaoRepository.existsBySubmissaoIdAndStatus(submissaoId, StatusAvaliacao.PROCESSANDO)) {
            throw new IllegalStateException("Esta submissão já possui avaliação em andamento.");
        }
        return avaliacaoRepository.save(new Avaliacao(submissao));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarResultado(Long avaliacaoId, Long modeloId, BigDecimal nota, String evidencias,
            String justificativa) {
        Avaliacao avaliacao = buscar(avaliacaoId);
        ModeloLlm modelo = buscarModelo(modeloId);
        avaliacao.adicionarResultado(new ResultadoAvaliacaoModelo(avaliacao, modelo, nota, evidencias, justificativa));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalhaModelo(Long avaliacaoId, Long modeloId, String motivo) {
        Avaliacao avaliacao = buscar(avaliacaoId);
        ModeloLlm modelo = buscarModelo(modeloId);
        avaliacao.adicionarResultado(new ResultadoAvaliacaoModelo(avaliacao, modelo, motivo));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void concluir(Long avaliacaoId, BigDecimal nota, String evidencias, String justificativa) {
        buscar(avaliacaoId).concluir(nota, evidencias, justificativa);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void falhar(Long avaliacaoId, String motivo) {
        buscar(avaliacaoId).falhar(motivo);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPorSubmissao(Long submissaoId) {
        return avaliacaoRepository.findBySubmissaoId(submissaoId)
                .map(AvaliacaoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarTentativas(Long submissaoId) {
        if (!submissaoRepository.existsById(submissaoId)) {
            throw new RecursoNaoEncontradoException("Submissão não encontrada.");
        }
        return avaliacaoRepository.findBySubmissaoIdOrderByIdAsc(submissaoId).stream()
                .map(AvaliacaoResponse::de)
                .toList();
    }

    private Avaliacao buscar(Long avaliacaoId) {
        return avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada."));
    }

    private ModeloLlm buscarModelo(Long modeloId) {
        return modeloLlmRepository.findById(modeloId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Modelo de LLM não encontrado."));
    }
}
