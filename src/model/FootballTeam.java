package model;

import exception.DuplicateRecordException;
import exception.InvalidValueException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A football team. It aggregates its players and coaches: they are stored here,
 * but they are independent people who can leave or move to another team.
 */
public class FootballTeam {
    private final String registrationNumber;
    private String name;
    private String parish;
    private int squadSize;
    private TeamStatus status;
    private int points;
    private final List<Player> players = new ArrayList<>();
    private final List<Coach> coaches = new ArrayList<>();

    public FootballTeam(String registrationNumber, String name, String parish, int squadSize)
            throws InvalidValueException {
        this.registrationNumber = Validate.text(registrationNumber, "Registration number");
        this.name = Validate.text(name, "Team name");
        this.parish = Validate.text(parish, "Parish");
        this.squadSize = Validate.positive(squadSize, "Squad size");
        this.status = TeamStatus.PENDING;
        this.points = 0;
    }

    /** Adds a player. The squad size is treated as the maximum number of players (rule D4). */
    public void addPlayer(Player player) throws InvalidValueException, DuplicateRecordException {
        Validate.notNull(player, "Player");
        for (Player existing : players) {
            if (existing.getId().equals(player.getId())) {
                throw new DuplicateRecordException("Player " + player.getId() + " is already in team " + name + ".");
            }
        }
        if (players.size() >= squadSize) {
            throw new InvalidValueException("Team " + name + " already has " + squadSize + " players, its squad size.");
        }
        players.add(player);
    }

    /** Removes the player with the given id. Returns true if someone was removed. */
    public boolean removePlayer(String playerId) {
        return players.removeIf(p -> p.getId().equals(playerId));
    }

    public void addCoach(Coach coach) throws InvalidValueException, DuplicateRecordException {
        Validate.notNull(coach, "Coach");
        for (Coach existing : coaches) {
            if (existing.getId().equals(coach.getId())) {
                throw new DuplicateRecordException("Coach " + coach.getId() + " is already in team " + name + ".");
            }
        }
        coaches.add(coach);
    }

    /** Removes the coach with the given id. Returns true if someone was removed. */
    public boolean removeCoach(String coachId) {
        return coaches.removeIf(c -> c.getId().equals(coachId));
    }

    /** True only for REGISTERED teams, which are the only ones allowed in fixtures (BR1). */
    public boolean isEligibleForFixtures() {
        return status == TeamStatus.REGISTERED;
    }

    public void addPoints(int extra) throws InvalidValueException {
        this.points += Validate.nonNegative(extra, "Points");
    }

    public void resetPoints() {
        this.points = 0;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidValueException {
        this.name = Validate.text(name, "Team name");
    }

    public String getParish() {
        return parish;
    }

    public void setParish(String parish) throws InvalidValueException {
        this.parish = Validate.text(parish, "Parish");
    }

    public int getSquadSize() {
        return squadSize;
    }

    public void setSquadSize(int squadSize) throws InvalidValueException {
        Validate.positive(squadSize, "Squad size");
        if (squadSize < players.size()) {
            throw new InvalidValueException("Squad size cannot be below the " + players.size() + " players already registered.");
        }
        this.squadSize = squadSize;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) throws InvalidValueException {
        this.status = Validate.notNull(status, "Status");
    }

    public int getPoints() {
        return points;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Coach> getCoaches() {
        return Collections.unmodifiableList(coaches);
    }

    @Override
    public String toString() {
        return registrationNumber + " " + name + " (" + parish + ", " + status + ", " + points + " pts, "
                + players.size() + "/" + squadSize + " players)";
    }
}
