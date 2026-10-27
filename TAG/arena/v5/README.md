# experiments_v5 – in-class tournament sprint (not part of the submission)

Version 5 of the agent was made in one 4.5-hour session before the in-class tournament (27 October). This folder
holds everything behind the v5 numbers in `groupA/README.md`: scripts, agent files, per-game results and logs.
It uses the v4 harness (`arena.Arena`, frozen v2 in `refv2`, TAG's ISMCTS as an *opponent* only) unchanged.

## Machine and harness

* 4 vCPUs, 15 GB RAM, Linux, OpenJDK 21 (cloud container) instead of the 18-core laptop of the v4 study. zsh is not
  installed, so `build.sh`, `run.sh`, `launch.sh`, `queue.sh` and `probe.sh` are bash versions of the v4 scripts.
* **Memory settings matter on 4 cores.** With a 3 GB heap the young generation filled about four times per second:
  garbage-collection pauses took ~45 % of the wall-clock time (each game thread got only 45 % of a core). With
  `-Xms11g -Xmx11g -Xmn8g -XX:SurvivorRatio=3 -XX:+UseParallelGC` (default in `run.sh` via `queue.sh`/`launch.sh`)
  the pauses take ~6 % and the threads ~87 % of a core. Budgets are CPU time per decision, so this changes only
  how fast a run finishes, not its result.
* One run at a time with 4 game threads (`queue.sh` runs the lines of `q.txt` one after another).
* The v4 baseline on this machine (`v5_base3p100`: 58.1 % [53.7, 62.3], margin +1.16 ± 0.42) agrees with the v4
  validation run on the old machine (`V_3p100`: 55.0 %, +1.00 ± 0.44; paired difference +3.1 ± 5.8 win points), so
  results of both machines are comparable.

## Tools

| File | What it does |
|---|---|
| `build.sh` | compiles `groupA` into `target/classes`, the arena and the dev tools |
| `run.sh` | one arena run (`key=value` arguments of `arena.Arena`); `groupA` classes are snapshotted per run |
| `launch.sh`, `queue.sh`, `q.txt` | start one run in the background / run the queue file line by line |
| `mk.py` | agent JSON = base JSON + changed parameters (`key=value` for 3 players, `4:key=value` for the `fourPlayer` block, `args=` heuristic constants) |
| `table.py` | markdown table: win % [Wilson 95 % CI], score margin, paired differences to a base run (same seed = same deals) |
| `compare.py`, `tab.py` | the v4 statistics scripts |
| `probe.sh` | pauses the arena runs and measures wall-clock / CPU time of every decision of the competition files (`arena.WallProbe`) |
| `../dev/groupA/GreedyTune.java` (v4) | search-free coordinate tuner of the card-model constants; `results/tune_X3.log` is the 3-player tuning |
| `../dev/groupA/DenialDuel.java` (new) | search-free duel of greedy card-value policies with and without the denial term N |

## Runs

Every run is a line of `q.txt` (`label players games seed focus opponents [threads]`, a `#` marks a run that was
started; `queue2.sh` writes `results/<label>.csv/.log/.err` and `results/queue.log`). All 100 ms runs use 4 game
threads; the opponents are `arena/agents/b100/ref.json` (v2) and `arena/agents/b100/tagmcts.json` (TAG's ISMCTS) with
the same budget, as in the v4 study.

| Label | Players | Games | Seed | Agent file | What changes against v4 |
|---|---|---|---|---|---|
| `v5_base3p100` | 3 | 500 | 7 | `arena/agents/final/SushiMCTS_100.json` | nothing (v4 final agent, 100 ms) |
| `v5_X3_3p100` | 3 | 500 | 7 | `agents/b100_X3.json` | heuristic args X3 = [15, 0.7, 0.5, 4, 0.8, 4, 0.25, 1.0, 1.5, 4, 3] |
| `v5_N05_3p100` | 3 | 500 | 7 | `agents/b100_N05.json` | `rolloutDenial` 0.5 |
| `v5_V2D_3p100` | 3 | 500 | 7 | `agents/b100_V2D.json` | `terminalScale` 2, `sigmaRoundDecay` 0.5 |
| `v5_W1_3p100` | 3 | 500 | 7 | `agents/b100_W1.json` | heuristic args [15, 1.0, 0.5, 4] (w 1.0) |
| `v5_R3V2_3p100` | 3 | 500 | 7 | `agents/b100_R3V2.json` | `rolloutRounds` 3, `terminalScale` 2 |
| `v5_4X3_4p100` | 4 | 500 | 7 | `agents/b100_4X3.json` | X3 in the `fourPlayer` block (instead of X0) |
| `v5_comp_3p1000` | 3 | 90 | 7 | `src/main/java/groupA/json/competition/SushiMCTS_1000ms.json` | competition file (900 ms CPU, guard 925 ms), opponents at 900 ms, 3 threads |
| `v5_comp_4p500` | 4 | 40 | 7 | `src/main/java/groupA/json/competition/SushiMCTS_500ms.json` | competition file (430 ms CPU, guard 450 ms), opponents at 500 ms, 3 threads |
| `v5_field_3p100` | 3 | 300 | 7 | `arena/agents/final/SushiMCTS_100.json` | v4 against **two** TAG ISMCTS (a stronger field) |

`v5_comp_3p1000b` (3 players, 45 games, seed 97 = the deals and seats of games 90–134 of `V_3p900`, the 1000 ms
competition file, 3 threads) adds to `v5_comp_3p1000`; `merge_comp.py` joins both into `results/v5_comp_3p1000_all.csv`
(135 games: 51.1 % [42.8, 59.4]; `V_3p900` on the same deals: 52.6 %).

## v4 is unchanged inside v5

The only v5 code change is switch N (`rolloutDenial`, default 0): `CardValueModel.addDenial` and three lines in
`SushiTreeNode.policyValues` (the card-model view is now built once and also passed to `addDenial` when λ > 0).
The v4 dev tool `EquivF` (deterministic searches with the 4-player competition settings, F + L8, on fixed states)
gives the same decision hash for the v4 build (commit 323ee7d) and the v5 build: `trace-hash=4814249538744286751`
(240 searches). The 3-player path cannot be hashed the same way (TAG re-samples hidden cards in
`AbstractGameState.copy(player)` with an unseeded random generator), but it differs from v4 only by the
`if (denial > 0)` block.

## Timing probes

`results/probe_quiet.txt` (`probe.sh`, the v4 probe settings: 2 GB heap, ParallelGC with 2 threads),
`results/probe_quiet_F.txt` (the same with fast simulation F for 3 players, `agents/comp1000_F.json`) and
`results/probe_quiet_defaultjvm.txt` (the JVM's default settings: G1, default heap; also budget and guard 850 ms,
`agents/comp1000_g850.json`). Every decision of our agent in 3 games per file, the arena runs paused (SIGSTOP).

| Setting | Players | CPU per decision: mean / max (ms) | Wall-clock: median / 95 % / max (ms) | Over the limit |
|---|---|---|---|---|
| 1000 ms file, default JVM | 3 | 845 / 900 | 925 / 926 / 1004 | 1 of 73 |
| 1000 ms file, budget and guard 850, default JVM | 3 | 769 / 846 | 850 / 852 / 1002 | 1 of 73 |
| 500 ms file, default JVM | 4 | 418 / 453 | 441 / 451 / 472 | 0 of 67 |
| 1000 ms file, 2 GB ParallelGC | 3 | 694 / 901 | 925 / 1085 / 1455 | 5 of 73 |
| 1000 ms file + F, 2 GB ParallelGC | 3 | 723 / 901 | 925 / 1319 / 1653 | 8 of 76 |
| 500 ms file, 2 GB ParallelGC | 4 | 398 / 430 | 440 / 451 / 766 | 2 of 66 |

The other files in `agents/` (K 0.35, X0, N 0.25 / 1.0, X3 + N, X3 + R3 / V2D / ε 0.1, N + V2D) were prepared but not
run. `tune_X3.log` is the 3-player tuning: `GreedyTune tune 3 20000 0.2 2 <X0> "<default>;<X0>" 2`.

## Reproducing

Put the v4 experiment folder at `<TAG root>/arena` (as in v4) and this folder at `<TAG root>/arena/v5`; the dev
tool goes to `<TAG root>/arena/dev/groupA/`. From the TAG root, after `mvn compile` and
`mvn dependency:build-classpath -Dmdep.outputFile=cp.txt`:

```
arena/v5/build.sh
nohup arena/v5/queue2.sh arena/v5/q.txt &       # runs the lines of q.txt that do not start with #
cd arena/v5 && ./make_results.sh                # RESULTS.md
```
