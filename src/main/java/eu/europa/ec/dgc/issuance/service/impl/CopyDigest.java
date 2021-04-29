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

package eu.europa.ec.dgc.issuance.service.impl;


import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.Arrays;

public class CopyDigest implements Digest {
    private final OpenByteArrayOutputStream binaryOut = new OpenByteArrayOutputStream();
    private final SHA256Digest sha256Digest = new SHA256Digest();
    private boolean wasReset = false;

    public String getAlgorithmName() {
        return "NULL";
    }

    public int getDigestSize() {
        return 32;
    }

    /**
     * Updates the Message Digest with one byte.
     *
     * @param in byte to update.
     */
    public void update(byte in) {
        if (wasReset) {
            sha256Digest.update(in);
        } else {
            binaryOut.write(in);
        }
    }

    /**
     * Updates the Message Digest with a byte array.
     *
     * @param in     byte array to insert
     * @param offset Offset
     * @param length length
     */
    public void update(byte[] in, int offset, int length) {
        if (wasReset) {
            sha256Digest.update(in, offset, length);
        } else {
            binaryOut.write(in, offset, length);
        }
    }

    /**
     * close the digest, producing the final digest value. The doFinal
     * call leaves the digest reset.
     *
     * @param out the array the digest is to be copied into.
     * @param offset the offset into the out array the digest is to start at.
     * @return size of output
     */
    public int doFinal(byte[] out, int offset) {
        if (wasReset) {
            return sha256Digest.doFinal(out, offset);
        } else {
            int size = binaryOut.size();
            binaryOut.copy(out, offset);
            reset();
            return size;
        }
    }

    /**
     * Resets the Message Digest.
     */
    public void reset() {
        if (wasReset) {
            sha256Digest.reset();
        } else {
            if (binaryOut.size() > 0) {
                wasReset = true;
                binaryOut.reset();
            }
        }
    }

    private static class OpenByteArrayOutputStream
        extends ByteArrayOutputStream {
        public synchronized void reset() {
            super.reset();

            Arrays.clear(buf);
        }

        void copy(byte[] out, int outOff) {
            System.arraycopy(buf, 0, out, outOff, this.size());
        }
    }
}
