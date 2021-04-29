package eu.europa.ec.dgc.issuance.service;

import java.security.PrivateKey;

public interface SigningService {
    /**
     * continue signing on already SHA256 generated content hash.
     * The it is only the Part of regular signing functionality.
     * Do not use regular Signing API because it will cause to hash the data twice and produce wrong
     * signature
     * @param hash SHA256 hash of content
     * @param privateKey RSA or EC
     * @return signature as raw byte array
     */
    byte[] signHash(byte[] hash, PrivateKey privateKey);
}
