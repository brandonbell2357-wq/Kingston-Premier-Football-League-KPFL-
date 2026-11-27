package exception;

// Thrown when a shop sale asks for more of a product than is in stock.
public class InsufficientStockException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public InsufficientStockException(String message) {
        super(message);
    }
}
