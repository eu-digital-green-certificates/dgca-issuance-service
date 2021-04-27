package eu.europa.ec.dgc.issuance.service;

public class WrongRequest extends RuntimeException {
    public WrongRequest(String message, Throwable inner) {
        super(message, inner);
    }

    public WrongRequest(String message) {
        super(message);
    }
}
