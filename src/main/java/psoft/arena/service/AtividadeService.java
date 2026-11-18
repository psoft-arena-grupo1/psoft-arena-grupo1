package psoft.arena.service;

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

    public AtividadeService(AtividadeRepository atividadeRepository, ProblemaService problemaService, TurmaService turmaService) {
        this.atividadeRepository = atividadeRepository;
        this.problemaService = problemaService;
        this.turmaService = turmaService;
    }

    public Atividade criar(AtividadeRequest dados) {
        if (dados.problemaId() == null) {
            throw new IllegalArgumentException("O problema é obrigatório.");
        }
        if (dados.turmaId() == null) {
            throw new IllegalArgumentException("A turma é obrigatório.");
        }
        return atividadeRepository.save(new Atividade(problemaService.buscar(dados.problemaId()),
            turmaService.buscar(dados.turmaId())));
    }

    public Atividade buscar(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atividade não encontrada."));
    }

    public List<Atividade> listar() {
        return atividadeRepository.findAll();
    }

}
