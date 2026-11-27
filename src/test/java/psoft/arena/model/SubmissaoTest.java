package psoft.arena.model;

import psoft.arena.exception.TransicaoInvalidaException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

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

    private Submissao novaSubmissao() {
        return new Submissao(estudante, atividade, CODIGO, 1);
    }

    private Submissao emFila() {
        Submissao submissao = novaSubmissao();
        submissao.encaminhar();
        return submissao;
    }

    private Submissao processando() {
        Submissao submissao = emFila();
        submissao.iniciarProcessamento();
        return submissao;
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
    void codigoJavaComErroDeSintaxeNaoEhAceito() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, "public class Biblioteca {\n    void m() { int x = ; }\n}", 1));
        assertTrue(erro.getMessage().startsWith("O código Java submetido possui erro de sintaxe na linha 2"));
        assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, "class A { lixo !!! }", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Submissao(estudante, atividade, "class A {", 1));
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

    // Estados e histórico (padrão State)

    @Test
    void novaSubmissaoRegistraEstadoInicialNoHistorico() {
        Submissao submissao = novaSubmissao();

        assertEquals(1, submissao.getHistorico().size());
        HistoricoSubmissao registro = submissao.getHistorico().get(0);
        assertNull(registro.getStatusAnterior());
        assertEquals(StatusSubmissao.SUBMETIDA, registro.getStatusNovo());
        assertNotNull(registro.getDataHora());
        assertSame(submissao, registro.getSubmissao());
    }

    @Test
    void submissaoPercorreTodosOsEstadosAteConcluida() {
        Submissao submissao = novaSubmissao();

        submissao.encaminhar();
        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        submissao.iniciarProcessamento();
        assertEquals(StatusSubmissao.PROCESSANDO, submissao.getStatus());
        submissao.concluir();
        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());

        List<HistoricoSubmissao> historico = submissao.getHistorico();
        assertEquals(List.of(StatusSubmissao.SUBMETIDA, StatusSubmissao.EM_FILA, StatusSubmissao.PROCESSANDO,
                StatusSubmissao.CONCLUIDA), historico.stream().map(HistoricoSubmissao::getStatusNovo).toList());
        for (int i = 1; i < historico.size(); i++) {
            assertEquals(historico.get(i - 1).getStatusNovo(), historico.get(i).getStatusAnterior());
        }
    }

    @Test
    void transicaoDevolveORegistroDoHistorico() {
        Submissao submissao = novaSubmissao();

        HistoricoSubmissao registro = submissao.encaminhar();

        assertEquals(StatusSubmissao.SUBMETIDA, registro.getStatusAnterior());
        assertEquals(StatusSubmissao.EM_FILA, registro.getStatusNovo());
        assertSame(submissao, registro.getSubmissao());
        assertSame(registro, submissao.getHistorico().get(submissao.getHistorico().size() - 1));
    }

    @Test
    void falhaEhPermitidaEmSubmetidaEmFilaEProcessando() {
        Submissao submetida = novaSubmissao();
        Submissao naFila = emFila();
        Submissao emProcessamento = processando();

        submetida.falhar();
        naFila.falhar();
        emProcessamento.falhar();

        assertEquals(StatusSubmissao.FALHA, submetida.getStatus());
        assertEquals(StatusSubmissao.FALHA, naFila.getStatus());
        assertEquals(StatusSubmissao.FALHA, emProcessamento.getStatus());
        assertEquals(StatusSubmissao.PROCESSANDO,
                emProcessamento.getHistorico().get(emProcessamento.getHistorico().size() - 1).getStatusAnterior());
    }

    @Test
    void transicoesForaDeOrdemSaoRejeitadas() {
        Submissao submetida = novaSubmissao();
        assertThrows(IllegalStateException.class, submetida::iniciarProcessamento);
        assertThrows(IllegalStateException.class, submetida::concluir);

        Submissao naFila = emFila();
        assertThrows(IllegalStateException.class, naFila::encaminhar);
        assertThrows(IllegalStateException.class, naFila::concluir);

        Submissao emProcessamento = processando();
        assertThrows(IllegalStateException.class, emProcessamento::encaminhar);
        assertThrows(IllegalStateException.class, emProcessamento::iniciarProcessamento);
    }

    @Test
    void mensagemDeTransicaoInvalidaInformaOEstadoAtual() {
        IllegalStateException erro = assertThrows(IllegalStateException.class, () -> novaSubmissao().concluir());

        assertEquals("Não é possível concluir uma submissão no estado Submetida.", erro.getMessage());
    }

    @Test
    void transicaoInvalidaNaoAlteraStatusNemHistorico() {
        Submissao submissao = emFila();

        assertThrows(IllegalStateException.class, submissao::concluir);

        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        assertEquals(2, submissao.getHistorico().size());
    }

    @Test
    void concluidaEhEstadoFinal() {
        Submissao submissao = processando();
        submissao.concluir();

        assertThrows(IllegalStateException.class, submissao::encaminhar);
        assertThrows(IllegalStateException.class, submissao::iniciarProcessamento);
        assertThrows(IllegalStateException.class, submissao::concluir);
        assertThrows(IllegalStateException.class, submissao::falhar);
        assertEquals(StatusSubmissao.CONCLUIDA, submissao.getStatus());
    }

    @Test
    void falhaEhEstadoFinal() {
        Submissao submissao = processando();
        submissao.falhar();

        assertThrows(IllegalStateException.class, submissao::encaminhar);
        assertThrows(IllegalStateException.class, submissao::iniciarProcessamento);
        assertThrows(IllegalStateException.class, submissao::concluir);
        assertThrows(IllegalStateException.class, submissao::falhar);
        assertEquals(StatusSubmissao.FALHA, submissao.getStatus());
    }

    @Test
    void historicoNaoPodeSerModificadoPorFora() {
        Submissao submissao = novaSubmissao();

        assertThrows(UnsupportedOperationException.class, () -> submissao.getHistorico().clear());
        assertEquals(1, submissao.getHistorico().size());
    }

    @Test
    void reprocessarSubmissaoComFalhaVoltaParaEmFilaComOMesmoCodigo() {
        Submissao submissao = processando();
        submissao.falhar();

        HistoricoSubmissao registro = submissao.reprocessar();

        assertEquals(StatusSubmissao.EM_FILA, submissao.getStatus());
        assertEquals(StatusSubmissao.FALHA, registro.getStatusAnterior());
        assertEquals(StatusSubmissao.EM_FILA, registro.getStatusNovo());
        assertEquals("Reprocessamento solicitado (tentativa 2).", registro.getMotivo());
        assertEquals(CODIGO, submissao.getCodigoFonte());
        assertEquals(1, submissao.getVersao());
    }

    @Test
    void reprocessamentoPreservaOHistoricoDasTentativasAnteriores() {
        Submissao submissao = processando();
        submissao.falhar();
        submissao.reprocessar();
        submissao.iniciarProcessamento();
        submissao.concluir();

        assertEquals(List.of(StatusSubmissao.SUBMETIDA, StatusSubmissao.EM_FILA, StatusSubmissao.PROCESSANDO,
                StatusSubmissao.FALHA, StatusSubmissao.EM_FILA, StatusSubmissao.PROCESSANDO,
                StatusSubmissao.CONCLUIDA),
                submissao.getHistorico().stream().map(HistoricoSubmissao::getStatusNovo).toList());
        assertEquals(2, submissao.tentativas());
    }

    @Test
    void somenteSubmissoesComFalhaPodemSerReprocessadas() {
        Submissao concluida = processando();
        concluida.concluir();

        assertThrows(TransicaoInvalidaException.class, () -> novaSubmissao().reprocessar());
        assertThrows(TransicaoInvalidaException.class, () -> emFila().reprocessar());
        assertThrows(TransicaoInvalidaException.class, () -> processando().reprocessar());
        TransicaoInvalidaException erro = assertThrows(TransicaoInvalidaException.class, concluida::reprocessar);
        assertEquals("Não é possível reprocessar uma submissão no estado Concluída.", erro.getMessage());
        assertEquals(StatusSubmissao.CONCLUIDA, concluida.getStatus());
    }

    @Test
    void submissaoPodeSerReprocessadaNovamenteSeFalharOutraVez() {
        Submissao submissao = processando();
        submissao.falhar();
        submissao.reprocessar();
        submissao.iniciarProcessamento();
        submissao.falhar();

        assertEquals("Reprocessamento solicitado (tentativa 3).", submissao.reprocessar().getMotivo());
        assertEquals(3, submissao.tentativas());
    }
}
