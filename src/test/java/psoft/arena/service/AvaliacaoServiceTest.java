package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import psoft.arena.dto.AvaliacaoResponse;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Avaliacao;
import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.ResultadoAvaliacaoModelo;
import psoft.arena.model.StatusAvaliacao;
import psoft.arena.model.Submissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;
import psoft.arena.repository.AtividadeRepository;
import psoft.arena.repository.AvaliacaoRepository;
import psoft.arena.repository.EstrategiaAvaliacaoRepository;
import psoft.arena.repository.EstudanteRepository;
import psoft.arena.repository.ModeloLlmRepository;
import psoft.arena.repository.ProblemaRepository;
import psoft.arena.repository.SubmissaoRepository;
import psoft.arena.repository.TurmaRepository;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:psoftarena-avaliacao-service-test;DB_CLOSE_DELAY=-1")
class AvaliacaoServiceTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Autowired
    private AvaliacaoService avaliacaoService;
    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    @Autowired
    private SubmissaoRepository submissaoRepository;
    @Autowired
    private AtividadeRepository atividadeRepository;
    @Autowired
    private EstudanteRepository estudanteRepository;
    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private ProblemaRepository problemaRepository;
    @Autowired
    private EstrategiaAvaliacaoRepository estrategiaRepository;
    @Autowired
    private ModeloLlmRepository modeloLlmRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private EntityManager entityManager;

    private record Cenario(Long submissaoId, Long modeloId) {
    }

    /**
     * O {@link AvaliacaoService} usa {@code REQUIRES_NEW}; por isso o cenário precisa estar
     * commitado antes de ser visível para as transações internas do serviço.
     */
    private Cenario criarCenario() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template.execute(status -> {
            Estudante estudante = estudanteRepository
                    .save(new Estudante("Ana", "MAT" + System.nanoTime(), "ana@email.com"));
            Turma turma = new Turma("POO", "P" + System.nanoTime(), "2026.1", "Prof. Silva");
            turma.adicionarEstudante(estudante);
            turmaRepository.save(turma);
            Problema problema = problemaRepository.save(new Problema("Biblioteca", "d", "rf", "rp"));
            EstrategiaAvaliacao estrategia = estrategiaRepository
                    .save(new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"));
            ModeloLlm modelo = modeloLlmRepository
                    .save(new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"));
            Atividade atividade = atividadeRepository.save(new Atividade(problema, turma, estrategia, modelo, "Lab 1",
                    "d", LocalDateTime.now().plusDays(7)));
            Submissao submissao = submissaoRepository.save(new Submissao(estudante, atividade, CODIGO, 1));
            return new Cenario(submissao.getId(), modelo.getId());
        });
    }

    private Avaliacao recarregar(Long avaliacaoId) {
        entityManager.clear();
        return avaliacaoRepository.findById(avaliacaoId).orElseThrow();
    }

    @Test
    void iniciarCriaAvaliacaoEmProcessamento() {
        Cenario cenario = criarCenario();

        Avaliacao avaliacao = avaliacaoService.iniciar(cenario.submissaoId());

        assertNotNull(avaliacao.getId());
        assertEquals(StatusAvaliacao.PROCESSANDO, avaliacao.getStatus());
        assertEquals(cenario.submissaoId(), avaliacao.getSubmissao().getId());
        assertNull(avaliacao.getNota());
        assertNotNull(avaliacao.getDataHora());
    }

    @Test
    void iniciarDuasVezesLancaIllegalStateException() {
        Cenario cenario = criarCenario();
        avaliacaoService.iniciar(cenario.submissaoId());

        assertThrows(IllegalStateException.class, () -> avaliacaoService.iniciar(cenario.submissaoId()));
    }

    @Test
    void iniciarDeSubmissaoInexistenteLancaNaoEncontrado() {
        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> avaliacaoService.iniciar(999999L));

        assertEquals("Submissão não encontrada.", erro.getMessage());
    }

    @Test
    void registrarResultadoEConcluirProduzAvaliacaoConcluidaComResultados() {
        Cenario cenario = criarCenario();
        Long avaliacaoId = avaliacaoService.iniciar(cenario.submissaoId()).getId();

        avaliacaoService.registrarResultado(avaliacaoId, cenario.modeloId(), new BigDecimal("8.5"), "e1", "j1");
        avaliacaoService.concluir(avaliacaoId, new BigDecimal("8.5"), "e1", "j1");

        Avaliacao avaliacao = recarregar(avaliacaoId);
        assertEquals(StatusAvaliacao.CONCLUIDA, avaliacao.getStatus());
        assertEquals(0, avaliacao.getNota().compareTo(new BigDecimal("8.5")));
        assertEquals("e1", avaliacao.getEvidencias());
        assertEquals("j1", avaliacao.getJustificativa());
        assertNull(avaliacao.getMotivoFalha());
        assertEquals(1, avaliacao.getResultados().size());
        ResultadoAvaliacaoModelo resultado = avaliacao.getResultados().get(0);
        assertEquals(cenario.modeloId(), resultado.getModelo().getId());
        assertTrue(resultado.isValido());
        assertEquals(0, resultado.getNota().compareTo(new BigDecimal("8.5")));
        assertEquals("e1", resultado.getEvidencias());
        assertEquals("j1", resultado.getJustificativa());
    }

    @Test
    void registrarFalhaModeloEFalharProduzAvaliacaoEmFalha() {
        Cenario cenario = criarCenario();
        Long avaliacaoId = avaliacaoService.iniciar(cenario.submissaoId()).getId();

        avaliacaoService.registrarFalhaModelo(avaliacaoId, cenario.modeloId(), "Modelo grok-3: timeout");
        avaliacaoService.falhar(avaliacaoId, "Modelo grok-3: timeout");

        Avaliacao avaliacao = recarregar(avaliacaoId);
        assertEquals(StatusAvaliacao.FALHA, avaliacao.getStatus());
        assertEquals("Modelo grok-3: timeout", avaliacao.getMotivoFalha());
        assertEquals(1, avaliacao.getResultados().size());
        ResultadoAvaliacaoModelo resultado = avaliacao.getResultados().get(0);
        assertFalse(resultado.isValido());
        assertEquals("Modelo grok-3: timeout", resultado.getMotivoErro());
    }

    @Test
    void buscarPorSubmissaoRetornaDtoComANota() {
        Cenario cenario = criarCenario();
        Long avaliacaoId = avaliacaoService.iniciar(cenario.submissaoId()).getId();
        avaliacaoService.registrarResultado(avaliacaoId, cenario.modeloId(), new BigDecimal("9.25"), "evidência",
                "justificativa");
        avaliacaoService.concluir(avaliacaoId, new BigDecimal("9.25"), "evidência", "justificativa");

        AvaliacaoResponse resposta = avaliacaoService.buscarPorSubmissao(cenario.submissaoId());

        assertEquals(cenario.submissaoId(), resposta.submissaoId());
        assertEquals(StatusAvaliacao.CONCLUIDA, resposta.status());
        assertEquals(0, resposta.nota().compareTo(new BigDecimal("9.25")));
        assertEquals("evidência", resposta.evidencias());
        assertEquals("justificativa", resposta.justificativa());
        assertEquals(1, resposta.resultados().size());
        assertEquals(cenario.modeloId(), resposta.resultados().get(0).modeloId());
    }

    @Test
    void buscarPorSubmissaoSemAvaliacaoLancaNaoEncontrado() {
        Cenario cenario = criarCenario();

        RecursoNaoEncontradoException erro = assertThrows(RecursoNaoEncontradoException.class,
                () -> avaliacaoService.buscarPorSubmissao(cenario.submissaoId()));

        assertEquals("Avaliação não encontrada.", erro.getMessage());
    }

    @Test
    void novaTentativaDeAvaliacaoEhPermitidaAposFalhaEPreservaAAnterior() {
        Cenario cenario = criarCenario();
        Avaliacao primeira = avaliacaoService.iniciar(cenario.submissaoId());
        avaliacaoService.falhar(primeira.getId(), "timeout");

        Avaliacao segunda = avaliacaoService.iniciar(cenario.submissaoId());
        avaliacaoService.concluir(segunda.getId(), new BigDecimal("9.0"), "e1", "j1");

        assertEquals(StatusAvaliacao.CONCLUIDA, avaliacaoService.buscarPorSubmissao(cenario.submissaoId()).status());
        var tentativas = avaliacaoService.listarTentativas(cenario.submissaoId());
        assertEquals(2, tentativas.size());
        assertEquals(StatusAvaliacao.FALHA, tentativas.get(0).status());
        assertEquals("timeout", tentativas.get(0).motivoFalha());
        assertEquals(StatusAvaliacao.CONCLUIDA, tentativas.get(1).status());
    }

    @Test
    void listarTentativasDeSubmissaoInexistenteLancaNaoEncontrado() {
        assertThrows(RecursoNaoEncontradoException.class, () -> avaliacaoService.listarTentativas(999999L));
    }
}
