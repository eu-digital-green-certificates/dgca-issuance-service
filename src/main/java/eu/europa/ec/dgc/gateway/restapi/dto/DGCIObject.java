package eu.europa.ec.dgc.gateway.restapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DGCIObject {
  private long id;
  private String dgci;
}
