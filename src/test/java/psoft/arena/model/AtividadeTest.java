package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtividadeTest {

    private Problema problema;

    @BeforeEach
    void setUp() {
        problema = new Problema("Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC");
    }

    @Test
    void criarComProblemaAtivo() {
        Atividade atividade = new Atividade(problema);

        assertSame(problema, atividade.getProblema());
    }

    @Test
    void naoDeveCriarComProblemaInativo() {
        problema.desativar();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Atividade(problema));
        assertEquals("Problemas inativos não podem ser associados a novas atividades.", e.getMessage());
    }
}
