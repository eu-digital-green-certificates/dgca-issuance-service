package eu.europa.ec.dgc.issuance.restapi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.dgc.issuance.service.ContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/context")
@AllArgsConstructor
@ConditionalOnExpression("${issuance.endpoints.wallet:false}")
public class ContextController {
    private final ContextService contextService;

    @Operation(
        summary = "provide configuration information for wallet app",
        description = "list of claim endpoints for wallet app"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "server list")}
    )
    @GetMapping(value = "")
    public ResponseEntity<JsonNode> context() {
        return ResponseEntity.ok(contextService.getContextDefintion());
    }
}
