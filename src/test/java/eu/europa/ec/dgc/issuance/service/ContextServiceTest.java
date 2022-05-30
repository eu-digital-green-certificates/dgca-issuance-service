package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    properties = {
        "issuance.contextData = {\"test\":\"Data\"}"
    }
)
class ContextServiceTest {

    @Autowired
    ContextService contextService;

    @Test
    void getContextFromEnvironment() {
        JsonNode json = contextService.getContextDefinition();
        assertEquals("{\"test\":\"Data\"}",json.toString());
    }

}
