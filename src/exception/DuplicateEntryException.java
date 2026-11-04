package model;

/** Thrown when something that must be unique (an ID, a jersey number) already exists. */
public class DuplicateEntryException extends KPFLException {
    public DuplicateEntryException(String message) {
        super(message);
    }
}
