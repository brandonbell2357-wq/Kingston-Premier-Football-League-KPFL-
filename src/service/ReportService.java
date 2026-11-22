package service;

import exception.InvalidDataException;
import exception.NotFoundException;
import model.Coach;
import model.Fixture;
import model.FootballLeague;
import model.FootballTeam;
import model.MatchOfficial;
import model.Player;
import model.Stadium;
import model.StandingsEntry;

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
}