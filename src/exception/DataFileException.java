package exception;

/** Thrown when a data file cannot be read or written, or its contents are damaged. */
public class DataFileException extends KPFLException {
    private static final long serialVersionUID = 1L;

    public DataFileException(String message) {
        super(message);
    }

    public DataFileException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}