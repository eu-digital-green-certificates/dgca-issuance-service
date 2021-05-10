package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpirationService {

    private final IssuanceConfigProperties issuanceConfigProperties;

    /**
     * expiration duration for given edgc type.
     * @param greenCertificateType edgc type
     * @return Duration
     */
    public Duration expirationForType(GreenCertificateType greenCertificateType) {
        Duration duration;
        if (issuanceConfigProperties.getExpiration() != null) {
            switch (greenCertificateType) {
                case Test:
                    duration = issuanceConfigProperties.getExpiration().getTest();
                    break;
                case Vaccination:
                    duration = issuanceConfigProperties.getExpiration().getVaccination();
                    break;
                case Recovery:
                    duration = issuanceConfigProperties.getExpiration().getRecovery();
                    break;
                default:
                    throw new IllegalArgumentException("unsupported cert type for expiration: " + greenCertificateType);
            }
        } else {
            duration = null;
        }
        if (duration == null) {
            duration = Duration.of(365, ChronoUnit.DAYS);
        }
        return duration;
    }
}
