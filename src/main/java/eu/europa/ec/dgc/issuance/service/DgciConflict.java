package eu.europa.ec.dgc.issuance.service;

public class DgciConflict extends RuntimeException {
    public DgciConflict(String message) {
        super(message);
    }
}
