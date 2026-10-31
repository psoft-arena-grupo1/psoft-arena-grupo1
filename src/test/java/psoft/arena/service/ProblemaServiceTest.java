package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.AtividadeRequest;
import psoft.arena.dto.ProblemaRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Problema;

@SpringBootTest
@Transactional
class ProblemaServiceTest {

    @Autowired
    private ProblemaService problemaService;

    @Autowired
    private AtividadeService atividadeService;

    private Long criarProblemaPadrao(String titulo) {
        return problemaService.criar(new ProblemaRequest(titulo, "Descrição", "Requisitos funcionais", "Requisitos de projeto")).getId();
    }

    private Long criarAtividade(Long idProblema) {
        return atividadeService.criar(new AtividadeRequest(idProblema)).getId();
    }

    @Test
    void criarProblemaDeveGerarIdsDiferentes() {
        assertNotEquals(criarProblemaPadrao("Biblioteca"), criarProblemaPadrao("Loja"));
    }

    @Test
    void criarProblemaDeveGuardarOsCampos() {
        Long id = problemaService.criar(new ProblemaRequest("Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC")).getId();

        Problema p = problemaService.buscar(id);
        assertEquals("Biblioteca", p.getTitulo());
        assertEquals("Sistema de biblioteca", p.getDescricao());
        assertEquals("Emprestar livros", p.getRequisitosFuncionais());
        assertEquals("Usar MVC", p.getRequisitosDeProjeto());
        assertEquals("Ativo", p.getStatus());
    }

    @Test
    void naoDeveCriarProblemaComTituloNulo() {
        assertThrows(IllegalArgumentException.class, () -> problemaService.criar(new ProblemaRequest(null, "d", "rf", "rp")));
    }

    @Test
    void naoDeveCriarProblemaComCampoVazio() {
        assertThrows(IllegalArgumentException.class, () -> problemaService.criar(new ProblemaRequest("t", "", "rf", "rp")));
        assertThrows(IllegalArgumentException.class, () -> problemaService.criar(new ProblemaRequest("t", "d", "   ", "rp")));
        assertThrows(IllegalArgumentException.class, () -> problemaService.criar(new ProblemaRequest("t", "d", "rf", null)));
        assertTrue(problemaService.listar().isEmpty());
    }

    @Test
    void buscarProblemaInexistenteDeveLancarExcecao() {
        RecursoNaoEncontradoException e = assertThrows(RecursoNaoEncontradoException.class, () -> problemaService.buscar(99L));
        assertEquals("Problema não encontrado.", e.getMessage());
    }

    @Test
    void listarProblemas() {
        criarProblemaPadrao("Biblioteca");
        criarProblemaPadrao("Loja");

        assertEquals(2, problemaService.listar().size());
    }

    @Test
    void buscarProblemasPorTituloDeveIgnorarMaiusculasEAceitarTrecho() {
        Long biblioteca = criarProblemaPadrao("Sistema de Biblioteca");
        criarProblemaPadrao("Loja Virtual");
        Long digital = criarProblemaPadrao("Biblioteca Digital");

        List<Problema> encontrados = problemaService.buscarPorTitulo("biblio");

        assertEquals(2, encontrados.size());
        assertTrue(encontrados.contains(problemaService.buscar(biblioteca)));
        assertTrue(encontrados.contains(problemaService.buscar(digital)));
    }

    @Test
    void buscarProblemasPorTituloSemResultado() {
        criarProblemaPadrao("Biblioteca");

        assertTrue(problemaService.buscarPorTitulo("xyz").isEmpty());
    }

    @Test
    void atualizarProblema() {
        Long id = criarProblemaPadrao("Biblioteca");

        problemaService.atualizar(id, new ProblemaRequest("Novo título", "Nova descrição", "Novos RF", "Novos RP"));

        Problema p = problemaService.buscar(id);
        assertEquals("Novo título", p.getTitulo());
        assertEquals("Nova descrição", p.getDescricao());
        assertEquals("Novos RF", p.getRequisitosFuncionais());
        assertEquals("Novos RP", p.getRequisitosDeProjeto());
    }

    @Test
    void naoDeveAtualizarProblemaComCampoVazio() {
        Long id = criarProblemaPadrao("Biblioteca");

        assertThrows(IllegalArgumentException.class, () -> problemaService.atualizar(id, new ProblemaRequest("", "d", "rf", "rp")));
        assertEquals("Biblioteca", problemaService.buscar(id).getTitulo());
    }

    @Test
    void atualizarProblemaInexistenteDeveLancarExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> problemaService.atualizar(99L, new ProblemaRequest("t", "d", "rf", "rp")));
    }

    @Test
    void desativarProblema() {
        Long id = criarProblemaPadrao("Biblioteca");

        problemaService.desativar(id);

        assertEquals("Inativo", problemaService.buscar(id).getStatus());
    }

    @Test
    void naoDeveCriarAtividadeComProblemaInativo() {
        Long id = criarProblemaPadrao("Biblioteca");
        problemaService.desativar(id);

        assertThrows(IllegalArgumentException.class, () -> criarAtividade(id));
        assertTrue(atividadeService.listar().isEmpty());
    }

    @Test
    void removerProblemaNaoUtilizado() {
        Long id = criarProblemaPadrao("Biblioteca");

        problemaService.remover(id);

        assertThrows(RecursoNaoEncontradoException.class, () -> problemaService.buscar(id));
    }

    @Test
    void naoDeveRemoverProblemaUtilizadoEmAtividade() {
        Long id = criarProblemaPadrao("Biblioteca");
        criarAtividade(id);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> problemaService.remover(id));
        assertEquals("Problemas já utilizados em atividades não podem ser removidos.", e.getMessage());
        assertNotNull(problemaService.buscar(id));
    }

    @Test
    void naoDeveRemoverProblemaUtilizadoMesmoDepoisDeDesativado() {
        Long id = criarProblemaPadrao("Biblioteca");
        criarAtividade(id);
        problemaService.desativar(id);

        assertThrows(IllegalArgumentException.class, () -> problemaService.remover(id));
    }

    @Test
    void removerProblemaInexistenteDeveLancarExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> problemaService.remover(99L));
    }

    @Test
    void idDeProblemaRemovidoNaoDeveSerReaproveitado() {
        Long id = criarProblemaPadrao("Biblioteca");
        problemaService.remover(id);

        assertTrue(criarProblemaPadrao("Loja") > id);
    }

    @Test
    void criarAtividadeDeveAssociarOProblema() {
        Long idProblema = criarProblemaPadrao("Biblioteca");

        Long idAtividade = criarAtividade(idProblema);

        assertSame(problemaService.buscar(idProblema), atividadeService.buscar(idAtividade).getProblema());
    }

    @Test
    void criarAtividadeComProblemaInexistenteDeveLancarExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> criarAtividade(99L));
    }

    @Test
    void buscarAtividadeInexistenteDeveLancarExcecao() {
        RecursoNaoEncontradoException e = assertThrows(RecursoNaoEncontradoException.class, () -> atividadeService.buscar(99L));
        assertEquals("Atividade não encontrada.", e.getMessage());
    }

    @Test
    void listarAtividades() {
        Long idProblema = criarProblemaPadrao("Biblioteca");
        criarAtividade(idProblema);
        criarAtividade(idProblema);

        assertEquals(2, atividadeService.listar().size());
    }
}
