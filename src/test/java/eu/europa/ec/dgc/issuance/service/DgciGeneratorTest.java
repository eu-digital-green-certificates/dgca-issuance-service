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
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DgciGeneratorTest {
    @Test
    public void testGenerateDGCI() throws Exception {
        IssuanceConfigProperties issuanceConfigProperties = new IssuanceConfigProperties();
        issuanceConfigProperties.setDgciPrefix("URN:UVCI:V1:DE");
        DgciGenerator dgciGenerator = new DgciGenerator(issuanceConfigProperties);
        String dgci = dgciGenerator.newDgci();
        assertNotNull(dgci);
        assertTrue(dgci.startsWith(issuanceConfigProperties.getDgciPrefix()));
        assertTrue("dgci too long",dgci.length() <= 50);
        System.out.println(dgci);
    }
}
