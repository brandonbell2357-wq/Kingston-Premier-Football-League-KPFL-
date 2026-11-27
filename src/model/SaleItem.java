package model;

import exception.InvalidDataException;
import java.math.BigDecimal;

/*
  One line of a shop sale: a product, how many, and the unit price at the time of the sale.
  The unit price is copied when the line is created, so a later price change does not
  change a receipt that was already printed.
 */
public final class SaleItem {
    private final Product product;
    private final int quantity;
    private final BigDecimal unitPrice;

    public SaleItem(Product product, int quantity) throws InvalidDataException {
        if (product == null) {
            throw new InvalidDataException("A sale line needs a product.");
        }
        if (quantity <= 0) {
            throw new InvalidDataException("Quantity must be at least 1 (value given: " + quantity + ").");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    // Unit price times quantity (GCT included)
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
