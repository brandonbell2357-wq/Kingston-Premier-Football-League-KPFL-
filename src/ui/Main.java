package ui;

import exception.KPFLException;
import service.SampleData;

// Starts the KPFL console program with demonstration data.
public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  KINGSTON PREMIER FOOTBALL LEAGUE (KPFL) SYSTEM  ");
        System.out.println("=================================================");
        try {
            new Menu(SampleData.createLeague()).run();
        } catch (KPFLException e) {
            System.out.println("The program could not start: " + e.getMessage());
        }
    }
}

 