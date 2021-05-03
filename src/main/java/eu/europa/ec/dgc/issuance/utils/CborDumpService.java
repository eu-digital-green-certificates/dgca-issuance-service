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

package eu.europa.ec.dgc.issuance.utils;

import com.upokecenter.cbor.CBORObject;
import java.io.IOException;
import java.io.Writer;
import org.springframework.stereotype.Service;

@Service
public class CborDumpService {

    public void dumpCbor(byte[] cb, Writer writer) throws IOException {
        CBORObject cborObject = CBORObject.DecodeFromBytes(cb);
        dumpCbor(cborObject, writer, 0);
    }

    private void dumpCbor(CBORObject cborObject, Writer writer, int ident) throws IOException {
        writer
            .append("")
            .append(String.valueOf(cborObject));
    }
}
