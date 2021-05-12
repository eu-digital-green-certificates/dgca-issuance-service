package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.utils.HashUtil;
import java.security.SecureRandom;
import org.apache.commons.lang3.RandomStringUtils;

public final class Tan {

    private static final int TAN_LENGTH = 8;
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final char[] CHAR_SET_FOR_TAN = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private String rawTan;
    private String hashedTan;

    private Tan() {
    }

    /**
     * Create new TAN object with a TAN and the hash of the TAN. The TAN is constructed from a charset consisting
     * of A-Z (exclcuding I and O) and 2-9.
     *
     * @return the newly created TAN object
     */
    public static Tan create() {
        Tan retVal = new Tan();
        retVal.rawTan = retVal.generateNewTan();
        retVal.hashedTan = HashUtil.sha256Base64(retVal.rawTan);
        return retVal;
    }

    private String generateNewTan() {
        SecureRandom random = new SecureRandom();
        long rnd = random.nextLong();
        int radixLen = CHAR_SET_FOR_TAN.length;
        StringBuilder tan = new StringBuilder();
        while (tan.length() < TAN_LENGTH) {
            if (rnd == 0) {
                rnd = random.nextLong();
                continue;
            }
            tan.append(CHAR_SET_FOR_TAN[Math.abs((int) (rnd % radixLen))]);
            rnd /= radixLen;
        }
        return tan.toString();
    }

    public String getRawTan() {
        return rawTan;
    }

    public String getHashedTan() {
        return hashedTan;
    }
}
