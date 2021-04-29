package eu.europa.ec.dgc.issuance.restapi.dto;

import eu.europa.ec.dgc.issuance.entity.GreenCertificateType;
import lombok.Data;

@Data
public class DgciInit {
    private GreenCertificateType greenCertificateType;
}
