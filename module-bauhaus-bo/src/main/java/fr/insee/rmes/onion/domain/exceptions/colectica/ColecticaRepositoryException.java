package fr.insee.rmes.onion.domain.exceptions.colectica;

import fr.insee.rmes.onion.domain.exceptions.RmesException;

public abstract class ColecticaRepositoryException extends RmesException {

    protected ColecticaRepositoryException(int status, String message, String details) {
        super(status, message, details);
    }

    protected ColecticaRepositoryException(int status, String message, String details, Throwable cause) {
        super(status, message, details, cause);
    }
}
