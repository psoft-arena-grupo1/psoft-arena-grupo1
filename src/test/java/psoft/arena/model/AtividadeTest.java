package psoft.arena.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AtividadeTest {

    private Problema problema;
    private Turma turma;
    private EstrategiaAvaliacao estrategia;
    private ModeloLlm llm;
    private Estudante estudante;
    private LocalDateTime prazo;

    @BeforeEach
    void setUp() {
        problema = new Problema("Biblioteca", "Sistema de biblioteca", "Emprestar livros", "Usar MVC");
        turma = new Turma("POO", "PSOFT01", "2026.1", "Prof. Silva");
        estrategia = new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p");
        llm = new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave");
        estudante = new Estudante("Ana", "111", "ana@email.com");
        prazo = LocalDateTime.now().plusDays(7);
    }

    private Atividade novaAtividade() {
        return new Atividade(problema, turma, estrategia, llm, "Lab 1", "Descrição", prazo);
    }

    @Test
    void criarAssociaTodosOsCamposEComecaAberta() {
        Atividade atividade = novaAtividade();

        assertSame(problema, atividade.getProblema());
        assertSame(turma, atividade.getTurma());
        assertSame(estrategia, atividade.getEstrategia());
        assertSame(llm, atividade.getLlmPadrao());
        assertEquals("Lab 1", atividade.getTitulo());
        assertEquals("Descrição", atividade.getDescricao());
        assertEquals(prazo, atividade.getPrazo());
        assertEquals(StatusAtividade.ABERTA, atividade.getStatus());
        assertTrue(atividade.aceitaSubmissoes());
    }

    @Test
    void naoDeveCriarComProblemaInativo() {
        problema.desativar();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, this::novaAtividade);
        assertEquals("Problemas inativos não podem ser associados a novas atividades.", e.getMessage());
    }

    @Test
    void naoDeveCriarComTurmaEncerrada() {
        turma.encerrar();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, this::novaAtividade);
        assertEquals("Turmas encerradas não podem receber novas atividades.", e.getMessage());
    }

    @Test
    void naoDeveCriarComPrazoNoPassado() {
        prazo = LocalDateTime.now().minusMinutes(1);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, this::novaAtividade);
        assertEquals("O prazo para submissão deve ser uma data futura.", e.getMessage());
    }

    @Test
    void llmExigeUrlHttps() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new ModeloLlm("grok-3", "http://api.x.ai/v1", "chave"));
        assertEquals("A URL da API do LLM deve usar HTTPS.", e.getMessage());
    }

    @Test
    void encerrarConcluiAtividadeENaoAceitaSubmissoes() {
        Atividade atividade = novaAtividade();

        atividade.encerrar();

        assertEquals(StatusAtividade.CONCLUIDA, atividade.getStatus());
        assertFalse(atividade.aceitaSubmissoes());
    }

    @Test
    void estudanteDaTurmaPodeParticipar() {
        turma.adicionarEstudante(estudante);
        Atividade atividade = novaAtividade();

        assertDoesNotThrow(() -> atividade.garantirParticipante(estudante));
    }

    @Test
    void estudanteForaDaTurmaNaoPodeParticipar() {
        Atividade atividade = novaAtividade();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> atividade.garantirParticipante(estudante));
        assertEquals("Apenas estudantes da turma podem ser adicionados a atividade.", e.getMessage());
    }

    @Test
    void toStringContemNomeDaTurmaETituloDoProblema() {
        String texto = novaAtividade().toString();

        assertTrue(texto.contains("turma: POO"));
        assertTrue(texto.contains("problema: Biblioteca"));
    }
}
