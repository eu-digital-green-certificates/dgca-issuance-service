package eu.europa.ec.dgc.issuance.service.impl;

import COSE.AlgorithmID;
import COSE.Attribute;
import COSE.CoseException;
import COSE.HeaderKeys;
import COSE.OneKey;
import COSE.Sign1Message;
import com.upokecenter.cbor.CBORObject;
import java.security.MessageDigest;
import java.security.Signature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SigningServiceImplTest {
    @Test
    public void testCompareCoseAndSplittedSignEC() throws Exception {
        CBORObject payload = CBORObject.FromObject("Test");
        CBORObject kid = CBORObject.FromObject("kid".getBytes());
        OneKey key = OneKey.generateKey(AlgorithmID.ECDSA_256);
        Sign1Message sign1Message = new Sign1Message();
        sign1Message.SetContent(payload.EncodeToBytes());
        sign1Message.addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED);
        sign1Message.addAttribute(HeaderKeys.KID, kid, Attribute.PROTECTED);
        sign1Message.sign(key);
        byte[] coseSigned = sign1Message.EncodeToBytes();

        Sign1Message messageDecoded = (Sign1Message) Sign1Message.DecodeFromBytes(coseSigned);
        CBORObject payloadDecoded = CBORObject.DecodeFromBytes(messageDecoded.GetContent());
        assertEquals(payloadDecoded.getType(),payload.getType());
        assertEquals(payloadDecoded.AsString(),payload.AsString());

        assertTrue(messageDecoded.validate(key));

        // Own COSE Sign1

        CBORObject protectedAttributes = CBORObject.NewMap();
        protectedAttributes.set(HeaderKeys.Algorithm.AsCBOR(),AlgorithmID.ECDSA_256.AsCBOR());
        protectedAttributes.set(HeaderKeys.KID.AsCBOR(),kid);
        byte[] protectedBytes = protectedAttributes.EncodeToBytes();
        byte[] cosePayload = payload.EncodeToBytes();

        CBORObject coseForSign = CBORObject.NewArray();
        coseForSign.Add(CBORObject.FromObject("Signature1"));
        coseForSign.Add(protectedBytes);
        coseForSign.Add(new byte[0]);
        coseForSign.Add(cosePayload);
        byte[] coseForSignBytes = coseForSign.EncodeToBytes();

        Signature sig = Signature.getInstance("SHA256withECDSA");
        sig.initSign(key.AsPrivateKey());
        sig.update(coseForSignBytes);
        byte[] signature = convertDerToConcat(sig.sign(),32);

        CBORObject coseArray = CBORObject.NewArray();
        coseArray.Add(protectedBytes);
        coseArray.Add(CBORObject.NewMap());
        coseArray.Add(cosePayload);
        coseArray.Add(signature);

        CBORObject coseObject = CBORObject.FromObjectAndTag(coseArray,18);
        byte[] ownCoseSigned = coseObject.EncodeToBytes();

        Sign1Message messageDecodedOwn = (Sign1Message) Sign1Message.DecodeFromBytes(ownCoseSigned);
        CBORObject payloadDecodedOwn = CBORObject.DecodeFromBytes(messageDecodedOwn.GetContent());
        assertEquals(payloadDecodedOwn.getType(),payload.getType());
        assertEquals(payloadDecodedOwn.AsString(),payload.AsString());

        assertTrue("own sign failed",messageDecodedOwn.validate(key));

        // Own Splitted Sign
        SigningServiceImpl signingService = new SigningServiceImpl();

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashbytes = digest.digest(coseForSignBytes);
        byte[] signatureSplited = signingService.signHash(hashbytes,key.AsPrivateKey());

        CBORObject coseArray2 = CBORObject.NewArray();
        coseArray2.Add(protectedBytes);
        coseArray2.Add(CBORObject.NewMap());
        coseArray2.Add(cosePayload);
        coseArray2.Add(signatureSplited);

        CBORObject coseObject2 = CBORObject.FromObjectAndTag(coseArray2,18);
        byte[] ownCoseSigned2 = coseObject2.EncodeToBytes();

        Sign1Message messageDecodedOwn2 = (Sign1Message) Sign1Message.DecodeFromBytes(ownCoseSigned2);
        CBORObject payloadDecodedOwn2 = CBORObject.DecodeFromBytes(messageDecodedOwn2.GetContent());
        assertEquals(payloadDecodedOwn2.getType(),payload.getType());
        assertEquals(payloadDecodedOwn2.AsString(),payload.AsString());

        assertTrue("own splitted sign failed",messageDecodedOwn2.validate(key));
    }

    private static byte[] convertDerToConcat(byte[] der, int len) throws CoseException {
        // this is far too naive
        byte[] concat = new byte[len * 2];

        // assumes SEQUENCE is organized as "R + S"
        int kLen = 4;
        if (der[0] != 0x30) {
            throw new CoseException("Unexpected signature input");
        }
        if ((der[1] & 0x80) != 0) {
            // offset actually 4 + (7-bits of byte 1)
            kLen = 4 + (der[1] & 0x7f);
        }

        // calculate start/end of R
        int rOff = kLen;
        int rLen = der[rOff - 1];
        int rPad = 0;
        if (rLen > len) {
            rOff += (rLen - len);
            rLen = len;
        } else {
            rPad = (len - rLen);
        }
        // copy R
        System.arraycopy(der, rOff, concat, rPad, rLen);

        // calculate start/end of S
        int sOff = rOff + rLen + 2;
        int sLen = der[sOff - 1];
        int sPad = 0;
        if (sLen > len) {
            sOff += (sLen - len);
            sLen = len;
        } else {
            sPad = (len - sLen);
        }
        // copy S
        System.arraycopy(der, sOff, concat, len + sPad, sLen);

        return concat;
    }
}