
package ui;

import model.FootballTeam;
import model.Stadium;

import java.util.Scanner;

/**
 * Console entry point prototype for Week 2 core implementation.
 * Demonstrates model instantiation, string representations, and the menu interface shell.
 */
public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("=================================================");
        System.out.println("  KINGSTON PREMIER FOOTBALL LEAGUE (KPFL) SYSTEM  ");
        System.out.println("=================================================");
        System.out.println("[SYSTEM INIT] Testing Week 2 Domain Entity Instantiation...\n");

        // 1. Instantiate and verify Stadium model
        try {
            Stadium nationalStadium = new Stadium("STD01", "National Stadium", "Kingston", 35000, "Excellent", 1500.00);
            System.out.println("✓ Stadium created successfully:");
            System.out.println("  " + nationalStadium);
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Stadium creation failed: " + e.getMessage());
        }

        // 2. Instantiate and verify FootballTeam model
        try {
            FootballTeam harbourView = new FootballTeam("TM01", "Harbour View FC", "Kingston", 25);
            System.out.println("✓ FootballTeam created successfully:");
            System.out.println("  " + harbourView + "\n");
        } catch (IllegalArgumentException e) {
            System.err.println("✗ Team creation failed: " + e.getMessage());
        }

        // 3. Interactive Main Menu Prototype Shell
        while (running) {
            displayMainMenu();
            System.out.print("Enter option (1-5): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    System.out.println("\n[INFO] Team & Squad Management Submenu selected.");
                    break;
                case "2":
                    System.out.println("\n[INFO] Fixtures & Match Operations Submenu selected.");
                    break;
                case "3":
                    System.out.println("\n[INFO] Ticketing & Stadium Shop Submenu selected.");
                    break;
                case "4":
                    System.out.println("\n[INFO] Management Reports Submenu selected.");
                    break;
                case "5":
                    System.out.println("\nExiting KPFL Management System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[ERROR] Invalid option selected. Please choose between 1 and 5.");
            }
            System.out.println();
        }

        scanner.close();
    }

    /**
     * Displays the main system menu options as specified in the Week 1 requirements.
     */
    private static void displayMainMenu() {
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
}