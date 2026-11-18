package psoft.arena.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.UniqueConstraint;

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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "atividade_llm_adicional", joinColumns = @JoinColumn(name = "atividade_id"), inverseJoinColumns = @JoinColumn(name = "llm_id"), uniqueConstraints = @UniqueConstraint(columnNames = {
            "atividade_id", "llm_id" }))
    private List<ModeloLlm> llmsAdicionais = new ArrayList<>();

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

    public void adicionarLlm(ModeloLlm llm) {
        if (llm == null || mesmoModelo(llm, llmPadrao) || contemModelo(llmsAdicionais, llm)) {
            return;
        }
        llmsAdicionais.add(llm);
    }

    public List<ModeloLlm> getLlmsAdicionais() {
        return List.copyOf(llmsAdicionais);
    }

    public List<ModeloLlm> modelosDeAvaliacao() {
        List<ModeloLlm> modelos = new ArrayList<>();
        if (llmPadrao != null) {
            modelos.add(llmPadrao);
        }
        for (ModeloLlm llm : llmsAdicionais) {
            if (llm == null || contemModelo(modelos, llm)) {
                continue;
            }
            modelos.add(llm);
        }
        return List.copyOf(modelos);
    }

    private static boolean contemModelo(List<ModeloLlm> modelos, ModeloLlm llm) {
        return modelos.stream().anyMatch(modelo -> mesmoModelo(modelo, llm));
    }

    private static boolean mesmoModelo(ModeloLlm primeiro, ModeloLlm segundo) {
        if (primeiro == segundo) {
            return true;
        }
        return primeiro != null && segundo != null && primeiro.getId() != null
                && primeiro.getId().equals(segundo.getId());
    }

    public void encerrar() {
        this.status = StatusAtividade.CONCLUIDA;
    }

    public boolean aceitaSubmissoes() {
        return status == StatusAtividade.ABERTA;
    }

    public boolean visivelPara(Estudante estudante) {
        return turma.possuiEstudante(estudante.getMatricula());
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
