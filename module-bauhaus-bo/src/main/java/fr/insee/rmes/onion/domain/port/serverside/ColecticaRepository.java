package fr.insee.rmes.onion.domain.port.serverside;

import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;

public interface ColecticaRepository {
    ColecticaItem fetchItem(ColecticaItemRequest request) throws ColecticaRepositoryException;
}
