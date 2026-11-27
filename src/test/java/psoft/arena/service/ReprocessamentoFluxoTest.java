package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import psoft.arena.dto.SubmissaoRequest;
import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.HistoricoSubmissao;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.StatusSubmissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;
import psoft.arena.repository.AtividadeRepository;
import psoft.arena.repository.EstrategiaAvaliacaoRepository;
import psoft.arena.repository.EstudanteRepository;
import psoft.arena.repository.ModeloLlmRepository;
import psoft.arena.repository.ProblemaRepository;
import psoft.arena.repository.SubmissaoRepository;
import psoft.arena.repository.TurmaRepository;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:psoftarena-reprocessamento-test;DB_CLOSE_DELAY=-1")
class ReprocessamentoFluxoTest {

    @MockitoBean
    private AvaliadorSubmissao avaliador;
    @MockitoBean
    private NotificadorSubmissao notificador;

    @Autowired
    private SubmissaoService submissaoService;
    @Autowired
    private SubmissaoRepository submissaoRepository;
    @Autowired
    private EstudanteRepository estudanteRepository;
    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private ProblemaRepository problemaRepository;
    @Autowired
    private EstrategiaAvaliacaoRepository estrategiaRepository;
    @Autowired
    private ModeloLlmRepository modeloLlmRepository;
    @Autowired
    private AtividadeRepository atividadeRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long criarAtividade(String matricula) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            Estudante estudante = estudanteRepository.save(new Estudante("Ana", matricula, "ana@email.com"));
            Turma turma = new Turma("POO", "P" + System.nanoTime(), "2026.1", "Prof. Silva");
            turma.adicionarEstudante(estudante);
            turmaRepository.save(turma);
            Problema problema = problemaRepository.save(new Problema("Biblioteca", "d", "rf", "rp"));
            EstrategiaAvaliacao estrategia = estrategiaRepository
                    .save(new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"));
            ModeloLlm modelo = modeloLlmRepository.save(new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"));
            return atividadeRepository.save(new Atividade(problema, turma, estrategia, modelo, "Lab 1", "d",
                    LocalDateTime.now().plusDays(7))).getId();
        });
    }

    private StatusSubmissao aguardar(Long id, StatusSubmissao esperado) throws InterruptedException {
        Supplier<StatusSubmissao> atual = () -> submissaoRepository.findById(id).orElseThrow().getStatus();
        for (int i = 0; i < 100 && atual.get() != esperado; i++) {
            Thread.sleep(50);
        }
        return atual.get();
    }

    @Test
    void submissaoComFalhaReprocessadaEhAvaliadaNovamentePelaFilaENotificaCadaMudanca() throws Exception {
        String matricula = "MAT" + System.nanoTime();
        Long atividadeId = criarAtividade(matricula);
        doThrow(new FalhaAvaliacaoException("O modelo grok-3 respondeu com erro HTTP 500."))
                .doNothing()
                .when(avaliador).avaliar(any());

        Long id = submissaoService.submeter(atividadeId, new SubmissaoRequest(matricula, "public class A { }"))
                .getId();
        assertEquals(StatusSubmissao.FALHA, aguardar(id, StatusSubmissao.FALHA));

        submissaoService.reprocessar(id);
        assertEquals(StatusSubmissao.CONCLUIDA, aguardar(id, StatusSubmissao.CONCLUIDA));

        verify(avaliador, times(2)).avaliar(any());
        List<StatusSubmissao> historico = new TransactionTemplate(transactionManager).execute(status ->
                submissaoRepository.findById(id).orElseThrow().getHistorico().stream()
                        .map(HistoricoSubmissao::getStatusNovo).toList());
        assertEquals(List.of(StatusSubmissao.SUBMETIDA, StatusSubmissao.EM_FILA, StatusSubmissao.PROCESSANDO,
                StatusSubmissao.FALHA, StatusSubmissao.EM_FILA, StatusSubmissao.PROCESSANDO,
                StatusSubmissao.CONCLUIDA), historico);
        verify(notificador, times(6)).notificar(any(HistoricoSubmissao.class));
        assertEquals(1, submissaoRepository.countByEstudanteMatriculaAndAtividadeId(matricula, atividadeId));
    }
}
