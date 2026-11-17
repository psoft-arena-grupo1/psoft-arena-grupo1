package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import psoft.arena.dto.AtividadeEstudanteResponse;
import psoft.arena.dto.AtividadeRequest;
import psoft.arena.exception.AcessoNegadoException;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.StatusAtividade;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;
import psoft.arena.repository.AtividadeRepository;

@ExtendWith(MockitoExtension.class)
class AtividadeServiceTest {

    @Mock
    private AtividadeRepository atividadeRepository;
    @Mock
    private ProblemaService problemaService;
    @Mock
    private TurmaService turmaService;
    @Mock
    private EstrategiaAvaliacaoService estrategiaService;
    @Mock
    private ModeloLlmService modeloLlmService;
    @Mock
    private EstudanteService estudanteService;
    @InjectMocks
    private AtividadeService atividadeService;

    private Problema problema;
    private Turma turma;
    private EstrategiaAvaliacao estrategia;
    private LocalDateTime prazo;
    private ModeloLlm llm;

    @BeforeEach
    void setUp() {
        problema = new Problema("Sistema de biblioteca", "Descrição", "Requisitos funcionais", "Requisitos de projeto");
        turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        estrategia = new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p");
        prazo = LocalDateTime.now().plusDays(7);
        llm = new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave");
    }

    private AtividadeRequest request(Long problemaId, Long turmaId) {
        return new AtividadeRequest(problemaId, turmaId, 3L, 4L, "Lab 1", "Descrição", prazo);
    }

    private void mockarAssociacoes() {
        when(problemaService.buscar(1L)).thenReturn(problema);
        when(turmaService.buscar(2L)).thenReturn(turma);
        when(estrategiaService.buscar(3L)).thenReturn(estrategia);
        when(modeloLlmService.buscar(4L)).thenReturn(llm);
    }

    @Test
    void criaAtividadeAbertaComTodasAsAssociacoes() {
        mockarAssociacoes();
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(i -> i.getArgument(0));

        Atividade atividade = atividadeService.criar(request(1L, 2L));

        assertSame(problema, atividade.getProblema());
        assertSame(turma, atividade.getTurma());
        assertSame(estrategia, atividade.getEstrategia());
        assertSame(llm, atividade.getLlmPadrao());
        assertEquals(StatusAtividade.ABERTA, atividade.getStatus());
        verify(atividadeRepository).save(atividade);
    }

    @Test
    void criarExigeTurma() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atividadeService.criar(request(1L, null)));

        assertEquals("Turma, problema, estratégia de avaliação e LLM padrão são obrigatórios.", erro.getMessage());
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarExigeTitulo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atividadeService.criar(new AtividadeRequest(1L, 2L, 3L, 4L, " ", "Descrição", prazo)));

        assertEquals("Título, descrição e prazo para submissão são obrigatórios.", erro.getMessage());
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarExigeLlmPadrao() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atividadeService.criar(new AtividadeRequest(1L, 2L, 3L, null, "Lab 1", "Descrição", prazo)));

        assertEquals("Turma, problema, estratégia de avaliação e LLM padrão são obrigatórios.", erro.getMessage());
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarFalhaQuandoTurmaNaoExiste() {
        when(problemaService.buscar(1L)).thenReturn(problema);
        when(turmaService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Turma não encontrada."));

        assertThrows(RecursoNaoEncontradoException.class, () -> atividadeService.criar(request(1L, 99L)));
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarFalhaQuandoProblemaNaoExiste() {
        when(problemaService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Problema não encontrado."));

        assertThrows(RecursoNaoEncontradoException.class, () -> atividadeService.criar(request(99L, 2L)));
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarFalhaQuandoTurmaEstaEncerrada() {
        turma.encerrar();
        mockarAssociacoes();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atividadeService.criar(request(1L, 2L)));

        assertEquals("Turmas encerradas não podem receber novas atividades.", erro.getMessage());
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void encerrarConcluiAtividade() {
        Atividade atividade = new Atividade(problema, turma, estrategia, llm, "Lab 1", "Descrição", prazo);
        when(atividadeRepository.findById(5L)).thenReturn(Optional.of(atividade));

        assertEquals(StatusAtividade.CONCLUIDA, atividadeService.encerrar(5L).getStatus());
    }

    @Test
    void listarDoEstudanteRetornaAtividadesDasSuasTurmas() {
        Estudante estudante = new Estudante("Ana", "123", "ana@email.com");
        turma.adicionarEstudante(estudante);
        Atividade atividade = new Atividade(problema, turma, estrategia, llm, "Lab 1", "Descrição", prazo);
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(atividadeRepository.findByTurmaEstudantesMatricula("123")).thenReturn(List.of(atividade));

        List<AtividadeEstudanteResponse> atividades = atividadeService.listarDoEstudante("123");

        assertEquals(1, atividades.size());
        AtividadeEstudanteResponse resposta = atividades.get(0);
        assertEquals("Lab 1", resposta.titulo());
        assertEquals("POO", resposta.turma());
        assertEquals("Sistema de biblioteca", resposta.problema());
        assertEquals("Requisitos funcionais", resposta.requisitosFuncionais());
        assertEquals("Requisitos de projeto", resposta.requisitosDeProjeto());
        assertEquals("Smells", resposta.estrategiaAvaliacao());
        assertEquals(prazo, resposta.prazo());
        assertEquals(StatusAtividade.ABERTA, resposta.status());
    }

    @Test
    void buscarDoEstudanteNegaAcessoAAtividadeDeOutraTurma() {
        Estudante estudante = new Estudante("Ana", "123", "ana@email.com");
        Atividade atividade = new Atividade(problema, turma, estrategia, llm, "Lab 1", "Descrição", prazo);
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(atividadeRepository.findById(5L)).thenReturn(Optional.of(atividade));

        assertThrows(AcessoNegadoException.class, () -> atividadeService.buscarDoEstudante("123", 5L));
    }

    @Test
    void buscarDoEstudanteMostraAtividadeConcluidaSemAceitarSubmissoes() {
        Estudante estudante = new Estudante("Ana", "123", "ana@email.com");
        turma.adicionarEstudante(estudante);
        Atividade atividade = new Atividade(problema, turma, estrategia, llm, "Lab 1", "Descrição", prazo);
        atividade.encerrar();
        when(estudanteService.buscar("123")).thenReturn(estudante);
        when(atividadeRepository.findById(5L)).thenReturn(Optional.of(atividade));

        AtividadeEstudanteResponse resposta = atividadeService.buscarDoEstudante("123", 5L);

        assertEquals(StatusAtividade.CONCLUIDA, resposta.status());
        assertEquals(false, resposta.aceitaSubmissoes());
    }
}
