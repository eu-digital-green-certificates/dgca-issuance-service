package eu.europa.ec.dgc.issuance.restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Data;

@Data
@JsonPropertyOrder({"@context", "id", "controller"})
public class DidDocument {
    @JsonProperty("@context")
    private String context;
    private String id;
    private String controller;
    private List<DidAuthentication> authentication;
}
