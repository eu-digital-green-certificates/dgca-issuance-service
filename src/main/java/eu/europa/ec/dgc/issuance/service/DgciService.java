package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DgciService {
    private final DgciRepository dgciRepository;
    private final TanService tanService;
    private final CertificateService certificateService;

    /**
     * init dbgi.
     *
     * @param dgciInit data
     * @return dgci
     */
    public DgciIdentifier initDgci(DgciInit dgciInit) {
        DgciEntity dgciEntity = new DgciEntity();
        dgciRepository.saveAndFlush(dgciEntity);
        // TODO how create the DGCI identifier
        return new DgciIdentifier(dgciEntity.getId(), "did:web:authority:dgci:V1:DE:blabla:" + dgciEntity.getId());
    }

    /**
     * finish DGCI.
     *
     * @param dgciId    id
     * @param issueData issueData
     * @return signature data
     */
    public SignatureData finishDgci(long dgciId, IssueData issueData) throws Exception {
        String signatureBase64 = certificateService.signHash(issueData.getHash());
        return new SignatureData("34932382303049", signatureBase64);
    }
}
