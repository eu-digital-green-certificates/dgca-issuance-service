package eu.europa.ec.dgc.issuance.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("issuance")
public class IssuanceConfigProperties {
    private String dgciPrefix;
    private String keyStoreFile;
    private String keyStorePassword;
    private String certAlias;
    private String privateKeyPassword;
    private String countryCode;
}
