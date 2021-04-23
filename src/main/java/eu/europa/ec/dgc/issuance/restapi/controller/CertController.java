package eu.europa.ec.dgc.issuance.restapi.controller;

import ehn.techiop.hcert.kotlin.chain.CborProcessingChain;
import ehn.techiop.hcert.kotlin.chain.ResultCbor;
import ehn.techiop.hcert.kotlin.chain.VaccinationData;
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
    public ResponseEntity<Map<String,Object>> decodeEGCert(@RequestBody String prefixedEncodedCompressedCose) throws IOException {
        Map<String,Object> result = new HashMap<>();
        // TODO decodeEGC devel endpoint
        return ResponseEntity.ok(result);
    }
}
