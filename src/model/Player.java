package model;

import exception.InvalidValueException;
import exception.UnderagePlayerException;

/** A footballer registered with a team. */
public class Player extends Person {
    /**
     * League minimum player age (BR6).
     * PLACEHOLDER VALUE: the project brief mentions a minimum age but never gives it.
     * Change this one constant once the lecturer confirms the real value.
     */
    public static final int MIN_AGE = 16;

    private String position;
    private int jerseyNumber;
    private int goals;
    private String playingStatus;

    public Player(String id, String name, int age, String gender,
                  String position, int jerseyNumber, String playingStatus) throws InvalidValueException {
        super(id, name, age, gender);
        if (age < MIN_AGE) {
            throw new UnderagePlayerException("Player " + name + " is " + age
                    + " but the league minimum age is " + MIN_AGE + ".");
        }
        this.position = Validate.text(position, "Position");
        this.jerseyNumber = Validate.positive(jerseyNumber, "Jersey number");
        this.playingStatus = Validate.text(playingStatus, "Playing status");
        this.goals = 0;
    }

    @Override
    public String getRole() {
        return "Player";
    }

    @Override
    public String describe() {
        return super.describe() + ", " + position + " #" + jerseyNumber + ", " + goals + " goals, " + playingStatus;
    }

    /** Adds goals scored. The number cannot be negative (BR5). */
    public void recordGoals(int newGoals) throws InvalidValueException {
        this.goals += Validate.nonNegative(newGoals, "Goals");
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) throws InvalidValueException {
        this.position = Validate.text(position, "Position");
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) throws InvalidValueException {
        this.jerseyNumber = Validate.positive(jerseyNumber, "Jersey number");
    }

    public int getGoals() {
        return goals;
    }

    public String getPlayingStatus() {
        return playingStatus;
    }

    public void setPlayingStatus(String playingStatus) throws InvalidValueException {
        this.playingStatus = Validate.text(playingStatus, "Playing status");
    }
}
