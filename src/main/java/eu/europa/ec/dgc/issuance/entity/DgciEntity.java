package eu.europa.ec.dgc.issuance.entity;

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
@Table(name = "dgci")
public class DgciEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false)
    private String dgci;

    @Column(name = "timestamp_created", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column
    private ZonedDateTime expires;

    @Column(length = 512)
    private String certHash;

    @Column(length = 512)
    private String hashedTan;

    @Column(name = "type")
    private GreenCertificateType greenCertificateType;

    @Column(name = "retry_coutner")
    private int retryCounter;

    @Column
    private boolean revoked;

    @Column
    private boolean claimed;

    @Column
    private boolean locked;
}
