package exception;

/** Thrown when a fixture is in the wrong state for an action, such as completing it without a result (BR8). */
public class FixtureStateException extends KpflException {
    private static final long serialVersionUID = 1L;

    public FixtureStateException(String message) {
        super(message);
    }
}
