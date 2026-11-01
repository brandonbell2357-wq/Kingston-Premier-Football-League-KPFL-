package model;

import exception.InvalidValueException;

/**
 * Abstract superclass of everyone the league deals with. It holds the personal data
 * shared by players, coaches, officials, the commissioner and spectators.
 */
public abstract class Person {
    private final String id;
    private String name;
    private int age;
    private String gender;

    protected Person(String id, String name, int age, String gender) throws InvalidValueException {
        this.id = Validate.text(id, "Person id");
        this.name = Validate.text(name, "Name");
        this.age = Validate.nonNegative(age, "Age");
        this.gender = Validate.text(gender, "Gender");
    }

    /** The role shown to users, such as "Player". Every subclass must supply it. */
    public abstract String getRole();

    /** One line describing this person. Subclasses can extend it. */
    public String describe() {
        return getRole() + " " + id + ": " + name + " (age " + age + ", " + gender + ")";
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidValueException {
        this.name = Validate.text(name, "Name");
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) throws InvalidValueException {
        this.age = Validate.nonNegative(age, "Age");
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) throws InvalidValueException {
        this.gender = Validate.text(gender, "Gender");
    }

    @Override
    public String toString() {
        return describe();
    }
}
