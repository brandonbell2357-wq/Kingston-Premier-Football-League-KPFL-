
package ui;

import exception.InvalidDataException;
import model.FootballTeam;
import model.Stadium;

import java.math.BigDecimal;
import java.util.Scanner;

/**
 * Console entry point prototype for Week 2 core implementation.
 * Demonstrates model instantiation, string representations, and the menu interface shell.
 */
public class Main {

    public static void main(String[] args) {

        Menu menu = new Menu();
        menu.run();
        
    }

}