package model;

import exception.InvalidDataException;
import exception.TeamNotEligibleException;
import java.time.LocalDate;
import java.time.LocalTime;

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
    private int ticketsSold;

    
     /* Creates a SCHEDULED fixture.
      Enforces BR1: both teams must be REGISTERED (not pending or suspended).
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
        this.ticketsSold = 0;
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
        this.ticketsSold = other.ticketsSold;
    }

    // True if the given team is the home or away team in this fixture. 
    public boolean involves(FootballTeam team) {
        return team != null && (homeTeam == team || awayTeam == team);
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
        if (!stadium.checkCapacity(ticketsSold)) {
            throw new InvalidDataException(stadium.getName() + " only has " + stadium.getSeatingCapacity()
                    + " seats, but " + ticketsSold + " tickets are already sold for this fixture.");
        }
        this.stadium = stadium;
    }

    // ---------- Occupancy (counted per fixture, see design document section 4) ----------

    //Number of seats taken for this fixture (tickets sold so far).
    public int getOccupancy() {
        return ticketsSold;
    }

    //Seats still free for this fixture: the stadium's capacity minus the tickets sold
    public int getSeatsRemaining() {
        return stadium.getSeatingCapacity() - ticketsSold;
    }

    /*
      True if this many more tickets would still fit in the stadium (BR4).
      The ticketing code (Week 4) calls this before it sells any tickets.
     */
    public boolean hasSeatsFor(int quantity) {
        return quantity > 0 && stadium.checkCapacity(ticketsSold + quantity);
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
                + " at " + stadium.getName() + " on " + date + " at " + time + " (" + status + ", "
                + ticketsSold + "/" + stadium.getSeatingCapacity() + " seats taken)";
    }
}
