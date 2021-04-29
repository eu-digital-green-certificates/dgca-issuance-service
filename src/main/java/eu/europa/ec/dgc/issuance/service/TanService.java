/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

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

    private final Random random = new SecureRandom();
    private final char[] charSet;
    private static final int TAN_LENGTH = 8;

    /**
     * Constructs TanService with Whitelist of allowed TAN-Chars.
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
     * Generates a new TAN.
     * The TAN has a length of 8 characters. The generated TAN does not include letter I and O.
     *
     * @return TAN String.
     */
    public String generateNewTan() {
        long rnd = random.nextLong();
        int radixLen = charSet.length;
        StringBuilder tan = new StringBuilder();
        while (tan.length() < TAN_LENGTH) {
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
