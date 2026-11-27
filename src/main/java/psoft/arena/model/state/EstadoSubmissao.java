package psoft.arena.model.state;

import psoft.arena.model.StatusSubmissao;

public interface EstadoSubmissao {

    StatusSubmissao status();

    default EstadoSubmissao encaminhar() {
        throw transicaoInvalida("encaminhar");
    }

    default EstadoSubmissao iniciarProcessamento() {
        throw transicaoInvalida("inicie o processamento de");
    }

    default EstadoSubmissao concluir() {
        throw transicaoInvalida("concluir");
    }
    
    default EstadoSubmissao falhar() {
        throw transicaoInvalida("marcar como falha");
    }

    private IllegalStateException transicaoInvalida(String operacao) {
        return new IllegalStateException("Não é possível " + operacao + " uma submissão no estado "
            + status().getDescricao() + ".");
    }

    static EstadoSubmissao de(StatusSubmissao status) {
        return switch (status) {
            case SUBMETIDA -> new Submetida();
            case EM_FILA -> new EmFila();
            case PROCESSANDO -> new Processando();
            case CONCLUIDA -> new Concluida();
            case FALHA -> new Falha();
        };
    }
}
