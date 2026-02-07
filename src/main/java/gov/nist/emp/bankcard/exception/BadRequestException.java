package gov.nist.emp.bankcard.exception;

/**
 * Exception for bad request (400).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
