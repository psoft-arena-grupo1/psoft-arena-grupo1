package psoft.arena.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String titulo;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;
    @Column(nullable = false)
    private LocalDateTime prazo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAtividade status;
    @ManyToOne(optional = false)
    private Problema problema;
    @ManyToOne(optional = false)
    private Turma turma;
    @ManyToOne(optional = false)
    private EstrategiaAvaliacao estrategia;
    @ManyToOne(optional = false)
    private ModeloLlm llmPadrao;

    protected Atividade() {
    }

    public Atividade(Problema problema, Turma turma, EstrategiaAvaliacao estrategia, ModeloLlm llmPadrao,
            String titulo, String descricao, LocalDateTime prazo) {
        if (!problema.isAtivo()) {
            throw new IllegalArgumentException("Problemas inativos não podem ser associados a novas atividades.");
        }
        if (turma.getStatus() != StatusTurma.ATIVA) {
            throw new IllegalArgumentException("Turmas encerradas não podem receber novas atividades.");
        }
        if (!prazo.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("O prazo para submissão deve ser uma data futura.");
        }
        this.problema = problema;
        this.turma = turma;
        this.estrategia = estrategia;
        this.llmPadrao = llmPadrao;
        this.titulo = titulo;
        this.descricao = descricao;
        this.prazo = prazo;
        this.status = StatusAtividade.ABERTA;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getPrazo() {
        return prazo;
    }

    public StatusAtividade getStatus() {
        return status;
    }

    public Problema getProblema() {
        return problema;
    }

    public Turma getTurma() {
        return turma;
    }

    public EstrategiaAvaliacao getEstrategia() {
        return estrategia;
    }

    public ModeloLlm getLlmPadrao() {
        return llmPadrao;
    }

    public void encerrar() {
        this.status = StatusAtividade.CONCLUIDA;
    }

    public boolean aceitaSubmissoes() {
        return status == StatusAtividade.ABERTA;
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
