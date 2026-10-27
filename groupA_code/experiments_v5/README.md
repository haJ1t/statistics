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
