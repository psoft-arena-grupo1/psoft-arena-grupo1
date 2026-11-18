package psoft.arena.service;

import psoft.arena.model.ModeloLlm;

public interface ClienteLlm {

    RespostaAvaliacaoLlm avaliar(ModeloLlm modelo, String prompt);
}
