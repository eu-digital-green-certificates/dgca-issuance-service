package eu.europa.ec.dgc.issuance.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Base64;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class CertificateService {
    private KeyStore certKeyStore;
    private PrivateKey privateKey;
    private PublicKey publicKey;

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
        final char[] password = "dgca".toCharArray();
        final String keyName = "edgc_dev_test";

        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (InputStream is = new FileInputStream("src/test/resources/cert_devtest_keystore.jks")) {
            keyStore.load(is, password);
            KeyStore.PasswordProtection keyPassword =       //Key password
                    new KeyStore.PasswordProtection(password);

            KeyStore.PrivateKeyEntry privateKeyEntry =
                    (KeyStore.PrivateKeyEntry) keyStore.getEntry(keyName, keyPassword);
            java.security.cert.Certificate cert = keyStore.getCertificate(keyName);
            publicKey = cert.getPublicKey();
            privateKey = privateKeyEntry.getPrivateKey();
        }
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
}
