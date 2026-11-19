package service;

import exception.InvalidDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import model.Fixture;
import model.FixtureStatus;
import model.FootballTeam;
import model.StandingsEntry;

/*
  Builds the league standings. Standings are never stored: they are rebuilt from the
  completed fixtures every time, so they cannot drift out of step with the results
  (design document section 5). FootballLeague should call this class rather than
  keep its own table.
 */
public final class StandingsService {

    private StandingsService() {
        // utility class: nothing to create
    }

    /**
     * Rebuilds the table, best team first.
     * Every team in {@code teams} gets a row, even one with no completed matches (all zeros).
     * Fixtures that are still SCHEDULED are ignored. A team that appears in a completed
     * fixture but not in {@code teams} still gets a row, so no result is silently lost.
     * Calling this twice with the same data gives the same table.
     *
     * @param teams    the teams that should appear in the table
     * @param fixtures every fixture in the league (scheduled and completed)
     */
    public static List<StandingsEntry> buildStandings(Collection<FootballTeam> teams,
                                                      Collection<Fixture> fixtures)
            throws InvalidDataException {
        Map<String, StandingsEntry> rows = new LinkedHashMap<>();
        if (teams != null) {
            for (FootballTeam team : teams) {
                rowFor(rows, team);
            }
        }
        if (fixtures != null) {
            for (Fixture fixture : fixtures) {
                if (fixture == null || fixture.getStatus() != FixtureStatus.COMPLETED
                        || fixture.getResult() == null) {
                    continue;
                }
                int homeGoals = fixture.getResult().getHomeGoals();
                int awayGoals = fixture.getResult().getAwayGoals();
                rowFor(rows, fixture.getHomeTeam()).recordMatch(homeGoals, awayGoals);
                rowFor(rows, fixture.getAwayTeam()).recordMatch(awayGoals, homeGoals);
            }
        }
        List<StandingsEntry> table = new ArrayList<>(rows.values());
        Collections.sort(table);
        return Collections.unmodifiableList(table);
    }

    /**
     * Copies each row's points onto its team, so FootballTeam.getPoints() matches the table.
     * Run this after buildStandings. Each team's points are reset first and then set to the table value, so running it twice is safe.
     */
    public static void applyPointsToTeams(List<StandingsEntry> table) throws InvalidDataException {
        if (table == null) {
            throw new InvalidDataException("Standings cannot be null.");
        }
        for (StandingsEntry row : table) {
            row.getTeam().resetPoints();
            row.getTeam().addPoints(row.getPoints());
        }
    }

    /** The table as printable text, for the standings report. */
    public static String format(List<StandingsEntry> table) {
        StringBuilder text = new StringBuilder();
        text.append(String.format("%-4s %-24s %3s %3s %3s %3s %4s %4s %4s %4s%n",
                "Pos", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts"));
        int position = 1;
        for (StandingsEntry row : table) {
            text.append(String.format("%-4d %-24s %3d %3d %3d %3d %4d %4d %+4d %4d%n",
                    position++, row.getTeam().getName(), row.getPlayed(), row.getWon(),
                    row.getDrawn(), row.getLost(), row.getGoalsFor(), row.getGoalsAgainst(),
                    row.getGoalDifference(), row.getPoints()));
        }
        return text.toString();
    }

    // Finds the row for a team, creating it the first time the team is seen.
    // Teams are matched by registration number, which is unique to each team.
    private static StandingsEntry rowFor(Map<String, StandingsEntry> rows, FootballTeam team)
            throws InvalidDataException {
        if (team == null) {
            throw new InvalidDataException("Standings cannot include a null team.");
        }
        StandingsEntry row = rows.get(team.getRegistrationNumber());
        if (row == null) {
            row = new StandingsEntry(team);
            rows.put(team.getRegistrationNumber(), row);
        }
        return row;
    }
}
