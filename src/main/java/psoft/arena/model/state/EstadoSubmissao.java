package psoft.arena.model.state;

import psoft.arena.exception.TransicaoInvalidaException;
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

    default EstadoSubmissao reprocessar() {
        throw transicaoInvalida("reprocessar");
    }

    private TransicaoInvalidaException transicaoInvalida(String operacao) {
        return new TransicaoInvalidaException("Não é possível " + operacao + " uma submissão no estado "
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
