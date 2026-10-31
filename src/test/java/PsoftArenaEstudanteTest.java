import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PsoftArenaEstudanteTest {

    private PsoftArena arena;

    @BeforeEach
    void setUp() {
        arena = new PsoftArena();
    }

    private String criarEstudantePadrao(String matricula) {
        return arena.criarEstudante("Estudante " + matricula, matricula, matricula + "@email.com");
    }

    // ---------- criar ----------

    @Test
    void criarEstudanteDeveRetornarMatricula() {
        assertEquals("123", arena.criarEstudante("Maria", "123", "maria@email.com"));
    }

    @Test
    void criarEstudanteDeveGuardarOsCampos() {
        String matricula = arena.criarEstudante("Maria Silva", "123456789", "maria@email.com");

        Estudante e = arena.buscarEstudante(matricula);
        assertEquals("Maria Silva", e.getNome());
        assertEquals("123456789", e.getMatricula());
        assertEquals("maria@email.com", e.getEmail());
    }

    @Test
    void naoDeveCriarEstudanteComNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante(null, "123", "a@b.com"));
    }

    @Test
    void naoDeveCriarEstudanteComCampoVazio() {
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("", "123", "a@b.com"));
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("Maria", "   ", "a@b.com"));
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("Maria", "123", null));
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("Maria", null, "a@b.com"));
        assertTrue(arena.listarEstudantes().isEmpty());
    }

    @Test
    void naoDeveCriarEstudanteComEmailInvalido() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> arena.criarEstudante("Maria", "123", "maria-sem-arroba"));
        assertEquals("E-mail inválido.", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("Maria", "123", "maria@email"));
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("Maria", "123", "@email.com"));
        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("Maria", "123", "ma ria@email.com"));
        assertTrue(arena.listarEstudantes().isEmpty());
    }

    @Test
    void naoDeveCriarEstudanteComMatriculaRepetida() {
        arena.criarEstudante("Maria", "123", "maria@email.com");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> arena.criarEstudante("João", "123", "joao@email.com"));
        assertEquals("Já existe um estudante com essa matrícula.", e.getMessage());
        assertEquals(1, arena.listarEstudantes().size());
        assertEquals("Maria", arena.buscarEstudante("123").getNome());
    }

    @Test
    void matriculaRepetidaComEspacosTambemDeveSerRejeitada() {
        arena.criarEstudante("Maria", "123", "maria@email.com");

        assertThrows(IllegalArgumentException.class, () -> arena.criarEstudante("João", " 123 ", "joao@email.com"));
        assertEquals(1, arena.listarEstudantes().size());
    }

    @Test
    void estudantesComMatriculasDiferentesPodemTerOMesmoNomeEEmail() {
        arena.criarEstudante("Maria", "123", "maria@email.com");
        arena.criarEstudante("Maria", "456", "maria@email.com");

        assertEquals(2, arena.listarEstudantes().size());
    }

    // ---------- consultar ----------

    @Test
    void buscarEstudanteInexistenteDeveLancarExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> arena.buscarEstudante("999"));
        assertEquals("Estudante não encontrado.", e.getMessage());
    }

    @Test
    void buscarEstudanteComMatriculaNulaDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> arena.buscarEstudante(null));
    }

    @Test
    void listarEstudantes() {
        criarEstudantePadrao("111");
        criarEstudantePadrao("222");

        assertEquals(2, arena.listarEstudantes().size());
    }

    @Test
    void listarEstudantesSemCadastros() {
        assertTrue(arena.listarEstudantes().isEmpty());
    }

    @Test
    void buscarEstudantesPorNomeDeveIgnorarMaiusculasEAceitarTrecho() {
        arena.criarEstudante("Maria Silva", "111", "maria@email.com");
        arena.criarEstudante("João Pereira", "222", "joao@email.com");
        arena.criarEstudante("Ana MARIA Souza", "333", "ana@email.com");

        List<Estudante> encontrados = arena.buscarEstudantesPorNome("maria");

        assertEquals(2, encontrados.size());
        assertTrue(encontrados.contains(arena.buscarEstudante("111")));
        assertTrue(encontrados.contains(arena.buscarEstudante("333")));
    }

    @Test
    void buscarEstudantesPorNomeSemResultado() {
        criarEstudantePadrao("111");

        assertTrue(arena.buscarEstudantesPorNome("xyz").isEmpty());
    }

    // ---------- editar ----------

    @Test
    void atualizarEstudante() {
        String matricula = criarEstudantePadrao("123");

        arena.atualizarEstudante(matricula, "Novo Nome", "novo@email.com");

        Estudante e = arena.buscarEstudante(matricula);
        assertEquals("Novo Nome", e.getNome());
        assertEquals("novo@email.com", e.getEmail());
        assertEquals("123", e.getMatricula());
    }

    @Test
    void atualizarEstudanteNaoDeveCriarNovoCadastro() {
        String matricula = criarEstudantePadrao("123");

        arena.atualizarEstudante(matricula, "Novo Nome", "novo@email.com");

        assertEquals(1, arena.listarEstudantes().size());
    }

    @Test
    void naoDeveAtualizarEstudanteComCampoVazio() {
        String matricula = criarEstudantePadrao("123");

        assertThrows(IllegalArgumentException.class, () -> arena.atualizarEstudante(matricula, "", "novo@email.com"));
        assertThrows(IllegalArgumentException.class, () -> arena.atualizarEstudante(matricula, "Novo", "  "));
        assertEquals("Estudante 123", arena.buscarEstudante(matricula).getNome());
        assertEquals("123@email.com", arena.buscarEstudante(matricula).getEmail());
    }

    @Test
    void naoDeveAtualizarEstudanteComEmailInvalido() {
        String matricula = criarEstudantePadrao("123");

        assertThrows(IllegalArgumentException.class, () -> arena.atualizarEstudante(matricula, "Novo", "invalido"));
        assertEquals("Estudante 123", arena.buscarEstudante(matricula).getNome());
        assertEquals("123@email.com", arena.buscarEstudante(matricula).getEmail());
    }

    @Test
    void atualizarEstudanteInexistenteDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> arena.atualizarEstudante("999", "Nome", "a@b.com"));
    }

    @Test
    void atualizarEstudanteNaoDeveAfetarOutrosEstudantes() {
        criarEstudantePadrao("111");
        criarEstudantePadrao("222");

        arena.atualizarEstudante("111", "Outro Nome", "outro@email.com");

        assertEquals("Estudante 222", arena.buscarEstudante("222").getNome());
        assertEquals("222@email.com", arena.buscarEstudante("222").getEmail());
    }
}