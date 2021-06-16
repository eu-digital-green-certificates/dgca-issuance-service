package eu.europa.ec.dgc.issuance.service;

import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import ehn.techiop.hcert.kotlin.chain.Base45Service;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultBase45Service;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The test shows how produce edgc from json the same way the frontend is doing
 * using CBOR primitives
 */
@SpringBootTest
class DGCGenTest {
    @Autowired
    DgciService dgciService;

    @Autowired
    EdgcValidator edgcValidator;

    @Autowired
    CertificateService certificateService;

    @Test
    void genEDGC() throws IOException {
        String edgcJson = "{\"ver\":\"1.0.0\",\"nam\":{\"fn\":\"Garcia\",\"fnt\":\"GARCIA\"," +
            "\"gn\":\"Francisco\",\"gnt\":\"FRANCISCO\"},\"dob\":\"1991-01-01\",\"v\":[{\"tg\":\"840539006\"," +
            "\"vp\":\"1119305005\",\"mp\":\"EU/1/20/1507\",\"ma\":\"ORG-100001699\",\"dn\":1,\"sd\":2,\"dt\":" +
            "\"2021-05-14\",\"co\":\"CY\",\"is\":\"Neha\",\"ci\":\"dgci:V1:CY:HIP4OKCIS8CXKQMJSSTOJXAMP:03\"}]}";
        String countryCode = "DE";
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime expiration = now.plus(Duration.of(365, ChronoUnit.DAYS));
        long issuedAt = now.toInstant().getEpochSecond();
        long expirationSec = expiration.toInstant().getEpochSecond();
        byte[] keyId = "key".getBytes(StandardCharsets.UTF_8);
        int algId = -7;

        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Test);
        DgciIdentifier certData = dgciService.initDgci(dgciInit);

        byte[] dgcCbor = genDGCCbor(edgcJson, countryCode, issuedAt, certData.getExpired());
        byte[] coseBytes = genCoseUnsigned(dgcCbor, Base64.getDecoder().decode(certData.getKid())
            ,certData.getAlgId());
        byte[] hash = dgciService.computeCoseSignHash(coseBytes);
        IssueData issueData = new IssueData();
        issueData.setHash(Base64.getEncoder().encodeToString(hash));
        SignatureData sign = dgciService.finishDgci(certData.getId(), issueData);
        byte[] coseSigned = genSetSignature(coseBytes,Base64.getDecoder().decode(sign.getSignature()));
        String edgcQR = coseToQRCode(coseSigned);

        EgcDecodeResult validationResult = edgcValidator.decodeEdgc(edgcQR);
        assertTrue(validationResult.isValidated());
        assertNull(validationResult.getErrorMessage());
    }

    private byte[] genDGCCbor(String edgcJson, String countryCode, long issuedAt, long expirationSec) {
        CBORObject map = CBORObject.NewMap();
        map.set(CBORObject.FromObject(1),CBORObject.FromObject(countryCode));
        map.set(CBORObject.FromObject(6),CBORObject.FromObject(issuedAt));
        map.set(CBORObject.FromObject(4),CBORObject.FromObject(expirationSec));
        CBORObject hcertVersion = CBORObject.NewMap();
        CBORObject hcert = CBORObject.FromJSONString(edgcJson);
        hcertVersion.set(CBORObject.FromObject(1),hcert);
        map.set(CBORObject.FromObject(-260),hcertVersion);
        return map.EncodeToBytes();
    }

    private byte[] genCoseUnsigned(byte[] payload,byte[] keyId,int algId) {
        CBORObject protectedHeader = CBORObject.NewMap();
        protectedHeader.set(CBORObject.FromObject(1),CBORObject.FromObject(algId));
        protectedHeader.set(CBORObject.FromObject(4),CBORObject.FromObject(keyId));
        byte[] protectedHeaderBytes = protectedHeader.EncodeToBytes();

        CBORObject coseObject = CBORObject.NewArray();
        coseObject.Add(protectedHeaderBytes);
        coseObject.Add(CBORObject.NewMap());
        coseObject.Add(CBORObject.FromObject(payload));
        byte[] sigDummy = new byte[0];
        coseObject.Add(CBORObject.FromObject(sigDummy));
        return CBORObject.FromObjectAndTag(coseObject,18).EncodeToBytes();
    }

    private byte[] genSetSignature(byte[] coseData,byte[] signature) {
        CBORObject cborObject = CBORObject.DecodeFromBytes(coseData);
        if (cborObject.getType() == CBORType.Array && cborObject.getValues().size()==4) {
            cborObject.set(3,CBORObject.FromObject(signature));
        } else {
            throw new IllegalArgumentException("seems not to be cose");
        }
        return cborObject.EncodeToBytes();
    }

    private String coseToQRCode(byte[] cose) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(cose);
        DeflaterInputStream compessedInput = new DeflaterInputStream(bis, new Deflater(9));
        byte[] coseCompressed = compessedInput.readAllBytes();
        Base45Service base45Service = new DefaultBase45Service();
        String coded = base45Service.encode(coseCompressed);
        return "HC1:"+coded;
    }
}
