package exception
;

/** Thrown when a player is younger than the league minimum age (BR6). */
public class UnderageException extends InvalidDataException {
    public UnderageException(String message) {
        super(message);
    }
}
