package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.Submissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;

class NotificadorSubmissaoTest {

    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream saida;
    private Submissao submissao;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));

        Estudante estudante = new Estudante("Maria", "123", "maria@email.com");
        Turma turma = new Turma("POO", "P1", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        Atividade atividade = new Atividade(new Problema("Biblioteca", "d", "rf", "rp"), turma,
                new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"),
                new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"), "Lab 1", "Descrição",
                LocalDateTime.now().plusDays(7));
        submissao = new Submissao(estudante, atividade, "public class A { }", 2);
        ReflectionTestUtils.setField(submissao, "id", 7L);
    }

    @AfterEach
    void tearDown() {
        System.setOut(saidaOriginal);
    }

    private String texto() {
        return saida.toString(StandardCharsets.UTF_8);
    }

    @Test
    void avisaQueOResultadoEstaDisponivelComOsDadosDaSubmissao() {
        new NotificadorSubmissao().notificarResultadoDisponivel(submissao);

        String mensagem = texto();
        assertTrue(mensagem.contains("[NOTIFICAÇÃO]"));
        assertTrue(mensagem.contains("Maria (123)"));
        assertTrue(mensagem.contains("submissão 7"));
        assertTrue(mensagem.contains("versão 2"));
        assertTrue(mensagem.contains("já está disponível"));
    }

    @Test
    void indicaOEndpointParaConsultarOResultado() {
        new NotificadorSubmissao().notificarResultadoDisponivel(submissao);

        assertTrue(texto().contains("GET /estudantes/123/submissoes/7/resultado"));
    }

    @Test
    void imprimeUmaUnicaLinha() {
        new NotificadorSubmissao().notificarResultadoDisponivel(submissao);

        assertEquals(1, texto().lines().count());
    }
}