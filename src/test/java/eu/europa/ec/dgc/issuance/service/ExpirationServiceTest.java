package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.kotlin.data.GreenCertificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import kotlinx.serialization.json.Json;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ExpirationServiceTest {
    @Autowired
    ExpirationService expirationService;
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testExpriationCalculation() throws Exception {
        String vacDataJson = SampleData.vaccination;
        GreenCertificate eudgc = testCalculation("vactination",vacDataJson);
        System.out.println(eudgc.getVaccinations()[0].getDate());
        String recoveryDataJson = SampleData.recovery;
        eudgc = testCalculation("recovery",recoveryDataJson);
        System.out.println(eudgc.getRecoveryStatements()[0].getCertificateValidUntil());
        String testDataJson = SampleData.testNaa;
        eudgc = testCalculation("test",testDataJson);
        System.out.println(eudgc.getTests()[0].getDateTimeSample());
        assertNotNull(eudgc);

    }

    private GreenCertificate testCalculation(String description, String vacDataJson) throws com.fasterxml.jackson.core.JsonProcessingException {
        System.out.println("testing: "+description);
        GreenCertificate eudgc = Json.Default.decodeFromString(GreenCertificate.Companion.serializer(), vacDataJson);
        ExpirationService.CwtTimeFields expTime = expirationService.calculateCwtExpiration(eudgc);
        LocalDateTime issuedAt = LocalDateTime.ofEpochSecond(expTime.getIssuedAt(), 0, ZoneOffset.UTC);
        assertNotNull(issuedAt);
        System.out.println(issuedAt);
        LocalDateTime expired = LocalDateTime.ofEpochSecond(expTime.getExpiration(), 0, ZoneOffset.UTC);
        System.out.println(expired);
        return eudgc;
    }
}
