package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import psoft.arena.dto.EstudanteRequest;
import psoft.arena.dto.EstudanteUpdateRequest;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Estudante;

@SpringBootTest
@Transactional
class EstudanteServiceTest {

    @Autowired
    private EstudanteService estudanteService;

    private String criar(String nome, String matricula, String email) {
        return estudanteService.criar(new EstudanteRequest(nome, matricula, email)).getMatricula();
    }

    private String criarEstudantePadrao(String matricula) {
        return criar("Estudante " + matricula, matricula, matricula + "@email.com");
    }

    private void atualizar(String matricula, String nome, String email) {
        estudanteService.atualizar(matricula, new EstudanteUpdateRequest(nome, email));
    }

    // ---------- criar ----------

    @Test
    void criarEstudanteDeveRetornarMatricula() {
        assertEquals("123", criar("Maria", "123", "maria@email.com"));
    }

    @Test
    void criarEstudanteDeveGuardarOsCampos() {
        String matricula = criar("Maria Silva", "123456789", "maria@email.com");

        Estudante e = estudanteService.buscar(matricula);
        assertEquals("Maria Silva", e.getNome());
        assertEquals("123456789", e.getMatricula());
        assertEquals("maria@email.com", e.getEmail());
    }

    @Test
    void naoDeveCriarEstudanteComNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> criar(null, "123", "a@b.com"));
    }

    @Test
    void naoDeveCriarEstudanteComCampoVazio() {
        assertThrows(IllegalArgumentException.class, () -> criar("", "123", "a@b.com"));
        assertThrows(IllegalArgumentException.class, () -> criar("Maria", "   ", "a@b.com"));
        assertThrows(IllegalArgumentException.class, () -> criar("Maria", "123", null));
        assertThrows(IllegalArgumentException.class, () -> criar("Maria", null, "a@b.com"));
        assertTrue(estudanteService.listar().isEmpty());
    }

    @Test
    void naoDeveCriarEstudanteComEmailInvalido() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> criar("Maria", "123", "maria-sem-arroba"));
        assertEquals("E-mail inválido.", e.getMessage());
        assertThrows(IllegalArgumentException.class, () -> criar("Maria", "123", "maria@email"));
        assertThrows(IllegalArgumentException.class, () -> criar("Maria", "123", "@email.com"));
        assertThrows(IllegalArgumentException.class, () -> criar("Maria", "123", "ma ria@email.com"));
        assertTrue(estudanteService.listar().isEmpty());
    }

    @Test
    void naoDeveCriarEstudanteComMatriculaRepetida() {
        criar("Maria", "123", "maria@email.com");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> criar("João", "123", "joao@email.com"));
        assertEquals("Já existe um estudante com essa matrícula.", e.getMessage());
        assertEquals(1, estudanteService.listar().size());
        assertEquals("Maria", estudanteService.buscar("123").getNome());
    }

    @Test
    void matriculaRepetidaComEspacosTambemDeveSerRejeitada() {
        criar("Maria", "123", "maria@email.com");

        assertThrows(IllegalArgumentException.class, () -> criar("João", " 123 ", "joao@email.com"));
        assertEquals(1, estudanteService.listar().size());
    }

    @Test
    void estudantesComMatriculasDiferentesPodemTerOMesmoNomeEEmail() {
        criar("Maria", "123", "maria@email.com");
        criar("Maria", "456", "maria@email.com");

        assertEquals(2, estudanteService.listar().size());
    }

    // ---------- consultar ----------

    @Test
    void buscarEstudanteInexistenteDeveLancarExcecao() {
        RecursoNaoEncontradoException e = assertThrows(RecursoNaoEncontradoException.class, () -> estudanteService.buscar("999"));
        assertEquals("Estudante não encontrado.", e.getMessage());
    }

    @Test
    void buscarEstudanteComMatriculaNulaDeveLancarExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> estudanteService.buscar(null));
    }

    @Test
    void listarEstudantes() {
        criarEstudantePadrao("111");
        criarEstudantePadrao("222");

        assertEquals(2, estudanteService.listar().size());
    }

    @Test
    void listarEstudantesSemCadastros() {
        assertTrue(estudanteService.listar().isEmpty());
    }

    @Test
    void buscarEstudantesPorNomeDeveIgnorarMaiusculasEAceitarTrecho() {
        criar("Maria Silva", "111", "maria@email.com");
        criar("João Pereira", "222", "joao@email.com");
        criar("Ana MARIA Souza", "333", "ana@email.com");

        List<Estudante> encontrados = estudanteService.buscarPorNome("maria");

        assertEquals(2, encontrados.size());
        assertTrue(encontrados.contains(estudanteService.buscar("111")));
        assertTrue(encontrados.contains(estudanteService.buscar("333")));
    }

    @Test
    void buscarEstudantesPorNomeSemResultado() {
        criarEstudantePadrao("111");

        assertTrue(estudanteService.buscarPorNome("xyz").isEmpty());
    }

    // ---------- editar ----------

    @Test
    void atualizarEstudante() {
        String matricula = criarEstudantePadrao("123");

        atualizar(matricula, "Novo Nome", "novo@email.com");

        Estudante e = estudanteService.buscar(matricula);
        assertEquals("Novo Nome", e.getNome());
        assertEquals("novo@email.com", e.getEmail());
        assertEquals("123", e.getMatricula());
    }

    @Test
    void atualizarEstudanteNaoDeveCriarNovoCadastro() {
        String matricula = criarEstudantePadrao("123");

        atualizar(matricula, "Novo Nome", "novo@email.com");

        assertEquals(1, estudanteService.listar().size());
    }

    @Test
    void naoDeveAtualizarEstudanteComCampoVazio() {
        String matricula = criarEstudantePadrao("123");

        assertThrows(IllegalArgumentException.class, () -> atualizar(matricula, "", "novo@email.com"));
        assertThrows(IllegalArgumentException.class, () -> atualizar(matricula, "Novo", "  "));
        assertEquals("Estudante 123", estudanteService.buscar(matricula).getNome());
        assertEquals("123@email.com", estudanteService.buscar(matricula).getEmail());
    }

    @Test
    void naoDeveAtualizarEstudanteComEmailInvalido() {
        String matricula = criarEstudantePadrao("123");

        assertThrows(IllegalArgumentException.class, () -> atualizar(matricula, "Novo", "invalido"));
        assertEquals("Estudante 123", estudanteService.buscar(matricula).getNome());
        assertEquals("123@email.com", estudanteService.buscar(matricula).getEmail());
    }

    @Test
    void atualizarEstudanteInexistenteDeveLancarExcecao() {
        assertThrows(RecursoNaoEncontradoException.class, () -> atualizar("999", "Nome", "a@b.com"));
    }

    @Test
    void atualizarEstudanteNaoDeveAfetarOutrosEstudantes() {
        criarEstudantePadrao("111");
        criarEstudantePadrao("222");

        atualizar("111", "Outro Nome", "outro@email.com");

        assertEquals("Estudante 222", estudanteService.buscar("222").getNome());
        assertEquals("222@email.com", estudanteService.buscar("222").getEmail());
    }
}
