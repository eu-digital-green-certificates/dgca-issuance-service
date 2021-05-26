package eu.europa.ec.dgc.issuance.restapi.controller;


import eu.europa.ec.dgc.issuance.service.CertKeyPublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci/certPublish")
@AllArgsConstructor
@ConditionalOnExpression("${issuance.endpoints.publishCert:false}")
public class CertPublisherController {
    private final CertKeyPublisherService certKeyPublisherService;

    @Operation(
        summary = "publish edgc signing public key to dgc gateway",
        description = "public key need to be published so the created edgc can be validated."
            + " The signature is validated agains published keys"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "cert published")}
    )
    @PutMapping(value = "")
    public ResponseEntity<Void> publishEdgcKeyToGateway() {
        certKeyPublisherService.publishKey();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
