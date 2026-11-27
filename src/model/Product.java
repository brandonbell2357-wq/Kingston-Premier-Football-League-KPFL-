package model;

import exception.InsufficientStockException;
import exception.InvalidDataException;
import java.math.BigDecimal;

/*
  Abstract superclass of everything the stadium shop sells (the second inheritance
  hierarchy, beside Person). Merchandise and FoodItem are the concrete kinds of product.
  Prices include 15% GCT, as the brief states, and are kept exact with BigDecimal.
 */
public abstract class Product {
    private final String productId;
    private String name;
    private BigDecimal price;
    private int stockQuantity;

    protected Product(String productId, String name, BigDecimal price, int stockQuantity)
            throws InvalidDataException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new InvalidDataException("Product ID cannot be blank.");
        }
        this.productId = productId.trim();
        setName(name);
        setPrice(price);
        setStockQuantity(stockQuantity);
    }

    // Copy constructor: copies every field of another product. 
    protected Product(Product other) {
        this.productId = other.productId;
        this.name = other.name;
        this.price = other.price;
        this.stockQuantity = other.stockQuantity;
    }

    //The kind of product shown to users, such as "Merchandise". Every subclass must supply it
    public abstract String getProductType();

    /*
      Stock at or below this level counts as low stock. Each kind of product sets its own
      level (polymorphism): food sells faster than merchandise, so it is reordered sooner.
     */
    public abstract int getLowStockThreshold();

    // True when the stock has fallen to the low-stock level for this kind of product. 
    public boolean isLowStock() {
        return stockQuantity <= getLowStockThreshold();
    }

    // One line describing this product. Subclasses add their own details.
    public String describe() {
        return getProductType() + " " + productId + ": " + name + ", $" + price + " (GCT incl.), "
                + stockQuantity + " in stock" + (isLowStock() ? " - LOW STOCK" : "");
    }

    // Adds delivered stock. The quantity must be at least 1 (BR5). 
    public void restock(int quantity) throws InvalidDataException {
        if (quantity <= 0) {
            throw new InvalidDataException("Restock quantity must be at least 1 (value given: " + quantity + ").");
        }
        this.stockQuantity += quantity;
    }

    // Removes sold stock. Refused if the quantity is not above zero (BR5) or is more than is in stock. 
    public void removeStock(int quantity) throws InvalidDataException, InsufficientStockException {
        if (quantity <= 0) {
            throw new InvalidDataException("Quantity must be at least 1 (value given: " + quantity + ").");
        }
        if (quantity > stockQuantity) {
            throw new InsufficientStockException("Only " + stockQuantity + " of " + name
                    + " in stock (asked for " + quantity + ").");
        }
        this.stockQuantity -= quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Product name cannot be blank.");
        }
        this.name = name.trim();
    }

    /** The price of one item, including 15% GCT. */
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) throws InvalidDataException {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDataException("Product price must be greater than 0.");
        }
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) throws InvalidDataException {
        if (stockQuantity < 0) {
            throw new InvalidDataException("Stock cannot be negative (value given: " + stockQuantity + ").");
        }
        this.stockQuantity = stockQuantity;
    }

    @Override
    public String toString() {
        return describe();
    }
}
