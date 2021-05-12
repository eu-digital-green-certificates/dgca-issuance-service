package eu.europa.ec.dgc.issuance.service.impl;

import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpClientAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import eu.europa.ec.dgc.issuance.service.CertKeyPublisherService;
import eu.europa.ec.dgc.issuance.service.CertificatePrivateKeyProvider;
import eu.europa.ec.dgc.signing.SignedCertificateMessageBuilder;
import eu.europa.ec.dgc.utils.CertificateUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.bouncycastle.cert.X509CertificateHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Publishes the issuer's public certificate to the DGC gateway. The public certificate will be signed with the upload
 * key provided by the upload key provider.
 *
 * @see BtpUploadKeyProviderImpl
 */
@Component
@Profile("btp")
@Slf4j
public class BtpCertKeyPublisherServiceImpl implements CertKeyPublisherService {

    private static final String DGCG_DESTINATION = "dgcg-destination";
    private static final String DGCG_UPLOAD_ENDPOINT = "/signerCertificate";

    private final CertificatePrivateKeyProvider uploadKeyProvider;
    private final CertificatePrivateKeyProvider issuerKeyProvider;
    private final CertificateUtils certificateUtils;

    /**
     * Initializes the publisher service with all key provider and utilities needed for uploading certificates to
     * the gateway.
     *
     * @param uploadKeyProvider the upload certificate needed to sign the request
     * @param issuerKeyProvider the issuer certificate beeing uploaded
     * @param certificateUtils utilities to convert different certificate formats
     */
    public BtpCertKeyPublisherServiceImpl(
        @Qualifier("uploadKeyProvider") CertificatePrivateKeyProvider uploadKeyProvider,
        @Qualifier("issuerKeyProvider") CertificatePrivateKeyProvider issuerKeyProvider,
        CertificateUtils certificateUtils) {
        this.uploadKeyProvider = uploadKeyProvider;
        this.issuerKeyProvider = issuerKeyProvider;
        this.certificateUtils = certificateUtils;
    }

    @Override
    public void publishKey() {
        log.debug("Uploading key to gateway.");
        HttpDestination httpDestination = DestinationAccessor.getDestination(DGCG_DESTINATION).asHttp();
        HttpClient httpClient = HttpClientAccessor.getHttpClient(httpDestination);

        try {
            X509CertificateHolder issuerCertHolder = certificateUtils
                .convertCertificate((X509Certificate) issuerKeyProvider.getCertificate());
            X509CertificateHolder uploadCertHolder = certificateUtils
                .convertCertificate((X509Certificate) uploadKeyProvider.getCertificate());

            String payload = new SignedCertificateMessageBuilder()
                .withPayloadCertificate(issuerCertHolder)
                .withSigningCertificate(uploadCertHolder, uploadKeyProvider.getPrivateKey()).buildAsString();

            HttpUriRequest postRequest = RequestBuilder.post(DGCG_UPLOAD_ENDPOINT)
                .addHeader("Content-type", "application/cms")
                .setEntity(new StringEntity(payload, StandardCharsets.UTF_8))
                .build();

            HttpResponse response = httpClient.execute(postRequest);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                log.info("Successfully upload certificate to gateway.");
            } else {
                log.warn("Gateway returned 'HTTP {}: {}'.", response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase());
            }

        } catch (CertificateEncodingException | IOException e) {
            log.error("Error while upload certificate to gateway: '{}'.", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
