package eu.europa.ec.dgc.issuance.restapi.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Data;

@Data
public class IssueData {
    @NotBlank
    @Size(max = 512)
    private String hash;
}
