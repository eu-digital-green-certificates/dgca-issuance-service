package eu.europa.ec.dgc.issuance.restapi.controller;

import com.nimbusds.jose.util.Base64URL;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Base64;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci")
@AllArgsConstructor
@ConditionalOnExpression("${issuance.endpoints.did:false}")
public class DgciDidController {
    private final DgciService dgciService;

    /**
     * dgci status.
     * @param dgciHash hash
     * @return response
     */
    @Operation(
        summary = "Checks the status of DGCI",
        description = "Produce status HTTP code message"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "dgci exists"),
        @ApiResponse(responseCode = "424", description = "dgci locked"),
        @ApiResponse(responseCode = "404", description = "dgci not found")})
    @RequestMapping(value = "/{dgciHash}",method = RequestMethod.HEAD)
    public ResponseEntity<Void> dgciStatus(
        @Parameter(description = "Base64URL encoded SHA-256 hash from dgci alias uvci", required = true)
        @PathVariable(name = "dgciHash") String dgciHash) {
        String dgciHashBase64 = Base64.getEncoder().encodeToString(Base64URL.from(dgciHash).decode());
        HttpStatus httpStatus;
        switch (dgciService.checkDgciStatus(dgciHashBase64)) {
            case EXISTS:
                httpStatus = HttpStatus.NO_CONTENT;
                break;
            case LOCKED:
                httpStatus = HttpStatus.LOCKED;
                break;
            case NOT_EXISTS:
                httpStatus = HttpStatus.NOT_FOUND;
                break;
            default:
                throw new IllegalArgumentException("unknown dgci status");
        }
        return ResponseEntity.status(httpStatus).build();
    }

    @Operation(
        summary = "Returns a DID document",
        description = "Return a DID document"
    )
    @GetMapping(value = "/{dgciHash}")
    public ResponseEntity<DidDocument> getDidDocument(
        @Parameter(description = "Base64URL encoded SHA-256 hash from dgci alias uvci", required = true)
        @PathVariable(name = "dgciHash") String dgciHash) {
        String dgciHashBase64 = Base64.getEncoder().encodeToString(Base64URL.from(dgciHash).decode());
        return ResponseEntity.ok(dgciService.getDidDocument(dgciHashBase64));
    }
}
