package eu.europa.ec.dgc.issuance.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TanService {
    private Random random = new SecureRandom();
    private char[] charSet;
    private static final int TAN_LENGHT = 8;

    /**
     * TODO comment.
     */
    public TanService() {
        StringBuilder chars = new StringBuilder();
        for (char i = '0'; i <= '9'; i++) {
            chars.append(i);
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            if (i != 'I' && i != '0') {
                chars.append(i);
            }
        }
        charSet = chars.toString().toCharArray();
    }

    /**
     * TODO comment.
     */
    public String generateNewTan() {
        long rnd = random.nextLong();
        int radixLen = charSet.length;
        StringBuilder tan = new StringBuilder();
        while (tan.length() < TAN_LENGHT) {
            if (rnd == 0) {
                rnd = random.nextLong();
                continue;
            }
            tan.append(charSet[Math.abs((int) (rnd % radixLen))]);
            rnd /= radixLen;
        }
        return tan.toString();
    }

    /**
     * TODO comment.
     */
    public String hashTan(String tan) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(tan.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
