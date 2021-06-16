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
import com.upokecenter.cbor.CBORObject;
import ehn.techiop.hcert.kotlin.chain.CryptoService;
import ehn.techiop.hcert.kotlin.chain.VerificationResult;
import ehn.techiop.hcert.kotlin.crypto.CertificateAdapter;
import ehn.techiop.hcert.kotlin.crypto.CoseHeaderKeys;
import ehn.techiop.hcert.kotlin.crypto.JvmPrivKey;
import ehn.techiop.hcert.kotlin.crypto.JvmPubKey;
import ehn.techiop.hcert.kotlin.crypto.PrivKey;
import ehn.techiop.hcert.kotlin.crypto.PubKey;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Arrays;
import java.util.List;
import kotlin.Pair;
import org.springframework.stereotype.Component;

@Component
public class EhdCryptoService implements CryptoService {
    private final X509Certificate cert;
    private final byte[] kid;
    private final List<Pair<CoseHeaderKeys, Object>> headers;
    private final PrivateKey privateKey;

    /**
     * the constructor.
     *
     * @param certificateService certificateService
     */
    public EhdCryptoService(CertificateService certificateService) {
        this.cert = certificateService.getCertficate();
        this.privateKey = certificateService.getPrivateKey();
        kid = certificateService.getKid();
        if (this.privateKey instanceof RSAPrivateCrtKey) {
            headers = Arrays.asList(new Pair<>(CoseHeaderKeys.ALGORITHM, AlgorithmID.RSA_PSS_256.AsCBOR()),
                new Pair<>(CoseHeaderKeys.KID, CBORObject.FromObject(kid)));
        } else {
            headers = Arrays.asList(new Pair<>(CoseHeaderKeys.ALGORITHM, AlgorithmID.ECDSA_256.AsCBOR()),
                new Pair<>(CoseHeaderKeys.KID, CBORObject.FromObject(kid)));
        }
    }


    @Override
    public List<Pair<CoseHeaderKeys, Object>> getCborHeaders() {
        return headers;
    }

    @Override
    public PrivKey getCborSigningKey() {
        return new JvmPrivKey(privateKey);
    }

    @Override
    public PubKey getCborVerificationKey(byte[] bytes, VerificationResult verificationResult) {
        if (Arrays.compare(this.kid, kid) == 0) {
            return new JvmPubKey(cert.getPublicKey());
        } else {
            throw new IllegalArgumentException("unknown kid");
        }
    }

    @Override
    public CertificateAdapter getCertificate() {
        return new CertificateAdapter(cert);
    }

    @Override
    public String exportCertificateAsPem() {
        return null;
    }

    @Override
    public String exportPrivateKeyAsPem() {
        return null;
    }

}
