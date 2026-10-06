package com.saturnrun.game;

import com.saturnrun.model.Station;
import java.util.List;

/** Controls the Vesperion rescue rules. Questions can be browsed freely; only correct answers add fuel and move the rocket. */
public class GameController {
    public static final int TOTAL_STATIONS = 12;
    public static final int CORRECT_NEEDED_TO_ESCAPE = 8;
    public static final int MAX_WRONG_ANSWERS = 4;
    // Kept for compatibility with the older project tests.
    public static final int STARTING_LIVES = MAX_WRONG_ANSWERS;

    private final List<Station> stations;
    private int currentStationIndex = 0;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private int attempts = 0;
    private final boolean[] solved = new boolean[TOTAL_STATIONS];
    private boolean gameOver = false;
    private boolean gameWon = false;

    public GameController(StationGenerator generator) {
        stations = generator.generateAllStations();
    }

    public Station getCurrentStation() {
        if (currentStationIndex >= stations.size()) return null;
        return stations.get(currentStationIndex);
    }

    public boolean submitAnswer(long playerAnswer) {
        if (gameOver) return false;
        Station current = getCurrentStation();
        if (current == null) return false;

        int idx = currentStationIndex;
        if (solved[idx]) return true;

        attempts++;
        boolean correct = playerAnswer == current.correctAnswer;
        if (correct) {
            solved[idx] = true;
            correctAnswers++;
            if (correctAnswers >= CORRECT_NEEDED_TO_ESCAPE) {
                gameWon = true;
                gameOver = true;
            }
        } else {
            wrongAnswers++;
            if (wrongAnswers >= MAX_WRONG_ANSWERS) {
                gameWon = false;
                gameOver = true;
            }
        }
        return correct;
    }

    /** Move to the next question without changing fuel or wrong-answer count. */
    public boolean nextQuestion() {
        if (currentStationIndex >= stations.size() - 1) return false;
        currentStationIndex++;
        return true;
    }

    /** Move to the previous question without changing fuel or wrong-answer count. */
    public boolean previousQuestion() {
        if (currentStationIndex <= 0) return false;
        currentStationIndex--;
        return true;
    }

    public boolean isCurrentQuestionSolved() {
        return solved[currentStationIndex];
    }

    public int getCorrectAnswers() { return correctAnswers; }
    public int getWrongAnswers() { return wrongAnswers; }
    public int getAttempts() { return attempts; }
    public int getFuelCollected() { return correctAnswers; }
    public int getFuelNeeded() { return CORRECT_NEEDED_TO_ESCAPE; }
    public int getLivesRemaining() { return MAX_WRONG_ANSWERS - wrongAnswers; }
    public int getStationsAttempted() { return currentStationIndex + 1; }
    public int getCurrentStationIndex() { return currentStationIndex; }
    public int getTotalStations() { return TOTAL_STATIONS; }
    public boolean isGameOver() { return gameOver; }
    public boolean isGameWon() { return gameWon; }
}
