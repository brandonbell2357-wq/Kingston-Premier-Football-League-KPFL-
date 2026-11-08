package model;

import exception.InvalidDataException;

/**
 * Abstract superclass of everyone the league deals with. It holds the personal data
 * shared by players, coaches, officials, the commissioner and spectators.
 */
public abstract class Person {
    /** Highest age accepted for any person. */
    public static final int MAX_AGE = 120;

    protected final String id;
    protected String name;
    protected int age;
    protected String gender;

    /** The constructor uses the setters, so the validation rules live in one place. */
    protected Person(String id, String name, int age, String gender) throws InvalidDataException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidDataException("ID cannot be blank.");
        }
        this.id = id.trim();
        setName(name);
        setAge(age);
        setGender(gender);
    }

    public Person() {
        this.id = "000";
        this.name = "N/A";
        this.age = 0;
        this.gender = "N/A";
    }

    public Person(Person p) {
        this.id = p.id;
        this.name = p.name;
        this.age = p.age;
        this.gender = p.gender;
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

    public void setName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Name cannot be blank.");
        }
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) throws InvalidDataException {
        if (age < 0 || age > MAX_AGE) {
            throw new InvalidDataException("Age must be between 0 and " + MAX_AGE + " (age given: " + age + ").");
        }
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) throws InvalidDataException {
        if (gender == null || gender.trim().isEmpty()) {
            throw new InvalidDataException("Gender cannot be blank.");
        }
        this.gender = gender.trim();
    }

    @Override
    public String toString() {
        return describe();
    }
}
