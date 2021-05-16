package eu.europa.ec.dgc.issuance.service.impl;

import eu.europa.ec.dgc.issuance.service.CertificatePrivateKeyProvider;
import eu.europa.ec.dgc.issuance.utils.btp.CredentialStore;
import eu.europa.ec.dgc.issuance.utils.btp.SapCredential;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract class with interfaces to the SAP BTP {@link CredentialStore}. It provides methods to get certificates
 * as well as private keys from the credential store. Implementations of {@link CertificatePrivateKeyProvider}
 * inheriting from this abstract class do not have to implement a connection to the credential store themselves.</p>
 * <p><b>Note: </b>Keys in the credential store are supposed to be in X.509 or RSA format and base64 encoded. Raw keys
 * will be stripped off line breaks and <code>-----BEGIN / END KEY-----</code> phrases.</p>
 */
public abstract class BtpAbstractKeyProvider implements CertificatePrivateKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(BtpAbstractKeyProvider.class);

    private static final List<String> ALLOWED_ALGORITHMS = Arrays.asList("EC", "RSA");

    protected final CredentialStore credentialStore;

    public BtpAbstractKeyProvider(CredentialStore credentialStore) {
        this.credentialStore = credentialStore;
    }

    protected Certificate getCertificateFromStore(String certName) {
        SapCredential cert = credentialStore.getKeyByName(certName);
        String certContent = cleanKeyString(cert.getValue());

        try {
            byte[] certDecoded = Base64.getDecoder().decode(certContent);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return certFactory.generateCertificate(new ByteArrayInputStream(certDecoded));
        } catch (CertificateException e) {
            log.error("Error building certificate: {}.", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    protected PrivateKey getPrivateKeyFromStore(String keyName) {
        SapCredential key = credentialStore.getKeyByName(keyName);
        if (!ALLOWED_ALGORITHMS.contains(key.getFormat())) {
            throw new IllegalArgumentException("Key Format not supported: " + key.getFormat());
        }

        try {
            KeyFactory kf = KeyFactory.getInstance(key.getFormat());
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder()
                .decode(cleanKeyString(key.getValue())));
            return kf.generatePrivate(pkcs8EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Error building private key: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String cleanKeyString(String rawKey) {
        return rawKey.replaceAll("\\n", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "");
    }
}
