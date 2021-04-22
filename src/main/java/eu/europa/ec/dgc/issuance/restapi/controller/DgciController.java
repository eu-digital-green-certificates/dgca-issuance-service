package eu.europa.ec.dgc.issuance.restapi.controller;

import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import eu.europa.ec.dgc.issuance.service.DgciService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci")
@AllArgsConstructor
public class DgciController {
    private final DgciService dgciService;

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DgciIdentifier> initDgci(@RequestBody DgciInit dgciInit) {
        return ResponseEntity.ok(dgciService.initDgci(dgciInit));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignatureData> finalizeDgci(@PathVariable long id, @RequestBody IssueData issueData)
            throws Exception {
        return ResponseEntity.ok(dgciService.finishDgci(id, issueData));
    }

    // Public API to get DID document of certification not needed in current architecture
    @GetMapping(value = "/V1/DE/{opaque}/{hash}")
    public ResponseEntity<DidDocument> getDidDocument(@PathVariable String opaque, String hash) {
        return ResponseEntity.ok(dgciService.getDidDocument(opaque,hash));
    }

    @GetMapping(value = "/status")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("fine");
    }
}
