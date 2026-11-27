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
public class HistoricoSubmissao {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Submissao submissao;

    @Enumerated(EnumType.STRING)
    private StatusSubmissao statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSubmissao statusNovo;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(columnDefinition = "TEXT")
    private String motivo;

    protected HistoricoSubmissao() {
    }

    HistoricoSubmissao(Submissao submissao, StatusSubmissao statusAnterior, StatusSubmissao statusNovo, String motivo) {
        this.submissao = submissao;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.motivo = motivo;
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Submissao getSubmissao() {
        return submissao;
    }

    public StatusSubmissao getStatusAnterior() {
        return statusAnterior;
    }

    public StatusSubmissao getStatusNovo() {
        return statusNovo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public String toString() {
         return "Historico de Submissao " + id + " - " + statusAnterior + " -> " + statusNovo + " em " + dataHora;
    }
    
}
