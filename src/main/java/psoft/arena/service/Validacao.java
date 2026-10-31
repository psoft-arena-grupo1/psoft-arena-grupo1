package psoft.arena.service;

final class Validacao {

    private Validacao() {
    }

    static boolean vazio(String campo) {
        return campo == null || campo.isBlank();
    }
}
