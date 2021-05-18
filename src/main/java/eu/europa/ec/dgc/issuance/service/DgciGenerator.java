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
import eu.europa.ec.dgc.issuance.utils.DgciUtil;
import java.util.UUID;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DgciGenerator {
    private final IssuanceConfigProperties issuanceConfigProperties;

    private static final String CODE_POINTS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789/:";

    /**
     * Check if dgci prefix contains character suitable for checksum calculation.
     */
    @PostConstruct
    public void checkPrefix() {
        String dgciPrefix = issuanceConfigProperties.getDgciPrefix();
        if (dgciPrefix != null) {
            for (int i = 0;i < dgciPrefix.length();i++) {
                if (CODE_POINTS.indexOf(dgciPrefix.charAt(i)) < 0) {
                    throw new IllegalArgumentException("configured DGCI prefix '"
                        + dgciPrefix + "' contains invalid character '"
                        + dgciPrefix.charAt(i) + "' only following are supported " + CODE_POINTS);
                }
            }
        }
    }

    /**
     * Generates a new DGCI.
     *
     * @return DGCI as String
     */
    public String newDgci() {
        StringBuilder sb = new StringBuilder();
        sb.append(issuanceConfigProperties.getDgciPrefix()).append(':');
        sb.append(DgciUtil.encodeDgci(UUID.randomUUID()));
        sb.append(generateCheckCharacter(sb.toString()));
        return sb.toString();
    }

    // see https://en.wikipedia.org/wiki/Luhn_mod_N_algorithm
    private char generateCheckCharacter(String input) {
        int factor = 2;
        int sum = 0;
        int n = CODE_POINTS.length();

        // Starting from the right and working leftwards is easier since
        // the initial "factor" will always be "2".
        for (int i = input.length() - 1; i >= 0; i--) {
            int codePoint = codePointFromCharacter(input.charAt(i));
            int addend = factor * codePoint;

            // Alternate the "factor" that each "codePoint" is multiplied by
            factor = (factor == 2) ? 1 : 2;

            // Sum the digits of the "addend" as expressed in base "n"
            addend = (addend / n) + (addend % n);
            sum += addend;
        }

        // Calculate the number that must be added to the "sum"
        // to make it divisible by "n".
        int remainder = sum % n;
        int checkCodePoint = (n - remainder) % n;

        return characterFromCodePoint(checkCodePoint);
    }

    private char characterFromCodePoint(int checkCodePoint) {
        return CODE_POINTS.charAt(checkCodePoint);
    }

    private int codePointFromCharacter(char charAt) {
        int codePoint = CODE_POINTS.indexOf(charAt);
        if (codePoint < 0) {
            throw new IllegalArgumentException("unsupported character for checksum: " + charAt);
        }
        return codePoint;
    }
}
