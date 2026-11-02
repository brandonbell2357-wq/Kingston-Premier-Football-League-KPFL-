package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A football team. It aggregates its players and coaches: they are stored here,
 * but they are independent people who can leave or move to another team.
 */
public class FootballTeam {
    private String registrationNumber;
    private String name;
    private String parish;
    private int squadSize;
    private TeamStatus status;
    private int points;
    private final List<Player> players = new ArrayList<>();
    private final List<Coach> coaches = new ArrayList<>();

    /** Default constructor: empty text, squad size 0, status PENDING and no people. */
    public FootballTeam() {
        this("", "", "", 0);
    }

    public FootballTeam(String registrationNumber, String name, String parish, int squadSize) {
        this.registrationNumber = registrationNumber;
        this.name = name;
        this.parish = parish;
        this.squadSize = squadSize;
        this.status = TeamStatus.PENDING;
        this.points = 0;
    }

    /**
     * Copy constructor: copies the team and makes a separate copy of each player and coach,
     * so changing the copy never changes the original.
     */
    public FootballTeam(FootballTeam other) {
        this(other.registrationNumber, other.name, other.parish, other.squadSize);
        this.status = other.status;
        this.points = other.points;
        for (Player player : other.players) {
            this.players.add(new Player(player));
        }
        for (Coach coach : other.coaches) {
            this.coaches.add(new Coach(coach));
        }
    }

    /**
     * Adds a player. Returns false, and adds nothing, if the player is null, a player with
     * the same id is already here, or the squad is full (the squad size is the maximum).
     */
    public boolean addPlayer(Player player) {
        if (player == null || players.size() >= squadSize) {
            return false;
        }
        for (Player existing : players) {
            if (existing.getId().equals(player.getId())) {
                return false;
            }
        }
        players.add(player);
        return true;
    }

    /** Removes the player with the given id. Returns true if someone was removed. */
    public boolean removePlayer(String playerId) {
        return players.removeIf(p -> p.getId().equals(playerId));
    }

    /** Adds a coach. Returns false, and adds nothing, if the coach is null or already here. */
    public boolean addCoach(Coach coach) {
        if (coach == null) {
            return false;
        }
        for (Coach existing : coaches) {
            if (existing.getId().equals(coach.getId())) {
                return false;
            }
        }
        coaches.add(coach);
        return true;
    }

    /** Removes the coach with the given id. Returns true if someone was removed. */
    public boolean removeCoach(String coachId) {
        return coaches.removeIf(c -> c.getId().equals(coachId));
    }

    /** True only for REGISTERED teams, which are the only ones that should be placed in fixtures. */
    public boolean isEligibleForFixtures() {
        return status == TeamStatus.REGISTERED;
    }

    public void addPoints(int extra) {
        this.points += extra;
    }

    public void resetPoints() {
        this.points = 0;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParish() {
        return parish;
    }

    public void setParish(String parish) {
        this.parish = parish;
    }

    public int getSquadSize() {
        return squadSize;
    }

    public void setSquadSize(int squadSize) {
        this.squadSize = squadSize;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
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
