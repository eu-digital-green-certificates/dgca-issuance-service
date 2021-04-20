package eu.europa.ec.dgc.gateway.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "tan")
public class TanEntity {

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
