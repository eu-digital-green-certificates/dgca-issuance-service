package eu.europa.ec.dgc.issuance.restapi.controller;

import COSE.CoseException;
import COSE.Message;
import COSE.Sign1Message;
import ehn.techiop.hcert.kotlin.chain.Base45Service;
import ehn.techiop.hcert.kotlin.chain.CborProcessingChain;
import ehn.techiop.hcert.kotlin.chain.CompressorService;
import ehn.techiop.hcert.kotlin.chain.ContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.ResultCbor;
import ehn.techiop.hcert.kotlin.chain.VaccinationData;
import ehn.techiop.hcert.kotlin.chain.VerificationResult;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultBase45Service;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCborService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCompressorService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCoseService;
import eu.europa.ec.dgc.issuance.service.CertificateService;
import eu.europa.ec.dgc.issuance.service.EhdCryptoService;
import eu.europa.ec.dgc.issuance.utils.CBORDump;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cert")
@AllArgsConstructor
public class CertController {
    private final CertificateService certificateService;
    private final EhdCryptoService ehdCryptoService;

    /**
     * create Vaccination Certificate.
     * @param vaccinationData vaccinationData
     * @return result
     */
    @PostMapping(value = "create",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResultCbor> createVaccinationCertificate(@RequestBody VaccinationData vaccinationData) {
        // Taken from https://github.com/ehn-digital-green-development/hcert-kotlin/blob/main/src/test/kotlin/ehn/techiop/hcert/kotlin/chain/CborProcessingChainTest.kt
        val coseService = new DefaultCoseService(ehdCryptoService);
        val contextIdentifierService = new DefaultContextIdentifierService();
        val compressorService = new DefaultCompressorService();
        val base45Service = new DefaultBase45Service();
        val cborService = new DefaultCborService();
        CborProcessingChain cborProcessingChain =
                new CborProcessingChain(cborService, coseService,
                        contextIdentifierService, compressorService, base45Service);
        ResultCbor resultCbor = cborProcessingChain.process(vaccinationData);
        return ResponseEntity.ok(resultCbor);
    }

    @PostMapping(value="dumpCBOR",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> decodeCBOR(@RequestBody String cbor) throws IOException {
        StringWriter stringWriter = new StringWriter();
        new CBORDump().dumpCBOR(Base64.getDecoder().decode(cbor),stringWriter);
        return ResponseEntity.ok(stringWriter.getBuffer().toString());
    }

    @PostMapping(value="decodeEGC",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> decodeEGCert(@RequestBody String prefixedEncodedCompressedCose) throws IOException, CoseException {
        Map<String,Object> result = new HashMap<>();
        VerificationResult verificationResult = new VerificationResult();
        ContextIdentifierService contextIdentifierService = new DefaultContextIdentifierService();
        Base45Service base45Service = new DefaultBase45Service();
        CompressorService compressorService = new DefaultCompressorService();
        val plainInput = contextIdentifierService.decode(prefixedEncodedCompressedCose, verificationResult);
        val compressedCose = base45Service.decode(plainInput, verificationResult);
        val cose = compressorService.decode(compressedCose, verificationResult);
        Message message = Sign1Message.DecodeFromBytes(cose);

        StringWriter stringWriter = new StringWriter();
        new CBORDump().dumpCBOR(message.GetContent(),stringWriter);
        result.put("cborDump",stringWriter.getBuffer().toString());
        result.put("cborBytes",Base64.getEncoder().encodeToString(message.GetContent()));
        result.put("coseBase64",Base64.getEncoder().encodeToString(cose));
        result.put("coseHEX", Hex.toHexString(cose));

        return ResponseEntity.ok(result);
    }
}
