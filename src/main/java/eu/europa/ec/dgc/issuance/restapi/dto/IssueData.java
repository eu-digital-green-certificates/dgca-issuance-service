package eu.europa.ec.dgc.issuance.restapi.dto;

import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import lombok.Data;

@Data
public class IssueData {
    private String hash;
    private GreenCertificateType greenCertificateType;
}
