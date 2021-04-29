package eu.europa.ec.dgc.issuance.service;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public interface CertificatePrivateKeyProvider {
    Certificate getCertificate();

    PrivateKey getPrivateKey();
}
