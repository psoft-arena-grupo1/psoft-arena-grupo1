package psoft.arena.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Avaliacao;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findBySubmissaoId(Long submissaoId);

    boolean existsBySubmissaoId(Long submissaoId);
}
