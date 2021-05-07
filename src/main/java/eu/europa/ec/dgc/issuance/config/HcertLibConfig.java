package eu.europa.ec.dgc.issuance.config;

import ehn.techiop.hcert.kotlin.chain.Base45Service;
import ehn.techiop.hcert.kotlin.chain.CompressorService;
import ehn.techiop.hcert.kotlin.chain.ContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.CoseService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultBase45Service;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCompressorService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultContextIdentifierService;
import ehn.techiop.hcert.kotlin.chain.impl.DefaultCoseService;
import eu.europa.ec.dgc.issuance.service.EhdCryptoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HcertLibConfig {

    private final EhdCryptoService ehdCryptoService;

    @Bean
    CoseService coseService() {
        return new DefaultCoseService(ehdCryptoService);
    }

    @Bean
    ContextIdentifierService contextIdentifierService() {
        return new DefaultContextIdentifierService();
    }

    @Bean
    CompressorService compressorService() {
        return new DefaultCompressorService();
    }

    @Bean
    Base45Service base45Service() {
        return new DefaultBase45Service();
    }

}
