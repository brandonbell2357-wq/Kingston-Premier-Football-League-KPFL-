package exception;


/** Thrown when an unregistered or suspended team is placed in a fixture (BR1). */
public class TeamNotEligibleException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public TeamNotEligibleException(String message) {
        super(message);
    }
}