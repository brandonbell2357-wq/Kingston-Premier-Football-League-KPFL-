import exception.*;
import model.*;

import java.math.BigDecimal;


public class ModelTest {

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

    /** Passes only if the action throws the expected exception type (or a subclass). */
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

    private static Player player(String id, int jersey) throws Exception {
        return new Player(id, "Player " + id, 22, "Male", "Forward", jersey, "Active");
    }

    public static void main(String[] args) throws Exception {
        playerTests();
        personTests();
        coachTests();
        stadiumTests();
        teamTests();
        copyAndDefaultTests();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- Player (BR5, BR6) ----------
    private static void playerTests() throws Exception {
        int min = Player.MIN_AGE;

        check("player at minimum age is accepted (boundary)",
                new Player("P1", "Ann", min, "Female", "Forward", 9, "Active").getAge() == min);
        check("normal adult player accepted", player("P2", 10).getRole().equals("Player"));

        expect("player one year under minimum age", UnderageException.class,
                () -> new Player("P3", "Kid", min - 1, "Male", "Forward", 9, "Active"));
        expect("negative age is invalid but not 'underage'", InvalidDataException.class,
                () -> new Player("P4", "Neg", -1, "Male", "Forward", 9, "Active"));
        check("negative age is not an UnderageException", !throwsUnderage(-1));

        expect("jersey number 0", InvalidDataException.class,
                () -> new Player("P5", "A", 20, "Male", "Forward", 0, "Active"));
        check("jersey number 1 accepted (boundary)", player("P6", 1).getJerseyNumber() == 1);
        check("jersey number 99 accepted (boundary)", player("P7", 99).getJerseyNumber() == 99);
        expect("jersey number 100", InvalidDataException.class,
                () -> new Player("P8", "A", 20, "Male", "Forward", 100, "Active"));
        expect("blank position", InvalidDataException.class,
                () -> new Player("P9", "A", 20, "Male", "  ", 5, "Active"));
        expect("blank playing status", InvalidDataException.class,
                () -> new Player("P10", "A", 20, "Male", "Forward", 5, ""));
        expect("blank id", InvalidDataException.class,
                () -> new Player(" ", "A", 20, "Male", "Forward", 5, "Active"));
        expect("blank name", InvalidDataException.class,
                () -> new Player("P11", "", 20, "Male", "Forward", 5, "Active"));

        Player p = player("P12", 7);
        p.recordGoals(2);
        p.recordGoals(0);
        check("goals add up", p.getGoals() == 2);
        expect("negative goals", InvalidDataException.class, () -> p.recordGoals(-1));
        check("goals unchanged after bad input", p.getGoals() == 2);
        expect("setAge below minimum", UnderageException.class, () -> p.setAge(min - 1));
    }

    private static boolean throwsUnderage(int age) {
        try {
            new Player("PX", "X", age, "Male", "Forward", 5, "Active");
            return false;
        } catch (UnderageException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ---------- Person rules through other subclasses ----------
    private static void personTests() throws Exception {
        check("age 0 accepted for a spectator (boundary)", new Spectator("S1", "Baby", 0, "Male").getAge() == 0);
        check("max age accepted (boundary)", new Spectator("S2", "Old", Person.MAX_AGE, "Male").getAge() == Person.MAX_AGE);
        expect("age above maximum", InvalidDataException.class,
                () -> new Spectator("S3", "Too old", Person.MAX_AGE + 1, "Male"));
        expect("blank gender", InvalidDataException.class, () -> new Spectator("S4", "A", 20, " "));
        check("spectator role", new Spectator("S5", "A", 20, "Male").getRole().equals("Spectator"));
        check("describe includes the name", new Spectator("S6", "Zed", 20, "Male").describe().contains("Zed"));
    }

    // ---------- Coach ----------
    private static void coachTests() throws Exception {
        check("coach created", new Coach("C1", "Coach A", 40, "Male", "Level 2", "Head Coach").getRole().equals("Coach"));
        expect("blank qualification", InvalidDataException.class,
                () -> new Coach("C2", "Coach B", 40, "Male", "", "Head Coach"));
        expect("blank coaching role", InvalidDataException.class,
                () -> new Coach("C3", "Coach C", 40, "Male", "Level 2", " "));
    }

    // ---------- Stadium (BR4, BR5) ----------
    private static void stadiumTests() throws Exception {
        Stadium s = new Stadium("S1", "National", "Kingston", 100, "Good", new BigDecimal("1500.00"));
        check("stadium created", s.getSeatingCapacity() == 100);
        check("capacity check: below capacity", s.checkCapacity(99));
        check("capacity check: exactly full (boundary)", s.checkCapacity(100));
        check("capacity check: one over (boundary)", !s.checkCapacity(101));
        check("free ticket price 0 accepted", new Stadium("S2", "A", "B", 10, "Good", BigDecimal.ZERO).getTicketPrice().signum() == 0);

        expect("capacity 0", InvalidDataException.class,
                () -> new Stadium("S3", "A", "B", 0, "Good", BigDecimal.ONE));
        expect("negative capacity", InvalidDataException.class,
                () -> new Stadium("S4", "A", "B", -5, "Good", BigDecimal.ONE));
        expect("negative ticket price", InvalidDataException.class,
                () -> new Stadium("S5", "A", "B", 10, "Good", new BigDecimal("-1")));
        expect("null ticket price", InvalidDataException.class,
                () -> new Stadium("S6", "A", "B", 10, "Good", null));
        expect("blank stadium id", InvalidDataException.class,
                () -> new Stadium("", "A", "B", 10, "Good", BigDecimal.ONE));
        expect("blank stadium name", InvalidDataException.class,
                () -> new Stadium("S7", " ", "B", 10, "Good", BigDecimal.ONE));
    }

    // ---------- FootballTeam (BR1, BR5) ----------
    private static void teamTests() throws Exception {
        FootballTeam t = new FootballTeam("T1", "Alpha FC", "Kingston", 2);
        check("new team starts PENDING", t.getStatus() == TeamStatus.PENDING);
        check("pending team is not eligible for fixtures", !t.isEligibleForFixtures());
        new LeagueCommissioner("L1", "Boss", 50, "Male").approveTeam(t);
        check("approved team is REGISTERED and eligible", t.getStatus() == TeamStatus.REGISTERED && t.isEligibleForFixtures());
        new LeagueCommissioner("L1", "Boss", 50, "Male").suspendTeam(t);
        check("suspended team is not eligible", !t.isEligibleForFixtures());
        expect("approve a null team", InvalidDataException.class,
                () -> new LeagueCommissioner("L2", "Boss", 50, "Male").approveTeam(null));

        // Squad rules
        t.addPlayer(player("A1", 1));
        t.addPlayer(player("A2", 2));
        check("two players added", t.getPlayers().size() == 2);
        expect("squad full (boundary: size 2, adding 3rd)", SquadFullException.class,
                () -> t.addPlayer(player("A3", 3)));
        check("squad unchanged after rejection", t.getPlayers().size() == 2);

        FootballTeam t2 = new FootballTeam("T2", "Bravo FC", "St Andrew", 5);
        t2.addPlayer(player("B1", 10));
        expect("duplicate player id", DuplicateEntryException.class, () -> t2.addPlayer(player("B1", 11)));
        expect("duplicate jersey number", DuplicateEntryException.class, () -> t2.addPlayer(player("B2", 10)));
        expect("null player", InvalidDataException.class, () -> t2.addPlayer(null));

        t2.addCoach(new Coach("C1", "Coach A", 40, "Male", "Level 2", "Head Coach"));
        expect("duplicate coach id", DuplicateEntryException.class,
                () -> t2.addCoach(new Coach("C1", "Coach B", 41, "Male", "Level 1", "Assistant")));
        t2.removePlayer("B1");
        check("player removed", t2.getPlayers().isEmpty());
        expect("remove a player who is not there", NotFoundException.class, () -> t2.removePlayer("NOPE"));
        t2.removeCoach("C1");
        expect("remove a coach who is not there", NotFoundException.class, () -> t2.removeCoach("C1"));

        // Other rules
        expect("squad size 0", InvalidDataException.class, () -> new FootballTeam("T3", "A", "B", 0));
        expect("blank registration number", InvalidDataException.class, () -> new FootballTeam(" ", "A", "B", 5));
        expect("blank team name", InvalidDataException.class, () -> new FootballTeam("T4", "", "B", 5));
        expect("shrink squad below current players", InvalidDataException.class, () -> t.setSquadSize(1));
        expect("negative points", InvalidDataException.class, () -> t.setPoints(-1));
        expect("negative points to add", InvalidDataException.class, () -> t.addPoints(-3));
        t.addPoints(3);
        t.addPoints(1);
        check("points add up", t.getPoints() == 4);
        expect("null status", InvalidDataException.class, () -> t.setStatus(null));
    }

    // ---------- Default and copy constructors ----------
    private static void copyAndDefaultTests() throws Exception {
        check("default Player builds", new Player().getRole().equals("Player"));
        check("default Stadium builds", new Stadium() != null);
        check("default FootballTeam builds", new FootballTeam() != null);
        check("default Coach builds", new Coach() != null);

        Player original = player("Z1", 8);
        original.recordGoals(3);
        Player copy = new Player(original);
        check("player copy has the same data", copy.getId().equals("Z1") && copy.getGoals() == 3);
        copy.recordGoals(2);
        check("changing the copy does not change the original", original.getGoals() == 3);

        FootballTeam team = new FootballTeam("T9", "Zulu FC", "Kingston", 5);
        team.addPlayer(player("Z2", 4));
        FootballTeam teamCopy = new FootballTeam(team);
        teamCopy.addPlayer(player("Z3", 5));
        check("team copy is independent of the original", team.getPlayers().size() == 1 && teamCopy.getPlayers().size() == 2);

        Stadium st = new Stadium("S9", "Ground", "Kingston", 50, "Good", new BigDecimal("500"));
        Stadium stCopy = new Stadium(st);
        check("stadium copy has same data", stCopy.getStadiumId().equals("S9") && stCopy.getSeatingCapacity() == 50);
    }
}
