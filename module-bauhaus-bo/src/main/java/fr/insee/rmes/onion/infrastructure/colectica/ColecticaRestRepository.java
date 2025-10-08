package fr.insee.rmes.onion.infrastructure.colectica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fr.insee.ddi.lifecycle33.instance.DDIInstanceDocument;
import fr.insee.rmes.config.colectica.ColecticaRepositoryProperties;
import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaItemNotFoundException;
import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryTechnicalException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;
import fr.insee.rmes.onion.domain.port.serverside.ColecticaRepository;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Repository
public class ColecticaRestRepository implements ColecticaRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ColecticaRestRepository.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    private final ColecticaRepositoryProperties properties;

    public ColecticaRestRepository(RestTemplateBuilder restTemplateBuilder,
                                   ColecticaRepositoryProperties properties,
                                   ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.xmlMapper = new XmlMapper();

        RestTemplateBuilder builder = restTemplateBuilder
                .setConnectTimeout(properties.getConnectTimeout())
                .setReadTimeout(properties.getReadTimeout());

        if (StringUtils.hasText(properties.getUsername())) {
            builder = builder.basicAuthentication(properties.getUsername(), properties.getPassword());
        }

        this.restTemplate = builder.build();
    }

    @Override
    public ColecticaItem fetchItem(ColecticaItemRequest request) throws ColecticaRepositoryException {
        URI targetUri = buildUri(request);
        HttpEntity<Void> entity = new HttpEntity<>(createHeaders());
        try {
            ResponseEntity<String> response = restTemplate.exchange(targetUri, HttpMethod.GET, entity, String.class);
            return transformResponse(response.getBody());
        } catch (HttpStatusCodeException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ColecticaItemNotFoundException(request);
            }
            throw new ColecticaRepositoryTechnicalException("Colectica repository returned an error", ex.getResponseBodyAsString(), ex);
        }
    }

    private URI buildUri(ColecticaItemRequest request) {
        String templatePath = request.latestVersion() ? properties.getLatestItemPath() : properties.getItemPath();
        UriTemplate template = new UriTemplate(templatePath);

        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("itemType", request.itemType());
        uriVariables.put("agency", request.agency());
        uriVariables.put("identifier", request.identifier());
        if (!request.latestVersion()) {
            uriVariables.put("version", request.version());
        }

        URI path = template.expand(uriVariables);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUri(properties.getBaseUrl())
                .path(path.getPath());

        request.queryParameters().forEach(builder::queryParam);

        return builder.build(true).toUri();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(MediaType.parseMediaTypes(MediaType.APPLICATION_JSON_VALUE));
        return headers;
    }

    private ColecticaItem transformResponse(String body) throws ColecticaRepositoryTechnicalException {
        if (!StringUtils.hasText(body)) {
            throw new ColecticaRepositoryTechnicalException("Empty Colectica response", "Response body was empty");
        }

        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(body);
        } catch (JsonProcessingException ex) {
            throw new ColecticaRepositoryTechnicalException("Unable to parse Colectica JSON response", ex.getOriginalMessage(), ex);
        }
        if (!rootNode.isObject()) {
            throw new ColecticaRepositoryTechnicalException("Unexpected Colectica payload", body);
        }

        ObjectNode metadata = objectMapper.createObjectNode();
        JsonNode itemNode = null;
        var fields = rootNode.fields();
        while (fields.hasNext()) {
            Entry<String, JsonNode> entry = fields.next();
            if ("Item".equals(entry.getKey())) {
                itemNode = entry.getValue();
            } else {
                metadata.set(entry.getKey(), entry.getValue());
            }
        }

        JsonNode ddiJson = parseDdi(itemNode);
        return new ColecticaItem(metadata, ddiJson);
    }

    private JsonNode parseDdi(JsonNode itemNode) throws ColecticaRepositoryTechnicalException {
        if (itemNode == null || itemNode.isMissingNode() || itemNode.isNull()) {
            return null;
        }

        String raw = extractContent(itemNode);
        if (!StringUtils.hasText(raw)) {
            return null;
        }

        String xml = decodeIfNeeded(raw);
        if (!StringUtils.hasText(xml)) {
            return null;
        }

        // Validate XML using the DDI model
        try {
            DDIInstanceDocument.Factory.parse(xml);
        } catch (XmlException ex) {
            throw new ColecticaRepositoryTechnicalException("Invalid DDI payload returned by Colectica", ex.getMessage(), ex);
        }
        try {
            return xmlMapper.readTree(xml);
        } catch (JsonProcessingException ex) {
            throw new ColecticaRepositoryTechnicalException("Unable to transform DDI XML into JSON", ex.getOriginalMessage(), ex);
        }
    }

    private String extractContent(JsonNode itemNode) {
        if (itemNode.isTextual()) {
            return itemNode.asText();
        }
        if (itemNode.has("XmlContent")) {
            return itemNode.get("XmlContent").asText();
        }
        if (itemNode.has("Content")) {
            return itemNode.get("Content").asText();
        }
        if (itemNode.has("Xml")) {
            return itemNode.get("Xml").asText();
        }
        return itemNode.toString();
    }

    private String decodeIfNeeded(String value) {
        String trimmed = value == null ? null : value.trim();
        if (!StringUtils.hasText(trimmed)) {
            return null;
        }

        if (trimmed.startsWith("<")) {
            return value;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(trimmed);
            String decodedString = new String(decoded, StandardCharsets.UTF_8);
            if (decodedString.trim().startsWith("<")) {
                return decodedString;
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.debug("Value is not base64 encoded DDI", ex);
        }

        return value;
    }
}
