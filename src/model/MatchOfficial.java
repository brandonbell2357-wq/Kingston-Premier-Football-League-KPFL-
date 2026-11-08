package model;

import exception.InvalidDataException;

/** A referee or other official who can be assigned to fixtures. */
public class MatchOfficial extends Person {
    private String officialRole;
    private String rank;
    private boolean available;

    /** Default constructor: empty text, and the official is available. */
    public MatchOfficial() {
        super();
        this.officialRole = "";
        this.rank = "";
        this.available = true;
    }

    public MatchOfficial(String id, String name, int age, String gender, String officialRole,
                         String rank) throws InvalidDataException {
        super(id, name, age, gender);
        setOfficialRole(officialRole);
        setRank(rank);
        this.available = true;
    }

    /** Copy constructor: copies every field of another official, including availability. */
    public MatchOfficial(MatchOfficial other) {
        super(other);
        this.officialRole = other.officialRole;
        this.rank = other.rank;
        this.available = other.available;
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

    public void setOfficialRole(String officialRole) throws InvalidDataException {
        if (officialRole == null || officialRole.trim().isEmpty()) {
            throw new InvalidDataException("Official role cannot be blank.");
        }
        this.officialRole = officialRole.trim();
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) throws InvalidDataException {
        if (rank == null || rank.trim().isEmpty()) {
            throw new InvalidDataException("Rank cannot be blank.");
        }
        this.rank = rank.trim();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
