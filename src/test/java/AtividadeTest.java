import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtividadeTest {

    private Problema problema;

    @BeforeEach
    void setUp() {
        problema = new Problema(1, "Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC");
    }

    @Test
    void criarComProblemaAtivo() {
        Atividade atividade = new Atividade(1, problema);

        assertEquals(1, atividade.getId());
        assertSame(problema, atividade.getProblema());
    }

    @Test
    void criarDeveMarcarProblemaComoUtilizado() {
        new Atividade(1, problema);

        assertTrue(problema.isUtilizado());
    }

    @Test
    void naoDeveCriarComProblemaInativo() {
        problema.desativar();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Atividade(1, problema));
        assertEquals("Problemas inativos não podem ser associados a novas atividades.", e.getMessage());
        assertFalse(problema.isUtilizado());
    }
}
