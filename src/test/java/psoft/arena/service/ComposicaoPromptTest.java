package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Problema;
import psoft.arena.model.TipoEstrategia;

class ComposicaoPromptTest {

    private static final String CODIGO = "public class Biblioteca { }";

    private ComposicaoPrompt composicaoPrompt;
    private Problema problema;
    private EstrategiaAvaliacao estrategia;

    @BeforeEach
    void setUp() {
        composicaoPrompt = new ComposicaoPrompt();
        problema = new Problema("Sistema de biblioteca", "Descrição do problema", "RF: cadastrar livros",
                "RP: usar MVC");
        estrategia = new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "Prompt da estratégia");
    }

    @Test
    void incluiPromptDaEstrategiaProblemaCodigoEContratoJson() {
        String prompt = composicaoPrompt.compor(problema, estrategia, CODIGO);

        assertTrue(prompt.contains("Prompt da estratégia"));
        assertTrue(prompt.contains("Sistema de biblioteca"));
        assertTrue(prompt.contains("Descrição do problema"));
        assertTrue(prompt.contains("RF: cadastrar livros"));
        assertTrue(prompt.contains("RP: usar MVC"));
        assertTrue(prompt.contains(CODIGO));
        assertTrue(prompt.contains("\"nota\""));
        assertTrue(prompt.contains("\"evidencias\""));
        assertTrue(prompt.contains("\"justificativa\""));
    }

    @Test
    void camposAusentesDoProblemaNaoQuebramAComposicao() {
        Problema incompleto = new Problema("Título", null, null, null);

        String prompt = composicaoPrompt.compor(incompleto, estrategia, CODIGO);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Título"));
        assertTrue(prompt.contains("Não informado."));
        assertTrue(prompt.contains(CODIGO));
    }

    @Test
    void naoInstruiAExecucaoDoCodigoFonte() {
        String prompt = composicaoPrompt.compor(problema, estrategia, CODIGO);

        assertTrue(prompt.contains("não deve ser executado"));
        assertFalse(prompt.contains("execute"));
        assertFalse(prompt.contains("executar o código"));
    }
}
