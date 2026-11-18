package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TurmaTest {

    private Turma turma;
    private Estudante ana;
    private Estudante bia;

    @BeforeEach
    void setUp() {
        turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        ana = new Estudante("Ana", "111", "ana@email.com");
        bia = new Estudante("Bia", "222", "bia@email.com");
    }

    @Test
    void turmaNovaAtivaESemEstudantes() {
        assertEquals(StatusTurma.ATIVA, turma.getStatus());
        assertTrue(turma.getEstudantes().isEmpty());
    }

    @Test
    void adicionaEstudanteEmTurmaAtiva() {
        turma.adicionarEstudante(ana);

        assertEquals(1, turma.getEstudantes().size());
        assertTrue(turma.possuiEstudante("111"));
    }

    @Test
    void turmaPodePossuirVariosEstudantes() {
        turma.adicionarEstudante(ana);
        turma.adicionarEstudante(bia);

        assertEquals(2, turma.getEstudantes().size());
        assertTrue(turma.possuiEstudante("111"));
        assertTrue(turma.possuiEstudante("222"));
    }

    @Test
    void naoPermiteAdicionarMesmoEstudanteDuasVezes() {
        turma.adicionarEstudante(ana);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> turma.adicionarEstudante(ana));

        assertEquals("Estudante já está matriculado nesta turma.", erro.getMessage());
        assertEquals(1, turma.getEstudantes().size());
    }

    @Test
    void naoPermiteDuplicidadeMesmoComOutraInstanciaDaMesmaMatricula() {
        turma.adicionarEstudante(ana);
        Estudante outraInstancia = new Estudante("Ana Clara", "111", "outro@email.com");

        assertThrows(IllegalArgumentException.class, () -> turma.adicionarEstudante(outraInstancia));
        assertEquals(1, turma.getEstudantes().size());
    }

    @Test
    void naoPermiteAdicionarEstudanteEmTurmaEncerrada() {
        turma.encerrar();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> turma.adicionarEstudante(ana));

        assertEquals("Não é possível alterar os estudantes de uma turma inativa.", erro.getMessage());
        assertTrue(turma.getEstudantes().isEmpty());
    }

    @Test
    void removeEstudanteMatriculado() {
        turma.adicionarEstudante(ana);
        turma.adicionarEstudante(bia);

        turma.removerEstudante("111");

        assertFalse(turma.possuiEstudante("111"));
        assertTrue(turma.possuiEstudante("222"));
        assertEquals(1, turma.getEstudantes().size());
    }

    @Test
    void naoPermiteRemoverEstudanteDeTurmaEncerrada() {
        turma.adicionarEstudante(ana);
        turma.encerrar();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> turma.removerEstudante("111"));

        assertEquals("Não é possível alterar os estudantes de uma turma inativa.", erro.getMessage());
        assertTrue(turma.possuiEstudante("111"));
    }

    @Test
    void mesmoEstudantePodeEstarEmVariasTurmas() {
        Turma outraTurma = new Turma("ED", "PSOFT02", "2026.1", "Prof. Souza");

        turma.adicionarEstudante(ana);
        outraTurma.adicionarEstudante(ana);

        assertTrue(turma.possuiEstudante("111"));
        assertTrue(outraTurma.possuiEstudante("111"));
    }

    @Test
    void possuiEstudanteRetornaFalsoParaMatriculaDesconhecida() {
        turma.adicionarEstudante(ana);

        assertFalse(turma.possuiEstudante("999"));
    }

    @Test
    void garantirAtivaNaoLancaErroEmTurmaAtiva() {
        turma.garantirAtiva();
    }

    @Test
    void garantirAtivaLancaErroEmTurmaEncerrada() {
        turma.encerrar();

        assertThrows(IllegalArgumentException.class, () -> turma.garantirAtiva());
    }
}
