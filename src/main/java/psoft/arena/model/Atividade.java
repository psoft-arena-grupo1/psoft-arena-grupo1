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
    @ManyToOne(optional = false)
    private Turma turma;

    protected Atividade() {
    }

    public Atividade(Problema problema, Turma turma) {
        if (!problema.isAtivo()) {
            throw new IllegalArgumentException("Problemas inativos não podem ser associados a novas atividades.");
        }
        if (turma.getStatus() != StatusTurma.ATIVA) {
            throw new IllegalArgumentException("Turmas encerradas não podem receber novas atividades.");
        }
        this.problema = problema;
        this.turma = turma;
    }

    public Long getId() {
        return id;
    }

    public Problema getProblema() {
        return problema;
    }

    public Turma getTurma() {
        return turma;
    }

    public void garantirParticipante(Estudante estudante) {
        if (!turma.possuiEstudante(estudante.getMatricula())) {
            throw new IllegalArgumentException("Apenas estudantes da turma podem ser adicionados a atividade.");
        }
    }

    @Override
    public String toString() {
        return "Atividade " + id + "- turma: " + turma.getNome() + " - problema: " + problema.getTitulo();
    }
}
