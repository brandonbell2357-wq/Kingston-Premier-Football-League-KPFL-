import exception.*;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/*
  Tests for ticket sales: BR4 (no more tickets than the stadium's capacity), BR5
  (quantity above zero), ticket prices by category, revenue, and the composition
  between Fixture and Ticket. Same style as ModelTest: run with "java -cp out TicketTest".
  Labels say Normal, Boundary or Invalid, matching the BR4 rows of the Week 1 test plan
  (C = the stadium's seating capacity).
 */
public class TicketTest {

    private static int passed = 0;
    private static int failed = 0;

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    // Passes only if the action throws the expected exception type (or a subclass).
    private static void expect(String name, Class<? extends Exception> type, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("FAIL: " + name + " (no exception thrown)");
        } catch (Exception e) {
            if (type.isInstance(e)) {
                passed++;
            } else {
                failed++;
                System.out.println("FAIL: " + name + " (threw " + e.getClass().getSimpleName() + ")");
            }
        }
    }

    //Passes only if the action runs WITHOUT throwing any exception. 
    private static void expectNoError(String name, Action action) {
        try {
            action.run();
            passed++;
        } catch (Exception e) {
            failed++;
            System.out.println("FAIL: " + name + " (threw " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }

    // ---------- Test data helpers ----------

    //A scheduled fixture between two registered teams at a stadium with the given capacity. 
    private static Fixture fixture(int capacity, String price) throws Exception {
        LeagueCommissioner commissioner = new LeagueCommissioner("L1", "Commissioner", 50, "Male");
        FootballTeam home = new FootballTeam("T1", "Harbour View FC", "Kingston", 25);
        FootballTeam away = new FootballTeam("T2", "Waterhouse FC", "Kingston", 25);
        commissioner.approveTeam(home);
        commissioner.approveTeam(away);
        Stadium stadium = new Stadium("S1", "National Stadium", "Kingston", capacity, "Good", new BigDecimal(price));
        return new Fixture("FX01", home, away, stadium, LocalDate.of(2026, 12, 5), LocalTime.of(15, 0));
    }

    private static Spectator spectator(String id) throws Exception {
        return new Spectator(id, "Fan " + id, 30, "Female");
    }

    public static void main(String[] args) throws Exception {
        br4CapacityTests();
        br5QuantityTests();
        priceAndRevenueTests();
        otherRuleTests();
        compositionTests();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- BR4: ticket sales cannot exceed the stadium's seating capacity ----------
    private static void br4CapacityTests() throws Exception {
        // Normal: 0 sold; sell 10 -> 10 sold.
        Fixture normal = fixture(100, "1500.00");
        expectNoError("BR4 normal: sell 10 when 0 are sold", () -> normal.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 10));
        check("BR4 normal: 10 tickets now sold", normal.getOccupancy() == 10);
        check("BR4 normal: 90 seats remaining", normal.getSeatsRemaining() == 90);

        // Boundary: sold + quantity equals C -> accepted, 0 seats left.
        Fixture full = fixture(10, "1500.00");
        full.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 6);
        expectNoError("BR4 boundary: sold + quantity equals C", () -> full.sellTickets(spectator("SP2"), TicketCategory.GENERAL, 4));
        check("BR4 boundary: 0 seats left", full.getSeatsRemaining() == 0 && full.getOccupancy() == 10);

        // Boundary: sold equals C; sell 1 -> refused.
        expect("BR4 boundary: sold equals C, sell 1 more", StadiumCapacityExceededException.class,
                () -> full.sellTickets(spectator("SP3"), TicketCategory.GENERAL, 1));

        // Invalid: sold + quantity equals C + 1 -> refused, sold count unchanged.
        Fixture over = fixture(10, "1500.00");
        over.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 6);
        expect("BR4 invalid: sold + quantity equals C + 1", StadiumCapacityExceededException.class,
                () -> over.sellTickets(spectator("SP2"), TicketCategory.GENERAL, 5));
        check("BR4 invalid: sold count unchanged after refusal", over.getOccupancy() == 6);
    }

    // ---------- BR5: quantities cannot be zero or negative ----------
    private static void br5QuantityTests() throws Exception {
        Fixture f = fixture(100, "1500.00");
        expect("BR5 invalid: ticket quantity 0", InvalidDataException.class,
                () -> f.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 0));
        expect("BR5 invalid: ticket quantity -1", InvalidDataException.class,
                () -> f.sellTickets(spectator("SP1"), TicketCategory.GENERAL, -1));
        check("BR5: nothing sold after refusals", f.getOccupancy() == 0);
        expectNoError("BR5 boundary: ticket quantity 1", () -> f.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 1));
        check("BR5 boundary: 1 ticket sold", f.getOccupancy() == 1);
    }

    // ---------- Prices by category and revenue (exact money with BigDecimal) ----------
    private static void priceAndRevenueTests() throws Exception {
        Fixture f = fixture(100, "1500.00");
        List<Ticket> general = f.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 2);
        List<Ticket> grandstand = f.sellTickets(spectator("SP2"), TicketCategory.GRANDSTAND, 1);
        List<Ticket> vip = f.sellTickets(spectator("SP3"), TicketCategory.VIP, 1);

        check("GENERAL ticket costs the stadium price (1500.00)", general.get(0).getPrice().compareTo(new BigDecimal("1500.00")) == 0);
        check("GRANDSTAND ticket costs 1.5 x (2250.00)", grandstand.get(0).getPrice().compareTo(new BigDecimal("2250.00")) == 0);
        check("VIP ticket costs 2.5 x (3750.00)", vip.get(0).getPrice().compareTo(new BigDecimal("3750.00")) == 0);
        check("revenue is exact: 2 x 1500 + 2250 + 3750 = 9000.00",
                f.getTicketRevenue().compareTo(new BigDecimal("9000.00")) == 0);
        check("prices have 2 decimal places", vip.get(0).getPrice().scale() == 2);
        check("count of GENERAL tickets", f.countTickets(TicketCategory.GENERAL) == 2);
        check("count of VIP tickets", f.countTickets(TicketCategory.VIP) == 1);

        Fixture cents = fixture(100, "999.99");
        Ticket odd = cents.sellTickets(spectator("SP1"), TicketCategory.GRANDSTAND, 1).get(0);
        check("price rounds to the cent: 999.99 x 1.5 = 1499.99 (half up from 1499.985)",
                odd.getPrice().compareTo(new BigDecimal("1499.99")) == 0);

        Fixture free = fixture(100, "0.00");
        check("free stadium: revenue stays 0.00",
                free.sellTickets(spectator("SP1"), TicketCategory.VIP, 3).size() == 3
                        && free.getTicketRevenue().compareTo(BigDecimal.ZERO) == 0);
    }

    // ---------- Other rules ----------
    private static void otherRuleTests() throws Exception {
        Fixture f = fixture(100, "1500.00");
        expect("no spectator", InvalidDataException.class, () -> f.sellTickets(null, TicketCategory.GENERAL, 1));
        expect("no category", InvalidDataException.class, () -> f.sellTickets(spectator("SP1"), null, 1));

        Fixture played = fixture(100, "1500.00");
        played.complete(new MatchResult(2, 1));
        expect("no tickets for a completed fixture", FixtureStateException.class,
                () -> played.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 1));

        // Now testable: a fixture cannot move to a stadium smaller than the tickets already sold.
        Fixture moving = fixture(100, "1500.00");
        moving.sellTickets(spectator("SP1"), TicketCategory.GENERAL, 50);
        Stadium small = new Stadium("S2", "Small Ground", "Kingston", 40, "Fair", new BigDecimal("500.00"));
        expect("cannot move to a stadium smaller than tickets sold", InvalidDataException.class,
                () -> moving.setStadium(small));
        check("stadium unchanged after refusal", moving.getStadium().getStadiumId().equals("S1"));
        Stadium exact = new Stadium("S3", "Exact Ground", "Kingston", 50, "Fair", new BigDecimal("500.00"));
        expectNoError("boundary: can move to a stadium exactly the size of tickets sold", () -> moving.setStadium(exact));
    }

    // ---------- Fixture & Ticket composition ----------
    private static void compositionTests() throws Exception {
        Fixture f = fixture(100, "1500.00");
        Spectator fan = spectator("SP1");
        List<Ticket> sold = f.sellTickets(fan, TicketCategory.GENERAL, 3);
        check("ticket IDs are numbered per fixture", sold.get(0).getTicketId().equals("FX01-T0001")
                && sold.get(2).getTicketId().equals("FX01-T0003"));
        Ticket next = f.sellTickets(fan, TicketCategory.GENERAL, 1).get(0);
        check("numbering continues after earlier sales", next.getTicketId().equals("FX01-T0004"));
        check("each ticket knows its fixture and spectator", sold.get(0).getFixture() == f && sold.get(0).getSpectator() == fan);

        expect("the fixture's ticket list cannot be changed from outside", UnsupportedOperationException.class,
                () -> f.getTickets().clear());

        Fixture copy = new Fixture(f);
        check("copy has the same number of tickets", copy.getOccupancy() == 4);
        check("copy owns its own ticket objects (composition)",
                copy.getTickets().get(0) != f.getTickets().get(0) && copy.getTickets().get(0).getFixture() == copy);
        copy.sellTickets(fan, TicketCategory.GENERAL, 1);
        check("selling on the copy does not change the original", f.getOccupancy() == 4 && copy.getOccupancy() == 5);

        expect("ticket with negative price is invalid", InvalidDataException.class,
                () -> new Ticket("X1", f, fan, TicketCategory.GENERAL, new BigDecimal("-1")));
    }
}
