package com.saturnrun.model;

import java.util.List;

/** One unique puzzle lock in a Vesperion-to-Earth run. */
public class Station {
    public final int stationNumber;
    public final StationType type;
    public final String difficulty;
    public final String flavorText;

    public int n, r;
    public int items, containers;
    public int coeffA, coeffB, constantC;
    public long base0, base1;
    public int targetTerm;

    public long correctAnswer;
    public List<String> derivationSteps;
    public List<Long> options;
    public int correctOptionIndex;

    public Station(int stationNumber, StationType type, String difficulty, String flavorText) {
        this.stationNumber = stationNumber;
        this.type = type;
        this.difficulty = difficulty;
        this.flavorText = flavorText;
    }
}
