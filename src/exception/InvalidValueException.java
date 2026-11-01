package exception;

/** Thrown when a value is blank, negative, missing or otherwise not allowed (BR5 and field checks). */
public class InvalidValueException extends KpflException {
    private static final long serialVersionUID = 1L;

    public InvalidValueException(String message) {
        super(message);
    }
}
