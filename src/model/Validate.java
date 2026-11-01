package model;

import exception.InvalidValueException;
import java.math.BigDecimal;

/** Small checks shared by the model classes. Every failed check throws InvalidValueException. */
final class Validate {
    private Validate() {
    }

    static String text(String value, String field) throws InvalidValueException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidValueException(field + " cannot be blank.");
        }
        return value.trim();
    }

    static <T> T notNull(T value, String field) throws InvalidValueException {
        if (value == null) {
            throw new InvalidValueException(field + " is required.");
        }
        return value;
    }

    static int nonNegative(int value, String field) throws InvalidValueException {
        if (value < 0) {
            throw new InvalidValueException(field + " cannot be negative.");
        }
        return value;
    }

    static int positive(int value, String field) throws InvalidValueException {
        if (value <= 0) {
            throw new InvalidValueException(field + " must be greater than zero.");
        }
        return value;
    }

    static BigDecimal nonNegative(BigDecimal value, String field) throws InvalidValueException {
        if (value == null || value.signum() < 0) {
            throw new InvalidValueException(field + " cannot be negative or missing.");
        }
        return value;
    }
}
