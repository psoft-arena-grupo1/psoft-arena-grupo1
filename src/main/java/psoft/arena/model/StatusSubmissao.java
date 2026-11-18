package psoft.arena.model;

public enum StatusSubmissao {
    SUBMETIDA("Submetida"),
    EM_FILA("Em fila"),
    PROCESSANDO("Processando"),
    CONCLUIDA("Concluída"),
    FALHA("Falha");

    private final String descricao;

    StatusSubmissao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao(){
        return descricao;
    }
}