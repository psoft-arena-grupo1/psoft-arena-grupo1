package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.EstudanteRequest;
import psoft.arena.dto.EstudanteUpdateRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Estudante;
import psoft.arena.repository.EstudanteRepository;

@Service
@Transactional
public class EstudanteService {

    private final EstudanteRepository estudanteRepository;

    public EstudanteService(EstudanteRepository estudanteRepository) {
        this.estudanteRepository = estudanteRepository;
    }

    public Estudante criar(EstudanteRequest dados) {
        validarCampos(dados.nome(), dados.matricula(), dados.email());
        String chave = dados.matricula().strip();
        if (estudanteRepository.existsById(chave)) {
            throw new IllegalArgumentException("Já existe um estudante com essa matrícula.");
        }
        return estudanteRepository.save(new Estudante(dados.nome(), chave, dados.email()));
    }

    public Estudante buscar(String matricula) {
        if (matricula == null) {
            throw new RecursoNaoEncontradoException("Estudante não encontrado.");
        }
        return estudanteRepository.findById(matricula.strip())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estudante não encontrado."));
    }

    public List<Estudante> listar() {
        return estudanteRepository.findAll();
    }

    public List<Estudante> buscarPorNome(String nome) {
        return estudanteRepository.findByNomeContainingIgnoreCase(nome);
    }

    public Estudante atualizar(String matricula, EstudanteUpdateRequest dados) {
        validarCampos(dados.nome(), matricula, dados.email());
        Estudante current = buscar(matricula);
        current.setNome(dados.nome());
        current.setEmail(dados.email());
        return current;
    }

    private void validarCampos(String nome, String matricula, String email) {
        if (vazio(nome) || vazio(matricula) || vazio(email)) {
            throw new IllegalArgumentException("Nome, matrícula e e-mail são obrigatórios.");
        }
        if (!email.strip().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
    }
}
