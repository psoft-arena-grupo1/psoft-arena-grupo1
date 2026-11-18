package psoft.arena.model.state;

import psoft.arena.model.StatusSubmissao;

public final class Submetida implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.SUBMETIDA;
    }

    @Override
    public EstadoSubmissao encaminhar() {
        return new EmFila();
    }

    @Override
    public EstadoSubmissao falhar() {
        return new Falha();
    }
}
