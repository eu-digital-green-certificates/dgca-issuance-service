package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class EdgcValidatorTest {
    @Autowired
    EdgcValidator edgcValidator;

    @Test
    void testValidateSamples() throws Exception {
        String testData = "HC1:6BFOXN%TSMAHN-H/P8JU6+BS.5E9%UD82.7JJ59W2TT+FM*4/IQ0YVKQCPTHCV4*XUA2PWKP/HLIJL8JF8J" +
            "F7LPMIH-O92UQ7QQ%NH0LA5O6/UIGSU7QQ7NGWWBA 7.UIAYU3X3SH90THYZQ H9+W3.G8MSGPRAAUICO1DV59UE6Q1M650 LHZA0" +
            "D9E2LBHHGKLO-K%FGLIA5D8MJKQJK JMDJL9GG.IA.C8KRDL4O54O4IGUJKJGI.IAHLCV5GVWN.FKP123NJ%HBX/KR968X2-36/-K" +
            "KTCY73$80PU6QW6H+932QDONAC5287T:7N95*K64POPGI*%DC*G2KV SU1Y6B.QEN7+SQ4:4P2C:8UFOFC072.T2PE0*J65UY.2ED" +
            "TYJDK8W$WKF.VUV9L+VF3TY71NSFIM2F:47*J0JLV50M1WB*C";

        EgcDecodeResult result = edgcValidator.decodeEdgc(testData);
        assertTrue(result.isValidated());
        assertNull(result.getErrorMessage());
    }

    @Test
    void testValidateAUSamples() throws Exception {
        String testData = "HC1:NCFOXN%TS3DHZN4HAF*PQFKKGTNA.Q/R8WRU2FCGJ9ZU6+9GNH5%DW+70ZMIN9HNO4*J8OX4W$C2VL*LA 4" +
            "3/IE%TE6UG+ZEAT1HQ13W1:O1YUI%F1PN1/T1J$HTR9/O14SI.J9DYHZROVZ05QNZ 20OP748$NI4L6RXKYQ8FRKBYOBM4T$7U-N0" +
            "O4RK43%JTXO$WOS%H*-VZIEQKERQ8IY1I$HH%U8 9PS5OH6*ZUFZFEPG:YN/P3JRH8LHGL2-LH/CJTK96L6SR9MU9DV5 R1:PI/E2" +
            "$4J6AL.+I9UV6$0+BNPHNBC7CTR3$VDY0DUFRLN/Y0Y/K9/IIF0%:K6*K$X4FUTD14//E3:FL.B$JDBLEH-BL1H6TK-CI:ULOPD6LF" +
            "20HFJC3DAYJDPKDUDBQEAJJKHHGEC8ZI9$JAQJKZ%K.CPM+81727KGB9S3EM.KV+LP:14JME*2T/5OFDSM*E.4RYXRN2IMA5GDUGS" +
            "0/X6*CW0Z3XZFK+FT6GF-I5U9:0J.LIVB0+2T+1K4OUWGQUKM.P6V20KK1R3";

        EgcDecodeResult result = edgcValidator.decodeEdgc(testData);
        // it can be not validated because different key
        assertFalse(result.isValidated());
        assertNull(result.getErrorMessage());
    }

}
