package psoft.arena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class EstrategiaAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Enumerated(EnumType.STRING)
    private TipoEstrategia tipo;
    @Column(columnDefinition = "TEXT")
    private String descricao;
    @Column(columnDefinition = "TEXT")
    private String prompt;

    protected EstrategiaAvaliacao() {
    }

    public EstrategiaAvaliacao(String nome, TipoEstrategia tipo, String descricao, String prompt) {
        this.nome = nome;
        this.tipo = tipo;
        this.descricao = descricao;
        this.prompt = prompt;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoEstrategia getTipo() {
        return tipo;
    }

    public void setTipo(TipoEstrategia tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "EstrategiaAvaliacao{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo.getDescricao() +
                ", descricao='" + descricao + '\'' +
                ", prompt='" + prompt + '\'' +
                '}';
    }
}
