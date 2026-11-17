package psoft.arena.dto;

import java.time.LocalDateTime;

import psoft.arena.model.StatusSubmissao;
import psoft.arena.model.Submissao;

public record SubmissaoResponse(Long id, Long atividadeId, String matricula, int versao, LocalDateTime dataHora,
        StatusSubmissao status, String mensagem) {

    public static SubmissaoResponse de(Submissao submissao) {
        return new SubmissaoResponse(submissao.getId(), submissao.getAtividade().getId(),
                submissao.getEstudante().getMatricula(), submissao.getVersao(), submissao.getDataHora(),
                submissao.getStatus(), "Submissão recebida com sucesso.");
    }
}