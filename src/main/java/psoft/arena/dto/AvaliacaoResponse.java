package psoft.arena.dto;

import java.math.BigDecimal;
import java.util.List;

import psoft.arena.model.Avaliacao;
import psoft.arena.model.ResultadoAvaliacaoModelo;
import psoft.arena.model.StatusAvaliacao;

public record AvaliacaoResponse(Long id, Long submissaoId, StatusAvaliacao status, BigDecimal nota, String evidencias,
        String justificativa, String motivoFalha, List<ResultadoModeloResponse> resultados) {

    public record ResultadoModeloResponse(Long modeloId, String modelo, boolean valido, BigDecimal nota,
            String evidencias, String justificativa, String motivoErro) {
    }

    public static AvaliacaoResponse de(Avaliacao avaliacao) {
        return new AvaliacaoResponse(avaliacao.getId(), avaliacao.getSubmissao().getId(), avaliacao.getStatus(),
                avaliacao.getNota(), avaliacao.getEvidencias(), avaliacao.getJustificativa(),
                avaliacao.getMotivoFalha(),
                avaliacao.getResultados().stream().map(AvaliacaoResponse::de).toList());
    }

    private static ResultadoModeloResponse de(ResultadoAvaliacaoModelo resultado) {
        return new ResultadoModeloResponse(resultado.getModelo().getId(), resultado.getModelo().getNome(),
                resultado.isValido(), resultado.getNota(), resultado.getEvidencias(), resultado.getJustificativa(),
                resultado.getMotivoErro());
    }
}
