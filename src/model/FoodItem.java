package model;

import exception.InvalidDataException;
import java.math.BigDecimal;

//Food and beverages sold in the stadium shop. 
public class FoodItem extends Product {

    // Food sells quickly on match days, so it is reordered when 20 or fewer are left. 
    public static final int LOW_STOCK_LEVEL = 20;

    private boolean beverage;   // true for drinks, false for food

    public FoodItem(String productId, String name, BigDecimal price, int stockQuantity, boolean beverage)
            throws InvalidDataException {
        super(productId, name, price, stockQuantity);
        this.beverage = beverage;
    }

    // Copy constructor: copies every field of another food item.
    public FoodItem(FoodItem other) {
        super(other);
        this.beverage = other.beverage;
    }

    @Override
    public String getProductType() {
        return beverage ? "Beverage" : "Food";
    }

    @Override
    public int getLowStockThreshold() {
        return LOW_STOCK_LEVEL;
    }

    public boolean isBeverage() {
        return beverage;
    }

    public void setBeverage(boolean beverage) {
        this.beverage = beverage;
    }
}
