package eu.europa.ec.dgc.issuance.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TanServiceTest {
    @Test
    public void testGenerateTan() throws Exception {
        TanService tanService = new TanService();
        String tan = tanService.generateNewTan();
        System.out.println(tan);
        assertEquals(8,tan.length());
    }
}