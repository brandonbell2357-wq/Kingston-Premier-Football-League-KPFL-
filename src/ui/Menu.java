package ui;

import java.util.Scanner;

/*
  Console menu for the KPFL system following the Console Menu Design
  in the Week 1 requirements document (section 8).
  Each option currently prints a placeholder; later weeks replace
  these with calls to the service classes.
  In every menu the last option goes back (or saves and exits on the main menu).
 */
public class Menu {

    private final Scanner scanner = new Scanner(System.in);

    // Runs the main menu until the user chooses Save Data & Exit. 
    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter option (1-5): ", 1, 5);
            switch (choice) {
                case 1 -> teamManagementMenu();
                case 2 -> fixturesAndMatchMenu();
                case 3 -> ticketingAndShopMenu();
                case 4 -> reportsMenu();
                case 5 -> {
                    saveData();
                    System.out.println("Exiting KPFL Management System. Goodbye!");
                    running = false;
                }
            }
        }
    }

    // ---------- Main menu ----------

    private void printMainMenu() {
        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println("                    MAIN MENU                    ");
        System.out.println("-------------------------------------------------");
        System.out.println("1. Team & Squad Management");
        System.out.println("2. Fixtures & Match Operations");
        System.out.println("3. Ticketing & Stadium Shop");
        System.out.println("4. Management Reports");
        System.out.println("5. Save Data & Exit");
        System.out.println("-------------------------------------------------");
    }

    // ---------- Submenus ----------

    private void teamManagementMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- 1. Team & Squad Management ---");
            System.out.println("1. Register New Team");
            System.out.println("2. Update / Approve / Suspend Team Status");
            System.out.println("3. Search & Display Team Details");
            System.out.println("4. Add Player / Coach to Squad");
            System.out.println("5. Remove Player / Coach from Squad");
            System.out.println("6. Return to Main Menu");
            int choice = readInt("Enter option (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> notImplemented("1.1 Register New Team");
                case 2 -> notImplemented("1.2 Update / Approve / Suspend Team Status");
                case 3 -> notImplemented("1.3 Search & Display Team Details");
                case 4 -> notImplemented("1.4 Add Player / Coach to Squad");
                case 5 -> notImplemented("1.5 Remove Player / Coach from Squad");
                case 6 -> back = true;
            }
        }
    }

    private void fixturesAndMatchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- 2. Fixtures & Match Operations ---");
            System.out.println("1. Register Stadium");
            System.out.println("2. Register Match Official");
            System.out.println("3. Create Fixture");
            System.out.println("4. Assign Match Official to Fixture");
            System.out.println("5. Record Match Result & Update Standings");
            System.out.println("6. Return to Main Menu");
            int choice = readInt("Enter option (1-6): ", 1, 6);
            switch (choice) {
                case 1 -> notImplemented("2.1 Register Stadium");
                case 2 -> notImplemented("2.2 Register Match Official");
                case 3 -> notImplemented("2.3 Create Fixture");
                case 4 -> notImplemented("2.4 Assign Match Official to Fixture");
                case 5 -> notImplemented("2.5 Record Match Result & Update Standings");
                case 6 -> back = true;
            }
        }
    }

    private void ticketingAndShopMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- 3. Ticketing & Stadium Shop ---");
            System.out.println("1. Purchase Match Tickets");
            System.out.println("2. Add / Update Shop Products & Stock");
            System.out.println("3. Process Shop Transaction (Subtotal, Tax, Payment, Change, Receipt)");
            System.out.println("4. Return to Main Menu");
            int choice = readInt("Enter option (1-4): ", 1, 4);
            switch (choice) {
                case 1 -> notImplemented("3.1 Purchase Match Tickets");
                case 2 -> notImplemented("3.2 Add / Update Shop Products & Stock");
                case 3 -> notImplemented("3.3 Process Shop Transaction");
                case 4 -> back = true;
            }
        }
    }

    private void reportsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- 4. Management Reports ---");
            System.out.println("1. View League Standings Report");
            System.out.println("2. View Fixtures, Attendance & Ticket Revenue Report");
            System.out.println("3. View Shop Sales & Low Stock Report");
            System.out.println("4. Return to Main Menu");
            int choice = readInt("Enter option (1-4): ", 1, 4);
            switch (choice) {
                case 1 -> notImplemented("4.1 League Standings Report");
                case 2 -> notImplemented("4.2 Fixtures, Attendance & Ticket Revenue Report");
                case 3 -> notImplemented("4.3 Shop Sales & Low Stock Report");
                case 4 -> back = true;
            }
        }
    }

    // ---------- Helpers ----------

    //Placeholder for features not built yet.
    private void notImplemented(String feature) {
        System.out.println("[" + feature + "] is not implemented yet.");
    }

    // Placeholder until the storage classes exist.
    private void saveData() {
        System.out.println("Saving data... (not implemented yet)");
    }

    /*
      Reads a whole number between min and max (inclusive),
      and keeps asking until the input is valid.
      If the input ends, it returns max, which is always the
      Return / Save & Exit option, so the program closes cleanly.
     */
    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                System.out.println();
                return max;
            }
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Enter a number, not text.");
            }
        }
    }
}
