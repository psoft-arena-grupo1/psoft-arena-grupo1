package psoft.arena.model.state;

import psoft.arena.model.StatusSubmissao;

public final class Concluida implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.CONCLUIDA;
    }
}