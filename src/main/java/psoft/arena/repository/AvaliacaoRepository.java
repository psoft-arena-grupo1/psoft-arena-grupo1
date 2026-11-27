package psoft.arena.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Avaliacao;
import psoft.arena.model.StatusAvaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findFirstBySubmissaoIdOrderByIdDesc(Long submissaoId);

    default Optional<Avaliacao> findBySubmissaoId(Long submissaoId) {
        return findFirstBySubmissaoIdOrderByIdDesc(submissaoId);
    }

    List<Avaliacao> findBySubmissaoIdOrderByIdAsc(Long submissaoId);

    boolean existsBySubmissaoIdAndStatus(Long submissaoId, StatusAvaliacao status);
}
