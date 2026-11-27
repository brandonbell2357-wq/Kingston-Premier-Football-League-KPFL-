package service;

import exception.KPFLException;
import model.Coach;
import model.FootballLeague;
import model.FoodItem;
import model.FootballTeam;
import model.LeagueCommissioner;
import model.MatchOfficial;
import model.Merchandise;
import model.Player;
import model.Spectator;
import model.StadiumShop;
import model.Stadium;
import model.TeamStatus;

import java.math.BigDecimal;

/*
  Demonstration data so the menu has something to work with before saving and loading
  exist (Week 4). Includes a PENDING and a SUSPENDED team so the BR1 error can be shown.
  Week 4: also spectators and shop products. Two products start at their low-stock level,
  so the low-stock report has something to show.
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

        league.addSpectator(new Spectator("SP01", "Alicia Brown", 30, "Female"));
        league.addSpectator(new Spectator("SP02", "Devon Campbell", 22, "Male"));
        league.addSpectator(new Spectator("SP03", "Shanice Williams", 41, "Female"));

        StadiumShop shop = league.getShop();
        shop.addProduct(new Merchandise("M01", "Harbour View Home Jersey", new BigDecimal("4500.00"), 25, "L", "Harbour View FC"));
        shop.addProduct(new Merchandise("M02", "KPFL Flag", new BigDecimal("1200.00"), 5, "One size", "KPFL"));
        shop.addProduct(new Merchandise("M03", "Waterhouse Scarf", new BigDecimal("1800.00"), 12, "One size", "Waterhouse FC"));
        shop.addProduct(new FoodItem("F01", "Beef Patty", new BigDecimal("350.00"), 80, false));
        shop.addProduct(new FoodItem("F02", "Jerk Chicken Box", new BigDecimal("1500.00"), 20, false));
        shop.addProduct(new FoodItem("F03", "Bottled Water", new BigDecimal("150.00"), 100, true));
        shop.addProduct(new FoodItem("F04", "Fruit Juice", new BigDecimal("250.00"), 60, true));
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
