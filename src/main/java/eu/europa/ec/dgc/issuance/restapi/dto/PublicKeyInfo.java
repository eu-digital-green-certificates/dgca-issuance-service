package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PublicKeyInfo {
    private String kid;
    private int algid;
    private String keyType;
    private String publicKeyFormat;
    private String publicKeyEncoded;
}
