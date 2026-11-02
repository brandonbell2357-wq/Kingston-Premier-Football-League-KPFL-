package model;

/** A member of a team's coaching staff. */
public class Coach extends Person {
    private String qualification;
    private String coachingRole;

    /** Default constructor: empty text. Fill the fields with the setters. */
    public Coach() {
        super();
        this.qualification = "";
        this.coachingRole = "";
    }

    public Coach(String id, String name, int age, String gender, String qualification, String coachingRole) {
        super(id, name, age, gender);
        this.qualification = qualification;
        this.coachingRole = coachingRole;
    }

    /** Copy constructor: copies every field of another coach. */
    public Coach(Coach other) {
        super(other);
        this.qualification = other.qualification;
        this.coachingRole = other.coachingRole;
    }

    @Override
    public String getRole() {
        return "Coach";
    }

    @Override
    public String describe() {
        return super.describe() + ", " + coachingRole + ", " + qualification;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getCoachingRole() {
        return coachingRole;
    }

    public void setCoachingRole(String coachingRole) {
        this.coachingRole = coachingRole;
    }
}
