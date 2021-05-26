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

import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import eu.europa.ec.dgc.issuance.restapi.dto.PublicKeyInfo;
import eu.europa.ec.dgc.issuance.service.CertificateService;
import eu.europa.ec.dgc.issuance.service.EdgcValidator;
import eu.europa.ec.dgc.issuance.utils.CborDumpService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The endpoint here are not public API and should be used only for developing testing purposes.
 */
@RestController
@RequestMapping("/cert")
@AllArgsConstructor
@ConditionalOnExpression("${issuance.endpoints.testTools:false}")
public class CertController {

    private final CertificateService certificateService;
    private final CborDumpService cborDumpService;
    private final EdgcValidator edgcValidator;

    /**
     * Rest Controller to decode CBOR.
     */
    @Operation(
        summary = "dump base64 cbor byte stream, developing tool"
    )
    @PostMapping(value = "dumpCBOR", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> decodeCbor(@RequestBody String cbor) throws IOException {
        StringWriter stringWriter = new StringWriter();

        cborDumpService.dumpCbor(Base64.getDecoder().decode(cbor), stringWriter);

        return ResponseEntity.ok(stringWriter.getBuffer().toString());
    }

    /**
     * decode and debug edgc.
     * This method tries decode and debug the edgc certificate.
     * It tries to provide as much usable information as possible.
     *
     * @param prefixedEncodedCompressedCose edgc
     * @return decode result
     */
    @Operation(
        summary = "decode edgc, developing tool",
        description = "decode and validate edgc raw string, extract raw data of each decode stage"
    )
    @PostMapping(value = "decodeEGC", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EgcDecodeResult> decodeEgCert(
        @RequestBody String prefixedEncodedCompressedCose) {

        EgcDecodeResult egcDecodeResult = edgcValidator.decodeEdgc(prefixedEncodedCompressedCose);
        return ResponseEntity.ok(egcDecodeResult);
    }


    /**
     * Rest Controller to get Public Key Information.
     */
    @Operation(
        summary = "get information about edgc public key, developing tool"
    )
    @GetMapping(value = "publicKey")
    public ResponseEntity<PublicKeyInfo> getPublic() {
        PublicKeyInfo result = new PublicKeyInfo(
            certificateService.getKidAsBase64(),
            certificateService.getAlgorithmIdentifier(),
            certificateService.getCertficate().getPublicKey().getAlgorithm(),
            certificateService.getCertficate().getPublicKey().getFormat(),
            Base64.getEncoder().encodeToString(certificateService.getCertficate().getPublicKey().getEncoded()));

        return ResponseEntity.ok(result);
    }


}
