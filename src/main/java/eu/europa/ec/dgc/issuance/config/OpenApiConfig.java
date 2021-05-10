package eu.europa.ec.dgc.issuance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import java.util.Optional;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Generated
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final Optional<BuildProperties> buildProperties;

    /**
     * Configure the OpenApi bean with title and version.
     *
     * @return the OpenApi bean.
     */
    @Bean
    public OpenAPI openApi() {
        String version;
        if (buildProperties.isPresent()) {
            version = buildProperties.get().getVersion();
        } else {
            // build properties is not available if starting from IDE without running mvn before (so fake this)
            version = "dev";
        }
        return new OpenAPI()
            .info(new Info()
                .title("Digital Green Certificate Issuance")
                .description("The API defines Issuance Service for digital green certificates.")
                .version(version)
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
