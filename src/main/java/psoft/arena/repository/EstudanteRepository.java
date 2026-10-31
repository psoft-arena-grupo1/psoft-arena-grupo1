package psoft.arena.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Estudante;

public interface EstudanteRepository extends JpaRepository<Estudante, String> {

    List<Estudante> findByNomeContainingIgnoreCase(String nome);
}
