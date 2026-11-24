package ui;

import exception.DataFileException;
import exception.KPFLException;
import model.FootballLeague;
import service.SampleData;
import storage.LeagueFileStorage;

import java.nio.file.Path;
import java.nio.file.Paths;

// Starts the KPFL console program. Saved data is loaded if it exists; otherwise demonstration data is used.
public class Main {

    private static final Path SAVE_FOLDER = Paths.get("data", "saved");

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  KINGSTON PREMIER FOOTBALL LEAGUE (KPFL) SYSTEM  ");
        System.out.println("=================================================");

        LeagueFileStorage storage = new LeagueFileStorage(SAVE_FOLDER);
        try {
            FootballLeague league = startingLeague(storage);
            new Menu(league, storage).run();
        } catch (KPFLException e) {
            System.out.println("The program could not start: " + e.getMessage());
        }
    }

    private static FootballLeague startingLeague(LeagueFileStorage storage) throws KPFLException {
        if (storage.hasSavedData()) {
            try {
                FootballLeague league = storage.load();
                System.out.println("Loaded saved data from " + storage.getDirectory() + ".");
                return league;
            } catch (DataFileException e) {
                System.out.println("Could not load the saved data: " + e.getMessage());
                System.out.println("Starting with demonstration data instead. Saving will replace the files in "
                        + storage.getDirectory() + ", so copy them first if you want to keep them.");
                return SampleData.createLeague();
            }
        }
        System.out.println("No saved data found. Starting with demonstration data.");
        return SampleData.createLeague();
    }
}
