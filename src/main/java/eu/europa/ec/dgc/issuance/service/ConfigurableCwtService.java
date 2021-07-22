package eu.europa.ec.dgc.issuance.service;

import com.upokecenter.cbor.CBORObject;
import ehn.techiop.hcert.kotlin.chain.CwtService;
import ehn.techiop.hcert.kotlin.chain.VerificationResult;
import ehn.techiop.hcert.kotlin.crypto.CwtHeaderKeys;
import ehn.techiop.hcert.kotlin.data.CborObject;
import ehn.techiop.hcert.kotlin.data.GreenCertificate;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import kotlinx.serialization.json.Json;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigurableCwtService implements CwtService {
    private final ExpirationService expirationService;
    private final IssuanceConfigProperties issuanceConfigProperties;

    @NotNull
    @Override
    public CborObject decode(@NotNull byte[] bytes, @NotNull VerificationResult verificationResult) {
        throw new UnsupportedOperationException("decoding not supported");
    }

    @NotNull
    @Override
    public byte[] encode(@NotNull byte[] bytes) {
        CBORObject cwtMap = CBORObject.NewMap();
        cwtMap.Add(CwtHeaderKeys.ISSUER.getIntVal(), issuanceConfigProperties.getCountryCode());
        CBORObject dcc = CBORObject.DecodeFromBytes(bytes);
        GreenCertificate greenCertificate = Json.Default.decodeFromString(GreenCertificate.Companion.serializer(),
            dcc.ToJSONString());
        ExpirationService.CwtTimeFields cwtTimes = expirationService.calculateCwtExpiration(greenCertificate);

        cwtMap.Add(CwtHeaderKeys.ISSUED_AT.getIntVal(), cwtTimes.issuedAt);
        cwtMap.Add(CwtHeaderKeys.EXPIRATION.getIntVal(), cwtTimes.expiration);
        CBORObject hcertMap = CBORObject.NewMap();
        hcertMap.Add(CwtHeaderKeys.EUDGC_IN_HCERT.getIntVal(),dcc);
        cwtMap.Add(CwtHeaderKeys.HCERT.getIntVal(), hcertMap);
        return cwtMap.EncodeToBytes();
    }


}
