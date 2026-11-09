package exception;

/**
 * Thrown when a team, stadium or match official is already booked
 * at the same date and time 
 */
public class SchedulingConflictException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public SchedulingConflictException(String message) {
        super(message);
    }
}