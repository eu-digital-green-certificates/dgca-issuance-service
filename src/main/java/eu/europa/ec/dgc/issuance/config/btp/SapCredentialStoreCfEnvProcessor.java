package eu.europa.ec.dgc.issuance.config.btp;

import io.pivotal.cfenv.core.CfCredentials;
import io.pivotal.cfenv.core.CfService;
import io.pivotal.cfenv.spring.boot.CfEnvProcessor;
import io.pivotal.cfenv.spring.boot.CfEnvProcessorProperties;
import java.util.Map;

/**
 * Custom implementation of {@link CfEnvProcessor} for reading the SAP credential store parameters from the <code>
 * VCAP_SERVICES</code> environment variable and making them available as properties in the spring context.
 * <br/><br/>
 * The following properties are available in the context after the processor is done:
 * <code>
 * <ul>
 *     <li>sap.btp.credstore.url</li>
 *     <li>sap.btp.credstore.password</li>
 *     <li>sap.btp.credstore.username</li>
 *     <li>sap.btp.credstore.clientPrivateKey</li>
 *     <li>sap.btp.credstore.serverPublicKey</li>
 * </ul>
 * </code>
 *
 * @see CfEnvProcessor
 */
public class SapCredentialStoreCfEnvProcessor implements CfEnvProcessor {

    private static final String CRED_STORE_SCHEME = "credstore";
    private static final String CRED_STORE_PROPERTY_PREFIX = "sap.btp.credstore";

    @Override
    public boolean accept(CfService service) {
        return service.existsByTagIgnoreCase(CRED_STORE_SCHEME, "securestore", "keystore", "credentials")
            || service.existsByLabelStartsWith(CRED_STORE_SCHEME)
            || service.existsByUriSchemeStartsWith(CRED_STORE_SCHEME);
    }

    @Override
    public void process(CfCredentials cfCredentials, Map<String, Object> properties) {
        properties.put(CRED_STORE_PROPERTY_PREFIX + ".url", cfCredentials.getString("url"));
        properties.put(CRED_STORE_PROPERTY_PREFIX + ".password", cfCredentials.getString("password"));
        properties.put(CRED_STORE_PROPERTY_PREFIX + ".username", cfCredentials.getString("username"));

        @SuppressWarnings("unchecked")
        Map<String, Object> encryption = (Map<String, Object>) cfCredentials.getMap().get("encryption");
        if (encryption == null) {
            // Encryption features have been disabled on this BTP instance.
            properties.put(CRED_STORE_PROPERTY_PREFIX + ".clientPrivateKey", "encryption-disabled");
            properties.put(CRED_STORE_PROPERTY_PREFIX + ".serverPublicKey", "encryption-disabled");
            return;
        }

        String clientPrivateKey = encryption.get("client_private_key").toString();
        String serverPublicKey = encryption.get("server_public_key").toString();

        properties.put(CRED_STORE_PROPERTY_PREFIX + ".clientPrivateKey", clientPrivateKey);
        properties.put(CRED_STORE_PROPERTY_PREFIX + ".serverPublicKey", serverPublicKey);
    }

    @Override
    public CfEnvProcessorProperties getProperties() {
        return CfEnvProcessorProperties.builder()
                .propertyPrefixes(CRED_STORE_PROPERTY_PREFIX)
                .serviceName("CredentialStore")
                .build();
    }

}
