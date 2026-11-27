package psoft.arena.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;

@Entity
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Submissao submissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAvaliacao status;

    @Column(precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(columnDefinition = "TEXT")
    private String evidencias;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(columnDefinition = "TEXT")
    private String motivoFalha;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ResultadoAvaliacaoModelo> resultados = new ArrayList<>();

    protected Avaliacao() {
    }

    public Avaliacao(Submissao submissao) {
        if (submissao == null) {
            throw new IllegalArgumentException("A submissão avaliada é obrigatória.");
        }
        this.submissao = submissao;
        this.status = StatusAvaliacao.PROCESSANDO;
        this.dataHora = LocalDateTime.now();
    }

    public void adicionarResultado(ResultadoAvaliacaoModelo resultado) {
        if (resultado == null) {
            throw new IllegalArgumentException("O resultado do modelo de LLM é obrigatório.");
        }
        resultados.add(resultado);
    }

    public void concluir(BigDecimal nota, String evidencias, String justificativa) {
        exigirProcessando("podem ser concluídas");
        validarNota(nota);
        if (evidencias == null || evidencias.isBlank()) {
            throw new IllegalArgumentException("As evidências da avaliação são obrigatórias.");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("A justificativa da avaliação é obrigatória.");
        }
        this.nota = nota;
        this.evidencias = evidencias;
        this.justificativa = justificativa;
        this.status = StatusAvaliacao.CONCLUIDA;
    }

    public void falhar(String motivo) {
        exigirProcessando("podem falhar");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("O motivo da falha da avaliação é obrigatório.");
        }
        this.motivoFalha = motivo;
        this.status = StatusAvaliacao.FALHA;
    }

    public boolean resultadoDisponivel() {
        return status == StatusAvaliacao.CONCLUIDA;
    }

    private void exigirProcessando(String predicado) {
        if (status != StatusAvaliacao.PROCESSANDO) {
            throw new IllegalStateException("Apenas avaliações em processamento " + predicado + ".");
        }
    }

    private static void validarNota(BigDecimal nota) {
        if (nota == null || nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("A nota da avaliação deve estar entre 0 e 10.");
        }
    }

    public Long getId() {
        return id;
    }

    public Submissao getSubmissao() {
        return submissao;
    }

    public StatusAvaliacao getStatus() {
        return status;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public String getEvidencias() {
        return evidencias;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getMotivoFalha() {
        return motivoFalha;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public List<ResultadoAvaliacaoModelo> getResultados() {
        return Collections.unmodifiableList(resultados);
    }

    @Override
    public String toString() {
        return "Avaliação " + id + " - submissão: " + (submissao == null ? null : submissao.getId()) + " - status: "
                + status;
    }
}
