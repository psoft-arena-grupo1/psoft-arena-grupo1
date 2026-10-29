import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProblemaTest {

    private Problema problema;

    @BeforeEach
    void setUp() {
        problema = new Problema(1, "Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC");
    }

    @Test
    void deveSerCriadoAtivo() {
        assertTrue(problema.isAtivo());
        assertEquals("Ativo", problema.getStatus());
    }

    @Test
    void deveSerCriadoNaoUtilizado() {
        assertFalse(problema.isUtilizado());
    }

    @Test
    void desativarDeveMudarStatusParaInativo() {
        problema.desativar();

        assertFalse(problema.isAtivo());
        assertEquals("Inativo", problema.getStatus());
    }

    @Test
    void marcarComoUtilizado() {
        problema.marcarComoUtilizado();

        assertTrue(problema.isUtilizado());
    }
}
