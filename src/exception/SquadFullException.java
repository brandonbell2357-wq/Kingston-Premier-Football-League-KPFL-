package model;

/** Thrown when a player is added to a team whose squad is already full. */
public class SquadFullException extends KPFLException {
    public SquadFullException(String message) {
        super(message);
    }
}
