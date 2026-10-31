package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.EstrategiaRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.repository.EstrategiaAvaliacaoRepository;

@Service
@Transactional
public class EstrategiaAvaliacaoService {

    private final EstrategiaAvaliacaoRepository estrategiaRepository;

    public EstrategiaAvaliacaoService(EstrategiaAvaliacaoRepository estrategiaRepository) {
        this.estrategiaRepository = estrategiaRepository;
    }

    public EstrategiaAvaliacao criar(EstrategiaRequest dados) {
        validarCampos(dados);
        return estrategiaRepository.save(
                new EstrategiaAvaliacao(dados.nome(), dados.tipo(), dados.descricao(), dados.prompt()));
    }

    public EstrategiaAvaliacao buscar(Long id) {
        return estrategiaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estratégia não encontrada."));
    }

    public List<EstrategiaAvaliacao> listar() {
        return estrategiaRepository.findAll();
    }

    public List<EstrategiaAvaliacao> listarPorTipo(TipoEstrategia tipo) {
        return estrategiaRepository.findByTipo(tipo);
    }

    public EstrategiaAvaliacao atualizar(Long id, EstrategiaRequest dados) {
        validarCampos(dados);
        EstrategiaAvaliacao current = buscar(id);
        current.setNome(dados.nome());
        current.setTipo(dados.tipo());
        current.setDescricao(dados.descricao());
        current.setPrompt(dados.prompt());
        return current;
    }

    public EstrategiaAvaliacao alterarPrompt(Long id, String prompt) {
        if (vazio(prompt)) {
            throw new IllegalArgumentException("O prompt de avaliação é obrigatório.");
        }
        EstrategiaAvaliacao current = buscar(id);
        current.setPrompt(prompt);
        return current;
    }

    private void validarCampos(EstrategiaRequest dados) {
        if (vazio(dados.nome()) || dados.tipo() == null || vazio(dados.descricao()) || vazio(dados.prompt())) {
            throw new IllegalArgumentException("Nome, tipo, descrição e prompt de avaliação são obrigatórios.");
        }
    }
}
