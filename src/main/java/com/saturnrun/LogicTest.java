package com.saturnrun;

import com.saturnrun.logic.PermCombSolver;
import com.saturnrun.logic.PigeonholeChecker;
import com.saturnrun.logic.RecurrenceSolver;

/**
 * Console-only test harness — Step 3 of the build plan.
 * Verifies the math logic is correct before any UI is built.
 * Run this and manually check the numbers against hand-calculated values.
 */
public class LogicTest {
    public static void main(String[] args) {
        System.out.println("===== PERMUTATION TEST: 5P3 (expected 60) =====");
        PermCombSolver.Result perm = PermCombSolver.permutation(5, 3);
        perm.steps.forEach(System.out::println);
        System.out.println("Answer = " + perm.answer);

        System.out.println("\n===== COMBINATION TEST: 6C2 (expected 15) =====");
        PermCombSolver.Result comb = PermCombSolver.combination(6, 2);
        comb.steps.forEach(System.out::println);
        System.out.println("Answer = " + comb.answer);

        System.out.println("\n===== PIGEONHOLE TEST: 10 items, 3 containers (expect overflow, min 4) =====");
        PigeonholeChecker.Result pigeon = PigeonholeChecker.check(10, 3);
        pigeon.steps.forEach(System.out::println);
        System.out.println("Overflow guaranteed = " + pigeon.overflowGuaranteed
                + ", min in fullest = " + pigeon.minItemsInSomeHole);

        System.out.println("\n===== PIGEONHOLE TEST 2: 3 items, 5 containers (expect NO overflow) =====");
        PigeonholeChecker.Result pigeon2 = PigeonholeChecker.check(3, 5);
        pigeon2.steps.forEach(System.out::println);
        System.out.println("Overflow guaranteed = " + pigeon2.overflowGuaranteed);

        System.out.println("\n===== RECURRENCE TEST: Fibonacci-style fuel(n) = fuel(n-1) + fuel(n-2), base 1,1, term 6 =====");
        // fuel: 1, 1, 2, 3, 5, 8, 13 -> fuel(6) should be 13
        RecurrenceSolver.Result rec = RecurrenceSolver.solve(1, 1, 0, 1, 1, 6);
        rec.steps.forEach(System.out::println);
        System.out.println("Answer = " + rec.answer + " (expected 13)");

        System.out.println("\n===== RECURRENCE TEST 2: single-term fuel(n) = 2*fuel(n-1) + 3, base 5, term 4 =====");
        // fuel: 5, 13, 29, 61, 125 -> fuel(4) should be 125
        RecurrenceSolver.Result rec2 = RecurrenceSolver.solve(2, 0, 3, 5, 0, 4);
        rec2.steps.forEach(System.out::println);
        System.out.println("Answer = " + rec2.answer + " (expected 125)");
    }
}
