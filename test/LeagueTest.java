import exception.*;
import model.*;
import service.SampleData;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Week 3 league tests (plain Java, no libraries): scheduling rules BR1 to BR3, results BR5 and BR8,
 * and the standings (win 3, draw 1, loss 0). Same style as ModelTest and FixtureTest.
 * Run from the project root after compiling src into "out":
 *   javac -d out -cp out test/LeagueTest.java
 *   java -cp out LeagueTest
 */
public class LeagueTest {

    private static int passed = 0;
    private static int failed = 0;

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

    private static final LocalDate D1 = LocalDate.of(2026, 12, 5);
    private static final LocalDate D2 = LocalDate.of(2026, 12, 12);
    private static final LocalTime T1 = LocalTime.of(15, 0);
    private static final LocalTime T2 = LocalTime.of(18, 0);

    // Sample league: T01-T04 REGISTERED, T05 PENDING, T06 SUSPENDED; stadiums S01, S02; officials O01-O03.
    private static FootballLeague league() throws Exception {
        return SampleData.createLeague();
    }

    public static void main(String[] args) throws Exception {
        schedulingTests();
        officialTests();
        resultTests();
        standingsTests();
        registrationTests();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- Scheduling: BR1, BR2, stadium clash ----------
    private static void schedulingTests() throws Exception {
        FootballLeague l = league();
        Fixture f1 = l.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        check("valid fixture is stored", l.getFixtures().size() == 1);
        check("new fixture is SCHEDULED with no result", f1.getStatus() == FixtureStatus.SCHEDULED && f1.getResult() == null);

        // BR1
        expect("BR1 pending home team", TeamNotEligibleException.class, () -> l.scheduleFixture("F2", "T05", "T03", "S02", D2, T1));
        expect("BR1 suspended away team", TeamNotEligibleException.class, () -> l.scheduleFixture("F2", "T03", "T06", "S02", D2, T1));
        check("a refused fixture is not stored", l.getFixtures().size() == 1);

        // BR1 boundary: a team approved a moment earlier is accepted immediately.
        l.findTeam("T05").setStatus(TeamStatus.REGISTERED);
        l.scheduleFixture("F2", "T05", "T03", "S02", D2, T1);
        check("newly approved team accepted straight away", l.getFixtures().size() == 2);

        // BR2
        expect("BR2 home team already playing", SchedulingConflictException.class, () -> l.scheduleFixture("F3", "T01", "T03", "S02", D1, T1));
        expect("BR2 away team already playing", SchedulingConflictException.class, () -> l.scheduleFixture("F3", "T03", "T02", "S02", D1, T1));
        expect("BR2 team plays as home in one and away in the other", SchedulingConflictException.class,
                () -> l.scheduleFixture("F3", "T03", "T01", "S02", D1, T1));
        // BR2 boundaries: same day at another time is fine, even one minute later.
        l.scheduleFixture("F3", "T01", "T03", "S02", D1, T2);
        l.scheduleFixture("F4", "T02", "T04", "S02", D1, LocalTime.of(15, 1));
        check("same day, different time accepted (boundary 15:01)", l.getFixtures().size() == 4);
        l.scheduleFixture("F5", "T01", "T02", "S01", D2.plusDays(7), T1);
        check("same teams on another date accepted", l.getFixtures().size() == 5);

        // Stadium clash
        expect("stadium already booked", SchedulingConflictException.class, () -> l.scheduleFixture("F6", "T03", "T04", "S01", D1, T1));

        // Invalid input
        expect("team plays itself", InvalidDataException.class, () -> l.scheduleFixture("F6", "T01", "T01", "S02", D2, T1));
        expect("blank fixture id", InvalidDataException.class, () -> l.scheduleFixture(" ", "T03", "T04", "S02", D2, T1));
        expect("missing date", InvalidDataException.class, () -> l.scheduleFixture("F6", "T03", "T04", "S02", null, T1));
        expect("missing time", InvalidDataException.class, () -> l.scheduleFixture("F6", "T03", "T04", "S02", D2, null));
        expect("duplicate fixture id", DuplicateEntryException.class, () -> l.scheduleFixture("F1", "T03", "T04", "S02", D2, T2));
        expect("unknown team", NotFoundException.class, () -> l.scheduleFixture("F6", "T03", "ZZZ", "S02", D2, T1));
        expect("unknown stadium", NotFoundException.class, () -> l.scheduleFixture("F6", "T03", "T04", "ZZZ", D2, T1));
        check("lookups ignore case", l.findTeam("t01").getName().equals("Harbour View FC"));
    }

    // ---------- Officials: BR3 ----------
    private static void officialTests() throws Exception {
        FootballLeague l = league();
        l.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        l.scheduleFixture("F2", "T03", "T04", "S02", D1, T1);       // same date and time as F1
        l.scheduleFixture("F3", "T03", "T04", "S02", D2, T1);       // another day

        l.assignOfficial("F1", "O01");
        check("official assigned", l.findFixture("F1").hasOfficial(l.findOfficial("O01")));
        expect("BR3 same official, same date and time", SchedulingConflictException.class, () -> l.assignOfficial("F2", "O01"));
        check("refused assignment changes nothing", l.findFixture("F2").getOfficials().isEmpty());
        l.assignOfficial("F2", "O02");                               // a different official is fine
        l.assignOfficial("F3", "O01");                               // same official on a different day is fine
        check("same official on another day accepted", l.findFixture("F3").getOfficials().size() == 1);
        expect("same official twice on one fixture", DuplicateEntryException.class, () -> l.assignOfficial("F1", "O01"));
        expect("unknown official", NotFoundException.class, () -> l.assignOfficial("F1", "ZZZ"));
        expect("unknown fixture", NotFoundException.class, () -> l.assignOfficial("ZZZ", "O03"));

        l.findOfficial("O03").setAvailable(false);
        expect("unavailable official", InvalidDataException.class, () -> l.assignOfficial("F3", "O03"));

        l.recordResult("F1", 1, 0);
        expect("cannot assign an official to a completed fixture", FixtureStateException.class, () -> l.assignOfficial("F1", "O02"));
    }

    // ---------- Results: BR5 and BR8 ----------
    private static void resultTests() throws Exception {
        FootballLeague l = league();
        Fixture f = l.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);

        expect("BR5 negative home score", InvalidDataException.class, () -> l.recordResult("F1", -1, 0));
        expect("BR5 negative away score", InvalidDataException.class, () -> l.recordResult("F1", 0, -2));
        check("a refused result leaves the fixture SCHEDULED", f.getStatus() == FixtureStatus.SCHEDULED);
        expect("BR8 complete without a result", FixtureStateException.class, () -> f.complete(null));
        expect("result for an unknown fixture", NotFoundException.class, () -> l.recordResult("ZZZ", 1, 0));

        l.recordResult("F1", 2, 1);
        check("fixture is COMPLETED with the score", f.getStatus() == FixtureStatus.COMPLETED
                && f.getResult().getHomeGoals() == 2 && f.getResult().getAwayGoals() == 1);
        expect("BR8 cannot record a second result", FixtureStateException.class, () -> l.recordResult("F1", 5, 5));
        check("the first result is kept", f.getResult().getHomeGoals() == 2);

        l.scheduleFixture("F2", "T03", "T04", "S01", D2, T1);
        l.recordResult("F2", 0, 0);
        check("0-0 is a valid draw (boundary)", l.findFixture("F2").getResult().isDraw());
    }

    // ---------- Standings: win 3, draw 1, loss 0 ----------
    private static void standingsTests() throws Exception {
        FootballLeague l = league();
        l.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);   // T01 2-1 T02
        l.scheduleFixture("F2", "T03", "T04", "S02", D1, T1);   // T03 0-0 T04
        l.scheduleFixture("F3", "T01", "T03", "S01", D2, T1);   // not played

        List<StandingsEntry> before = l.getStandings();
        check("every team is listed, even with no matches", before.size() == 6);
        check("no points before any result", before.get(0).getPoints() == 0);

        l.recordResult("F1", 2, 1);
        l.recordResult("F2", 0, 0);
        List<StandingsEntry> table = l.getStandings();

        check("leader is Harbour View FC", table.get(0).getTeam().getName().equals("Harbour View FC"));
        check("win = 3 points", table.get(0).getPoints() == 3 && table.get(0).getWon() == 1);
        StandingsEntry t03 = row(table, "T03");
        check("draw = 1 point", t03.getPoints() == 1 && t03.getDrawn() == 1);
        StandingsEntry t02 = row(table, "T02");
        check("loss = 0 points", t02.getPoints() == 0 && t02.getLost() == 1);
        check("goal difference", table.get(0).getGoalDifference() == 1 && t02.getGoalDifference() == -1);
        check("scheduled fixtures do not count", row(table, "T01").getPlayed() == 1);
        check("team points follow the table", l.findTeam("T01").getPoints() == 3 && l.findTeam("T03").getPoints() == 1);
        check("rebuilding the table gives the same result", l.getStandings().get(0).getPoints() == 3);
    }

    private static StandingsEntry row(List<StandingsEntry> table, String regNo) {
        for (StandingsEntry e : table) {
            if (e.getTeam().getRegistrationNumber().equals(regNo)) {
                return e;
            }
        }
        throw new IllegalStateException("Team not in table: " + regNo);
    }

    // ---------- Registering and searching ----------
    private static void registrationTests() throws Exception {
        FootballLeague l = league();
        expect("duplicate team", DuplicateEntryException.class, () -> l.registerTeam(new FootballTeam("T01", "Other", "Kingston", 20)));
        expect("duplicate stadium", DuplicateEntryException.class,
                () -> l.addStadium(new Stadium("S01", "X", "Y", 100, "Good", java.math.BigDecimal.ONE)));
        expect("duplicate official", DuplicateEntryException.class,
                () -> l.addOfficial(new MatchOfficial("O01", "Dup", 30, "Male", "Referee", "Senior")));
        expect("null team", InvalidDataException.class, () -> l.registerTeam(null));
        expect("unknown team lookup", NotFoundException.class, () -> l.findTeam("NOPE"));
        expect("blank league name", InvalidDataException.class, () -> new FootballLeague(" "));

        check("search by name", l.searchTeams("harbour").size() == 1);
        check("search by parish", l.searchTeams("kingston").size() == 4);
        check("search with no match", l.searchTeams("zzz").isEmpty());

        l.registerTeam(new FootballTeam("T07", "New FC", "Clarendon", 20));
        check("new team starts PENDING", l.findTeam("T07").getStatus() == TeamStatus.PENDING);
        l.getCommissioner().approveTeam(l.findTeam("T07"));
        check("commissioner approves a team", l.findTeam("T07").isEligibleForFixtures());
        l.getCommissioner().suspendTeam(l.findTeam("T07"));
        check("commissioner suspends a team", !l.findTeam("T07").isEligibleForFixtures());
    }
}
