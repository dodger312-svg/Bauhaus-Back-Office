package fr.insee.rmes.onion.infrastructure.webservice.colectica;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;
import fr.insee.rmes.onion.domain.port.clientside.ColecticaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ColecticaResourcesTest {

    private ColecticaService service;
    private ObjectMapper objectMapper;
    private ColecticaResources resources;

    @BeforeEach
    void setUp() {
        service = mock(ColecticaService.class);
        objectMapper = new ObjectMapper();
        resources = new ColecticaResources(service, objectMapper);
    }

    @Test
    void shouldReturnJsonPayloadWithMetadataAndDdi() throws Exception {
        ObjectNode metadata = objectMapper.createObjectNode().put("AgencyId", "fr.insee");
        ObjectNode ddi = objectMapper.createObjectNode();
        ddi.set("DDIInstance", objectMapper.createObjectNode().put("ID", "test"));
        when(service.getItem(any(ColecticaItemRequest.class))).thenReturn(new ColecticaItem(metadata, ddi));

        ColecticaItemRequest request = new ColecticaItemRequest("fr.insee", "id", "1", "StudyUnit", false, Map.of());

        JsonNode body = resources.getItem(request).getBody();

        assertThat(body).isNotNull();
        assertThat(body.path("metadata").path("AgencyId").asText()).isEqualTo("fr.insee");
        assertThat(body.path("ddi").path("DDIInstance").path("ID").asText()).isEqualTo("test");

        ArgumentCaptor<ColecticaItemRequest> captor = ArgumentCaptor.forClass(ColecticaItemRequest.class);
        verify(service).getItem(captor.capture());
        assertThat(captor.getValue()).isEqualTo(request);
    }

    @Test
    void shouldOmitMissingNodes() throws ColecticaRepositoryException {
        ObjectNode metadata = objectMapper.createObjectNode().put("Identifier", "id");
        when(service.getItem(any(ColecticaItemRequest.class))).thenReturn(new ColecticaItem(metadata, null));

        JsonNode body = resources.getItem(new ColecticaItemRequest("fr.insee", "id", "1", "StudyUnit", false, Map.of())).getBody();

        assertThat(body.path("metadata").path("Identifier").asText()).isEqualTo("id");
        assertThat(body.path("ddi").isMissingNode()).isTrue();
    }
}
