import exception.*;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/*
 * Tests for Fixture: BR1 (team eligibility), stadium occupancy and
 * BR3 (match official scheduling conflicts). Same style as ModelTest:
 * run it with "java -cp out FixtureTest".
 * Labels say Normal, Boundary or Invalid, as in the Week 1 test plan.
 */
public class FixtureTest {

    private static int passed = 0;
    private static int failed = 0;

    private static final LocalDate MATCH_DAY = LocalDate.of(2026, 11, 20);
    private static final LocalTime THREE_PM = LocalTime.of(15, 0);

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    // Passes only if the action throws the expected exception type (or a subclass)
    private static void expect(String name, Class<? extends Exception> type, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("FAIL: " + name + " (no exception thrown)");
        } catch (Exception e) {
            if (type.isInstance(e)) {
                passed++;
            } else {
                failed++;
                System.out.println("FAIL: " + name + " (threw " + e.getClass().getSimpleName() + ")");
            }
        }
    }

    // Passes only if the action runs WITHOUT throwing any exception.
    private static void expectNoError(String name, Action action) {
        try {
            action.run();
            passed++;
        } catch (Exception e) {
            failed++;
            System.out.println("FAIL: " + name + " (threw " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }

    // ---------- Test data helpers ----------

    private static final LeagueCommissioner COMMISSIONER = commissioner();

    private static LeagueCommissioner commissioner() {
        try {
            return new LeagueCommissioner("L1", "Commissioner", 50, "Male");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** A new team that has been approved (REGISTERED). */
    private static FootballTeam registeredTeam(String regNo, String name) throws Exception {
        FootballTeam team = new FootballTeam(regNo, name, "Kingston", 25);
        COMMISSIONER.approveTeam(team);
        return team;
    }

    private static Stadium stadium(int capacity) throws Exception {
        return new Stadium("S1", "National Stadium", "Kingston", capacity, "Good", new BigDecimal("1500.00"));
    }

    private static MatchOfficial referee() throws Exception {
        return new MatchOfficial("O1", "Kevin Brown", 40, "Male", "Referee", "FIFA");
    }

    public static void main(String[] args) throws Exception {
        br1Tests();
        fixtureDataTests();
        occupancyTests();
        br3Tests();
        copyTests();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- BR1: only registered teams ----------
    private static void br1Tests() throws Exception {
        FootballTeam home = registeredTeam("T1", "Harbour View FC");
        FootballTeam away = registeredTeam("T2", "Waterhouse FC");
        Stadium s = stadium(35000);

        Fixture f = new Fixture("F1", home, away, s, MATCH_DAY, THREE_PM);
        check("BR1 normal: two registered teams create a fixture", f.getHomeTeam() == home && f.getAwayTeam() == away);
        check("new fixture starts SCHEDULED", f.getStatus() == FixtureStatus.SCHEDULED);

        FootballTeam justApproved = registeredTeam("T3", "Portmore United");
        check("BR1 boundary: team approved a moment earlier is accepted",
                new Fixture("F2", home, justApproved, s, MATCH_DAY, THREE_PM).getAwayTeam() == justApproved);

        FootballTeam pending = new FootballTeam("T4", "Pending FC", "Kingston", 25);
        expect("BR1 invalid: pending HOME team", TeamNotEligibleException.class,
                () -> new Fixture("F3", pending, away, s, MATCH_DAY, THREE_PM));
        expect("BR1 invalid: pending AWAY team", TeamNotEligibleException.class,
                () -> new Fixture("F3", home, pending, s, MATCH_DAY, THREE_PM));

        FootballTeam suspended = registeredTeam("T5", "Suspended FC");
        COMMISSIONER.suspendTeam(suspended);
        expect("BR1 invalid: suspended HOME team", TeamNotEligibleException.class,
                () -> new Fixture("F4", suspended, away, s, MATCH_DAY, THREE_PM));
        expect("BR1 invalid: suspended AWAY team", TeamNotEligibleException.class,
                () -> new Fixture("F4", home, suspended, s, MATCH_DAY, THREE_PM));
    }

    // ---------- Other fixture data rules ----------
    private static void fixtureDataTests() throws Exception {
        FootballTeam home = registeredTeam("T1", "Harbour View FC");
        FootballTeam away = registeredTeam("T2", "Waterhouse FC");
        Stadium s = stadium(35000);

        expect("a team cannot play itself", InvalidDataException.class,
                () -> new Fixture("F1", home, home, s, MATCH_DAY, THREE_PM));
        expect("blank fixture id", InvalidDataException.class,
                () -> new Fixture(" ", home, away, s, MATCH_DAY, THREE_PM));
        expect("null home team", InvalidDataException.class,
                () -> new Fixture("F1", null, away, s, MATCH_DAY, THREE_PM));
        expect("no stadium", InvalidDataException.class,
                () -> new Fixture("F1", home, away, null, MATCH_DAY, THREE_PM));
        expect("no date", InvalidDataException.class,
                () -> new Fixture("F1", home, away, s, null, THREE_PM));
        expect("no time", InvalidDataException.class,
                () -> new Fixture("F1", home, away, s, MATCH_DAY, null));

        Fixture f = new Fixture("F1", home, away, s, MATCH_DAY, THREE_PM);
        check("involves the home team", f.involves(home));
        check("involves the away team", f.involves(away));
        check("does not involve another team", !f.involves(registeredTeam("T9", "Other FC")));
    }

    // ---------- Stadium assignment and occupancy (BR4, BR5 support) ----------
    private static void occupancyTests() throws Exception {
        FootballTeam home = registeredTeam("T1", "Harbour View FC");
        FootballTeam away = registeredTeam("T2", "Waterhouse FC");

        Fixture f = new Fixture("F1", home, away, stadium(10), MATCH_DAY, THREE_PM);
        check("new fixture has 0 seats taken", f.getOccupancy() == 0);
        check("new fixture has full capacity remaining", f.getSeatsRemaining() == 10);
        check("seats for exactly the capacity (boundary)", f.hasSeatsFor(10));
        check("no seats for one over capacity (boundary)", !f.hasSeatsFor(11));
        check("quantity 0 is not accepted (BR5)", !f.hasSeatsFor(0));
        check("negative quantity is not accepted (BR5)", !f.hasSeatsFor(-1));

        Stadium bigger = new Stadium("S2", "Sabina Park", "Kingston", 20000, "Good", new BigDecimal("1000.00"));
        f.setStadium(bigger);
        check("fixture moved to another stadium", f.getStadium() == bigger && f.getSeatsRemaining() == 20000);
        expect("cannot move fixture to no stadium", InvalidDataException.class, () -> f.setStadium(null));
        check("stadium unchanged after rejection", f.getStadium() == bigger);
    }

    // ---------- BR3: officials cannot be double-booked ----------
    private static void br3Tests() throws Exception {
        FootballTeam t1 = registeredTeam("T1", "Harbour View FC");
        FootballTeam t2 = registeredTeam("T2", "Waterhouse FC");
        FootballTeam t3 = registeredTeam("T3", "Arnett Gardens FC");
        FootballTeam t4 = registeredTeam("T4", "Cavalier SC");
        Stadium s = stadium(35000);

        // Normal: two different days
        MatchOfficial ref = referee();
        Fixture day1 = new Fixture("F1", t1, t2, s, MATCH_DAY, THREE_PM);
        Fixture day2 = new Fixture("F2", t3, t4, s, MATCH_DAY.plusDays(7), THREE_PM);
        List<Fixture> twoDays = List.of(day1, day2);
        expectNoError("BR3 normal: assign official to day 1", () -> day1.assignOfficial(ref, twoDays));
        expectNoError("BR3 normal: same official on day 2", () -> day2.assignOfficial(ref, twoDays));
        check("BR3 normal: official on two different days", day1.hasOfficial(ref) && day2.hasOfficial(ref));

        // Boundary: same day, 15:00 and 15:01
        MatchOfficial ref2 = referee();
        Fixture at1500 = new Fixture("F3", t1, t2, s, MATCH_DAY, THREE_PM);
        Fixture at1501 = new Fixture("F4", t3, t4, s, MATCH_DAY, LocalTime.of(15, 1));
        List<Fixture> sameDay = List.of(at1500, at1501);
        expectNoError("BR3 boundary: assign official at 15:00", () -> at1500.assignOfficial(ref2, sameDay));
        expectNoError("BR3 boundary: same official at 15:01", () -> at1501.assignOfficial(ref2, sameDay));
        check("BR3 boundary: same day at 15:00 and 15:01 is accepted", at1501.hasOfficial(ref2));

        // Invalid: same date and time
        MatchOfficial ref3 = referee();
        Fixture first = new Fixture("F5", t1, t2, s, MATCH_DAY, THREE_PM);
        Fixture clash = new Fixture("F6", t3, t4, s, MATCH_DAY, THREE_PM);
        List<Fixture> clashing = List.of(first, clash);
        first.assignOfficial(ref3, clashing);
        expect("BR3 invalid: same date and time", SchedulingConflictException.class,
                () -> clash.assignOfficial(ref3, clashing));
        check("officials unchanged after rejection", clash.getOfficials().isEmpty());

        // Other official rules
        expect("same official twice on one fixture", DuplicateEntryException.class,
                () -> first.assignOfficial(ref3, clashing));
        check("still only one official after duplicate", first.getOfficials().size() == 1);

        MatchOfficial busy = new MatchOfficial("O2", "Busy Official", 45, "Female", "Assistant", "Local");
        busy.setAvailable(false);
        expect("unavailable official", InvalidDataException.class, () -> clash.assignOfficial(busy, clashing));
        expect("null official", InvalidDataException.class, () -> clash.assignOfficial(null, clashing));

        first.removeOfficial("O1");
        check("official removed", first.getOfficials().isEmpty());
        expect("remove an official who is not assigned", NotFoundException.class, () -> first.removeOfficial("O1"));
        expect("officials list cannot be changed from outside", UnsupportedOperationException.class,
                () -> first.getOfficials().add(ref3));
    }

    // ---------- Copy constructor ----------
    private static void copyTests() throws Exception {
        FootballTeam home = registeredTeam("T1", "Harbour View FC");
        FootballTeam away = registeredTeam("T2", "Waterhouse FC");
        Fixture original = new Fixture("F1", home, away, stadium(35000), MATCH_DAY, THREE_PM);
        MatchOfficial ref = referee();
        original.assignOfficial(ref, List.of(original));

        Fixture copy = new Fixture(original);
        check("copy has the same id and status",
                copy.getFixtureId().equals("F1") && copy.getStatus() == original.getStatus());
        check("copy shares the same teams and stadium (association)",
                copy.getHomeTeam() == home && copy.getStadium() == original.getStadium());
        check("copy shares the same official object", copy.getOfficials().get(0) == ref);
    }
}
