package psoft.arena.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.ResultadoAvaliacaoResponse;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Avaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.StatusAvaliacao;
import psoft.arena.model.Submissao;
import psoft.arena.repository.AvaliacaoRepository;
import psoft.arena.repository.SubmissaoRepository;

@Service
@Transactional(readOnly = true)
public class ResultadoAvaliacaoService {

    private final EstudanteService estudanteService;
    private final SubmissaoRepository submissaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    public ResultadoAvaliacaoService(EstudanteService estudanteService, SubmissaoRepository submissaoRepository,
            AvaliacaoRepository avaliacaoRepository) {
        this.estudanteService = estudanteService;
        this.submissaoRepository = submissaoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public ResultadoAvaliacaoResponse buscarDoEstudante(String matricula, Long submissaoId) {
        Estudante estudante = estudanteService.buscar(matricula);
        Submissao submissao = submissaoRepository
                .findByIdAndEstudanteMatricula(submissaoId, estudante.getMatricula())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Submissão não encontrada."));
        Avaliacao avaliacao = avaliacaoRepository.findBySubmissaoId(submissao.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "O resultado da avaliação ainda não está disponível."));
        if (!avaliacao.resultadoDisponivel()) {
            throw new RecursoNaoEncontradoException(mensagemIndisponivel(avaliacao));
        }
        return ResultadoAvaliacaoResponse.de(avaliacao);
    }

    private static String mensagemIndisponivel(Avaliacao avaliacao) {
        if (avaliacao.getStatus() == StatusAvaliacao.FALHA) {
            return "A avaliação desta submissão não foi concluída; o resultado não está disponível.";
        }
        return "O resultado da avaliação ainda não está disponível.";
    }
}