package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.ProblemaRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Problema;
import psoft.arena.repository.AtividadeRepository;
import psoft.arena.repository.ProblemaRepository;

@Service
@Transactional
public class ProblemaService {

    private final ProblemaRepository problemaRepository;
    private final AtividadeRepository atividadeRepository;

    public ProblemaService(ProblemaRepository problemaRepository, AtividadeRepository atividadeRepository) {
        this.problemaRepository = problemaRepository;
        this.atividadeRepository = atividadeRepository;
    }

    public Problema criar(ProblemaRequest dados) {
        validarCampos(dados);
        return problemaRepository.save(new Problema(dados.titulo(), dados.descricao(),
                dados.requisitosFuncionais(), dados.requisitosDeProjeto()));
    }

    public Problema buscar(Long id) {
        return problemaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Problema não encontrado."));
    }

    public List<Problema> listar() {
        return problemaRepository.findAll();
    }

    public List<Problema> buscarPorTitulo(String titulo) {
        return problemaRepository.findByTituloContainingIgnoreCase(titulo);
    }

    public Problema atualizar(Long id, ProblemaRequest dados) {
        validarCampos(dados);
        Problema current = buscar(id);
        current.setTitulo(dados.titulo());
        current.setDescricao(dados.descricao());
        current.setRequisitosFuncionais(dados.requisitosFuncionais());
        current.setRequisitosDeProjeto(dados.requisitosDeProjeto());
        return current;
    }

    public Problema desativar(Long id) {
        Problema current = buscar(id);
        current.desativar();
        return current;
    }

    public void remover(Long id) {
        Problema current = buscar(id);
        if (atividadeRepository.existsByProblemaId(id)) {
            throw new IllegalArgumentException("Problemas já utilizados em atividades não podem ser removidos.");
        }
        problemaRepository.delete(current);
    }

    private void validarCampos(ProblemaRequest dados) {
        if (vazio(dados.titulo()) || vazio(dados.descricao()) || vazio(dados.requisitosFuncionais())
                || vazio(dados.requisitosDeProjeto())) {
            throw new IllegalArgumentException("Título, descrição, requisitos funcionais e requisitos de projeto são obrigatórios.");
        }
    }
}
