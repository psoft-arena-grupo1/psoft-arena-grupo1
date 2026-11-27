package psoft.arena.service;

import org.springframework.stereotype.Component;

import psoft.arena.model.Estudante;
import psoft.arena.model.HistoricoSubmissao;
import psoft.arena.model.Submissao;

@Component
public class NotificadorSubmissao {

    public void notificar(HistoricoSubmissao registro) {
        Submissao submissao = registro.getSubmissao();
        Estudante estudante = submissao.getEstudante();
        String mensagem = "[NOTIFICAÇÃO] " + estudante.getNome() + " (" + estudante.getMatricula()
                + "): sua submissão " + submissao.getId() + " (atividade " + submissao.getAtividade().getId()
                + ", versão " + submissao.getVersao() + ") mudou de " + registro.getStatusAnterior().getDescricao()
                + " para " + registro.getStatusNovo().getDescricao() + ".";
        if (registro.getMotivo() != null) {
            mensagem += " Motivo: " + registro.getMotivo();
        }
        System.out.println(mensagem);
    }

    public void notificarResultadoDisponivel(Submissao submissao) {
        Estudante estudante = submissao.getEstudante();
        System.out.println("[NOTIFICAÇÃO] " + estudante.getNome() + " (" + estudante.getMatricula()
                + "): o resultado da avaliação da sua submissão " + submissao.getId() + " (atividade "
                + submissao.getAtividade().getId() + ", versão " + submissao.getVersao()
                + ") já está disponível. Consulte em GET /estudantes/" + estudante.getMatricula()
                + "/submissoes/" + submissao.getId() + "/resultado.");
    }
}
