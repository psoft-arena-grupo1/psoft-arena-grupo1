package psoft.arena.service;

import static psoft.arena.service.Validacao.vazio;

import org.springframework.stereotype.Component;

import psoft.arena.model.EstrategiaAvaliacao;
import psoft.arena.model.Problema;

@Component
public class ComposicaoPrompt {

    private static final String TEXTO_AUSENTE = "Não informado.";
    private static final String INSTRUCAO_ANALISE_ESTATICA =
            "Esta avaliação é uma análise estática: o código-fonte não deve ser executado.";
    private static final String CONTRATO_SAIDA =
            "Responda somente com um objeto JSON no formato exato: "
                    + "{\"nota\": numero entre 0 e 10, \"evidencias\": [\"...\"], \"justificativa\": \"...\"}";

    public String compor(Problema problema, EstrategiaAvaliacao estrategia, String codigoFonte) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(texto(estrategia == null ? null : estrategia.getPrompt())).append("\n\n");
        prompt.append(INSTRUCAO_ANALISE_ESTATICA).append("\n\n");
        prompt.append("PROBLEMA\n");
        prompt.append("Título: ").append(texto(problema == null ? null : problema.getTitulo())).append('\n');
        prompt.append("Descrição: ").append(texto(problema == null ? null : problema.getDescricao())).append("\n\n");
        prompt.append("REQUISITOS FUNCIONAIS\n");
        prompt.append(texto(problema == null ? null : problema.getRequisitosFuncionais())).append("\n\n");
        prompt.append("REQUISITOS DE PROJETO\n");
        prompt.append(texto(problema == null ? null : problema.getRequisitosDeProjeto())).append("\n\n");
        prompt.append("CÓDIGO-FONTE (Java)\n");
        prompt.append(texto(codigoFonte)).append("\n\n");
        prompt.append(CONTRATO_SAIDA);
        return prompt.toString();
    }

    private static String texto(String valor) {
        return vazio(valor) ? TEXTO_AUSENTE : valor.trim();
    }
}
