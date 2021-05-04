package eu.europa.ec.dgc.issuance.restapi.controller;

import eu.europa.ec.dgc.issuance.restapi.dto.PublicKeyInfo;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class CertControllerTest {
    @Autowired
    CertController certController;

    @Test
    public void testPublicKey() throws Exception {
        ResponseEntity<PublicKeyInfo> publicKeyResponse = certController.getPublic();
        assertEquals(HttpStatus.OK,publicKeyResponse.getStatusCode());
        publicKeyResponse.getBody();
        PublicKeyInfo publicKey = publicKeyResponse.getBody();

        byte[] keyBytes = Base64.getDecoder().decode(publicKey.getPublicKeyEncoded());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance(publicKey.getKeyType());
        PublicKey publicKeySec = kf.generatePublic(spec);
        assertNotNull(publicKeySec);
    }
}
