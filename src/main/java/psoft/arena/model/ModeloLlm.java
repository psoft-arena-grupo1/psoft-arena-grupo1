package psoft.arena.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import psoft.arena.config.ChaveApiConverter;

@Entity
public class ModeloLlm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nome;
    @Column(nullable = false)
    private String urlApi;
    @JsonIgnore
    @Convert(converter = ChaveApiConverter.class)
    @Column(nullable = false, length = 1024)
    private String chaveApi;

    protected ModeloLlm() {
    }

    public ModeloLlm(String nome, String urlApi, String chaveApi) {
        if (!urlApi.startsWith("https://")) {
            throw new IllegalArgumentException("A URL da API do LLM deve usar HTTPS.");
        }
        this.nome = nome;
        this.urlApi = urlApi;
        this.chaveApi = chaveApi;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUrlApi() {
        return urlApi;
    }

    public String getChaveApi() {
        return chaveApi;
    }
}
