package gov.nist.emp.bankcard.exception;

/**
 * Exception for forbidden access (403).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
