package model;

import exception.InvalidValueException;
import java.math.BigDecimal;

/** Food or a beverage sold at the stadium. */
public class FoodItem extends Product {
    private boolean beverage;

    public FoodItem(String productId, String name, BigDecimal price, int stockQuantity,
                    boolean beverage) throws InvalidValueException {
        super(productId, name, price, stockQuantity);
        this.beverage = beverage;
    }

    @Override
    public String getCategory() {
        return beverage ? "Beverage" : "Food";
    }

    public boolean isBeverage() {
        return beverage;
    }

    public void setBeverage(boolean beverage) {
        this.beverage = beverage;
    }
}
