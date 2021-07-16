package eu.europa.ec.dgc.issuance.service;

import COSE.ASN1;
import COSE.CoseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimResponse;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import eu.europa.ec.dgc.issuance.restapi.dto.EgdcCodeData;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.PublicKey;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
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

    @Autowired
    CertificateService certificateService;

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

        String dgciHash = sha256(dgciIdentifier.getDgci());
        DidDocument didDocument = dgciService.getDidDocument(dgciHash);
        assertNotNull(didDocument);
    }

    @Test
    void testCreateEdgcBackend() throws Exception {
        String vacDataJson = SampleData.vaccination;
        EgdcCodeData egdcCodeData = dgciService.createEdgc(vacDataJson);
        assertNotNull(egdcCodeData);
        assertNotNull(egdcCodeData.getQrCode());
        Optional<DgciEntity> dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertEquals(GreenCertificateType.Vaccination,dgciEnitiyOpt.get().getGreenCertificateType());
        assertNotNull(dgciEnitiyOpt.get().getCertHash());
        assertNotNull(dgciEnitiyOpt.get().getDgciHash());
        assertNotNull(dgciEnitiyOpt.get().getHashedTan());
        assertNotNull(dgciEnitiyOpt.get().getExpiresAt());

        EgcDecodeResult decodeResult = edgcValidator.decodeEdgc(egdcCodeData.getQrCode());
        assertTrue(decodeResult.isValidated());
        assertNull(decodeResult.getErrorMessage());
        JsonNode cborJson = decodeResult.getCborJson();
        assertNotNull(cborJson);
        assertEquals(issuanceConfigProperties.getCountryCode(),cborJson.get("1").asText());
        long createdAt = cborJson.get("1").asLong();
        long expiredAt = cborJson.get("4").asLong();
        JsonNode payload = cborJson.get("-260").get("1");
        assertNotNull(payload);
        assertTrue(payload.isObject());
    }

    @Test
    void testWalletClaim() throws Exception {
        String vacDataJson = SampleData.vaccination;
        EgdcCodeData egdcCodeData = dgciService.createEdgc(vacDataJson);
        assertNotNull(egdcCodeData);
        assertNotNull(egdcCodeData.getQrCode());
        Optional<DgciEntity> dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertFalse(dgciEnitiyOpt.get().isClaimed());
        String tanHash = dgciEnitiyOpt.get().getHashedTan();
        String certHash = dgciEnitiyOpt.get().getCertHash();
        assertEquals(GreenCertificateType.Vaccination,dgciEnitiyOpt.get().getGreenCertificateType());

        EgcDecodeResult decodeResult = edgcValidator.decodeEdgc(egdcCodeData.getQrCode());
        assertTrue(decodeResult.isValidated());
        assertNull(decodeResult.getErrorMessage());

        ClaimRequest claimRequest = generateClaimRequest(Hex.decode(decodeResult.getCoseHex()),
            egdcCodeData.getDgci(),tanHash, certHash,
            "RSA","SHA256WithRSA");
        ClaimResponse claimResponse = dgciService.claim(claimRequest);

        dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertTrue(dgciEnitiyOpt.get().isClaimed());

        // new claim
        String newTanHash = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(claimResponse.getTan().getBytes(StandardCharsets.UTF_8)));
        ClaimRequest newClaimRequest = generateClaimRequest(Hex.decode(decodeResult.getCoseHex()),
            egdcCodeData.getDgci(),newTanHash, certHash,
            "RSA","SHA256WithRSA");
        dgciService.claim(newClaimRequest);

        String dgciHash = sha256(egdcCodeData.getDgci());
        DidDocument didDocument = dgciService.getDidDocument(dgciHash);
        assertNotNull(didDocument);
        assertNotNull(didDocument.getAuthentication());
        assertFalse(didDocument.getAuthentication().isEmpty());
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(didDocument.getAuthentication().get(0).getPublicKeyJsw()));

    }

    private String sha256(String toHash) throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA256")
                .digest(toHash.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testWalletClaimEC() throws Exception {
        String vacDataJson = SampleData.vaccination;
        EgdcCodeData egdcCodeData = dgciService.createEdgc(vacDataJson);
        assertNotNull(egdcCodeData);
        assertNotNull(egdcCodeData.getQrCode());
        Optional<DgciEntity> dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertFalse(dgciEnitiyOpt.get().isClaimed());
        String tanHash = dgciEnitiyOpt.get().getHashedTan();
        String certHash = dgciEnitiyOpt.get().getCertHash();
        assertEquals(GreenCertificateType.Vaccination,dgciEnitiyOpt.get().getGreenCertificateType());

        EgcDecodeResult decodeResult = edgcValidator.decodeEdgc(egdcCodeData.getQrCode());
        assertTrue(decodeResult.isValidated());
        assertNull(decodeResult.getErrorMessage());

        ClaimRequest claimRequest = generateClaimRequest(Hex.decode(decodeResult.getCoseHex()),
            egdcCodeData.getDgci(),tanHash, certHash,
            "EC","SHA256withECDSA");
        dgciService.claim(claimRequest);

        dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertTrue(dgciEnitiyOpt.get().isClaimed());

        String dgciHash = sha256(egdcCodeData.getDgci());
        DidDocument didDocument = dgciService.getDidDocument(dgciHash);
        assertNotNull(didDocument);
        assertNotNull(didDocument.getAuthentication());
        assertFalse(didDocument.getAuthentication().isEmpty());
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(didDocument.getAuthentication().get(0).getPublicKeyJsw()));
    }



    private ClaimRequest generateClaimRequest(byte[] coseMessage, String dgci, String tanHash, String certHash64, String keyType, String sigAlg) throws Exception {
        ClaimRequest claimRequest = new ClaimRequest();
        claimRequest.setDgci(dgci);

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(keyType);
        keyPairGen.initialize("RSA".equals(keyType) ? 2048 : 256);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(dgci.getBytes());
        bos.write(Base64.getDecoder().decode(tanHash));
        bos.write(publicKeyBytes);

        claimRequest.setTanHash(tanHash);

        PublicKey publicKey = new PublicKey();
        publicKey.setValue(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        publicKey.setType(keyPair.getPublic().getAlgorithm());
        claimRequest.setPublicKey(publicKey);
        claimRequest.setSigAlg(sigAlg);
        byte[] certHash = dgciService.computeCoseSignHash(coseMessage);
        String recomputedCertHash64 = Base64.getEncoder().encodeToString(certHash);
        assertEquals(certHash64,recomputedCertHash64);
        claimRequest.setCertHash(recomputedCertHash64);
        createClaimSignature(claimRequest,keyPair.getPrivate(), sigAlg);

        return claimRequest;
    }

    private void createClaimSignature(ClaimRequest claimRequest, PrivateKey privateKey, String sigAlg) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        StringBuilder sigValue = new StringBuilder();
        sigValue.append(claimRequest.getTanHash())
            .append(claimRequest.getCertHash())
            .append(claimRequest.getPublicKey().getValue());
        Signature signature = Signature.getInstance(sigAlg);
        signature.initSign(privateKey);
        signature.update(sigValue.toString().getBytes(StandardCharsets.UTF_8));
        byte[] sigData = signature.sign();
        claimRequest.setSignature(Base64.getEncoder().encodeToString(sigData));
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

    @Test
    void signFromHash() throws Exception {
        String hash64 = "ZALr2hyVD4l5veh7+Auq78TQeS4PKOMAgVyy4GVSi9g=";
        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Vaccination);

        java.security.interfaces.ECPublicKey pubKey = (java.security.interfaces.ECPublicKey) certificateService.getPublicKey();
        AsymmetricKeyParameter keyParameter = ECUtil.generatePublicKeyParameter(pubKey);
        ECDSASigner ecdsaSigner = new ECDSASigner();
        ecdsaSigner.init(false, keyParameter);
        StandardDSAEncoding standardDSAEncoding = new StandardDSAEncoding();

        IssueData issueData = new IssueData();
        // Try more time to get all possible byte paddings options
        for (int i = 0;i<1000;i++) {
            DgciIdentifier dgciIdentifier = dgciService.initDgci(dgciInit);
            Random rnd = new Random();
            byte[] hash = new byte[32];
            rnd.nextBytes(hash);
            hash64 = Base64.getEncoder().encodeToString(hash);
            issueData.setHash(hash64);
            SignatureData signatureData = dgciService.finishDgci(dgciIdentifier.getId(), issueData);
            BigInteger[] sig = standardDSAEncoding.decode(ecdsaSigner.getOrder(), convertConcatToDer(Base64.getDecoder().decode(signatureData.getSignature())));
            assertTrue(ecdsaSigner.verifySignature(hash,sig[0],sig[1]));
        }
    }

    private static byte[] convertConcatToDer(byte[] concat) throws CoseException {
        int len = concat.length / 2;
        byte[] r = Arrays.copyOfRange(concat, 0, len);
        byte[] s = Arrays.copyOfRange(concat, len, concat.length);
        return ASN1.EncodeSignature(r, s);
    }

    @Test
    void checkDgciExists() throws Exception {
        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Vaccination);
        DgciIdentifier initResult = dgciService.initDgci(dgciInit);
        String dgciHash = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA256")
                .digest(initResult.getDgci().getBytes(StandardCharsets.UTF_8)));
        assertEquals(DgciService.DgciStatus.EXISTS,dgciService.checkDgciStatus(dgciHash));
        String dgciHashNotExsits = Base64.getEncoder().encodeToString(
            MessageDigest.getInstance("SHA256")
                .digest("not exists".getBytes(StandardCharsets.UTF_8)));
        assertEquals(DgciService.DgciStatus.NOT_EXISTS,dgciService.checkDgciStatus(dgciHashNotExsits));
    }



}
