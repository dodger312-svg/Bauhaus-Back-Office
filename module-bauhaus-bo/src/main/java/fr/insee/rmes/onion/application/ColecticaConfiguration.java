package fr.insee.rmes.onion.application;

import fr.insee.rmes.onion.domain.port.clientside.ColecticaService;
import fr.insee.rmes.onion.domain.port.serverside.ColecticaRepository;
import fr.insee.rmes.onion.domain.services.colectica.ColecticaServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ColecticaConfiguration {

    @Bean
    ColecticaService colecticaService(ColecticaRepository repository) {
        return new ColecticaServiceImpl(repository);
    }
}
