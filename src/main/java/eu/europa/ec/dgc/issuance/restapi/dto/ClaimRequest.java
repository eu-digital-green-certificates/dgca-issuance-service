package eu.europa.ec.dgc.issuance.restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClaimRequest {
    @JsonProperty("DGCI")
    private String dgci;
    @JsonProperty("certhash")
    private String certHash;
    @JsonProperty("TANHash")
    private String tanHash;
    private PublicKey publicKey;
}
