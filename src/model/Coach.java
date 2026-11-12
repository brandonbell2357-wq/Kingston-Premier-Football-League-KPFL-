package model;
import exception.InvalidDataException;

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

    public Coach(String id, String name, int age, String gender, String qualification,
                 String coachingRole) throws InvalidDataException {
        super(id, name, age, gender);
        setQualification(qualification);
        setCoachingRole(coachingRole);
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

   public void setQualification(String qualification) throws InvalidDataException {
       if (qualification == null || qualification.trim().isEmpty()) {
            throw new InvalidDataException("Qualification cannot be blank.");
        }
        this.qualification = qualification.trim();
    }

    public String getCoachingRole() {
        return coachingRole;
    }

    public void setCoachingRole(String coachingRole) throws InvalidDataException {
        if (coachingRole == null || coachingRole.trim().isEmpty()) {
            throw new InvalidDataException("Coaching role cannot be blank.");
        }
        this.coachingRole = coachingRole.trim();
    }

    
}
