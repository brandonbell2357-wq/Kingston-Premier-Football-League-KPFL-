import exception.*;
import model.*;
import service.SampleData;
import storage.LeagueFileStorage;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Tests for saving and reloading the league (plain Java, no libraries).
 * Every test uses a fresh temporary folder, so no real data is touched.
 * Run from the project root after compiling src into "out":
 *   javac -d out -cp out test/StorageTest.java
 *   java -cp out StorageTest
 */
public class StorageTest {

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

    private static Path tempFolder() throws Exception {
        return Files.createTempDirectory("kpfl-test");
    }

    // Overwrites one line of a saved file, to create damaged data.
    private static void replaceLine(Path folder, String file, int index, String text) throws Exception {
        Path path = folder.resolve(file);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        lines.set(index, text);
        Files.write(path, lines, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws Exception {
        sampleRoundTrip();
        busyLeagueRoundTrip();
        specialCharacters();
        emptyAndPartialLeagues();
        savingRules();
        missingAndDamagedFiles();
        rulesAreCheckedOnLoad();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- The sample league survives save and load ----------
    private static void sampleRoundTrip() throws Exception {
        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        check("no saved data in a new folder", !storage.hasSavedData());

        FootballLeague original = SampleData.createLeague();
        storage.save(original);
        check("saved data is found after saving", storage.hasSavedData());

        FootballLeague loaded = storage.load();
        check("league name", loaded.getName().equals(original.getName()));
        check("commissioner", loaded.getCommissioner().getName().equals("Marcus Reid"));
        check("team count", loaded.getTeams().size() == original.getTeams().size());
        check("stadium count", loaded.getStadiums().size() == 2);
        check("official count", loaded.getOfficials().size() == 3);

        FootballTeam t = loaded.findTeam("T01");
        check("team fields", t.getName().equals("Harbour View FC") && t.getParish().equals("Kingston") && t.getSquadSize() == 25);
        check("team status kept", t.getStatus() == TeamStatus.REGISTERED);
        check("pending team stays pending", loaded.findTeam("T05").getStatus() == TeamStatus.PENDING);
        check("suspended team stays suspended", loaded.findTeam("T06").getStatus() == TeamStatus.SUSPENDED);
        check("players loaded", t.getPlayers().size() == 2 && t.getPlayers().get(0).getJerseyNumber() == 9);
        check("coaches loaded", t.getCoaches().size() == 1);
        check("stadium price keeps its decimals", loaded.findStadium("S01").getTicketPrice().toPlainString().equals("1500.00"));
        check("stadium capacity", loaded.findStadium("S01").getSeatingCapacity() == 35000);
        check("loaded objects are separate from the original", loaded.findTeam("T01") != original.findTeam("T01"));
    }

    // ---------- Fixtures, results, officials, points and availability are all kept ----------
    private static void busyLeagueRoundTrip() throws Exception {
        FootballLeague l = SampleData.createLeague();
        l.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        l.scheduleFixture("F2", "T03", "T04", "S02", D1, T1);
        l.scheduleFixture("F3", "T01", "T03", "S01", D2, T1);
        l.assignOfficial("F1", "O01");
        l.assignOfficial("F1", "O03");
        l.assignOfficial("F2", "O02");
        l.recordResult("F1", 2, 1);
        l.recordResult("F2", 0, 0);
        l.findOfficial("O03").setAvailable(false);
        l.findTeam("T03").setStatus(TeamStatus.SUSPENDED);     // suspended after playing and being scheduled
        l.findTeam("T01").getPlayers().get(0).recordGoals(2);

        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        storage.save(l);
        FootballLeague loaded = storage.load();

        check("fixture count", loaded.getFixtures().size() == 3);
        Fixture f1 = loaded.findFixture("F1");
        check("completed fixture keeps its status", f1.getStatus() == FixtureStatus.COMPLETED);
        check("result kept", f1.getResult().getHomeGoals() == 2 && f1.getResult().getAwayGoals() == 1);
        check("date and time kept", f1.getDate().equals(D1) && f1.getTime().equals(T1));
        check("stadium kept", f1.getStadium().getStadiumId().equals("S01"));
        check("officials kept", f1.getOfficials().size() == 2 && f1.hasOfficial(loaded.findOfficial("O01")));
        check("0-0 draw kept", loaded.findFixture("F2").getResult().isDraw());
        check("scheduled fixture stays scheduled", loaded.findFixture("F3").getStatus() == FixtureStatus.SCHEDULED
                && loaded.findFixture("F3").getResult() == null);
        check("team suspended after scheduling is still suspended", loaded.findTeam("T03").getStatus() == TeamStatus.SUSPENDED);
        check("official availability kept", !loaded.findOfficial("O03").isAvailable() && loaded.findOfficial("O01").isAvailable());
        check("player goals kept", loaded.findTeam("T01").getPlayers().get(0).getGoals() == 2);
        check("team points kept", loaded.findTeam("T01").getPoints() == 3 && loaded.findTeam("T03").getPoints() == 1);

        List<StandingsEntry> before = l.getStandings();
        List<StandingsEntry> after = loaded.getStandings();
        boolean same = before.size() == after.size();
        for (int i = 0; same && i < before.size(); i++) {
            same = before.get(i).getTeam().getName().equals(after.get(i).getTeam().getName())
                    && before.get(i).getPoints() == after.get(i).getPoints()
                    && before.get(i).getGoalDifference() == after.get(i).getGoalDifference();
        }
        check("standings are identical after reloading", same);

        // The reloaded league still enforces the rules.
        expect("BR2 still enforced after reloading", SchedulingConflictException.class,
                () -> loaded.scheduleFixture("F9", "T01", "T04", "S02", D2, T1));
        expect("BR8 still enforced after reloading", FixtureStateException.class, () -> loaded.recordResult("F1", 9, 9));

        // Saving the reloaded league and loading again gives the same data (stable round trip).
        Path folder2 = tempFolder();
        LeagueFileStorage storage2 = new LeagueFileStorage(folder2);
        storage2.save(loaded);
        check("second save is identical to the first",
                Files.readAllLines(folder.resolve("fixtures.txt")).equals(Files.readAllLines(folder2.resolve("fixtures.txt")))
                        && Files.readAllLines(folder.resolve("teams.txt")).equals(Files.readAllLines(folder2.resolve("teams.txt"))));
    }

    // ---------- Text that could break a record ----------
    private static void specialCharacters() throws Exception {
        FootballLeague l = new FootballLeague("League | with \\ odd chars");
        FootballTeam team = new FootballTeam("X1", "Pipes | United", "St. Mary\\Parish", 10);
        team.addPlayer(new Player("X1-P1", "Ann | Bar\\ker", 20, "Female", "Mid|field", 5, "Active"));
        l.registerTeam(team);

        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        storage.save(l);
        FootballLeague loaded = storage.load();
        check("league name with | and \\", loaded.getName().equals("League | with \\ odd chars"));
        check("team name with |", loaded.findTeam("X1").getName().equals("Pipes | United"));
        check("parish with backslash", loaded.findTeam("X1").getParish().equals("St. Mary\\Parish"));
        check("player name and position with | and \\",
                loaded.findTeam("X1").getPlayers().get(0).getName().equals("Ann | Bar\\ker")
                        && loaded.findTeam("X1").getPlayers().get(0).getPosition().equals("Mid|field"));
        check("each team is still one line", Files.readAllLines(folder.resolve("teams.txt")).size() == 1);
    }

    // ---------- Empty league and a league without a commissioner ----------
    private static void emptyAndPartialLeagues() throws Exception {
        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        storage.save(new FootballLeague("Empty League"));
        FootballLeague loaded = storage.load();
        check("empty league loads", loaded.getTeams().isEmpty() && loaded.getFixtures().isEmpty()
                && loaded.getStadiums().isEmpty() && loaded.getOfficials().isEmpty());
        check("no commissioner stays empty", loaded.getCommissioner() == null);
        check("empty standings", loaded.getStandings().isEmpty());
    }

    // ---------- Saving rules ----------
    private static void savingRules() throws Exception {
        Path folder = tempFolder().resolve("nested").resolve("saved");   // folder does not exist yet
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        storage.save(SampleData.createLeague());
        check("save creates a missing folder", Files.isDirectory(folder));

        FootballLeague second = new FootballLeague("Second League");
        storage.save(second);
        check("saving again replaces the old data", storage.load().getName().equals("Second League")
                && storage.load().getTeams().isEmpty());

        boolean leftovers = false;
        try (var files = Files.list(folder)) {
            leftovers = files.anyMatch(p -> p.getFileName().toString().endsWith(".tmp"));
        }
        check("no temporary files are left behind", !leftovers);
        expect("saving nothing", DataFileException.class, () -> storage.save(null));

        Path aFile = Files.createTempFile("kpfl", ".txt");
        expect("cannot save into a path that is a file", DataFileException.class,
                () -> new LeagueFileStorage(aFile.resolve("sub")).save(SampleData.createLeague()));
    }

    // ---------- Missing and damaged files ----------
    private static void missingAndDamagedFiles() throws Exception {
        expect("load from a folder with no data", DataFileException.class,
                () -> new LeagueFileStorage(tempFolder()).load());

        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        storage.save(SampleData.createLeague());
        Files.delete(folder.resolve("players.txt"));
        expect("one data file missing", DataFileException.class, storage::load);

        Path f1 = tempFolder();
        new LeagueFileStorage(f1).save(SampleData.createLeague());
        replaceLine(f1, "teams.txt", 0, "T01|only three|fields");
        expect("wrong number of fields", DataFileException.class, () -> new LeagueFileStorage(f1).load());

        Path f2 = tempFolder();
        new LeagueFileStorage(f2).save(SampleData.createLeague());
        replaceLine(f2, "stadiums.txt", 0, "S01|National|Kingston|lots|Good|1500.00");
        expect("a number that is not a number", DataFileException.class, () -> new LeagueFileStorage(f2).load());

        Path f3 = tempFolder();
        new LeagueFileStorage(f3).save(SampleData.createLeague());
        replaceLine(f3, "teams.txt", 0, "T01|Harbour View FC|Kingston|25|ACTIVE|0");
        expect("an unknown team status", DataFileException.class, () -> new LeagueFileStorage(f3).load());

        Path f4 = tempFolder();
        FootballLeague withFixture = SampleData.createLeague();
        withFixture.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        new LeagueFileStorage(f4).save(withFixture);
        replaceLine(f4, "fixtures.txt", 0, "F1|T01|T02|S01|05-12-2026|15:00|SCHEDULED|||");
        expect("a badly written date", DataFileException.class, () -> new LeagueFileStorage(f4).load());

        Path f5 = tempFolder();
        new LeagueFileStorage(f5).save(SampleData.createLeague());
        replaceLine(f5, "officials.txt", 0, "O01|Daniel Brown|38|Male|Referee|Senior|maybe");
        expect("a true/false value that is neither", DataFileException.class, () -> new LeagueFileStorage(f5).load());

        Path f6 = tempFolder();
        new LeagueFileStorage(f6).save(SampleData.createLeague());
        Files.write(f6.resolve("league.txt"), List.of(""), StandardCharsets.UTF_8);
        expect("an empty league file", DataFileException.class, () -> new LeagueFileStorage(f6).load());

        Path f7 = tempFolder();
        new LeagueFileStorage(f7).save(SampleData.createLeague());
        Files.write(f7.resolve("teams.txt"), List.of("", "   ", ""), StandardCharsets.UTF_8);
        check("blank lines are ignored", loadsWithoutError(f7));
    }

    private static boolean loadsWithoutError(Path folder) {
        try {
            // teams.txt is blank, so the players refer to teams that do not exist: that must still be reported.
            new LeagueFileStorage(folder).load();
            return false;
        } catch (DataFileException e) {
            return e.getMessage().contains("T01");
        }
    }

    // ---------- Business rules are checked again on load ----------
    private static void rulesAreCheckedOnLoad() throws Exception {
        Path f1 = tempFolder();
        new LeagueFileStorage(f1).save(SampleData.createLeague());
        replaceLine(f1, "players.txt", 0, "T01|T01-P1|Too Young|10|Male|Forward|9|Active|0");
        expect("a player under the minimum age", DataFileException.class, () -> new LeagueFileStorage(f1).load());

        Path f2 = tempFolder();
        new LeagueFileStorage(f2).save(SampleData.createLeague());
        replaceLine(f2, "players.txt", 0, "T99|T01-P1|Lost Player|24|Male|Forward|9|Active|0");
        expect("a player on a team that does not exist", DataFileException.class, () -> new LeagueFileStorage(f2).load());

        Path f3 = tempFolder();
        new LeagueFileStorage(f3).save(SampleData.createLeague());
        List<String> teams = Files.readAllLines(f3.resolve("teams.txt"));
        teams.add(teams.get(0));
        Files.write(f3.resolve("teams.txt"), teams, StandardCharsets.UTF_8);
        expect("the same team twice", DataFileException.class, () -> new LeagueFileStorage(f3).load());

        Path f4 = tempFolder();
        FootballLeague l = SampleData.createLeague();
        l.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        l.scheduleFixture("F2", "T03", "T04", "S02", D2, T1);
        new LeagueFileStorage(f4).save(l);
        List<String> fixtures = Files.readAllLines(f4.resolve("fixtures.txt"));
        fixtures.set(1, "F2|T01|T03|S02|2026-12-05|15:00|SCHEDULED|||");   // T01 is already playing then (BR2)
        Files.write(f4.resolve("fixtures.txt"), fixtures, StandardCharsets.UTF_8);
        expect("two fixtures for one team at the same time", DataFileException.class, () -> new LeagueFileStorage(f4).load());

        Path f5 = tempFolder();
        FootballLeague l5 = SampleData.createLeague();
        l5.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        new LeagueFileStorage(f5).save(l5);
        replaceLine(f5, "fixtures.txt", 0, "F1|T01|T02|S01|2026-12-05|15:00|COMPLETED|-1|0|");
        expect("a negative score", DataFileException.class, () -> new LeagueFileStorage(f5).load());

        Path f6 = tempFolder();
        FootballLeague l6 = SampleData.createLeague();
        l6.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        new LeagueFileStorage(f6).save(l6);
        replaceLine(f6, "fixtures.txt", 0, "F1|T01|T02|S01|2026-12-05|15:00|SCHEDULED|||O77");
        expect("a fixture with an official that does not exist", DataFileException.class, () -> new LeagueFileStorage(f6).load());
    }
}