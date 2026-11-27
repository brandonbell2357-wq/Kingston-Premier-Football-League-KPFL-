package model;

import exception.InvalidDataException;
import java.math.BigDecimal;

/*
  One match ticket. A ticket is for exactly one fixture and is created by that fixture
  when it is sold (composition: Fixture owns its tickets, see design document section 4).
  Each ticket is bought by one spectator (association).
  A ticket never changes after it is issued, so it has no setters. The price is stored
  at the moment of sale, so a later change to the stadium's price does not change old tickets.
 */
public final class Ticket {
    private final String ticketId;
    private final Fixture fixture;
    private final Spectator spectator;
    private final TicketCategory category;
    private final BigDecimal price;

    public Ticket(String ticketId, Fixture fixture, Spectator spectator, TicketCategory category,
                  BigDecimal price) throws InvalidDataException {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new InvalidDataException("Ticket ID cannot be blank.");
        }
        if (fixture == null) {
            throw new InvalidDataException("A ticket must be for a fixture.");
        }
        if (spectator == null) {
            throw new InvalidDataException("A ticket must be bought by a spectator.");
        }
        if (category == null) {
            throw new InvalidDataException("Ticket category cannot be empty.");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Ticket price cannot be empty or negative.");
        }
        this.ticketId = ticketId.trim();
        this.fixture = fixture;
        this.spectator = spectator;
        this.category = category;
        this.price = price;
    }

    public String getTicketId() {
        return ticketId;
    }

    public Fixture getFixture() {
        return fixture;
    }

    public Spectator getSpectator() {
        return spectator;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return ticketId + ": " + fixture.getHomeTeam().getName() + " vs " + fixture.getAwayTeam().getName()
                + " on " + fixture.getDate() + " at " + fixture.getTime() + ", " + category
                + ", $" + price + ", bought by " + spectator.getName() + " (" + spectator.getId() + ")";
    }
}

