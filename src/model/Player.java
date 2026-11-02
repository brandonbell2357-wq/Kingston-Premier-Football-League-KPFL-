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

    public Player(){
        super();
        this.position = "";
        this.jerseyNumber = 0;
        this.playingStatus = "";
        this.goals = 0;

    }

    public Player(String id, String name, int age, String gender,
                  String position, int jerseyNumber, String playingStatus)  {
        super(id, name, age, gender);
        if (age < MIN_AGE) {
            //Create Exception to throw
        }
        this.position = position;
        this.jerseyNumber = jerseyNumber;
        this.playingStatus = playingStatus;
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

    /** Adds goals scored. The number cannot be negative (BR5). */
    public void recordGoals(int newGoals){
        this.goals += newGoals;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public int getGoals() {
        return goals;
    }

    public String getPlayingStatus() {
        return playingStatus;
    }

    public void setPlayingStatus(String playingStatus) {
        this.playingStatus = playingStatus;
    }
}
