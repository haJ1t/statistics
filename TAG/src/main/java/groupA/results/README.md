# Per-game results of every run in the report

`v3/`, `v4/` and `v5/` hold the result files of the three experiment stages (copies of
`experiments_v3/results`, `experiments_v4/results` and `experiments_v5/results`, which sit next to this package in
our repository together with the scripts that made them). They are data only; nothing here is needed to run the
agent.

* `*.csv`: one row per game – game number `game` (game g uses seed s + g; in the seed-7 and held-out runs equal
  game numbers in two runs of the same seed block are the same deal, earlier runs used TAG's own shuffle), the seat
  of our agent `focusSeat`, its result `focusWin` (1 win, 1/k for a k-way shared first place, 0 otherwise),
  `focusScore` and `bestOppScore` (score margin = focusScore − bestOppScore), the measured milliseconds per
  decision, the wall time and number of decisions of the game and, in later runs, our points per card type.
* `*.log`: the arena log of the run – the agents and their JSON files, the win share of every agent over its seats
  ("win x% over n seats") and points per game by card type.
* `*_c0` … `*_c3`: chunks of a long run (each its own seed offset); the run of the same name without the suffix
  joins them in game order. `*_all.csv` joins a 300-game and a 200-game run.
* `*.txt`: other measurements (iterations per decision, timing probes, the four-player tree statistics
  `treestats4.txt`, `bench4.txt` and decision agreement `agree4.txt`).

Each report number can be recomputed from these files: win rates with Wilson 95% intervals, differences between
runs as the mean of the per-game differences matched by game number ± 1.96 standard errors (two-sample standard
errors for runs on different deals).
