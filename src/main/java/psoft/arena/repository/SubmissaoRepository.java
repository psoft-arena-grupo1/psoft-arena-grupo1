package psoft.arena.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Submissao;

import java.util.List;
import java.util.Optional;

public interface SubmissaoRepository extends JpaRepository<Submissao, Long> {
    long countByEstudanteMatriculaAndAtividadeId(String matricula, Long atividadeId);

    List<Submissao> findByEstudanteMatriculaOrderByDataHoraDesc(String matricula);

    Optional<Submissao> findByIdAndEstudanteMatricula(Long id, String matricula);
}