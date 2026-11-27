package psoft.arena.model.state;

import psoft.arena.model.StatusSubmissao;

public final class EmFila implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.EM_FILA;
    }

    @Override
    public EstadoSubmissao iniciarProcessamento() {
        return new Processando();
    }

    @Override
    public EstadoSubmissao falhar() {
        return new Falha();
    }
}
