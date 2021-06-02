package eu.europa.ec.dgc.issuance.restapi.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class EgcDecodeResult {
    private boolean validated;
    private String cborDump;
    private JsonNode cborJson;
    private String cborHex;
    private String cborBase64;
    private String coseHex;
    private String coseProtected;
    private JsonNode coseProtectedJson;
    private String coseUnprotected;
    private JsonNode coseUnprotectedJson;
    private String errorMessage;
}
