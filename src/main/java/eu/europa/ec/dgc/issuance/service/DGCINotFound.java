package eu.europa.ec.dgc.issuance.service;

public class DGCINotFound extends RuntimeException {
    public DGCINotFound(String message, Throwable inner) {
        super(message, inner);
    }
    public DGCINotFound(String message) {
        super(message);
    }
}
