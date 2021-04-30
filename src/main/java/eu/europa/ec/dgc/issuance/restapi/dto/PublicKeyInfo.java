package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.Data;

@Data
public class PublicKeyInfo {
    private String kid;
    private int algid;
    private String keyType;
    private String publicKeyFormat;
    private String publicKeyEncoded;
}
