package com.saturnrun.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Solves a linear recurrence relation of the form:
 *   fuel(n) = a * fuel(n-1) + b * fuel(n-2) + c
 * (supports 1-term or 2-term recurrences by setting b = 0 for a 1-term one)
 *
 * given base case(s), up to a target term N, showing every intermediate
 * step so the game can display the derivation on screen.
 */
public class RecurrenceSolver {

    public static class Result {
        public final long answer;       // fuel(targetTerm)
        public final List<String> steps;
        public final List<Long> sequence; // fuel(0), fuel(1), ... fuel(targetTerm)

        public Result(long answer, List<String> steps, List<Long> sequence) {
            this.answer = answer;
            this.steps = steps;
            this.sequence = sequence;
        }
    }

    /**
     * @param a           coefficient of fuel(n-1)
     * @param b           coefficient of fuel(n-2) (use 0 for a single-term recurrence)
     * @param c           constant term added each step
     * @param base0       fuel(0)
     * @param base1       fuel(1) (ignored if b == 0, i.e. single-term recurrence)
     * @param targetTerm  which term to solve for (n)
     */
    public static Result solve(int a, int b, int c, long base0, long base1, int targetTerm) {
        List<String> steps = new ArrayList<>();
        List<Long> sequence = new ArrayList<>();

        boolean twoTerm = (b != 0);

        if (twoTerm) {
            steps.add("Recurrence: fuel(n) = " + a + "*fuel(n-1) + " + b + "*fuel(n-2) + " + c);
            steps.add("Base cases: fuel(0) = " + base0 + ", fuel(1) = " + base1);
        } else {
            steps.add("Recurrence: fuel(n) = " + a + "*fuel(n-1) + " + c);
            steps.add("Base case: fuel(0) = " + base0);
        }

        sequence.add(base0);
        if (twoTerm) {
            sequence.add(base1);
        }

        int startFrom = twoTerm ? 2 : 1;
        for (int n = startFrom; n <= targetTerm; n++) {
            long prev1 = sequence.get(n - 1);
            long value;
            if (twoTerm) {
                long prev2 = sequence.get(n - 2);
                value = (long) a * prev1 + (long) b * prev2 + c;
                steps.add("fuel(" + n + ") = " + a + "*fuel(" + (n - 1) + ") + " + b + "*fuel(" + (n - 2) + ") + " + c
                        + " = " + a + "*" + prev1 + " + " + b + "*" + prev2 + " + " + c
                        + " = " + value);
            } else {
                value = (long) a * prev1 + c;
                steps.add("fuel(" + n + ") = " + a + "*fuel(" + (n - 1) + ") + " + c
                        + " = " + a + "*" + prev1 + " + " + c
                        + " = " + value);
            }
            sequence.add(value);
        }

        long answer = sequence.get(targetTerm);
        steps.add("Answer: fuel(" + targetTerm + ") = " + answer);

        return new Result(answer, steps, sequence);
    }
}
