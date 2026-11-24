package ui;

import exception.DataFileException;
import exception.KPFLException;
import model.Coach;
import model.FootballLeague;
import model.FootballTeam;
import model.Fixture;
import model.LeagueCommissioner;
import model.MatchOfficial;
import model.Player;
import model.Stadium;
import model.StandingsEntry;
import model.TeamStatus;
import service.ReportService;
import service.StandingsService;
import storage.LeagueFileStorage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/*
  Console menu for the KPFL system following the Console Menu Design
  in the Week 1 requirements document (section 8).
  Week 3 options (teams, squads, stadiums, officials, fixtures, results, standings) call
  the FootballLeague. Every action is wrapped in try/catch so a broken business rule
  shows the exception's message instead of crashing. Ticketing, shop and the shop
  report are still placeholders (Week 4).
  In every menu the last option goes back (or saves and exits on the main menu).
 */
public class Menu {

    private final Scanner scanner = new Scanner(System.in);
    private final FootballLeague league;
    private final ReportService reportService;
    private final LeagueFileStorage storage;   // null means saving is not available

    public Menu(FootballLeague league) {
        this(league, null);
    }

    public Menu(FootballLeague league, LeagueFileStorage storage) {
        this.league = league;
        this.reportService = new ReportService(league);
        this.storage = storage;
    }

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
                    // If saving fails, the user can choose to stay and try again instead of losing the data.
                    if (saveData() || confirmExitWithoutSaving()) {
                        System.out.println("Exiting KPFL Management System. Goodbye!");
                        running = false;
                    }
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
            System.out.println("4. Search Players (by Name, ID, or Position)");
            System.out.println("5. Add Player / Coach to Squad");
            System.out.println("6. Remove Player / Coach from Squad");
            System.out.println("7. Return to Main Menu");
            int choice = readInt("Enter option (1-7): ", 1, 7);
            switch (choice) {
                case 1 -> registerTeam();
                case 2 -> changeTeamStatus();
                case 3 -> searchTeams();
                case 4 -> searchPlayers();
                case 5 -> addToSquad();
                case 6 -> removeFromSquad();
                case 7 -> back = true;
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
            System.out.println("6. Search Stadiums (by Name, ID, or Location)");
            System.out.println("7. Search Fixtures (by Team, Date, or Status)");
            System.out.println("8. Return to Main Menu");
            int choice = readInt("Enter option (1-8): ", 1, 8);
            switch (choice) {
                case 1 -> registerStadium();
                case 2 -> registerOfficial();
                case 3 -> createFixture();
                case 4 -> assignOfficial();
                case 5 -> recordResult();
                case 6 -> searchStadiums();
                case 7 -> searchFixtures();
                case 8 -> back = true;
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
            System.out.println("2. View Team & Roster Directory Report");
            System.out.println("3. View Stadiums, Occupancy & Schedule Report");
            System.out.println("4. View Shop Sales & Low Stock Report");
            System.out.println("5. Return to Main Menu");
            int choice = readInt("Enter option (1-5): ", 1, 5);
            switch (choice) {
                case 1 -> showStandings();
                case 2 -> showTeamDirectoryReport();
                case 3 -> showStadiumReport();
                case 4 -> notImplemented("4.4 Shop Sales & Low Stock Report");
                case 5 -> back = true;
            }
        }
    }

    // ---------- 1. Team & Squad Management ----------

    private void registerTeam() {
        try {
            String regNo = ask("Registration number: ");
            String name = ask("Team name: ");
            String parish = ask("Parish: ");
            int squadSize = askInt("Maximum squad size: ");
            league.registerTeam(new FootballTeam(regNo, name, parish, squadSize));
            System.out.println("Team registered with status PENDING. A commissioner must approve it before it can play.");
        } catch (KPFLException e) {
            System.out.println("Cannot register team: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Cannot register team: squad size must be a whole number.");
        }
    }

    private void changeTeamStatus() {
        try {
            listTeams();
            FootballTeam team = league.findTeam(ask("Team registration number: "));
            System.out.println(team.getName() + " is currently " + team.getStatus() + ".");
            System.out.println("1. Approve (REGISTERED)   2. Suspend   3. Cancel");
            int choice = readInt("Enter option (1-3): ", 1, 3);
            LeagueCommissioner commissioner = league.getCommissioner();
            if (choice == 3) {
                return;
            }
            if (commissioner == null) {
                System.out.println("There is no league commissioner to approve or suspend teams.");
                return;
            }
            if (choice == 1) {
                commissioner.approveTeam(team);
            } else {
                commissioner.suspendTeam(team);
            }
            System.out.println(team.getName() + " is now " + team.getStatus() + ".");
        } catch (KPFLException e) {
            System.out.println("Cannot change status: " + e.getMessage());
        }
    }

    private void searchTeams() {
        try {
            String text = ask("Search by name, registration number or parish: ");
            List<FootballTeam> found = reportService.searchTeams(text);
            for (FootballTeam team : found) {
                System.out.println();
                System.out.println(team);
                for (Player player : team.getPlayers()) {
                    System.out.println("   " + player.describe());
                }
                for (Coach coach : team.getCoaches()) {
                    System.out.println("   " + coach.describe());
                }
            }
        } catch (KPFLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void searchPlayers() {
        try {
            String text = ask("Search player by name, ID, or position: ");
            List<Player> found = reportService.searchPlayers(text);
            System.out.println();
            for (Player player : found) {
                System.out.println("   " + player.describe());
            }
        } catch (KPFLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addToSquad() {
        try {
            listTeams();
            FootballTeam team = league.findTeam(ask("Team registration number: "));
            System.out.println("1. Player   2. Coach   3. Cancel");
            int choice = readInt("Enter option (1-3): ", 1, 3);
            if (choice == 3) {
                return;
            }
            String id = ask("ID: ");
            String name = ask("Name: ");
            int age = askInt("Age: ");
            String gender = ask("Gender: ");
            if (choice == 1) {
                String position = ask("Position: ");
                int jersey = askInt("Jersey number (1-99): ");
                String status = ask("Playing status (e.g. Active): ");
                team.addPlayer(new Player(id, name, age, gender, position, jersey, status));
                System.out.println("Player added to " + team.getName() + ".");
            } else {
                String qualification = ask("Qualification: ");
                String role = ask("Coaching role: ");
                team.addCoach(new Coach(id, name, age, gender, qualification, role));
                System.out.println("Coach added to " + team.getName() + ".");
            }
        } catch (KPFLException e) {
            System.out.println("Cannot add to squad: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Cannot add to squad: age and jersey number must be whole numbers.");
        }
    }

    private void removeFromSquad() {
        try {
            listTeams();
            FootballTeam team = league.findTeam(ask("Team registration number: "));
            System.out.println("1. Player   2. Coach   3. Cancel");
            int choice = readInt("Enter option (1-3): ", 1, 3);
            if (choice == 3) {
                return;
            }
            String id = ask("ID to remove: ");
            if (choice == 1) {
                team.removePlayer(id);
            } else {
                team.removeCoach(id);
            }
            System.out.println("Removed " + id.trim() + " from " + team.getName() + ".");
        } catch (KPFLException e) {
            System.out.println("Cannot remove: " + e.getMessage());
        }
    }

    // ---------- 2. Fixtures & Match Operations ----------

    private void registerStadium() {
        try {
            String id = ask("Stadium ID: ");
            String name = ask("Name: ");
            String location = ask("Location: ");
            int capacity = askInt("Seating capacity: ");
            String condition = ask("Field condition: ");
            BigDecimal price = new BigDecimal(ask("Ticket price (e.g. 1500.00): "));
            league.addStadium(new Stadium(id, name, location, capacity, condition, price));
            System.out.println("Stadium registered.");
        } catch (KPFLException e) {
            System.out.println("Cannot register stadium: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Cannot register stadium: capacity must be a whole number and price a number.");
        }
    }

    private void registerOfficial() {
        try {
            String id = ask("Official ID: ");
            String name = ask("Name: ");
            int age = askInt("Age: ");
            String gender = ask("Gender: ");
            String role = ask("Official role (e.g. Referee): ");
            String rank = ask("Rank (e.g. Senior): ");
            league.addOfficial(new MatchOfficial(id, name, age, gender, role, rank));
            System.out.println("Match official registered.");
        } catch (KPFLException e) {
            System.out.println("Cannot register official: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Cannot register official: age must be a whole number.");
        }
    }

    private void createFixture() {
        try {
            listTeams();
            listStadiums();
            String id = ask("New fixture ID: ");
            String home = ask("Home team registration number: ");
            String away = ask("Away team registration number: ");
            String stadium = ask("Stadium ID: ");
            LocalDate date = LocalDate.parse(ask("Date (yyyy-mm-dd): "));
            LocalTime time = LocalTime.parse(ask("Time (hh:mm, 24-hour): "));
            Fixture fixture = league.scheduleFixture(id, home, away, stadium, date, time);
            System.out.println("Fixture created: " + fixture);
        } catch (KPFLException e) {
            System.out.println("Cannot create fixture: " + e.getMessage());
        } catch (DateTimeParseException e) {
            System.out.println("Cannot create fixture: use a date like 2026-12-05 and a time like 15:00.");
        }
    }

    private void assignOfficial() {
        try {
            listFixtures();
            listOfficials();
            String fixtureId = ask("Fixture ID: ");
            String officialId = ask("Official ID: ");
            league.assignOfficial(fixtureId, officialId);
            System.out.println("Official assigned.");
        } catch (KPFLException e) {
            System.out.println("Cannot assign official: " + e.getMessage());
        }
    }

    private void recordResult() {
        try {
            listFixtures();
            String fixtureId = ask("Fixture ID: ");
            Fixture fixture = league.findFixture(fixtureId);
            int home = askInt(fixture.getHomeTeam().getName() + " goals: ");
            int away = askInt(fixture.getAwayTeam().getName() + " goals: ");
            league.recordResult(fixtureId, home, away);
            System.out.println("Result recorded: " + league.findFixture(fixtureId));
            System.out.println();
            System.out.print(StandingsService.format(league.getStandings()));
        } catch (KPFLException e) {
            System.out.println("Cannot record result: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Cannot record result: goals must be whole numbers.");
        }
    }

    private void searchStadiums() {
        try {
            String text = ask("Search stadium by name, ID, or location: ");
            List<Stadium> found = reportService.searchStadiums(text);
            System.out.println();
            for (Stadium stadium : found) {
                System.out.println("  " + stadium);
            }
        } catch (KPFLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void searchFixtures() {
        try {
            String text = ask("Search fixture by team name, date, or status: ");
            List<Fixture> found = reportService.searchFixtures(text);
            System.out.println();
            for (Fixture fixture : found) {
                System.out.println("  " + fixture);
            }
        } catch (KPFLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ---------- 4. Reports ----------

    private void showStandings() {
        try {
            System.out.println();
            System.out.print(reportService.generateLeagueStandingsReport());
        } catch (KPFLException e) {
            System.out.println("Cannot show standings: " + e.getMessage());
        }
    }

    private void showTeamDirectoryReport() {
        System.out.println();
        System.out.print(reportService.generateTeamDirectoryReport());
    }

    private void showStadiumReport() {
        System.out.println();
        System.out.print(reportService.generateStadiumReport());
    }

    // ---------- Listing helpers ----------

    private void listTeams() {
        System.out.println("Teams:");
        for (FootballTeam team : league.getTeams()) {
            System.out.println("  " + team.getRegistrationNumber() + "  " + team.getName()
                    + " [" + team.getStatus() + "]");
        }
    }

    private void listStadiums() {
        System.out.println("Stadiums:");
        for (Stadium stadium : league.getStadiums()) {
            System.out.println("  " + stadium.getStadiumId() + "  " + stadium.getName()
                    + " (" + stadium.getSeatingCapacity() + " seats)");
        }
    }

    private void listOfficials() {
        System.out.println("Match officials:");
        for (MatchOfficial official : league.getOfficials()) {
            System.out.println("  " + official.getId() + "  " + official.getName()
                    + (official.isAvailable() ? "" : " [unavailable]"));
        }
    }

    private void listFixtures() {
        System.out.println("Fixtures:");
        if (league.getFixtures().isEmpty()) {
            System.out.println("  (none yet)");
        }
        for (Fixture fixture : league.getFixtures()) {
            System.out.println("  " + fixture);
        }
    }

    // ---------- Input helpers ----------

    // Asks for a line of text. At end of input it returns an empty string.
    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    // Asks for a whole number. Throws NumberFormatException if it is not one, so each action can report it.
    private int askInt(String prompt) {
        return Integer.parseInt(ask(prompt));
    }

    // ---------- Helpers ----------

    // Placeholder for features not built yet.
    private void notImplemented(String feature) {
        System.out.println("[" + feature + "] is not implemented yet.");
    }

    // Saves the league to the data folder. Returns true if the data was saved.
    private boolean saveData() {
        if (storage == null) {
            System.out.println("Saving is not available in this session.");
            return false;
        }
        try {
            storage.save(league);
            System.out.println("Data saved to " + storage.getDirectory() + ".");
            return true;
        } catch (DataFileException e) {
            System.out.println("Could not save data: " + e.getMessage());
            return false;
        }
    }

    private boolean confirmExitWithoutSaving() {
        System.out.print("The data was not saved. Exit anyway and lose this session's changes? (y/n): ");
        String answer = scanner.hasNextLine() ? scanner.nextLine().trim() : "y";
        return answer.equalsIgnoreCase("y");
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