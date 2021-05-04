package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.Data;

@Data
public class EgdcCodeData {
    String dgci;
    String qrcCode;
    String tan;
}
