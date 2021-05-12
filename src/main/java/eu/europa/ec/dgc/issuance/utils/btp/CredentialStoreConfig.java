package eu.europa.ec.dgc.issuance.utils.btp;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("btp")
public class CredentialStoreConfig {

    @Value("${sap.btp.credstore.username}")
    private String username;

    @Value("${sap.btp.credstore.password}")
    private String password;

    @Value("${sap.btp.credstore.namespace}")
    private String namespace;

    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Basic " + getAuthToken());
            request.getHeaders().set("sapcp-credstore-namespace", namespace);
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    private String getAuthToken() {
        String authHeader = username + ":" + password;
        return Base64.getEncoder().encodeToString(authHeader.getBytes(StandardCharsets.UTF_8));
    }

}
