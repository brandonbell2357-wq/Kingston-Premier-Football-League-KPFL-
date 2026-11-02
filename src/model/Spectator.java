package model;

/** A person who attends matches and buys tickets and shop products. */
public class Spectator extends Person {
    /** Default constructor: empty text and age 0. Fill the fields with the setters. */
    public Spectator() {
        super();
    }

    public Spectator(String id, String name, int age, String gender) {
        super(id, name, age, gender);
    }

    /** Copy constructor: copies every field of another spectator. */
    public Spectator(Spectator other) {
        super(other);
    }

    @Override
    public String getRole() {
        return "Spectator";
    }
}
