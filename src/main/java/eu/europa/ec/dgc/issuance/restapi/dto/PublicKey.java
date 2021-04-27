package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.Data;

@Data
public class PublicKey {
    private String type;
    private String value;
}
