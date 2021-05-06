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

import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DgciGenerator {
    private final IssuanceConfigProperties issuanceConfigProperties;

    /**
     * Generates a new DGCI.
     *
     * @return DGCI as String
     */
    public String newDgci() {
        StringBuilder sb = new StringBuilder();
        sb.append(issuanceConfigProperties.getDgciPrefix()).append(':');
        // use uuid but encode to 0-9A-Z charset
        UUID uuid = UUID.randomUUID();
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        BigInteger bint = new BigInteger(1, bb.array());
        int radix = 10 + ('Z' - 'A');
        String randomUuidEncoded = bint.toString(radix).toUpperCase();
        sb.append(randomUuidEncoded);
        String checkSum = createDgciCheckSum(sb.toString());
        sb.append(':').append(checkSum);
        return sb.toString();
    }

    private String createDgciCheckSum(String dgciRaw) {
        BigInteger dgciRawAsNumber = new BigInteger(1, dgciRaw.getBytes(StandardCharsets.UTF_8));
        BigInteger modValue = dgciRawAsNumber.mod(BigInteger.valueOf(97));
        String checkSum = modValue.toString();
        if (checkSum.length() == 1) {
            checkSum = '0' + checkSum;
        }
        return checkSum;
    }
}
