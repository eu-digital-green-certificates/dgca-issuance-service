package eu.europa.ec.dgc.issuance.service;

import ehn.techiop.hcert.kotlin.chain.impl.PkiUtils;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Base64;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CertificateService {
    private final IssuanceConfigProperties issuanceConfigProperties;

    private KeyStore certKeyStore;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private java.security.cert.Certificate cert;
    private byte[] kid;

    // TODO refactor cert keystore to be configurable

    /**
     * load key store.
     *
     * @throws KeyStoreException           exception
     * @throws IOException                 exception
     * @throws CertificateException        exception
     * @throws NoSuchAlgorithmException    exception
     * @throws UnrecoverableEntryException exception
     */
    @PostConstruct
    public void loadKeyStore() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        final char[] keyStorePassword = issuanceConfigProperties.getKeyStorePassword().toCharArray();
        final String keyName = issuanceConfigProperties.getCertAlias();

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        KeyStore keyStore = KeyStore.getInstance("JKS");

        try (InputStream is = new FileInputStream(issuanceConfigProperties.getKeyStoreFile())) {
            final char[] privateKeyPassword = issuanceConfigProperties.getPrivateKeyPassword().toCharArray();
            keyStore.load(is, privateKeyPassword);
            KeyStore.PasswordProtection keyPassword =
                    new KeyStore.PasswordProtection(keyStorePassword);

            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyName, keyPassword);
            cert = keyStore.getCertificate(keyName);
            publicKey = cert.getPublicKey();
            privateKey = privateKeyEntry.getPrivateKey();
            kid = new PkiUtils().calcKid((X509Certificate) cert);
        }
    }

    public byte[] getKid() {
        return kid;
    }

    public String getKidAsBase64() {
        return Base64.getEncoder().encodeToString(kid);
    }

    public X509Certificate getCertficate() {
        return (X509Certificate) cert;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * sign hash.
     * @param base64Hash base64Hash
     * @return string
     * @throws NoSuchAlgorithmException exception
     * @throws InvalidKeyException      exception
     * @throws SignatureException       exception
     */
    public String signHash(String base64Hash) throws CryptoException {
        byte[] hashBytes = Base64.getDecoder().decode(base64Hash);
        Digest contentDigest = new CopyDigest();
        Digest mgfDigest = new SHA256Digest();
        RSAPrivateCrtKey k = (RSAPrivateCrtKey) privateKey;
        RSAPrivateCrtKeyParameters keyparam = new RSAPrivateCrtKeyParameters(k.getModulus(),
                k.getPublicExponent(), k.getPrivateExponent(),
                k.getPrimeP(), k.getPrimeQ(), k.getPrimeExponentP(), k.getPrimeExponentQ(), k.getCrtCoefficient());
        RSABlindedEngine rsaBlindedEngine = new RSABlindedEngine();
        rsaBlindedEngine.init(true,keyparam);
        PSSSigner pssSigner = new PSSSigner(rsaBlindedEngine,contentDigest,mgfDigest,32, (byte) (-68));
        pssSigner.init(true,keyparam);
        pssSigner.update(hashBytes,0,hashBytes.length);
        byte[] signature = pssSigner.generateSignature();
        return Base64.getEncoder().encodeToString(signature);
    }

    public byte[] publicKey() {
        return publicKey.getEncoded();
    }
}
