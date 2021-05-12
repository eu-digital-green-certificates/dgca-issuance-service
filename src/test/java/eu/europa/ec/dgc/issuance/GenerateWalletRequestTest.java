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

package eu.europa.ec.dgc.issuance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.PublicKey;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import org.junit.Test;

public class GenerateWalletRequestTest {
    // This can be used to generate valid json structure for claim
    @Test
    public void testGenerateWalletRequest() throws Exception {
        // Please adapt this to your certificate (the values can be get from browser network log
        // see POST /dgci
        // and PUT /dgci/{id}
        String dgci = "dgci:V1:DE:2e974b3b-d932-4bc9-bbae-d387f93f8bf3:edbcb873196f24be";
        String certHash = "mfg0MI7wPFexNkOa4n9OKojrzhe9a9lcim4JzJO3WtY=";
        String tan = "U7ULCYZY";
        String tanHash = "avmGz38ugM7uBePwKKlvh3IB8+7O+WFhQEbjIxhTxgY=";

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        String sigAlg = "SHA256WithRSA";

        ClaimRequest claimRequest = new ClaimRequest();
        claimRequest.setDgci(dgci);
        claimRequest.setTanHash(tanHash);
        PublicKey publicKeyDTO = new PublicKey();
        publicKeyDTO.setType(keyPair.getPublic().getAlgorithm());
        publicKeyDTO.setValue(Base64.getEncoder().encodeToString(publicKeyBytes));
        claimRequest.setPublicKey(publicKeyDTO);

        claimRequest.setSigAlg(sigAlg);
        claimRequest.setCertHash(certHash);
        createClaimSignature(claimRequest, keyPair.getPrivate(), sigAlg);

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(claimRequest));
    }

    public static void main(String[] args) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest("U7ULCYZY".getBytes(StandardCharsets.UTF_8));
            System.out.println(Base64.getEncoder().encodeToString(hashBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
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
    public void testGenerateWalletRequestEC() throws Exception {
        // Please adapt this to your certificate (the values can be get from browser network log
        // see POST /dgci
        // and PUT /dgci/{id}
        String dgci = "dgci:V1:DE:2e974b3b-d932-4bc9-bbae-d387f93f8bf3:edbcb873196f24be";
        String certHash = "mfg0MI7wPFexNkOa4n9OKojrzhe9a9lcim4JzJO3WtY=";
        String tan = "U7ULCYZY";
        String tanHash = "avmGz38ugM7uBePwKKlvh3IB8+7O+WFhQEbjIxhTxgY=";

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("EC");
        keyPairGen.initialize(256);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        String sigAlg = "SHA256withECDSA";

        ClaimRequest claimRequest = new ClaimRequest();
        claimRequest.setDgci(dgci);
        claimRequest.setTanHash(tanHash);
        PublicKey publicKeyDTO = new PublicKey();
        publicKeyDTO.setType(keyPair.getPublic().getAlgorithm());
        publicKeyDTO.setValue(Base64.getEncoder().encodeToString(publicKeyBytes));
        claimRequest.setPublicKey(publicKeyDTO);

        claimRequest.setSigAlg(sigAlg);
        claimRequest.setCertHash(certHash);
        createClaimSignature(claimRequest,keyPair.getPrivate(),sigAlg);

        ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(claimRequest));
    }

}
