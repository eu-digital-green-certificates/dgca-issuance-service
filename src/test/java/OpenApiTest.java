import eu.europa.ec.dgc.issuance.DgcIssuanceApplication;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(
    classes = DgcIssuanceApplication.class,
    properties = {
        "server.port=8080",
        "springdoc.api-docs.enabled=true",
        "springdoc.api-docs.path=/openapi"
    },
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
public class OpenApiTest {

    @Test
    public void apiDocs() {
        try (BufferedInputStream in = new BufferedInputStream(new URL("http://localhost:8080/openapi").openStream());
            FileOutputStream out = new FileOutputStream("target/openapi.json")) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (Exception e) {
            log.error("Failed to download openapi specification.", e);
            Assertions.fail();
        }
    }
}
