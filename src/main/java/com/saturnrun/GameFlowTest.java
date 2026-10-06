package com.saturnrun;

import com.saturnrun.game.GameController;
import com.saturnrun.game.StationGenerator;
import com.saturnrun.model.Station;

/**
 * Console-only simulation of a full playthrough (Step 4/5 of the build plan).
 * Verifies the station generation + game controller logic before any UI.
 */
public class GameFlowTest {
    public static void main(String[] args) {
        StationGenerator generator = new StationGenerator(42L); // fixed seed for repeatable test
        GameController game = new GameController(generator);

        System.out.println("=== VESPERION: THE RETURN — console simulation ===");
        System.out.println("Total stations: " + game.getTotalStations()
                + " | Fuel needed to launch: " + game.getFuelNeeded()
                + " | Starting lives: " + GameController.STARTING_LIVES);

        int stationCount = 0;
        boolean wrongInjected = false;
        while (!game.isGameOver()) {
            Station s = game.getCurrentStation();
            if (s == null) break;
            stationCount++;

            System.out.println("\n--- " + s.flavorText + " ---");
            System.out.println("(Type: " + s.type + ")");

            // Simulate: answer correctly on odd stations, wrong on station 5, to test both paths
            long simulatedAnswer;
            if (s.stationNumber == 5 && !wrongInjected) {
                simulatedAnswer = s.correctAnswer + 999; // force one wrong answer
                wrongInjected = true;
            } else {
                simulatedAnswer = s.correctAnswer; // force correct
            }

            boolean correct = game.submitAnswer(simulatedAnswer);
            if (correct) {
                System.out.println("Player answered " + simulatedAnswer + " -> CORRECT. Fuel collected!");
            } else {
                System.out.println("Player answered " + simulatedAnswer + " -> WRONG.");
                System.out.println("Derivation shown to player:");
                s.derivationSteps.forEach(step -> System.out.println("   " + step));
            }
            System.out.println("Fuel: " + game.getFuelCollected() + "/" + game.getFuelNeeded()
                    + " | Lives: " + game.getLivesRemaining());
            // The UI now lets the player browse freely with Previous/Next, so
            // the console simulation explicitly moves to the next question.
            if (!game.isGameOver()) game.nextQuestion();
        }

        System.out.println("\n=== GAME OVER ===");
        System.out.println("Stations attempted: " + stationCount);
        System.out.println("Result: " + (game.isGameWon() ? "WON — reached Earth!" : "LOST — stranded on Vesperion."));
    }
}
