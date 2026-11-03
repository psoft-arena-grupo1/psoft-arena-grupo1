package psoft.arena.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.TipoEstrategia;

public interface EstrategiaAvaliacaoRepository extends JpaRepository<EstrategiaAvaliacao, Long> {

    List<EstrategiaAvaliacao> findByTipo(TipoEstrategia tipo);
}
