package eu.europa.ec.dgc.issuance.service;

import ehn.techiop.hcert.data.Eudgc;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
    public CwtTimeFields calculateCwtExpiration(Eudgc eudgc) {
        CwtTimeFields result = new CwtTimeFields();
        GreenCertificateType greenCertificateType;
        long expirationTime;
        long issueTime = Instant.now().getEpochSecond();
        long expirationStartTime = issueTime;

        if (eudgc.getT() != null && !eudgc.getT().isEmpty()) {
            greenCertificateType = GreenCertificateType.Test;
            expirationStartTime = extractTimesSec(eudgc.getT().get(0).getSc(),expirationStartTime);
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
        } else if (eudgc.getR() != null && !eudgc.getR().isEmpty()) {
            greenCertificateType = GreenCertificateType.Recovery;
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
            expirationTime = extractTimesSec(eudgc.getR().get(0).getDu(),expirationTime);
        } else if (eudgc.getV() != null && !eudgc.getV().isEmpty()) {
            greenCertificateType = GreenCertificateType.Vaccination;
            expirationStartTime = extractTimesSec(eudgc.getV().get(0).getDt(),expirationStartTime);
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
        } else {
            // fallback
            greenCertificateType = GreenCertificateType.Vaccination;
            expirationTime = expirationStartTime + expirationForType(greenCertificateType).get(ChronoUnit.SECONDS);
        }
        result.setIssuedAt(issueTime);
        result.setExpiration(expirationTime);
        return result;
    }

    private long extractTimesSec(Date date, long defaultTimeSec) {
        long timeSec;
        if (date != null) {
            timeSec = date.toInstant().getEpochSecond();
        } else {
            timeSec = defaultTimeSec;
        }
        return timeSec;
    }

    private long extractTimesSec(String dateAsString, long defaultTimeSec) {
        long timeSec;
        if (dateAsString != null && dateAsString.length() > 0) {
            timeSec = LocalDate.parse(dateAsString).atStartOfDay().toInstant(ZoneOffset.UTC).getEpochSecond();
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
