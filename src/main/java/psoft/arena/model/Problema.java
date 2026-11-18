package psoft.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Problema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String titulo;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(columnDefinition = "TEXT")
    private String requisitosFuncionais;
    @Column(columnDefinition = "TEXT")
    private String requisitosDeProjeto;
    private boolean ativo;

    protected Problema() {
    }

    public Problema(String titulo, String descricao, String requisitosFuncionais, String requisitosDeProjeto) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.requisitosFuncionais = requisitosFuncionais;
        this.requisitosDeProjeto = requisitosDeProjeto;
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRequisitosFuncionais() {
        return requisitosFuncionais;
    }

    public void setRequisitosFuncionais(String requisitosFuncionais) {
        this.requisitosFuncionais = requisitosFuncionais;
    }

    public String getRequisitosDeProjeto() {
        return requisitosDeProjeto;
    }

    public void setRequisitosDeProjeto(String requisitosDeProjeto) {
        this.requisitosDeProjeto = requisitosDeProjeto;
    }

    public String getStatus() {
        return ativo ? "Ativo" : "Inativo";
    }

    @Override
    public String toString() {
        return "Problema{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", requisitosFuncionais='" + requisitosFuncionais + '\'' +
                ", requisitosDeProjeto='" + requisitosDeProjeto + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
