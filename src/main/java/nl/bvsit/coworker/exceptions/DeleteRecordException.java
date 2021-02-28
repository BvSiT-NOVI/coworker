package nl.bvsit.coworker.exceptions;

public class DeleteRecordException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DeleteRecordException(String message) { super(message); }
    public DeleteRecordException() {
        super("Record can not be deleted.");
    }

}