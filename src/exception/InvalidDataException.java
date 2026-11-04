package model;

/** Thrown when data is blank, null, negative or out of range. */
public class InvalidDataException extends KPFLException {
    public InvalidDataException(String message) {
        super(message);
    }
}
