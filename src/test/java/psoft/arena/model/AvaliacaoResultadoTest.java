package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvaliacaoResultadoTest {

    private Avaliacao avaliacao;

    @BeforeEach
    void setUp() {
        Estudante estudante = new Estudante("Maria", "123", "maria@email.com");
        Turma turma = new Turma("POO", "P1", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        Atividade atividade = new Atividade(new Problema("Biblioteca", "d", "rf", "rp"), turma,
                new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"),
                new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"), "Lab 1", "Descrição",
                LocalDateTime.now().plusDays(7));
        avaliacao = new Avaliacao(new Submissao(estudante, atividade, "public class A { }", 1));
    }

    @Test
    void avaliacaoEmProcessamentoNaoTemResultadoDisponivel() {
        assertFalse(avaliacao.resultadoDisponivel());
    }

    @Test
    void avaliacaoConcluidaTemResultadoDisponivel() {
        avaliacao.concluir(new BigDecimal("8.5"), "evidência", "justificativa");

        assertTrue(avaliacao.resultadoDisponivel());
    }

    @Test
    void avaliacaoComFalhaNaoTemResultadoDisponivel() {
        avaliacao.falhar("API fora do ar");

        assertFalse(avaliacao.resultadoDisponivel());
    }
}