# experiments_v3 – benchmark tools and raw results (not part of the submission)

This folder holds everything behind the numbers in `RESULTS.md` and in the results section of
`groupA/README.md`. It is **not** part of the `groupA` submission: it contains a frozen copy of the
previous agent (package `refv2`) and benchmark configurations that use TAG's own MCTS as an *opponent*.

## Layout

| Path | What it is |
|---|---|
| `src/arena/Arena.java` | parallel match runner: plays N games, writes one CSV row per game |
| `src/arena/Probe.java` | MCTS iterations per decision of a groupA agent |
| `src/arena/WallProbe.java` | wall-clock and CPU time of every decision of an agent (timeout check) |
| `src/check/Equiv.java` | checks that the refactored card model gives the same numbers as v2 |
| `src/refv2/` | frozen copy of the previous agent v2 (package renamed to `refv2`) |
| `agents/b100`, `b500`, `b900` | agent JSON files per time budget (ms per decision) |
| `results/*.csv` | one row per game: seat, win (1/k for a shared first place), scores, ms per decision, points per card type |
| `results/*.log` | summary lines of each batch |
| `compare.py`, `summarise.py`, `types.py`, `pooled.py`, `factorial.py` | statistics: Wilson CI, paired differences, points per card type, paired differences pooled over seed sets, 2^4 factorial effects |
| `make_tables.sh`, `make_results.sh` | all tables, and `RESULTS.md` (`arena/make_results.sh` rewrites it) |
| `run.sh`, `batch.sh`, `build.sh` | compile and run (see below) |

## How the games are set up

* Game `g` of a run uses game seed `seed + g`, so two runs with the same seed play the **same deals**.
* The focus agent sits in seat `g mod n`; the opponents fill the other seats, their order rotated every `n` games.
* So every comparison between two runs is **paired** game by game, which removes most of the deal-to-deal noise.
* Time budgets are **CPU time** per decision (TAG's `ElapsedCpuTimer`), so results depend little on machine load.
* The deals are reproducible, the searches are not: TAG's determinisation RNG (`AbstractGameState.redeterminisationRnd`)
  is not seeded, so a repeated run plays the same deals but not exactly the same games.
* **Correction (found in the v4 study):** the deals were *not* the same. TAG fills the deck by iterating a `HashMap`
  whose order follows identity hash codes, which differ in every JVM run, so the same seed gave different deals in
  different runs. The comparisons of this study are therefore two-sample, not paired, comparisons. Their intervals
  are still about right, because pairing reduces the variance very little in Sushi Go (the win/loss correlation of
  two agents on the same deal is only 0.1–0.2). The v4 harness (`experiments_v4`) re-deals every game from a deck
  in a fixed order, shuffled with the game seed.

## Reproducing

Put this folder at `<TAG root>/arena`, then from the TAG root (Java 21+ on the PATH, TAG compiled with
`mvn compile`, the dependency classpath in `cp.txt`, e.g. `mvn dependency:build-classpath -Dmdep.outputFile=cp.txt`):

```
arena/build.sh
SEED=1 arena/batch.sh arena/results/my.log "label|3|500|arena/agents/b100/finalT4.json|arena/agents/b100/ref.json,arena/agents/b100/tagmcts.json"
cd arena/results && python3 ../summarise.py "v2=s1_ref.csv" "final=label.csv"
```

`batch.sh` takes `THREADS` (default 16) and `SEED` (default 1); `run.sh` takes JVM options in `JOPTS`.
Long budgets need fewer threads: every tree node stores a full game state, about 0.5 GB of live data per
game at 500 ms and about 1 GB at 900 ms. Used here: 100 ms – 16 threads; 500 ms – 10 threads (JOPTS
`-Xms22g -Xmx22g -Xmn16g -XX:SurvivorRatio=2 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC`); 900 ms – 6 threads
(same JOPTS, 250 games per run). The default collector settings thrashed at these budgets.

Final agent: `agents/b*/finalT4.json` (H + B + D + P + R + M, K 0.5, default heuristic). The T 8 candidate:
`agents/b100/r_K0.5_T8_M.json`, `agents/b500/finalM.json`, `agents/b900/finalM.json`.
