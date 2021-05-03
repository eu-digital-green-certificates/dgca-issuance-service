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

import COSE.CoseException;
import COSE.KeyKeys;
import COSE.OneKey;
import COSE.Sign1Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import ehn.techiop.hcert.data.Eudgc;
import ehn.techiop.hcert.kotlin.chain.Base45Service;
import ehn.techiop.hcert.kotlin.chain.Chain;
import ehn.techiop.hcert.kotlin.chain.ChainResult;
import ehn.techiop.hcert.kotlin.chain.CompressorService;
import ehn.techiop.hcert.kotlin.chain.ContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.VerificationResult;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultBase45Service;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCborService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCompressorService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCoseService;
import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import eu.europa.ec.dgc.issuance.restapi.dto.PublicKeyInfo;
import eu.europa.ec.dgc.issuance.service.CertificateService;
import eu.europa.ec.dgc.issuance.service.EhdCryptoService;
import eu.europa.ec.dgc.issuance.utils.CborDump;
import io.swagger.v3.oas.annotations.Operation;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cert")
@AllArgsConstructor
@Profile("dev")
public class CertController {
    private final CertificateService certificateService;
    private final EhdCryptoService ehdCryptoService;

    /**
     * Controller for creating Vaccination Certificate.
     */
    @PostMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChainResult> createVaccinationCertificate(@RequestBody Eudgc eudgc) {
        // Taken from https://github.com/ehn-digital-green-development/hcert-kotlin/blob/main/src/test/kotlin/ehn/techiop/hcert/kotlin/chain/CborProcessingChainTest.kt
        val coseService = new DefaultCoseService(ehdCryptoService);
        val contextIdentifierService = new DefaultContextIdentifierService();
        val compressorService = new DefaultCompressorService();
        val base45Service = new DefaultBase45Service();
        val cborService = new DefaultCborService();
        Chain cborProcessingChain =
                new Chain(cborService, coseService,
                        contextIdentifierService, compressorService, base45Service);
        ChainResult chainResult = cborProcessingChain.encode(eudgc);
        return ResponseEntity.ok(chainResult);
    }

    /**
     * Rest Controller to decode CBOR.
     */
    @PostMapping(value = "dumpCBOR", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> decodeCbor(@RequestBody String cbor) throws IOException {
        StringWriter stringWriter = new StringWriter();
        new CborDump().dumpCbor(Base64.getDecoder().decode(cbor), stringWriter);
        return ResponseEntity.ok(stringWriter.getBuffer().toString());
    }

    /**
     * decode edgc.
     * @param prefixedEncodedCompressedCose edgc
     * @return decode result
     * @throws IOException IOException
     */
    @Operation(
            summary = "decode edgc",
            description = "decode and validy edgc raw string, extract raw data of each decode stage"
    )
    @PostMapping(value = "decodeEGC", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EgcDecodeResult> decodeEgCert(
            @RequestBody String prefixedEncodedCompressedCose) throws IOException {
        VerificationResult verificationResult = new VerificationResult();
        ContextIdentifierService contextIdentifierService = new DefaultContextIdentifierService();
        Base45Service base45Service = new DefaultBase45Service();
        CompressorService compressorService = new DefaultCompressorService();
        val plainInput = contextIdentifierService.decode(prefixedEncodedCompressedCose, verificationResult);
        val compressedCose = base45Service.decode(plainInput, verificationResult);

        EgcDecodeResult egcDecodeResult = new EgcDecodeResult();
        try {
            val cose = compressorService.decode(compressedCose, verificationResult);
            egcDecodeResult.setCoseHex(Hex.toHexString(cose));

            CBORObject map = CBORObject.NewMap();
            OneKey oneKey;
            if (certificateService.getPublicKey() instanceof RSAPublicKey) {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) certificateService.getPublicKey();
                map.set(KeyKeys.KeyType.AsCBOR(), KeyKeys.KeyType_RSA);
                map.set(KeyKeys.RSA_N.AsCBOR(), stripLeadingZero(rsaPublicKey.getModulus()));
                map.set(KeyKeys.RSA_E.AsCBOR(), stripLeadingZero(rsaPublicKey.getPublicExponent()));
                oneKey = new OneKey(map);
            } else {
                ECPublicKey ecPublicKey = (ECPublicKey) certificateService.getPublicKey();
                map.set(KeyKeys.KeyType.AsCBOR(), KeyKeys.KeyType_EC2);
                map.set(KeyKeys.EC2_Curve.AsCBOR(),getEcCurve(ecPublicKey));
                map.set(KeyKeys.EC2_X.AsCBOR(),stripLeadingZero(ecPublicKey.getW().getAffineX()));
                map.set(KeyKeys.EC2_Y.AsCBOR(),stripLeadingZero(ecPublicKey.getW().getAffineY()));
                oneKey = new OneKey(map);
            }

            Sign1Message message = (Sign1Message) Sign1Message.DecodeFromBytes(cose);
            try {
                egcDecodeResult.setValidated(message.validate(oneKey));
            } catch (CoseException coseException) {
                egcDecodeResult.setErrorMessage("COSE Validation error: "
                        + (coseException.getCause() != null
                        ? coseException.getCause().getMessage() : coseException.getMessage()));
            }

            StringWriter stringWriter = new StringWriter();
            new CborDump().dumpCbor(message.GetContent(), stringWriter);
            egcDecodeResult.setCborDump(stringWriter.getBuffer().toString());
            egcDecodeResult.setCborHex(Hex.toHexString(message.GetContent()));

            CBORObject certData = CBORObject.DecodeFromBytes(message.GetContent());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            CBORObject.WriteJSON(certData, bos);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode cborJson = mapper.readTree(bos.toByteArray());
            egcDecodeResult.setCborJson(cborJson);

            CBORObject protectedHeader = message.getProtectedAttributes();
            egcDecodeResult.setCoseProtected(protectedHeader.toString());

            bos = new ByteArrayOutputStream();
            CBORObject.WriteJSON(protectedHeader, bos);
            JsonNode coseProtectedJson = mapper.readTree(bos.toByteArray());
            egcDecodeResult.setCoseProtectedJson(coseProtectedJson);
        } catch (CBORException cborException) {
            egcDecodeResult.setErrorMessage("CBOR decode exception: " + cborException.getMessage());
        } catch (Exception exception) {
            egcDecodeResult.setErrorMessage("Decode exception: " + exception.getMessage());
        }

        return ResponseEntity.ok(egcDecodeResult);
    }

    /**
     * Rest Controller to get Public Key Information.
     */
    @GetMapping(value = "publicKey")
    public ResponseEntity<PublicKeyInfo> getPublic() {
        PublicKeyInfo result = new PublicKeyInfo();
        result.setKid(certificateService.getKidAsBase64());
        result.setAlgid(certificateService.getAlgorithmIdentifier());
        result.setKeyType(certificateService.getCertficate().getPublicKey().getAlgorithm());
        result.setPublicKeyFormat(certificateService.getCertficate().getPublicKey().getFormat());
        result.setPublicKeyEncoded(
                Base64.getEncoder().encodeToString(certificateService.getCertficate().getPublicKey().getEncoded()));

        return ResponseEntity.ok(result);
    }

    private CBORObject getEcCurve(ECPublicKey publicKey) {
        CBORObject keyKeys;
        switch (publicKey.getParams().getOrder().bitLength()) {
            case 384:
                keyKeys = KeyKeys.EC2_P384;
                break;
            case 256:
                keyKeys = KeyKeys.EC2_P256;
                break;
            default:
                throw new IllegalArgumentException("unsupported EC curveSize");
        }
        return keyKeys;
    }

    private CBORObject stripLeadingZero(BigInteger input) {
        val bytes = input.toByteArray();
        byte[] stripped;

        if (bytes.length % 8 != 0 && bytes[0] == 0x00) {
            stripped = Arrays.copyOfRange(bytes, 1, bytes.length);
        } else {
            stripped = bytes;
        }
        return CBORObject.FromObject(stripped);
    }
}
