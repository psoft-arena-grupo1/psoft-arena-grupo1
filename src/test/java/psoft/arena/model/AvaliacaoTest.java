package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvaliacaoTest {

    private static final String CODIGO = "public class Biblioteca { }";

    private Submissao submissao;
    private ModeloLlm modelo;

    @BeforeEach
    void setUp() {
        Estudante estudante = new Estudante("Ana", "123", "ana@email.com");
        Turma turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        Problema problema = new Problema("Biblioteca", "d", "rf", "rp");
        EstrategiaAvaliacao estrategia = new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p");
        modelo = new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave");
        Atividade atividade = new Atividade(problema, turma, estrategia, modelo, "Lab 1", "d",
                LocalDateTime.now().plusDays(7));
        submissao = new Submissao(estudante, atividade, CODIGO, 1);
    }

    private Avaliacao novaAvaliacao() {
        return new Avaliacao(submissao);
    }

    @Test
    void exigeSubmissaoObrigatoria() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> new Avaliacao(null));

        assertEquals("A submissão avaliada é obrigatória.", erro.getMessage());
    }

    @Test
    void iniciaEmProcessamentoComASubmissaoAssociada() {
        Avaliacao avaliacao = novaAvaliacao();

        assertSame(submissao, avaliacao.getSubmissao());
        assertEquals(StatusAvaliacao.PROCESSANDO, avaliacao.getStatus());
        assertNull(avaliacao.getNota());
        assertNull(avaliacao.getEvidencias());
        assertNull(avaliacao.getJustificativa());
        assertNull(avaliacao.getMotivoFalha());
        assertNotNull(avaliacao.getDataHora());
        assertTrue(avaliacao.getResultados().isEmpty());
    }

    @Test
    void concluirComNotaEvidenciasEJustificativa() {
        Avaliacao avaliacao = novaAvaliacao();

        avaliacao.concluir(new BigDecimal("8.5"), "Evidência 1", "Justificativa 1");

        assertEquals(StatusAvaliacao.CONCLUIDA, avaliacao.getStatus());
        assertEquals(0, avaliacao.getNota().compareTo(new BigDecimal("8.5")));
        assertEquals("Evidência 1", avaliacao.getEvidencias());
        assertEquals("Justificativa 1", avaliacao.getJustificativa());
    }

    @Test
    void concluirAceitaNotaZero() {
        Avaliacao avaliacao = novaAvaliacao();

        avaliacao.concluir(BigDecimal.ZERO, "e", "j");

        assertEquals(StatusAvaliacao.CONCLUIDA, avaliacao.getStatus());
        assertEquals(0, avaliacao.getNota().compareTo(BigDecimal.ZERO));
    }

    @Test
    void concluirAceitaNotaDez() {
        Avaliacao avaliacao = novaAvaliacao();

        avaliacao.concluir(BigDecimal.TEN, "e", "j");

        assertEquals(0, avaliacao.getNota().compareTo(BigDecimal.TEN));
    }

    @Test
    void concluirRejeitaNotaNegativa() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> avaliacao.concluir(new BigDecimal("-0.5"), "e", "j"));

        assertEquals("A nota da avaliação deve estar entre 0 e 10.", erro.getMessage());
        assertEquals(StatusAvaliacao.PROCESSANDO, avaliacao.getStatus());
    }

    @Test
    void concluirRejeitaNotaAcimaDeDez() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> avaliacao.concluir(new BigDecimal("10.1"), "e", "j"));

        assertEquals("A nota da avaliação deve estar entre 0 e 10.", erro.getMessage());
    }

    @Test
    void concluirRejeitaNotaNula() {
        Avaliacao avaliacao = novaAvaliacao();

        assertThrows(IllegalArgumentException.class, () -> avaliacao.concluir(null, "e", "j"));
        assertEquals(StatusAvaliacao.PROCESSANDO, avaliacao.getStatus());
    }

    @Test
    void concluirRejeitaEvidenciasVazias() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> avaliacao.concluir(new BigDecimal("8"), " ", "j"));

        assertEquals("As evidências da avaliação são obrigatórias.", erro.getMessage());
    }

    @Test
    void concluirRejeitaJustificativaVazia() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> avaliacao.concluir(new BigDecimal("8"), "e", ""));

        assertEquals("A justificativa da avaliação é obrigatória.", erro.getMessage());
    }

    @Test
    void naoConcluiDuasVezes() {
        Avaliacao avaliacao = novaAvaliacao();
        avaliacao.concluir(new BigDecimal("8"), "e", "j");

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> avaliacao.concluir(new BigDecimal("9"), "e2", "j2"));

        assertEquals("Apenas avaliações em processamento podem ser concluídas.", erro.getMessage());
        assertEquals(0, avaliacao.getNota().compareTo(new BigDecimal("8")));
    }

    @Test
    void naoConcluiDepoisDeFalhar() {
        Avaliacao avaliacao = novaAvaliacao();
        avaliacao.falhar("timeout");

        assertThrows(IllegalStateException.class, () -> avaliacao.concluir(new BigDecimal("8"), "e", "j"));
        assertEquals(StatusAvaliacao.FALHA, avaliacao.getStatus());
    }

    @Test
    void falharRegistraMotivoEMudaStatus() {
        Avaliacao avaliacao = novaAvaliacao();

        avaliacao.falhar("Modelo grok-3: timeout");

        assertEquals(StatusAvaliacao.FALHA, avaliacao.getStatus());
        assertEquals("Modelo grok-3: timeout", avaliacao.getMotivoFalha());
    }

    @Test
    void falharExigeMotivo() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> avaliacao.falhar(null));
        assertEquals("O motivo da falha da avaliação é obrigatório.", erro.getMessage());

        assertThrows(IllegalArgumentException.class, () -> avaliacao.falhar("  "));
        assertEquals(StatusAvaliacao.PROCESSANDO, avaliacao.getStatus());
    }

    @Test
    void naoFalhaDuasVezes() {
        Avaliacao avaliacao = novaAvaliacao();
        avaliacao.falhar("timeout");

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> avaliacao.falhar("outra falha"));

        assertEquals("Apenas avaliações em processamento podem falhar.", erro.getMessage());
        assertEquals("timeout", avaliacao.getMotivoFalha());
    }

    @Test
    void resultadosNaoSaoModificaveisPelaListaRetornada() {
        Avaliacao avaliacao = novaAvaliacao();
        ResultadoAvaliacaoModelo resultado = new ResultadoAvaliacaoModelo(avaliacao, modelo, new BigDecimal("7"), "e",
                "j");

        assertThrows(UnsupportedOperationException.class, () -> avaliacao.getResultados().add(resultado));
    }

    @Test
    void adicionarResultadoNuloLancaIllegalArgument() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> avaliacao.adicionarResultado(null));

        assertEquals("O resultado do modelo de LLM é obrigatório.", erro.getMessage());
    }

    @Test
    void adicionarResultadoValidoFicaDisponivelNaAvaliacao() {
        Avaliacao avaliacao = novaAvaliacao();
        ResultadoAvaliacaoModelo resultado = new ResultadoAvaliacaoModelo(avaliacao, modelo, new BigDecimal("7"), "e",
                "j");

        avaliacao.adicionarResultado(resultado);

        assertEquals(1, avaliacao.getResultados().size());
        assertSame(resultado, avaliacao.getResultados().get(0));
    }

    @Test
    void resultadoValidoExpoeDadosDoModelo() {
        Avaliacao avaliacao = novaAvaliacao();

        ResultadoAvaliacaoModelo resultado = new ResultadoAvaliacaoModelo(avaliacao, modelo,
                new BigDecimal("9.5"), "Evidência", "Justificativa");

        assertSame(avaliacao, resultado.getAvaliacao());
        assertSame(modelo, resultado.getModelo());
        assertTrue(resultado.isValido());
        assertEquals(0, resultado.getNota().compareTo(new BigDecimal("9.5")));
        assertEquals("Evidência", resultado.getEvidencias());
        assertEquals("Justificativa", resultado.getJustificativa());
        assertNull(resultado.getMotivoErro());
        assertNotNull(resultado.getDataHora());
    }

    @Test
    void resultadoInvalidoMarcaValidoFalsoComMotivo() {
        Avaliacao avaliacao = novaAvaliacao();

        ResultadoAvaliacaoModelo resultado = new ResultadoAvaliacaoModelo(avaliacao, modelo, "timeout");

        assertSame(avaliacao, resultado.getAvaliacao());
        assertSame(modelo, resultado.getModelo());
        assertFalse(resultado.isValido());
        assertNull(resultado.getNota());
        assertEquals("timeout", resultado.getMotivoErro());
        assertNotNull(resultado.getDataHora());
    }

    @Test
    void resultadoExigeNotaEntreZeroEDez() {
        Avaliacao avaliacao = novaAvaliacao();

        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, modelo, new BigDecimal("11"), "e", "j"));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, modelo, new BigDecimal("-0.5"), "e", "j"));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, modelo, null, "e", "j"));
    }

    @Test
    void resultadoValidoExigeEvidenciasEJustificativa() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException evidencias = assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, modelo, new BigDecimal("7"), " ", "j"));
        assertEquals("As evidências do modelo são obrigatórias.", evidencias.getMessage());

        IllegalArgumentException justificativa = assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, modelo, new BigDecimal("7"), "e", ""));
        assertEquals("A justificativa do modelo é obrigatória.", justificativa.getMessage());
    }

    @Test
    void resultadoInvalidoExigeMotivo() {
        Avaliacao avaliacao = novaAvaliacao();

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, modelo, "  "));

        assertEquals("O motivo do erro do modelo é obrigatório.", erro.getMessage());
    }

    @Test
    void resultadoExigeAvaliacaoEModelo() {
        Avaliacao avaliacao = novaAvaliacao();

        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(null, modelo, new BigDecimal("7"), "e", "j"));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, null, new BigDecimal("7"), "e", "j"));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(null, modelo, "timeout"));
        assertThrows(IllegalArgumentException.class,
                () -> new ResultadoAvaliacaoModelo(avaliacao, null, "timeout"));
    }
}
