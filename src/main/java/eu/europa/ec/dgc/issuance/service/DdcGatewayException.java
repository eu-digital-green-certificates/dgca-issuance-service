package eu.europa.ec.dgc.issuance.service;

public class DdcGatewayException extends RuntimeException {
    public DdcGatewayException(String message) {
        super(message);
    }

    public DdcGatewayException(String message, Throwable inner) {
        super(message, inner);
    }
}
