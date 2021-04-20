package eu.europa.ec.dgc.gateway.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Data
@Entity
@Table(name = "dgci")
public class DGCIEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @Column(name = "id")
  private Long id;

  /**
   * Timestamp of the Record creation.
   */
  @Column(name = "timestamp_created", nullable = false)
  private ZonedDateTime createdAt = ZonedDateTime.now();
}
