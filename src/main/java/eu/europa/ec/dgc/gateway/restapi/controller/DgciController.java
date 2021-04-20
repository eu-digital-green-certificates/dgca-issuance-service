package eu.europa.ec.dgc.gateway.restapi.controller;

import eu.europa.ec.dgc.gateway.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.gateway.restapi.dto.DgciInit;
import eu.europa.ec.dgc.gateway.restapi.dto.IssueData;
import eu.europa.ec.dgc.gateway.restapi.dto.SignatureData;
import eu.europa.ec.dgc.gateway.service.DgciService;
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
    public ResponseEntity<SignatureData> finalizeDgci(@PathVariable long id, @RequestBody IssueData issueData) {
        return ResponseEntity.ok(dgciService.finishDgci(id, issueData));
    }

    @GetMapping(value = "/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello");
    }
}
