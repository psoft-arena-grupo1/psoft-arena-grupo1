package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SubmissaoTest {

    private static final String CODIGO = "public class Biblioteca {\n    public void emprestar() { }\n}";

    private Estudante estudante;
    private Turma turma;
    private Atividade atividade;

    @BeforeEach
    void setUp() {
        estudante = new Estudante("Maria", "123", "maria@email.com");
        turma = new Turma("POO", "P1", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        atividade = novaAtividade(turma);
    }

    private Atividade novaAtividade(Turma turma) {
        Problema problema = new Problema("Biblioteca", "d", "rf", "rp");
        EstrategiaAvaliacao estrategia = new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p");
        ModeloLlm llm = new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave");
        return new Atividade(problema, turma, estrategia, llm, "Lab 1", "Descrição",
                LocalDateTime.now().plusDays(7));
    }

    @Test
    void novaSubmissaoGuardaEstudanteAtividadeECodigo() {
        Submissao submissao = new Submissao(estudante, atividade, CODIGO, 1);

        assertSame(estudante, submissao.getEstudante());
        assertSame(atividade, submissao.getAtividade());
        assertEquals(CODIGO, submissao.getCodigoFonte());
        assertEquals(1, submissao.getVersao());
    }

    @Test
    void novaSubmissaoIniciaComoSubmetida() {
        Submissao submissao = new Submissao(estudante, atividade, CODIGO, 1);

        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
    }

    @Test
    void novaSubmissaoRegistraDataEHora() {
        LocalDateTime antes = LocalDateTime.now();

        Submissao submissao = new Submissao(estudante, atividade, CODIGO, 1);

        assertNotNull(submissao.getDataHora());
        assertTrue(!submissao.getDataHora().isBefore(antes));
        assertTrue(!submissao.getDataHora().isAfter(LocalDateTime.now()));
    }

    @Test
    void atividadeConcluidaNaoAceitaSubmissao() {
        atividade.encerrar();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, CODIGO, 1));

        assertEquals("Atividades concluídas não aceitam novas submissões.", erro.getMessage());
    }

    @Test
    void codigoFonteNuloOuVazioNaoEhAceito() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, null, 1));
        assertEquals("O código-fonte da solução é obrigatório.", erro.getMessage());
        assertThrows(IllegalArgumentException.class, () -> new Submissao(estudante, atividade, "", 1));
        assertThrows(IllegalArgumentException.class, () -> new Submissao(estudante, atividade, "   \n ", 1));
    }

    @Test
    void codigoQueNaoEhJavaNaoEhAceito() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, "def soma(a, b):\n    return a + b", 1));

        assertEquals("A solução deve ser submetida em Java.", erro.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, "texto qualquer sem declaração de tipo", 1));
    }

    @Test
    void aceitaInterfaceEnumERecord() {
        assertDoesNotThrow(() -> new Submissao(estudante, atividade, "public interface Repo { void salvar(); }", 1));
        assertDoesNotThrow(() -> new Submissao(estudante, atividade, "enum Cor { AZUL, VERDE }", 1));
        assertDoesNotThrow(() -> new Submissao(estudante, atividade, "record Ponto(int x, int y) { }", 1));
    }

    @Test
    void aceitaClasseGenericaComHerancaEOutrosTipos() {
        assertDoesNotThrow(() -> new Submissao(estudante, atividade,
                "package app;\nimport java.util.List;\nabstract class Repo<T extends Entidade> implements Base {\n}", 1));
    }

    @Test
    void versaoDeveSerPositiva() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, CODIGO, 0));

        assertEquals("A versão da submissão deve ser maior que zero.", erro.getMessage());
    }

    @Test
    void estudantePodeTerVariasSubmissoesNaMesmaAtividade() {
        Submissao primeira = new Submissao(estudante, atividade, CODIGO, 1);
        Submissao segunda = new Submissao(estudante, atividade, CODIGO + "\nclass Extra { }", 2);

        assertEquals(1, primeira.getVersao());
        assertEquals(2, segunda.getVersao());
        assertEquals(CODIGO, primeira.getCodigoFonte());
    }
}