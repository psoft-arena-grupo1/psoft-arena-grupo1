package psoft.arena.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Submissao;

public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {
    long countByEstudanteMatriculaAndAtividadeId(String matricula, Long atividadeId);
}