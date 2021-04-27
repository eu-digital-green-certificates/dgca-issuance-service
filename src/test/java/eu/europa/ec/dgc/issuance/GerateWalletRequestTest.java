package eu.europa.ec.dgc.issuance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.PublicKey;
import java.io.ByteArrayOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import org.junit.Test;

public class GerateWalletRequestTest {
    @Test
    public void testGerateWalletRequest() throws Exception {
        String dgci = "did:web:authority:dgci:V1:DE:blabla:78d5a4d3-1409-4d6c-8aac-e7781b8cebe2";
        String tan = "844b31d1-5432-4589-96b3-788b2513c578";
        String certHash = "hash";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] tanHash = digest.digest(tan.getBytes());

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(dgci.getBytes());
        bos.write(tan.getBytes());
        bos.write(publicKeyBytes);

        Signature signature = Signature.getInstance("SHA256WithRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(bos.toByteArray());
        byte[] sigData = signature.sign();

        ClaimRequest claimRequest = new ClaimRequest();
        claimRequest.setDgci(dgci);
        claimRequest.setTanHash(tan);
        PublicKey publicKeyDTO = new PublicKey();
        publicKeyDTO.setType(keyPair.getPublic().getAlgorithm());
        publicKeyDTO.setValue(Base64.getEncoder().encodeToString(publicKeyBytes));
        claimRequest.setPublicKey(publicKeyDTO);
        claimRequest.setSignature(Base64.getEncoder().encodeToString(sigData));
        claimRequest.setCertHash(certHash);

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(claimRequest));
    }
}
