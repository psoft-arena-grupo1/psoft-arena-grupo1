import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PsoftArenaTest {

    private PsoftArena arena;

    @BeforeEach
    void setUp() {
        arena = new PsoftArena();
    }

    private int criarProblemaPadrao(String titulo) {
        return arena.criarProblema(titulo, "Descrição", "Requisitos funcionais", "Requisitos de projeto");
    }

    @Test
    void criarProblemaDeveGerarIdsSequenciais() {
        assertEquals(1, criarProblemaPadrao("Biblioteca"));
        assertEquals(2, criarProblemaPadrao("Loja"));
    }

    @Test
    void criarProblemaDeveGuardarOsCampos() {
        int id = arena.criarProblema("Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC");

        Problema p = arena.buscarProblema(id);
        assertEquals("Biblioteca", p.getTitulo());
        assertEquals("Sistema de biblioteca", p.getDescricao());
        assertEquals("Emprestar livros", p.getRequisitosFuncionais());
        assertEquals("Usar MVC", p.getRequisitosDeProjeto());
        assertEquals("Ativo", p.getStatus());
    }

    @Test
    void naoDeveCriarProblemaComTituloNulo() {
        assertThrows(IllegalArgumentException.class, () -> arena.criarProblema(null, "d", "rf", "rp"));
    }

    @Test
    void naoDeveCriarProblemaComCampoVazio() {
        assertThrows(IllegalArgumentException.class, () -> arena.criarProblema("t", "", "rf", "rp"));
        assertThrows(IllegalArgumentException.class, () -> arena.criarProblema("t", "d", "   ", "rp"));
        assertThrows(IllegalArgumentException.class, () -> arena.criarProblema("t", "d", "rf", null));
        assertTrue(arena.listarProblemas().isEmpty());
    }

    @Test
    void buscarProblemaInexistenteDeveLancarExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> arena.buscarProblema(99));
        assertEquals("Problema não encontrado.", e.getMessage());
    }

    @Test
    void listarProblemas() {
        criarProblemaPadrao("Biblioteca");
        criarProblemaPadrao("Loja");

        assertEquals(2, arena.listarProblemas().size());
    }

    @Test
    void buscarProblemasPorTituloDeveIgnorarMaiusculasEAceitarTrecho() {
        int biblioteca = criarProblemaPadrao("Sistema de Biblioteca");
        criarProblemaPadrao("Loja Virtual");
        int digital = criarProblemaPadrao("Biblioteca Digital");

        List<Problema> encontrados = arena.buscarProblemasPorTitulo("biblio");

        assertEquals(2, encontrados.size());
        assertTrue(encontrados.contains(arena.buscarProblema(biblioteca)));
        assertTrue(encontrados.contains(arena.buscarProblema(digital)));
    }

    @Test
    void buscarProblemasPorTituloSemResultado() {
        criarProblemaPadrao("Biblioteca");

        assertTrue(arena.buscarProblemasPorTitulo("xyz").isEmpty());
    }

    @Test
    void atualizarProblema() {
        int id = criarProblemaPadrao("Biblioteca");

        arena.atualizarProblema(id, "Novo título", "Nova descrição", "Novos RF", "Novos RP");

        Problema p = arena.buscarProblema(id);
        assertEquals("Novo título", p.getTitulo());
        assertEquals("Nova descrição", p.getDescricao());
        assertEquals("Novos RF", p.getRequisitosFuncionais());
        assertEquals("Novos RP", p.getRequisitosDeProjeto());
    }

    @Test
    void naoDeveAtualizarProblemaComCampoVazio() {
        int id = criarProblemaPadrao("Biblioteca");

        assertThrows(IllegalArgumentException.class, () -> arena.atualizarProblema(id, "", "d", "rf", "rp"));
        assertEquals("Biblioteca", arena.buscarProblema(id).getTitulo());
    }

    @Test
    void atualizarProblemaInexistenteDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> arena.atualizarProblema(99, "t", "d", "rf", "rp"));
    }

    @Test
    void desativarProblema() {
        int id = criarProblemaPadrao("Biblioteca");

        arena.desativarProblema(id);

        assertEquals("Inativo", arena.buscarProblema(id).getStatus());
    }

    @Test
    void naoDeveCriarAtividadeComProblemaInativo() {
        int id = criarProblemaPadrao("Biblioteca");
        arena.desativarProblema(id);

        assertThrows(IllegalArgumentException.class, () -> arena.criarAtividade(id));
        assertTrue(arena.listarAtividades().isEmpty());
    }

    @Test
    void removerProblemaNaoUtilizado() {
        int id = criarProblemaPadrao("Biblioteca");

        arena.removerProblema(id);

        assertThrows(IllegalArgumentException.class, () -> arena.buscarProblema(id));
    }

    @Test
    void naoDeveRemoverProblemaUtilizadoEmAtividade() {
        int id = criarProblemaPadrao("Biblioteca");
        arena.criarAtividade(id);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> arena.removerProblema(id));
        assertEquals("Problemas já utilizados em atividades não podem ser removidos.", e.getMessage());
        assertNotNull(arena.buscarProblema(id));
    }

    @Test
    void naoDeveRemoverProblemaUtilizadoMesmoDepoisDeDesativado() {
        int id = criarProblemaPadrao("Biblioteca");
        arena.criarAtividade(id);
        arena.desativarProblema(id);

        assertThrows(IllegalArgumentException.class, () -> arena.removerProblema(id));
    }

    @Test
    void removerProblemaInexistenteDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> arena.removerProblema(99));
    }

    @Test
    void idDeProblemaRemovidoNaoDeveSerReaproveitado() {
        int id = criarProblemaPadrao("Biblioteca");
        arena.removerProblema(id);

        assertEquals(2, criarProblemaPadrao("Loja"));
    }

    @Test
    void criarAtividadeDeveGerarIdsSequenciais() {
        int idProblema = criarProblemaPadrao("Biblioteca");

        assertEquals(1, arena.criarAtividade(idProblema));
        assertEquals(2, arena.criarAtividade(idProblema));
    }

    @Test
    void criarAtividadeDeveAssociarOProblema() {
        int idProblema = criarProblemaPadrao("Biblioteca");

        int idAtividade = arena.criarAtividade(idProblema);

        assertSame(arena.buscarProblema(idProblema), arena.buscarAtividade(idAtividade).getProblema());
    }

    @Test
    void criarAtividadeComProblemaInexistenteDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class, () -> arena.criarAtividade(99));
    }

    @Test
    void falhaAoCriarAtividadeNaoDeveGastarId() {
        int inativo = criarProblemaPadrao("Biblioteca");
        arena.desativarProblema(inativo);
        int ativo = criarProblemaPadrao("Loja");

        assertThrows(IllegalArgumentException.class, () -> arena.criarAtividade(inativo));
        assertEquals(1, arena.criarAtividade(ativo));
    }

    @Test
    void buscarAtividadeInexistenteDeveLancarExcecao() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> arena.buscarAtividade(99));
        assertEquals("Atividade não encontrada.", e.getMessage());
    }

    @Test
    void listarAtividades() {
        int idProblema = criarProblemaPadrao("Biblioteca");
        arena.criarAtividade(idProblema);
        arena.criarAtividade(idProblema);

        assertEquals(2, arena.listarAtividades().size());
    }
}
