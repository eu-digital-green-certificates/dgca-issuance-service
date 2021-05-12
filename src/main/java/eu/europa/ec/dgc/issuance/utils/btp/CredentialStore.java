package eu.europa.ec.dgc.issuance.utils.btp;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Profile("btp")
public class CredentialStore {

    private static final Logger log = LoggerFactory.getLogger(CredentialStore.class);

    @Value("${sap.btp.credstore.url}")
    private String url;

    private final CredentialStoreCryptoUtil cryptoUtil;

    private final RestTemplate restTemplate;

    @Autowired
    public CredentialStore(CredentialStoreCryptoUtil cryptoUtil, RestTemplate restTemplate) {
        this.cryptoUtil = cryptoUtil;
        this.restTemplate = restTemplate;
    }

    /**
     * Return the key located under the given name in the credential store.
     *
     * @param name the name of the key
     * @return the key from the credential store
     */
    public SapCredential getKeyByName(String name) {
        log.debug("Querying key with name '{}'.", name);
        String response = restTemplate.getForEntity(url + "/key?name=" + URLEncoder.encode(name,
            StandardCharsets.UTF_8), String.class).getBody();
        return SapCredential.fromJson(cryptoUtil.decrypt(response));
    }

}
