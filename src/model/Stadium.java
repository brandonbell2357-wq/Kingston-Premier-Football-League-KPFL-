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
        this("", "", "", 0, "", BigDecimal.ZERO);
    }

    public Stadium(String stadiumId, String name, String location, int seatingCapacity,
                   String fieldCondition, BigDecimal ticketPrice) {
        this.stadiumId = stadiumId;
        this.name = name;
        this.location = location;
        this.seatingCapacity = seatingCapacity;
        this.fieldCondition = fieldCondition;
        this.ticketPrice = ticketPrice;
    }

    /** Copy constructor: copies every field of another stadium. */
    public Stadium(Stadium other) {
        this(other.stadiumId, other.name, other.location, other.seatingCapacity,
                other.fieldCondition, other.ticketPrice);
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

    public void setStadiumId(String stadiumId) {
        this.stadiumId = stadiumId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) {
        this.seatingCapacity = seatingCapacity;
    }

    public String getFieldCondition() {
        return fieldCondition;
    }

    public void setFieldCondition(String fieldCondition) {
        this.fieldCondition = fieldCondition;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    @Override
    public String toString() {
        return stadiumId + " " + name + " (" + location + ", capacity " + seatingCapacity + ", " + fieldCondition + ")";
    }
}
