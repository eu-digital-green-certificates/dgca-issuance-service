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

package eu.europa.ec.dgc.issuance;

import com.nimbusds.jose.util.Base64URL;
import eu.europa.ec.dgc.issuance.utils.DgciUtil;
import eu.europa.ec.dgc.issuance.utils.HashUtil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class EncodingTest {

    public static final String TEST_TAN = "U7ULCYZY";
    public static final String TEST_TAN_HASHED = "avmGz38ugM7uBePwKKlvh3IB8+7O+WFhQEbjIxhTxgY=";

    public static final String TEST_UUID = "cd7737d4-51ca-45f8-9f74-3a173b9a1f47";
    public static final String TEST_DGCI_REP = "NW393C1D87A44870V7TTFQMYC";

    @Test
    public void testCreateSHA256Hash() throws Exception {
        String output = HashUtil.sha256Base64(TEST_TAN);
        assertEquals(TEST_TAN_HASHED, output);
    }

    @Test
    public void dgciEncoding() throws Exception {
        UUID uuid = UUID.fromString(TEST_UUID);
        String dgciRep = DgciUtil.encodeDgci(uuid);
        assertEquals(25, dgciRep.length());
        assertEquals(TEST_DGCI_REP, dgciRep);
    }

    @Test
    public void testBase64URL() throws Exception {
        String dgci= "URN:UVCI:V1:DE:NW513NLDH01JY3JCMU4M67WOHA";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dgci.getBytes(StandardCharsets.UTF_8));
        String hashBase64URL = Base64URL.encode(hash).toString();
        System.out.println(hashBase64URL);
        assertArrayEquals(hash,Base64URL.from(hashBase64URL).decode());
    }
}
