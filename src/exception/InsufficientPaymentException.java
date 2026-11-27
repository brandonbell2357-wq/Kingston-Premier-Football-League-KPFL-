package exception;

// Thrown when a shop transaction is paid with less than its total (BR7).
public class InsufficientPaymentException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public InsufficientPaymentException(String message) {
        super(message);
    }
}
