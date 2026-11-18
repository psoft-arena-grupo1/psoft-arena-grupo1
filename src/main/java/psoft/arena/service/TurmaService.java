package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.TurmaRequest;
import psoft.arena.dto.MatriculaRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.StatusTurma;
import psoft.arena.model.Turma;
import psoft.arena.model.Estudante;
import psoft.arena.repository.TurmaRepository;

@Service
@Transactional
public class TurmaService {
    private final TurmaRepository turmaRepository;
    private final EstudanteService estudanteService;

    public TurmaService(TurmaRepository turmaRepository, EstudanteService estudanteService) {
        this.turmaRepository = turmaRepository;
        this.estudanteService = estudanteService;
    }

    public Turma criar(TurmaRequest dados) {
        validarCampos(dados);
        return turmaRepository.save(new Turma(dados.nome(), dados.codigo(), dados.periodoLetivo(),
                dados.professorResponsavel()));
    }

    public List<Turma> listar() {
        return turmaRepository.findAll();
    }

    public Turma buscar(Long id) {
        return turmaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Turma não encontrada."));
    }

    public Turma atualizar(Long id, TurmaRequest dados) {
        validarCampos(dados);
        Turma turma = buscar(id);
        if (turma.getStatus() == StatusTurma.ENCERRADA) {
            throw new IllegalArgumentException("Não é possível editar uma turma encerrada.");
        }
        turma.setNome(dados.nome());
        turma.setCodigo(dados.codigo());
        turma.setPeriodoLetivo(dados.periodoLetivo());
        turma.setProfessorResponsavel(dados.professorResponsavel());
        return turma;
    }

    public Turma encerrar(Long id) {
        Turma turma = buscar(id);
        turma.encerrar();
        return turma;
    }

    private void validarCampos(TurmaRequest dados) {
        if (dados == null || vazio(dados.nome()) || vazio(dados.codigo()) || vazio(dados.periodoLetivo())
                || vazio(dados.professorResponsavel())) {
            throw new IllegalArgumentException("Nome, código, período letivo e professor responsável são obrigatórios.");
        }
        if (!dados.periodoLetivo().matches("[0-9]{4}\\.[12]")) {
            throw new IllegalArgumentException("Período letivo deve estar no formato AAAA.1 ou AAAA.2.");
        }
    }

    public Turma adicionarEstudante(Long id, MatriculaRequest dados) {
        if (dados == null || vazio(dados.matricula())) {
            throw new IllegalArgumentException("Insira a matrícula.");
        }
        Turma turma = buscar(id);
        turma.adicionarEstudante(estudanteService.buscar(dados.matricula()));
        return turma;
    }

    public Turma removeEstudante(Long id, String matricula) {
        Turma turma = buscar(id);
        turma.removerEstudante(matricula.strip());
        return turma;
    }

    public List<Estudante> listarEstudantes(Long id) {
        return buscar(id).getEstudantes();
    }

}
