package eu.europa.ec.dgc.issuance.service;

public class DgciNotFound extends RuntimeException {
    public DgciNotFound(String message, Throwable inner) {
        super(message, inner);
    }

    public DgciNotFound(String message) {
        super(message);
    }
}
