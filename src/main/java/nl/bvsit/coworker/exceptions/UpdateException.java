package nl.bvsit.coworker.exceptions;

public class UpdateException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public UpdateException(String message) { super(message); }
    public UpdateException() {
        super("Cannot update specified record.");
    }
}