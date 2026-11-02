package model;

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

    public MatchOfficial(String id, String name, int age, String gender, String officialRole, String rank) {
        super(id, name, age, gender);
        this.officialRole = officialRole;
        this.rank = rank;
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

    public void setOfficialRole(String officialRole) {
        this.officialRole = officialRole;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
