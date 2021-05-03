package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ehn.techiop.hcert.data.Eudgc;
import ehn.techiop.hcert.kotlin.chain.SampleData;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.EgdcCodeData;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
public class DgciServiceTest {
    @Autowired
    public DgciService dgciService;

    @Autowired
    public IssuanceConfigProperties issuanceConfigProperties;

    @Autowired
    public DgciRepository dgciRepository;

    @Test
    public void testDGCIInit() throws Exception {
        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Vaccination);
        DgciIdentifier dgciIdentifier = dgciService.initDgci(dgciInit);
        assertNotNull(dgciIdentifier.getDgci());
        assertTrue(dgciIdentifier.getDgci().startsWith(issuanceConfigProperties.getDgciPrefix()));
    }

    @Test
    public void testDGCISign() throws Exception {
        DgciInit dgciInit = new DgciInit();
        dgciInit.setGreenCertificateType(GreenCertificateType.Vaccination);
        DgciIdentifier dgciIdentifier = dgciService.initDgci(dgciInit);
        assertNotNull(dgciIdentifier.getDgci());
        assertTrue(dgciIdentifier.getDgci().startsWith(issuanceConfigProperties.getDgciPrefix()));

        IssueData issueData = new IssueData();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        issueData.setHash(Base64.getEncoder().encodeToString(digest.digest("test".getBytes())));

        SignatureData signatureData = dgciService.finishDgci(dgciIdentifier.getId(), issueData);
        assertNotNull(signatureData.getSignature());
        assertNotNull(signatureData.getTan());
        assertEquals(8,signatureData.getTan().length());
    }

    @Test
    public void testCreateEdgcBackend() throws Exception {
        String vacDataJson = SampleData.Companion.getVaccination();
        ObjectMapper objectMapper = new ObjectMapper();
        Eudgc eudgc = objectMapper.readValue(vacDataJson,Eudgc.class);
        EgdcCodeData egdcCodeData = dgciService.createEdgc(eudgc);
        assertNotNull(egdcCodeData);
        assertNotNull(egdcCodeData.getQrcCode());
        Optional<DgciEntity> dgciEnitiyOpt = dgciRepository.findByDgci(egdcCodeData.getDgci());
        assertTrue(dgciEnitiyOpt.isPresent());
        assertEquals(GreenCertificateType.Vaccination,dgciEnitiyOpt.get().getGreenCertificateType());

    }

}
