package model;

import exception.InvalidValueException;

/** The score of a completed fixture. Scores cannot be negative (BR5). */
public class MatchResult {
    private final int homeGoals;
    private final int awayGoals;

    public MatchResult(int homeGoals, int awayGoals) throws InvalidValueException {
        this.homeGoals = Validate.nonNegative(homeGoals, "Home goals");
        this.awayGoals = Validate.nonNegative(awayGoals, "Away goals");
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    public boolean isDraw() {
        return homeGoals == awayGoals;
    }

    public boolean isHomeWin() {
        return homeGoals > awayGoals;
    }

    public boolean isAwayWin() {
        return awayGoals > homeGoals;
    }

    /** League points for the home team: win 3, draw 1, loss 0. */
    public int getHomePoints() {
        return isHomeWin() ? 3 : isDraw() ? 1 : 0;
    }

    /** League points for the away team: win 3, draw 1, loss 0. */
    public int getAwayPoints() {
        return isAwayWin() ? 3 : isDraw() ? 1 : 0;
    }

    @Override
    public String toString() {
        return homeGoals + " - " + awayGoals;
    }
}
