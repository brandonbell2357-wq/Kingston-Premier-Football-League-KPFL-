package model;

import exception.InvalidValueException;

/** The person who runs the league and approves or suspends teams. */
public class LeagueCommissioner extends Person {
    public LeagueCommissioner(String id, String name, int age, String gender) throws InvalidValueException {
        super(id, name, age, gender);
    }

    @Override
    public String getRole() {
        return "League Commissioner";
    }

    /** Approves a team so it can be placed in fixtures. */
    public void approveTeam(FootballTeam team) throws InvalidValueException {
        Validate.notNull(team, "Team").setStatus(TeamStatus.REGISTERED);
    }

    /** Suspends a team, which blocks it from fixtures (BR1). */
    public void suspendTeam(FootballTeam team) throws InvalidValueException {
        Validate.notNull(team, "Team").setStatus(TeamStatus.SUSPENDED);
    }
}
