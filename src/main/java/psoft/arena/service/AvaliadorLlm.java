package psoft.arena.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import psoft.arena.model.Avaliacao;
import psoft.arena.model.Atividade;
import psoft.arena.model.ModeloLlm;
import psoft.arena.model.Submissao;

@Component
public class AvaliadorLlm implements AvaliadorSubmissao {

    private final AvaliacaoService avaliacaoService;
    private final ComposicaoPrompt composicaoPrompt;
    private final ClienteLlm clienteLlm;

    public AvaliadorLlm(AvaliacaoService avaliacaoService, ComposicaoPrompt composicaoPrompt, ClienteLlm clienteLlm) {
        this.avaliacaoService = avaliacaoService;
        this.composicaoPrompt = composicaoPrompt;
        this.clienteLlm = clienteLlm;
    }

    @Override
    public void avaliar(Submissao submissao) {
        Avaliacao avaliacao = avaliacaoService.iniciar(submissao.getId());
        try {
            Atividade atividade = submissao.getAtividade();
            String prompt = composicaoPrompt.compor(atividade.getProblema(), atividade.getEstrategia(),
                    submissao.getCodigoFonte());
            List<ModeloLlm> modelos = atividade.modelosDeAvaliacao();
            if (modelos == null || modelos.isEmpty()) {
                throw new FalhaAvaliacaoException("A atividade não possui modelos de LLM configurados para avaliação.");
            }

            List<ResultadoModelo> resultados = new ArrayList<>();
            List<String> falhas = new ArrayList<>();
            for (ModeloLlm modelo : modelos) {
                try {
                    RespostaAvaliacaoLlm resposta = clienteLlm.avaliar(modelo, prompt);
                    avaliacaoService.registrarResultado(avaliacao.getId(), modelo.getId(), resposta.nota(),
                            String.join("\n", resposta.evidencias()), resposta.justificativa());
                    resultados.add(new ResultadoModelo(modelo, resposta));
                } catch (RuntimeException e) {
                    String motivo = "Modelo " + nome(modelo) + ": " + mensagem(e);
                    falhas.add(motivo);
                    avaliacaoService.registrarFalhaModelo(avaliacao.getId(), modelo.getId(), motivo);
                }
            }

            if (!falhas.isEmpty()) {
                String motivo = String.join("; ", falhas);
                tentarFalhar(avaliacao.getId(), motivo);
                throw new FalhaAvaliacaoException(motivo);
            }

            avaliacaoService.concluir(avaliacao.getId(), notaFinal(resultados), evidenciasAgregadas(resultados),
                    justificativaAgregada(resultados));
        } catch (FalhaAvaliacaoException e) {
            throw e;
        } catch (RuntimeException e) {
            String motivo = "Falha inesperada durante a avaliação automática: " + mensagem(e);
            tentarFalhar(avaliacao.getId(), motivo);
            throw new FalhaAvaliacaoException(motivo, e);
        }
    }

    private void tentarFalhar(Long avaliacaoId, String motivo) {
        try {
            avaliacaoService.falhar(avaliacaoId, motivo);
        } catch (RuntimeException ignorada) {
            // preserva a causa original da falha: o motivo registrado já é o mais relevante
        }
    }

    private BigDecimal notaFinal(List<ResultadoModelo> resultados) {
        if (resultados.size() == 1) {
            return resultados.get(0).resposta().nota();
        }
        BigDecimal soma = BigDecimal.ZERO;
        for (ResultadoModelo resultado : resultados) {
            soma = soma.add(resultado.resposta().nota());
        }
        return soma.divide(BigDecimal.valueOf(resultados.size()), 1, RoundingMode.HALF_UP);
    }

    private String evidenciasAgregadas(List<ResultadoModelo> resultados) {
        if (resultados.size() == 1) {
            return String.join("\n", resultados.get(0).resposta().evidencias());
        }
        List<String> linhas = new ArrayList<>();
        for (ResultadoModelo resultado : resultados) {
            for (String evidencia : resultado.resposta().evidencias()) {
                linhas.add(resultado.modelo().getNome() + ": " + evidencia);
            }
        }
        return String.join("\n", linhas);
    }

    private String justificativaAgregada(List<ResultadoModelo> resultados) {
        if (resultados.size() == 1) {
            return resultados.get(0).resposta().justificativa();
        }
        StringBuilder texto = new StringBuilder("Média aritmética das notas dos modelos: ");
        for (int i = 0; i < resultados.size(); i++) {
            if (i > 0) {
                texto.append("; ");
            }
            ResultadoModelo resultado = resultados.get(i);
            texto.append(resultado.modelo().getNome()).append('=')
                    .append(resultado.resposta().nota().toPlainString());
        }
        for (ResultadoModelo resultado : resultados) {
            texto.append("\n").append(resultado.modelo().getNome()).append(": ")
                    .append(resultado.resposta().justificativa());
        }
        return texto.toString();
    }

    private static String nome(ModeloLlm modelo) {
        return modelo == null || modelo.getNome() == null || modelo.getNome().isBlank()
                ? "desconhecido"
                : modelo.getNome();
    }

    private static String mensagem(Throwable erro) {
        return erro.getMessage() == null || erro.getMessage().isBlank()
                ? erro.getClass().getSimpleName()
                : erro.getMessage();
    }

    private record ResultadoModelo(ModeloLlm modelo, RespostaAvaliacaoLlm resposta) {
    }
}
