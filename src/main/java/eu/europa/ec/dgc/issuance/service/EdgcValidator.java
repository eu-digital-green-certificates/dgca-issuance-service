package eu.europa.ec.dgc.issuance.service;

import COSE.CoseException;
import COSE.KeyKeys;
import COSE.Message;
import COSE.OneKey;
import COSE.Sign1Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORException;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import ehn.techiop.hcert.kotlin.chain.Base45Service;
import ehn.techiop.hcert.kotlin.chain.CompressorService;
import ehn.techiop.hcert.kotlin.chain.ContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.VerificationResult;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultBase45Service;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCompressorService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultContextIdentifierService;
import eu.europa.ec.dgc.issuance.restapi.dto.EgcDecodeResult;
import eu.europa.ec.dgc.issuance.utils.CborDumpService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.val;
import org.bouncycastle.util.encoders.Hex;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EdgcValidator {
    private final CertificateService certificateService;
    private final CborDumpService cborDumpService;

    /**
     * decode and validate edgc.
     * Validates edgc and provided detailed information if the validation fails.
     * It provided interim result data for each processing step.
     * @param prefixedEncodedCompressedCose prefixed and encoded coso container
     * @return validation result
     */
    public EgcDecodeResult decodeEdgc(String prefixedEncodedCompressedCose) {
        VerificationResult verificationResult = new VerificationResult();
        ContextIdentifierService contextIdentifierService = new DefaultContextIdentifierService();
        Base45Service base45Service = new DefaultBase45Service();
        CompressorService compressorService = new DefaultCompressorService();
        val plainInput = contextIdentifierService.decode(prefixedEncodedCompressedCose, verificationResult);
        val compressedCose = base45Service.decode(plainInput, verificationResult);

        EgcDecodeResult egcDecodeResult = new EgcDecodeResult();
        StringBuilder errorMessages = new StringBuilder();
        try {
            val cose = compressorService.decode(compressedCose, verificationResult);
            egcDecodeResult.setCoseHex(Hex.toHexString(cose));
            Message message = Message.DecodeFromBytes(cose);
            if ((message instanceof Sign1Message)) {
                Sign1Message singn1Message = (Sign1Message)message;
                validateSignature(egcDecodeResult, errorMessages, singn1Message);
            } else {
                errorMessages.append("not Sign1 cose message");
            }
            StringWriter stringWriter = new StringWriter();
            cborDumpService.dumpCbor(message.GetContent(), stringWriter);
            egcDecodeResult.setCborDump(stringWriter.getBuffer().toString());
            egcDecodeResult.setCborHex(Hex.toHexString(message.GetContent()));

            CBORObject certData = CBORObject.DecodeFromBytes(message.GetContent());
            egcDecodeResult.setCborJson(cborToJson(certData));

            CBORObject protectedHeader = message.getProtectedAttributes();
            egcDecodeResult.setCoseProtected(protectedHeader.toString());
            egcDecodeResult.setCoseProtectedJson(cborToJson(protectedHeader));

            CBORObject unprotectedHeader = message.getUnprotectedAttributes();
            egcDecodeResult.setCoseUnprotected(unprotectedHeader.toString());
            egcDecodeResult.setCoseUnprotectedJson(cborToJson(unprotectedHeader));

            validateCosePayload(errorMessages, certData);
        } catch (CBORException cborException) {
            errorMessages.append("CBOR decode exception: ").append(cborException.getMessage());
        } catch (Exception exception) {
            // We try to provide as much usable information as possible even
            // if we do not know the specific exception here
            errorMessages.append("Decode exception: ").append(exception.getMessage());
        }
        if (errorMessages.length() > 0) {
            egcDecodeResult.setErrorMessage(errorMessages.toString());
        }

        return egcDecodeResult;
    }

    private void validateSignature(EgcDecodeResult egcDecodeResult,
                                   StringBuilder errorMessages, Sign1Message message)  {
        try {
            OneKey oneKey = getOneKeyForValidation();
            egcDecodeResult.setValidated(message.validate(oneKey));
        } catch (CoseException coseException) {
            errorMessages.append("COSE Validation error: ")
                .append((coseException.getCause() != null
                    ? coseException.getCause().getMessage() : coseException.getMessage()));
        }
    }

    private JsonNode cborToJson(CBORObject cborObject) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CBORObject.WriteJSON(cborObject, bos);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(bos.toByteArray());
    }

    private void validateCosePayload(StringBuilder errorMessages, CBORObject certData) {
        if (certData.getType() == CBORType.Map) {
            checkElemType(certData, 1, CBORType.TextString, errorMessages, "issuer");
            checkElemType(certData, 6, CBORType.Integer, errorMessages, "issued at");
            checkElemType(certData, 4, CBORType.Integer, errorMessages, "expiration");
            CBORObject hcert = checkElemType(certData, -260, CBORType.Map, errorMessages, "hcert");
            if (hcert != null) {
                checkElemType(hcert, 1, CBORType.Map, errorMessages, "v1");
                // CBORObject v1 =
                // TODO: validate hcert v1 against schema
            }
        } else {
            errorMessages.append("cose payload is not a Map");
        }
    }

    @NotNull
    private OneKey getOneKeyForValidation() throws CoseException {
        CBORObject map = CBORObject.NewMap();
        OneKey oneKey;
        if (certificateService.getPublicKey() instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) certificateService.getPublicKey();
            map.set(KeyKeys.KeyType.AsCBOR(), KeyKeys.KeyType_RSA);
            map.set(KeyKeys.RSA_N.AsCBOR(), stripLeadingZero(rsaPublicKey.getModulus()));
            map.set(KeyKeys.RSA_E.AsCBOR(), stripLeadingZero(rsaPublicKey.getPublicExponent()));
            oneKey = new OneKey(map);
        } else {
            ECPublicKey ecPublicKey = (ECPublicKey) certificateService.getPublicKey();
            map.set(KeyKeys.KeyType.AsCBOR(), KeyKeys.KeyType_EC2);
            map.set(KeyKeys.EC2_Curve.AsCBOR(), getEcCurve(ecPublicKey));
            map.set(KeyKeys.EC2_X.AsCBOR(), stripLeadingZero(ecPublicKey.getW().getAffineX()));
            map.set(KeyKeys.EC2_Y.AsCBOR(), stripLeadingZero(ecPublicKey.getW().getAffineY()));
            oneKey = new OneKey(map);
        }
        return oneKey;
    }

    private CBORObject checkElemType(CBORObject certData, int key, CBORType valueType,
                                     StringBuilder errorMessages, String objectName) {
        CBORObject cborValue = certData.get(key);
        if (cborValue == null) {
            errorMessages.append("missing " + objectName + " key: " + key);
            cborValue = null;
        } else {
            if (cborValue.getType() != valueType) {
                errorMessages.append("wrong type of: " + objectName + " is: "
                    + cborValue.getType() + " expected: " + valueType);
                cborValue = null;
            }
        }
        return cborValue;
    }

    private CBORObject getEcCurve(ECPublicKey publicKey) {
        CBORObject keyKeys;
        switch (publicKey.getParams().getOrder().bitLength()) {
            case 384:
                keyKeys = KeyKeys.EC2_P384;
                break;
            case 256:
                keyKeys = KeyKeys.EC2_P256;
                break;
            default:
                throw new IllegalArgumentException("unsupported EC curveSize");
        }
        return keyKeys;
    }

    private CBORObject stripLeadingZero(BigInteger input) {
        val bytes = input.toByteArray();
        byte[] stripped;

        if (bytes.length % 8 != 0 && bytes[0] == 0x00) {
            stripped = Arrays.copyOfRange(bytes, 1, bytes.length);
        } else {
            stripped = bytes;
        }
        return CBORObject.FromObject(stripped);
    }

}
