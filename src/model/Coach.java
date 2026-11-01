package model;

import exception.InvalidValueException;

/** A member of a team's coaching staff. */
public class Coach extends Person {
    private String qualification;
    private String coachingRole;

    public Coach(String id, String name, int age, String gender,
                 String qualification, String coachingRole) throws InvalidValueException {
        super(id, name, age, gender);
        this.qualification = Validate.text(qualification, "Qualification");
        this.coachingRole = Validate.text(coachingRole, "Coaching role");
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

    public void setQualification(String qualification) throws InvalidValueException {
        this.qualification = Validate.text(qualification, "Qualification");
    }

    public String getCoachingRole() {
        return coachingRole;
    }

    public void setCoachingRole(String coachingRole) throws InvalidValueException {
        this.coachingRole = Validate.text(coachingRole, "Coaching role");
    }
}
