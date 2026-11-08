
package ui;

import java.util.Scanner;

/**
 * Console menu for the KPFL system.
 * Each option currently prints a placeholder; later weeks replace
 * these with calls to the service classes.
 */
public class Menu {

    private final Scanner scanner = new Scanner(System.in);

    /** Runs the main menu until the user chooses Save & Exit. */
    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choose an option: ", 0, 4);
            switch (choice) {
                case 1 -> teamsAndPersonnelMenu();
                case 2 -> fixturesAndStadiumsMenu();
                case 3 -> ticketsAndShopMenu();
                case 4 -> reportsAndSearchMenu();
                case 0 -> {
                    saveData();
                    System.out.println("Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ---------- Main menu ----------

    private void printMainMenu() {
        System.out.println();
        System.out.println("===== KPFL MAIN MENU =====");
        System.out.println("1. Teams and Personnel");
        System.out.println("2. Fixtures and Stadiums");
        System.out.println("3. Tickets and Shop");
        System.out.println("4. Reports and Search");
        System.out.println("0. Save and Exit");
    }

    // ---------- Submenus ----------

    private void teamsAndPersonnelMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Teams and Personnel ---");
            System.out.println("1. Register team");
            System.out.println("2. Add player to team");
            System.out.println("3. Add coach to team");
            System.out.println("4. View all teams");
            System.out.println("0. Back");
            int choice = readInt("Choose an option: ", 0, 4);
            switch (choice) {
                case 1 -> notImplemented("Register team");
                case 2 -> notImplemented("Add player");
                case 3 -> notImplemented("Add coach");
                case 4 -> notImplemented("View teams");
                case 0 -> back = true;
            }
        }
    }

    private void fixturesAndStadiumsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Fixtures and Stadiums ---");
            System.out.println("1. Add stadium");
            System.out.println("2. Schedule fixture");
            System.out.println("3. Record match result");
            System.out.println("4. View fixtures");
            System.out.println("0. Back");
            int choice = readInt("Choose an option: ", 0, 4);
            switch (choice) {
                case 1 -> notImplemented("Add stadium");
                case 2 -> notImplemented("Schedule fixture");
                case 3 -> notImplemented("Record result");
                case 4 -> notImplemented("View fixtures");
                case 0 -> back = true;
            }
        }
    }

    private void ticketsAndShopMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Tickets and Shop ---");
            System.out.println("1. Sell ticket");
            System.out.println("2. Sell shop item");
            System.out.println("3. View stock");
            System.out.println("0. Back");
            int choice = readInt("Choose an option: ", 0, 3);
            switch (choice) {
                case 1 -> notImplemented("Sell ticket");
                case 2 -> notImplemented("Sell shop item");
                case 3 -> notImplemented("View stock");
                case 0 -> back = true;
            }
        }
    }

    private void reportsAndSearchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Reports and Search ---");
            System.out.println("1. League standings");
            System.out.println("2. Search player by name");
            System.out.println("3. Sales report");
            System.out.println("0. Back");
            int choice = readInt("Choose an option: ", 0, 3);
            switch (choice) {
                case 1 -> notImplemented("Standings");
                case 2 -> notImplemented("Search player");
                case 3 -> notImplemented("Sales report");
                case 0 -> back = true;
            }
        }
    }

    // ---------- Helpers ----------

    /** Placeholder for features not built yet. */
    private void notImplemented(String feature) {
        System.out.println("[" + feature + "] is not implemented yet.");
    }

    /** Placeholder until the storage classes exist (Week 3). */
    private void saveData() {
        System.out.println("Saving data... (not implemented yet)");
    }

    /**
     * Reads a whole number between min and max (inclusive),
     * and keeps asking until the input is valid.
     */
    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
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