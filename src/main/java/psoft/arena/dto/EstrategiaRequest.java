package psoft.arena.dto;

import psoft.arena.model.TipoEstrategia;

public record EstrategiaRequest(String nome, TipoEstrategia tipo, String descricao, String prompt) {
}
