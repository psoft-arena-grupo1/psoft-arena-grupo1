public class Atividade {

    private int id;
    private Problema problema;

    public Atividade(int id, Problema problema) {
        if (!problema.isAtivo()) {
            throw new IllegalArgumentException("Problemas inativos não podem ser associados a novas atividades.");
        }
        this.id = id;
        this.problema = problema;
        problema.marcarComoUtilizado();
    }

    public int getId() {
        return id;
    }

    public Problema getProblema() {
        return problema;
    }

    @Override
    public String toString() {
        return "Atividade " + id + " - problema: " + problema.getTitulo();
    }
}
