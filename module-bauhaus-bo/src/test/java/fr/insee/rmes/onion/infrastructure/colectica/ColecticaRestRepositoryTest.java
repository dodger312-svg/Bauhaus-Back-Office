package fr.insee.rmes.onion.infrastructure.colectica;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.rmes.config.colectica.ColecticaRepositoryProperties;
import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ColecticaRestRepositoryTest {

    private static final String SAMPLE_XML = """
            <DDIInstance xmlns=\"ddi:instance:3_3\"
                         xmlns:r=\"ddi:reusable:3_3\"
                         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
                         xsi:schemaLocation=\"ddi:instance:3_3 https://www.ddialliance.org/Specification/DDI-Lifecycle/3.3/XMLSchema/instance.xsd\">
               <r:Agency>fr.insee</r:Agency>
               <r:ID>example-id</r:ID>
               <r:Version>1</r:Version>
            </DDIInstance>
            """;

    private ColecticaRestRepository repository;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        ColecticaRepositoryProperties properties = new ColecticaRepositoryProperties();
        properties.setBaseUrl(URI.create("http://localhost:8089"));
        properties.setConnectTimeout(Duration.ofSeconds(5));
        properties.setReadTimeout(Duration.ofSeconds(5));

        repository = new ColecticaRestRepository(new RestTemplateBuilder(), properties, new ObjectMapper());
        RestTemplate restTemplate = (RestTemplate) Objects.requireNonNull(
                ReflectionTestUtils.getField(repository, "restTemplate"));
        server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    }

    @Test
    void shouldFetchItemAndConvertDdiXmlToJson() throws ColecticaRepositoryException {
        String payload = new ObjectMapper().createObjectNode()
                .put("AgencyId", "fr.insee")
                .put("Identifier", "example-id")
                .put("Version", "1")
                .put("ItemType", "StudyUnit")
                .put("Item", Base64.getEncoder().encodeToString(SAMPLE_XML.getBytes(StandardCharsets.UTF_8)))
                .toString();

        server.expect(requestTo("http://localhost:8089/Repository/api/items/StudyUnit/fr.insee/example-id/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(payload, MediaType.APPLICATION_JSON));

        ColecticaItemRequest request = new ColecticaItemRequest("fr.insee", "example-id", "1", "StudyUnit", false, Map.of());
        ColecticaItem item = repository.fetchItem(request);

        assertThat(item.metadata().path("AgencyId").asText()).isEqualTo("fr.insee");
        assertThat(item.metadata().path("Item").isMissingNode()).isTrue();
        assertThat(item.ddi()).isNotNull();
        assertThat(item.ddi().toString()).contains("example-id");
        server.verify();
    }
}
