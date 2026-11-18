package psoft.arena.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import psoft.arena.model.state.EstadoSubmissao;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "estudante_matricula", "atividade_id", "versao" }))
public class Submissao {

    private static final Pattern DECLARACAO_JAVA = Pattern
            .compile("\\b(class|interface|enum|record)\\s+[A-Za-z_$][\\w$]*[^;{]*\\{");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Estudante estudante;

    @ManyToOne(optional = false)
    private Atividade atividade;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String codigoFonte;

    @Column(nullable = false)
    private int versao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSubmissao status;

    @OneToMany(mappedBy = "submissao", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
    private List<HistoricoSubmissao> historico = new ArrayList<>();

    protected Submissao() {
    }

    public Submissao(Estudante estudante, Atividade atividade, String codigoFonte, int versao) {
        if (!atividade.aceitaSubmissoes()) {
            throw new IllegalArgumentException("Atividades concluídas não aceitam novas submissões.");
        }
        if (codigoFonte == null || codigoFonte.isBlank()) {
            throw new IllegalArgumentException("O código-fonte da solução é obrigatório.");
        }
        if (!pareceCodigoJava(codigoFonte)) {
            throw new IllegalArgumentException("A solução deve ser submetida em Java.");
        }
        if (versao < 1) {
            throw new IllegalArgumentException("A versão da submissão deve ser maior que zero.");
        }
        this.estudante = estudante;
        this.atividade = atividade;
        this.codigoFonte = codigoFonte;
        this.versao = versao;
        this.dataHora = LocalDateTime.now();
        this.status = StatusSubmissao.SUBMETIDA;
        registrar(null, StatusSubmissao.SUBMETIDA, null);
    }

    private static boolean pareceCodigoJava(String codigoFonte) {
        return DECLARACAO_JAVA.matcher(codigoFonte).find();
    }

    public HistoricoSubmissao encaminhar() {
        return transitar(estado().encaminhar(), null);
    }

    public HistoricoSubmissao iniciarProcessamento() {
        return transitar(estado().iniciarProcessamento(), null);
    }

    public HistoricoSubmissao concluir() {
        return transitar(estado().concluir(), null);
    }

    public HistoricoSubmissao falhar() {
        return transitar(estado().falhar(), null);
    }

    private EstadoSubmissao estado() {
        return EstadoSubmissao.de(status);
    }

    private HistoricoSubmissao transitar(EstadoSubmissao novoEstado, String motivo) {
        HistoricoSubmissao registro = registrar(status, novoEstado.status(), motivo);
        this.status = novoEstado.status();
        return registro;
    }

    private HistoricoSubmissao registrar(StatusSubmissao anterior, StatusSubmissao novo, String motivo) {
        HistoricoSubmissao registro = new HistoricoSubmissao(this, anterior, novo, motivo);
        historico.add(registro);
        return registro;
    }

    public Long getId() {
        return id;
    }

    public Estudante getEstudante() {
        return estudante;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getCodigoFonte() {
        return codigoFonte;
    }

    public int getVersao() {
        return versao;
    }

    public StatusSubmissao getStatus() {
        return status;
    }

    public List<HistoricoSubmissao> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    @Override
    public String toString() {
        return "Submissao " + id + " - estudante: " + estudante.getMatricula() + " - atividade: "
                + atividade.getId() + " - versão: " + versao + " - status: " + status;
    }
}