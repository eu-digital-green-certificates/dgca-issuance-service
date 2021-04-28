package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DgciGenerator {
    private final IssuanceConfigProperties issuanceConfigProperties;

    /**
     * new gdci.
     * @return dgci
     */
    public String newDgci() {
        StringBuilder sb = new StringBuilder();
        sb.append(issuanceConfigProperties.getDgciPrefix()).append(':').append(UUID.randomUUID());
        String checkSum = createDgciCheckSum(sb.toString());
        sb.append(':').append(checkSum);
        return sb.toString();
    }

    private String createDgciCheckSum(String dgciRaw) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashBytes = digest.digest(dgciRaw.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hashBytes, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
