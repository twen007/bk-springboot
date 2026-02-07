package gov.nist.emp.bankcard.exception;

/**
 * Exception for unauthorized access (401).
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
