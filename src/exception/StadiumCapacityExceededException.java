package exception;

// Thrown when a ticket sale would put more people in a fixture than the stadium can seat (BR4).
public class StadiumCapacityExceededException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public StadiumCapacityExceededException(String message) {
        super(message);
    }
}
