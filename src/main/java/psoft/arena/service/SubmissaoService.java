package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.AcompanhamentoSubmissaoResponse;
import psoft.arena.dto.SubmissaoRequest;
import psoft.arena.exception.AcessoNegadoException;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.model.Estudante;
import psoft.arena.model.Submissao;
import psoft.arena.repository.SubmissaoRepository;

@Service
@Transactional
public class SubmissaoService {

    private final SubmissaoRepository submissaoRepository;
    private final AtividadeService atividadeService;
    private final EstudanteService estudanteService;
    private final ApplicationEventPublisher eventos;
    private final NotificadorSubmissao notificador;

    public SubmissaoService(SubmissaoRepository submissaoRepository, AtividadeService atividadeService,
            EstudanteService estudanteService, ApplicationEventPublisher eventos, NotificadorSubmissao notificador) {
        this.submissaoRepository = submissaoRepository;
        this.atividadeService = atividadeService;
        this.estudanteService = estudanteService;
        this.eventos = eventos;
        this.notificador = notificador;
    }

    public Submissao submeter(Long atividadeId, SubmissaoRequest dados) {
        if (dados == null || vazio(dados.matricula())) {
            throw new IllegalArgumentException("A matrícula do estudante é obrigatória.");
        }
        Atividade atividade = atividadeService.buscar(atividadeId);
        Estudante estudante = estudanteService.buscar(dados.matricula());
        if (!atividade.visivelPara(estudante)) {
            throw new AcessoNegadoException("Somente estudantes da turma da atividade podem realizar submissões.");
        }
        int versao = (int) submissaoRepository
                .countByEstudanteMatriculaAndAtividadeId(estudante.getMatricula(), atividadeId) + 1;
        Submissao submissao = submissaoRepository
                .save(new Submissao(estudante, atividade, dados.codigoFonte(), versao));
        eventos.publishEvent(new SubmissaoCriadaEvent(submissao.getId()));
        return submissao;
    }

    @Transactional(readOnly = true)
    public List<AcompanhamentoSubmissaoResponse> listarDoEstudante(String matricula) {
        Estudante estudante = estudanteService.buscar(matricula);
        return submissaoRepository.findByEstudanteMatriculaOrderByDataHoraDesc(estudante.getMatricula())
                .stream().map(AcompanhamentoSubmissaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public AcompanhamentoSubmissaoResponse buscarDoEstudante(String matricula, Long id) {
        Estudante estudante = estudanteService.buscar(matricula);
        return submissaoRepository.findByIdAndEstudanteMatricula(id, estudante.getMatricula())
                .map(AcompanhamentoSubmissaoResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Submissão não encontrada."));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void encaminhar(Long id) {
        notificador.notificar(buscar(id).encaminhar());
    }

    public Submissao iniciarProcessamento(Long id) {
        Submissao submissao = buscar(id);
        notificador.notificar(submissao.iniciarProcessamento());
        return submissao;
    }

    public void concluir(Long id) {
        notificador.notificar(buscar(id).concluir());
    }

    public void falhar(Long id) {
        notificador.notificar(buscar(id).falhar());
    }

    private Submissao buscar(Long id) {
        return submissaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Submissão não encontrada."));
    }
}