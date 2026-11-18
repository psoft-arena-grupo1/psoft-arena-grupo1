package psoft.arena.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Atividade;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    boolean existsByProblemaId(Long problemaId);
}
