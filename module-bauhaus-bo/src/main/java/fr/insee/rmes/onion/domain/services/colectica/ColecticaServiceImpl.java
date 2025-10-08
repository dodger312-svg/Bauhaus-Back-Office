package fr.insee.rmes.onion.domain.services.colectica;

import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;
import fr.insee.rmes.onion.domain.port.clientside.ColecticaService;
import fr.insee.rmes.onion.domain.port.serverside.ColecticaRepository;

public class ColecticaServiceImpl implements ColecticaService {

    private final ColecticaRepository repository;

    public ColecticaServiceImpl(ColecticaRepository repository) {
        this.repository = repository;
    }

    @Override
    public ColecticaItem getItem(ColecticaItemRequest request) throws ColecticaRepositoryException {
        return repository.fetchItem(request);
    }
}
