package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidAuthentication;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
        // TODO TAN fake
        return new SignatureData("34932382303049", signatureBase64);
    }

    /**
     * get did document.
     * @param opaque opaque
     * @param hash hash
     * @return didDocument
     */
    public DidDocument getDidDocument(String opaque, String hash) {
        DidDocument didDocument = new DidDocument();
        didDocument.setContext("https://w3id.org/did/v1");
        // TODO DID fake data
        didDocument.setId("dgc:V1:DE:xxxxxxxxx:34sdfmnn3434fdf89");
        didDocument.setController("did:web:ec.europa.eu/health/dgc/efdv34k34mdmdfj344");
        DidAuthentication didAuthentication = new DidAuthentication();
        didAuthentication.setController("dgc:V1:DE:xxxxxxxxx:34sdfmnn3434fdf89");
        didAuthentication.setType("EcdsaSecp256k1VerificationKey2018");
        didAuthentication.setExpires("2017-02-08T16:02:20Z");
        // TODO use base58 here
        didAuthentication.setPublicKeyBase58(Base64.getEncoder().encodeToString(certificateService.publicKey()));

        List<DidAuthentication> didAuthentications = new ArrayList<>();
        didAuthentications.add(didAuthentication);
        didDocument.setAuthentication(didAuthentications);
        return didDocument;
    }
}
