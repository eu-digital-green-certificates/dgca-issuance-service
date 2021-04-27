package eu.europa.ec.dgc.issuance.service;

import COSE.AlgorithmID;
import COSE.CoseException;
import COSE.HeaderKeys;
import COSE.OneKey;
import com.upokecenter.cbor.CBORObject;
import ehn.techiop.hcert.kotlin.chain.CryptoService;
import ehn.techiop.hcert.kotlin.chain.VerificationResult;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Adapter for EHDC kotlin code.
 */
@Component
public class EhdCryptoService implements CryptoService {
    private final X509Certificate cert;
    private final byte[] kid;
    private final List<Pair<HeaderKeys, CBORObject>> headers;
    private final PrivateKey privateKey;

    /**
     * the constructor.
     * @param certificateService certificateService
     */
    public EhdCryptoService(CertificateService certificateService) {
        this.cert = certificateService.getCertficate();
        this.privateKey = certificateService.getPrivateKey();
        kid = certificateService.getKid();
        headers = Arrays.asList(new Pair<>(HeaderKeys.Algorithm, AlgorithmID.RSA_PSS_256.AsCBOR()),
                new Pair<>(HeaderKeys.KID, CBORObject.FromObject(kid)));
    }

    @Override
    public List<Pair<HeaderKeys, CBORObject>> getCborHeaders() {
        return headers;
    }

    @Override
    public COSE.OneKey getCborSigningKey() {
        try {
            return new OneKey(cert.getPublicKey(), privateKey);
        } catch (CoseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public COSE.OneKey getCborVerificationKey(byte[] bytes, VerificationResult verificationResult) {
        if (Arrays.compare(this.kid, kid) == 0) {
            try {
                return new OneKey(cert.getPublicKey(), privateKey);
            } catch (CoseException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("unknown kid");
        }
    }

    @Override
    public X509Certificate getCertificate() {
        if (Arrays.compare(this.kid, kid) == 0) {
            return cert;
        } else {
            throw new IllegalArgumentException("unknown kid");
        }
    }

    @NotNull
    @Override
    public String exportCertificateAsPem() {
        return null;
    }

    @NotNull
    @Override
    public String exportPrivateKeyAsPem() {
        return null;
    }

}
