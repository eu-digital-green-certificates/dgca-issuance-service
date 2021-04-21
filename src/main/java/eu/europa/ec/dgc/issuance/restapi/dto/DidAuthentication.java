package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.Data;

@Data
public class DidAuthentication {
    private String type;
    private String controller;
    // TODO use data type and ISO Date formater
    private String expires;
    private String publicKeyBase58;
}
