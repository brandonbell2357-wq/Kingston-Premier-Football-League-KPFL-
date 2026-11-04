package model;

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

    public Player() {
        super();
        this.position = "";
        this.jerseyNumber = 0;
        this.playingStatus = "";
        this.goals = 0;
    }

    /**
     * The minimum-age rule (BR6) is enforced by setAge() below. Person's constructor
     * calls setAge(), so it applies here automatically.
     */
    public Player(String id, String name, int age, String gender,
                  String position, int jerseyNumber, String playingStatus) throws InvalidDataException {
        super(id, name, age, gender);
        setPosition(position);
        setJerseyNumber(jerseyNumber);
        setPlayingStatus(playingStatus);
        this.goals = 0;
    }

    public Player(Player player) {
        super(player);
        this.position = player.position;
        this.jerseyNumber = player.jerseyNumber;
        this.playingStatus = player.playingStatus;
        this.goals = player.goals;
    }

    @Override
    public String getRole() {
        return "Player";
    }

    @Override
    public String describe() {
        return super.describe() + ", " + position + " #" + jerseyNumber + ", " + goals + " goals, " + playingStatus;
    }

    /** Overrides Person.setAge to add the player minimum-age rule (BR6). */
    @Override
    public void setAge(int age) throws InvalidDataException {
        if (age >= 0 && age < MIN_AGE) {
            throw new UnderageException("A player must be at least " + MIN_AGE + " years old (age given: " + age + ").");
        }
        super.setAge(age); // still checks the normal 0 to MAX_AGE range
    }

    /** Adds goals scored. The number cannot be negative (BR5). */
    public void recordGoals(int newGoals) throws InvalidDataException {
        if (newGoals < 0) {
            throw new InvalidDataException("Goals cannot be negative (value given: " + newGoals + ").");
        }
        this.goals += newGoals;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) throws InvalidDataException {
        if (position == null || position.trim().isEmpty()) {
            throw new InvalidDataException("Position cannot be blank.");
        }
        this.position = position.trim();
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) throws InvalidDataException {
        if (jerseyNumber < 1 || jerseyNumber > 99) {
            throw new InvalidDataException("Jersey number must be between 1 and 99 (value given: " + jerseyNumber + ").");
        }
        this.jerseyNumber = jerseyNumber;
    }

    public int getGoals() {
        return goals;
    }

    public String getPlayingStatus() {
        return playingStatus;
    }

    public void setPlayingStatus(String playingStatus) throws InvalidDataException {
        if (playingStatus == null || playingStatus.trim().isEmpty()) {
            throw new InvalidDataException("Playing status cannot be blank.");
        }
        this.playingStatus = playingStatus.trim();
    }
}
