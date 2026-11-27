package psoft.arena.service;

public class FalhaAvaliacaoException extends RuntimeException {

    public FalhaAvaliacaoException(String mensagem) {
        super(mensagem);
    }

    public FalhaAvaliacaoException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
