package psoft.arena.dto;

import java.math.BigDecimal;
import java.util.List;

import psoft.arena.model.Avaliacao;
import psoft.arena.model.StatusAvaliacao;
import psoft.arena.model.Submissao;

public record ResultadoAvaliacaoResponse(Long submissaoId, Long atividadeId, int versao, StatusAvaliacao situacao,
        BigDecimal nota, String justificativa, List<String> evidencias) {

    public static ResultadoAvaliacaoResponse de(Avaliacao avaliacao) {
        Submissao submissao = avaliacao.getSubmissao();
        return new ResultadoAvaliacaoResponse(submissao.getId(), submissao.getAtividade().getId(),
                submissao.getVersao(), avaliacao.getStatus(), avaliacao.getNota(), avaliacao.getJustificativa(),
                separarEvidencias(avaliacao.getEvidencias()));
    }

    private static List<String> separarEvidencias(String evidencias) {
        if (evidencias == null) {
            return List.of();
        }
        return evidencias.lines().map(String::strip).filter(linha -> !linha.isEmpty()).toList();
    }
}