package eu.europa.ec.dgc.issuance.restapi.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class PublicKey {
    @NotBlank
    @Size(max=100)
    private String type;
    @NotBlank
    @Size(max=512)
    private String value;
}
