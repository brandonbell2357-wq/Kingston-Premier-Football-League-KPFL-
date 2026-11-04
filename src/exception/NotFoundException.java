package model;

/** Thrown when we try to remove or find something that does not exist. */
public class NotFoundException extends KPFLException {
    public NotFoundException(String message) {
        super(message);
    }
}
