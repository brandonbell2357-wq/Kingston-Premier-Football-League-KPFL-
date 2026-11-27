package model;

import exception.InsufficientPaymentException;
import exception.InsufficientStockException;
import exception.InvalidDataException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
  One shop transaction. Items are added first; then the shop completes it with the
  amount paid. Shop prices already include 15% GCT, so the tax is the part of the
  total that is GCT:  GCT = total x 15 / 115,  subtotal (before GCT) = total - GCT.
  Example from the test plan: total $2,645.00 -> GCT $345.00, subtotal $2,300.00;
  paid $3,000.00 -> change $355.00.
  All money is BigDecimal rounded to the cent, so tax and change are exact.
 */
public class Sale {

    // GCT rate included in shop prices: 15%.
    public static final BigDecimal GCT_RATE = new BigDecimal("0.15");

    private String saleId;          // given by the shop when the sale is completed
    private final Spectator customer;
    private final List<SaleItem> items = new ArrayList<>();
    private BigDecimal amountPaid;  // null until the sale is completed
    private LocalDateTime completedAt;

    //Starts an empty sale. The customer may be null for a walk-in customer. 
    public Sale(Spectator customer) {
        this.customer = customer;
    }

    /*
      Adds a product to the sale. The quantity must be at least 1 (BR5) and, together with
      any of the same product already in this sale, cannot be more than is in stock.
      Stock is NOT removed yet: that only happens when the shop completes the sale.
     */
    public void addItem(Product product, int quantity) throws InvalidDataException, InsufficientStockException {
        if (isCompleted()) {
            throw new InvalidDataException("Sale " + saleId + " is already completed.");
        }
        SaleItem item = new SaleItem(product, quantity);   // checks product and quantity (BR5)
        int alreadyInSale = quantityOf(product);
        if (alreadyInSale + quantity > product.getStockQuantity()) {
            throw new InsufficientStockException("Only " + product.getStockQuantity() + " of " + product.getName()
                    + " in stock (this sale would need " + (alreadyInSale + quantity) + ").");
        }
        items.add(item);
    }

    // How many of this product are already in the sale.
    public int quantityOf(Product product) {
        int total = 0;
        for (SaleItem item : items) {
            if (item.getProduct() == product) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    // ---------- Money (exact, 2 decimal places) ----------

    //Sum of all line totals. Prices include GCT, so this is what the customer pays. 
    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : items) {
            total = total.add(item.getLineTotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    // The GCT contained in the total: total x 15 / 115.
    public BigDecimal getTax() {
        BigDecimal rate = GCT_RATE;
        return getTotal().multiply(rate).divide(BigDecimal.ONE.add(rate), 2, RoundingMode.HALF_UP);
    }

    // The total before GCT. Subtotal + tax always equals the total exactly.
    public BigDecimal getSubtotal() {
        return getTotal().subtract(getTax());
    }

    //Change to give back, or null if the sale is not completed yet.
    public BigDecimal getChange() {
        return amountPaid == null ? null : amountPaid.subtract(getTotal());
    }

    // ---------- Completing (called by StadiumShop) ----------

    /*
      Checks that the payment covers the total (BR7). Package-private: only the shop,
      which also removes the stock, can complete a sale.
     */
    void checkPayment(BigDecimal paid) throws InvalidDataException, InsufficientPaymentException {
        if (items.isEmpty()) {
            throw new InvalidDataException("A sale needs at least one item.");
        }
        if (paid == null || paid.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDataException("Amount paid cannot be empty or negative.");
        }
        if (paid.compareTo(getTotal()) < 0) {
            throw new InsufficientPaymentException("Payment of $" + paid.setScale(2, RoundingMode.HALF_UP)
                    + " is less than the total of $" + getTotal() + " (short by $"
                    + getTotal().subtract(paid).setScale(2, RoundingMode.HALF_UP) + ").");
        }
    }

    // Marks the sale as completed. Only the shop calls this, after every check has passed. 
    void markCompleted(String saleId, BigDecimal paid) {
        this.saleId = saleId;
        this.amountPaid = paid.setScale(2, RoundingMode.HALF_UP);
        this.completedAt = LocalDateTime.now().withNano(0);
    }

    public boolean isCompleted() {
        return amountPaid != null;
    }

    // ---------- Receipt ----------

    //The printed receipt: every line, subtotal, GCT, total, amount paid and change.
    public String toReceipt() {
        StringBuilder r = new StringBuilder();
        r.append("========================================\n");
        r.append("        KPFL STADIUM SHOP RECEIPT       \n");
        r.append("========================================\n");
        r.append("Sale: ").append(saleId == null ? "(not completed)" : saleId).append('\n');
        if (completedAt != null) {
            r.append("Date: ").append(completedAt.toString().replace('T', ' ')).append('\n');
        }
        r.append("Customer: ").append(customer == null ? "Walk-in" : customer.getName() + " (" + customer.getId() + ")")
                .append('\n');
        r.append("----------------------------------------\n");
        for (SaleItem item : items) {
            r.append(String.format("%-22s %3d x %9s%n", shorten(item.getProduct().getName()),
                    item.getQuantity(), item.getUnitPrice()));
            r.append(String.format("%40s%n", "$" + item.getLineTotal().setScale(2, RoundingMode.HALF_UP)));
        }
        r.append("----------------------------------------\n");
        r.append(String.format("%-25s %14s%n", "Subtotal (before GCT):", "$" + getSubtotal()));
        r.append(String.format("%-25s %14s%n", "GCT (15%):", "$" + getTax()));
        r.append(String.format("%-25s %14s%n", "TOTAL:", "$" + getTotal()));
        if (isCompleted()) {
            r.append(String.format("%-25s %14s%n", "Paid:", "$" + amountPaid));
            r.append(String.format("%-25s %14s%n", "Change:", "$" + getChange()));
        }
        r.append("========================================\n");
        return r.toString();
    }

    private static String shorten(String text) {
        return text.length() <= 22 ? text : text.substring(0, 21) + ".";
    }

    public String getSaleId() {
        return saleId;
    }

    public Spectator getCustomer() {
        return customer;
    }

    public List<SaleItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
