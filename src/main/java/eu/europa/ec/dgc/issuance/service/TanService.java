package eu.europa.ec.dgc.issuance.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TanService {
    public String generateNewTan() {
        // TODO how generate new TAN
        return UUID.randomUUID().toString();
    }

    public String hashTan(String tan) {
        // TODO make tan hash
        return tan;
    }
}
