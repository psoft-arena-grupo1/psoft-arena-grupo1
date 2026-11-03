package psoft.arena.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import psoft.arena.model.Problema;

public interface ProblemaRepository extends JpaRepository<Problema, Long> {

    List<Problema> findByTituloContainingIgnoreCase(String titulo);
}
