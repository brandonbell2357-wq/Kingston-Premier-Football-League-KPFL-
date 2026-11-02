package model;

/** The person who runs the league and approves or suspends teams. */
public class LeagueCommissioner extends Person {
    /** Default constructor: empty text and age 0. Fill the fields with the setters. */
    public LeagueCommissioner() {
        super();
    }

    public LeagueCommissioner(String id, String name, int age, String gender) {
        super(id, name, age, gender);
    }

    /** Copy constructor: copies every field of another commissioner. */
    public LeagueCommissioner(LeagueCommissioner other) {
        super(other);
    }

    @Override
    public String getRole() {
        return "League Commissioner";
    }

    /** Approves a team so it can be placed in fixtures. Does nothing if the team is null. */
    public void approveTeam(FootballTeam team) {
        if (team != null) {
            team.setStatus(TeamStatus.REGISTERED);
        }
    }

    /** Suspends a team, which blocks it from fixtures. Does nothing if the team is null. */
    public void suspendTeam(FootballTeam team) {
        if (team != null) {
            team.setStatus(TeamStatus.SUSPENDED);
        }
    }
}
