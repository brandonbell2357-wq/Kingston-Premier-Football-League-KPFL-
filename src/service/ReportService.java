package service;

import exception.InvalidDataException;
import exception.NotFoundException;
import model.Coach;
import model.Fixture;
import model.FootballLeague;
import model.FootballTeam;
import model.MatchOfficial;
import model.Player;
import model.Product;
import model.Stadium;
import model.StadiumShop;
import model.StandingsEntry;
import model.TicketCategory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ReportService {

    private final FootballLeague league;

    public ReportService(FootballLeague league) {
        this.league = league;
    }

    // =========================================================================
    // SEARCH METHODS
    // =========================================================================

    public List<FootballTeam> searchTeams(String keyword) throws NotFoundException {
        List<FootballTeam> matches = league.searchTeams(keyword);
        if (matches.isEmpty()) {
            throw new NotFoundException("No teams found matching: " + keyword);
        }
        return matches;
    }

    public List<Player> searchPlayers(String keyword) throws NotFoundException {
        String query = keyword.trim().toLowerCase();
        List<Player> matches = new ArrayList<>();

        for (FootballTeam team : league.getTeams()) {
            for (Player player : team.getPlayers()) {
                if (player.getId().toLowerCase().contains(query)
                        || player.getName().toLowerCase().contains(query)
                        || player.getPosition().toLowerCase().contains(query)) {
                    matches.add(player);
                }
            }
        }

        if (matches.isEmpty()) {
            throw new NotFoundException("No players found matching: " + keyword);
        }
        return matches;
    }

    public List<Stadium> searchStadiums(String keyword) throws NotFoundException {
        String query = keyword.trim().toLowerCase();
        List<Stadium> matches = new ArrayList<>();

        for (Stadium stadium : league.getStadiums()) {
            if (stadium.getStadiumId().toLowerCase().contains(query)
                    || stadium.getName().toLowerCase().contains(query)
                    || stadium.getLocation().toLowerCase().contains(query)) {
                matches.add(stadium);
            }
        }

        if (matches.isEmpty()) {
            throw new NotFoundException("No stadiums found matching: " + keyword);
        }
        return matches;
    }

    public List<Fixture> searchFixtures(String keyword) throws NotFoundException {
        String query = keyword.trim().toLowerCase();
        List<Fixture> matches = new ArrayList<>();

        for (Fixture fixture : league.getFixtures()) {
            String homeName = fixture.getHomeTeam().getName().toLowerCase();
            String awayName = fixture.getAwayTeam().getName().toLowerCase();
            String date = fixture.getDate().toString().toLowerCase();
            String status = fixture.getStatus().toString().toLowerCase();

            if (homeName.contains(query) || awayName.contains(query)
                    || date.contains(query) || status.contains(query)) {
                matches.add(fixture);
            }
        }

        if (matches.isEmpty()) {
            throw new NotFoundException("No fixtures found matching: " + keyword);
        }
        return matches;
    }

    // =========================================================================
    // MANAGEMENT REPORTS (3 REQUIRED REPORTS)
    // =========================================================================

    // Report 1: League Standings (reuses league.getStandings() and StandingsService.format())
    public String generateLeagueStandingsReport() throws InvalidDataException {
        String report = "=== KPFL LEAGUE STANDINGS ===\n";
        List<StandingsEntry> table = league.getStandings();

        if (table.isEmpty()) {
            return report + "No standings data available yet.\n";
        }

        return report + StandingsService.format(table);
    }

    // Report 2: Registered Teams & Roster Summary
    public String generateTeamDirectoryReport() {
        String report = "=== KPFL TEAM & ROSTER DIRECTORY ===\n";

        if (league.getTeams().isEmpty()) {
            return report + "No teams registered yet.\n";
        }

        for (FootballTeam team : league.getTeams()) {
            report += "Team: " + team.getName()
                    + " (" + team.getParish() + ")"
                    + " | Status: " + team.getStatus()
                    + " | Squad: " + team.getPlayers().size() + "/" + team.getSquadSize() + "\n";

            report += "  Coaches: ";
            if (team.getCoaches().isEmpty()) {
                report += "None";
            } else {
                for (Coach coach : team.getCoaches()) {
                    report += coach.getName() + " (" + coach.getCoachingRole() + ") ";
                }
            }
            report += "\n\n";
        }
        return report;
    }

    // Report 3: Stadiums, Occupancy & Match Schedule Summary
    public String generateStadiumReport() {
        String report = "=== KPFL STADIUM & SCHEDULE REPORT ===\n";

        if (league.getStadiums().isEmpty()) {
            return report + "No stadiums registered yet.\n";
        }

        for (Stadium stadium : league.getStadiums()) {
            int matchCount = 0;
            int totalTicketsSold = 0;

            for (Fixture fixture : league.getFixtures()) {
                if (fixture.getStadium().getStadiumId().equals(stadium.getStadiumId())) {
                    matchCount++;
                    totalTicketsSold += fixture.getOccupancy();
                }
            }

            report += "Stadium: " + stadium.getName()
                    + " (" + stadium.getLocation() + ")"
                    + " | Capacity: " + stadium.getSeatingCapacity()
                    + " | Condition: " + stadium.getFieldCondition()
                    + " | Matches: " + matchCount
                    + " | Tickets Sold: " + totalTicketsSold + "\n";
        }

        int availableOfficials = 0;
        for (MatchOfficial official : league.getOfficials()) {
            if (official.isAvailable()) {
                availableOfficials++;
            }
        }
        report += "\nAvailable Match Officials: " + availableOfficials
                + " of " + league.getOfficials().size() + "\n";

        return report;
    }

    // Report 4: Fixtures, Attendance & Ticket Revenue (Week 1 menu design, option 4.2)
    public String generateFixtureAttendanceReport() {
        StringBuilder report = new StringBuilder("=== KPFL FIXTURES, ATTENDANCE & TICKET REVENUE ===\n");
        if (league.getFixtures().isEmpty()) {
            return report.append("No fixtures scheduled yet.\n").toString();
        }
        int totalTickets = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO.setScale(2);
        for (Fixture fixture : league.getFixtures()) {
            int capacity = fixture.getStadium().getSeatingCapacity();
            int sold = fixture.getOccupancy();
            report.append(fixture.getFixtureId()).append(": ")
                    .append(fixture.getHomeTeam().getName()).append(" vs ").append(fixture.getAwayTeam().getName())
                    .append(" | ").append(fixture.getDate()).append(' ').append(fixture.getTime())
                    .append(" | ").append(fixture.getStadium().getName())
                    .append(" | ").append(fixture.getStatus())
                    .append(fixture.getResult() == null ? "" : " " + fixture.getResult()).append('\n');
            report.append("  Attendance: ").append(sold).append(" / ").append(capacity)
                    .append(" seats (").append(percent(sold, capacity)).append(")")
                    .append(" | Revenue: $").append(fixture.getTicketRevenue()).append('\n');
            report.append("  By category:");
            for (TicketCategory category : TicketCategory.values()) {
                report.append(' ').append(category).append(' ').append(fixture.countTickets(category));
            }
            report.append("\n\n");
            totalTickets += sold;
            totalRevenue = totalRevenue.add(fixture.getTicketRevenue());
        }
        report.append("Total tickets sold: ").append(totalTickets)
                .append(" | Total ticket revenue: $").append(totalRevenue).append('\n');
        return report.toString();
    }

    // Report 5: Shop Sales & Low Stock (Week 1 menu design, option 4.3)
    public String generateShopReport() {
        StadiumShop shop = league.getShop();
        StringBuilder report = new StringBuilder("=== KPFL SHOP SALES & LOW STOCK ===\n");
        report.append("Completed sales: ").append(shop.getSales().size())
                .append(" | Total sales: $").append(shop.getTotalSales())
                .append(" | GCT collected: $").append(shop.getTotalTaxCollected())
                .append(" | Sales before GCT: $").append(shop.getTotalSales().subtract(shop.getTotalTaxCollected()))
                .append('\n');
        if (shop.getProducts().isEmpty()) {
            return report.append("No products in the shop yet.\n").toString();
        }
        report.append("\nProducts (units sold, stock left):\n");
        for (Product product : shop.getProducts()) {
            report.append(String.format("  %-6s %-26s %-11s sold %4d | stock %4d%s%n", product.getProductId(),
                    product.getName(), product.getProductType(), shop.unitsSold(product),
                    product.getStockQuantity(), product.isLowStock() ? "  LOW" : ""));
        }
        List<Product> low = shop.getLowStockProducts();
        report.append("\nLow stock (reorder): ");
        if (low.isEmpty()) {
            report.append("none\n");
        } else {
            report.append('\n');
            for (Product product : low) {
                report.append("  ").append(product.getProductId()).append(' ').append(product.getName())
                        .append(": ").append(product.getStockQuantity()).append(" left (reorder level ")
                        .append(product.getLowStockThreshold()).append(")\n");
            }
        }
        return report.toString();
    }

    private static String percent(int part, int whole) {
        if (whole <= 0) {
            return "0.0%";
        }
        return BigDecimal.valueOf(part * 100L).divide(BigDecimal.valueOf(whole), 1, RoundingMode.HALF_UP) + "%";
    }
}
