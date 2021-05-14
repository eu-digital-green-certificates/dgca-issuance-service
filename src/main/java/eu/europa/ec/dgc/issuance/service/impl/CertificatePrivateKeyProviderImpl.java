package eu.europa.ec.dgc.issuance.service.impl;

import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.service.CertificatePrivateKeyProvider;
import eu.europa.ec.dgc.issuance.service.DgciNotFound;
import eu.europa.ec.dgc.utils.CertificateUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("issuerKeyProvider")
@Profile("!btp")
@Slf4j
@RequiredArgsConstructor
public class CertificatePrivateKeyProviderImpl implements CertificatePrivateKeyProvider {
    private Certificate cert;
    private final IssuanceConfigProperties issuanceConfigProperties;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    /**
     * PostConstruct method to load KeyStore for issuing certificates.
     */
    @PostConstruct
    public void loadKeyStore() throws KeyStoreException, IOException,
            CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        final char[] keyStorePassword = issuanceConfigProperties.getKeyStorePassword().toCharArray();
        final String keyName = issuanceConfigProperties.getCertAlias();

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        KeyStore keyStore = KeyStore.getInstance("JKS");

        File keyFile = new File(issuanceConfigProperties.getKeyStoreFile());
        if (!keyFile.isFile()) {
            log.error("keyfile not found on: {} please adapt the configuration property: issuance.keyStoreFile",
                    keyFile);
            throw new DgciNotFound("keyfile not found on: " + keyFile
                    + " please adapt the configuration property: issuance.keyStoreFile");
        }
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
            CertificateUtils certificateUtils = new CertificateUtils();
            String kidBase64 = certificateUtils.getCertKid((X509Certificate) cert);
        }
    }

    @Override
    public Certificate getCertificate() {
        return cert;
    }

    @Override
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}
