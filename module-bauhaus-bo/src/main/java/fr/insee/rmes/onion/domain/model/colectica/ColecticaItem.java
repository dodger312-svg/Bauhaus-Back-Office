package fr.insee.rmes.onion.domain.model.colectica;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ColecticaItem(ObjectNode metadata, JsonNode ddi) {
}
