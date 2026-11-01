package model;

import exception.InvalidValueException;
import java.math.BigDecimal;

/** A ticket for one fixture, bought by one spectator. */
public class Ticket {
    private final String ticketId;
    private final String category;
    private final BigDecimal price;
    private final Spectator buyer;

    public Ticket(String ticketId, String category, BigDecimal price, Spectator buyer) throws InvalidValueException {
        this.ticketId = Validate.text(ticketId, "Ticket id");
        this.category = Validate.text(category, "Ticket category");
        this.price = Validate.nonNegative(price, "Ticket price");
        this.buyer = Validate.notNull(buyer, "Spectator");
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Spectator getBuyer() {
        return buyer;
    }

    @Override
    public String toString() {
        return ticketId + " " + category + " " + price + " (" + buyer.getName() + ")";
    }
}
