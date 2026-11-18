package psoft.arena.dto;

import java.time.LocalDateTime;

import psoft.arena.model.Atividade;
import psoft.arena.model.StatusAtividade;

public record AtividadeEstudanteResponse(Long id, String titulo, String turma, String problema, String descricao,
        String descricaoProblema, String requisitosFuncionais, String requisitosDeProjeto, String estrategiaAvaliacao,
        LocalDateTime prazo, StatusAtividade status, boolean aceitaSubmissoes) {

    public static AtividadeEstudanteResponse de(Atividade atividade) {
        return new AtividadeEstudanteResponse(
                atividade.getId(),
                atividade.getTitulo(),
                atividade.getTurma().getNome(),
                atividade.getProblema().getTitulo(),
                atividade.getDescricao(),
                atividade.getProblema().getDescricao(),
                atividade.getProblema().getRequisitosFuncionais(),
                atividade.getProblema().getRequisitosDeProjeto(),
                atividade.getEstrategia().getNome(),
                atividade.getPrazo(),
                atividade.getStatus(),
                atividade.aceitaSubmissoes());
    }
}
