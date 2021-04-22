package eu.europa.ec.dgc.issuance.restapi.controller;

import COSE.AlgorithmID;
import COSE.CoseException;
import COSE.HeaderKeys;
import COSE.OneKey;
import com.upokecenter.cbor.CBORObject;
import ehn.techiop.hcert.kotlin.chain.CryptoService;
import ehn.techiop.hcert.kotlin.chain.impl.PkiUtils;
import eu.europa.ec.dgc.issuance.service.CertificateService;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import kotlin.Pair;

/**
 * Adapter for EHDC kotlin code.
 */
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
        kid = new PkiUtils().calcKid(cert);
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
    public COSE.OneKey getCborVerificationKey(byte[] bytes) {
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
    public Certificate getCertificate(byte[] kid) {
        if (Arrays.compare(this.kid, kid) == 0) {
            return cert;
        } else {
            throw new IllegalArgumentException("unknown kid");
        }
    }
}
