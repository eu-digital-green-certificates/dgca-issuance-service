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

import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimResponse;
import eu.europa.ec.dgc.issuance.service.DgciService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dgci/wallet")
@AllArgsConstructor
@ConditionalOnExpression("${issuance.endpoints.wallet:false}")
public class WalletController {
    private final DgciService dgciService;

    @Operation(
        summary = "Claims the DGCI for a TAN and certificate Holder",
        description = "claim, check signatue, cert hash, TAN, assign dgci public key "
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "successful claim"),
        @ApiResponse(responseCode = "404", description = "dgci not found"),
        @ApiResponse(responseCode = "400", description = "wrong claim data")})
    @PostMapping(value = "/claim", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClaimResponse> claim(@Valid @RequestBody ClaimRequest claimRequest) {
        return ResponseEntity.ok(dgciService.claim(claimRequest));
    }
}
