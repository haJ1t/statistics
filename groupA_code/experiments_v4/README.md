# experiments_v4 – benchmark tools and raw results of the v4 study (not part of the submission)

This folder holds everything behind the numbers in `RESULTS.md` and in the results section of
`groupA/README.md` (version 4 of the agent). It is **not** part of the `groupA` submission: it contains a frozen
copy of the previous agent (package `refv2`, the "v2" opponent) and benchmark configurations that use TAG's
own MCTS (`players.mcts`) as an *opponent*. The v3 study is in `experiments_v3/` (same harness, older runs).

## Layout

| Path | What it is |
|---|---|
| `src/arena/Arena.java` | parallel match runner: plays N games, writes one CSV row per game |
| `src/arena/Probe.java`, `src/arena/WallProbe.java` | iterations per decision; wall-clock and CPU time of every decision (timeout check) |
| `src/check/Equiv.java` | v3 check: the refactored card model gives the same numbers as v2 |
| `src/refv2/` | frozen copy of the previous agent v2 (package renamed to `refv2`) |
| `dev/groupA/` | development checks and diagnostics of v4 (`dev/README.md`): `SushiSimCheck` (F is exact), tree statistics, opponent prediction, the X0 tuner, … |
| `agents/b100`, `b500`, `b900` | agent JSON files per time budget (ms per decision); `ref.json` = v2, `tagmcts.json` = TAG's ISMCTS |
| `agents/final/` | the final agent per budget, as used in the validation runs |
| `agents/comp/` | the two competition files (`groupA/json/competition`) |
| `results/*.csv` | one row per game: seat, win (1/k for a shared first place), scores, ms per decision, points per card type |
| `results/*.log`, `results/*.err` | summary block of each run (win rates of every agent, points per card type) and progress |
| `make_v4_tables.py` | every table of `RESULTS.md` (run it in `results/`) |
| `tab.py`, `compare.py`, `summarise.py`, `types.py`, `pooled.py`, `factorial.py` | statistics: Wilson CI, score margin, paired differences, points per card type (the last three are from the v3 study) |
| `run.sh`, `batch.sh`, `build.sh` | compile and run (see below) |
| `battery_guard.sh` | pauses all runs (SIGSTOP) when a laptop on battery falls below a threshold, resumes them on AC |
| `tools/quiet_probe.sh` | pauses the running games and measures every decision time of the two competition files on the idle machine (`results/probe_quiet.txt`); `results/probe_comp.txt` is the same probe with 20 other game threads running |

## How the games are set up

* The focus agent sits in seat `g mod n` in game `g`; the opponents fill the other seats, alternating.
* Game `g` of a run uses game seed `seed + g`. **Since 19 October, 01:37** the runner re-deals every game from a
  deck in a fixed order, shuffled with the game seed (`Arena.canonicalDeal`), so two runs with the same seed play the
  **same deals and seats** and can be compared game by game. Before that, TAG's own deal was used: TAG fills the deck
  by iterating a `HashMap` whose order follows identity hash codes, which differ in every JVM run, so the same seed
  gave different deals in different runs. Runs from before 01:37 on 19 October (marked † in `RESULTS.md`, listed in
  `make_v4_tables.py`) and all runs of the v3 study were therefore played on independent deals: their "paired"
  differences are ordinary two-sample differences, whose confidence intervals are still valid.
* Pairing helps little in Sushi Go: the win/loss correlation of two agents on the same deal is only 0.1–0.2.
* Development used seeds 1 and 2; the final agent was validated on seed 7, which no decision used.
* Time budgets are **CPU time** per decision (TAG's `ElapsedCpuTimer`, thread CPU time), the same for every agent,
  so results depend little on machine load. None of the benchmark files sets the wall-clock guard
  (`wallTimeLimitMS` 0); only the competition files do.
* The searches are not seeded, so a repeated run plays the same deals but not exactly the same games.

## Reproducing

Put this folder at `<TAG root>/arena`, then from the TAG root (Java 21+ on the PATH, TAG compiled with
`mvn compile`, the dependency classpath in `cp.txt`, e.g. `mvn dependency:build-classpath -Dmdep.outputFile=cp.txt`):

```
arena/build.sh
SEED=7 THREADS=8 arena/batch.sh arena/results/my.log "V_3p100|3|500|arena/agents/final/SushiMCTS_100.json|arena/agents/b100/ref.json,arena/agents/b100/tagmcts.json"
cd arena/results && python3 ../tab.py V_3p100.csv && python3 ../make_v4_tables.py
```

`batch.sh` takes `THREADS` and `SEED`; `run.sh` takes JVM options in `JOPTS`. Used here: 100 ms – 8 to 16
threads; 500 and 900 ms – 8 threads with
`JOPTS="-Xms10g -Xmx10g -Xmn7g -XX:SurvivorRatio=2 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC"`.
v4's open-loop tree stores no game states, so it needs far less memory than v3's.

## Naming of the runs

| Prefix | Setting |
|---|---|
| `V_` | validation of the final agent (seed 7) |
| `A_` | leave-one-out ablation (3 players, 100 ms, seed 7) |
| `B_` | other framework agents (3 players, 100 ms, seed 7) |
| `d1_`, `s1_`, `abl_` | 3 players, 100 ms, seed 1 |
| `f5_`, `f9_` | 3 players, 500 / 900 ms, seed 1 |
| `q4_` | 4 players, 100 ms, seed 1 |
| `q5_` | 4 players, 500 ms, seed 1 (`q5_X0K10` … `q5_FW2i`) or seed 2 (`q5_W2r`, `q5_FL8…`, `q5_FUL8`, `q5_FW2th8`, `q5_X0b100`) |

`runs.md` lists the players, games, seed, agent file and opponents of every run.
