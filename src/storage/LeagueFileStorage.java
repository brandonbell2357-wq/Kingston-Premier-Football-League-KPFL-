package storage;

import exception.DataFileException;
import exception.KPFLException;
import model.Coach;
import model.Fixture;
import model.FixtureStatus;
import model.FootballLeague;
import model.FootballTeam;
import model.LeagueCommissioner;
import model.MatchOfficial;
import model.Player;
import model.Stadium;
import model.TeamStatus;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/*
  Saves and reloads the league's data using sequential text files (one record per line,
  fields separated by "|").

  Files in the chosen folder:
    league.txt     league name and commissioner
    stadiums.txt   one stadium per line
    officials.txt  one match official per line
    teams.txt      one team per line
    players.txt    one player per line, with the registration number of their team
    coaches.txt    one coach per line, with the registration number of their team
    fixtures.txt   one fixture per line, with its result and assigned officials

  Standings are not stored: they are rebuilt from the completed fixtures.
  Text containing "|", a backslash or a line break is escaped so it cannot break a record.

  Saving writes every file to a temporary name first and only then replaces the real files,
  so a failure while writing does not leave a half-written file behind.
  Loading rebuilds the objects through their constructors and the league's own methods, so every
  business rule is checked again and damaged data is reported as a DataFileException.
 */
public class LeagueFileStorage {

    public static final String LEAGUE_FILE = "league.txt";
    public static final String STADIUMS_FILE = "stadiums.txt";
    public static final String OFFICIALS_FILE = "officials.txt";
    public static final String TEAMS_FILE = "teams.txt";
    public static final String PLAYERS_FILE = "players.txt";
    public static final String COACHES_FILE = "coaches.txt";
    public static final String FIXTURES_FILE = "fixtures.txt";

    private static final String[] ALL_FILES = {
            LEAGUE_FILE, STADIUMS_FILE, OFFICIALS_FILE, TEAMS_FILE, PLAYERS_FILE, COACHES_FILE, FIXTURES_FILE
    };

    private static final String SEPARATOR = "|";

    private final Path directory;

    public LeagueFileStorage(Path directory) {
        this.directory = directory;
    }

    public Path getDirectory() {
        return directory;
    }

    /** True if a previous save exists in the folder. */
    public boolean hasSavedData() {
        return Files.exists(directory.resolve(LEAGUE_FILE));
    }

    // =====================================================================
    // Saving
    // =====================================================================

    public void save(FootballLeague league) throws DataFileException {
        if (league == null) {
            throw new DataFileException("There is no league to save.");
        }

        List<String> leagueLines = new ArrayList<>();
        LeagueCommissioner c = league.getCommissioner();
        if (c == null) {
            leagueLines.add(line(league.getName(), "", "", "", ""));
        } else {
            leagueLines.add(line(league.getName(), c.getId(), c.getName(), c.getAge(), c.getGender()));
        }

        List<String> stadiumLines = new ArrayList<>();
        for (Stadium s : league.getStadiums()) {
            stadiumLines.add(line(s.getStadiumId(), s.getName(), s.getLocation(), s.getSeatingCapacity(),
                    s.getFieldCondition(), s.getTicketPrice().toPlainString()));
        }

        List<String> officialLines = new ArrayList<>();
        for (MatchOfficial o : league.getOfficials()) {
            officialLines.add(line(o.getId(), o.getName(), o.getAge(), o.getGender(),
                    o.getOfficialRole(), o.getRank(), o.isAvailable()));
        }

        List<String> teamLines = new ArrayList<>();
        List<String> playerLines = new ArrayList<>();
        List<String> coachLines = new ArrayList<>();
        for (FootballTeam t : league.getTeams()) {
            teamLines.add(line(t.getRegistrationNumber(), t.getName(), t.getParish(), t.getSquadSize(),
                    t.getStatus(), t.getPoints()));
            for (Player p : t.getPlayers()) {
                playerLines.add(line(t.getRegistrationNumber(), p.getId(), p.getName(), p.getAge(), p.getGender(),
                        p.getPosition(), p.getJerseyNumber(), p.getPlayingStatus(), p.getGoals()));
            }
            for (Coach k : t.getCoaches()) {
                coachLines.add(line(t.getRegistrationNumber(), k.getId(), k.getName(), k.getAge(), k.getGender(),
                        k.getQualification(), k.getCoachingRole()));
            }
        }

        List<String> fixtureLines = new ArrayList<>();
        for (Fixture f : league.getFixtures()) {
            StringBuilder officialIds = new StringBuilder();
            for (MatchOfficial o : f.getOfficials()) {
                if (officialIds.length() > 0) {
                    officialIds.append(',');
                }
                officialIds.append(o.getId());
            }
            String homeGoals = f.getResult() == null ? "" : String.valueOf(f.getResult().getHomeGoals());
            String awayGoals = f.getResult() == null ? "" : String.valueOf(f.getResult().getAwayGoals());
            fixtureLines.add(line(f.getFixtureId(), f.getHomeTeam().getRegistrationNumber(),
                    f.getAwayTeam().getRegistrationNumber(), f.getStadium().getStadiumId(), f.getDate(),
                    f.getTime(), f.getStatus(), homeGoals, awayGoals, officialIds.toString()));
        }

        try {
            Files.createDirectories(directory);
            // Write everything to temporary files first...
            write(LEAGUE_FILE, leagueLines);
            write(STADIUMS_FILE, stadiumLines);
            write(OFFICIALS_FILE, officialLines);
            write(TEAMS_FILE, teamLines);
            write(PLAYERS_FILE, playerLines);
            write(COACHES_FILE, coachLines);
            write(FIXTURES_FILE, fixtureLines);
            // ...then replace the real files.
            for (String name : ALL_FILES) {
                Files.move(directory.resolve(name + ".tmp"), directory.resolve(name),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new DataFileException("Could not save the data to " + directory + ": " + e.getMessage(), e);
        }
    }

    private void write(String name, List<String> lines) throws IOException {
        Files.write(directory.resolve(name + ".tmp"), lines, StandardCharsets.UTF_8);
    }

    // Builds one record line: values are converted to text, escaped and joined with "|".
    private static String line(Object... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(SEPARATOR);
            }
            sb.append(escape(String.valueOf(values[i])));
        }
        return sb.toString();
    }

    // =====================================================================
    // Loading
    // =====================================================================

    public FootballLeague load() throws DataFileException {
        try {
            return loadLeague();
        } catch (DataFileException e) {
            throw e;
        } catch (KPFLException e) {
            // A business rule rejected the saved data (for example a duplicate ID).
            throw new DataFileException("The saved data breaks a league rule: " + e.getMessage(), e);
        }
    }

    private FootballLeague loadLeague() throws KPFLException {
        // ---- league and commissioner ----
        List<String[]> leagueRows = read(LEAGUE_FILE, 5);
        if (leagueRows.size() != 1) {
            throw new DataFileException(LEAGUE_FILE + " must contain exactly one line.");
        }
        String[] lr = leagueRows.get(0);
        FootballLeague league = new FootballLeague(lr[0]);
        if (!lr[1].isEmpty()) {
            league.setCommissioner(new LeagueCommissioner(lr[1], lr[2], toInt(LEAGUE_FILE, lr[3]), lr[4]));
        }

        // ---- stadiums and officials ----
        for (String[] r : read(STADIUMS_FILE, 6)) {
            league.addStadium(new Stadium(r[0], r[1], r[2], toInt(STADIUMS_FILE, r[3]), r[4], toMoney(r[5])));
        }
        List<MatchOfficial> officials = new ArrayList<>();
        List<Boolean> availability = new ArrayList<>();
        for (String[] r : read(OFFICIALS_FILE, 7)) {
            MatchOfficial official = new MatchOfficial(r[0], r[1], toInt(OFFICIALS_FILE, r[2]), r[3], r[4], r[5]);
            league.addOfficial(official);
            officials.add(official);
            availability.add(toBoolean(OFFICIALS_FILE, r[6]));
        }

        // ---- teams, players and coaches ----
        List<FootballTeam> teams = new ArrayList<>();
        List<TeamStatus> statuses = new ArrayList<>();
        List<Integer> points = new ArrayList<>();
        for (String[] r : read(TEAMS_FILE, 6)) {
            FootballTeam team = new FootballTeam(r[0], r[1], r[2], toInt(TEAMS_FILE, r[3]));
            league.registerTeam(team);
            teams.add(team);
            statuses.add(toStatus(r[4]));
            points.add(toInt(TEAMS_FILE, r[5]));
        }
        for (String[] r : read(PLAYERS_FILE, 9)) {
            FootballTeam team = league.findTeam(r[0]);
            Player player = new Player(r[1], r[2], toInt(PLAYERS_FILE, r[3]), r[4], r[5],
                    toInt(PLAYERS_FILE, r[6]), r[7]);
            player.recordGoals(toInt(PLAYERS_FILE, r[8]));
            team.addPlayer(player);
        }
        for (String[] r : read(COACHES_FILE, 7)) {
            FootballTeam team = league.findTeam(r[0]);
            team.addCoach(new Coach(r[1], r[2], toInt(COACHES_FILE, r[3]), r[4], r[5], r[6]));
        }

        // ---- fixtures ----
        // A fixture saved earlier may involve a team that has since been suspended, and a completed
        // fixture cannot take new officials. So teams are treated as REGISTERED while the fixtures are
        // rebuilt, then their real status, points and the officials' availability are put back.
        for (FootballTeam team : teams) {
            team.setStatus(TeamStatus.REGISTERED);
        }
        for (String[] r : read(FIXTURES_FILE, 10)) {
            league.scheduleFixture(r[0], r[1], r[2], r[3], toDate(r[4]), toTime(r[5]));
            if (!r[9].isEmpty()) {
                for (String officialId : r[9].split(",")) {
                    league.assignOfficial(r[0], officialId);
                }
            }
            FixtureStatus status = toFixtureStatus(r[6]);
            if (status == FixtureStatus.COMPLETED) {
                league.recordResult(r[0], toInt(FIXTURES_FILE, r[7]), toInt(FIXTURES_FILE, r[8]));
            }
        }
        for (int i = 0; i < teams.size(); i++) {
            teams.get(i).setStatus(statuses.get(i));
            teams.get(i).setPoints(points.get(i));
        }
        for (int i = 0; i < officials.size(); i++) {
            officials.get(i).setAvailable(availability.get(i));
        }
        return league;
    }

    // Reads a file and splits every non-blank line into exactly "count" fields.
    private List<String[]> read(String name, int count) throws DataFileException {
        Path path = directory.resolve(name);
        if (!Files.exists(path)) {
            throw new DataFileException("The data file " + name + " is missing from " + directory + ".");
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DataFileException("Could not read " + name + ": " + e.getMessage(), e);
        }
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String text = lines.get(i);
            if (text.trim().isEmpty()) {
                continue;
            }
            String[] parts = text.split("\\|", -1);
            if (parts.length != count) {
                throw new DataFileException(name + " line " + (i + 1) + " has " + parts.length
                        + " fields but " + count + " were expected.");
            }
            for (int j = 0; j < parts.length; j++) {
                parts[j] = unescape(parts[j]);
            }
            rows.add(parts);
        }
        return rows;
    }

    // ---- converting text back to values; a bad value becomes a DataFileException ----

    private static int toInt(String file, String text) throws DataFileException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new DataFileException(file + ": \"" + text + "\" is not a whole number.", e);
        }
    }

    private static BigDecimal toMoney(String text) throws DataFileException {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new DataFileException(STADIUMS_FILE + ": \"" + text + "\" is not a valid price.", e);
        }
    }

    private static boolean toBoolean(String file, String text) throws DataFileException {
        if (text.equals("true") || text.equals("false")) {
            return Boolean.parseBoolean(text);
        }
        throw new DataFileException(file + ": \"" + text + "\" should be true or false.");
    }

    private static TeamStatus toStatus(String text) throws DataFileException {
        try {
            return TeamStatus.valueOf(text.trim());
        } catch (IllegalArgumentException e) {
            throw new DataFileException(TEAMS_FILE + ": \"" + text + "\" is not a team status.", e);
        }
    }

    private static FixtureStatus toFixtureStatus(String text) throws DataFileException {
        try {
            return FixtureStatus.valueOf(text.trim());
        } catch (IllegalArgumentException e) {
            throw new DataFileException(FIXTURES_FILE + ": \"" + text + "\" is not a fixture status.", e);
        }
    }

    private static LocalDate toDate(String text) throws DataFileException {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new DataFileException(FIXTURES_FILE + ": \"" + text + "\" is not a valid date.", e);
        }
    }

    private static LocalTime toTime(String text) throws DataFileException {
        try {
            return LocalTime.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new DataFileException(FIXTURES_FILE + ": \"" + text + "\" is not a valid time.", e);
        }
    }

    // ---- escaping, so text can never contain the field separator or a line break ----

    static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '|' -> sb.append("\\p");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                default -> sb.append(ch);
            }
        }
        return sb.toString();
    }

    static String unescape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);
                switch (next) {
                    case 'p' -> sb.append('|');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    default -> sb.append(next);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
