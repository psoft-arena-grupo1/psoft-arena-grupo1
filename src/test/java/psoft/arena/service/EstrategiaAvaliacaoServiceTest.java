package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.EstrategiaRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.TipoEstrategia;

@SpringBootTest
@Transactional
class EstrategiaAvaliacaoServiceTest {

    @Autowired
    private EstrategiaAvaliacaoService estrategiaService;

    private Long criar(String nome, TipoEstrategia tipo, String descricao, String prompt) {
        return estrategiaService.criar(new EstrategiaRequest(nome, tipo, descricao, prompt)).getId();
    }

    private Long criarEstrategiaPadrao(TipoEstrategia tipo) {
        return criar("Estratégia", tipo, "Descrição", "Avalie o código.");
    }

    // ---------- criar ----------

    @Test
    void criarEstrategiaDeveGuardarOsCampos() {
        Long id = criar("Smells básicos", TipoEstrategia.CODE_SMELLS,
                "Procura code smells comuns", "Liste os code smells encontrados.");

        EstrategiaAvaliacao e = estrategiaService.buscar(id);
        assertEquals("Smells básicos", e.getNome());
        assertEquals(TipoEstrategia.CODE_SMELLS, e.getTipo());
        assertEquals("Procura code smells comuns", e.getDescricao());
        assertEquals("Liste os code smells encontrados.", e.getPrompt());
    }

    @Test
    void idsDevemSerGeradosAutomaticamente() {
        Long id1 = criarEstrategiaPadrao(TipoEstrategia.CODE_SMELLS);
        Long id2 = criarEstrategiaPadrao(TipoEstrategia.PADROES_DE_PROJETO);

        assertNotEquals(id1, id2);
        assertEquals(2, estrategiaService.listar().size());
    }

    @Test
    void naoDeveCriarEstrategiaComCampoVazioOuNulo() {
        assertThrows(IllegalArgumentException.class, () -> criar(null, TipoEstrategia.CODE_SMELLS, "d", "p"));
        assertThrows(IllegalArgumentException.class, () -> criar("n", null, "d", "p"));
        assertThrows(IllegalArgumentException.class, () -> criar("n", TipoEstrategia.CODE_SMELLS, "  ", "p"));
        assertThrows(IllegalArgumentException.class, () -> criar("n", TipoEstrategia.CODE_SMELLS, "d", ""));
        assertTrue(estrategiaService.listar().isEmpty());
    }

    @Test
    void deveExistirOsTresTiposDeEstrategia() {
        assertEquals(3, TipoEstrategia.values().length);
        for (TipoEstrategia tipo : TipoEstrategia.values()) {
            criarEstrategiaPadrao(tipo);
        }
        assertEquals(3, estrategiaService.listar().size());
    }

    @Test
    void podemExistirVariasEstrategiasDoMesmoTipoComPromptsDiferentes() {
        Long id1 = criar("A", TipoEstrategia.CODE_SMELLS, "d", "Prompt A");
        Long id2 = criar("B", TipoEstrategia.CODE_SMELLS, "d", "Prompt B");
        criarEstrategiaPadrao(TipoEstrategia.PADROES_DE_PROJETO);

        assertEquals(2, estrategiaService.listarPorTipo(TipoEstrategia.CODE_SMELLS).size());
        assertEquals("Prompt A", estrategiaService.buscar(id1).getPrompt());
        assertEquals("Prompt B", estrategiaService.buscar(id2).getPrompt());
    }

    // ---------- consultar ----------

    @Test
    void buscarEstrategiaInexistenteDeveLancarExcecao() {
        RecursoNaoEncontradoException e = assertThrows(RecursoNaoEncontradoException.class, () -> estrategiaService.buscar(99L));
        assertEquals("Estratégia não encontrada.", e.getMessage());
    }

    // ---------- editar ----------

    @Test
    void atualizarEstrategiaDeveAlterarTodosOsCampos() {
        Long id = criarEstrategiaPadrao(TipoEstrategia.CODE_SMELLS);

        estrategiaService.atualizar(id, new EstrategiaRequest("Nova", TipoEstrategia.ACOPLAMENTO_COESAO_PRINCIPIOS, "Nova desc", "Novo prompt"));

        EstrategiaAvaliacao e = estrategiaService.buscar(id);
        assertEquals("Nova", e.getNome());
        assertEquals(TipoEstrategia.ACOPLAMENTO_COESAO_PRINCIPIOS, e.getTipo());
        assertEquals("Nova desc", e.getDescricao());
        assertEquals("Novo prompt", e.getPrompt());
    }

    @Test
    void atualizarComCampoInvalidoNaoDeveAlterarNada() {
        Long id = criarEstrategiaPadrao(TipoEstrategia.CODE_SMELLS);

        assertThrows(IllegalArgumentException.class,
                () -> estrategiaService.atualizar(id, new EstrategiaRequest("Nova", TipoEstrategia.CODE_SMELLS, "d", " ")));
        assertEquals("Estratégia", estrategiaService.buscar(id).getNome());
    }

    @Test
    void atualizarEstrategiaInexistenteDeveLancarExcecao() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> estrategiaService.atualizar(42L, new EstrategiaRequest("n", TipoEstrategia.CODE_SMELLS, "d", "p")));
    }

    @Test
    void alterarPromptDeveMudarSomenteOPrompt() {
        Long id = criarEstrategiaPadrao(TipoEstrategia.PADROES_DE_PROJETO);

        estrategiaService.alterarPrompt(id, "Verifique o uso do padrão Strategy.");

        EstrategiaAvaliacao e = estrategiaService.buscar(id);
        assertEquals("Verifique o uso do padrão Strategy.", e.getPrompt());
        assertEquals("Estratégia", e.getNome());
        assertEquals(TipoEstrategia.PADROES_DE_PROJETO, e.getTipo());
    }

    @Test
    void naoDeveAlterarPromptParaVazio() {
        Long id = criarEstrategiaPadrao(TipoEstrategia.CODE_SMELLS);

        assertThrows(IllegalArgumentException.class, () -> estrategiaService.alterarPrompt(id, ""));
        assertThrows(IllegalArgumentException.class, () -> estrategiaService.alterarPrompt(id, null));
        assertEquals("Avalie o código.", estrategiaService.buscar(id).getPrompt());
    }
}
