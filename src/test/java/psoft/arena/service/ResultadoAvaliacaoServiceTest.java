package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import psoft.arena.dto.ResultadoAvaliacaoResponse;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.model.Avaliacao;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.StatusAvaliacao;
import psoft.arena.model.Submissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;
import psoft.arena.repository.AvaliacaoRepository;
import psoft.arena.repository.SubmissaoRepository;

@ExtendWith(MockitoExtension.class)
class ResultadoAvaliacaoServiceTest {

    private static final String INDISPONIVEL = "O resultado da avaliação ainda não está disponível.";

    @Mock
    private EstudanteService estudanteService;
    @Mock
    private SubmissaoRepository submissaoRepository;
    @Mock
    private AvaliacaoRepository avaliacaoRepository;
    @InjectMocks
    private ResultadoAvaliacaoService resultadoService;

    private Estudante estudante;
    private Submissao submissao;
    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        estudante = new Estudante("Maria", "123", "maria@email.com");
        Turma turma = new Turma("POO", "P1", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        Atividade atividade = new Atividade(new Problema("Biblioteca", "d", "rf", "rp"), turma,
                new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"),
                new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"), "Lab 1", "Descrição",
                LocalDateTime.now().plusDays(7));
        ReflectionTestUtils.setField(atividade, "id", 10L);
        submissao = new Submissao(estudante, atividade, "public class A { }", 2);
        ReflectionTestUtils.setField(submissao, "id", 1L);
        avaliacao = new Avaliacao(submissao);
    }

    private void mockarBuscas() {
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(submissaoRepository.findByIdAndEstudanteMatricula(1L, "123")).thenReturn(Optional.of(submissao));
        when(avaliacaoRepository.findBySubmissaoId(1L)).thenReturn(Optional.of(avaliacao));
    }

    @Test
    void retornaNotaJustificativaEvidenciasESituacaoDaAvaliacaoConcluida() {
        avaliacao.concluir(new BigDecimal("8.5"), "Classe Biblioteca coesa", "Bom uso de interfaces.");
        mockarBuscas();

        ResultadoAvaliacaoResponse resultado = resultadoService.buscarDoEstudante("123", 1L);

        assertEquals(1L, resultado.submissaoId());
        assertEquals(10L, resultado.atividadeId());
        assertEquals(2, resultado.versao());
        assertEquals(StatusAvaliacao.CONCLUIDA, resultado.situacao());
        assertEquals(new BigDecimal("8.5"), resultado.nota());
        assertEquals("Bom uso de interfaces.", resultado.justificativa());
        assertEquals(List.of("Classe Biblioteca coesa"), resultado.evidencias());
    }

    @Test
    void evidenciasSaoSeparadasPorLinhaIgnorandoLinhasVazias() {
        avaliacao.concluir(new BigDecimal("7"), "grok-3: evidência 1\n\n  grok-3: evidência 2  \n", "j");
        mockarBuscas();

        ResultadoAvaliacaoResponse resultado = resultadoService.buscarDoEstudante("123", 1L);

        assertEquals(List.of("grok-3: evidência 1", "grok-3: evidência 2"), resultado.evidencias());
    }

    @Test
    void matriculaComEspacosEhNormalizadaPelaMatriculaDoEstudante() {
        avaliacao.concluir(new BigDecimal("9"), "e", "j");
        when(estudanteService.buscar(" 123 ")).thenReturn(estudante);
        when(submissaoRepository.findByIdAndEstudanteMatricula(1L, "123")).thenReturn(Optional.of(submissao));
        when(avaliacaoRepository.findBySubmissaoId(1L)).thenReturn(Optional.of(avaliacao));

        ResultadoAvaliacaoResponse resultado = resultadoService.buscarDoEstudante(" 123 ", 1L);

        assertEquals(new BigDecimal("9"), resultado.nota());
        verify(submissaoRepository).findByIdAndEstudanteMatricula(1L, "123");
    }

    @Test
    void avaliacaoEmProcessamentoNaoDisponibilizaOResultado() {
        mockarBuscas();

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> resultadoService.buscarDoEstudante("123", 1L));

        assertEquals(INDISPONIVEL, erro.getMessage());
    }

    @Test
    void avaliacaoComFalhaNaoDisponibilizaOResultado() {
        avaliacao.falhar("API do modelo fora do ar");
        mockarBuscas();

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> resultadoService.buscarDoEstudante("123", 1L));

        assertEquals("A avaliação desta submissão não foi concluída; o resultado não está disponível.",
                erro.getMessage());
    }

    @Test
    void falhaDaAvaliacaoNaoVazaOMotivoParaOEstudante() {
        avaliacao.falhar("Modelo grok-3: erro 500 da API");
        mockarBuscas();

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> resultadoService.buscarDoEstudante("123", 1L));

        assertFalse(erro.getMessage().contains("grok-3"));
    }

    @Test
    void submissaoAindaSemAvaliacaoNaoTemResultadoDisponivel() {
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(submissaoRepository.findByIdAndEstudanteMatricula(1L, "123")).thenReturn(Optional.of(submissao));
        when(avaliacaoRepository.findBySubmissaoId(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> resultadoService.buscarDoEstudante("123", 1L));

        assertEquals(INDISPONIVEL, erro.getMessage());
    }

    @Test
    void submissaoDeOutroEstudanteOuInexistenteRespondeNaoEncontrada() {
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(submissaoRepository.findByIdAndEstudanteMatricula(99L, "123")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> resultadoService.buscarDoEstudante("123", 99L));

        assertEquals("Submissão não encontrada.", erro.getMessage());
        verifyNoInteractions(avaliacaoRepository);
    }

    @Test
    void estudanteInexistenteRespondeNaoEncontrado() {
        when(estudanteService.buscar("999"))
                .thenThrow(new RecursoNaoEncontradoException("Estudante não encontrado."));

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> resultadoService.buscarDoEstudante("999", 1L));

        assertEquals("Estudante não encontrado.", erro.getMessage());
        verifyNoInteractions(submissaoRepository, avaliacaoRepository);
    }
}