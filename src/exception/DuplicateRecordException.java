package exception;

/** Thrown when an id or registration number that must be unique already exists. */
public class DuplicateRecordException extends KpflException {
    private static final long serialVersionUID = 1L;

    public DuplicateRecordException(String message) {
        super(message);
    }
}
