package model;

import exception.InvalidValueException;
import exception.StadiumCapacityExceededException;
import java.math.BigDecimal;

/** A stadium where fixtures are played. */
public class Stadium {
    private final String stadiumId;
    private String name;
    private String location;
    private int seatingCapacity;
    private String fieldCondition;
    private BigDecimal ticketPrice;

    public Stadium(String stadiumId, String name, String location, int seatingCapacity,
                   String fieldCondition, BigDecimal ticketPrice) throws InvalidValueException {
        this.stadiumId = Validate.text(stadiumId, "Stadium id");
        this.name = Validate.text(name, "Stadium name");
        this.location = Validate.text(location, "Location");
        this.seatingCapacity = Validate.positive(seatingCapacity, "Seating capacity");
        this.fieldCondition = Validate.text(fieldCondition, "Field condition");
        this.ticketPrice = Validate.nonNegative(ticketPrice, "Ticket price");
    }

    /**
     * Checks that a total number of tickets fits in the stadium (BR4).
     *
     * @param totalTickets the number of tickets that would be sold for a fixture after the sale
     */
    public void checkCapacity(int totalTickets) throws StadiumCapacityExceededException {
        if (totalTickets > seatingCapacity) {
            throw new StadiumCapacityExceededException("That sale would bring tickets to " + totalTickets
                    + " but " + name + " seats only " + seatingCapacity + ".");
        }
    }

    public String getStadiumId() {
        return stadiumId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidValueException {
        this.name = Validate.text(name, "Stadium name");
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) throws InvalidValueException {
        this.location = Validate.text(location, "Location");
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public void setSeatingCapacity(int seatingCapacity) throws InvalidValueException {
        this.seatingCapacity = Validate.positive(seatingCapacity, "Seating capacity");
    }

    public String getFieldCondition() {
        return fieldCondition;
    }

    public void setFieldCondition(String fieldCondition) throws InvalidValueException {
        this.fieldCondition = Validate.text(fieldCondition, "Field condition");
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) throws InvalidValueException {
        this.ticketPrice = Validate.nonNegative(ticketPrice, "Ticket price");
    }

    @Override
    public String toString() {
        return stadiumId + " " + name + " (" + location + ", capacity " + seatingCapacity + ", " + fieldCondition + ")";
    }
}
