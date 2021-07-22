/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.issuance.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.util.Base64URL;
import com.upokecenter.cbor.CBORObject;
import com.upokecenter.cbor.CBORType;
import ehn.techiop.hcert.kotlin.chain.Base45Service;
import ehn.techiop.hcert.kotlin.chain.CborService;
import ehn.techiop.hcert.kotlin.chain.Chain;
import ehn.techiop.hcert.kotlin.chain.ChainResult;
import ehn.techiop.hcert.kotlin.chain.CompressorService;
import ehn.techiop.hcert.kotlin.chain.ContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.CoseService;
import ehn.techiop.hcert.kotlin.chain.CwtService;
import ehn.techiop.hcert.kotlin.chain.HigherOrderValidationService;
import ehn.techiop.hcert.kotlin.chain.SchemaValidationService;
import ehn.techiop.hcert.kotlin.data.GreenCertificate;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import eu.europa.ec.dgc.issuance.entity.DgciEntity;
import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import eu.europa.ec.dgc.issuance.repository.DgciRepository;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimRequest;
import eu.europa.ec.dgc.issuance.restapi.dto.ClaimResponse;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciIdentifier;
import eu.europa.ec.dgc.issuance.restapi.dto.DgciInit;
import eu.europa.ec.dgc.issuance.restapi.dto.DidAuthentication;
import eu.europa.ec.dgc.issuance.restapi.dto.DidDocument;
import eu.europa.ec.dgc.issuance.restapi.dto.EgdcCodeData;
import eu.europa.ec.dgc.issuance.restapi.dto.IssueData;
import eu.europa.ec.dgc.issuance.restapi.dto.SignatureData;
import eu.europa.ec.dgc.issuance.utils.HashUtil;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import kotlinx.serialization.SerializationException;
import kotlinx.serialization.json.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DgciService {
    private static final String ID_SEP = "_";

    public enum DgciStatus {
        EXISTS, NOT_EXISTS, LOCKED
    }

    private final DgciRepository dgciRepository;
    private final CertificateService certificateService;
    private final IssuanceConfigProperties issuanceConfigProperties;
    private final DgciGenerator dgciGenerator;
    private final HigherOrderValidationService higherOrderValidationService;
    private final CborService cborService;
    private final CoseService coseService;
    private final CwtService cwtService;
    private final ContextIdentifierService contextIdentifierService;
    private final CompressorService compressorService;
    private final Base45Service base45Service;
    private final SchemaValidationService schemaValidationService;
    private final ExpirationService expirationService;

    private static final int MAX_CLAIM_RETRY_TAN = 3;

    /**
     * Initializes new DGCI.
     *
     * @param dgciInit object with required parameters.
     * @return DGCI Identifier.
     */
    public DgciIdentifier initDgci(DgciInit dgciInit) {
        DgciEntity dgciEntity = new DgciEntity();
        String dgci = generateDgci();

        dgciEntity.setDgci(dgci);
        dgciEntity.setDgciHash(HashUtil.sha256Base64(dgci));
        dgciEntity.setGreenCertificateType(dgciInit.getGreenCertificateType());

        ZonedDateTime now = ZonedDateTime.now();
        Duration expirationDuration = expirationService.expirationForType(dgciInit.getGreenCertificateType());
        ZonedDateTime expiration = now.plus(expirationDuration);

        dgciEntity.setExpiresAt(expiration);
        dgciRepository.saveAndFlush(dgciEntity);

        log.debug("Initialized new certificate with ID '{}' and database ID '{}'.", dgci, dgciEntity.getId());

        long expirationSec = expiration.toInstant().getEpochSecond();
        byte[] dgciHash = Base64.getDecoder().decode(dgciEntity.getDgciHash());
        // We need Base64URL encoding because Base64 contains slashes that are not allowed
        // by tomcat
        String id = dgciEntity.getId().toString() + ID_SEP + Base64URL.encode(dgciHash);
        return new DgciIdentifier(
            id,
            dgci,
            certificateService.getKidAsBase64(),
            certificateService.getAlgorithmIdentifier(),
            issuanceConfigProperties.getCountryCode(),
            expirationSec,
            expirationDuration.get(ChronoUnit.SECONDS)
        );
    }

    @NotNull
    private String generateDgci() {
        return dgciGenerator.newDgci();
    }

    /**
     * finish DGCI.
     *
     * @param dgciId    id
     * @param issueData issueData
     * @return signature data
     */
    public SignatureData finishDgci(String dgciId, IssueData issueData) {
        log.debug("Finalizing certificate with ID '{}'.", dgciId);
        int colIdx = dgciId.indexOf(ID_SEP);
        if (colIdx < 0) {
            throw new WrongRequest("ID unknown");
        }
        long id = Long.parseLong(dgciId.substring(0,colIdx));
        byte[] dgciHash = Base64URL.from(dgciId.substring(colIdx + 1)).decode();
        String dgciHashBase64 = Base64.getEncoder().encodeToString(dgciHash);
        Optional<DgciEntity> dgciEntityOpt = dgciRepository.findById(id);
        if (dgciEntityOpt.isPresent()) {
            if (dgciEntityOpt.get().getCertHash() != null) {
                throw new DgciConflict("Already signed");
            }
            if (!dgciEntityOpt.get().getDgciHash().equals(dgciHashBase64)) {
                throw new DgciNotFound("DGCI not found (hash mismatch)");
            }
            var dgciEntity = dgciEntityOpt.get();
            Tan tan = Tan.create();
            dgciEntity.setHashedTan(tan.getHashedTan());
            dgciEntity.setCertHash(issueData.getHash());
            dgciRepository.saveAndFlush(dgciEntity);
            log.debug("Done finalizing certificate with ID '{}'. ", dgciId);
            String signatureBase64 = certificateService.signHash(issueData.getHash());
            return new SignatureData(tan.getRawTan(), signatureBase64);
        } else {
            log.warn("Cannot find certificate with ID '{}'.", dgciId);
            throw new DgciNotFound("Certificate with ID '" + dgciId + "' not found");
        }
    }

    /**
     * get did document.
     *
     * @param dgciHash dgciHash
     * @return didDocument
     */
    public DidDocument getDidDocument(String dgciHash) {
        Optional<DgciEntity> dgciEntityOpt = dgciRepository.findByDgciHash(dgciHash);
        if (dgciEntityOpt.isPresent()) {
            DgciEntity dgciEntity = dgciEntityOpt.get();
            DidDocument didDocument = new DidDocument();
            didDocument.setContext("https://w3id.org/did/v1");
            didDocument.setId(dgciEntity.getDgci());
            didDocument.setController(dgciEntity.getDgci());
            List<DidAuthentication> didAuthentications = new ArrayList<>();
            if (dgciEntity.isClaimed()) {
                DidAuthentication didAuthentication = new DidAuthentication();
                didAuthentication.setController(dgciEntity.getDgci());
                didAuthentication.setType("EcdsaSecp256r1VerificationKey2019");
                didAuthentication.setExpires(dgciEntity.getExpiresAt()
                    .toOffsetDateTime().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    JsonNode jwkNode = objectMapper.readTree(dgciEntity.getPublicKey());
                    didAuthentication.setPublicKeyJsw(jwkNode);
                } catch (JsonProcessingException e) {
                    log.error("data error, public key is not jwk json for dgci.id" + dgciEntity.getId());
                }
                didAuthentications.add(didAuthentication);
            }
            didDocument.setAuthentication(didAuthentications);
            return didDocument;
        } else {
            throw new DgciNotFound("can not find dgci with hash: " + dgciHash);
        }
    }

    /**
     * compute cose sign hash.
     *
     * @param coseMessage cose message
     * @return hash value
     */
    public byte[] computeCoseSignHash(byte[] coseMessage) {
        try {
            CBORObject coseForSign = CBORObject.NewArray();
            CBORObject cborCose = CBORObject.DecodeFromBytes(coseMessage);
            if (cborCose.getType() == CBORType.Array) {
                coseForSign.Add(CBORObject.FromObject("Signature1"));
                coseForSign.Add(cborCose.get(0).GetByteString());
                coseForSign.Add(new byte[0]);
                coseForSign.Add(cborCose.get(2).GetByteString());
            }
            byte[] coseForSignBytes = coseForSign.EncodeToBytes();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(coseForSignBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * claim dgci to wallet app.
     * means bind dgci with some public key from wallet app
     *
     * @param claimRequest claim request
     */
    public ClaimResponse claim(ClaimRequest claimRequest) {
        log.debug("Claim certificate with ID '{}'", claimRequest.getDgci());
        if (!verifySignature(claimRequest)) {
            throw new WrongRequest("Signature verification failed");
        }
        Optional<DgciEntity> dgciEntityOptional = dgciRepository.findByDgci(claimRequest.getDgci());
        if (dgciEntityOptional.isPresent()) {
            DgciEntity dgciEntity = dgciEntityOptional.get();
            if (dgciEntity.getRetryCounter() > MAX_CLAIM_RETRY_TAN) {
                throw new WrongRequest("Claim max try exceeded");
            }
            if (!dgciEntity.getCertHash().equals(claimRequest.getCertHash())) {
                throw new WrongRequest("Cert hash mismatch");
            }
            if (!dgciEntity.getHashedTan().equals(claimRequest.getTanHash())) {
                dgciEntity.setRetryCounter(dgciEntity.getRetryCounter() + 1);
                dgciRepository.saveAndFlush(dgciEntity);
                throw new WrongRequest("TAN mismatch");
            }
            if (!dgciEntity.isClaimed()) {
                ZonedDateTime tanExpireTime = dgciEntity.getCreatedAt()
                    .plus(issuanceConfigProperties.getTanExpirationHours());
                if (tanExpireTime.isBefore(ZonedDateTime.now())) {
                    throw new WrongRequest("TAN expired");
                }
            }
            dgciEntity.setClaimed(true);
            dgciEntity.setRetryCounter(dgciEntity.getRetryCounter() + 1);
            dgciEntity.setPublicKey(asJwk(claimRequest.getPublicKey()));
            Tan newTan = Tan.create();
            dgciEntity.setHashedTan(newTan.getHashedTan());
            dgciEntity.setRetryCounter(0);
            dgciRepository.saveAndFlush(dgciEntity);
            log.info("Certificate with ID '{}' successfully claimed.", dgciEntity.getDgci());

            ClaimResponse claimResponse = new ClaimResponse();
            claimResponse.setTan(newTan.getRawTan());
            return claimResponse;
        } else {
            log.warn("Cannot find certificate with ID '{}'", claimRequest.getDgci());
            throw new DgciNotFound("Cannot find DGCI: " + claimRequest.getDgci());
        }
    }

    private String asJwk(eu.europa.ec.dgc.issuance.restapi.dto.PublicKey publicKeyClaim) {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyClaim.getValue());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance(publicKeyClaim.getType());
        } catch (NoSuchAlgorithmException e) {
            throw new WrongRequest("key type not supported: '" + publicKeyClaim.getType()
                + "', try RSA or EC");
        }
        PublicKey publicKey;
        try {
            publicKey = kf.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new WrongRequest("invalid key");
        }
        String jwkString;
        if (publicKey instanceof RSAPublicKey) {
            RSAKey jwkKey = new RSAKey.Builder((RSAPublicKey) publicKey).build();
            jwkString = jwkKey.toJSONString();
        } else if (publicKey instanceof ECPublicKey) {
            ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
            Curve curve = Curve.forECParameterSpec(ecPublicKey.getParams());
            ECKey jwkKey = new ECKey.Builder(curve,ecPublicKey).build();
            jwkString = jwkKey.toJSONString();
        } else {
            throw new WrongRequest("unsupported key type");
        }
        return jwkString;
    }

    private boolean verifySignature(ClaimRequest claimRequest) {
        byte[] keyBytes = Base64.getDecoder().decode(claimRequest.getPublicKey().getValue());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance(claimRequest.getPublicKey().getType());
        } catch (NoSuchAlgorithmException e) {
            throw new WrongRequest("key type not supported: '" + claimRequest.getPublicKey().getType()
                + "', try RSA or EC");
        }
        PublicKey publicKey;
        try {
            publicKey = kf.generatePublic(spec);
        } catch (InvalidKeySpecException e) {
            throw new WrongRequest("invalid key");
        }
        Signature signature;
        try {
            signature = Signature.getInstance(claimRequest.getSigAlg());
        } catch (NoSuchAlgorithmException e) {
            throw new WrongRequest("signature algorithm not supported: '" + claimRequest.getSigAlg() + "'");
        }
        StringBuilder dataToSign = new StringBuilder();
        dataToSign.append(claimRequest.getTanHash())
            .append(claimRequest.getCertHash())
            .append(claimRequest.getPublicKey().getValue());
        try {
            signature.initVerify(publicKey);
            signature.update(dataToSign.toString().getBytes(StandardCharsets.UTF_8));
            byte[] sigBytes = Base64.getDecoder().decode(claimRequest.getSignature());
            return signature.verify(sigBytes);
        } catch (InvalidKeyException e) {
            throw new WrongRequest("invalid key for signature");
        } catch (SignatureException e) {
            throw new WrongRequest("can not validity signature", e);
        }
    }

    /**
     * Create edgc in backend.
     *
     * @param dccJson certificate
     * @return edgc qr code and tan
     */
    public EgdcCodeData createEdgc(String dccJson) {
        String dgci = dgciGenerator.newDgci();
        dccJson = updateCI(dccJson, dgci);

        GreenCertificate eudgc;
        try {
            eudgc = Json.Default.decodeFromString(GreenCertificate.Companion.serializer(), dccJson);
        } catch (SerializationException se) {
            throw new WrongRequest(se.getMessage());
        }
        GreenCertificateType greenCertificateType = GreenCertificateType.Vaccination;
        if (eudgc.getRecoveryStatements() != null) {
            for (val v : eudgc.getRecoveryStatements()) {
                greenCertificateType = GreenCertificateType.Recovery;
            }
        }
        if (eudgc.getTests() != null) {
            for (val v : eudgc.getTests()) {
                greenCertificateType = GreenCertificateType.Test;
            }
        }
        if (eudgc.getVaccinations() != null) {
            for (val v : eudgc.getVaccinations()) {
                greenCertificateType = GreenCertificateType.Vaccination;
            }
        }
        Chain cborProcessingChain =
            new Chain(higherOrderValidationService, schemaValidationService, cborService, cwtService, coseService,
                compressorService, base45Service, contextIdentifierService);
        ChainResult chainResult = cborProcessingChain.encode(eudgc);

        EgdcCodeData egdcCodeData = new EgdcCodeData();
        egdcCodeData.setQrCode(chainResult.getStep5Prefixed());
        egdcCodeData.setDgci(dgci);
        Tan ta = Tan.create();
        egdcCodeData.setTan(ta.getRawTan());

        DgciEntity dgciEntity = new DgciEntity();
        dgciEntity.setDgci(dgci);
        dgciEntity.setCertHash(Base64.getEncoder().encodeToString(computeCoseSignHash(chainResult.getStep2Cose())));
        dgciEntity.setDgciHash(HashUtil.sha256Base64(dgci));
        dgciEntity.setHashedTan(ta.getHashedTan());
        dgciEntity.setGreenCertificateType(greenCertificateType);
        dgciEntity.setCreatedAt(ZonedDateTime.now());
        dgciEntity.setExpiresAt(ZonedDateTime.now().plus(expirationService.expirationForType(greenCertificateType)));
        dgciRepository.saveAndFlush(dgciEntity);

        return egdcCodeData;
    }

    private String updateCI(String dccJson, String dgci) {
        // The fields can be not modified on GreenCertificate object so we need to set ci on json level
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode dccTree = mapper.readTree(dccJson);
            updateCI(dccTree, dgci);
            return mapper.writeValueAsString(dccTree);
        } catch (JsonProcessingException e) {
            throw new WrongRequest(e.getMessage());
        }
    }

    private void updateCI(JsonNode jsonNode, String dgci) {
        if (jsonNode.isObject()) {
            if (jsonNode.has("ci")) {
                ((ObjectNode)jsonNode).put("ci",dgci);
            } else {
                for (JsonNode value : jsonNode) {
                    updateCI(value, dgci);
                }
            }
        } else if (jsonNode.isArray()) {
            for (JsonNode item : jsonNode) {
                updateCI(item, dgci);
            }
        }
    }

    /**
     * Check if dgci exists.
     *
     * @param dgciHash dgci hash
     * @return DgciStatus
     */
    public DgciStatus checkDgciStatus(String dgciHash) {
        log.debug("Checking status of DGC with hash '{}'...", dgciHash);
        DgciStatus dgciStatus;
        Optional<DgciEntity> dgciEntity = dgciRepository.findByDgciHash(dgciHash);
        if (dgciEntity.isPresent()) {
            if (dgciEntity.get().isLocked()) {
                log.debug("DGC with hash '{}' is locked.", dgciHash);
                dgciStatus = DgciStatus.LOCKED;
            } else {
                log.debug("DGC with hash '{}' exists.", dgciHash);
                dgciStatus = DgciStatus.EXISTS;
            }
        } else {
            log.debug("DGC with hash '{}' does not exist.", dgciHash);
            dgciStatus = DgciStatus.NOT_EXISTS;
        }
        return dgciStatus;
    }
}
