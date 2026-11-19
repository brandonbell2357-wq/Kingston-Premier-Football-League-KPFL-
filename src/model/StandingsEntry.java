package model;

import exception.InvalidDataException;

/*
  One row of the league table: a team and its record over all completed fixtures.
  Rows are not stored in files. StandingsService rebuilds them from the completed
  fixtures after every result and every load (design document section 5).

  Points system (project brief, Week 3): win = 3, draw = 1, loss = 0.
  Rows sort best first: points, then goal difference, then goals scored, then team name.
  These tie-breakers are an assumption: the brief does not state any.
 */
public class StandingsEntry implements Comparable<StandingsEntry> {
    public static final int WIN_POINTS = 3;
    public static final int DRAW_POINTS = 1;
    public static final int LOSS_POINTS = 0;

    private final FootballTeam team;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;

    /** A team with no matches played yet: every figure is zero. */
    public StandingsEntry(FootballTeam team) throws InvalidDataException {
        if (team == null) {
            throw new InvalidDataException("A standings row needs a team.");
        }
        this.team = team;
    }

    /**
     * Adds one completed match from this team's point of view.
     * Goals cannot be negative (BR5), and a refused call changes nothing.
     *
     * @param scored   goals this team scored
     * @param conceded goals this team let in
     */
    public void recordMatch(int scored, int conceded) throws InvalidDataException {
        if (scored < 0 || conceded < 0) {
            throw new InvalidDataException("Goals cannot be negative (scored " + scored
                    + ", conceded " + conceded + ").");
        }
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        if (scored > conceded) {
            won++;
        } else if (scored == conceded) {
            drawn++;
        } else {
            lost++;
        }
    }

    public FootballTeam getTeam() {
        return team;
    }

    public int getPlayed() {
        return played;
    }

    public int getWon() {
        return won;
    }

    public int getDrawn() {
        return drawn;
    }

    public int getLost() {
        return lost;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public int getGoalDifference() {
        return goalsFor - goalsAgainst;
    }

    /** Points are always calculated from the record, so they can never disagree with it. */
    public int getPoints() {
        return won * WIN_POINTS + drawn * DRAW_POINTS + lost * LOSS_POINTS;
    }

    /** Negative when this row ranks higher than the other, so a sorted list is best first. */
    @Override
    public int compareTo(StandingsEntry other) {
        int byPoints = Integer.compare(other.getPoints(), getPoints());
        if (byPoints != 0) {
            return byPoints;
        }
        int byDifference = Integer.compare(other.getGoalDifference(), getGoalDifference());
        if (byDifference != 0) {
            return byDifference;
        }
        int byGoals = Integer.compare(other.goalsFor, goalsFor);
        if (byGoals != 0) {
            return byGoals;
        }
        return team.getName().compareToIgnoreCase(other.team.getName());
    }

    @Override
    public String toString() {
        return team.getName() + " P" + played + " W" + won + " D" + drawn + " L" + lost
                + " GF" + goalsFor + " GA" + goalsAgainst + " GD" + getGoalDifference()
                + " Pts" + getPoints();
    }
}
