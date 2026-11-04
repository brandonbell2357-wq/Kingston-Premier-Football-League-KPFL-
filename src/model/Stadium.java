package model;

import java.math.BigDecimal;

/** A stadium where fixtures are played. */
public class Stadium {
    private String stadiumId;
    private String name;
    private String location;
    private int seatingCapacity;
    private String fieldCondition;
    private BigDecimal ticketPrice;

    /** Default constructor: empty text, capacity 0 and a ticket price of zero. */
    public Stadium() {
        this.stadiumId = "";
        this.name = "";
        this.location = "";
        this.seatingCapacity = 0;
        this.fieldCondition = "";
        this.ticketPrice = BigDecimal.ZERO;
    }

    public Stadium(String stadiumId, String name, String location, int seatingCapacity,
                   String fieldCondition, BigDecimal ticketPrice) throws InvalidDataException {
        setStadiumId(stadiumId);
        setName(name);
        setLocation(location);
        setSeatingCapacity(seatingCapacity);
        setFieldCondition(fieldCondition);
        setTicketPrice(ticketPrice);
    }

    /** Copy constructor: copies every field of another stadium. */
    public Stadium(Stadium other) {
        this.stadiumId = other.stadiumId;
        this.name = other.name;
        this.location = other.location;
        this.seatingCapacity = other.seatingCapacity;
        this.fieldCondition = other.fieldCondition;
        this.ticketPrice = other.ticketPrice;
    }

    /**
     * Checks that a total number of tickets fits in the stadium.
     *
     * @param totalTickets the number of tickets that would be sold for a fixture after the sale
     * @return true if the total is within the seating capacity
     */
    public boolean checkCapacity(int totalTickets) {
        return totalTickets <= seatingCapacity;
    }

    public String getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(String stadiumId) throws InvalidDataException {
        if (stadiumId == null || stadiumId.trim().isEmpty()) {
            throw new InvalidDataException("Stadium ID cannot be blank.");
        }
        this.stadiumId = stadiumId.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Stadium name cannot be blank.");
        }
        this.name = name.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) throws InvalidDataException {
        if (location == null || location.trim().isEmpty()) {
            throw new InvalidDataException("Location cannot be blank.");
        }
        this.location = location.trim();
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) throws InvalidDataException {
        if (seatingCapacity <= 0) {
            throw new InvalidDataException("Seating capacity must be greater than 0 (value given: " + seatingCapacity + ").");
        }
        this.seatingCapacity = seatingCapacity;
    }

    public String getFieldCondition() {
        return fieldCondition;
    }

    public void setFieldCondition(String fieldCondition) throws InvalidDataException {
        if (fieldCondition == null || fieldCondition.trim().isEmpty()) {
            throw new InvalidDataException("Field condition cannot be blank.");
        }
        this.fieldCondition = fieldCondition.trim();
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) throws InvalidDataException {
        if (ticketPrice == null || ticketPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Ticket price cannot be null or negative.");
        }
        this.ticketPrice = ticketPrice;
    }

    @Override
    public String toString() {
        return stadiumId + " " + name + " (" + location + ", capacity " + seatingCapacity + ", "
                + fieldCondition + ")";
    }
}
