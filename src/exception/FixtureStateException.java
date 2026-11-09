package exception;

/** Thrown when a fixture is completed without a valid result, or completed twice (BR8). */
public class FixtureStateException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public FixtureStateException(String message) {
        super(message);
    }
}