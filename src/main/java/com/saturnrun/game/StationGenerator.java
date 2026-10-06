package com.saturnrun.game;

import com.saturnrun.logic.PermCombSolver;
import com.saturnrun.logic.PigeonholeChecker;
import com.saturnrun.logic.RecurrenceSolver;
import com.saturnrun.model.Station;
import com.saturnrun.model.StationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Creates exactly 12 fresh locks: 2 permutations, 2 combinations, 4 pigeonholes, 4 recurrences. */
public class StationGenerator {
    private final Random random;
    private final Set<String> usedSignatures = new HashSet<>();

    public StationGenerator() { random = new Random(); }
    public StationGenerator(long seed) { random = new Random(seed); }

    public List<Station> generateAllStations() {
        List<Station> out = new ArrayList<>();
        int number = 1;

        // Fixed learning progression: Permutation -> Combination ->
        // Pigeonhole -> Recurrence. Within each topic, questions are ordered
        // from easier to harder. The project keeps its original 12-question
        // distribution: 2 permutation, 2 combination, 4 pigeonhole, 4 recurrence.

        // Permutation: easy -> medium.
        out.add(uniquePermutation(number++, "Easy", 4, 5));
        out.add(uniquePermutation(number++, "Medium", 6, 8));

        // Combination: easy -> medium.
        out.add(uniqueCombination(number++, "Easy", 5, 7));
        out.add(uniqueCombination(number++, "Medium", 8, 10));

        // Pigeonhole: easy -> easy -> medium -> hard.
        out.add(uniquePigeonhole(number++, "Easy", 3, 6));
        out.add(uniquePigeonhole(number++, "Easy", 4, 9));
        out.add(uniquePigeonhole(number++, "Medium", 5, 14));
        out.add(uniquePigeonhole(number++, "Hard", 7, 24));

        // Recurrence: easy -> easy -> medium -> hard.
        out.add(uniqueRecurrence(number++, "Easy", 3, 4, false));
        out.add(uniqueRecurrence(number++, "Easy", 4, 5, false));
        out.add(uniqueRecurrence(number++, "Medium", 5, 7, true));
        out.add(uniqueRecurrence(number++, "Hard", 6, 9, true));

        // Intentionally do NOT shuffle. The player should see the four
        // mathematics topics in the requested learning order.
        return out;
    }

    private Station uniquePermutation(int num, String difficulty, int minN, int maxN) {
        for (int tries = 0; tries < 100; tries++) {
            int n = minN + random.nextInt(maxN - minN + 1);
            int r;
            if ("Easy".equals(difficulty)) r = 2 + random.nextInt(Math.max(1, n - 2));
            else if ("Medium".equals(difficulty)) r = Math.max(3, n - 2);
            else r = Math.max(4, n - 1);
            String sig = "P:" + n + ":" + r;
            if (usedSignatures.add(sig)) return buildPermutation(num, difficulty, n, r);
        }
        return buildPermutation(num, difficulty, minN, Math.min(minN - 1, 2));
    }

    private Station uniqueCombination(int num, String difficulty, int minN, int maxN) {
        for (int tries = 0; tries < 100; tries++) {
            int n = minN + random.nextInt(maxN - minN + 1);
            int r;
            if ("Easy".equals(difficulty)) r = 2 + random.nextInt(Math.max(1, n / 2 - 1));
            else if ("Medium".equals(difficulty)) r = Math.max(3, n / 2);
            else r = Math.max(4, n - 3);
            r = Math.min(r, n - 1);
            String sig = "C:" + n + ":" + r;
            if (usedSignatures.add(sig)) return buildCombination(num, difficulty, n, r);
        }
        return buildCombination(num, difficulty, minN, 2);
    }

    private Station uniquePigeonhole(int num, String difficulty, int minContainers, int maxItems) {
        for (int tries = 0; tries < 100; tries++) {
            int containers = minContainers + random.nextInt(3);
            int max = Math.max(containers + 2, maxItems);
            int items;
            if ("Easy".equals(difficulty)) items = containers + 1 + random.nextInt(Math.max(1, 4));
            else if ("Medium".equals(difficulty)) items = containers * 2 + 1 + random.nextInt(5);
            else {
            // Hard mode uses a larger ratio and less convenient numbers so
            // the ceiling calculation is genuinely worth working through.
            int extra = 1 + random.nextInt(9);
            items = containers * 4 + extra;
        }
            items = Math.min(items, max);
            String sig = "H:" + items + ":" + containers;
            if (usedSignatures.add(sig)) return buildPigeonhole(num, difficulty, items, containers);
        }
        return buildPigeonhole(num, difficulty, minContainers * 4 + 5, minContainers);
    }

    private Station uniqueRecurrence(int num, String difficulty, int minTerm, int maxTerm, boolean twoTerm) {
        for (int tries = 0; tries < 100; tries++) {
            int target = "Hard".equals(difficulty)
                    ? Math.max(9, minTerm + random.nextInt(maxTerm - minTerm + 1))
                    : minTerm + random.nextInt(maxTerm - minTerm + 1);
            int a = "Hard".equals(difficulty) ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
            int b = twoTerm ? ("Hard".equals(difficulty) ? 1 + random.nextInt(2) : 1 + random.nextInt(2)) : 0;
            int c = "Hard".equals(difficulty) ? 2 + random.nextInt(5) : random.nextInt(3);
            int base0 = "Hard".equals(difficulty) ? 2 + random.nextInt(4) : 1 + random.nextInt(4);
            int base1 = twoTerm ? ("Hard".equals(difficulty) ? 2 + random.nextInt(4) : 1 + random.nextInt(4)) : 0;
            String sig = "R:" + a + ":" + b + ":" + c + ":" + base0 + ":" + base1 + ":" + target;
            if (usedSignatures.add(sig)) return buildRecurrence(num, difficulty, a, b, c, base0, base1, target);
        }
        return buildRecurrence(num, difficulty, 1, twoTerm ? 1 : 0, 1, 1, twoTerm ? 1 : 0, minTerm);
    }

    private Station buildPermutation(int num, String diff, int n, int r) {
        String[] texts = {
            "The Vesperion navigation lock needs an ordered code.\nYou have " + n + " signal keys and must arrange " + r + " of them.\nHow many different ordered codes can you make?",
            "The rescue console has " + n + " switches. Choose " + r + " switches and place them in a fixed order.\nOrder matters here. How many codes are possible?"
        };
        Station s = new Station(num, StationType.PERMUTATION, diff, texts[random.nextInt(texts.length)]);
        s.n = n; s.r = r;
        PermCombSolver.Result res = PermCombSolver.permutation(n, r);
        s.correctAnswer = res.answer; s.derivationSteps = res.steps;
        finalizeOptions(s, PermCombSolver.combination(n, r).answer);
        return s;
    }

    private Station buildCombination(int num, String diff, int n, int r) {
        String[] texts = {
            "The astronaut must choose " + r + " repair parts from " + n + " parts in the storage bay.\nThe order of choosing does not matter.\nHow many different groups can be chosen?",
            "There are " + n + " crew badges and only " + r + " are needed for the repair team.\nWho is chosen matters, but order does not.\nHow many teams are possible?"
        };
        Station s = new Station(num, StationType.COMBINATION, diff, texts[random.nextInt(texts.length)]);
        s.n = n; s.r = r;
        PermCombSolver.Result res = PermCombSolver.combination(n, r);
        s.correctAnswer = res.answer; s.derivationSteps = res.steps;
        finalizeOptions(s, PermCombSolver.permutation(n, r).answer);
        return s;
    }

    private Station buildPigeonhole(int num, String diff, int items, int containers) {
        String[] texts;
        if ("Hard".equals(diff)) {
            texts = new String[]{
                "Atherion-7's rescue network receives " + items + " independent signal packets that must be distributed across " + containers + " relay channels.\nEvery packet has to be assigned to exactly one channel, and the packets may be distributed in any possible way.\nWithout knowing the distribution in advance, what is the smallest number of packets that you can guarantee will be present in at least one channel? Show the ceiling calculation carefully.",
                "During a high-load transmission, " + items + " diagnostic records must be placed into " + containers + " isolated memory sectors. No record may be discarded, and a sector may contain any number of records.\nWhat is the guaranteed minimum number of records in one sector, even in the most balanced possible distribution? Use the pigeonhole principle and calculate the ceiling rather than rounding down."
            };
        } else {
            texts = new String[]{
                "There are " + items + " emergency data chips and " + containers + " storage boxes.\nEvery chip must go into one box.\nWhat minimum number of chips is guaranteed in one box?",
                "The station has " + items + " samples and " + containers + " sealed drawers.\nAll samples must be stored.\nWhat is the guaranteed minimum in at least one drawer?"
            };
        }
        Station s = new Station(num, StationType.PIGEONHOLE, diff, texts[random.nextInt(texts.length)]);
        s.items = items; s.containers = containers;
        PigeonholeChecker.Result res = PigeonholeChecker.check(items, containers);
        s.correctAnswer = res.minItemsInSomeHole; s.derivationSteps = res.steps;
        finalizeOptions(s, (long) (items / containers));
        return s;
    }

    private Station buildRecurrence(int num, String diff, int a, int b, int c, long base0, long base1, int target) {
        boolean two = b != 0;
        String formula = two
                ? "f(n) = " + a + "f(n-1) + " + b + "f(n-2) + " + c + ", with f(0)=" + base0 + " and f(1)=" + base1
                : "f(n) = " + a + "f(n-1) + " + c + ", with f(0)=" + base0;
        String[] texts;
        if ("Hard".equals(diff)) {
            texts = new String[]{
                "The Atherion-7 navigation core recalculates its fuel reserve recursively. At every step it uses the two previous readings and then adds a fixed correction: " + formula + ".\nThe computer must be evaluated sequentially, so you cannot jump directly to the final term. Starting from the supplied base value(s), calculate each required intermediate term without rounding or skipping a step.\nWhat is the exact value of f(" + target + ")?",
                "A damaged guidance processor follows the recurrence " + formula + ".\nThe processor has already recorded the initial condition(s), but every later reading depends on earlier readings. Work forward term-by-term, keeping the arithmetic exact because a single incorrect intermediate value changes every later result.\nAfter carrying the recurrence through all required steps, what is the exact reading at step " + target + "?"
            };
        } else {
            texts = new String[]{
                "The Vesperion fuel computer follows this rule: " + formula + ".\nFollow the rule one step at a time.\nWhat is f(" + target + ")?",
                "A repair sensor updates its reading using: " + formula + ".\nThe first value is given above.\nWhat will the reading be at step " + target + "?"
            };
        }
        Station s = new Station(num, StationType.RECURRENCE, diff, texts[random.nextInt(texts.length)]);
        s.coeffA = a; s.coeffB = b; s.constantC = c; s.base0 = base0; s.base1 = base1; s.targetTerm = target;
        RecurrenceSolver.Result res = RecurrenceSolver.solve(a, b, c, base0, base1, target);
        s.correctAnswer = res.answer; s.derivationSteps = res.steps;
        Long mistake = target > 0 ? res.sequence.get(target - 1) : null;
        finalizeOptions(s, mistake);
        return s;
    }

    private void finalizeOptions(Station s, Long classicMistake) {
        long correct = s.correctAnswer;
        List<Long> opts = new ArrayList<>();
        opts.add(correct);
        if (classicMistake != null && classicMistake >= 0 && classicMistake != correct) opts.add(classicMistake);

        int spread = (int) Math.min(1000, Math.max(3, Math.abs(correct) / 3));
        int guard = 0;
        while (opts.size() < 4 && guard++ < 500) {
            long delta = 1 + random.nextInt(spread);
            long candidate = correct + (random.nextBoolean() ? delta : -delta);
            if (candidate < 0) candidate = correct + delta;
            if (!opts.contains(candidate)) opts.add(candidate);
        }
        long filler = Math.max(1, correct + Math.abs(correct) + 11);
        while (opts.size() < 4) {
            if (!opts.contains(filler)) opts.add(filler);
            filler++;
        }
        Collections.shuffle(opts, random);
        s.options = opts;
        s.correctOptionIndex = opts.indexOf(correct);
    }
}
