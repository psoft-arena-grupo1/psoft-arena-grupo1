package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.SubmissaoRequest;
import psoft.arena.exception.AcessoNegadoException;
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

    public SubmissaoService(SubmissaoRepository submissaoRepository, AtividadeService atividadeService,
            EstudanteService estudanteService) {
        this.submissaoRepository = submissaoRepository;
        this.atividadeService = atividadeService;
        this.estudanteService = estudanteService;
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
        return submissaoRepository.save(new Submissao(estudante, atividade, dados.codigoFonte(), versao));
    }
}