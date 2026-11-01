package model;

import exception.InvalidValueException;
import java.math.BigDecimal;

/** Club merchandise such as jerseys and flags. */
public class Merchandise extends Product {
    private String itemType;
    private String size;

    public Merchandise(String productId, String name, BigDecimal price, int stockQuantity,
                       String itemType, String size) throws InvalidValueException {
        super(productId, name, price, stockQuantity);
        this.itemType = Validate.text(itemType, "Item type");
        this.size = Validate.text(size, "Size");
    }

    @Override
    public String getCategory() {
        return "Merchandise";
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) throws InvalidValueException {
        this.itemType = Validate.text(itemType, "Item type");
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) throws InvalidValueException {
        this.size = Validate.text(size, "Size");
    }
}
