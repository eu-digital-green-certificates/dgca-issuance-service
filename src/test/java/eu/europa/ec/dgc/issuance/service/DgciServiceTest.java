package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.data.Eudgc;
import ehn.techiop.hcert.kotlin.chain.SampleData;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import eu.europa.ec.dgc.issuance.restapi.dto.EgdcCodeData;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.PublicKey;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.codec.Hex;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class DgciServiceTest {
    @Autowired
    DgciService dgciService;

    @Autowired
    IssuanceConfigProperties issuanceConfigProperties;

    @Autowired
    DgciRepository dgciRepository;

    @Autowired
    EdgcValidator edgcValidator;

    @Test
    void testDGCIInit() throws Exception {
        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Vaccination);
        DgciIdentifier dgciIdentifier = dgciService.initDgci(dgciInit);
        assertNotNull(dgciIdentifier.getDgci());
        assertTrue(dgciIdentifier.getDgci().startsWith(issuanceConfigProperties.getDgciPrefix()));
    }

    @Test
    void testDGCISign() throws Exception {
        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Vaccination);
        DgciIdentifier dgciIdentifier = dgciService.initDgci(dgciInit);
        assertNotNull(dgciIdentifier.getDgci());
        assertTrue(dgciIdentifier.getDgci().startsWith(issuanceConfigProperties.getDgciPrefix()));

        IssueData issueData = new IssueData();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        issueData.setHash(Base64.getEncoder().encodeToString(digest.digest("test".getBytes())));

        SignatureData signatureData = dgciService.finishDgci(dgciIdentifier.getId(), issueData);
        assertNotNull(signatureData.getSignature());
        assertNotNull(signatureData.getTan());
        assertEquals(8,signatureData.getTan().length());
    }

    @Test
    void testCreateEdgcBackend() throws Exception {
        String vacDataJson = SampleData.Companion.getVaccination();
        ObjectMapper objectMapper = new ObjectMapper();
        Eudgc eudgc = objectMapper.readValue(vacDataJson,Eudgc.class);
        EgdcCodeData egdcCodeData = dgciService.createEdgc(eudgc);
        assertNotNull(egdcCodeData);
        assertNotNull(egdcCodeData.getQrcCode());
        Optional<DgciEntity> dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertEquals(GreenCertificateType.Vaccination,dgciEnitiyOpt.get().getGreenCertificateType());

        EgcDecodeResult decodeResult = edgcValidator.decodeEdgc(egdcCodeData.getQrcCode());
        assertTrue(decodeResult.isValidated());
        assertNull(decodeResult.getErrorMessage());
    }

    @Test
    void testWalletClaim() throws Exception {
        String vacDataJson = SampleData.Companion.getVaccination();
        ObjectMapper objectMapper = new ObjectMapper();
        Eudgc eudgc = objectMapper.readValue(vacDataJson,Eudgc.class);
        EgdcCodeData egdcCodeData = dgciService.createEdgc(eudgc);
        assertNotNull(egdcCodeData);
        assertNotNull(egdcCodeData.getQrcCode());
        Optional<DgciEntity> dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertFalse(dgciEnitiyOpt.get().isClaimed());
        String tanHash = dgciEnitiyOpt.get().getHashedTan();
        String certHash = dgciEnitiyOpt.get().getCertHash();
        assertEquals(GreenCertificateType.Vaccination,dgciEnitiyOpt.get().getGreenCertificateType());

        EgcDecodeResult decodeResult = edgcValidator.decodeEdgc(egdcCodeData.getQrcCode());
        assertTrue(decodeResult.isValidated());
        assertNull(decodeResult.getErrorMessage());

        ClaimRequest claimRequest = generateClaimRequest(Hex.decode(decodeResult.getCoseHex()),egdcCodeData.getDgci(),tanHash, certHash);
        dgciService.claim(claimRequest);

        dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertTrue(dgciEnitiyOpt.get().isClaimed());
    }

    private ClaimRequest generateClaimRequest(byte[] coseMessage, String dgci, String tanHash, String certHash64) throws Exception {
        ClaimRequest claimRequest = new ClaimRequest();
        claimRequest.setDgci(dgci);

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(dgci.getBytes());
        bos.write(Base64.getDecoder().decode(tanHash));
        bos.write(publicKeyBytes);

        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(bos.toByteArray());
        byte[] sigData = signature.sign();

        claimRequest.setTanHash(tanHash);
        claimRequest.setSignature(Base64.getEncoder().encodeToString(sigData));

        byte[] certHash = dgciService.computeCoseSignHash(coseMessage);
        String recomputedCertHash64 = Base64.getEncoder().encodeToString(certHash);
        assertEquals(certHash64,recomputedCertHash64);
        claimRequest.setCertHash(recomputedCertHash64);

        PublicKey publicKey = new PublicKey();
        publicKey.setValue(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        publicKey.setType(keyPair.getPublic().getAlgorithm());
        claimRequest.setPublicKey(publicKey);
        claimRequest.setSigAlg("SHA256WithRSA");

        return claimRequest;
    }

    @Test
    void computeSignHash() throws Exception {
        String certHash64 = "myj52o5mEvsZ4frw5dAFrXtHVAmz4HsaypQTTObwyOE=";
        String edgc = "HC1:6BFOXN%TSMAHN-H/P8JU6+BS.5E9%U6B2B2JJ59/V2O+G5B9:V9U21+T9*GE5VC9:BPCNJINQ+MN/Q19QE8QEA7IB65C94JBTKFVCAWKD+.COKEH-BTKFI8A RDV7L9JAO/B0PCSC9FDA6LF3E9:OC-69T7AZI9$JAQJKO-KX2M484$X4HZ6-G9+E93ZM$96PZ6+Q6X46+E5+DP:Q67ZMC%6QW6Z467PPDFPVX1R270:6NEQ0R6AOM*PP:+P*.1D9R$P6*C0 L6MXPDXI25P6QS25P9ZI4Q5%H0KDKM3L--8YE9/MV*4J3ZCK25IV0V.4 1D4S8PAB*BWA4AC1NY:88UOYBC81H2PDKTLU.F7853JDABS%FWB6IBO3P%2I7N9$7-RMN%T6HM13D1AT31ET$7*Q9H7T9ZQ.05T*6YF75AA/S1H3C4CD";
        EgcDecodeResult edgcResult = edgcValidator.decodeEdgc(edgc);
        assertTrue(edgcResult.isValidated());
        byte[] cose = Hex.decode(edgcResult.getCoseHex());
        byte[] certHash = Base64.getDecoder().decode(certHash64);
        byte[] certHashComputed = dgciService.computeCoseSignHash(cose);
        assertArrayEquals(certHash,certHashComputed);


    }



}