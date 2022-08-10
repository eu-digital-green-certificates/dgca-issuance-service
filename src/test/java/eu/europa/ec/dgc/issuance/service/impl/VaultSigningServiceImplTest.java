package eu.europa.ec.dgc.issuance.service.impl;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.core.VaultTransitOperations;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.Signature;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("vault")
class VaultSigningServiceImplTest {

    @Autowired
    private VaultSigningServiceImpl vaultSigningService;
    @MockBean
    private VaultTemplate vaultTemplate;

    @Test
    void signHash() {
        byte[] hash = Base64.getDecoder().decode("dGVzdA==");

        VaultTransitOperations vaultTransitOperation = mock(VaultTransitOperations.class);
        when(vaultTemplate.opsForTransit()).thenReturn(vaultTransitOperation);
        when(vaultTransitOperation.sign("issuer-key", Plaintext.of("dGVzdA==")))
            .thenReturn(Signature.of("vault:v1:dGVzdA=="));

        byte[] result = vaultSigningService.signHash(hash, null);
        assertNotNull(result);
    }

    @Test
    void signHash_noVaultPrefix() {
        byte[] hash = Base64.getDecoder().decode("dGVzdA==");

        VaultTransitOperations vaultTransitOperation = mock(VaultTransitOperations.class);
        when(vaultTemplate.opsForTransit()).thenReturn(vaultTransitOperation);
        when(vaultTransitOperation.sign("issuer-key", Plaintext.of("dGVzdA==")))
            .thenReturn(Signature.of("dGVzdA=="));

        byte[] result = vaultSigningService.signHash(hash, null);
        assertNotNull(result);
    }

}
