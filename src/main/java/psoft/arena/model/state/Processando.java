package psoft.arena.model.state;

import psoft.arena.model.StatusSubmissao;

public final class Processando implements EstadoSubmissao {

    @Override
    public StatusSubmissao status() {
        return StatusSubmissao.PROCESSANDO;
    }

    @Override
    public EstadoSubmissao concluir() {
        return new Concluida();
    }

    @Override
    public EstadoSubmissao falhar() {
        return new Falha();
    }
}