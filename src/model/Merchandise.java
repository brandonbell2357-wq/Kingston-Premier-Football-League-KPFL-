package model;

import exception.InvalidDataException;
import java.math.BigDecimal;

// Jerseys, flags, scarves and other team merchandise sold in the stadium shop. 
public class Merchandise extends Product {

    // Merchandise is reordered when 5 or fewer are left. */
    public static final int LOW_STOCK_LEVEL = 5;

    private String size;      // e.g. "S", "M", "L", "XL", or "One size" for flags and scarves
    private String teamName;  // the team the item is for, or "KPFL" for league items

    public Merchandise(String productId, String name, BigDecimal price, int stockQuantity,
                       String size, String teamName) throws InvalidDataException {
        super(productId, name, price, stockQuantity);
        setSize(size);
        setTeamName(teamName);
    }

    // Copy constructor: copies every field of another merchandise item. 
    public Merchandise(Merchandise other) {
        super(other);
        this.size = other.size;
        this.teamName = other.teamName;
    }

    @Override
    public String getProductType() {
        return "Merchandise";
    }

    @Override
    public int getLowStockThreshold() {
        return LOW_STOCK_LEVEL;
    }

    @Override
    public String describe() {
        return super.describe() + " [" + teamName + ", size " + size + "]";
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) throws InvalidDataException {
        if (size == null || size.trim().isEmpty()) {
            throw new InvalidDataException("Size cannot be blank (use \"One size\" if it has none).");
        }
        this.size = size.trim();
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) throws InvalidDataException {
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new InvalidDataException("Team name cannot be blank (use \"KPFL\" for league items).");
        }
        this.teamName = teamName.trim();
    }
}
