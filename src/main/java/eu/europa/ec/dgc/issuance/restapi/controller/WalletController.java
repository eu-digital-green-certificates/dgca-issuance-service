package eu.europa.ec.dgc.issuance.restapi.controller;

import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimResponse;
import eu.europa.ec.dgc.issuance.service.DgciService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci/wallet")
@AllArgsConstructor
public class WalletController {
    private final DgciService dgciService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClaimResponse> claimUpdate(@RequestBody ClaimRequest claimRequest) throws Exception {
        return ResponseEntity.ok(dgciService.claimUpdate(claimRequest));
    }

    @PutMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClaimResponse> claim(@RequestBody ClaimRequest claimRequest) throws Exception {
        return ResponseEntity.ok(dgciService.claim(claimRequest));
    }

}
