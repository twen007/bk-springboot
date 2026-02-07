package gov.nist.emp.bankcard.exception;

/**
 * Exception for resource not found (404).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
