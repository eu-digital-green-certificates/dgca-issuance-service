package eu.europa.ec.dgc.issuance.service.impl;

import eu.europa.ec.dgc.issuance.service.SigningService;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Plaintext;


@Service
@RequiredArgsConstructor
@Profile("vault")
public class VaultSigningServiceImpl implements SigningService {

    private final VaultTemplate vaultTemplate;

    @Value("${dgc.signKey:issuer-key}")
    private String signKey;

    @Override
    public byte[] signHash(byte[] hash, PrivateKey privateKey) {
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        String signature = vaultTemplate.opsForTransit().sign(signKey, Plaintext.of(hashBase64)).getSignature();
        if (signature.startsWith("vault:v1:")) {
            signature = signature.substring(9);
        }
        return Base64.getDecoder().decode(signature);
    }
}
