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

package eu.europa.ec.dgc.issuance.service;

import COSE.AlgorithmID;
import eu.europa.ec.dgc.utils.CertificateUtils;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CertificateService {
    private final CertificatePrivateKeyProvider certificatePrivateKeyProvider;
    private final SigningService signingService;
    private byte[] kid;

    @Autowired
    public CertificateService(@Qualifier("issuerKeyProvider") CertificatePrivateKeyProvider
                                      certificatePrivateKeyProvider, SigningService signingService) {
        this.certificatePrivateKeyProvider = certificatePrivateKeyProvider;
        this.signingService = signingService;
    }

    /**
     * compute kid.
     * key identifier needed for cose
     */
    @PostConstruct
    public void computeKid() {
        CertificateUtils certificateUtils = new CertificateUtils();
        String kidBase64 = certificateUtils.getCertKid(getCertficate());
        kid = Base64.getDecoder().decode(kidBase64);
    }

    public byte[] getKid() {
        return kid;
    }

    public String getKidAsBase64() {
        return Base64.getEncoder().encodeToString(kid);
    }

    public X509Certificate getCertficate() {
        return (X509Certificate) certificatePrivateKeyProvider.getCertificate();
    }

    public PublicKey getPublicKey() {
        return certificatePrivateKeyProvider.getCertificate().getPublicKey();
    }

    public PrivateKey getPrivateKey() {
        return certificatePrivateKeyProvider.getPrivateKey();
    }

    /**
     * sign hash.
     *
     * @param base64Hash base64Hash
     * @return signature as base64
     */
    public String signHash(String base64Hash) {
        byte[] hashBytes = Base64.getDecoder().decode(base64Hash);
        byte[] signature = signingService.signHash(hashBytes, certificatePrivateKeyProvider.getPrivateKey());
        return Base64.getEncoder().encodeToString(signature);
    }

    public byte[] publicKey() {
        return certificatePrivateKeyProvider.getCertificate().getPublicKey().getEncoded();
    }

    /**
     * Method to get the Algorithm Identifier of Public Key.
     *
     * @return CBOR AlgorithmID As Integer
     */
    public int getAlgorithmIdentifier() {
        PublicKey publicKey = certificatePrivateKeyProvider.getCertificate().getPublicKey();
        if (publicKey instanceof RSAPublicKey) {
            return AlgorithmID.RSA_PSS_256.AsCBOR().AsInt32();
        } else if (publicKey instanceof ECPublicKey) {
            return AlgorithmID.ECDSA_256.AsCBOR().AsInt32();
        } else {
            throw new IllegalArgumentException("unsupported key type");
        }
    }
}
