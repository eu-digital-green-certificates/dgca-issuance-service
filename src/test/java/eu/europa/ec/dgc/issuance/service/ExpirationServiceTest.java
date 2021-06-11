package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.data.Eudgc;
import ehn.techiop.hcert.kotlin.chain.SampleData;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
        String vacDataJson = SampleData.Companion.getVaccination();
        Eudgc eudgc = testCalculation("vactination",vacDataJson);
        System.out.println(eudgc.getV().get(0).getDt());
        String recoveryDataJson = SampleData.Companion.getRecovery();
        eudgc = testCalculation("recovery",recoveryDataJson);
        System.out.println(eudgc.getR().get(0).getDu());
        String testDataJson = SampleData.Companion.getTestNaa();
        eudgc = testCalculation("test",testDataJson);
        System.out.println(eudgc.getT().get(0).getSc().toInstant().atOffset(ZoneOffset.UTC));
        assertNotNull(eudgc);

    }

    private Eudgc testCalculation(String description, String vacDataJson) throws com.fasterxml.jackson.core.JsonProcessingException {
        System.out.println("testing: "+description);
        Eudgc eudgc = objectMapper.readValue(vacDataJson,Eudgc.class);
        ExpirationService.CwtTimeFields expTime = expirationService.calculateCwtExpiration(eudgc);
        LocalDateTime issuedAt = LocalDateTime.ofEpochSecond(expTime.getIssuedAt(), 0, ZoneOffset.UTC);
        assertNotNull(issuedAt);
        System.out.println(issuedAt);
        LocalDateTime expired = LocalDateTime.ofEpochSecond(expTime.getExpiration(), 0, ZoneOffset.UTC);
        System.out.println(expired);
        return eudgc;
    }
}
