package psoft.arena.config;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Component
@Converter
public class ChaveApiConverter implements AttributeConverter<String, String> {

    private final CifradorChaveApi cifrador;

    public ChaveApiConverter(CifradorChaveApi cifrador) {
        this.cifrador = cifrador;
    }

    @Override
    public String convertToDatabaseColumn(String chaveApi) {
        return cifrador.cifrar(chaveApi);
    }

    @Override
    public String convertToEntityAttribute(String valorCifrado) {
        return cifrador.decifrar(valorCifrado);
    }
}
