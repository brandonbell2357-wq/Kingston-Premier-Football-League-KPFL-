
package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
  The categories of match ticket. Each category's price is the stadium's ticket price
  multiplied by the category's factor, so prices stay exact (BigDecimal, 2 decimal places).
  Example at a stadium price of $1,500.00: GENERAL $1,500.00, GRANDSTAND $2,250.00, VIP $3,750.00.
 */
public enum TicketCategory {
    GENERAL("1.00"),
    GRANDSTAND("1.50"),
    VIP("2.50");

    private final BigDecimal priceFactor;

    TicketCategory(String priceFactor) {
        this.priceFactor = new BigDecimal(priceFactor);
    }

    public BigDecimal getPriceFactor() {
        return priceFactor;
    }

    // The price of one ticket in this category, given the stadium's ticket price.
    public BigDecimal priceFor(BigDecimal stadiumTicketPrice) {
        return stadiumTicketPrice.multiply(priceFactor).setScale(2, RoundingMode.HALF_UP);
    }
}
