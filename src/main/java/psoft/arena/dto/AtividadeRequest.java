package psoft.arena.dto;

import java.time.LocalDateTime;

public record AtividadeRequest(Long problemaId, Long turmaId, Long estrategiaId, Long llmPadraoId, String titulo,
        String descricao, LocalDateTime prazo) {
}
