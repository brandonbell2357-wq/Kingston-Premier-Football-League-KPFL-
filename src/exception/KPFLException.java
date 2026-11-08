package exception;

/** Parent of every exception in the KPFL system, so one catch block can handle them all. */
public class KPFLException extends Exception {
    public KPFLException(String message) {
        super(message); // pass message to parent class constructor
    }
}
