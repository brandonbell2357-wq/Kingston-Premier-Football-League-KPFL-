package model;

import exception.InvalidDataException;

// The person who runs the league and approves or suspends teams.
public class LeagueCommissioner extends Person {

    // Default constructor: ID "000", name and gender "N/A", age 0. Fill the fields with the setters.
    public LeagueCommissioner() {
        super();
    }

    public LeagueCommissioner(String id, String name, int age, String gender) throws InvalidDataException {
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

    /** Approves a team so it can be placed in fixtures. Throws if the team is null. */
    public void approveTeam(FootballTeam team) throws InvalidDataException {
        if (team == null) {
            throw new InvalidDataException("Cannot approve a team that does not exist (null).");
        }
        team.setStatus(TeamStatus.REGISTERED);
    }

    /** Suspends a team, which blocks it from fixtures. Throws if the team is null. */
    public void suspendTeam(FootballTeam team) throws InvalidDataException {
        if (team == null) {
            throw new InvalidDataException("Cannot suspend a team that does not exist (null).");
        }
        team.setStatus(TeamStatus.SUSPENDED);
    }
}
