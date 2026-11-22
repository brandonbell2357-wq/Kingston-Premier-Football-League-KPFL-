package service;

import exception.KPFLException;
import model.Coach;
import model.FootballLeague;
import model.FootballTeam;
import model.LeagueCommissioner;
import model.MatchOfficial;
import model.Player;
import model.Stadium;
import model.TeamStatus;

import java.math.BigDecimal;

/*
  Demonstration data so the menu has something to work with before saving and loading
  exist (Week 4). Includes a PENDING and a SUSPENDED team so the BR1 error can be shown.
 */
public final class SampleData {

    private SampleData() {
        // utility class: nothing to create
    }

    public static FootballLeague createLeague() throws KPFLException {
        FootballLeague league = new FootballLeague("Kingston Premier Football League");
        league.setCommissioner(new LeagueCommissioner("C01", "Marcus Reid", 52, "Male"));

        addTeam(league, "T01", "Harbour View FC", "Kingston", TeamStatus.REGISTERED);
        addTeam(league, "T02", "Portmore United", "St Catherine", TeamStatus.REGISTERED);
        addTeam(league, "T03", "Waterhouse FC", "Kingston", TeamStatus.REGISTERED);
        addTeam(league, "T04", "Mount Pleasant FA", "St Ann", TeamStatus.REGISTERED);
        addTeam(league, "T05", "Cavalier SC", "Kingston", TeamStatus.PENDING);
        addTeam(league, "T06", "Arnett Gardens", "Kingston", TeamStatus.SUSPENDED);

        league.addStadium(new Stadium("S01", "National Stadium", "Kingston", 35000, "Excellent",
                new BigDecimal("1500.00")));
        league.addStadium(new Stadium("S02", "Harbour View Ground", "Kingston", 7000, "Good",
                new BigDecimal("1000.00")));

        league.addOfficial(new MatchOfficial("O01", "Daniel Brown", 38, "Male", "Referee", "Senior"));
        league.addOfficial(new MatchOfficial("O02", "Karen Clarke", 34, "Female", "Referee", "Senior"));
        league.addOfficial(new MatchOfficial("O03", "Paul Grant", 41, "Male", "Assistant Referee", "Junior"));
        return league;
    }

    private static void addTeam(FootballLeague league, String regNo, String name, String parish,
                                TeamStatus status) throws KPFLException {
        FootballTeam team = new FootballTeam(regNo, name, parish, 25);
        team.addPlayer(new Player(regNo + "-P1", name + " Striker", 24, "Male", "Forward", 9, "Active"));
        team.addPlayer(new Player(regNo + "-P2", name + " Keeper", 28, "Male", "Goalkeeper", 1, "Active"));
        team.addCoach(new Coach(regNo + "-C1", name + " Coach", 45, "Male", "Level 2", "Head Coach"));
        team.setStatus(status);
        league.registerTeam(team);
    }
}
