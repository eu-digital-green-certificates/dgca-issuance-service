package eu.europa.ec.dgc.issuance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import org.junit.Test;

public class Sh256HashTest {
    @Test
    public void testCreateSHA256Hash() throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        final byte[] hashbytes = digest.digest(
                "some_data".getBytes(StandardCharsets.UTF_8));
        System.out.println(Base64.getEncoder().encodeToString(hashbytes));
    }
}
