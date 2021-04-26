package eu.europa.ec.dgc.issuance.service;


import java.io.ByteArrayOutputStream;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.Arrays;

public class CopyDigest implements Digest
{
    private OpenByteArrayOutputStream bOut = new OpenByteArrayOutputStream();
    private SHA256Digest sha256Digest = new SHA256Digest();
    private boolean wasReset = false;

    public String getAlgorithmName()
    {
        return "NULL";
    }

    public int getDigestSize()
    {
        return 32;
    }

    public void update(byte in)
    {
        if (wasReset) {
            sha256Digest.update(in);
        } else {
            bOut.write(in);
        }
    }

    public void update(byte[] in, int inOff, int len)
    {
        if (wasReset) {
            sha256Digest.update(in,inOff,len);
        } else {
            bOut.write(in, inOff, len);
        }
    }

    public int doFinal(byte[] out, int outOff)
    {
        if (wasReset) {
            return sha256Digest.doFinal(out,outOff);
        } else {
            int size = bOut.size();
            bOut.copy(out, outOff);
            reset();
            return size;
        }
    }

    public void reset()
    {
        if (wasReset) {
            sha256Digest.reset();
        } else {
            if (bOut.size()>0) {
                wasReset = true;
                bOut.reset();
            }
        }
    }

    private static class OpenByteArrayOutputStream
            extends ByteArrayOutputStream
    {
        public void reset()
        {
            super.reset();

            Arrays.clear(buf);
        }

        void copy(byte[] out, int outOff)
        {
            System.arraycopy(buf, 0, out, outOff, this.size());
        }
    }
}
