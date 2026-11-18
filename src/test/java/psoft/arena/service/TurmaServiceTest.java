package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import psoft.arena.dto.MatriculaRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Estudante;
import psoft.arena.model.Turma;
import psoft.arena.repository.TurmaRepository;

@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {

    @Mock
    private TurmaRepository turmaRepository;
    @Mock
    private EstudanteService estudanteService;
    @InjectMocks
    private TurmaService turmaService;

    private Turma turma;
    private Estudante ana;

    @BeforeEach
    void setUp() {
        turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        ana = new Estudante("Ana", "111", "ana@email.com");
    }

    // adicionarEstudante

    @Test
    void adicionaEstudanteNaTurma() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(estudanteService.buscar("111")).thenReturn(ana);

        Turma resultado = turmaService.adicionarEstudante(1L, new MatriculaRequest("111"));

        assertSame(turma, resultado);
        assertTrue(resultado.possuiEstudante("111"));
    }

    @Test
    void adicionarExigeMatricula() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> turmaService.adicionarEstudante(1L, new MatriculaRequest("  ")));

        assertEquals("Insira a matrícula.", erro.getMessage());
        verify(turmaRepository, never()).findById(1L);
    }

    @Test
    void adicionarRejeitaMatriculaNula() {
        assertThrows(IllegalArgumentException.class,
                () -> turmaService.adicionarEstudante(1L, new MatriculaRequest(null)));
    }

    @Test
    void adicionarRejeitaCorpoNulo() {
        assertThrows(IllegalArgumentException.class, () -> turmaService.adicionarEstudante(1L, null));
    }

    @Test
    void adicionarFalhaQuandoTurmaNaoExiste() {
        when(turmaRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> turmaService.adicionarEstudante(99L, new MatriculaRequest("111")));

        assertEquals("Turma não encontrada.", erro.getMessage());
    }

    @Test
    void adicionarFalhaQuandoEstudanteNaoExiste() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(estudanteService.buscar("999")).thenThrow(new RecursoNaoEncontradoException("Estudante não encontrado."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> turmaService.adicionarEstudante(1L, new MatriculaRequest("999")));
        assertTrue(turma.getEstudantes().isEmpty());
    }

    @Test
    void adicionarFalhaEmTurmaEncerrada() {
        turma.encerrar();
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(estudanteService.buscar("111")).thenReturn(ana);

        assertThrows(IllegalArgumentException.class,
                () -> turmaService.adicionarEstudante(1L, new MatriculaRequest("111")));
        assertTrue(turma.getEstudantes().isEmpty());
    }

    @Test
    void adicionarFalhaQuandoEstudanteJaEstaNaTurma() {
        turma.adicionarEstudante(ana);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));
        when(estudanteService.buscar("111")).thenReturn(ana);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> turmaService.adicionarEstudante(1L, new MatriculaRequest("111")));

        assertEquals("Estudante já está matriculado nesta turma.", erro.getMessage());
        assertEquals(1, turma.getEstudantes().size());
    }

    // removeEstudante

    @Test
    void removeEstudanteDaTurma() {
        turma.adicionarEstudante(ana);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        Turma resultado = turmaService.removeEstudante(1L, "111");

        assertTrue(resultado.getEstudantes().isEmpty());
    }


    @Test
    void removerFalhaQuandoTurmaNaoExiste() {
        when(turmaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> turmaService.removeEstudante(99L, "111"));
    }

    @Test
    void removerFalhaQuandoEstudanteNaoEstaNaTurma() {
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> turmaService.removeEstudante(1L, "999"));

        assertEquals("Estudante não está matriculado nesta turma.", erro.getMessage());
    }

    @Test
    void removerFalhaEmTurmaEncerrada() {
        turma.adicionarEstudante(ana);
        turma.encerrar();
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        assertThrows(IllegalArgumentException.class, () -> turmaService.removeEstudante(1L, "111"));
        assertEquals(1, turma.getEstudantes().size());
    }


    @Test
    void listaEstudantesDaTurma() {
        turma.adicionarEstudante(ana);
        when(turmaRepository.findById(1L)).thenReturn(Optional.of(turma));

        List<Estudante> estudantes = turmaService.listarEstudantes(1L);

        assertEquals(1, estudantes.size());
        assertSame(ana, estudantes.get(0));
    }


    @Test
    void listarEstudantesFalhaQuandoTurmaNaoExiste() {
        when(turmaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> turmaService.listarEstudantes(99L));
    }
}
