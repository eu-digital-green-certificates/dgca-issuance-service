package eu.europa.ec.dgc.issuance.service.impl;

import eu.europa.ec.dgc.issuance.service.SigningService;
import java.io.IOException;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.springframework.stereotype.Component;

@Component
public class SigningServiceImpl implements SigningService {
    @Override
    public byte[] signHash(byte[] hashBytes, PrivateKey privateKey) {
        byte[] signature;
        try {
            if (privateKey instanceof RSAPrivateCrtKey) {
                signature = signRsapss(hashBytes, privateKey);
            } else {
                signature = signEc(hashBytes, privateKey);
            }
        } catch (CryptoException | IOException e) {
            throw new IllegalArgumentException("error during signing ",e);
        }
        return signature;
    }

    private byte[] signRsapss(byte[] hashBytes, PrivateKey privateKey) throws CryptoException {
        Digest contentDigest = new CopyDigest();
        Digest mgfDigest = new SHA256Digest();
        RSAPrivateCrtKey k = (RSAPrivateCrtKey) privateKey;
        RSAPrivateCrtKeyParameters keyparam = new RSAPrivateCrtKeyParameters(k.getModulus(),
                k.getPublicExponent(), k.getPrivateExponent(),
                k.getPrimeP(), k.getPrimeQ(), k.getPrimeExponentP(), k.getPrimeExponentQ(), k.getCrtCoefficient());
        RSABlindedEngine rsaBlindedEngine = new RSABlindedEngine();
        rsaBlindedEngine.init(true, keyparam);
        PSSSigner pssSigner = new PSSSigner(rsaBlindedEngine, contentDigest, mgfDigest, 32, (byte) (-68));
        pssSigner.init(true, keyparam);
        pssSigner.update(hashBytes, 0, hashBytes.length);
        return pssSigner.generateSignature();
    }

    private byte[] signEc(byte[] hash, PrivateKey privateKey) throws IOException {
        java.security.interfaces.ECPrivateKey privKey = (java.security.interfaces.ECPrivateKey) privateKey;
        ECParameterSpec s = EC5Util.convertSpec(privKey.getParams());
        ECPrivateKeyParameters keyparam = new ECPrivateKeyParameters(
                privKey.getS(),
                new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        ECDSASigner pssSigner = new ECDSASigner();
        pssSigner.init(true, keyparam);
        BigInteger[] result3BI = pssSigner.generateSignature(hash);
        byte[] rArr = result3BI[0].toByteArray();
        byte[] sArr = result3BI[1].toByteArray();
        byte[] sig = new byte[64];
        System.arraycopy(rArr, rArr.length==33 ? 1 : 0, sig, 0, 32);
        System.arraycopy(sArr, sArr.length==33 ? 1 : 0, sig, 32, 32);

        return sig;
    }
}