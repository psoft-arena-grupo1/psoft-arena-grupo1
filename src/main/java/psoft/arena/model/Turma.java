package psoft.arena.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.UniqueConstraint;

@Entity
public class Turma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String periodoLetivo;
    @Column(nullable = false)
    private String professorResponsavel;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTurma status;

    @ManyToMany
    @JoinTable(name = "turma_estudante",
        joinColumns = @JoinColumn(name = "turma_id"),
        inverseJoinColumns = @JoinColumn(name = "estudante_matricula"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"turma_id", "estudante_matricula"}))
    private List<Estudante> estudantes = new ArrayList<>();

    protected Turma() {
    }

    public Turma(String nome, String codigo, String periodoLetivo, String professorResponsavel) {
        this.nome = nome;
        this.codigo = codigo;
        this.periodoLetivo = periodoLetivo;
        this.professorResponsavel = professorResponsavel;
        this.status = StatusTurma.ATIVA;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getPeriodoLetivo() { return periodoLetivo; }
    public void setPeriodoLetivo(String periodoLetivo) { this.periodoLetivo = periodoLetivo; }
    public String getProfessorResponsavel() { return professorResponsavel; }
    public void setProfessorResponsavel(String professorResponsavel) { this.professorResponsavel = professorResponsavel; }
    public StatusTurma getStatus() { return status; }
    public List<Estudante> getEstudantes() { return estudantes; }

    public void encerrar() {
        this.status = StatusTurma.ENCERRADA;
    }

    public void adicionarEstudante(Estudante estudante) {
        garantirAtiva();
        if (possuiEstudante(estudante.getMatricula())) {
            throw new IllegalArgumentException("Estudante já está matriculado nesta turma.");
        }
        estudantes.add(estudante);
    }

    public void removerEstudante(String matricula) {
        garantirAtiva();
        boolean removido = estudantes.removeIf(e -> e.getMatricula().equals(matricula));
        if (!removido) {
            throw new IllegalArgumentException("Estudante não está matriculado nesta turma.");
        }
    }

    public void garantirAtiva() {
        if (status != StatusTurma.ATIVA) {
            throw new IllegalArgumentException("Não é possível alterar os estudantes de uma turma inativa.");
        }
    }

    public boolean possuiEstudante(String matricula) {
        return estudantes.stream().anyMatch(e -> e.getMatricula().equals(matricula));

    }
}
