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

package eu.europa.ec.dgc.issuance.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@Getter
@Setter
@ConfigurationProperties("issuance")
public class IssuanceConfigProperties {
    @NotBlank
    @Size(max = 20)
    private String dgciPrefix;
    private String keyStoreFile;
    private String keyStorePassword;
    private String certAlias;
    private String privateKeyPassword;
    @NotBlank
    @Size(max = 2)
    private String countryCode;
    @DurationUnit(ChronoUnit.HOURS)
    private Duration tanExpirationHours = Duration.ofHours(24);
    /**
     * JSON file that is provided to /context endpoint.
     */
    private String contextFile;
    @NotNull
    private Expiration expiration;

    @Getter
    @Setter
    public static class Expiration {
        @DurationUnit(ChronoUnit.DAYS)
        @NotNull
        private Duration vaccination;
        @DurationUnit(ChronoUnit.DAYS)
        @NotNull
        private Duration recovery;
        @DurationUnit(ChronoUnit.DAYS)
        @NotNull
        private Duration test;
    }

    @Getter
    @Setter
    @NotNull
    public static class Endpoints {
        private boolean frontendIssuing;
        private boolean backendIssuing;
        private boolean testTools;
        private boolean wallet;
        private boolean publishCert;
        private boolean did;
    }

}
