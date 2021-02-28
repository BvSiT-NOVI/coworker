package nl.bvsit.coworker.exceptions;

public class NotUniqueException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NotUniqueException(String message) { super(message); }
    public NotUniqueException() {
        super("Record is not unique.");
    }

}