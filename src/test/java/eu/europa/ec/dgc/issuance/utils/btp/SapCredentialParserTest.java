package eu.europa.ec.dgc.issuance.utils.btp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

public class SapCredentialParserTest {

    private static final String EXPECTED_ID = "5f5fa34e-be21-4c7a-8548-4a538b7156ed";
    private static final String EXPECTED_VALUE = "SoMeBaSe64EnCoDeDkEy==";
    private static final String EXPECTED_NAME = "key-name";

    @Test
    public void testJsonParsing() throws IOException {
        ClassPathResource jsonResource = new ClassPathResource("/data/fromCredStore.json");
        String json = Files.readString(jsonResource.getFile().toPath(), StandardCharsets.UTF_8);

        SapCredential sapCredential = SapCredential.fromJson(json);
        Assert.assertEquals(EXPECTED_ID, sapCredential.getId());
        Assert.assertEquals(EXPECTED_VALUE, sapCredential.getValue());
        Assert.assertEquals(EXPECTED_NAME, sapCredential.getName());
    }
}
