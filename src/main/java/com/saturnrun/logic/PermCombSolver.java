package com.saturnrun.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Solves Permutation and Combination problems and produces a
 * step-by-step derivation (not just the final number), so the
 * game can show its reasoning on screen — this satisfies the
 * ISE-2 "step-by-step output" requirement.
 */
public class PermCombSolver {

    /** Result of a P&C calculation: final answer + human-readable steps. */
    public static class Result {
        public final long answer;
        public final List<String> steps;

        public Result(long answer, List<String> steps) {
            this.answer = answer;
            this.steps = steps;
        }
    }

    /** n! — factorial, used internally by both permutation and combination. */
    public static long factorial(int n) {
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }

    /**
     * nPr = n! / (n - r)!
     * "How many ways to ARRANGE r fuel modules out of n, where order matters."
     */
    public static Result permutation(int n, int r) {
        List<String> steps = new ArrayList<>();
        if (r > n || r < 0 || n < 0) {
            steps.add("Invalid input: r must be between 0 and n.");
            return new Result(-1, steps);
        }
        long nFact = factorial(n);
        long nMinusRFact = factorial(n - r);
        long answer = nFact / nMinusRFact;

        steps.add("Formula: nPr = n! / (n - r)!");
        steps.add("Here n = " + n + ", r = " + r);
        steps.add(n + "! = " + nFact);
        steps.add("(" + n + " - " + r + ")! = " + (n - r) + "! = " + nMinusRFact);
        steps.add("nPr = " + nFact + " / " + nMinusRFact + " = " + answer);
        steps.add("Meaning: there are " + answer + " valid ordered sequences for the ignition sequence.");

        return new Result(answer, steps);
    }

    /**
     * nCr = n! / (r! * (n - r)!)
     * "How many ways to CHOOSE r fuel components out of n, where order does not matter."
     */
    public static Result combination(int n, int r) {
        List<String> steps = new ArrayList<>();
        if (r > n || r < 0 || n < 0) {
            steps.add("Invalid input: r must be between 0 and n.");
            return new Result(-1, steps);
        }
        long nFact = factorial(n);
        long rFact = factorial(r);
        long nMinusRFact = factorial(n - r);
        long answer = nFact / (rFact * nMinusRFact);

        steps.add("Formula: nCr = n! / (r! * (n - r)!)");
        steps.add("Here n = " + n + ", r = " + r);
        steps.add(n + "! = " + nFact);
        steps.add(r + "! = " + rFact);
        steps.add("(" + n + " - " + r + ")! = " + (n - r) + "! = " + nMinusRFact);
        steps.add("nCr = " + nFact + " / (" + rFact + " * " + nMinusRFact + ") = "
                + nFact + " / " + (rFact * nMinusRFact) + " = " + answer);
        steps.add("Meaning: there are " + answer + " valid ways to select the fuel components, order doesn't matter.");

        return new Result(answer, steps);
    }
}
