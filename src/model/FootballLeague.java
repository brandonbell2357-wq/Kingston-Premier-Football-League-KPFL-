package model;

import exception.DuplicateEntryException;
import exception.FixtureStateException;
import exception.InvalidDataException;
import exception.NotFoundException;
import exception.SchedulingConflictException;
import exception.StadiumCapacityExceededException;
import exception.TeamNotEligibleException;
import service.StandingsService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
  Coordinates the league: it keeps the lists of teams, stadiums, match officials and
  fixtures, and applies the scheduling rules that need to look at more than one fixture.

  Relationships (see the UML):
    - Teams, stadiums and officials are aggregated: they exist on their own.
    - Fixtures belong to the league (composition).
    - Standings are never stored; they are rebuilt from completed fixtures by StandingsService.

  Rules enforced here:
      Only registered teams in a fixture      (checked by the Fixture constructor)
      A team cannot play two fixtures at the same date and time
      An official cannot be in two fixtures at the same date and time
      A fixture is completed only with a valid result
  A stadium also cannot host two fixtures at the same date and time.

  Week 4: the league also keeps the spectators and its stadium shop, and sells tickets
  for its fixtures (BR4 and BR5 are checked by Fixture.sellTickets).
 */
public class FootballLeague {

    private String name;
    private LeagueCommissioner commissioner;
    private final List<FootballTeam> teams = new ArrayList<>();
    private final List<Stadium> stadiums = new ArrayList<>();
    private final List<MatchOfficial> officials = new ArrayList<>();
    private final List<Fixture> fixtures = new ArrayList<>();
    private final List<Spectator> spectators = new ArrayList<>();
    private final StadiumShop shop = createShop();

    /** Name of the league's stadium shop. */
    public static final String SHOP_NAME = "KPFL Stadium Shop";

    private static StadiumShop createShop() {
        try {
            return new StadiumShop(SHOP_NAME);
        } catch (InvalidDataException e) {
            throw new IllegalStateException("The shop name constant is invalid.", e);
        }
    }

    public FootballLeague() {
        this.name = "";
    }

    public FootballLeague(String name) throws InvalidDataException {
        setName(name);
    }

    // ---------- Registering ----------

    public void registerTeam(FootballTeam team) throws InvalidDataException, DuplicateEntryException {
        if (team == null) {
            throw new InvalidDataException("Team cannot be null.");
        }
        for (FootballTeam existing : teams) {
            if (existing.getRegistrationNumber().equalsIgnoreCase(team.getRegistrationNumber())) {
                throw new DuplicateEntryException("A team with registration number "
                        + team.getRegistrationNumber() + " already exists.");
            }
        }
        teams.add(team);
    }

    public void addStadium(Stadium stadium) throws InvalidDataException, DuplicateEntryException {
        if (stadium == null) {
            throw new InvalidDataException("Stadium cannot be null.");
        }
        for (Stadium existing : stadiums) {
            if (existing.getStadiumId().equalsIgnoreCase(stadium.getStadiumId())) {
                throw new DuplicateEntryException("A stadium with ID " + stadium.getStadiumId() + " already exists.");
            }
        }
        stadiums.add(stadium);
    }

    public void addOfficial(MatchOfficial official) throws InvalidDataException, DuplicateEntryException {
        if (official == null) {
            throw new InvalidDataException("Match official cannot be null.");
        }
        for (MatchOfficial existing : officials) {
            if (existing.getId().equalsIgnoreCase(official.getId())) {
                throw new DuplicateEntryException("An official with ID " + official.getId() + " already exists.");
            }
        }
        officials.add(official);
    }

    // Registers a spectator so they can buy tickets and shop products. IDs must be unique. 
    public void addSpectator(Spectator spectator) throws InvalidDataException, DuplicateEntryException {
        if (spectator == null) {
            throw new InvalidDataException("Spectator cannot be null.");
        }
        for (Spectator existing : spectators) {
            if (existing.getId().equalsIgnoreCase(spectator.getId())) {
                throw new DuplicateEntryException("A spectator with ID " + spectator.getId() + " already exists.");
            }
        }
        spectators.add(spectator);
    }

    // ---------- Lookups ----------

    public FootballTeam findTeam(String registrationNumber) throws NotFoundException {
        for (FootballTeam team : teams) {
            if (team.getRegistrationNumber().equalsIgnoreCase(trim(registrationNumber))) {
                return team;
            }
        }
        throw new NotFoundException("No team with registration number " + trim(registrationNumber) + ".");
    }

    public Stadium findStadium(String stadiumId) throws NotFoundException {
        for (Stadium stadium : stadiums) {
            if (stadium.getStadiumId().equalsIgnoreCase(trim(stadiumId))) {
                return stadium;
            }
        }
        throw new NotFoundException("No stadium with ID " + trim(stadiumId) + ".");
    }

    public MatchOfficial findOfficial(String officialId) throws NotFoundException {
        for (MatchOfficial official : officials) {
            if (official.getId().equalsIgnoreCase(trim(officialId))) {
                return official;
            }
        }
        throw new NotFoundException("No match official with ID " + trim(officialId) + ".");
    }

    public Fixture findFixture(String fixtureId) throws NotFoundException {
        for (Fixture fixture : fixtures) {
            if (fixture.getFixtureId().equalsIgnoreCase(trim(fixtureId))) {
                return fixture;
            }
        }
        throw new NotFoundException("No fixture with ID " + trim(fixtureId) + ".");
    }

    public Spectator findSpectator(String spectatorId) throws NotFoundException {
        for (Spectator spectator : spectators) {
            if (spectator.getId().equalsIgnoreCase(trim(spectatorId))) {
                return spectator;
            }
        }
        throw new NotFoundException("No spectator with ID " + trim(spectatorId) + ".");
    }

    //Teams whose name, registration number or parish contains the text (ignoring case).
    public List<FootballTeam> searchTeams(String text) {
        String wanted = trim(text).toLowerCase();
        List<FootballTeam> found = new ArrayList<>();
        for (FootballTeam team : teams) {
            if (team.getName().toLowerCase().contains(wanted)
                    || team.getRegistrationNumber().toLowerCase().contains(wanted)
                    || team.getParish().toLowerCase().contains(wanted)) {
                found.add(team);
            }
        }
        return found;
    }

    // ---------- Scheduling (BR1, BR2) ----------

    /*
      Creates a fixture after checking every rule. If any check fails nothing is added.
    */
    public Fixture scheduleFixture(String fixtureId, String homeRegNo, String awayRegNo, String stadiumId,
                                   LocalDate date, LocalTime time)
            throws InvalidDataException, NotFoundException, DuplicateEntryException,
                   TeamNotEligibleException, SchedulingConflictException {

        if (fixtureId == null || fixtureId.trim().isEmpty()) {
            throw new InvalidDataException("Fixture ID cannot be blank.");
        }
        for (Fixture existing : fixtures) {
            if (existing.getFixtureId().equalsIgnoreCase(fixtureId.trim())) {
                throw new DuplicateEntryException("A fixture with ID " + fixtureId.trim() + " already exists.");
            }
        }

        FootballTeam home = findTeam(homeRegNo);
        FootballTeam away = findTeam(awayRegNo);
        Stadium stadium = findStadium(stadiumId);

        // The Fixture constructor enforces BR1 and rejects a team playing itself or a missing date/time.
        Fixture fixture = new Fixture(fixtureId, home, away, stadium, date, time);

        // BR2 and the stadium clash: compare with every other fixture at the same date AND time.
        for (Fixture other : fixtures) {
            if (!fixture.clashesWith(other)) {
                continue;
            }
            if (other.involves(home)) {
                throw new SchedulingConflictException(home.getName() + " already plays in "
                        + other.getFixtureId() + " on " + date + " at " + time + ".");
            }
            if (other.involves(away)) {
                throw new SchedulingConflictException(away.getName() + " already plays in "
                        + other.getFixtureId() + " on " + date + " at " + time + ".");
            }
            if (other.getStadium().getStadiumId().equalsIgnoreCase(stadium.getStadiumId())) {
                throw new SchedulingConflictException(stadium.getName() + " is already booked for "
                        + other.getFixtureId() + " on " + date + " at " + time + ".");
            }
        }

        fixtures.add(fixture);
        return fixture;
    }

    // Assigns an official to a fixture. BR3 is checked against every fixture in the league.
    public void assignOfficial(String fixtureId, String officialId)
            throws InvalidDataException, NotFoundException, DuplicateEntryException,
                   FixtureStateException, SchedulingConflictException {
        Fixture fixture = findFixture(fixtureId);
        MatchOfficial official = findOfficial(officialId);
        fixture.assignOfficial(official, fixtures);
    }

    // ---------- Ticket sales (BR4, BR5) ----------

    /*
      Sells tickets for a fixture to a registered spectator. The fixture checks the quantity (BR5),
      that it has not been played, and the stadium's capacity (BR4); a refused sale issues nothing.
     */
    public List<Ticket> sellTickets(String fixtureId, String spectatorId, TicketCategory category, int quantity)
            throws InvalidDataException, NotFoundException, FixtureStateException, StadiumCapacityExceededException {
        Fixture fixture = findFixture(fixtureId);
        Spectator spectator = findSpectator(spectatorId);
        return fixture.sellTickets(spectator, category, quantity);
    }

    // ---------- Results and standings (BR5, BR8) ----------

    /*
      Records the final score, completes the fixture, and updates every team's points.
      A negative score (BR5) or a second result for the same fixture (BR8) is refused.
     */
    public void recordResult(String fixtureId, int homeGoals, int awayGoals)
            throws InvalidDataException, NotFoundException, FixtureStateException {
        Fixture fixture = findFixture(fixtureId);
        fixture.complete(new MatchResult(homeGoals, awayGoals));
        StandingsService.applyPointsToTeams(getStandings());
    }

    /** The league table, rebuilt from completed fixtures (win 3, draw 1, loss 0). */
    public List<StandingsEntry> getStandings() throws InvalidDataException {
        return StandingsService.buildStandings(teams, fixtures);
    }

    // ---------- Accessors ----------

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("League name cannot be blank.");
        }
        this.name = name.trim();
    }

    public LeagueCommissioner getCommissioner() {
        return commissioner;
    }

    public void setCommissioner(LeagueCommissioner commissioner) throws InvalidDataException {
        if (commissioner == null) {
            throw new InvalidDataException("Commissioner cannot be null.");
        }
        this.commissioner = commissioner;
    }

    public List<FootballTeam> getTeams() {
        return Collections.unmodifiableList(teams);
    }

    public List<Stadium> getStadiums() {
        return Collections.unmodifiableList(stadiums);
    }

    public List<MatchOfficial> getOfficials() {
        return Collections.unmodifiableList(officials);
    }

    public List<Fixture> getFixtures() {
        return Collections.unmodifiableList(fixtures);
    }

    public List<Spectator> getSpectators() {
        return Collections.unmodifiableList(spectators);
    }

    // The league's stadium shop (its products and completed sales).
    public StadiumShop getShop() {
        return shop;
    }

    private static String trim(String text) {
        return text == null ? "" : text.trim();
    }
}
