package psoft.arena.dto;

import java.time.LocalDateTime;

import psoft.arena.model.HistoricoSubmissao;
import psoft.arena.model.StatusSubmissao;

public record HistoricoSubmissaoResponse(StatusSubmissao statusAnterior, StatusSubmissao statusNovo,
        LocalDateTime dataHora, String motivo) {

    public static HistoricoSubmissaoResponse de(HistoricoSubmissao registro) {
        return new HistoricoSubmissaoResponse(registro.getStatusAnterior(), registro.getStatusNovo(),
                registro.getDataHora(), registro.getMotivo());
    }
}
