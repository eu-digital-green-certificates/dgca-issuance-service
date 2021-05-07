package eu.europa.ec.dgc.issuance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import org.junit.jupiter.api.Test;

class JwkCoreTest {
    @Test
    void testRSAgeneration() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();

        RSAKey jwkKey = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic()).build();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String jwkString = jwkKey.toJSONString();
        JsonNode jwkElem = objectMapper.readTree(jwkString);
        System.out.println(jwkString);
        System.out.println(objectMapper.writeValueAsString(jwkElem));
    }

    @Test
    void testECgeneration() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
        keyPairGen.initialize(256);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
        Curve curve = Curve.forECParameterSpec(ecPublicKey.getParams());

        ECKey jwkKey = new ECKey.Builder(curve,(ECPublicKey) keyPair.getPublic()).build();
        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        String jwkString = jwkKey.toJSONString();
        JsonNode jwkElem = objectMapper.readTree(jwkString);
        System.out.println(objectMapper.writeValueAsString(jwkElem));
    }
}
