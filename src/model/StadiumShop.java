package model;

import exception.DuplicateEntryException;
import exception.InsufficientPaymentException;
import exception.InsufficientStockException;
import exception.InvalidDataException;
import exception.NotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
  The stadium shop: manages the inventory of products, completes sales (tax, payment,
  change and receipts) and keeps the list of completed sales for the sales report.
  Products are aggregated: they exist in the catalogue regardless of any one sale.
 */
public class StadiumShop {
    private String name;
    private final List<Product> products = new ArrayList<>();
    private final List<Sale> sales = new ArrayList<>();

    public StadiumShop(String name) throws InvalidDataException {
        setName(name);
    }

    // ---------- Inventory ----------

    public void addProduct(Product product) throws InvalidDataException, DuplicateEntryException {
        if (product == null) {
            throw new InvalidDataException("Product cannot be null.");
        }
        for (Product existing : products) {
            if (existing.getProductId().equalsIgnoreCase(product.getProductId())) {
                throw new DuplicateEntryException("A product with ID " + product.getProductId() + " already exists.");
            }
        }
        products.add(product);
    }

    public Product findProduct(String productId) throws NotFoundException {
        String wanted = productId == null ? "" : productId.trim();
        for (Product product : products) {
            if (product.getProductId().equalsIgnoreCase(wanted)) {
                return product;
            }
        }
        throw new NotFoundException("No product with ID " + wanted + ".");
    }

    public void restock(String productId, int quantity) throws NotFoundException, InvalidDataException {
        findProduct(productId).restock(quantity);
    }

    public void updatePrice(String productId, BigDecimal newPrice) throws NotFoundException, InvalidDataException {
        findProduct(productId).setPrice(newPrice);
    }

    //Products at or below their low-stock level (each kind of product has its own level). 
    public List<Product> getLowStockProducts() {
        List<Product> low = new ArrayList<>();
        for (Product product : products) {
            if (product.isLowStock()) {   // polymorphism: Merchandise and FoodItem answer differently
                low.add(product);
            }
        }
        return low;
    }

    // ---------- Sales (BR5, BR7) ----------

    // Starts a new, empty sale. Add items to it, then call completeSale. 
    public Sale startSale(Spectator customer) {
        return new Sale(customer);
    }

    /*
      Completes a sale. Every check runs before anything changes, so a refused sale
      removes no stock and records nothing:
        - the sale has at least one item, and the payment is not negative
        - BR7: the payment covers the total
        - every product still has enough stock
      Then the stock is removed, the sale gets its ID, and it is recorded.
     */
    public Sale completeSale(Sale sale, BigDecimal amountPaid)
            throws InvalidDataException, InsufficientPaymentException, InsufficientStockException {
        if (sale == null) {
            throw new InvalidDataException("There is no sale to complete.");
        }
        if (sale.isCompleted()) {
            throw new InvalidDataException("Sale " + sale.getSaleId() + " is already completed.");
        }
        sale.checkPayment(amountPaid);
        for (SaleItem item : sale.getItems()) {
            Product product = item.getProduct();
            if (sale.quantityOf(product) > product.getStockQuantity()) {
                throw new InsufficientStockException("Only " + product.getStockQuantity() + " of "
                        + product.getName() + " left in stock.");
            }
        }
        for (SaleItem item : sale.getItems()) {
            item.getProduct().removeStock(item.getQuantity());
        }
        sale.markCompleted(String.format("SALE%04d", sales.size() + 1), amountPaid);
        sales.add(sale);
        return sale;
    }

    // ---------- Totals for the sales report ----------

    public BigDecimal getTotalSales() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Sale sale : sales) {
            total = total.add(sale.getTotal());
        }
        return total;
    }

    public BigDecimal getTotalTaxCollected() {
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (Sale sale : sales) {
            total = total.add(sale.getTax());
        }
        return total;
    }

    /** How many of one product have been sold in all completed sales. */
    public int unitsSold(Product product) {
        int units = 0;
        for (Sale sale : sales) {
            units += sale.quantityOf(product);
        }
        return units;
    }

    // ---------- Accessors ----------

    public String getName() {
        return name;
    }

    public void setName(String name) throws InvalidDataException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidDataException("Shop name cannot be blank.");
        }
        this.name = name.trim();
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }

    public List<Sale> getSales() {
        return Collections.unmodifiableList(sales);
    }
} 
