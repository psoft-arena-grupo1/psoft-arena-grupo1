public class Problema {

    private int id;
    private String titulo;
    private String descricao;
    private String requisitosFuncionais;
    private String requisitosDeProjeto;
    private boolean ativo;
    private boolean utilizado;

    public Problema(int id, String titulo, String descricao, String requisitosFuncionais, String requisitosDeProjeto) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.requisitosFuncionais = requisitosFuncionais;
        this.requisitosDeProjeto = requisitosDeProjeto;
        this.ativo = true;
        this.utilizado = false;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void marcarComoUtilizado() {
        this.utilizado = true;
    }

    public boolean isUtilizado() {
        return utilizado;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getRequisitosFuncionais() {
        return requisitosFuncionais;
    }

    public void setRequisitosFuncionais(String requisitosFuncionais) {
        this.requisitosFuncionais = requisitosFuncionais;
    }

    public String getRequisitosDeProjeto() {
        return requisitosDeProjeto;
    }

    public void setRequisitosDeProjeto(String requisitosDeProjeto) {
        this.requisitosDeProjeto = requisitosDeProjeto;
    }

    public String getStatus() {
        return ativo ? "Ativo" : "Inativo";
    }

    @Override
    public String toString() {
        return "Problema{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", descricao='" + descricao + '\'' +
                ", requisitosFuncionais='" + requisitosFuncionais + '\'' +
                ", requisitosDeProjeto='" + requisitosDeProjeto + '\'' +
                ", status='" + getStatus() + '\'' +
                '}';
    }
}
