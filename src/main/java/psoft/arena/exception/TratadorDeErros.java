package psoft.arena.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse naoEncontrado(RecursoNaoEncontradoException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErroResponse naoAutenticado(NaoAutenticadoException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse transicaoInvalida(TransicaoInvalidaException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroResponse acessoNegado(AcessoNegadoException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse requisicaoInvalida(IllegalArgumentException e) {
        return new ErroResponse(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse corpoInvalido(HttpMessageNotReadableException e) {
        return new ErroResponse("Corpo da requisição inválido.");
    }
}
