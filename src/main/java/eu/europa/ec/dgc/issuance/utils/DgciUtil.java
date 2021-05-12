package eu.europa.ec.dgc.issuance.utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;

public class DgciUtil {

    /**
     * Encode UUID to charset of A-Z and 0-9.
     *
     * @param uuid the UUID to hash
     * @return the hashed UUID
     */
    public static String encodeDgci(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        BigInteger bint = new BigInteger(1, bb.array());
        int radix = 10 + ('Z' - 'A');
        return bint.toString(radix).toUpperCase();
    }

}
