package psoft.arena.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import psoft.arena.dto.SubmissaoRequest;
import psoft.arena.exception.AcessoNegadoException;
import psoft.arena.exception.RecursoNaoEncontradoException;
import psoft.arena.model.Atividade;
import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Estudante;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Problema;
import psoft.arena.model.StatusSubmissao;
import psoft.arena.model.Submissao;
import psoft.arena.model.TipoEstrategia;
import psoft.arena.model.Turma;
import psoft.arena.repository.SubmissaoRepository;

@ExtendWith(MockitoExtension.class)
class SubmissaoServiceTest {

    private static final String CODIGO = "public class Biblioteca { }";

    @Mock
    private SubmissaoRepository submissaoRepository;
    @Mock
    private AtividadeService atividadeService;
    @Mock
    private EstudanteService estudanteService;
    @InjectMocks
    private SubmissaoService submissaoService;

    private Estudante estudante;
    private Atividade atividade;

    @BeforeEach
    void setUp() {
        estudante = new Estudante("Maria", "123", "maria@email.com");
        Turma turma = new Turma("POO", "P1", "2026.1", "Prof. Silva");
        turma.adicionarEstudante(estudante);
        atividade = new Atividade(new Problema("Biblioteca", "d", "rf", "rp"), turma,
                new EstrategiaAvaliacao("Smells", TipoEstrategia.CODE_SMELLS, "d", "p"),
                new ModeloLlm("grok-3", "https://api.x.ai/v1", "chave"), "Lab 1", "Descrição",
                LocalDateTime.now().plusDays(7));
    }

    private void mockarBuscas() {
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar("123")).thenReturn(estudante);
    }

    @Test
    void primeiraSubmissaoTemVersaoUmEStatusSubmetida() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> i.getArgument(0));

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO));

        assertEquals(1, submissao.getVersao());
        assertEquals(StatusSubmissao.SUBMETIDA, submissao.getStatus());
        assertSame(estudante, submissao.getEstudante());
        assertSame(atividade, submissao.getAtividade());
        assertEquals(CODIGO, submissao.getCodigoFonte());
        verify(submissaoRepository).save(submissao);
    }

    @Test
    void novaSubmissaoDoMesmoEstudanteNaMesmaAtividadeIncrementaAVersao() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(2L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> i.getArgument(0));

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO));

        assertEquals(3, submissao.getVersao());
    }

    @Test
    void matriculaComEspacosEhNormalizadaNaBusca() {
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar(" 123 ")).thenReturn(estudante);
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);
        when(submissaoRepository.save(any(Submissao.class))).thenAnswer(i -> i.getArgument(0));

        Submissao submissao = submissaoService.submeter(1L, new SubmissaoRequest(" 123 ", CODIGO));

        assertEquals("123", submissao.getEstudante().getMatricula());
    }

    @Test
    void exigeMatricula() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest(" ", CODIGO)));

        assertEquals("A matrícula do estudante é obrigatória.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void exigeCorpoDaRequisicao() {
        assertThrows(IllegalArgumentException.class, () -> submissaoService.submeter(1L, null));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoAtividadeNaoExiste() {
        when(atividadeService.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Atividade não encontrada."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> submissaoService.submeter(99L, new SubmissaoRequest("123", CODIGO)));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoEstudanteNaoExiste() {
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar("999")).thenThrow(new RecursoNaoEncontradoException("Estudante não encontrado."));

        assertThrows(RecursoNaoEncontradoException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("999", CODIGO)));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void negaAcessoQuandoEstudanteNaoPertenceATurmaDaAtividade() {
        Estudante outro = new Estudante("João", "456", "joao@email.com");
        when(atividadeService.buscar(1L)).thenReturn(atividade);
        when(estudanteService.buscar("456")).thenReturn(outro);

        AcessoNegadoException erro = assertThrows(AcessoNegadoException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("456", CODIGO)));

        assertEquals("Somente estudantes da turma da atividade podem realizar submissões.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void negaAcessoQuandoEstudanteFoiRemovidoDaTurma() {
        atividade.getTurma().removerEstudante("123");
        mockarBuscas();

        assertThrows(AcessoNegadoException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO)));
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoAtividadeEstaConcluida() {
        atividade.encerrar();
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", CODIGO)));

        assertEquals("Atividades concluídas não aceitam novas submissões.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }

    @Test
    void falhaQuandoCodigoNaoEhJava() {
        mockarBuscas();
        when(submissaoRepository.countByEstudanteMatriculaAndAtividadeId("123", 1L)).thenReturn(0L);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> submissaoService.submeter(1L, new SubmissaoRequest("123", "print('oi')")));

        assertEquals("A solução deve ser submetida em Java.", erro.getMessage());
        verify(submissaoRepository, never()).save(any());
    }
}