package psoft.arena.service;

import java.math.BigDecimal;
import java.util.List;

public record RespostaAvaliacaoLlm(BigDecimal nota, List<String> evidencias, String justificativa) {
}
