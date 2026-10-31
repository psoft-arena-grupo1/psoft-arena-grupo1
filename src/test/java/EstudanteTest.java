import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstudanteTest {

    private Estudante estudante;

    @BeforeEach
    void setUp() {
        estudante = new Estudante("Maria Silva", "123456789", "maria@email.com");
    }

    @Test
    void deveGuardarOsCampos() {
        assertEquals("Maria Silva", estudante.getNome());
        assertEquals("123456789", estudante.getMatricula());
        assertEquals("maria@email.com", estudante.getEmail());
    }

    @Test
    void alterarNome() {
        estudante.setNome("Maria Souza");

        assertEquals("Maria Souza", estudante.getNome());
    }

    @Test
    void alterarEmail() {
        estudante.setEmail("maria.souza@email.com");

        assertEquals("maria.souza@email.com", estudante.getEmail());
    }

    @Test
    void alterarNomeEEmailNaoDeveAlterarMatricula() {
        estudante.setNome("Outro Nome");
        estudante.setEmail("outro@email.com");

        assertEquals("123456789", estudante.getMatricula());
    }
}