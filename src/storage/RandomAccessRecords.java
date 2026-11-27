package storage;

import exception.DataFileException;
import exception.InvalidDataException;
import model.FoodItem;
import model.Merchandise;
import model.Product;
import model.Ticket;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*
  Reads and writes the shop's products and the sold tickets as RANDOM ACCESS files, as planned
  in the Week 1 design (section 5): every record has the same fixed length, so record number n
  starts at byte n x RECORD_SIZE and can be read directly with seek(), without reading the
  records before it.

  Text fields are written as a fixed number of characters (writeChars, 2 bytes each), padded with
  spaces. Money is stored as a whole number of cents (a long), so no rounding can creep in.
  Text that is too long for its field is refused (DataFileException) instead of being cut short.

  products.dat, one record = 175 bytes:
    type        char       2   'M' = Merchandise, 'F' = FoodItem
    productId   10 chars  20
    name        30 chars  60
    priceCents  long       8
    stock       int        4
    teamName    30 chars  60   Merchandise only (spaces for food)
    size        10 chars  20   Merchandise only (spaces for food)
    beverage    boolean    1   FoodItem only (false for merchandise)

  tickets.dat, one record = 128 bytes:
    ticketId    20 chars  40
    fixtureId   15 chars  30
    spectatorId 15 chars  30
    category    10 chars  20
    priceCents  long       8
 */
public final class RandomAccessRecords {

    public static final int ID_CHARS = 10;
    public static final int NAME_CHARS = 30;
    public static final int TEAM_CHARS = 30;
    public static final int SIZE_CHARS = 10;
    public static final int PRODUCT_RECORD_SIZE = 2 + 2 * ID_CHARS + 2 * NAME_CHARS + 8 + 4
            + 2 * TEAM_CHARS + 2 * SIZE_CHARS + 1;   // 175 bytes

    public static final int TICKET_ID_CHARS = 20;
    public static final int FIXTURE_ID_CHARS = 15;
    public static final int SPECTATOR_ID_CHARS = 15;
    public static final int CATEGORY_CHARS = 10;
    public static final int TICKET_RECORD_SIZE = 2 * TICKET_ID_CHARS + 2 * FIXTURE_ID_CHARS
            + 2 * SPECTATOR_ID_CHARS + 2 * CATEGORY_CHARS + 8;   // 128 bytes

    /** One ticket record as read from tickets.dat; the storage class turns it back into a Ticket. */
    public record TicketRow(String ticketId, String fixtureId, String spectatorId, String category,
                            BigDecimal price) {
    }

    private RandomAccessRecords() {
        // only static methods
    }

    // =====================================================================
    // Products
    // =====================================================================

    /** Writes every product, one fixed-length record each, replacing anything already in the file. */
    public static void writeProducts(Path file, List<Product> products) throws DataFileException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw")) {
            raf.setLength(0);
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                raf.seek((long) i * PRODUCT_RECORD_SIZE);
                if (p instanceof Merchandise m) {
                    raf.writeChar('M');
                    writeFixed(raf, "productId", p.getProductId(), ID_CHARS);
                    writeFixed(raf, "product name", p.getName(), NAME_CHARS);
                    raf.writeLong(toCents(p.getPrice()));
                    raf.writeInt(p.getStockQuantity());
                    writeFixed(raf, "team name", m.getTeamName(), TEAM_CHARS);
                    writeFixed(raf, "size", m.getSize(), SIZE_CHARS);
                    raf.writeBoolean(false);
                } else if (p instanceof FoodItem f) {
                    raf.writeChar('F');
                    writeFixed(raf, "productId", p.getProductId(), ID_CHARS);
                    writeFixed(raf, "product name", p.getName(), NAME_CHARS);
                    raf.writeLong(toCents(p.getPrice()));
                    raf.writeInt(p.getStockQuantity());
                    writeFixed(raf, "team name", "", TEAM_CHARS);
                    writeFixed(raf, "size", "", SIZE_CHARS);
                    raf.writeBoolean(f.isBeverage());
                } else {
                    throw new DataFileException("Unknown kind of product: " + p.getClass().getSimpleName());
                }
            }
        } catch (IOException e) {
            throw new DataFileException("Could not write " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    /** Reads every product record in order. A missing file means there are no products yet. */
    public static List<Product> readProducts(Path file) throws DataFileException {
        List<Product> products = new ArrayList<>();
        if (!Files.exists(file)) {
            return products;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long count = recordCount(raf, PRODUCT_RECORD_SIZE, file);
            for (int i = 0; i < count; i++) {
                products.add(readProductAt(raf, i, file));
            }
        } catch (IOException e) {
            throw new DataFileException("Could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
        return products;
    }

    /**
     * Reads ONE product directly by its record number (0 = the first record), using seek().
     * This is the point of a random access file: no earlier record is read.
     */
    public static Product readProduct(Path file, int recordNumber) throws DataFileException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long count = recordCount(raf, PRODUCT_RECORD_SIZE, file);
            if (recordNumber < 0 || recordNumber >= count) {
                throw new DataFileException(file.getFileName() + " has no record number " + recordNumber
                        + " (it holds " + count + " records).");
            }
            return readProductAt(raf, recordNumber, file);
        } catch (IOException e) {
            throw new DataFileException("Could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    private static Product readProductAt(RandomAccessFile raf, int recordNumber, Path file)
            throws IOException, DataFileException {
        raf.seek((long) recordNumber * PRODUCT_RECORD_SIZE);
        char type = raf.readChar();
        String id = readFixed(raf, ID_CHARS);
        String name = readFixed(raf, NAME_CHARS);
        BigDecimal price = fromCents(raf.readLong());
        int stock = raf.readInt();
        String team = readFixed(raf, TEAM_CHARS);
        String size = readFixed(raf, SIZE_CHARS);
        boolean beverage = raf.readBoolean();
        try {
            if (type == 'M') {
                return new Merchandise(id, name, price, stock, size, team);
            }
            if (type == 'F') {
                return new FoodItem(id, name, price, stock, beverage);
            }
        } catch (InvalidDataException e) {
            throw new DataFileException(file.getFileName() + " record " + recordNumber + " is invalid: "
                    + e.getMessage(), e);
        }
        throw new DataFileException(file.getFileName() + " record " + recordNumber
                + " has an unknown product type '" + type + "'.");
    }

    // =====================================================================
    // Tickets
    // =====================================================================

    /** Writes every ticket, one fixed-length record each, replacing anything already in the file. */
    public static void writeTickets(Path file, List<Ticket> tickets) throws DataFileException {
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "rw")) {
            raf.setLength(0);
            for (int i = 0; i < tickets.size(); i++) {
                Ticket t = tickets.get(i);
                raf.seek((long) i * TICKET_RECORD_SIZE);
                writeFixed(raf, "ticket ID", t.getTicketId(), TICKET_ID_CHARS);
                writeFixed(raf, "fixture ID", t.getFixture().getFixtureId(), FIXTURE_ID_CHARS);
                writeFixed(raf, "spectator ID", t.getSpectator().getId(), SPECTATOR_ID_CHARS);
                writeFixed(raf, "category", t.getCategory().name(), CATEGORY_CHARS);
                raf.writeLong(toCents(t.getPrice()));
            }
        } catch (IOException e) {
            throw new DataFileException("Could not write " + file.getFileName() + ": " + e.getMessage(), e);
        }
    }

    /** Reads every ticket record in order. A missing file means no tickets have been sold yet. */
    public static List<TicketRow> readTickets(Path file) throws DataFileException {
        List<TicketRow> rows = new ArrayList<>();
        if (!Files.exists(file)) {
            return rows;
        }
        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long count = recordCount(raf, TICKET_RECORD_SIZE, file);
            for (int i = 0; i < count; i++) {
                raf.seek((long) i * TICKET_RECORD_SIZE);
                rows.add(new TicketRow(readFixed(raf, TICKET_ID_CHARS), readFixed(raf, FIXTURE_ID_CHARS),
                        readFixed(raf, SPECTATOR_ID_CHARS), readFixed(raf, CATEGORY_CHARS),
                        fromCents(raf.readLong())));
            }
        } catch (IOException e) {
            throw new DataFileException("Could not read " + file.getFileName() + ": " + e.getMessage(), e);
        }
        return rows;
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    /** Number of records in the file. A length that is not a whole number of records means damage. */
    private static long recordCount(RandomAccessFile raf, int recordSize, Path file)
            throws IOException, DataFileException {
        long length = raf.length();
        if (length % recordSize != 0) {
            throw new DataFileException(file.getFileName() + " is damaged: its size (" + length
                    + " bytes) is not a whole number of " + recordSize + "-byte records.");
        }
        return length / recordSize;
    }

    /** Writes text as exactly "chars" characters, padded with spaces. Too long is refused. */
    private static void writeFixed(RandomAccessFile raf, String field, String text, int chars)
            throws IOException, DataFileException {
        String value = text == null ? "" : text;
        if (value.length() > chars) {
            throw new DataFileException("The " + field + " \"" + value + "\" is too long to save: the most is "
                    + chars + " characters.");
        }
        StringBuilder padded = new StringBuilder(value);
        while (padded.length() < chars) {
            padded.append(' ');
        }
        raf.writeChars(padded.toString());
    }

    /** Reads exactly "chars" characters and removes the padding spaces at the end. */
    private static String readFixed(RandomAccessFile raf, int chars) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars; i++) {
            sb.append(raf.readChar());
        }
        int end = sb.length();
        while (end > 0 && sb.charAt(end - 1) == ' ') {
            end--;
        }
        return sb.substring(0, end);
    }

    /** $1,500.00 -> 150000 cents. A price with more than 2 decimal places is refused. */
    private static long toCents(BigDecimal price) throws DataFileException {
        try {
            return price.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
        } catch (ArithmeticException e) {
            throw new DataFileException("The price " + price.toPlainString()
                    + " cannot be saved: prices must have at most 2 decimal places.", e);
        }
    }

    private static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }
}
