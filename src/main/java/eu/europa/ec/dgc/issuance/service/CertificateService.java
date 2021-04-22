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
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
    public String signHash(String base64Hash) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] hashBytes = Base64.getDecoder().decode(base64Hash);
        /*
        // The content is already hash so we need only to encrypt with private key
        // TODO check if cipher reusing
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] cipherText = encryptCipher.doFinal(hashBytes);
        return Base64.getEncoder().encodeToString(cipherText);
         */
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(hashBytes);
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public byte[] publicKey() {
        return publicKey.getEncoded();
    }
}
