package model;

import exception.InvalidDataException;

/*
  The final score of a completed fixture.
  A MatchResult is created when a fixture is completed and has no life of its own
  (composition: Fixture owns its MatchResult, see design document section 4).
  It is immutable: once a result is recorded it cannot be edited, so standings
  rebuilt from results are always repeatable.
 */
public final class MatchResult {
    private final int homeGoals;
    private final int awayGoals;

    /**
     * Creates a result. Enforces BR5: match scores cannot be negative.
     *
     * @throws InvalidDataException if either score is negative
     */
    public MatchResult(int homeGoals, int awayGoals) throws InvalidDataException {
        if (homeGoals < 0) {
            throw new InvalidDataException("Home goals cannot be negative (value given: " + homeGoals + ").");
        }
        if (awayGoals < 0) {
            throw new InvalidDataException("Away goals cannot be negative (value given: " + awayGoals + ").");
        }
        this.homeGoals = homeGoals;
        this.awayGoals = awayGoals;
    }

    /** Copy constructor. */
    public MatchResult(MatchResult other) {
        this.homeGoals = other.homeGoals;
        this.awayGoals = other.awayGoals;
    }

    public int getHomeGoals() {
        return homeGoals;
    }

    public int getAwayGoals() {
        return awayGoals;
    }

    /** Total goals scored by both teams (a match statistic). */
    public int getTotalGoals() {
        return homeGoals + awayGoals;
    }

    public boolean isHomeWin() {
        return homeGoals > awayGoals;
    }

    public boolean isAwayWin() {
        return awayGoals > homeGoals;
    }

    public boolean isDraw() {
        return homeGoals == awayGoals;
    }

    /** Shown as "home - away", for example "2 - 1". */
    @Override
    public String toString() {
        return homeGoals + " - " + awayGoals;
    }
}
