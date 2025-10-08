package fr.insee.rmes.onion.domain.port.clientside;

import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;

public interface ColecticaService {
    ColecticaItem getItem(ColecticaItemRequest request) throws ColecticaRepositoryException;
}
