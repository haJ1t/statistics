# Pre-upload check: group A, ECS7032P Assignment 1

**Files checked** (both dated 28 Oct 2026, 20:58)

| File | Size | sha256 |
|---|---|---|
| `/home/user/statistics/upload_ready/groupA.zip` | 328,202 bytes | `d87a0791…e6cec` |
| `/home/user/statistics/upload_ready/ECS7032P_Assignment1_Report_GroupA.pdf` | 580,592 bytes, 5 pages | `b144979e…44fff` |

I changed nothing under `/home/user/statistics`. All my work is under `/tmp/claude-0/pkgcheck/`.

## Verdict

- **The PDF can be uploaded as it is.**
- **The zip cannot be uploaded yet.** The agent itself is sound: it compiles on its own, loads through `PlayerFactory`, plays legal 3- and 4-player games within the time limits, and uses nothing from `players.mcts`. The problem is its README:
  - It is a stale copy. The zip was built at 20:58:49, and the README was corrected at 21:01:27.
  - It still says three things that are false. Each takes one sentence to fix, and then the zip needs rebuilding (about 15 minutes).
- **Confirm the group letter.** Nothing in the repository confirms that it is A.

---

## 1. Does groupA.zip meet section 3 of the brief?

| Requirement | What I found | Status |
|---|---|---|
| Only the package directory, no framework | 43 entries, all under `groupA/`: 4 directories, 7 `.java` files, `README.md`, `GENAI_PROMPTS.md`, 2 JSON files and 28 files in `genai/`. No framework file, no `.class` files, no IDE or OS clutter. | ✓ |
| Package named `group{LETTER}` | It is called `groupA`, but nothing confirms the letter. The development README (`TAG/src/main/java/groupA/README.md`, lines 3–4) still says: "**Rename first:** replace every `groupA` … with your real group letter, e.g. `groupK`." | ⚠ confirm |
| README: how to call the constructor, initialisation, parameters | Present: JSON use, `createPlayer`, a constructor example and a parameter table. Accuracy problems are listed in §3. | ✓ (with errors) |
| JSON file for the competition (3 players, 1 s) | `json/competition/SushiMCTS_1000ms.json`:<br>• `BUDGET_TIME` 900 ms CPU, plus a real-time guard `wallTimeLimitMS` 925<br>• three-player settings at the top level, and a `fourPlayer` block<br>• the extra `json/SushiMCTS_500ms.json` (430/450 ms) covers the brief's other statement (§4: four players, 500 ms); the README says which file to use when | ✓ |
| Everything needed is inside the package | Compiles against the framework alone. It uses only `core.*`, `games.sushigo.*`, `players.PlayerConstants`, `players.PlayerParameters` and `utilities.*`. | ✓ |
| Nothing from `players.mcts` used or copied | No import and no fully qualified reference. Of 947 non-trivial source lines, 0 also appear in `players/mcts/*.java` (lines shared with `players/basicMCTS` are excluded). | ✓ |
| Permitted base agent | Copies of `players.basicMCTS.BasicMCTSPlayer`, `BasicTreeNode` and `BasicMCTSParams` (identical to upstream TAG). This is the brief's "players.mcts.BasicMCTS", which lives in `players.basicMCTS` in this TAG. The `BasicTreeNode` structure and its untouched methods are kept, so it is not an "MCTS from scratch". | ✓ |
| Superfluous content | `genai/`: 28 files of reviewer prompts and reviews, 657 KB of the 842 KB unpacked (84% of the compressed zip). It is not code, but the report's GenAI statement points to it, so it must stay unless the report changes (§6). | keep |

---

## 2. Does the package compile and run on its own?

### Setup: a framework copy with no other groupA classes

```
FW=/tmp/claude-0/pkgcheck/fw; T=/home/user/statistics/TAG
cp -r $T/src/{main,test,assembly} $FW/src/;   rm -rf $FW/src/main/java/groupA
cp -r $T/target/classes $FW/target/classes;   rm -rf $FW/target/classes/groupA
cp -r $T/json $T/metrics $T/pom.xml $T/cp.txt $FW/;  ln -s $T/data $FW/data
cd $FW/src/main/java && unzip -q /home/user/statistics/upload_ready/groupA.zip
```

### a. Compiling with javac

```
cd $FW && javac -Xlint:all -d out -cp "target/classes:$(cat cp.txt)" src/main/java/groupA/*.java
```

- Exit code 0; 12 classes produced.
- With `-Xlint:all` the package produces 8 warnings:
  - 7 raw-type generic array creations (`OpponentModel.java:48–52`, `SushiTreeNode.java:1222–1223`)
  - 1 `this-escape` in `SushiMCTSParams.java:79`
  - (a 9th warning, `[path]`, comes from a dependency jar on the classpath, not from the package)
- With default flags there are no warnings.

### b. Following the README's build instruction

`mvn -o -q compile` in `$FW` exits 0 in 14 s.

### c. Loading the competition JSON and playing 3-player games

The harness is `/tmp/claude-0/pkgcheck/harness/src/PkgCheck.java`. It loads the JSON with `PlayerFactory.createPlayer`, plays games with `Game.runOne(GameType.SushiGo, …)`, rotates our seat, times every decision and asserts that each move is legal.

```
java -cp "target/classes:out:/tmp/claude-0/pkgcheck/harness/out:$(cat cp.txt)" PkgCheck \
     src/main/java/groupA/json/competition/SushiMCTS_1000ms.json 3 <games> <seed> \
     json/players/mcts.json json/players/basicmcts.json      # or json/players/osla.json
```

- **Loading:** the result is a `groupA.SushiMCTSPlayer` with:
  - budget: `BUDGET_TIME` 900, `wallTimeLimitMS` 925
  - K 0.5 and `SushiGoHeuristic`
  - switches: B, R, P, M, S and I on; A, D and F off
  - TAG printed no "Unexpected key" warning.
- **Games:** 13 games (seeds 42, 1000–1002 and 5000–5008) against TAG's MCTS (40 ms) plus BasicMCTS or OSLA.
  - All moves were legal and nothing threw an exception.
  - The agent won 11 of the 13.
- **Timing, 318 decisions:**
  - CPU time: at most 900.1 ms
  - real time: median 925.0 ms, at most 948.5 ms (while a second JVM was running)
  - no decision took more than 1,000 ms
  - iterations per decision: per-game means of 19,800 to 25,500; lowest single decision 2,661

### d. The README's tournament command

`target/TAG.jar` is built by `mvn package`, not by `mvn compile`, and the assembly plugin is not available offline here. So I ran the jar's Main-Class directly:

```
java -cp "target/classes:$(cat cp.txt)" core.TAG RunGames game=SushiGo nPlayers=3 \
     playerDirectory=../rg_agents mode=random matchups=3
```

- The folder held the competition file plus TAG's `mcts.json` and `osla.json`.
- Exit 0; results in `metrics/out/TournamentResults.txt`. Our agent won 0 of 3.
- Re-run with `matchups=9`: it won 8 of 9. The 0 of 3 was noise.

### e. The README's constructor example, as written

I extracted README lines 58–71 programmatically.

- **jshell:** `jshell --class-path "target/classes:out:$(cat cp.txt)" readme_ctor_test.jsh` runs the snippet unchanged.
  - The resulting parameters equal the top level of the competition file.
  - `p.copy()` keeps them.
  - A 3-player game ran (won 55–46–38).
- **javac:** the same statements inside the `main` of a class, with the imports above it, compile and run.
- **`new SushiMCTSPlayer()`:** every switch is off, the budget is `BUDGET_FM_CALLS` 4000, and it plays a game.

### f. Four players

- **500 ms file, 2 games (won both):** CPU at most 429.8 ms, real time at most 453.5 ms.
  - Reflection on `fourPlayerParams` shows the `fourPlayer` block in use with the top-level budget: 430/450, K 1, F with L 8, terminalScale 2, sigmaRoundDecay 0.5, opponentScoreValue, w 1, and the X0 constants.
  - With three players the block is not used.
- **1 s file, one 4-player game:** CPU at most 901.3 ms, real time at most 925.7 ms.

### g. The fast simulator F against TAG's forward model

The report and `SushiSim.java` both say the simulator was "checked move by move against the forward model". That check is not in the package, so I wrote one: `/tmp/claude-0/pkgcheck/simtest/src/groupA/SimDiffTest.java`.

- 300 three-player and 300 four-player random games: 15,300 turns, 1,466 of them with Chopsticks.
- **0 mismatches** in hands, counters, scores, round changes and final scores.
- The claim holds.

### h. Running in a fresh copy of the repository

- **API comparison:** I compared the 19 framework files the package uses with upstream TAG master (17 Oct 2026). 17 are identical; 2 differ only internally (`AbstractGameState` stack handling and `SGForwardModel._afterAction`).
- **The README's version requirement is wrong.** The agent calls `SGGameState.getCurrentSimultaneousPlayers()` (`SushiTreeNode.isTurnStart`).
  - Upstream added that method on **28 Sep 2026** (commit `ac69edbc`). Before it, neither `SGGameState` nor `AbstractGameState` has it.
  - `isHandKnown` dates from 11 Nov 2024 (`ae0f272a`).
  - The module's copy of TAG matches upstream as of 28–29 Sep 2026.
  - So the README's "TAG from November 2024 or later" is false: any TAG from November 2024 to August 2026 fails to compile the package.

---

## 3. The README against the code, the JSON files and Table III

### What checks out

- Authors and student numbers.
- Every file in the Files table exists.
- The competition timing values: 900 ms CPU / 925 ms real; 430 / 450.
- The `createPlayer` path, which is relative to the TAG root.
- The constructor signature and example.
- "Without a `fourPlayer` block … same settings."
- Every default in the Parameters table:
  - `BUDGET_FM_CALLS`/4000
  - `wallTimeLimitMS` 0
  - K √2
  - the game's own heuristic
  - B false/0.2
  - R, P, M, S and I false
  - W2 settings 0/0/false
  - F false, L 1
  - `fourPlayer` none
- Every 3- and 4-player value in that table matches both JSON files.
- The `args` order and the default (15, 0.7, 0.5, 4, 1, 0, 0, 0, 1, 2, 0) equal the constants in `SushiGoHeuristic` and `CardValueModel`. The 4-player `args` [15, 1, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3] equal the JSON.
- Steps 1–6 of "How the agent chooses a card" match `informationSetIteration`, `selectInformationSet`, `playOut`, `valuesOf` and `bestAction`.
- The H, B, R, P, M, S, I, X0, W2, F/L, A and D rows match the switches and the JSON.
- The time-limit text matches TAG's `ElapsedCpuTimer` (thread CPU time; `nanoTime` on Windows) and my timings.
- **Results table = rows 1–5 of Table III**, identical in win %, CI and margin.

### What is wrong or inaccurate

Line numbers refer to the zip's README. In the committed copy, `groupA_code/submission/README.md`, they are 1 higher from line 119 on and 2 higher from line 148 on.

- **R1. A false statement about v2 (zip only), lines 118–119.**
  - Current text: "Our earlier versions v2 (H, A, B, D) and v3 (H, B, D, P, R, M), used as baselines in the report, are configurations of the same code."
  - Every v2 opponent file (`TAG/arena/agents/*/ref.json`) uses `refv2.SushiMCTSParams`, a separately compiled copy of older code.
  - The committed README (modified 21:01:27; zip built 20:58:49) already has the correct sentence, plus two more edits the zip lacks:
    - "TAG's tuned ISMCTS" → "TAG's ISMCTS agent"
    - "(garbage collection)" → "(garbage collection, compilation)"
- **R2. G and Q are listed as "tested", lines 116–118.**
  - Current text: "Further switches, tested and off in the final agent: … G (MAST rollouts) … Q (online opponent model) …"
  - Neither has a completed run. `q5_mast10` and `q5_FL8Q` stopped at 50 of 250 games (`TAG/arena/results/*.err`, no CSV).
  - The report (§V) calls the opponent model "an untested option in our code".
  - `genai/review_draft7.md`, line 292, inside this same zip, already says so: "The README says Q is 'tested, off', but its only game run stopped at 50/250 games".
  - O, U, K, E and N do have results.
- **R3. Line 28, "TAG from November 2024 or later":** false (see §2h).
- **R4. Line 50, `java -jar target/TAG.jar …`:** the README's own build step ("`mvn compile` or the IDE") does not create `target/TAG.jar`. It needs `mvn package -DskipTests`.
- **R5. Line 134, "I (needs S)":** the code requires M, not S.
  - `SushiTreeNode` line 148 and `SushiMCTSPlayer.numberOfDeterminisations` test `useInformationSets && useMaxN`.
  - With S off, I still runs, and its selection is decoupled anyway.
  - With M off, I is silently ignored.
- **R6. Line 16, "every change is marked `MODIFICATION <id>`":** not true.
  - These changes carry no id: W2's parts (sigmaRoundDecay/`roundScale`, opponentScoreValue), maxChildShare, opponentK, treeThreshold, ownCardTree, stopAtRoundEnd and rolloutTemperature.
  - "W2" never appears in the code.
  - terminalScale is labelled "**Modification V**", an id that neither the README nor Table I defines (`SushiMCTSParams.java:41`, `SushiTreeNode.java:25` and `:1433`).
- **R7. The "4 players, 100 ms" result row cannot be reproduced from the package.** It came from a configuration that is not shipped: `TAG/arena/agents/final/SushiMCTS_100.json`, which uses X0 with w = 0.7 and K = 0.5, and has no W2, F or L. The shipped `fourPlayer` block (K 1, w 1, W2, F, L) is only hinted at by the "(≥ 500 ms)" notes.
- **R8. Minor points:**
  - P's "Where" column names `distinctActions`. In the final agent, identical cards are merged by `CardValueModel.distinctCards` inside I.
  - "never used more than 900 ms of CPU time": I measured 900.1 ms (3 players) and 901.3 ms (4 players).
  - "plays like BasicMCTS" holds against `new BasicMCTSPlayer(new BasicMCTSParams())`. The no-argument `BasicMCTSPlayer()` uses maxTreeDepth 5.
  - The README says the other JSON keys are explained next to their fields in `SushiMCTSParams`, but `breakMS` belongs to `PlayerParameters`.

---

## 4. How clean are the code and the package?

### What is good

- No `System.out`/`System.err`, `printStackTrace`, TODO/FIXME, commented-out code or debug switches.
- Warning-free with default compiler flags.
- No references to classes outside the package and the framework.
- **The comments explain the critical parts well:**
  - the class Javadoc of `SushiTreeNode`, covering A to I
  - `informationSetIteration`
  - the availability-count UCB in `selectInformationSet`
  - the formulas in `CardValueModel`
  - `SushiGoHeuristic`
  - the wall-clock guard
  - the switch to four-player settings
  - the rules in `SushiSim`

### Issues

- **C1. The V / W2 mismatch** (= R6).
- **C2. An unsupported "tested" claim in code:** `SushiTreeNode.java:62` says G was "Tested; off by default." (= R2).
- **C3. Comments that mislead about the final agent:**
  - `SushiTreeNode.java:21–22`: "The structure of BasicTreeNode is kept (closed-loop tree: every node stores its own state, a single scalar value from the root player's point of view, paranoid opponents in UCB)". This is true only with every switch off; the final agent uses an open-loop tree with max^n backups.
  - `SushiMCTSPlayer.java:113`: "// Identical to BasicMCTSPlayer: one tree on the state we were given". With I on, the hidden cards are re-sampled in every iteration.
  - `CardValueModel.java:18–23`: "shared by three components" (H, A, B). P, I, K, N, Q and F use it too.
- **C4. Leftover development notes:**
  - `CardValueModel.java:25–26`: "…which is why we always report iterations per decision under BUDGET_TIME" refers to the experiments.
  - `SushiMCTSPlayer.java:44–47`: the public fields `lastDeterminisations` and `lastIterations` are "for logging / analysis", but the logging code is not in the package.
  - `SushiSim.java:20`: "We checked it against the forward model move by move." The claim is true (§2g), but the test is not included.
- **C5. Unused code:**
  - overloads: `CardValueModel.estimatedScore(SGGameState,int)`, `marginalValue(SGGameState,int,SGCard)`, `actionValue(SGGameState,AbstractAction)`, `Config(double,double)`; `SushiGoHeuristic.scoreValue(double[],int)`, `evaluateAll(SGGameState)`; `SushiSim.makiAll()`
  - fields: `OpponentModel.observations` is never read
  - inherited leftovers: `SushiMCTSPlayer.toString()` (only calls `super`) and `setStateHeuristic`
- **C6. Size:** `SushiTreeNode.java` has 1,754 lines and about 20 switches, most of them off in the final agent. That is defensible because the report's ablations need them. No change advised before upload.
- **C7. `genai/`:** 26 of the 28 files contain absolute paths on the authors' machine (`/home/user/statistics/…`) and refer to files that are not in the package (`groupA_code/experiments_v*`, `report/build_report.py`, `TAG/arena/…`).

---

## 5. Is the report consistent with the package?

- **Page limits: ✓**
  - 5 A4 pages in IEEE two-column format, matching `Paper_Template.docx`.
  - The main text ends with the Conclusions on page 4.
  - The GenAI statement starts at the foot of page 4 and continues on page 5, followed by the 15 references (page 5 is about 60% full).
  - No placeholders. All references [1]–[15], Fig. 1–3 and Tables I–III are cited in the text.
- **Authors: ✓**
  - Halit Ozger 230625112, Weronika Podgorska 230394735, Hakan Coskun 241076864.
  - Identical in the PDF title block, the PDF metadata and the README, and they match the numbers the authors supplied (`GENAI_PROMPTS.md`, prompt 3).
  - Check the spelling against QMplus (diacritics).
- **The report's statements about the code: all ✓**
  - "a modified copy of BasicMCTS (package players.basicMCTS) … no code of TAG's advanced MCTS agent"
  - every modification is a switch, with the ids of Table I
  - the constants: σ 15, w 0.7, α 0.5, T 4, π 1, ν 2, Chopsticks 1, ωt = t/3, min(1, (r − k + 1)/T)
  - the footnote of Table I agrees with the JSON and `CardValueModel`
  - the descriptions of B, R, P, M, S and I, including the availability count and the greedy second Chopsticks card in the tree
  - K = 0.5 with three players
  - the four-player settings: X0; W2 (w 1, tanh(gap/2) with a Pudding tie-break, σ·(ρ/2)^0.5, opponents valuing their lead over the others' mean); F with L = 8; K 1
  - "submitted agent files: 900 ms (1 s limit) or 430 ms (500 ms limit), with wall-clock guards"
  - "F … checked move by move" (confirmed in §2g)
  - about 21,200 iterations per decision at 900 ms; I measured a mean of 19,800–25,500 per game
  - the descriptions of the opponents `SushiGo_3+P_HiBudget` and `SushiGoDUCT` match TAG's JSON
  - "framework unchanged": git history shows only the import commit touching `TAG/src` outside groupA
  - the results are "available from the authors"; the report does not claim they are in the package
  - the rows of Table III equal the README's results table
- **One contradiction:** the opponent model Q (R2). The report is right, so fix the README.
- **Minor:** §III-D's "Its files for 500 ms or more also use W2" implies there are files below 500 ms, but the package has none (= R7).

---

## 6. The generative-AI rules (section 2 of the brief)

### What is in place

- The acknowledgement sits at the end of the report, before the references. ✓
- A page-1 footnote cites [15] for "All sections, tables and figures". ✓
- [15] cites Anthropic, Claude, used through Claude Code, Oct 2026. It names no model version (optional to add). ✓
- **Prompts:**
  - The report prompt is given verbatim, in Turkish with a translation. It is identical in the report and in `GENAI_PROMPTS.md`. ✓
  - The quoted reviewer phrase "an independent, strict marker for the QMUL module ECS7032P" appears verbatim in 12 of the 14 reviewer prompts. ✓
  - `genai/` holds the prompt and the review for every marked draft (1–12, 14, and the check of 15), byte-identical to `report/reviews/`. Drafts 13 and 16 were not marked, which fits "successive drafts". ✓
  - `GENAI_PROMPTS.md` also gives the later-changes prompt and the session messages that produced the README and the package. ✓
- So the report's statement "the prompts of this report and these reports: GENAI_PROMPTS.md and the folder genai in our code package" is **true**.

### Gaps

- **(i) A translation is quoted as if verbatim.** The report quotes the tournament-sprint prompt as "the prompt", but `GENAI_PROMPTS.md` marks it as an English translation, and the original text is given nowhere.
- **(ii) Development-session prompts are "not reproduced".** This is disclosed, and acceptable, because the brief asks for prompts behind AI-generated report text.
- **(iii) The wrong draft is called final.** `GENAI_PROMPTS.md` says "The final draft's rewritten sections were checked once more". The checked draft was 15; the uploaded PDF is draft 16 with the author names added, and was not checked.
- **(iv) "The verbatim prompt for each draft"** should say "each marked draft".

---

## 7. Prioritised fix list

Edit `groupA_code/submission/README.md` and the Java sources in `TAG/src/main/java/groupA/`. `make_upload_ready.sh` copies from these.

### Must fix before upload

- **M0. Confirm on QMplus that your group letter is A.**
  - If it is not, rename: the folder; the `package` line in all 7 `.java` files; the `"class"` and `"heuristic"` names in both JSON files; the README title and paths; the PDF file name and its metadata.
- **M1. Correct the "tested" claims for G and Q** (R2, C2).
  - README, current text: "Further switches, tested and off in the final agent: O (opponents not searched), G (MAST rollouts), U (one tree per player), K (opponents limited to their best cards), E (several independent trees), Q (online opponent model) and N (denying the next player its favourite card in the rollouts)."
  - Replacement: "Further switches, tested and off in the final agent: O (opponents not searched), U (one tree per player), K (opponents limited to their best cards), E (several independent trees) and N (denying the next player its favourite card in the rollouts). G (MAST rollouts) and Q (online opponent model) are implemented but were not evaluated (no run finished)."
  - `SushiTreeNode.java:62`: "Tested; off by default." → "Implemented but not evaluated; off by default."
- **M2. Correct the TAG version requirement** (R3).
  - README, current text: "TAG from November 2024 or later (the agent calls `SGGameState.isHandKnown`) and Java 21, as for TAG itself."
  - Replacement: "TAG from 28 September 2026 or later, such as the copy provided for the module (the agent calls `SGGameState.getCurrentSimultaneousPlayers`, added in TAG commit ac69edbc, and `SGGameState.isHandKnown`), and Java 21, as for TAG itself."
- **M3. Rebuild the zip** with `bash /home/user/statistics/groupA_code/submission/make_upload_ready.sh`.
  - This also brings in the committed v2 sentence (R1), "TAG's ISMCTS agent" and "(garbage collection, compilation)".
  - Then re-check:

    ```
    cd /tmp/claude-0/pkgcheck/fw && rm -rf out src/main/java/groupA target/classes/groupA && mkdir out
    (cd src/main/java && unzip -q /home/user/statistics/upload_ready/groupA.zip)
    javac -d out -cp "target/classes:$(cat cp.txt)" src/main/java/groupA/*.java
    java -cp "target/classes:out:/tmp/claude-0/pkgcheck/harness/out:$(cat cp.txt)" PkgCheck \
         src/main/java/groupA/json/competition/SushiMCTS_1000ms.json 3 1 42 json/players/mcts.json json/players/osla.json
    unzip -p /home/user/statistics/upload_ready/groupA.zip groupA/README.md | diff - /home/user/statistics/groupA_code/submission/README.md   # must print nothing
    ```

### Should fix (small, in the same rebuild)

- **S1. Align the ids** (R6, C1).
  - README line 16: "every change is marked `MODIFICATION <id>`" → "changes are marked `MODIFICATION <id>` (ids of Table I) or named after their switch in `SushiMCTSParams`".
  - `SushiMCTSParams.java:41`: "// Modification V: finished games valued by the gap to the winner (0 = off)" → "// W2: finished games valued by the gap to the winner (0 = off)".
  - `SushiMCTSParams.java:44`: "// leaf values sharper when…" → "// W2: leaf values sharper when…".
  - `SushiMCTSParams.java:45`: "// max^n opponent model: every other player plays for its own score (our value unchanged)" → "// W2: in the backups every other player values its lead over the others' mean (our value unchanged)".
  - `SushiTreeNode.java:25`: "(F, L, U, K, V, N, Q)" → "(F, L, U, K, W2, N, Q)".
  - `SushiTreeNode.java:1433`: "MODIFICATION V: value of a finished game" → "MODIFICATION W2 (terminalScale): value of a finished game".
- **S2. Fix the tournament command** (R4).
  - Current text: "…all agent files in one folder (copy the competition file into it):"
  - Replacement: "…all agent files in one folder (copy the competition file into it; build `target/TAG.jar` with `mvn package -DskipTests`, or run `java -cp "target/classes:<dependency jars>" core.TAG RunGames …` after `mvn compile`):"
- **S3. Parameters table** (R5): "I (needs S)" → "I (needs M)".
- **S4. Results table** (R7). Add after "Score margin: …":
  - "The 4-player 100 ms row used the four-player settings without W2, F and L, with K = 0.5 and w = 0.7; the files in this package are for 500 ms and 1 s."
- **S5. Report, only if the PDF is rebuilt anyway** (page 5 has room):
  - "tested with the prompt “The lecturer…”" → "tested with the prompt (translated from Turkish) “The lecturer…”".
  - Add the original wording to `GENAI_PROMPTS.md` if it can be recovered.

### Optional improvements

- **O1. Fix the comments in C3 and C4.** For example, on `SushiTreeNode.java:21`, prefix "With every switch off, …".
- **O2. Remove the unused code in C5.**
- **O3. Tighten `GENAI_PROMPTS.md`:**
  - Add one sentence saying that paths inside `genai/` refer to the authors' development repository, which is not submitted.
  - "for each draft" → "for each marked draft (1–12 and 14)".
  - "The final draft's rewritten sections were checked once more" → "Draft 15's rewritten sections were checked once more; the submitted version applies that check's corrections".
- **O4. Name the base agent the way the brief does.** In the README's first paragraph, after `players.basicMCTS.BasicMCTSPlayer`, add "(listed in the brief as players.mcts.BasicMCTS)".
- **O5. Ship the v3 and 100 ms configurations** as JSON files (for example in `json/baselines/`), so results can be reproduced.
- **O6. Consider a wider timing margin.** The 925 ms real-time guard leaves 75 ms, and under load I saw 948.5 ms. TAG itself counts CPU time (at most about 901 ms), so this is fine there. If the competition measures real time on a busy machine, 875 ms would be safer, at a cost of about 5% fewer iterations.
- **O7. Correct the minor wording in R8.**
- **O8. Fonts:** the PDF embeds Liberation Serif, LibreOffice's metric-identical stand-in for the template's Times New Roman. It is invisible in print and can be ignored.

---

## 8. Are the two files ready to upload?

**No, not yet.**

- **The PDF is ready to upload unchanged.**
- **The zip is technically sound but should not be uploaded as it is.** Its README is a stale copy that:
  - wrongly says v2 is a configuration of the same code;
  - claims G and Q were tested, which contradicts the report;
  - gives a TAG version requirement that is false.
- **Once M0–M3 are done**, ideally together with S1–S4, and the re-check passes, both files are ready.
- **Upload the two files themselves**, not `/home/user/statistics/upload_ready.zip`.
