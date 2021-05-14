package eu.europa.ec.dgc.issuance.service.impl;

import eu.europa.ec.dgc.issuance.utils.btp.CredentialStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component("issuerKeyProvider")
@Profile("btp")
public class BtpIssuerKeyProviderImpl extends BtpAbstractKeyProvider {

    private static final String ISSUER_KEY_NAME = "issuer-key";
    private static final String ISSUER_CERT_NAME = "issuer-cert";

    @Autowired
    public BtpIssuerKeyProviderImpl(CredentialStore credentialStore) {
        super(credentialStore);
    }

    @Override
    public Certificate getCertificate() {
        return this.getCertificateFromStore(ISSUER_CERT_NAME);
    }

    @Override
    public PrivateKey getPrivateKey() {
        return getPrivateKeyFromStore(ISSUER_KEY_NAME);
    }

}
