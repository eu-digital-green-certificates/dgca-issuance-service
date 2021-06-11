package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.upokecenter.cbor.CBORObject;
import ehn.techiop.hcert.data.Eudgc;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCborService;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

/**
 * own cbor service.
 * The default one inject fixed country code and expiration period
 *
 */
@Service
@RequiredArgsConstructor
public class ConfigurableCborService extends DefaultCborService {
    public static final int ISSUER = 1;
    public static final int ISSUED_AT = 6;
    public static final int EXPIRATION = 4;
    public static final int HCERT = -260;
    public static final int HCERT_VERSION = 1;

    private final ExpirationService expirationService;
    private final IssuanceConfigProperties issuanceConfigProperties;

    @Override
    public byte[] encode(@NotNull Eudgc input) {
        byte[] cbor;
        try {
            cbor = new CBORMapper().writeValueAsBytes(input);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
        ExpirationService.CwtTimeFields cwtTimes = expirationService.calculateCwtExpiration(input);
        CBORObject coseContainer = CBORObject.NewMap();
        coseContainer.set(CBORObject.FromObject(ISSUER),
            CBORObject.FromObject(issuanceConfigProperties.getCountryCode()));
        coseContainer.set(CBORObject.FromObject(ISSUED_AT),CBORObject.FromObject(cwtTimes.getIssuedAt()));
        coseContainer.set(CBORObject.FromObject(EXPIRATION),CBORObject.FromObject(cwtTimes.getExpiration()));
        CBORObject hcert = CBORObject.NewMap();
        hcert.set(CBORObject.FromObject(HCERT_VERSION),CBORObject.DecodeFromBytes(cbor));
        coseContainer.set(CBORObject.FromObject(HCERT),hcert);
        return coseContainer.EncodeToBytes();
    }
}
