package eu.europa.ec.dgc.issuance.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DgciIdentifier {
    private long id;
    private String dgci;
    private String kid;
    private int algId;
}
