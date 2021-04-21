package eu.europa.ec.dgc.issuance.restapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Schema(
    name = "ProblemReport",
    type = "object",
    example = "{\n"
        + "\"code\":\"0x001\",\n"
        + "\"problem\":\"[PROBLEM]\",\n"
        + "\"sent value\":\"[Sent Value]\",\n"
        + "\"details\":\"...\"\n"
        + "}"
)
@Data
@AllArgsConstructor
public class ProblemReportDto {

    private String code;

    private String problem;

    private String sendValue;

    private String details;

}
