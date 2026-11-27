package psoft.arena.service;

import psoft.arena.exception.TransicaoInvalidaException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import psoft.arena.dto.AcompanhamentoSubmissaoResponse;
import psoft.arena.dto.SubmissaoRequest;
import psoft.arena.exception.AcessoNegadoException;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.HistoricoSubmissao;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.StatusSubmissao;
import psoft.arena.model.Submissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;
import psoft.arena.repository.SubmissaoRepository;

@ExtendWith(MockitoExtension.class)
class SubmissaoServiceTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Mock
    private SubmissaoRepository submissaoRepository;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private EstudanteService estudanteService;
    @Mock
    private ApplicationEventPublisher eventos;
    @Mock
    private NotificadorSubmissao notificador;
    @InjectMocks
    private SubmissaoService submissaoService;

    private Estudante estudante;
    private Atividade atividade;

    @BeforeEach
    void setUp() {
        estudante = new Estudante("Maria", "123", "maria@email.com");
        Turma turma = new Turma("POO", "P1", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        atividade = new Atividade(new Problema("Biblioteca", "d", "rf", "rp"), turma,
                new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"),
                new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"), "Lab 1", "Descrição",
                LocalDateTime.now().plusDays(7));
    }

    private void mockarBuscas() {
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar("123")).thenReturn(estudante);
    }

    private Submissao novaSubmissao() {
        return new Submissao(estudante, atividade, CODIGO, 1);
    }

    @Test
    void primeiraSubmissaoTemVersaoUmEStatusSubmetida() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> i.getArgument(0));

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO));

        assertEquals(1, submissao.getVersao());
        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        assertSame(estudante, submissao.getEstudante());
        assertSame(atividade, submissao.getAtividade());
        assertEquals(CODIGO, submissao.getCodigoFonte());
        verify(submissaoRepository).save(submissao);
    }

    @Test
    void novaSubmissaoDoMesmoEstudanteNaMesmaAtividadeIncrementaAVersao() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(2L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> i.getArgument(0));

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO));

        assertEquals(3, submissao.getVersao());
    }

    @Test
    void matriculaComEspacosEhNormalizadaNaBusca() {
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar(" 123 ")).thenReturn(estudante);
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> i.getArgument(0));

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest(" 123 ", CODIGO));

        assertEquals("123", submissao.getEstudante().getMatricula());
    }

    @Test
    void exigeMatricula() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest(" ", CODIGO)));

        assertEquals("A matrícula do estudante é obrigatória.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void exigeCorpoDaRequisicao() {
        assertThrows(IllegalArgumentException.class, () -> submissaoService.submeter(1L, null));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoAtividadeNaoExiste() {
        when(atividadeService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Atividade não encontrada."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> submissaoService.submeter(99L, new SubmissaoRequest("123", CODIGO)));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoEstudanteNaoExiste() {
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar("999")).thenThrow(new RecursoNaoEncontradoException("Estudante não encontrado."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("999", CODIGO)));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void negaAcessoQuandoEstudanteNaoPertenceATurmaDaAtividade() {
        Estudante outro = new Estudante("João", "456", "joao@email.com");
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar("456")).thenReturn(outro);

        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("456", CODIGO)));

        assertEquals("Somente estudantes da turma da atividade podem realizar submissões.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void negaAcessoQuandoEstudanteFoiRemovidoDaTurma() {
        atividade.getTurma().removerEstudante("123");
        mockarBuscas();

        assertThrows(AcessoNegadoException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO)));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoAtividadeEstaConcluida() {
        atividade.encerrar();
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO)));

        assertEquals("Atividades concluídas não aceitam novas submissões.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoCodigoNaoEhJava() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", "print('oi')")));

        assertEquals("A solução deve ser submetida em Java.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    // Encaminhamento para a fila, transições e consultas

    @Test
    void submeterPublicaEventoComOIdDaSubmissaoSemNotificar() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> {
            Submissao salva = i.getArgument(0);
            ReflectionTestUtils.setField(salva, "id", 10L);
            return salva;
        });

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO));

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        verify(eventos).publishEvent(new SubmissaoCriadaEvent(10L));
        verifyNoInteractions(notificador);
    }

    @Test
    void submissaoRejeitadaNaoEhEncaminhadaParaAFila() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);

        assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", "print('oi')")));

        verifyNoInteractions(eventos);
    }

    @Test
    void encaminharPassaParaEmFilaENotificaOEstudante() {
        Submissao submissao = novaSubmissao();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        submissaoService.encaminhar(1L);

        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        ArgumentCaptor<HistoricoSubmissao> registro = ArgumentCaptor.forClass(HistoricoSubmissao.class);
        verify(notificador).notificar(registro.capture());
        assertEquals(StatusSubmissao.SUBMETIDA, registro.getValue().getStatusAnterior());
        assertEquals(StatusSubmissao.EM_FILA, registro.getValue().getStatusNovo());
    }

    @Test
    void fluxoCompletoPeloServicePassaPorTodosOsEstadosENotificaCadaMudanca() {
        Submissao submissao = novaSubmissao();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        submissaoService.encaminhar(1L);
        Submissao emProcessamento = submissaoService.iniciarProcessamento(1L);
        assertSame(submissao, emProcessamento);
        assertEquals(StatusSubmissao.PROCESSANDO, emProcessamento.getStatus());
        submissaoService.concluir(1L);

        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());
        assertEquals(4, submissao.getHistorico().size());
        verify(notificador, times(3)).notificar(any(HistoricoSubmissao.class));
    }

    @Test
    void concluirAvisaOEstudanteQueOResultadoEstaDisponivel() {
        Submissao submissao = novaSubmissao();
        submissao.encaminhar();
        submissao.iniciarProcessamento();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        submissaoService.concluir(1L);

        verify(notificador).notificarResultadoDisponivel(submissao);
    }

    @Test
    void falharNaoAvisaQueOResultadoEstaDisponivel() {
        Submissao submissao = novaSubmissao();
        submissao.encaminhar();
        submissao.iniciarProcessamento();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        submissaoService.falhar(1L);

        verify(notificador, never()).notificarResultadoDisponivel(any());
    }

    @Test
    void falharMarcaASubmissaoComoFalhaENotifica() {
        Submissao submissao = novaSubmissao();
        submissao.encaminhar();
        submissao.iniciarProcessamento();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        submissaoService.falhar(1L);

        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
        ArgumentCaptor<HistoricoSubmissao> registro = ArgumentCaptor.forClass(HistoricoSubmissao.class);
        verify(notificador).notificar(registro.capture());
        assertEquals(StatusSubmissao.PROCESSANDO, registro.getValue().getStatusAnterior());
        assertEquals(StatusSubmissao.FALHA, registro.getValue().getStatusNovo());
    }

    @Test
    void transicaoInvalidaNaoNotificaENaoAlteraASubmissao() {
        Submissao submissao = novaSubmissao();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        assertThrows(IllegalStateException.class, () -> submissaoService.concluir(1L));

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        assertEquals(1, submissao.getHistorico().size());
        verifyNoInteractions(notificador);
    }

    @Test
    void transicoesDeSubmissaoInexistenteLancamNaoEncontrado() {
        when(submissaoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> submissaoService.encaminhar(99L));
        assertEquals("Submissão não encontrada.", erro.getMessage());
        assertThrows(RecursoNaoEncontradoException.class, () -> submissaoService.iniciarProcessamento(99L));
        assertThrows(RecursoNaoEncontradoException.class, () -> submissaoService.concluir(99L));
        assertThrows(RecursoNaoEncontradoException.class, () -> submissaoService.falhar(99L));
        verifyNoInteractions(notificador);
    }

    @Test
    void listarDoEstudanteRetornaEstadoEHistoricoDeCadaSubmissao() {
        Submissao primeira = new Submissao(estudante, atividade, CODIGO, 1);
        Submissao segunda = new Submissao(estudante, atividade, CODIGO, 2);
        segunda.encaminhar();
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(submissaoRepository.findByEstudanteMatriculaOrderByDataHoraDesc("123"))
                .thenReturn(List.of(segunda, primeira));

        List<AcompanhamentoSubmissaoResponse> respostas = submissaoService.listarDoEstudante("123");

        assertEquals(2, respostas.size());
        assertEquals(2, respostas.get(0).versao());
        assertEquals(StatusSubmissao.EM_FILA, respostas.get(0).status());
        assertEquals(2, respostas.get(0).historico().size());
        assertEquals(1, respostas.get(1).versao());
        assertEquals(StatusSubmissao.SUBMETIDA, respostas.get(1).status());
        assertEquals("123", respostas.get(1).matricula());
    }

    @Test
    void buscarDoEstudanteRetornaAndamentoDaSubmissao() {
        Submissao submissao = novaSubmissao();
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(submissaoRepository.findByIdAndEstudanteMatricula(5L, "123")).thenReturn(Optional.of(submissao));

        AcompanhamentoSubmissaoResponse resposta = submissaoService.buscarDoEstudante("123", 5L);

        assertEquals("123", resposta.matricula());
        assertEquals(StatusSubmissao.SUBMETIDA, resposta.status());
        assertEquals(1, resposta.historico().size());
        assertNull(resposta.historico().get(0).statusAnterior());
        assertEquals(StatusSubmissao.SUBMETIDA, resposta.historico().get(0).statusNovo());
    }

    @Test
    void buscarSubmissaoDeOutroEstudanteOuInexistenteLancaNaoEncontrado() {
        Estudante outro = new Estudante("João", "456", "joao@email.com");
        when(estudanteService.buscar("456")).thenReturn(outro);
        when(submissaoRepository.findByIdAndEstudanteMatricula(5L, "456")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> submissaoService.buscarDoEstudante("456", 5L));

        assertEquals("Submissão não encontrada.", erro.getMessage());
    }

    @Test
    void reprocessarVoltaParaEmFilaNotificaEPublicaEventoSemCriarNovaSubmissao() {
        Submissao submissao = novaSubmissao();
        submissao.encaminhar();
        submissao.iniciarProcessamento();
        submissao.falhar();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        AcompanhamentoSubmissaoResponse resposta = submissaoService.reprocessar(1L);

        assertEquals(StatusSubmissao.EM_FILA, resposta.status());
        assertEquals(5, resposta.historico().size());
        ArgumentCaptor<HistoricoSubmissao> registro = ArgumentCaptor.forClass(HistoricoSubmissao.class);
        verify(notificador).notificar(registro.capture());
        assertEquals(StatusSubmissao.FALHA, registro.getValue().getStatusAnterior());
        assertEquals(StatusSubmissao.EM_FILA, registro.getValue().getStatusNovo());
        verify(eventos).publishEvent(any(SubmissaoReprocessadaEvent.class));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void reprocessarSubmissaoQueNaoFalhouNaoNotificaNemReenfileira() {
        Submissao submissao = novaSubmissao();
        when(submissaoRepository.findById(1L)).thenReturn(Optional.of(submissao));

        assertThrows(TransicaoInvalidaException.class, () -> submissaoService.reprocessar(1L));

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        verifyNoInteractions(notificador, eventos);
    }

    @Test
    void reprocessarSubmissaoInexistenteLancaNaoEncontrado() {
        when(submissaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> submissaoService.reprocessar(99L));
    }
}
