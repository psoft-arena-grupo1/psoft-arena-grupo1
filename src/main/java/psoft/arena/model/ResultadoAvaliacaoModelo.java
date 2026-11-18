package psoft.arena.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "avaliacao_id", "modelo_id" }))
public class ResultadoAvaliacaoModelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Avaliacao avaliacao;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private ModeloLlm modelo;

    @Column(nullable = false)
    private boolean valido;

    @Column(precision = 4, scale = 2)
    private BigDecimal nota;

    @Column(columnDefinition = "TEXT")
    private String evidencias;

    @Column(columnDefinition = "TEXT")
    private String justificativa;

    @Column(columnDefinition = "TEXT")
    private String motivoErro;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    protected ResultadoAvaliacaoModelo() {
    }

    public ResultadoAvaliacaoModelo(Avaliacao avaliacao, ModeloLlm modelo, BigDecimal nota, String evidencias,
            String justificativa) {
        exigirAvaliacaoEModelo(avaliacao, modelo);
        validarNota(nota);
        if (evidencias == null || evidencias.isBlank()) {
            throw new IllegalArgumentException("As evidências do modelo são obrigatórias.");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("A justificativa do modelo é obrigatória.");
        }
        this.avaliacao = avaliacao;
        this.modelo = modelo;
        this.valido = true;
        this.nota = nota;
        this.evidencias = evidencias;
        this.justificativa = justificativa;
        this.dataHora = LocalDateTime.now();
    }

    public ResultadoAvaliacaoModelo(Avaliacao avaliacao, ModeloLlm modelo, String motivoErro) {
        exigirAvaliacaoEModelo(avaliacao, modelo);
        if (motivoErro == null || motivoErro.isBlank()) {
            throw new IllegalArgumentException("O motivo do erro do modelo é obrigatório.");
        }
        this.avaliacao = avaliacao;
        this.modelo = modelo;
        this.valido = false;
        this.motivoErro = motivoErro;
        this.dataHora = LocalDateTime.now();
    }

    private static void exigirAvaliacaoEModelo(Avaliacao avaliacao, ModeloLlm modelo) {
        if (avaliacao == null) {
            throw new IllegalArgumentException("A avaliação do resultado é obrigatória.");
        }
        if (modelo == null) {
            throw new IllegalArgumentException("O modelo de LLM do resultado é obrigatório.");
        }
    }

    private static void validarNota(BigDecimal nota) {
        if (nota == null || nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("A nota do modelo deve estar entre 0 e 10.");
        }
    }

    public Long getId() {
        return id;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public ModeloLlm getModelo() {
        return modelo;
    }

    public boolean isValido() {
        return valido;
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

    public String getMotivoErro() {
        return motivoErro;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
