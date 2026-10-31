package psoft.arena.model;

public enum TipoEstrategia {
    CODE_SMELLS("Code Smells"),
    ACOPLAMENTO_COESAO_PRINCIPIOS("Baixo Acoplamento, Alta Coesão e Princípios (Programe para uma Interface e use Herança com moderação)"),
    PADROES_DE_PROJETO("Aplicação de Padrões de Projeto");

    private final String descricao;

    TipoEstrategia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
