package eu.europa.ec.dgc.issuance.service;


import eu.europa.ec.dgc.gateway.connector.DgcGatewayUploadConnector;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CertKeyPublisherService {
    private final CertificateService certificateService;
    private final Optional<DgcGatewayUploadConnector> dgcGetewayUploadConnector;

    /**
     * publish signing certificate to gateway.
     */
    public void publishKey() {
        if (dgcGetewayUploadConnector.isPresent()) {
            log.info("start publish certificate to gateway");
            DgcGatewayUploadConnector connector = dgcGetewayUploadConnector.get();
            try {
                connector.uploadTrustedCertificate(certificateService.getCertficate());
                log.info("certificate uploaded to gateway");
            } catch (DgcGatewayUploadConnector.DgcCertificateUploadException e) {
                log.error("can not upload certificate to gateway",e);
                throw new DdcGatewayException("error during gateway connector communication",e);
            }
        } else {
            log.warn("can not publish certificate to gateway, because the getaway connector is not enabled");
            throw new DdcGatewayException("getaway connector is configured as disabled");
        }
    }
}
