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
        this.registrationNumber = "";
        this.name = "";
        this.parish = "";
        this.squadSize = 0;
        this.status = TeamStatus.PENDING;
        this.points = 0;
    }

    public FootballTeam(String registrationNumber, String name, String parish, int squadSize)
            throws InvalidDataException {
        setRegistrationNumber(registrationNumber);
        setName(name);
        setParish(parish);
        setSquadSize(squadSize);
        this.status = TeamStatus.PENDING;
        this.points = 0;
    }

    /**
     * Copy constructor: copies the team and makes a separate copy of each player and coach,
     * so changing the copy never changes the original.
     */
    public FootballTeam(FootballTeam other) {
        this.registrationNumber = other.registrationNumber;
        this.name = other.name;
        this.parish = other.parish;
        this.squadSize = other.squadSize;
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
     * Adds a player. Throws an exception, and adds nothing, if the player is null,
     * the squad is full (the squad size is the maximum), or a player with the same
     * id or jersey number is already here.
     */
    public void addPlayer(Player player)
            throws InvalidDataException, SquadFullException, DuplicateEntryException {
        if (player == null) {
            throw new InvalidDataException("Player cannot be null.");
        }
        if (players.size() >= squadSize) {
            throw new SquadFullException("The squad is full (maximum " + squadSize + " players).");
        }
        for (Player existing : players) {
            if (existing.getId().equals(player.getId())) {
                throw new DuplicateEntryException("A player with ID " + player.getId() + " is already on this team.");
            }
            if (existing.getJerseyNumber() == player.getJerseyNumber()) {
                throw new DuplicateEntryException("Jersey number " + player.getJerseyNumber() + " is already taken.");
            }
        }
        players.add(player);
    }
    ///


    /** Removes the player with the given id. Throws if nobody with that id is on the team. */
    public void removePlayer(String playerId) throws NotFoundException {
        boolean removed = players.removeIf(p -> p.getId().equals(playerId));
        if (!removed) {
            throw new NotFoundException("No player with ID " + playerId + " is on this team.");
        }
    }

    /** Adds a coach. Throws an exception, and adds nothing, if the coach is null or already here. */
    public void addCoach(Coach coach) throws InvalidDataException, DuplicateEntryException {
        if (coach == null) {
            throw new InvalidDataException("Coach cannot be null.");
        }
        for (Coach existing : coaches) {
            if (existing.getId().equals(coach.getId())) {
                throw new DuplicateEntryException("A coach with ID " + coach.getId() + " is already on this team.");
            }
        }
        coaches.add(coach);
    }

    /** Removes the coach with the given id. Throws if nobody with that id is on the team. */
    public void removeCoach(String coachId) throws NotFoundException {
        boolean removed = coaches.removeIf(c -> c.getId().equals(coachId));
        if (!removed) {
            throw new NotFoundException("No coach with ID " + coachId + " is on this team.");
        }
    }

    /** True only for REGISTERED teams, which are the only ones that should be placed in fixtures. */
    public boolean isEligibleForFixtures() {
        return status == TeamStatus.REGISTERED;
    }

    public void addPoints(int extra) throws InvalidDataException {
        if (extra < 0) {
            throw new InvalidDataException("Points to add cannot be negative (value given: " + extra + ").");
        }
        this.points += extra;
    }

    public void resetPoints() {
        this.points = 0;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) throws InvalidDataException {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new InvalidDataException("Registration number cannot be blank.");
        }
        this.registrationNumber = registrationNumber.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Team name cannot be blank.");
        }
        this.name = name.trim();
    }

    public String getParish() {
        return parish;
    }

    public void setParish(String parish) throws InvalidDataException {
        if (parish == null || parish.trim().isEmpty()) {
            throw new InvalidDataException("Parish cannot be blank.");
        }
        this.parish = parish.trim();
    }

    public int getSquadSize() {
        return squadSize;
    }

    /** The squad size cannot be negative, or smaller than the number of players already on the team. */
    public void setSquadSize(int squadSize) throws InvalidDataException {
        if (squadSize < 0) {
            throw new InvalidDataException("Squad size cannot be negative (value given: " + squadSize + ").");
        }
        if (squadSize < players.size()) {
            throw new InvalidDataException("Squad size cannot be less than the " + players.size()
                    + " players already on the team.");
        }
        this.squadSize = squadSize;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) throws InvalidDataException {
        if (status == null) {
            throw new InvalidDataException("Team status cannot be null.");
        }
        this.status = status;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) throws InvalidDataException {
        if (points < 0) {
            throw new InvalidDataException("Points cannot be negative (value given: " + points + ").");
        }
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
