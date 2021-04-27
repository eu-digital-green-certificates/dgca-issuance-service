package eu.europa.ec.dgc.issuance.restapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class ClaimRequest {
    @JsonProperty("DGCI")
    @NotBlank
    @Size(max=100)
    private String dgci;
    @NotBlank
    @Size(max=100)
    @JsonProperty("certhash")
    private String certHash;
    @NotBlank
    @Size(max=100)
    @JsonProperty("TANHash")
    private String tanHash;
    @NotNull
    private PublicKey publicKey;
    @NotBlank
    private String signature;
}
