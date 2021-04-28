package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DgciGeneratorTest {
    @Test
    public void testGenerateDGCI() throws Exception {
        IssuanceConfigProperties issuanceConfigProperties = new IssuanceConfigProperties();
        issuanceConfigProperties.setDgciPrefix("dgci:V1:DE");
        DgciGenerator dgciGenerator = new DgciGenerator(issuanceConfigProperties);
        String dgci = dgciGenerator.newDgci();
        assertNotNull(dgci);
        assertTrue(dgci.startsWith(issuanceConfigProperties.getDgciPrefix()));
        System.out.println(dgci);
    }
}