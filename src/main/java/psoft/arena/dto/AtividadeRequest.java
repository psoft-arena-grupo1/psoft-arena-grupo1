package psoft.arena.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AtividadeRequest(Long problemaId, Long turmaId, Long estrategiaId, Long llmPadraoId, String titulo,
        String descricao, LocalDateTime prazo, List<Long> llmsAdicionaisIds) {

    public AtividadeRequest(Long problemaId, Long turmaId, Long estrategiaId, Long llmPadraoId, String titulo,
            String descricao, LocalDateTime prazo) {
        this(problemaId, turmaId, estrategiaId, llmPadraoId, titulo, descricao, prazo, List.of());
    }
}
