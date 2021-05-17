package eu.europa.ec.dgc.issuance.restapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.data.Eudgc;
import eu.europa.ec.dgc.issuance.restapi.dto.EgdcCodeData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DgciControllerTest {
    @Autowired
    DgciController dgciController;

    @Test
    void checkBackendIssuing() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Eudgc edgc = mapper.readValue("{\"ver\":\"1.0.0\",\"nam\":{\"fn\":\"Garcia\",\"fnt\":\"GARCIA\"," +
                "\"gn\":\"Francisco\",\"gnt\":\"FRANCISCO\"},\"dob\":\"1991-01-01\",\"v\":[{\"tg\":\"840539006\"," +
                "\"vp\":\"1119305005\",\"mp\":\"EU/1/20/1507\",\"ma\":\"ORG-100001699\",\"dn\":1,\"sd\":2,\"dt\":" +
                "\"2021-05-14\",\"co\":\"CY\",\"is\":\"Neha\",\"ci\":\"dgci:V1:CY:HIP4OKCIS8CXKQMJSSTOJXAMP:03\"}]}"
            ,Eudgc.class);
        ResponseEntity<EgdcCodeData> responseEntity = dgciController.createEdgc(edgc);
        assertNotNull(responseEntity.getBody());
    }
}
