package eu.europa.ec.dgc.issuance.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import java.io.File;
import java.io.IOException;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnExpression("${issuance.endpoints.wallet:false}")
public class ContextService {
    private final IssuanceConfigProperties issuanceConfigProperties;
    private JsonNode contextDefinition;

    /**
     * load json context file.
     */
    @PostConstruct
    public void loadContextFile() {
        if (issuanceConfigProperties.getContextFile() != null
            && issuanceConfigProperties.getContextFile().length() > 0) {
            File contextFile = new File(issuanceConfigProperties.getContextFile());
            if (!contextFile.isFile()) {
                throw new IllegalArgumentException("configured context file can not be found: " + contextFile);
            }
            ObjectMapper mapper = new ObjectMapper();
            try {
                contextDefinition = mapper.readTree(contextFile);
                log.info("context file loaded from: " + contextFile);
            } catch (IOException e) {
                throw new IllegalArgumentException("can not read json context file: " + contextFile, e);
            }
        } else {
            log.warn("the context json file not configured (property: issuance.contextFile)."
                + " The empty context file is generated instead");
            JsonNodeFactory jsonNodeFactory = JsonNodeFactory.instance;
            ObjectNode contextObj = jsonNodeFactory.objectNode();
            contextObj.set("Origin", jsonNodeFactory.textNode(issuanceConfigProperties.getCountryCode()));
            contextObj.set("versions", jsonNodeFactory.objectNode());
            contextDefinition = contextObj;
        }
    }

    public JsonNode getContextDefintion() {
        return contextDefinition;
    }
}
