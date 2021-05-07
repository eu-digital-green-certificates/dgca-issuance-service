/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ---license-end
 */

package eu.europa.ec.dgc.issuance.entity;

import java.time.ZonedDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dgci", nullable = false, unique = true)
    private String dgci;

    @Column(name = "dgci_hash", nullable = false, unique = true, length = 512)
    private String dgciHash;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt = ZonedDateTime.now();

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "cert_hash", length = 512)
    private String certHash;

    @Column(name = "hashed_tan", length = 512)
    private String hashedTan;

    @Column(name = "green_certificate_type")
    @Enumerated(EnumType.STRING)
    private GreenCertificateType greenCertificateType;

    @Column(name = "retry_counter")
    private int retryCounter;

    @Column(name = "public_key", length = 1024)
    private String publicKey;

    @Column(name = "revoked")
    private boolean revoked;

    @Column(name = "claimed")
    private boolean claimed;

    @Column(name = "locked")
    private boolean locked;
}
