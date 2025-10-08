package fr.insee.rmes.onion.domain.exceptions.colectica;

import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;
import org.springframework.http.HttpStatus;

public class ColecticaItemNotFoundException extends ColecticaRepositoryException {

    public ColecticaItemNotFoundException(ColecticaItemRequest request) {
        super(HttpStatus.NOT_FOUND.value(),
                "Colectica item not found",
                String.format("No item found for agency=%s, identifier=%s, version=%s, itemType=%s",
                        request.agency(), request.identifier(), request.version(), request.itemType()));
    }
}
