package exception;

/** Thrown when a shop sale asks for more units than are in stock. */
public class InsufficientStockException extends KpflException {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
