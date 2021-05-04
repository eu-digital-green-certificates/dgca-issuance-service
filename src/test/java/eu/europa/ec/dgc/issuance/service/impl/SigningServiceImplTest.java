package eu.europa.ec.dgc.issuance.service.impl;

import COSE.AlgorithmID;
import COSE.Attribute;
import COSE.CoseException;
import COSE.HeaderKeys;
import COSE.OneKey;
import COSE.Sign1Message;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import static org.junit.Assert.*;

public class SigningServiceImplTest {

    private OneKey key;
    private CBORObject payload;

    @Test
    public void testCompareCoseAndSplittedSignEC() throws Exception {
        payload = CBORObject.FromObject("Test");
        CBORObject kid = CBORObject.FromObject("kid".getBytes());
        key = OneKey.generateKey(AlgorithmID.ECDSA_256);
        Sign1Message sign1Message = new Sign1Message();
        sign1Message.SetContent(payload.EncodeToBytes());
        sign1Message.addAttribute(HeaderKeys.Algorithm, AlgorithmID.ECDSA_256.AsCBOR(), Attribute.PROTECTED);
        sign1Message.addAttribute(HeaderKeys.KID, kid, Attribute.PROTECTED);
        sign1Message.sign(key);
        byte[] coseSigned = sign1Message.EncodeToBytes();

        validataCoseBytes(coseSigned);

        // Own COSE Sign1 with

        CBORObject protectedAttributes = CBORObject.NewMap();
        protectedAttributes.set(HeaderKeys.Algorithm.AsCBOR(),AlgorithmID.ECDSA_256.AsCBOR());
        protectedAttributes.set(HeaderKeys.KID.AsCBOR(),kid);
        byte[] protectedBytes = protectedAttributes.EncodeToBytes();
        byte[] cosePayload = payload.EncodeToBytes();

        byte[] coseForSignBytes = computeToSignValue(protectedBytes, cosePayload);

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

        validataCoseBytes(ownCoseSigned);

        // Own Splitted Sign
        SigningServiceImpl signingService = new SigningServiceImpl();

        byte[] hashbytes = sha256(coseForSignBytes);
        byte[] signatureSplited = signingService.signHash(hashbytes, key.AsPrivateKey());

        CBORObject coseArray2 = CBORObject.NewArray();
        coseArray2.Add(protectedBytes);
        coseArray2.Add(CBORObject.NewMap());
        coseArray2.Add(cosePayload);
        coseArray2.Add(signatureSplited);

        CBORObject coseObject2 = CBORObject.FromObjectAndTag(coseArray2,18);
        byte[] ownCoseSigned2 = coseObject2.EncodeToBytes();

        validataCoseBytes(ownCoseSigned2);
    }

    @Test
    public void testCompareCoseAndSplittedSignRSA() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("crypto.policy", "unlimited");

        payload = CBORObject.FromObject("Test");
        CBORObject kid = CBORObject.FromObject("kid".getBytes());
        key = OneKey.generateKey(AlgorithmID.RSA_PSS_256);
        Sign1Message sign1Message = new Sign1Message();
        sign1Message.SetContent(payload.EncodeToBytes());
        sign1Message.addAttribute(HeaderKeys.Algorithm, AlgorithmID.RSA_PSS_256.AsCBOR(), Attribute.PROTECTED);
        sign1Message.addAttribute(HeaderKeys.KID, kid, Attribute.PROTECTED);
        sign1Message.sign(key);
        byte[] coseSigned = sign1Message.EncodeToBytes();


        validataCoseBytes(coseSigned);

        // Own splitted signing
        CBORObject protectedAttributes = CBORObject.NewMap();
        protectedAttributes.set(HeaderKeys.Algorithm.AsCBOR(),AlgorithmID.RSA_PSS_256.AsCBOR());
        protectedAttributes.set(HeaderKeys.KID.AsCBOR(),kid);
        byte[] protectedBytes = protectedAttributes.EncodeToBytes();
        byte[] cosePayload = payload.EncodeToBytes();

        byte[] coseForSignBytes = computeToSignValue(protectedBytes, cosePayload);

        SigningServiceImpl signingService = new SigningServiceImpl();

        byte[] hashbytes = sha256(coseForSignBytes);
        byte[] signatureSplited = signingService.signHash(hashbytes, key.AsPrivateKey());

        CBORObject coseArray2 = CBORObject.NewArray();
        coseArray2.Add(protectedBytes);
        coseArray2.Add(CBORObject.NewMap());
        coseArray2.Add(cosePayload);
        coseArray2.Add(signatureSplited);

        CBORObject coseObject2 = CBORObject.FromObjectAndTag(coseArray2,18);
        byte[] ownCoseSigned2 = coseObject2.EncodeToBytes();

        validataCoseBytes(ownCoseSigned2);

        byte[] bytesToSignFromCose = computeToSignValue(coseSigned);
        assertArrayEquals(coseForSignBytes,bytesToSignFromCose);

    }

    private byte[] sha256(byte[] coseForSignBytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(coseForSignBytes);
    }


    private byte[] computeToSignValue(byte[] protectedBytes, byte[] cosePayload) {
        CBORObject coseForSign = CBORObject.NewArray();
        coseForSign.Add(CBORObject.FromObject("Signature1"));
        coseForSign.Add(protectedBytes);
        coseForSign.Add(new byte[0]);
        coseForSign.Add(cosePayload);
        byte[] coseForSignBytes = coseForSign.EncodeToBytes();
        return coseForSignBytes;
    }

    public byte[] computeToSignValue(byte[] coseMessage) throws CoseException {
        CBORObject coseForSign = CBORObject.NewArray();
        CBORObject cborCose = CBORObject.DecodeFromBytes(coseMessage);
        if (cborCose.getType()== CBORType.Array) {
            coseForSign.Add(CBORObject.FromObject("Signature1"));
            coseForSign.Add(cborCose.get(0).GetByteString());
            coseForSign.Add(new byte[0]);
            coseForSign.Add(cborCose.get(2).GetByteString());
        }
        return coseForSign.EncodeToBytes();
    }

    private void validataCoseBytes(byte[] coseSigned) throws CoseException {
        Sign1Message messageDecoded = (Sign1Message) Sign1Message.DecodeFromBytes(coseSigned);
        CBORObject payloadDecoded = CBORObject.DecodeFromBytes(messageDecoded.GetContent());
        assertEquals(payloadDecoded.getType(), payload.getType());
        assertEquals(payloadDecoded.AsString(), payload.AsString());

        assertTrue(messageDecoded.validate(key));
    }

    // code taken from COSE library
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
