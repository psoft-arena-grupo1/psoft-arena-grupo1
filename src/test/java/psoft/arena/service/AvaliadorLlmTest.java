package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import psoft.arena.model.Avaliacao;
import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.Submissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;

@ExtendWith(MockitoExtension.class)
class AvaliadorLlmTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Mock
    private AvaliacaoService avaliacaoService;
    @Mock
    private ComposicaoPrompt composicaoPrompt;
    @Mock
    private ClienteLlm clienteLlm;

    private AvaliadorLlm avaliador;
    private Problema problema;
    private EstrategiaAvaliacao estrategia;
    private ModeloLlm modelo;
    private Atividade atividade;
    private Submissao submissao;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        avaliador = new AvaliadorLlm(avaliacaoService, composicaoPrompt, clienteLlm);

        Estudante estudante = new Estudante("Ana", "123", "ana@email.com");
        Turma turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        problema = new Problema("Biblioteca", "d", "rf", "rp");
        estrategia = new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p");
        modelo = new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave");
        ReflectionTestUtils.setField(modelo, "id", 1L);
        atividade = new Atividade(problema, turma, estrategia, modelo, "Lab 1", "d",
                LocalDateTime.now().plusDays(7));
        submissao = new Submissao(estudante, atividade, CODIGO, 1);
        ReflectionTestUtils.setField(submissao, "id", 10L);

        avaliacao = new Avaliacao(submissao);
        ReflectionTestUtils.setField(avaliacao, "id", 7L);
    }

    private ModeloLlm adicionarSegundoModelo() {
        ModeloLlm segundo = new ModeloLlm("gpt-4o", "https://api.openai.com/v1", "chave2");
        ReflectionTestUtils.setField(segundo, "id", 2L);
        atividade.adicionarLlm(segundo);
        return segundo;
    }

    @Test
    void sucessoComUmModeloRegistraResultadoEConcluiComANotaDoModelo() {
        when(avaliacaoService.iniciar(10L)).thenReturn(avaliacao);
        when(composicaoPrompt.compor(problema, estrategia, CODIGO)).thenReturn("PROMPT");
        when(clienteLlm.avaliar(modelo, "PROMPT"))
                .thenReturn(new RespostaAvaliacaoLlm(new BigDecimal("8.5"), List.of("e1"), "j1"));

        avaliador.avaliar(submissao);

        verify(avaliacaoService).registrarResultado(7L, 1L, new BigDecimal("8.5"), "e1", "j1");
        ArgumentCaptor<BigDecimal> nota = ArgumentCaptor.forClass(BigDecimal.class);
        verify(avaliacaoService).concluir(eq(7L), nota.capture(), eq("e1"), eq("j1"));
        assertEquals(0, nota.getValue().compareTo(new BigDecimal("8.5")));
        verify(avaliacaoService, never()).falhar(any(), any());
    }

    @Test
    void mediaDeDoisModelosConcluiComMediaEApresentaOsDoisModelos() {
        ModeloLlm segundo = adicionarSegundoModelo();
        when(avaliacaoService.iniciar(10L)).thenReturn(avaliacao);
        when(composicaoPrompt.compor(problema, estrategia, CODIGO)).thenReturn("PROMPT");
        when(clienteLlm.avaliar(modelo, "PROMPT"))
                .thenReturn(new RespostaAvaliacaoLlm(new BigDecimal("8.0"), List.of("e1"), "j1"));
        when(clienteLlm.avaliar(segundo, "PROMPT"))
                .thenReturn(new RespostaAvaliacaoLlm(new BigDecimal("6.0"), List.of("e2"), "j2"));

        avaliador.avaliar(submissao);

        verify(avaliacaoService).registrarResultado(7L, 1L, new BigDecimal("8.0"), "e1", "j1");
        verify(avaliacaoService).registrarResultado(7L, 2L, new BigDecimal("6.0"), "e2", "j2");
        ArgumentCaptor<BigDecimal> nota = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<String> evidencias = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> justificativa = ArgumentCaptor.forClass(String.class);
        verify(avaliacaoService).concluir(eq(7L), nota.capture(), evidencias.capture(), justificativa.capture());
        assertEquals(0, nota.getValue().compareTo(new BigDecimal("7.0")));
        assertTrue(evidencias.getValue().contains("grok-3"));
        assertTrue(evidencias.getValue().contains("gpt-4o"));
        assertTrue(justificativa.getValue().contains("grok-3"));
        assertTrue(justificativa.getValue().contains("gpt-4o"));
        verify(avaliacaoService, never()).falhar(any(), any());
    }

    @Test
    void falhaDeUmDosModelosRegistraFalhaEConsideraAvaliacaoComoFalha() {
        ModeloLlm segundo = adicionarSegundoModelo();
        when(avaliacaoService.iniciar(10L)).thenReturn(avaliacao);
        when(composicaoPrompt.compor(problema, estrategia, CODIGO)).thenReturn("PROMPT");
        when(clienteLlm.avaliar(modelo, "PROMPT"))
                .thenReturn(new RespostaAvaliacaoLlm(new BigDecimal("8.0"), List.of("e1"), "j1"));
        when(clienteLlm.avaliar(segundo, "PROMPT")).thenThrow(new FalhaAvaliacaoException("timeout"));

        FalhaAvaliacaoException erro = assertThrows(FalhaAvaliacaoException.class,
                () -> avaliador.avaliar(submissao));

        assertTrue(erro.getMessage().contains("timeout"));
        verify(avaliacaoService).registrarFalhaModelo(eq(7L), eq(2L), contains("timeout"));
        verify(avaliacaoService, times(1)).falhar(eq(7L), contains("timeout"));
        verify(avaliacaoService, never()).concluir(any(), any(), any(), any());
    }

    @Test
    void resultadoInvalidoDoModeloEhTratadoComoFalha() {
        when(avaliacaoService.iniciar(10L)).thenReturn(avaliacao);
        when(composicaoPrompt.compor(problema, estrategia, CODIGO)).thenReturn("PROMPT");
        when(clienteLlm.avaliar(modelo, "PROMPT")).thenThrow(new IllegalArgumentException("nota inválida"));

        assertThrows(FalhaAvaliacaoException.class, () -> avaliador.avaliar(submissao));

        verify(avaliacaoService).registrarFalhaModelo(eq(7L), eq(1L), contains("nota inválida"));
        verify(avaliacaoService, times(1)).falhar(eq(7L), contains("nota inválida"));
        verify(avaliacaoService, never()).registrarResultado(any(), any(), any(), any(), any());
        verify(avaliacaoService, never()).concluir(any(), any(), any(), any());
    }

    @Test
    void submissaoJaAvaliadaPropagaIllegalStateSemChamarOCliente() {
        when(avaliacaoService.iniciar(10L))
                .thenThrow(new IllegalStateException("Esta submissão já possui avaliação."));

        assertThrows(IllegalStateException.class, () -> avaliador.avaliar(submissao));

        verifyNoInteractions(clienteLlm);
        verifyNoInteractions(composicaoPrompt);
        verify(avaliacaoService, never()).falhar(any(), any());
    }
}
