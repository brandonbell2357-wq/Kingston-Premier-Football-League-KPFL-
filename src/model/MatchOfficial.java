package model;

import exception.InvalidValueException;

/** A referee or other official who can be assigned to fixtures. */
public class MatchOfficial extends Person {
    private String officialRole;
    private String rank;
    private boolean available;

    public MatchOfficial(String id, String name, int age, String gender,
                         String officialRole, String rank) throws InvalidValueException {
        super(id, name, age, gender);
        this.officialRole = Validate.text(officialRole, "Official role");
        this.rank = Validate.text(rank, "Rank");
        this.available = true;
    }

    @Override
    public String getRole() {
        return "Match Official";
    }

    @Override
    public String describe() {
        return super.describe() + ", " + officialRole + ", rank " + rank
                + (available ? ", available" : ", unavailable");
    }

    public String getOfficialRole() {
        return officialRole;
    }

    public void setOfficialRole(String officialRole) throws InvalidValueException {
        this.officialRole = Validate.text(officialRole, "Official role");
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) throws InvalidValueException {
        this.rank = Validate.text(rank, "Rank");
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
