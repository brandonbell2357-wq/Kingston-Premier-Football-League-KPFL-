package model;

import exception.InvalidValueException;

/** A person who attends matches and buys tickets and shop products. */
public class Spectator extends Person {
    public Spectator(String id, String name, int age, String gender) throws InvalidValueException {
        super(id, name, age, gender);
    }

    @Override
    public String getRole() {
        return "Spectator";
    }
}
