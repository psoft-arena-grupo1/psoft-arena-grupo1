package psoft.arena.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import psoft.arena.model.Submissao;

@Component
public class FilaSubmissoes {

    private final BlockingQueue<Long> fila = new LinkedBlockingQueue<>();
    private final SubmissaoService submissaoService;
    private final AvaliadorSubmissao avaliador;
    private Thread trabalhador;

    public FilaSubmissoes(SubmissaoService submissaoService, AvaliadorSubmissao avaliador) {
        this.submissaoService = submissaoService;
        this.avaliador = avaliador;
    }

    @PostConstruct
    void iniciar() {
        trabalhador = new Thread(this::consumir, "fila-submissoes");
        trabalhador.setDaemon(true);
        trabalhador.start();
    }

    @PreDestroy
    void parar() {
        trabalhador.interrupt();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aoSubmeter(SubmissaoCriadaEvent evento) {
        Long id = evento.submissaoId();
        try {
            submissaoService.encaminhar(id);
            fila.offer(id);
        } catch (RuntimeException e) {
            registrarFalha(id, e);
        }
    }

    private void consumir() {
        try {
            while (true) {
                processar(fila.take());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processar(Long id) {
        try {
            Submissao submissao = submissaoService.iniciarProcessamento(id);
            avaliador.avaliar(submissao);
            submissaoService.concluir(id);
        } catch (RuntimeException e) {
            registrarFalha(id, e);
        }
    }

    private void registrarFalha(Long id, RuntimeException causa) {
        try {
            submissaoService.falhar(id);
        } catch (RuntimeException e) {
            System.err.println("Não foi possível registrar a falha da submissão " + id + ": " + e.getMessage());
        }
    }
}
