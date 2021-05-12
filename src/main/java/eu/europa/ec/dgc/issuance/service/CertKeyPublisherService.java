package eu.europa.ec.dgc.issuance.service;

public interface CertKeyPublisherService {
    /**
     * Publishes the signing certificate to the DGC gateway.
     */
    void publishKey();
}
