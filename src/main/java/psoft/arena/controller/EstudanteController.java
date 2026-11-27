package psoft.arena.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import psoft.arena.dto.AcompanhamentoSubmissaoResponse;
import psoft.arena.dto.AtividadeEstudanteResponse;
import psoft.arena.dto.EstudanteRequest;
import psoft.arena.dto.EstudanteUpdateRequest;
import psoft.arena.model.Estudante;
import psoft.arena.service.AtividadeService;
import psoft.arena.service.EstudanteService;
import psoft.arena.service.SubmissaoService;

@RestController
@RequestMapping("/estudantes")
@Tag(name = "Estudantes", description = "US02 - Cadastro de estudantes")
public class EstudanteController {

    private final EstudanteService estudanteService;
    private final AtividadeService atividadeService;
    private final SubmissaoService submissaoService;

    public EstudanteController(EstudanteService estudanteService, AtividadeService atividadeService,
        SubmissaoService submissaoService) {
        this.estudanteService = estudanteService;
        this.atividadeService = atividadeService;
        this.submissaoService = submissaoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um estudante (matrícula única)")
    public Estudante criar(@RequestBody EstudanteRequest dados) {
        return estudanteService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista os estudantes, opcionalmente filtrando por trecho do nome")
    public List<Estudante> listar(@RequestParam(required = false) String nome) {
        return nome == null ? estudanteService.listar() : estudanteService.buscarPorNome(nome);
    }

    @GetMapping("/{matricula}")
    @Operation(summary = "Consulta um estudante pela matrícula")
    public Estudante buscar(@PathVariable String matricula) {
        return estudanteService.buscar(matricula);
    }

    @PutMapping("/{matricula}")
    @Operation(summary = "Edita nome e e-mail de um estudante")
    public Estudante atualizar(@PathVariable String matricula, @RequestBody EstudanteUpdateRequest dados) {
        return estudanteService.atualizar(matricula, dados);
    }

    @GetMapping("/{matricula}/atividades")
    @Operation(summary = "US07 - Lista as atividades das turmas das quais o estudante participa")
    public List<AtividadeEstudanteResponse> listarAtividades(@PathVariable String matricula) {
        return atividadeService.listarDoEstudante(matricula);
    }

    @GetMapping("/{matricula}/atividades/{id}")
    @Operation(summary = "US07 - Consulta uma atividade de uma turma da qual o estudante participa")
    public AtividadeEstudanteResponse buscarAtividade(@PathVariable String matricula, @PathVariable Long id) {
        return atividadeService.buscarDoEstudante(matricula, id);
    }

    @GetMapping("/{matricula}/submissoes")
    @Operation(summary = "US09 - Lista as submissões do estudante com o estado atual e o histórico de estados")
    public List<AcompanhamentoSubmissaoResponse> listarSubmissoes(@PathVariable String matricula) {
        return submissaoService.listarDoEstudante(matricula);
    }

    @GetMapping("/{matricula}/submissoes/{id}")
    @Operation(summary = "US09 - Consulta o andamento de uma submissão do estudante")
    public AcompanhamentoSubmissaoResponse buscarSubmissao(@PathVariable String matricula, @PathVariable Long id) {
        return submissaoService.buscarDoEstudante(matricula, id);
    }
}
