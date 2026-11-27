package model;

import exception.DuplicateEntryException;
import exception.FixtureStateException;
import exception.InvalidDataException;
import exception.NotFoundException;
import exception.SchedulingConflictException;
import exception.StadiumCapacityExceededException;
import exception.TeamNotEligibleException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
  A match between a home team and an away team at a stadium on a date and time.
  A fixture refers to its teams and stadium (association): it does not own them,
  so deleting a fixture never deletes a team or a stadium.
*/
public class Fixture {
    private final String fixtureId;
    private final FootballTeam homeTeam;
    private final FootballTeam awayTeam;
    private Stadium stadium;
    private LocalDate date;
    private LocalTime time;
    private FixtureStatus status;
    private MatchResult result; // null until the fixture is completed 
    private final List<MatchOfficial> officials = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>(); // composition: the fixture owns its tickets

    
     /* Creates a SCHEDULED fixture.
     Both teams must be REGISTERED (not pending or suspended).
     */
    public Fixture(String fixtureId, FootballTeam homeTeam, FootballTeam awayTeam,
                   Stadium stadium, LocalDate date, LocalTime time)
            throws InvalidDataException, TeamNotEligibleException {
        if (fixtureId == null || fixtureId.trim().isEmpty()) {
            throw new InvalidDataException("Fixture ID cannot be blank.");
        }
        if (homeTeam == null || awayTeam == null) {
            throw new InvalidDataException("A fixture needs both a home team and an away team.");
        }
        if (homeTeam == awayTeam
                || homeTeam.getRegistrationNumber().equals(awayTeam.getRegistrationNumber())) {
            throw new InvalidDataException("A team cannot play against itself ("
                    + homeTeam.getName() + ").");
        }
        if (!homeTeam.isEligibleForFixtures()) {
            throw new TeamNotEligibleException(homeTeam.getName() + " cannot be placed in a fixture (status: "
                    + homeTeam.getStatus() + ").");
        }
        if (!awayTeam.isEligibleForFixtures()) {
            throw new TeamNotEligibleException(awayTeam.getName() + " cannot be placed in a fixture (status: "
                    + awayTeam.getStatus() + ").");
        }
        this.fixtureId = fixtureId.trim();
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        setStadium(stadium);
        setDate(date);
        setTime(time);
        this.status = FixtureStatus.SCHEDULED;
    }

    /*
     Copy constructor. The copy points to the SAME team and stadium objects,
      because a fixture only refers to them (association); it does not own them.
     */
    public Fixture(Fixture other) {
        this.fixtureId = other.fixtureId;
        this.homeTeam = other.homeTeam;
        this.awayTeam = other.awayTeam;
        this.stadium = other.stadium;
        this.date = other.date;
        this.time = other.time;
        this.status = other.status;
        this.result = other.result == null ? null : new MatchResult(other.result);
        this.officials.addAll(other.officials);
        // Tickets belong to the fixture (composition), so the copy gets its own ticket objects.
        for (Ticket ticket : other.tickets) {
            try {
                this.tickets.add(new Ticket(ticket.getTicketId(), this, ticket.getSpectator(),
                        ticket.getCategory(), ticket.getPrice()));
            } catch (InvalidDataException e) {
                throw new IllegalStateException("A valid ticket could not be copied.", e);
            }
        }
    }

    // True if the given team is the home or away team in this fixture. 
    public boolean involves(FootballTeam team) {
        return team != null && (homeTeam == team || awayTeam == team);
    }

    // ---------- Official assignment  ----------

    /**
     * True if the other fixture is a different fixture held on the same date AND at the same time.
     * Only an identical date and time counts as a clash (same day at another time is fine).
     */
    public boolean clashesWith(Fixture other) {
        return other != null && other != this
                && date.equals(other.date) && time.equals(other.time);
    }

    //True if this official is already assigned to this fixture. */
    public boolean hasOfficial(MatchOfficial official) {
        if (official == null) {
            return false;
        }
        for (MatchOfficial assigned : officials) {
            if (assigned.getId().equals(official.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Assigns a match official to this fixture. Every check runs first, so a refused
     * assignment changes nothing.
     */
    public void assignOfficial(MatchOfficial official, Collection<Fixture> otherFixtures)
            throws InvalidDataException, DuplicateEntryException,
                   FixtureStateException, SchedulingConflictException {
        if (official == null) {
            throw new InvalidDataException("Official cannot be null.");
        }
        if (status != FixtureStatus.SCHEDULED) {
            throw new FixtureStateException("Officials can only be assigned to a scheduled fixture ("
                    + fixtureId + " is " + status + ").");
        }
        if (!official.isAvailable()) {
            throw new InvalidDataException(official.getName() + " is marked as unavailable.");
        }
        if (hasOfficial(official)) {
            throw new DuplicateEntryException(official.getName() + " is already assigned to " + fixtureId + ".");
        }
        if (otherFixtures != null) {
            for (Fixture other : otherFixtures) {
                if (clashesWith(other) && other.hasOfficial(official)) {
                    throw new SchedulingConflictException(official.getName() + " is already assigned to "
                            + other.getFixtureId() + " on " + date + " at " + time + ".");
                }
            }
        }
        officials.add(official);
    }

    /** Removes the official with the given ID. Throws if that official is not assigned here. */
    public void removeOfficial(String officialId) throws NotFoundException {
        boolean removed = officials.removeIf(o -> o.getId().equals(officialId));
        if (!removed) {
            throw new NotFoundException("No official with ID " + officialId + " is assigned to " + fixtureId + ".");
        }
    }

    /** The officials assigned to this fixture. The list cannot be changed from outside. */
    public List<MatchOfficial> getOfficials() {
        return Collections.unmodifiableList(officials);
    }

    // ---------- Results (BR8) ----------

    /**
     * Completes this fixture with its result. This is the ONLY way a fixture becomes
     * COMPLETED (there is deliberately no setStatus), so a fixture can never be
     * completed without a valid result (BR8). Every check runs first, so a refused
     * call changes nothing.
     */
    public void complete(MatchResult matchResult) throws FixtureStateException {
        if (matchResult == null) {
            throw new FixtureStateException("Fixture " + fixtureId
                    + " cannot be completed without a valid result.");
        }
        if (status == FixtureStatus.COMPLETED) {
            throw new FixtureStateException("Fixture " + fixtureId
                    + " is already completed (result " + result + ").");
        }
        this.result = matchResult;
        this.status = FixtureStatus.COMPLETED;
    }

    /** True once a result has been recorded. */
    public boolean isCompleted() {
        return status == FixtureStatus.COMPLETED;
    }

    /** The recorded result, or null while the fixture is still SCHEDULED. */
    public MatchResult getResult() {
        return result;
    }

    /** The winning team, or null if the fixture is not completed or ended in a draw. */
    public FootballTeam getWinner() {
        if (result == null || result.isDraw()) {
            return null;
        }
        if (result.isHomeWin()) {
            return homeTeam;
        }
        return awayTeam;
    }

    public String getFixtureId() {
        return fixtureId;
    }

    public FootballTeam getHomeTeam() {
        return homeTeam;
    }

    public FootballTeam getAwayTeam() {
        return awayTeam;
    }

    public Stadium getStadium() {
        return stadium;
    }

    /*
      Assigns (or changes) the stadium. A fixture cannot be moved to a stadium
      that is too small for the tickets already sold.
     */
    public void setStadium(Stadium stadium) throws InvalidDataException {
        if (stadium == null) {
            throw new InvalidDataException("A fixture must be assigned a stadium.");
        }
        if (!stadium.checkCapacity(tickets.size())) {
            throw new InvalidDataException(stadium.getName() + " only has " + stadium.getSeatingCapacity()
                    + " seats, but " + tickets.size() + " tickets are already sold for this fixture.");
        }
        this.stadium = stadium;
    }

    // ---------- Occupancy (counted per fixture, see design document section 4) ----------

    //Number of seats taken for this fixture (tickets sold so far).
    public int getOccupancy() {
        return tickets.size();
    }

    //Seats still free for this fixture: the stadium's capacity minus the tickets sold
    public int getSeatsRemaining() {
        return stadium.getSeatingCapacity() - getOccupancy();
    }

    /*
      True if this many more tickets would still fit in the stadium (BR4).
      The ticketing code (Week 4) calls this before it sells any tickets.
     */
    public boolean hasSeatsFor(int quantity) {
        return quantity > 0 && stadium.checkCapacity(getOccupancy() + quantity);
    }

    // ---------- Ticket sales (BR4, BR5) ----------

    /**
     * Sells tickets for this fixture to one spectator and returns the new tickets.
     * Every check runs first, so a refused sale issues no tickets at all.
     *
     * @throws InvalidDataException             no spectator or category, or a quantity of 0 or less (BR5)
     * @throws FixtureStateException            the fixture has already been played
     * @throws StadiumCapacityExceededException the sale would go over the stadium's capacity (BR4)
     */
    public List<Ticket> sellTickets(Spectator spectator, TicketCategory category, int quantity)
            throws InvalidDataException, FixtureStateException, StadiumCapacityExceededException {
        if (spectator == null) {
            throw new InvalidDataException("Tickets must be sold to a spectator.");
        }
        if (category == null) {
            throw new InvalidDataException("Ticket category cannot be empty.");
        }
        if (quantity <= 0) {
            throw new InvalidDataException("Ticket quantity must be at least 1 (value given: " + quantity + ").");
        }
        if (status != FixtureStatus.SCHEDULED) {
            throw new FixtureStateException("Tickets cannot be sold for " + fixtureId + " because it is " + status + ".");
        }
        if (!hasSeatsFor(quantity)) {
            throw new StadiumCapacityExceededException("Cannot sell " + quantity + " tickets for " + fixtureId
                    + ": only " + getSeatsRemaining() + " of " + stadium.getSeatingCapacity() + " seats are left.");
        }
        BigDecimal price = category.priceFor(stadium.getTicketPrice());
        List<Ticket> sold = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            sold.add(new Ticket(nextTicketId(tickets.size() + i), this, spectator, category, price));
        }
        tickets.addAll(sold);
        return Collections.unmodifiableList(sold);
    }

    /**
     * Puts back a ticket that was sold earlier, when saved data is loaded. Unlike sellTickets,
     * it also works for a fixture that has since been played, but the ticket is still checked:
     * no duplicate ticket ID, and never more tickets than the stadium's capacity (BR4).
     */
    public Ticket restoreTicket(String ticketId, Spectator spectator, TicketCategory category, BigDecimal price)
            throws InvalidDataException, DuplicateEntryException, StadiumCapacityExceededException {
        for (Ticket existing : tickets) {
            if (existing.getTicketId().equalsIgnoreCase(ticketId == null ? "" : ticketId.trim())) {
                throw new DuplicateEntryException("Ticket " + ticketId + " already exists for " + fixtureId + ".");
            }
        }
        if (!stadium.checkCapacity(getOccupancy() + 1)) {
            throw new StadiumCapacityExceededException("Ticket " + ticketId + " would put " + fixtureId
                    + " over the capacity of " + stadium.getSeatingCapacity() + ".");
        }
        Ticket ticket = new Ticket(ticketId, this, spectator, category, price);
        tickets.add(ticket);
        return ticket;
    }

    /** Ticket IDs are the fixture ID plus a running number, e.g. FX01-T0001. */
    private String nextTicketId(int number) {
        return String.format("%s-T%04d", fixtureId, number);
    }

    /** Every ticket sold for this fixture. The list cannot be changed from outside. */
    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    /** How many tickets of one category have been sold. */
    public int countTickets(TicketCategory category) {
        int count = 0;
        for (Ticket ticket : tickets) {
            if (ticket.getCategory() == category) {
                count++;
            }
        }
        return count;
    }

    /** Total money taken from ticket sales for this fixture. */
    public BigDecimal getTicketRevenue() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Ticket ticket : tickets) {
            total = total.add(ticket.getPrice());
        }
        return total;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) throws InvalidDataException {
        if (date == null) {
            throw new InvalidDataException("Fixture date cannot be empty.");
        }
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) throws InvalidDataException {
        if (time == null) {
            throw new InvalidDataException("Fixture time cannot be empty.");
        }
        this.time = time;
    }

    public FixtureStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return fixtureId + ": " + homeTeam.getName() + " vs " + awayTeam.getName()
                + " at " + stadium.getName() + " on " + date + " at " + time + " (" + status
                + (result == null ? "" : " " + result) + ", "
                + getOccupancy() + "/" + stadium.getSeatingCapacity() + " seats taken)";
    }
}
