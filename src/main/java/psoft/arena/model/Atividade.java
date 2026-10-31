package psoft.arena.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Problema problema;

    protected Atividade() {
    }

    public Atividade(Problema problema) {
        if (!problema.isAtivo()) {
            throw new IllegalArgumentException("Problemas inativos não podem ser associados a novas atividades.");
        }
        this.problema = problema;
    }

    public Long getId() {
        return id;
    }

    public Problema getProblema() {
        return problema;
    }

    @Override
    public String toString() {
        return "Atividade " + id + " - problema: " + problema.getTitulo();
    }
}
