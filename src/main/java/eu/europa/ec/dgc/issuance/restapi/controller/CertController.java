package eu.europa.ec.dgc.issuance.restapi.controller;

import COSE.CoseException;
import COSE.KeyKeys;
import COSE.OneKey;
import COSE.Sign1Message;
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
import eu.europa.ec.dgc.issuance.service.CertificateService;
import eu.europa.ec.dgc.issuance.service.EhdCryptoService;
import eu.europa.ec.dgc.issuance.utils.CBORDump;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;
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
public class CertController {
    private final CertificateService certificateService;
    private final EhdCryptoService ehdCryptoService;

    /**
     * create Vaccination Certificate.
     * @param eudgc eudgc
     * @return result
     */
    @PostMapping(value = "create",consumes = MediaType.APPLICATION_JSON_VALUE)
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
        Sign1Message message = (Sign1Message) Sign1Message.DecodeFromBytes(cose);

        CBORObject map = CBORObject.NewMap();
        // TODO dev decode EGC, add support for EC validation
        RSAPublicKey rsaPublicKey = (RSAPublicKey) certificateService.getCertficate().getPublicKey();
        map.set(KeyKeys.KeyType.AsCBOR(),KeyKeys.KeyType_RSA);
        map.set(KeyKeys.RSA_N.AsCBOR(),stripLeadingZero(rsaPublicKey.getModulus()));
        map.set(KeyKeys.RSA_E.AsCBOR(),stripLeadingZero(rsaPublicKey.getPublicExponent()));
        OneKey oneKey = new OneKey(map);
        result.put("validated",message.validate(oneKey));

        StringWriter stringWriter = new StringWriter();
        new CBORDump().dumpCBOR(message.GetContent(),stringWriter);
        result.put("cborDump",stringWriter.getBuffer().toString());
        result.put("cborBytes",Base64.getEncoder().encodeToString(message.GetContent()));
        result.put("coseBase64",Base64.getEncoder().encodeToString(cose));
        result.put("coseHEX", Hex.toHexString(cose));
        result.put("coseProtected",message.getProtectedAttributes().toString());
        return ResponseEntity.ok(result);
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

    @GetMapping(value="publicKey")
    public ResponseEntity<Map<String,Object>> getPublic() throws Exception {
        Map<String,Object> result = new HashMap<>();
        result.put("kid",certificateService.getKidAsBase64());
        result.put("algid",certificateService.getAlgId());
        result.put("keyType",certificateService.getCertficate().getPublicKey().getAlgorithm());
        result.put("publicKeyFormat",certificateService.getCertficate().getPublicKey().getFormat());
        result.put("publicKeyEncoded",Base64.getEncoder().encodeToString(certificateService.getCertficate().getPublicKey().getEncoded()));

        return ResponseEntity.ok(result);
    }
}
