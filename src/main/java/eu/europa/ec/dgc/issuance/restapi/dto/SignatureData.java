package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignatureData {
    private String tan;
    private String signature;
}
