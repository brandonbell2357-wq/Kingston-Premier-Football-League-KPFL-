package model;

import exception.InsufficientStockException;
import exception.InvalidValueException;
import java.math.BigDecimal;

/** Abstract superclass for everything the stadium shop sells. Prices include 15% GCT. */
public abstract class Product {
    private final String productId;
    private String name;
    private BigDecimal price;
    private int stockQuantity;

    protected Product(String productId, String name, BigDecimal price, int stockQuantity) throws InvalidValueException {
        this.productId = Validate.text(productId, "Product id");
        this.name = Validate.text(name, "Product name");
        this.price = Validate.nonNegative(price, "Price");
        this.stockQuantity = Validate.nonNegative(stockQuantity, "Stock quantity");
    }

    /** The product category shown to users, such as "Merchandise". Every subclass must supply it. */
    public abstract String getCategory();

    /** Takes units out of stock. The quantity must be above zero (BR5) and no more than is in stock. */
    public void reduceStock(int quantity) throws InvalidValueException, InsufficientStockException {
        Validate.positive(quantity, "Quantity");
        if (quantity > stockQuantity) {
            throw new InsufficientStockException("Only " + stockQuantity + " of " + name + " in stock, but " + quantity + " requested.");
        }
        stockQuantity -= quantity;
    }

    /** Puts units into stock. The quantity must be above zero (BR5). */
    public void addStock(int quantity) throws InvalidValueException {
        stockQuantity += Validate.positive(quantity, "Quantity");
    }

    public String describe() {
        return productId + " " + name + " [" + getCategory() + "] " + price + " (" + stockQuantity + " in stock)";
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidValueException {
        this.name = Validate.text(name, "Product name");
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) throws InvalidValueException {
        this.price = Validate.nonNegative(price, "Price");
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    @Override
    public String toString() {
        return describe();
    }
}
