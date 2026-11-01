package exception;

/**
 * Parent of every KPFL business-rule exception, so one catch block can handle them all.
 * It is a checked exception, so the compiler forces callers to deal with rule violations.
 */
public class KpflException extends Exception {
    private static final long serialVersionUID = 1L;

    public KpflException(String message) {
        super(message);
    }
}
