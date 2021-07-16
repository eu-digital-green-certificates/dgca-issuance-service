package eu.europa.ec.dgc.issuance.service;

import ehn.techiop.hcert.kotlin.data.GreenCertificate;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import lombok.Data;
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

    /**
     * calulate cbor web token expiration fields.
     * It depends partly on configuration and for test and recovery also from DGC Json data
     * @param eudgc json data of dgc
     * @return the times
     */
    public CwtTimeFields calculateCwtExpiration(GreenCertificate eudgc) {
        CwtTimeFields result = new CwtTimeFields();
        GreenCertificateType greenCertificateType;
        long expirationTime;
        long issueTime = Instant.now().getEpochSecond();
        long expirationStartTime = issueTime;

        if (eudgc.getTests() != null && eudgc.getTests().length > 0) {
            greenCertificateType = GreenCertificateType.Test;
            expirationStartTime = extractTimesSec(eudgc.getTests()[0].getDateTimeSample(),expirationStartTime);
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
        } else if (eudgc.getRecoveryStatements() != null && eudgc.getRecoveryStatements().length > 0) {
            greenCertificateType = GreenCertificateType.Recovery;
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
            expirationTime = extractTimesSec(eudgc.getRecoveryStatements()[0].getCertificateValidUntil(),
                expirationTime);
        } else if (eudgc.getVaccinations() != null && eudgc.getVaccinations().length > 0) {
            greenCertificateType = GreenCertificateType.Vaccination;
            expirationStartTime = extractTimesSec(eudgc.getVaccinations()[0].getDate(),expirationStartTime);
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
        } else {
            greenCertificateType = GreenCertificateType.Vaccination;
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
        }
        result.setIssuedAt(issueTime);
        result.setExpiration(expirationTime);
        return result;
    }

    private long extractTimesSec(kotlinx.datetime.Instant date, long defaultTimeSec) {
        long timeSec;
        if (date != null) {
            timeSec = date.getEpochSeconds();
        } else {
            timeSec = defaultTimeSec;
        }
        return timeSec;
    }

    private long extractTimesSec(kotlinx.datetime.LocalDate localDate, long defaultTimeSec) {
        long timeSec;
        if (localDate != null) {
            timeSec = localDate.getValue$kotlinx_datetime().atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        } else {
            timeSec = defaultTimeSec;
        }
        return timeSec;
    }


    @Data
    public static class CwtTimeFields {
        long issuedAt;
        long expiration;
    }
}
