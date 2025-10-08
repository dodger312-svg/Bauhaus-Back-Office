package fr.insee.rmes.onion.domain.model.colectica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

public record ColecticaItemRequest(
        @NotBlank String agency,
        @NotBlank String identifier,
        String version,
        @NotBlank String itemType,
        @JsonProperty(defaultValue = "false") boolean latestVersion,
        Map<String, String> queryParameters
) {

    @JsonCreator
    public ColecticaItemRequest {
        queryParameters = CollectionUtils.isEmpty(queryParameters) ? Collections.emptyMap() : Map.copyOf(queryParameters);
    }

    @AssertTrue(message = "version is required when latestVersion is false")
    public boolean isVersionPresentWhenRequired() {
        return latestVersion || StringUtils.hasText(version);
    }
}
