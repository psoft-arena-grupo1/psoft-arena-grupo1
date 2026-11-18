package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import psoft.arena.dto.AtividadeRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.model.Problema;
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
    @InjectMocks
    private AtividadeService atividadeService;

    private Problema problema;
    private Turma turma;

    @BeforeEach
    void setUp() {
        problema = new Problema("Sistema de biblioteca", "Descrição", "Requisitos funcionais", "Requisitos de projeto");
        turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
    }

    @Test
    void criaAtividadeParaProblemaETurmaAtivos() {
        when(problemaService.buscar(1L)).thenReturn(problema);
        when(turmaService.buscar(2L)).thenReturn(turma);
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(i -> i.getArgument(0));

        Atividade atividade = atividadeService.criar(new AtividadeRequest(1L, 2L));

        assertSame(problema, atividade.getProblema());
        assertSame(turma, atividade.getTurma());
        verify(atividadeRepository).save(atividade);
    }

    @Test
    void criarExigeTurma() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atividadeService.criar(new AtividadeRequest(1L, null)));

        assertEquals("A turma é obrigatório.", erro.getMessage());
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarFalhaQuandoTurmaNaoExiste() {
        when(problemaService.buscar(1L)).thenReturn(problema);
        when(turmaService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Turma não encontrada."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> atividadeService.criar(new AtividadeRequest(1L, 99L)));
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarFalhaQuandoProblemaNaoExiste() {
        when(problemaService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Problema não encontrado."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> atividadeService.criar(new AtividadeRequest(99L, 2L)));
        verify(atividadeRepository, never()).save(any());
    }

    @Test
    void criarFalhaQuandoTurmaEstaEncerrada() {
        turma.encerrar();
        when(problemaService.buscar(1L)).thenReturn(problema);
        when(turmaService.buscar(2L)).thenReturn(turma);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> atividadeService.criar(new AtividadeRequest(1L, 2L)));

        assertEquals("Turmas encerradas não podem receber novas atividades.", erro.getMessage());
        verify(atividadeRepository, never()).save(any());
    }

}
