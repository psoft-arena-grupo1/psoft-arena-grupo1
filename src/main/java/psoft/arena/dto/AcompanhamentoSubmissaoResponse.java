package psoft.arena.dto;

import java.time.LocalDateTime;
import java.util.List;

import psoft.arena.model.StatusSubmissao;
import psoft.arena.model.Submissao;

public record AcompanhamentoSubmissaoResponse(Long id, Long atividadeId, String matricula, int versao,
        LocalDateTime dataHora, StatusSubmissao status, List<HistoricoSubmissaoResponse> historico) {

    public static AcompanhamentoSubmissaoResponse de(Submissao submissao) {
        return new AcompanhamentoSubmissaoResponse(submissao.getId(), submissao.getAtividade().getId(),
                submissao.getEstudante().getMatricula(), submissao.getVersao(), submissao.getDataHora(),
                submissao.getStatus(),
                submissao.getHistorico().stream().map(HistoricoSubmissaoResponse::de).toList());
    }
}
