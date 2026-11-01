package model;

import exception.FixtureStateException;
import exception.InvalidValueException;
import exception.StadiumCapacityExceededException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A scheduled match between two teams at a stadium. It owns its tickets and its result
 * (composition), and refers to its teams, stadium and officials (association).
 *
 * Whether the teams are eligible (BR1) and whether the slot is free (BR2, BR3) are checked
 * by the league when it creates or changes fixtures, because that needs every other fixture.
 */
public class Fixture {
    private final String fixtureId;
    private final FootballTeam homeTeam;
    private final FootballTeam awayTeam;
    private final Stadium stadium;
    private final LocalDate date;
    private final LocalTime time;
    private FixtureStatus status;
    private MatchResult result;
    private final List<MatchOfficial> officials = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();

    public Fixture(String fixtureId, FootballTeam homeTeam, FootballTeam awayTeam, Stadium stadium,
                   LocalDate date, LocalTime time) throws InvalidValueException {
        this.fixtureId = Validate.text(fixtureId, "Fixture id");
        this.homeTeam = Validate.notNull(homeTeam, "Home team");
        this.awayTeam = Validate.notNull(awayTeam, "Away team");
        if (homeTeam.getRegistrationNumber().equals(awayTeam.getRegistrationNumber())) {
            throw new InvalidValueException("A team cannot play itself.");
        }
        this.stadium = Validate.notNull(stadium, "Stadium");
        this.date = Validate.notNull(date, "Date");
        this.time = Validate.notNull(time, "Time");
        this.status = FixtureStatus.SCHEDULED;
    }

    /** Adds an official to this fixture. An official already on it is not added twice. */
    public void assignOfficial(MatchOfficial official) throws InvalidValueException {
        Validate.notNull(official, "Match official");
        if (!officials.contains(official)) {
            officials.add(official);
        }
    }

    /** True if the other fixture is at the same date and time (the clash rule used for BR2 and BR3). */
    public boolean clashesWith(Fixture other) {
        return other != null && date.equals(other.date) && time.equals(other.time);
    }

    /**
     * Completes the fixture with its result (BR8). A fixture cannot be completed without a
     * result, and a completed fixture cannot be completed again.
     */
    public void complete(MatchResult matchResult) throws FixtureStateException {
        if (matchResult == null) {
            throw new FixtureStateException("Fixture " + fixtureId + " cannot be completed without a result.");
        }
        if (status == FixtureStatus.COMPLETED) {
            throw new FixtureStateException("Fixture " + fixtureId + " is already completed.");
        }
        this.result = matchResult;
        this.status = FixtureStatus.COMPLETED;
    }

    /**
     * Sells tickets to a spectator at the stadium ticket price. The quantity must be above
     * zero (BR5) and tickets sold cannot exceed capacity (BR4). Nothing is sold if a check fails.
     */
    public List<Ticket> sellTickets(Spectator spectator, String category, int quantity)
            throws InvalidValueException, StadiumCapacityExceededException, FixtureStateException {
        Validate.notNull(spectator, "Spectator");
        Validate.text(category, "Ticket category");
        Validate.positive(quantity, "Ticket quantity");
        if (status != FixtureStatus.SCHEDULED) {
            throw new FixtureStateException("Tickets can only be sold for scheduled fixtures.");
        }
        stadium.checkCapacity(tickets.size() + quantity);
        List<Ticket> sold = new ArrayList<>();
        for (int i = 1; i <= quantity; i++) {
            sold.add(new Ticket(fixtureId + "-T" + (tickets.size() + i), category, stadium.getTicketPrice(), spectator));
        }
        tickets.addAll(sold);
        return sold;
    }

    public int getTicketsSold() {
        return tickets.size();
    }

    public int getSeatsRemaining() {
        return stadium.getSeatingCapacity() - tickets.size();
    }

    public BigDecimal getTicketRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Ticket ticket : tickets) {
            total = total.add(ticket.getPrice());
        }
        return total;
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

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public FixtureStatus getStatus() {
        return status;
    }

    public MatchResult getResult() {
        return result;
    }

    public List<MatchOfficial> getOfficials() {
        return Collections.unmodifiableList(officials);
    }

    public List<Ticket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    @Override
    public String toString() {
        return fixtureId + ": " + homeTeam.getName() + " v " + awayTeam.getName() + " at " + stadium.getName()
                + ", " + date + " " + time + " (" + status + (result != null ? ", " + result : "") + ")";
    }
}
