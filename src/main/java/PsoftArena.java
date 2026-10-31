import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PsoftArena {
    private Map<Integer, Problema> problemas;
    private Map<Integer, Atividade> atividades;
    private Map<String, Estudante> estudantes;
    private int proximoIdProblema;
    private int proximoIdAtividade;

    public PsoftArena() {
        this.problemas = new HashMap<>();
        this.atividades = new HashMap<>();
        this.estudantes = new HashMap<>();
        this.proximoIdProblema = 1;
        this.proximoIdAtividade = 1;
    }

    public int criarProblema(String titulo, String descricao, String requisitosFuncionais, String requisitosDeProjeto) {
        validarCampos(titulo, descricao, requisitosFuncionais, requisitosDeProjeto);
        Problema novoProblema = new Problema(proximoIdProblema, titulo, descricao, requisitosFuncionais, requisitosDeProjeto);
        problemas.put(novoProblema.getId(), novoProblema);
        proximoIdProblema++;
        return novoProblema.getId();
    }

    public Problema buscarProblema(int id) {
        Problema current = problemas.get(id);
        if (current == null) {
            throw new IllegalArgumentException("Problema não encontrado.");
        }
        return current;
    }

    public Collection<Problema> listarProblemas() {
        return problemas.values();
    }

    public List<Problema> buscarProblemasPorTitulo(String titulo) {
        List<Problema> encontrados = new ArrayList<>();
        for (Problema p : problemas.values()) {
            if (p.getTitulo().toLowerCase().contains(titulo.toLowerCase())) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }

    public void atualizarProblema(int id, String titulo, String descricao, String requisitosFuncionais, String requisitosDeProjeto) {
        validarCampos(titulo, descricao, requisitosFuncionais, requisitosDeProjeto);
        Problema current = buscarProblema(id);
        current.setTitulo(titulo);
        current.setDescricao(descricao);
        current.setRequisitosFuncionais(requisitosFuncionais);
        current.setRequisitosDeProjeto(requisitosDeProjeto);
    }

    public void desativarProblema(int id) {
        buscarProblema(id).desativar();
    }

    public void removerProblema(int id) {
        if (buscarProblema(id).isUtilizado()) {
            throw new IllegalArgumentException("Problemas já utilizados em atividades não podem ser removidos.");
        }
        problemas.remove(id);
    }

    public int criarAtividade(int idProblema) {
        Atividade novaAtividade = new Atividade(proximoIdAtividade, buscarProblema(idProblema));
        atividades.put(novaAtividade.getId(), novaAtividade);
        proximoIdAtividade++;
        return novaAtividade.getId();
    }

    public Atividade buscarAtividade(int id) {
        Atividade atividade = atividades.get(id);
        if (atividade == null) {
            throw new IllegalArgumentException("Atividade não encontrada.");
        }
        return atividade;
    }

    public Collection<Atividade> listarAtividades() {
        return atividades.values();
    }

    public String criarEstudante(String nome, String matricula, String email) {
        validarCamposEstudante(nome, matricula, email);
        String chave = matricula.strip();
        if (estudantes.containsKey(chave)) {
            throw new IllegalArgumentException("Já existe um estudante com essa matrícula.");
        }
        estudantes.put(chave, new Estudante(nome, chave, email));
        return chave;
    }

    public Estudante buscarEstudante(String matricula) {
        Estudante current = matricula == null ? null : estudantes.get(matricula.strip());
        if (current == null) {
            throw new IllegalArgumentException("Estudante não encontrado.");
        }
        return current;
    }

    public Collection<Estudante> listarEstudantes() {
        return estudantes.values();
    }

    public List<Estudante> buscarEstudantesPorNome(String nome) {
        List<Estudante> encontrados = new ArrayList<>();
        for (Estudante e : estudantes.values()) {
            if (e.getNome().toLowerCase().contains(nome.toLowerCase())) {
                encontrados.add(e);
            }
        }
        return encontrados;
    }

    public void atualizarEstudante(String matricula, String nome, String email) {
        validarCamposEstudante(nome, matricula, email);
        Estudante current = buscarEstudante(matricula);
        current.setNome(nome);
        current.setEmail(email);
    }

    private void validarCampos(String titulo, String descricao, String requisitosFuncionais, String requisitosDeProjeto) {
        if (vazio(titulo) || vazio(descricao) || vazio(requisitosFuncionais) || vazio(requisitosDeProjeto)) {
            throw new IllegalArgumentException("Título, descrição, requisitos funcionais e requisitos de projeto são obrigatórios.");
        }
    }

    private void validarCamposEstudante(String nome, String matricula, String email) {
        if (vazio(nome) || vazio(matricula) || vazio(email)) {
            throw new IllegalArgumentException("Nome, matrícula e e-mail são obrigatórios.");
        }
        if (!email.strip().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
    }

    private boolean vazio(String campo) {
        return campo == null || campo.isBlank();
    }
}