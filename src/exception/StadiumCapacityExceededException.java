package exception;

/** Thrown when ticket sales would exceed the stadium's seating capacity (BR4). */
public class StadiumCapacityExceededException extends KpflException {
    private static final long serialVersionUID = 1L;

    public StadiumCapacityExceededException(String message) {
        super(message);
    }
}
