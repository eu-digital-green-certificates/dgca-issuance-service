package eu.europa.ec.dgc.issuance.service;


import eu.europa.ec.dgc.gateway.connector.DgcGatewayUploadConnector;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!btp")
@Slf4j
@RequiredArgsConstructor
public class CertKeyPublisherServiceImpl implements CertKeyPublisherService {
    private final CertificateService certificateService;
    private final Optional<DgcGatewayUploadConnector> dgcGatewayUploadConnector;

    @Override
    public void publishKey() {
        if (dgcGatewayUploadConnector.isPresent()) {
            log.info("start publish certificate to gateway");
            DgcGatewayUploadConnector connector = dgcGatewayUploadConnector.get();
            try {
                connector.uploadTrustedCertificate(certificateService.getCertficate());
                log.info("certificate uploaded to gateway");
            } catch (DgcGatewayUploadConnector.DgcCertificateUploadException e) {
                log.error("can not upload certificate to gateway", e);
                throw new DdcGatewayException("error during gateway connector communication", e);
            }
        } else {
            log.warn("can not publish certificate to gateway, because the gateway connector is not enabled");
            throw new DdcGatewayException("gateway connector is configured as disabled");
        }
    }

}
