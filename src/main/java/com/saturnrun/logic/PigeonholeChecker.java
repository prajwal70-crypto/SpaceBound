package com.saturnrun.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the Pigeonhole Principle: if you place N items ("pigeons",
 * e.g. oxygen canisters) into M containers ("holes", e.g. compartments),
 * and N > M, then at least one container must hold more than one item.
 *
 * More generally: at least one hole must contain at least ceil(N / M) items.
 */
public class PigeonholeChecker {

    public static class Result {
        public final boolean overflowGuaranteed;
        public final int minItemsInSomeHole; // guaranteed minimum in the fullest hole
        public final List<String> steps;

        public Result(boolean overflowGuaranteed, int minItemsInSomeHole, List<String> steps) {
            this.overflowGuaranteed = overflowGuaranteed;
            this.minItemsInSomeHole = minItemsInSomeHole;
            this.steps = steps;
        }
    }

    /**
     * @param items      number of items to distribute (e.g. oxygen canisters)
     * @param containers number of containers/slots available (e.g. compartments)
     */
    public static Result check(int items, int containers) {
        List<String> steps = new ArrayList<>();

        if (containers <= 0) {
            steps.add("Invalid input: number of containers must be positive.");
            return new Result(false, 0, steps);
        }

        // Pigeonhole principle: minimum guaranteed items in the fullest container
        // is ceil(items / containers).
        int minInFullest = (int) Math.ceil((double) items / containers);
        boolean overflow = items > containers;

        steps.add("Pigeonhole Principle: placing N items into M containers guarantees");
        steps.add("that some container holds at least ceil(N / M) items.");
        steps.add("Here N (items) = " + items + ", M (containers) = " + containers);
        steps.add("ceil(" + items + " / " + containers + ") = " + minInFullest);

        if (overflow) {
            steps.add("Since N (" + items + ") > M (" + containers + "), at least one container");
            steps.add("is GUARANTEED to hold " + minInFullest + " or more items. Overflow confirmed.");
        } else {
            steps.add("Since N (" + items + ") <= M (" + containers + "), it is POSSIBLE for every");
            steps.add("container to hold at most one item — overflow is NOT guaranteed.");
        }

        return new Result(overflow, minInFullest, steps);
    }
}
