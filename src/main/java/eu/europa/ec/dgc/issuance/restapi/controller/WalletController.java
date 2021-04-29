package eu.europa.ec.dgc.issuance.restapi.controller;

import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimResponse;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci/wallet")
@AllArgsConstructor
public class WalletController {
    private final DgciService dgciService;

    @Operation(
            summary = "claim dgci",
            description = "claim, assign dgci public key by TAN"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "successful claim"),
            @ApiResponse(responseCode = "404", description = "dgci not found"),
            @ApiResponse(responseCode = "400", description = "wrong claim data")})
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> claimUpdate(@Valid @RequestBody ClaimRequest claimRequest) throws Exception {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClaimResponse> claim(@Valid @RequestBody ClaimRequest claimRequest) throws Exception {
        return ResponseEntity.ok(dgciService.claimUpdate(claimRequest));
    }

}
