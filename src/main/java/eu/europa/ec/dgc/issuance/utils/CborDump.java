package eu.europa.ec.dgc.issuance.utils;

import com.upokecenter.cbor.CBORObject;
import java.io.IOException;
import java.io.Writer;

public class CborDump {

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
