package eu.europa.ec.dgc.gateway.restapi.controller;

import eu.europa.ec.dgc.gateway.restapi.dto.*;
import eu.europa.ec.dgc.gateway.service.DGCIService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dgci")
@AllArgsConstructor
public class DGCIController {
  private final DGCIService dgciService;

  @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<DGCIObject> initDGCI(@RequestBody DGCIInit dgciInit) {
    return ResponseEntity.ok(dgciService.initDGCI(dgciInit));
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SignatureData> finalizeDGCI(@PathVariable long id,@RequestBody IssueData issueData) {
    return ResponseEntity.ok(dgciService.finishDGCI(id,issueData));
  }

  @GetMapping(value = "/hello")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok("Hello");
  }
}
