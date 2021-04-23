package eu.europa.ec.dgc.issuance.utils;

import com.upokecenter.cbor.CBORObject;
import java.io.IOException;
import java.io.Writer;

public class CBORDump {
    public void dumpCBOR(byte[] cb, Writer writer) throws IOException {
        CBORObject cborObject = CBORObject.DecodeFromBytes(cb);
        dumpCBOR(cborObject,writer, 0);
    }

    private void dumpCBOR(CBORObject cborObject, Writer writer, int ident) throws IOException {
        writer.append(""+cborObject);
    }
}
