# SpaceBound

## Gameplay update / compile repair

This version combines the original project ZIP with the supplied updated `GameApp.java` and adds the missing `GameController.skipCurrentStation()` implementation.

### Question flow
- 12 questions are available in one run and can be browsed freely with **PREVIOUS** and **NEXT**.
- Topic order: **Permutation -> Combination -> Pigeonhole -> Recurrence**.
- A **correct** answer collects one unit of fuel and advances the rocket.
- A wrong answer does **not** move the rocket; it increases the wrong counter and the player can retry or browse to another question.
- Four wrong answers end the mission.
- Eight correct answers provide enough fuel to win the mission and reach Earth.
- Hints are limited to **4 total across all 12 questions**.

### Compile repair
`GameApp.java` calls `game.skipCurrentStation()`. The original `GameController.java` did not contain that method, which caused IntelliJ's:

`cannot find symbol: method skipCurrentStation()`

The method is now implemented in `GameController.java`.

### Tests run
The non-JavaFX logic was compiled successfully with `javac`, then `LogicTest`, `GameFlowTest`, and an additional behavior test were run. The behavior test confirmed that a wrong answer advances to a new question, skip advances without changing fuel/wrong counters, and the requested topic order is maintained.

JavaFX UI compilation depends on the JavaFX SDK configured in the user's IntelliJ project.

### UI updates
- The upper header and 12-number rocket route stay fixed.
- Step-by-step solutions use smaller text and stay inside a bounded solution box with its own scrollbar.
- The question card remains centered.
- The separate right-side learning panel contains real concept/formula guidance and the 4-use hint button.
- The question screen uses a darker background, less prominent solar artwork, and less star blinking.
- PREVIOUS/NEXT navigation is available for all questions.
- Corrected the destination planet spelling to **Atherion-7** wherever that name appears in the UI.

