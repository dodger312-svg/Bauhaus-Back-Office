package fr.insee.rmes.onion.domain.exceptions.colectica;

import org.springframework.http.HttpStatus;

public class ColecticaRepositoryTechnicalException extends ColecticaRepositoryException {

    public ColecticaRepositoryTechnicalException(String message, String details) {
        super(HttpStatus.BAD_GATEWAY.value(), message, details);
    }

    public ColecticaRepositoryTechnicalException(String message, String details, Throwable cause) {
        super(HttpStatus.BAD_GATEWAY.value(), message, details, cause);
    }
}
