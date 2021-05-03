/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.issuance.restapi.controller;

import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
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

    @Operation(
        summary = "Creates new dgci",
        description = "Creates new dgci and return meta data for certificate creation"
    )
    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DgciIdentifier> initDgci(@Valid @RequestBody DgciInit dgciInit) {
        return ResponseEntity.ok(dgciService.initDgci(dgciInit));
    }

    @Operation(
        summary = "calculate cose signature for edgc",
        description = "calculate cose signature for given certificate hash, "
            + "generate TAN and update DGCI Registry database"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "signature created"),
        @ApiResponse(responseCode = "404", description = "dgci with related id not found"),
        @ApiResponse(responseCode = "400", description = "wrong issue data")})
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignatureData> finalizeDgci(@PathVariable long id, @Valid @RequestBody IssueData issueData)
        throws Exception {
        return ResponseEntity.ok(dgciService.finishDgci(id, issueData));
    }

    // Public API to get DID document of certification not needed in current architecture
    @GetMapping(value = "/V1/DE/{opaque}/{hash}")
    public ResponseEntity<DidDocument> getDidDocument(@PathVariable String opaque, String hash) {
        return ResponseEntity.ok(dgciService.getDidDocument(opaque, hash));
    }

    @Operation(
        summary = "Produce status message",
        description = "Produce status message"
    )
    @GetMapping(value = "/status")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("fine");
    }
}
