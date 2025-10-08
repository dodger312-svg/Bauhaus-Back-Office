package fr.insee.rmes.onion.infrastructure.webservice.colectica;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.rmes.onion.domain.exceptions.colectica.ColecticaRepositoryException;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItem;
import fr.insee.rmes.onion.domain.model.colectica.ColecticaItemRequest;
import fr.insee.rmes.onion.domain.port.clientside.ColecticaService;
import fr.insee.rmes.rbac.HasAccess;
import fr.insee.rmes.rbac.RBAC;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/colectica")
@SecurityRequirement(name = "bearerAuth")
@ConditionalOnExpression("'${fr.insee.rmes.bauhaus.activeModules}'.contains('colectica')")
public class ColecticaResources {

    private final ColecticaService service;
    private final ObjectMapper objectMapper;

    public ColecticaResources(ColecticaService service, ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/item", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @HasAccess(module = RBAC.Module.EXTERNAL_COLECTICA, privilege = RBAC.Privilege.READ)
    @Operation(summary = "Fetch an item from the Colectica repository",
            responses = {@ApiResponse(responseCode = "200", description = "Item retrieved",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(type = "object")))})
    public ResponseEntity<JsonNode> getItem(@Valid @RequestBody ColecticaItemRequest request)
            throws ColecticaRepositoryException {
        ColecticaItem item = service.getItem(request);
        ObjectNode payload = objectMapper.createObjectNode();
        if (item.metadata() != null) {
            payload.set("metadata", item.metadata());
        }
        if (item.ddi() != null) {
            payload.set("ddi", item.ddi());
        }
        return ResponseEntity.ok(payload);
    }
}
