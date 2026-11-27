import exception.*;
import model.*;

import java.math.BigDecimal;
import java.util.List;

/*
  Tests for the stadium shop: the Product hierarchy (Product -> Merchandise, FoodItem),
  inventory and low stock, BR5 (quantities above zero), BR7 (payment must cover the total),
  GCT included in prices (GCT = total x 15/115), change and receipts.
  Same style as ModelTest: run with "java -cp out ShopTest".
 The BR7 cases use the numbers from the Week 1 test plan.
 */
public class ShopTest {

    private static int passed = 0;
    private static int failed = 0;

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    // Passes only if the action throws the expected exception type (or a subclass).
    private static void expect(String name, Class<? extends Exception> type, Action action) {
        try {
            action.run();
            failed++;
            System.out.println("FAIL: " + name + " (no exception thrown)");
        } catch (Exception e) {
            if (type.isInstance(e)) {
                passed++;
            } else {
                failed++;
                System.out.println("FAIL: " + name + " (threw " + e.getClass().getSimpleName() + ")");
            }
        }
    }

    // Passes only if the action runs WITHOUT throwing any exception.
    private static void expectNoError(String name, Action action) {
        try {
            action.run();
            passed++;
        } catch (Exception e) {
            failed++;
            System.out.println("FAIL: " + name + " (threw " + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
        }
    }

    private static boolean money(BigDecimal actual, String expected) {
        return actual != null && actual.compareTo(new BigDecimal(expected)) == 0;
    }

    // ---------- Test data helpers ----------

    private static Merchandise jersey(int stock) throws Exception {
        return new Merchandise("M01", "Harbour View Home Jersey", new BigDecimal("1000.00"), stock, "L", "Harbour View FC");
    }

    private static FoodItem patty(int stock) throws Exception {
        return new FoodItem("F01", "Beef Patty", new BigDecimal("215.00"), stock, false);
    }

    private static FoodItem water(int stock) throws Exception {
        return new FoodItem("F02", "Bottled Water", new BigDecimal("150.00"), stock, true);
    }

    //A shop with a jersey (stock 10) and patties (stock 50).
    private static StadiumShop shop(Merchandise jersey, FoodItem patty) throws Exception {
        StadiumShop shop = new StadiumShop("National Stadium Shop");
        shop.addProduct(jersey);
        shop.addProduct(patty);
        return shop;
    }

    // The test plan's sale: 2 jerseys (2 x 1000.00) + 3 patties (3 x 215.00) = 2645.00.
    private static Sale testPlanSale(StadiumShop shop, Product jersey, Product patty) throws Exception {
        Sale sale = shop.startSale(new Spectator("SP1", "Alicia Brown", 30, "Female"));
        sale.addItem(jersey, 2);
        sale.addItem(patty, 3);
        return sale;
    }

    public static void main(String[] args) throws Exception {
        hierarchyTests();
        productRuleTests();
        inventoryTests();
        gctTests();
        br7PaymentTests();
        br5AndStockTests();
        receiptAndReportTests();

        System.out.println("Passed: " + passed + "   Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------- Inheritance and polymorphism (Product hierarchy) ----------
    private static void hierarchyTests() throws Exception {
        List<Product> products = List.of(jersey(10), patty(50), water(30));   // superclass references
        check("Merchandise is a Product", products.get(0) instanceof Product);
        check("Merchandise type", products.get(0).getProductType().equals("Merchandise"));
        check("FoodItem (food) type", products.get(1).getProductType().equals("Food"));
        check("FoodItem (drink) type", products.get(2).getProductType().equals("Beverage"));
        check("each kind has its own low-stock level (polymorphism)",
                products.get(0).getLowStockThreshold() == 5 && products.get(1).getLowStockThreshold() == 20);
        check("Merchandise.describe() adds team and size",
                products.get(0).describe().contains("Harbour View FC") && products.get(0).describe().contains("size L"));
        check("toString uses describe()", products.get(1).toString().equals(products.get(1).describe()));

        Merchandise original = jersey(10);
        Merchandise copy = new Merchandise(original);
        copy.restock(5);
        check("copy constructor: changing the copy does not change the original",
                original.getStockQuantity() == 10 && copy.getStockQuantity() == 15);
    }

    // ---------- Product validation ----------
    private static void productRuleTests() throws Exception {
        expect("blank product id", InvalidDataException.class,
                () -> new FoodItem(" ", "Patty", new BigDecimal("215.00"), 5, false));
        expect("blank product name", InvalidDataException.class,
                () -> new FoodItem("F9", "", new BigDecimal("215.00"), 5, false));
        expect("price 0 is not allowed", InvalidDataException.class,
                () -> new FoodItem("F9", "Patty", BigDecimal.ZERO, 5, false));
        expect("negative price", InvalidDataException.class,
                () -> new FoodItem("F9", "Patty", new BigDecimal("-1"), 5, false));
        expect("negative stock", InvalidDataException.class,
                () -> new FoodItem("F9", "Patty", new BigDecimal("215.00"), -1, false));
        check("stock 0 is allowed (boundary)", new FoodItem("F9", "Patty", new BigDecimal("215.00"), 0, false).getStockQuantity() == 0);
        expect("blank merchandise size", InvalidDataException.class,
                () -> new Merchandise("M9", "Flag", new BigDecimal("500"), 5, " ", "KPFL"));
        expect("blank merchandise team", InvalidDataException.class,
                () -> new Merchandise("M9", "Flag", new BigDecimal("500"), 5, "One size", ""));
    }

    // ---------- Inventory ----------
    private static void inventoryTests() throws Exception {
        Merchandise jersey = jersey(10);
        FoodItem patty = patty(50);
        StadiumShop shop = shop(jersey, patty);

        expect("duplicate product id", DuplicateEntryException.class, () -> shop.addProduct(jersey(3)));
        expect("null product", InvalidDataException.class, () -> shop.addProduct(null));
        check("find product by id (any case)", shop.findProduct("m01") == jersey);
        expect("find a product that does not exist", NotFoundException.class, () -> shop.findProduct("X99"));

        shop.restock("M01", 5);
        check("restock adds stock", jersey.getStockQuantity() == 15);
        expect("restock 0 (BR5)", InvalidDataException.class, () -> shop.restock("M01", 0));
        expect("restock -3 (BR5)", InvalidDataException.class, () -> shop.restock("M01", -3));
        check("stock unchanged after bad restock", jersey.getStockQuantity() == 15);

        shop.updatePrice("F01", new BigDecimal("230.00"));
        check("price updated", money(patty.getPrice(), "230.00"));
        expect("cannot set price to 0", InvalidDataException.class, () -> shop.updatePrice("F01", BigDecimal.ZERO));

        // Low stock: merchandise at 5 or less, food at 20 or less.
        StadiumShop lowShop = new StadiumShop("Low Shop");
        lowShop.addProduct(new Merchandise("M1", "Flag", new BigDecimal("500"), 5, "One size", "KPFL"));   // = 5 -> low
        lowShop.addProduct(new Merchandise("M2", "Scarf", new BigDecimal("800"), 6, "One size", "KPFL"));  // 6 -> ok
        lowShop.addProduct(new FoodItem("F1", "Patty", new BigDecimal("215"), 20, false));                 // = 20 -> low
        lowShop.addProduct(new FoodItem("F2", "Juice", new BigDecimal("200"), 21, true));                  // 21 -> ok
        List<Product> low = lowShop.getLowStockProducts();
        check("low stock boundary: merchandise at 5 and food at 20 are low; 6 and 21 are not",
                low.size() == 2 && low.get(0).getProductId().equals("M1") && low.get(1).getProductId().equals("F1"));
    }

    // ---------- GCT included in prices: GCT = total x 15 / 115 ----------
    private static void gctTests() throws Exception {
        StadiumShop shop = shop(jersey(10), patty(50));
        Sale sale = testPlanSale(shop, shop.findProduct("M01"), shop.findProduct("F01"));
        check("test plan total: 2 x 1000.00 + 3 x 215.00 = 2645.00", money(sale.getTotal(), "2645.00"));
        check("GCT in 2645.00 is 345.00", money(sale.getTax(), "345.00"));
        check("subtotal before GCT is 2300.00", money(sale.getSubtotal(), "2300.00"));

        StadiumShop shop2 = new StadiumShop("Shop 2");
        FoodItem hundred = new FoodItem("F5", "Combo", new BigDecimal("100.00"), 10, false);
        shop2.addProduct(hundred);
        Sale odd = shop2.startSale(null);
        odd.addItem(hundred, 1);
        check("rounding: GCT in 100.00 is 13.04 (13.0434... rounded)", money(odd.getTax(), "13.04"));
        check("subtotal + GCT always equals the total exactly",
                odd.getSubtotal().add(odd.getTax()).compareTo(odd.getTotal()) == 0 && money(odd.getSubtotal(), "86.96"));
        check("money has 2 decimal places", odd.getTotal().scale() == 2 && odd.getTax().scale() == 2);
    }

    // ---------- BR7: a transaction cannot complete when payment is insufficient ----------
    private static void br7PaymentTests() throws Exception {
        // Normal: total 2,645.00, paid 3,000.00 -> change 355.00; stock reduced.
        Merchandise jersey = jersey(10);
        FoodItem patty = patty(50);
        StadiumShop shop = shop(jersey, patty);
        Sale normal = testPlanSale(shop, jersey, patty);
        expectNoError("BR7 normal: paid 3000.00 for 2645.00", () -> shop.completeSale(normal, new BigDecimal("3000.00")));
        check("BR7 normal: change is 355.00", money(normal.getChange(), "355.00"));
        check("BR7 normal: stock reduced (jersey 10 -> 8, patty 50 -> 47)",
                jersey.getStockQuantity() == 8 && patty.getStockQuantity() == 47);
        check("completed sale gets an ID", "SALE0001".equals(normal.getSaleId()));

        // Boundary: paid equals the total -> accepted, change 0.00.
        Sale exact = testPlanSale(shop, jersey, patty);
        expectNoError("BR7 boundary: paid exactly the total", () -> shop.completeSale(exact, new BigDecimal("2645.00")));
        check("BR7 boundary: change is 0.00", money(exact.getChange(), "0.00"));

        // Boundary: paid is 0.01 below the total -> refused, stock unchanged.
        Merchandise j2 = jersey(10);
        FoodItem p2 = patty(50);
        StadiumShop shop2 = shop(j2, p2);
        Sale shortSale = testPlanSale(shop2, j2, p2);
        expect("BR7 boundary: paid 0.01 below the total", InsufficientPaymentException.class,
                () -> shop2.completeSale(shortSale, new BigDecimal("2644.99")));
        check("BR7: stock unchanged after refusal", j2.getStockQuantity() == 10 && p2.getStockQuantity() == 50);
        check("BR7: refused sale is not recorded and not completed",
                shop2.getSales().isEmpty() && !shortSale.isCompleted() && shortSale.getChange() == null);
        expectNoError("BR7: the same sale can be paid properly afterwards",
                () -> shop2.completeSale(shortSale, new BigDecimal("2645.00")));

        expect("negative payment", InvalidDataException.class,
                () -> shop2.completeSale(testPlanSale(shop2, j2, p2), new BigDecimal("-5")));
        expect("empty sale cannot be completed", InvalidDataException.class,
                () -> shop2.completeSale(shop2.startSale(null), new BigDecimal("100")));
        expect("a sale cannot be completed twice", InvalidDataException.class,
                () -> shop2.completeSale(shortSale, new BigDecimal("3000")));
    }

    // ---------- BR5 quantities and stock ----------
    private static void br5AndStockTests() throws Exception {
        Merchandise jersey = jersey(3);
        StadiumShop shop = new StadiumShop("Shop");
        shop.addProduct(jersey);
        Sale sale = shop.startSale(null);

        expect("BR5 invalid: shop quantity -1", InvalidDataException.class, () -> sale.addItem(jersey, -1));
        expect("BR5 invalid: shop quantity 0", InvalidDataException.class, () -> sale.addItem(jersey, 0));
        expect("more than is in stock", InsufficientStockException.class, () -> sale.addItem(jersey, 4));
        expectNoError("boundary: exactly the stock", () -> sale.addItem(jersey, 3));
        expect("adding the same product again would go over stock", InsufficientStockException.class,
                () -> sale.addItem(jersey, 1));
        check("stock not removed until the sale is completed", jersey.getStockQuantity() == 3);

        // Stock sold elsewhere between adding and paying: completion is refused and nothing changes.
        Merchandise flag = new Merchandise("M2", "Flag", new BigDecimal("500.00"), 2, "One size", "KPFL");
        FoodItem patty = patty(50);
        StadiumShop shop2 = new StadiumShop("Shop 2");
        shop2.addProduct(flag);
        shop2.addProduct(patty);
        Sale first = shop2.startSale(null);
        first.addItem(patty, 5);
        first.addItem(flag, 2);
        Sale second = shop2.startSale(null);
        second.addItem(flag, 2);
        shop2.completeSale(second, new BigDecimal("1000.00"));
        expect("completing a sale when stock ran out", InsufficientStockException.class,
                () -> shop2.completeSale(first, new BigDecimal("5000.00")));
        check("all-or-nothing: patty stock unchanged when the sale is refused", patty.getStockQuantity() == 50);
    }

    // ---------- Receipt and totals for the sales report ----------
    private static void receiptAndReportTests() throws Exception {
        Merchandise jersey = jersey(10);
        FoodItem patty = patty(50);
        StadiumShop shop = shop(jersey, patty);
        Sale sale = testPlanSale(shop, jersey, patty);
        shop.completeSale(sale, new BigDecimal("3000.00"));
        String receipt = sale.toReceipt();
        check("receipt shows each item", receipt.contains("Harbour View Home Jer") && receipt.contains("Beef Patty"));
        check("receipt shows subtotal, GCT, total, paid and change",
                receipt.contains("$2300.00") && receipt.contains("$345.00") && receipt.contains("$2645.00")
                        && receipt.contains("$3000.00") && receipt.contains("$355.00"));
        check("receipt shows the customer and sale ID", receipt.contains("Alicia Brown") && receipt.contains("SALE0001"));

        shop.updatePrice("M01", new BigDecimal("1200.00"));
        check("a later price change does not change a completed sale", money(sale.getTotal(), "2645.00"));

        Sale second = shop.startSale(null);
        second.addItem(patty, 2);
        shop.completeSale(second, new BigDecimal("500.00"));
        check("total sales across sales: 2645.00 + 430.00 = 3075.00", money(shop.getTotalSales(), "3075.00"));
        check("total GCT collected: 345.00 + 56.09 = 401.09", money(shop.getTotalTaxCollected(), "401.09"));
        check("units sold per product", shop.unitsSold(patty) == 5 && shop.unitsSold(jersey) == 2);
        expect("the shop's product list cannot be changed from outside", UnsupportedOperationException.class,
                () -> shop.getProducts().clear());
    }
}
