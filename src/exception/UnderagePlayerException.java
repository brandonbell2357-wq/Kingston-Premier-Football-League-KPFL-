package exception;

/** Thrown when a player is younger than the league minimum age (BR6). */
public class UnderagePlayerException extends InvalidValueException {
    private static final long serialVersionUID = 1L;

    public UnderagePlayerException(String message) {
        super(message);
    }
}
