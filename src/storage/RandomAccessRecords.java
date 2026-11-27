import exception.*;
import model.*;
import service.SampleData;
import storage.LeagueFileStorage;
import storage.RandomAccessRecords;

import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/*
  Tests for the Week 4 persistence: spectators (sequential), products and tickets (random access,
  fixed-length records), and the league's spectator and ticket methods.
  Every test uses a fresh temporary folder, so no real data is touched.
  Same style as ModelTest: run with "java -cp out RandomAccessTest".
 */
public class RandomAccessTest {

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

    /** Passes only if the action throws the expected exception type (or a subclass). */
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

    private static final LocalDate D1 = LocalDate.of(2026, 12, 5);
    private static final LocalTime T1 = LocalTime.of(15, 0);

    private static Path tempFolder() throws Exception {
        return Files.createTempDirectory("kpfl-ra-test");
    }

    private static boolean money(BigDecimal actual, String expected) {
        return actual != null && actual.compareTo(new BigDecimal(expected)) == 0;
    }

    // Sample league with one fixture (F1 at the National Stadium) and some tickets sold.
    private static FootballLeague leagueWithTickets() throws Exception {
        FootballLeague league = SampleData.createLeague();
        league.scheduleFixture("F1", "T01", "T02", "S01", D1, T1);
        league.sellTickets("F1", "SP01", TicketCategory.GENERAL, 2);
        league.sellTickets("F1", "SP02", TicketCategory.VIP, 1);
        return league;
    }

    public static void main(String[] args) throws Exception {
        leagueMethods();
        roundTrip();
        randomAccessRecords();
        savingRules();
        damagedAndInvalidData();
        olderSaves();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- Spectators and ticket sales through the league ----------
    private static void leagueMethods() throws Exception {
        FootballLeague league = SampleData.createLeague();
        check("sample data has 3 spectators and 7 products",
                league.getSpectators().size() == 3 && league.getShop().getProducts().size() == 7);
        expect("duplicate spectator ID", DuplicateEntryException.class,
                () -> league.addSpectator(new Spectator("sp01", "Someone", 30, "Male")));
        expect("null spectator", InvalidDataException.class, () -> league.addSpectator(null));
        expect("find a spectator who does not exist", NotFoundException.class, () -> league.findSpectator("SP99"));

        league.scheduleFixture("F1", "T01", "T02", "S02", D1, T1);
        List<Ticket> sold = league.sellTickets("F1", "SP01", TicketCategory.GRANDSTAND, 2);
        check("league sells tickets: 2 grandstand at 1000.00 x 1.5 = 1500.00 each",
                sold.size() == 2 && money(sold.get(0).getPrice(), "1500.00"));
        expect("tickets for a fixture that does not exist", NotFoundException.class,
                () -> league.sellTickets("F9", "SP01", TicketCategory.GENERAL, 1));
        expect("tickets for a spectator who does not exist", NotFoundException.class,
                () -> league.sellTickets("F1", "SP99", TicketCategory.GENERAL, 1));
        expect("BR4 through the league: more than the 7000 seats", StadiumCapacityExceededException.class,
                () -> league.sellTickets("F1", "SP02", TicketCategory.GENERAL, 6999));
        check("sold count unchanged after refusal", league.findFixture("F1").getOccupancy() == 2);
        check("low-stock products in the sample data (flag at 5, chicken box at 20)",
                league.getShop().getLowStockProducts().size() == 2);
    }

    // ---------- Save, then load: everything comes back ----------
    private static void roundTrip() throws Exception {
        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        FootballLeague original = leagueWithTickets();
        original.getShop().restock("F01", 5);          // stock 80 -> 85
        storage.save(original);
        check("products.dat, tickets.dat and spectators.txt are written",
                Files.exists(folder.resolve("products.dat")) && Files.exists(folder.resolve("tickets.dat"))
                        && Files.exists(folder.resolve("spectators.txt")));

        FootballLeague loaded = storage.load();
        check("spectators come back", loaded.getSpectators().size() == 3
                && loaded.findSpectator("SP03").getName().equals("Shanice Williams"));

        List<Product> products = loaded.getShop().getProducts();
        check("all 7 products come back, in order", products.size() == 7
                && products.get(0).getProductId().equals("M01") && products.get(6).getProductId().equals("F04"));
        Product jersey = loaded.getShop().findProduct("M01");
        check("merchandise keeps its type, size and team", jersey instanceof Merchandise m
                && m.getSize().equals("L") && m.getTeamName().equals("Harbour View FC"));
        check("prices are exact after the round trip (4500.00)", money(jersey.getPrice(), "4500.00"));
        check("stock changes are saved (patty 85)", loaded.getShop().findProduct("F01").getStockQuantity() == 85);
        check("a beverage stays a beverage", loaded.getShop().findProduct("F03") instanceof FoodItem f && f.isBeverage());
        check("food stays food", loaded.getShop().findProduct("F01") instanceof FoodItem f && !f.isBeverage());

        Fixture f1 = loaded.findFixture("F1");
        check("tickets come back on their fixture", f1.getOccupancy() == 3);
        check("ticket details come back", f1.getTickets().get(2).getCategory() == TicketCategory.VIP
                && f1.getTickets().get(2).getSpectator().getId().equals("SP02")
                && f1.getTickets().get(0).getTicketId().equals("F1-T0001"));
        check("ticket revenue is exact: 2 x 1500 + 3750 = 6750.00", money(f1.getTicketRevenue(), "6750.00"));
        check("loaded tickets point to the loaded spectator objects",
                f1.getTickets().get(0).getSpectator() == loaded.findSpectator("SP01"));

        List<Ticket> more = loaded.sellTickets("F1", "SP03", TicketCategory.GENERAL, 1);
        check("ticket numbering continues after loading", more.get(0).getTicketId().equals("F1-T0004"));

        // A ticket keeps the price it was sold at, even if the stadium price changes later.
        FootballLeague changed = leagueWithTickets();
        changed.findStadium("S01").setTicketPrice(new BigDecimal("9999.00"));
        Path f2 = tempFolder();
        new LeagueFileStorage(f2).save(changed);
        check("old tickets keep their sale price after reload",
                money(new LeagueFileStorage(f2).load().findFixture("F1").getTickets().get(0).getPrice(), "1500.00"));

        // Tickets for a fixture that has since been played still load.
        FootballLeague played = leagueWithTickets();
        played.recordResult("F1", 2, 1);
        Path f3 = tempFolder();
        new LeagueFileStorage(f3).save(played);
        FootballLeague playedLoaded = new LeagueFileStorage(f3).load();
        check("tickets of a completed fixture load", playedLoaded.findFixture("F1").getOccupancy() == 3
                && playedLoaded.findFixture("F1").isCompleted());
    }

    // ---------- The random access files themselves ----------
    private static void randomAccessRecords() throws Exception {
        Path folder = tempFolder();
        new LeagueFileStorage(folder).save(leagueWithTickets());
        Path products = folder.resolve("products.dat");
        Path tickets = folder.resolve("tickets.dat");

        check("record sizes are 175 and 128 bytes", RandomAccessRecords.PRODUCT_RECORD_SIZE == 175
                && RandomAccessRecords.TICKET_RECORD_SIZE == 128);
        check("products.dat holds exactly 7 fixed-length records", Files.size(products) == 7L * 175);
        check("tickets.dat holds exactly 3 fixed-length records", Files.size(tickets) == 3L * 128);

        Product fifth = RandomAccessRecords.readProduct(products, 4);
        check("direct read of record 4 with seek() gives the 5th product (Jerk Chicken Box)",
                fifth.getProductId().equals("F02") && fifth.getName().equals("Jerk Chicken Box"));
        check("direct read of record 0 gives the first product", RandomAccessRecords.readProduct(products, 0)
                .getProductId().equals("M01"));
        expect("record number past the end", DataFileException.class, () -> RandomAccessRecords.readProduct(products, 7));
        expect("negative record number", DataFileException.class, () -> RandomAccessRecords.readProduct(products, -1));

        // Price is stored as whole cents: the long at byte 82 of record 0 is 450000 (4500.00).
        try (RandomAccessFile raf = new RandomAccessFile(products.toFile(), "r")) {
            raf.seek(2 + 20 + 60);
            check("price is stored as whole cents (450000 = $4,500.00)", raf.readLong() == 450000L);
        }
    }

    // ---------- Saving rules ----------
    private static void savingRules() throws Exception {
        Path folder = tempFolder();
        LeagueFileStorage storage = new LeagueFileStorage(folder);
        storage.save(leagueWithTickets());
        long goodSize = Files.size(folder.resolve("products.dat"));

        FootballLeague tooLong = leagueWithTickets();
        tooLong.getShop().addProduct(new FoodItem("F99", "A product name that is far too long to fit",
                new BigDecimal("100.00"), 5, false));
        expect("a name longer than 30 characters is refused", DataFileException.class, () -> storage.save(tooLong));
        check("a refused save leaves the old files untouched", Files.size(folder.resolve("products.dat")) == goodSize
                && storage.load().getShop().getProducts().size() == 7);
        boolean leftovers;
        try (var files = Files.list(folder)) {
            leftovers = files.anyMatch(p -> p.getFileName().toString().endsWith(".tmp"));
        }
        check("a refused save leaves no temporary files", !leftovers);

        FootballLeague oddPrice = leagueWithTickets();
        oddPrice.getShop().addProduct(new FoodItem("F98", "Odd Price", new BigDecimal("1.005"), 5, false));
        expect("a price with more than 2 decimal places is refused", DataFileException.class, () -> storage.save(oddPrice));

        FootballLeague exact = leagueWithTickets();
        exact.getShop().addProduct(new FoodItem("ABCDEFGHIJ", "Exactly thirty characters long",
                new BigDecimal("1.00"), 1, false));
        Path f2 = tempFolder();
        new LeagueFileStorage(f2).save(exact);
        check("boundary: a 10-character ID and a 30-character name fit exactly",
                new LeagueFileStorage(f2).load().getShop().findProduct("ABCDEFGHIJ").getName()
                        .equals("Exactly thirty characters long"));
    }

    // ---------- Damaged or rule-breaking data is reported ----------
    private static void damagedAndInvalidData() throws Exception {
        Path f1 = tempFolder();
        new LeagueFileStorage(f1).save(leagueWithTickets());
        try (RandomAccessFile raf = new RandomAccessFile(f1.resolve("products.dat").toFile(), "rw")) {
            raf.setLength(raf.length() - 1);
        }
        expect("a products.dat that is not a whole number of records", DataFileException.class,
                () -> new LeagueFileStorage(f1).load());

        Path f2 = tempFolder();
        new LeagueFileStorage(f2).save(leagueWithTickets());
        try (RandomAccessFile raf = new RandomAccessFile(f2.resolve("products.dat").toFile(), "rw")) {
            raf.seek(0);
            raf.writeChar('X');
        }
        expect("an unknown product type", DataFileException.class, () -> new LeagueFileStorage(f2).load());

        Path f3 = tempFolder();
        new LeagueFileStorage(f3).save(leagueWithTickets());
        try (RandomAccessFile raf = new RandomAccessFile(f3.resolve("products.dat").toFile(), "rw")) {
            raf.seek(2 + 20 + 60 + 8);
            raf.writeInt(-4);
        }
        expect("a negative stock in the file", DataFileException.class, () -> new LeagueFileStorage(f3).load());

        Path f4 = tempFolder();
        new LeagueFileStorage(f4).save(leagueWithTickets());
        Files.write(f4.resolve("spectators.txt"), List.of("SP01|Alicia Brown|30|Female", "SP03|Shanice Williams|41|Female"));
        expect("a ticket for a spectator who is no longer saved", DataFileException.class,
                () -> new LeagueFileStorage(f4).load());

        Path f5 = tempFolder();
        new LeagueFileStorage(f5).save(leagueWithTickets());
        try (RandomAccessFile raf = new RandomAccessFile(f5.resolve("tickets.dat").toFile(), "rw")) {
            raf.seek(40 + 30 + 30);
            raf.writeChars("BALCONY   ");
        }
        expect("an unknown ticket category", DataFileException.class, () -> new LeagueFileStorage(f5).load());

        // BR4 is checked again on load: tickets.dat claims more tickets than the stadium has seats.
        FootballLeague tiny = SampleData.createLeague();
        tiny.addStadium(new Stadium("S09", "Tiny Ground", "Kingston", 2, "Fair", new BigDecimal("500.00")));
        tiny.scheduleFixture("F1", "T01", "T02", "S09", D1, T1);
        tiny.sellTickets("F1", "SP01", TicketCategory.GENERAL, 2);
        Path f6 = tempFolder();
        new LeagueFileStorage(f6).save(leagueWithTickets());   // 35,000 seats, so only the duplicate is wrong
        byte[] one = new byte[128];
        try (RandomAccessFile raf = new RandomAccessFile(f6.resolve("tickets.dat").toFile(), "rw")) {
            raf.seek(0);
            raf.readFully(one);
            raf.seek(raf.length());
            raf.write(one);            // a third copy of ticket 1, same ID
        }
        expect("a duplicate ticket ID on load", DataFileException.class, () -> new LeagueFileStorage(f6).load());

        Path f7 = tempFolder();
        new LeagueFileStorage(f7).save(tiny);
        try (RandomAccessFile raf = new RandomAccessFile(f7.resolve("tickets.dat").toFile(), "rw")) {
            raf.seek(0);
            raf.readFully(one);
            long third = raf.length();
            raf.seek(third);
            raf.write(one);
            raf.seek(third);
            raf.writeChars("F1-T0003            ");   // a new ID, so only the capacity rule is broken
        }
        expect("BR4 on load: a third ticket for a 2-seat stadium", DataFileException.class,
                () -> new LeagueFileStorage(f7).load());
    }

    // ---------- A folder saved before Week 4 still loads ----------
    private static void olderSaves() throws Exception {
        Path folder = tempFolder();
        new LeagueFileStorage(folder).save(SampleData.createLeague());
        Files.delete(folder.resolve("spectators.txt"));
        Files.delete(folder.resolve("products.dat"));
        Files.delete(folder.resolve("tickets.dat"));
        FootballLeague loaded = new LeagueFileStorage(folder).load();
        check("an older save without the Week 4 files loads with no spectators, products or tickets",
                loaded.getSpectators().isEmpty() && loaded.getShop().getProducts().isEmpty()
                        && loaded.getTeams().size() == 6);
    }
}
