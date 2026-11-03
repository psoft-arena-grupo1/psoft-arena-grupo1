package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtividadeTest {

    private Problema problema;
    private Turma turma;
    private Estudante estudante;

    @BeforeEach
    void setUp() {
        problema = new Problema("Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC");
        turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        estudante = new Estudante("Ana", "111", "ana@email.com");
    }

    @Test
    void criarComProblemaETurmaAtivos() {
        Atividade atividade = new Atividade(problema, turma);

        assertSame(problema, atividade.getProblema());
        assertSame(turma, atividade.getTurma());
    }

    @Test
    void naoDeveCriarComProblemaInativo() {
        problema.desativar();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Atividade(problema, turma));
        assertEquals("Problemas inativos não podem ser associados a novas atividades.", e.getMessage());
    }

    @Test
    void naoDeveCriarComTurmaEncerrada() {
        turma.encerrar();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Atividade(problema, turma));
        assertEquals("Turmas encerradas não podem receber novas atividades.", e.getMessage());
    }

    @Test
    void estudanteDaTurmaPodeParticipar() {
        turma.adicionarEstudante(estudante);
        Atividade atividade = new Atividade(problema, turma);

        assertDoesNotThrow(() -> atividade.garantirParticipante(estudante));
    }

    @Test
    void estudanteForaDaTurmaNaoPodeParticipar() {
        Atividade atividade = new Atividade(problema, turma);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> atividade.garantirParticipante(estudante));
        assertEquals("Apenas estudantes da turma podem ser adicionados a atividade.", e.getMessage());
    }

    @Test
    void toStringContemNomeDaTurmaETituloDoProblema() {
        String texto = new Atividade(problema, turma).toString();

        assertTrue(texto.contains("turma: POO"));
        assertTrue(texto.contains("problema: Biblioteca"));
    }
}
