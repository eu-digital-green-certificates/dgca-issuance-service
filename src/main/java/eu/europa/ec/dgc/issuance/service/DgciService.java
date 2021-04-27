package eu.europa.ec.dgc.issuance.service;

import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimResponse;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidAuthentication;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DgciService {
    private final DgciRepository dgciRepository;
    private final TanService tanService;
    private final CertificateService certificateService;
    private final IssuanceConfigProperties issuanceConfigProperties;

    /**
     * init dbgi.
     *
     * @param dgciInit data
     * @return dgci
     */
    public DgciIdentifier initDgci(DgciInit dgciInit) {
        DgciEntity dgciEntity = new DgciEntity();
        String dgci = generateDgci();
        dgciEntity.setDgci(dgci);
        dgciRepository.saveAndFlush(dgciEntity);
        log.info("init dgci: "+dgci+ " id: "+dgciEntity.getId());
        return new DgciIdentifier(dgciEntity.getId(), dgci, certificateService.getKidAsBase64(), certificateService.getAlgId(),issuanceConfigProperties.getCountryCode());
    }

    @NotNull
    private String generateDgci() {
        return issuanceConfigProperties.getDgciPrefix() + UUID.randomUUID().toString();
    }

    /**
     * finish DGCI.
     *
     * @param dgciId    id
     * @param issueData issueData
     * @return signature data
     */
    public SignatureData finishDgci(long dgciId, IssueData issueData) throws Exception {
        Optional<DgciEntity> dgciEntityOpt = dgciRepository.findById(dgciId);
        if (dgciEntityOpt.isPresent()) {
            var dgciEntity = dgciEntityOpt.get();
            String signatureBase64 = certificateService.signHash(issueData.getHash());
            String tan = tanService.generateNewTan();
            dgciEntity.setHashedTan(tanService.hashTan(tan));
            dgciEntity.setGreenCertificateType(issueData.getGreenCertificateType());
            dgciEntity.setCertHash(signatureBase64);
            dgciRepository.saveAndFlush(dgciEntity);
            log.info("signed for "+dgciId);
            return new SignatureData(tan, signatureBase64);
        } else {
            log.warn("can not find dgci with id "+dgciId);
            throw new DGCINotFound("dgci with id "+dgciId+ " not found");
        }
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

    public ClaimResponse claimUpdate(ClaimRequest claimRequest) {
        ClaimResponse claimResponse = new ClaimResponse();
        // TODO wallet claim post (update?)
        throw new RuntimeException("not implemented yet");
        // return claimResponse;
    }

    public ClaimResponse claim(ClaimRequest claimRequest) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeySpecException, InvalidKeyException {
        if (!verifySignature(claimRequest)) {
            throw new WrongRequest("signature verification failed");
        }
        Optional<DgciEntity> dgciEntityOptional = dgciRepository.findByDgci(claimRequest.getDgci());
        if (dgciEntityOptional.isPresent()) {
            DgciEntity dgciEntity = dgciEntityOptional.get();
            if (!dgciEntity.getCertHash().equals(claimRequest.getCertHash())) {
                throw new WrongRequest("cert hash mismatch");
            }
            if (!dgciEntity.getHashedTan().equals(claimRequest.getTanHash())) {
                throw new WrongRequest("tan mismatch");
            }
            if (dgciEntity.isClaimed()) {
                throw new WrongRequest("already claimed");
            }
            dgciEntity.setClaimed(true);
            dgciEntity.setRetryCounter(dgciEntity.getRetryCounter()+1);
            dgciEntity.setPublicKey(claimRequest.getPublicKey().getValue());
            dgciEntity.setHashedTan(null);
        } else {
            log.info("can not find dgci "+claimRequest.getDgci());
            throw new DGCINotFound("can not find dgci: "+claimRequest.getDgci());
        }
        ClaimResponse claimResponse = new ClaimResponse();

        return claimResponse;
    }

    private boolean verifySignature(ClaimRequest claimRequest) throws IOException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(claimRequest.getDgci().getBytes());
        bos.write(claimRequest.getTanHash().getBytes());
        byte[] keyBytes = Base64.getDecoder().decode(claimRequest.getPublicKey().getValue());
        bos.write(keyBytes);
        // TODO type or types of tan signature of claim now SHA256WithRSA only
        Signature signature = Signature.getInstance("SHA256WithRSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);
        signature.initVerify(publicKey);
        signature.update(bos.toByteArray());
        byte[] sigBytes = Base64.getDecoder().decode(claimRequest.getSignature());
        return signature.verify(sigBytes);

    }
}
