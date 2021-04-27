package eu.europa.ec.dgc.issuance.service;

import COSE.AlgorithmID;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.utils.CertificateUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
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
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.springframework.stereotype.Component;

@Component
@Slf4j
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

        File keyFile = new File(issuanceConfigProperties.getKeyStoreFile());
        if (!keyFile.isFile()) {
            log.error("keyfile not found on: "+keyFile+ " please adapt the configuration property: issuance.keyStoreFile");
            throw new DGCINotFound("keyfile not found on: "+keyFile+ " please adapt the configuration property: issuance.keyStoreFile");
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
            kid = Base64.getDecoder().decode(kidBase64);
            log.info("cert key loaded kid (base64): '"+getKidAsBase64()+ "' algid: "+getAlgId());
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
    public String signHash(String base64Hash) throws CryptoException, IOException {
        byte[] hashBytes = Base64.getDecoder().decode(base64Hash);
        byte[] signature;
        if (publicKey instanceof RSAPublicKey) {
            signature = signRSAPSS(hashBytes);
        } else {
            signature = signEC(hashBytes);
        }
        return Base64.getEncoder().encodeToString(signature);
    }

    private byte[] signRSAPSS(byte[] hashBytes) throws CryptoException {
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
        return signature;
    }

    private byte[] signEC(byte[] hash) throws IOException {
        java.security.interfaces.ECPrivateKey privKey = (java.security.interfaces.ECPrivateKey)privateKey;
        ECParameterSpec s = EC5Util.convertSpec(privKey.getParams());
        ECPrivateKeyParameters keyparam = new ECPrivateKeyParameters(
                privKey.getS(),
                new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        ECDSASigner pssSigner = new ECDSASigner();
        pssSigner.init(true,keyparam);
        BigInteger[] result3BI = pssSigner.generateSignature(hash);
        byte[] result3 = StandardDSAEncoding.INSTANCE.encode(pssSigner.getOrder(), result3BI[0], result3BI[1]);
        return result3;
    }

    public byte[] publicKey() {
        return publicKey.getEncoded();
    }

    public int getAlgId() {
        int algId;
        if (publicKey instanceof RSAPublicKey) {
            algId = AlgorithmID.RSA_PSS_256.AsCBOR().AsInt32();
        } else if (publicKey instanceof ECPublicKey) {
            algId = AlgorithmID.ECDSA_256.AsCBOR().AsInt32();
        } else {
            throw new IllegalArgumentException("unsupported key type");
        }
        return algId;
    }
}
