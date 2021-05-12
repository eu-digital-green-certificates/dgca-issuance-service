package eu.europa.ec.dgc.issuance.service.impl;

import eu.europa.ec.dgc.issuance.utils.btp.CredentialStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component("uploadKeyProvider")
@Profile("btp")
@Slf4j
public class BtpUploadKeyProviderImpl extends BtpAbstractKeyProvider {

    private static final String UPLOAD_KEY_NAME = "upload-key";
    private static final String UPLOAD_CERT_NAME = "upload-cert";

    @Autowired
    public BtpUploadKeyProviderImpl(CredentialStore credentialStore) {
        super(credentialStore);
    }

    @Override
    public Certificate getCertificate() {
        return getCertificateFromStore(UPLOAD_CERT_NAME);
    }

    @Override
    public PrivateKey getPrivateKey() {
        return getPrivateKeyFromStore(UPLOAD_KEY_NAME);
    }

}
