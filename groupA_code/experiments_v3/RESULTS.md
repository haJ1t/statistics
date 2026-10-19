# Sushi Go agent v3 – experiment results

All experiments: Sushi Go in TAG, 3 players unless stated, 500 games per condition unless stated.
Our agent ("focus") plays against **v2** (the previous version of the groupA agent, H+A+B+D, frozen copy in
`src/refv2`) and **TAG's tuned ISMCTS** (`players.mcts.MCTSPlayer` configuration from `agents/*/tagmcts.json`,
used only as an opponent). Game *g* uses game seed *seed + g*; the focus agent sits in seat *g* mod *n* and the
opponents rotate over the other seats. Runs with the same seed therefore play **identical deals and seats** and
are compared game by game.

* **Win %**: first place, a shared first place counts 1/k. Brackets: Wilson 95 % confidence interval.
  Chance level: 33.3 % (3 players), 25 % (4 players).
* **Δ win (paired)**: mean of the per-game differences to the base run, ± 95 % CI (1.96 × standard error), z = mean / SE.
* **Score margin**: focus score − best opponent score, points per game, ± 95 % CI.
* Budgets are CPU time per decision (TAG `ElapsedCpuTimer`), identical for all agents in a game.

Letters: H heuristic, A progressive bias, B informed rollouts, D determinisation, P action pruning,
O opponent model in the tree, M max^n backups, R rollouts to the end of the round.
**final** = H + B + D + P + R + M with K = 0.5 and the default heuristic args [σ 15, leaderWeight 0.7, α 0.5, T 4].
**candidate T 8** = the same agent with heuristic horizon T = 8; it was the final agent until the tie-break below
(the leave-one-out ablation was run around it).

### 1. Screening at 100 ms (seed 1, 500 games each, paired with v2)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 = previous final agent (H+A+B+D) (base) | 500 | 24.8 [21.2, 28.8] | – | -3.40 ± 0.52 | – |
| v2 + P | 500 | 28.1 [24.3, 32.2] | +3.3 ± 5.4 (z = +1.20) | -2.90 ± 0.49 | +0.50 ± 0.67 |
| v2 + O | 500 | 22.9 [19.4, 26.8] | -1.9 ± 5.2 (z = -0.71) | -3.88 ± 0.52 | -0.48 ± 0.74 |
| v2 + P + O | 500 | 23.3 [19.8, 27.2] | -1.5 ± 5.0 (z = -0.58) | -3.24 ± 0.51 | +0.16 ± 0.70 |
| v2 + P + O − A | 500 | 27.1 [23.4, 31.2] | +2.3 ± 5.4 (z = +0.83) | -3.16 ± 0.51 | +0.24 ± 0.72 |
| HBDP (v2 + P − A) | 500 | 27.3 [23.6, 31.4] | +2.5 ± 5.3 (z = +0.93) | -2.60 ± 0.47 | +0.80 ± 0.68 |
| HBDP + K 0.5 | 500 | 26.8 [23.1, 30.8] | +2.0 ± 5.4 (z = +0.72) | -2.91 ± 0.49 | +0.49 ± 0.73 |
| HBDP + M | 500 | 28.5 [24.7, 32.6] | +3.7 ± 5.4 (z = +1.34) | -2.94 ± 0.52 | +0.46 ± 0.72 |
| HBDP + rollouts to game end | 500 | 34.0 [30.0, 38.3] | +9.2 ± 5.4 (z = +3.36) | -1.91 ± 0.48 | +1.49 ± 0.70 |
| HBDP + R | 500 | 39.0 [34.8, 43.3] | +14.2 ± 6.0 (z = +4.63) | -1.19 ± 0.48 | +2.21 ± 0.76 |
| HBDP + R + M | 500 | 39.4 [35.2, 43.7] | +14.6 ± 5.6 (z = +5.13) | -1.01 ± 0.51 | +2.39 ± 0.71 |
| HBDP + R, K 0.5 | 500 | 41.3 [37.1, 45.7] | +16.5 ± 5.7 (z = +5.65) | -1.16 ± 0.45 | +2.24 ± 0.69 |
| HBDP + R, ε 0.1 | 500 | 34.3 [30.3, 38.6] | +9.5 ± 5.6 (z = +3.33) | -1.70 ± 0.47 | +1.70 ± 0.67 |
| HBDP + R, ε 0.3 | 500 | 39.4 [35.2, 43.7] | +14.6 ± 5.6 (z = +5.09) | -1.13 ± 0.46 | +2.27 ± 0.69 |
| HBDP + R − D | 500 | 37.9 [33.8, 42.2] | +13.1 ± 5.8 (z = +4.45) | -1.39 ± 0.46 | +2.01 ± 0.70 |
| HBDP + R, T 8 | 500 | 40.2 [36.0, 44.6] | +15.4 ± 5.8 (z = +5.24) | -1.00 ± 0.49 | +2.40 ± 0.71 |
| HBDP + R, α 1 | 500 | 31.0 [27.1, 35.2] | +6.2 ± 5.7 (z = +2.15) | -2.38 ± 0.43 | +1.02 ± 0.70 |
| HBDP + R, leaderWeight 0.5 | 500 | 34.9 [30.9, 39.2] | +10.1 ± 5.5 (z = +3.61) | -1.37 ± 0.44 | +2.03 ± 0.69 |
| HBDP + R, σ 8 | 500 | 40.7 [36.5, 45.1] | +15.9 ± 5.8 (z = +5.38) | -1.19 ± 0.47 | +2.21 ± 0.70 |
| HBDP + R, K 0.5, T 8 | 500 | 42.2 [37.9, 46.6] | +17.4 ± 5.7 (z = +5.98) | -1.30 ± 0.43 | +2.10 ± 0.68 |
| HBDP + R, K 0.3, T 8 | 500 | 34.5 [30.4, 38.7] | +9.7 ± 5.7 (z = +3.32) | -1.90 ± 0.44 | +1.50 ± 0.68 |
| HBDP + R, K 0.5, α 0.25 | 500 | 40.9 [36.7, 45.3] | +16.1 ± 5.6 (z = +5.60) | -0.94 ± 0.48 | +2.46 ± 0.72 |
| HBDP + R, K 0.5, T 8, ε 0.3 | 500 | 39.0 [34.8, 43.3] | +14.2 ± 5.5 (z = +5.02) | -1.41 ± 0.45 | +1.99 ± 0.69 |
| candidate = HBDP + R + M, K 0.5, T 8 | 500 | 45.2 [40.9, 49.6] | +20.4 ± 5.7 (z = +6.98) | -0.09 ± 0.42 | +3.31 ± 0.68 |
| **final** = HBDP + R + M, K 0.5 (T 4) | 500 | 50.1 [45.7, 54.5] | +25.3 ± 5.6 (z = +8.79) | +0.02 ± 0.44 | +3.42 ± 0.67 |

### 2. Replication on new deals (seed 1001, 100 ms)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (H+A+B+D) (base) | 500 | 22.9 [19.4, 26.8] | – | -3.42 ± 0.49 | – |
| HBDP + R, K 0.5, T 8 | 500 | 37.5 [33.4, 41.8] | +14.6 ± 5.5 (z = +5.18) | -1.66 ± 0.46 | +1.77 ± 0.65 |
| candidate T 8 (+ M) | 500 | 41.9 [37.7, 46.3] | +19.0 ± 5.7 (z = +6.48) | -0.48 ± 0.43 | +2.95 ± 0.68 |
| **final (T 4)** | 500 | 49.4 [45.0, 53.8] | +26.5 ± 5.8 (z = +8.90) | -0.02 ± 0.44 | +3.41 ± 0.66 |

### 3. Leave-one-out ablation around the T 8 candidate (seed 1, 100 ms, paired with it)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| candidate (T 8) (base) | 500 | 45.2 [40.9, 49.6] | – | -0.09 ± 0.42 | – |
| − M | 500 | 42.2 [37.9, 46.6] | -3.0 ± 6.1 (z = -0.97) | -1.30 ± 0.43 | -1.21 ± 0.58 |
| − R | 500 | 45.2 [40.9, 49.6] | +0.0 ± 6.2 (z = +0.00) | -0.22 ± 0.47 | -0.13 ± 0.64 |
| − P | 500 | 43.1 [38.8, 47.5] | -2.1 ± 6.2 (z = -0.66) | -0.71 ± 0.45 | -0.62 ± 0.63 |
| − B | 500 | 45.7 [41.4, 50.1] | +0.5 ± 6.1 (z = +0.16) | -0.90 ± 0.49 | -0.81 ± 0.65 |
| − D | 500 | 41.9 [37.7, 46.3] | -3.3 ± 6.2 (z = -1.04) | -0.85 ± 0.47 | -0.76 ± 0.63 |
| + A | 500 | 48.1 [43.8, 52.5] | +2.9 ± 6.3 (z = +0.91) | -0.13 ± 0.44 | -0.04 ± 0.61 |
| + O | 500 | 28.3 [24.5, 32.4] | -16.9 ± 5.8 (z = -5.74) | -2.83 ± 0.52 | -2.74 ± 0.66 |
| K1.41 | 500 | 39.9 [35.7, 44.3] | -5.3 ± 6.2 (z = -1.66) | -1.09 ± 0.46 | -1.00 ± 0.63 |
| T4 | 500 | 50.1 [45.7, 54.5] | +4.9 ± 6.0 (z = +1.59) | +0.02 ± 0.44 | +0.11 ± 0.61 |
| − H | 500 | 25.2 [21.6, 29.2] | -20.0 ± 5.9 (z = -6.62) | -5.51 ± 0.66 | -5.42 ± 0.77 |

### Tie-break on new deals (100 ms; seeds 2001 and 3001, 500 games each; base v2)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (seed 2001) (base) | 500 | 23.5 [20.0, 27.4] | – | -3.42 ± 0.50 | – |
| candidate T 8 | 500 | 43.7 [39.4, 48.1] | +20.2 ± 5.8 (z = +6.83) | -0.77 ± 0.47 | +2.65 ± 0.71 |
| **final = T 4** | 500 | 47.5 [43.1, 51.8] | +24.0 ± 5.7 (z = +8.19) | -0.10 ± 0.44 | +3.32 ± 0.67 |
| T 4 + A | 500 | 42.1 [37.8, 46.5] | +18.6 ± 5.6 (z = +6.49) | -0.70 ± 0.48 | +2.72 ± 0.72 |
| T 4 − R | 500 | 33.8 [29.8, 38.1] | +10.3 ± 5.4 (z = +3.72) | -1.72 ± 0.49 | +1.70 ± 0.70 |

| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (seed 3001) (base) | 500 | 24.4 [20.8, 28.4] | – | -3.29 ± 0.51 | – |
| candidate T 8 | 500 | 42.2 [37.9, 46.6] | +17.8 ± 5.5 (z = +6.30) | -0.72 ± 0.44 | +2.56 ± 0.66 |
| **final = T 4** | 500 | 45.1 [40.8, 49.5] | +20.7 ± 5.6 (z = +7.25) | -0.61 ± 0.46 | +2.68 ± 0.65 |
| T 4 + A | 500 | 48.1 [43.8, 52.5] | +23.7 ± 5.5 (z = +8.39) | -0.03 ± 0.45 | +3.25 ± 0.65 |
| T 4 − R | 500 | 33.7 [29.7, 38.0] | +9.3 ± 5.5 (z = +3.31) | -1.70 ± 0.48 | +1.59 ± 0.72 |

Pooled paired comparisons:
| Comparison | Paired games | Δ win (points) | z | Δ score margin | z |
|---|---|---|---|---|---|
| T 4 vs T 8 (seeds 1, 2001, 3001; the tie-break) | 1500 | +3.9 ± 3.4 | +2.21 | +0.30 ± 0.36 | +1.63 |
| T 4 vs T 8 (seeds 1, 1001, 2001, 3001) | 2000 | +4.8 ± 3.0 | +3.16 | +0.34 ± 0.31 | +2.16 |
| T 4 + A vs T 4 (seeds 2001, 3001) | 1000 | -1.2 ± 4.3 | -0.54 | -0.01 ± 0.46 | -0.06 |
| T 4 − R vs T 4 (seeds 1, 2001, 3001) | 1500 | -13.2 ± 3.5 | -7.47 | -1.37 ± 0.38 | -7.07 |
| final (T 4) vs v2 (seeds 1, 2001, 3001) | 1500 | +23.3 ± 3.3 | +14.00 | +3.14 ± 0.38 | +16.03 |
| final (T 4) vs v2 (seeds 1, 1001, 2001, 3001) | 2000 | +24.1 ± 2.9 | +16.58 | +3.21 ± 0.33 | +18.96 |
| candidate T 8 vs v2 (seeds 1, 1001, 2001, 3001) | 2000 | +19.4 ± 2.8 | +13.31 | +2.87 ± 0.34 | +16.50 |

### 4. Budget 500 ms (3 players, seed 1, 10 games in parallel, paired with v2)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (H+A+B+D) (base) | 500 | 18.3 [15.2, 21.9] | – | -4.26 ± 0.47 | – |
| candidate T 8 | 500 | 40.5 [36.3, 44.9] | +22.2 ± 5.3 (z = +8.21) | -0.82 ± 0.42 | +3.44 ± 0.63 |
| **final (T 4)** | 500 | 40.7 [36.4, 45.0] | +22.4 ± 5.5 (z = +7.90) | -0.96 ± 0.41 | +3.30 ± 0.62 |

### 4b. Changes aimed at long budgets, 500 ms (seed 1, paired with the final agent)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| final (K 0.5, B on) (base) | 500 | 40.7 [36.4, 45.0] | – | -0.96 ± 0.41 | – |
| K 1.0 | 500 | 40.4 [36.2, 44.8] | -0.3 ± 5.9 (z = -0.09) | -1.09 ± 0.43 | -0.13 ± 0.59 |
| − B (uniformly random rollouts) | 500 | 39.3 [35.1, 43.6] | -1.4 ± 5.9 (z = -0.47) | -1.33 ± 0.39 | -0.37 ± 0.56 |

### 5. Budget 900 ms (3 players, seed 1, 250 games, 6 games in parallel, paired with v2)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (H+A+B+D) (base) | 250 | 18.8 [14.4, 24.1] | – | -3.93 ± 0.60 | – |
| **final (T 4)** | 250 | 32.8 [27.3, 38.8] | +14.0 ± 7.3 (z = +3.74) | -1.86 ± 0.54 | +2.06 ± 0.81 |

### 6. Four players, 100 ms (seed 1; the three opponent seats alternate v2 / TAG ISMCTS, 2 + 1, swapped every 4 games; chance 25 %)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (H+A+B+D) (base) | 500 | 16.0 [13.0, 19.5] | – | -5.38 ± 0.52 | – |
| **final (T 4)** | 500 | 38.6 [34.4, 42.9] | +22.6 ± 5.2 (z = +8.50) | -1.15 ± 0.44 | +4.23 ± 0.69 |

### 7. Four players, 500 ms (seed 1, 250 games, 10 games in parallel; opponent seats as in 6; chance 25 %)
| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |
|---|---|---|---|---|---|
| v2 (H+A+B+D) (base) | 250 | 11.2 [7.9, 15.7] | – | -6.39 ± 0.72 | – |
| **final (T 4)** | 250 | 35.4 [29.7, 41.5] | +24.2 ± 6.9 (z = +6.85) | -1.57 ± 0.58 | +4.82 ± 0.96 |

### Points per card type of the focus agent, 500 ms (v2 vs final, same deals)
| Card type | f5_ref.csv | f5_finalT4.csv | paired Δ (95% CI) |
|---|---|---|---|
| Maki | 9.65 | 7.76 | -1.88 ± 0.51 |
| Tempura | 6.21 | 6.47 | +0.26 ± 0.49 |
| Sashimi | 5.86 | 3.18 | -2.68 ± 0.70 |
| Dumpling | 4.86 | 6.75 | +1.89 ± 0.50 |
| SquidNigiri | 5.65 | 6.46 | +0.81 ± 0.61 |
| SalmonNigiri | 8.34 | 8.20 | -0.14 ± 0.58 |
| EggNigiri | 1.44 | 1.62 | +0.18 ± 0.16 |
| Wasabi | 0.00 | 0.00 | +0.00 ± 0.00 |
| Chopsticks | 0.00 | 0.00 | +0.00 ± 0.00 |
| Pudding | 1.09 | 2.39 | +1.30 ± 0.28 |
| TOTAL | 40.30 | 42.13 | +1.83 ± 0.60 |

### Points per card type of the focus agent, 900 ms (v2 vs final, same deals)
| Card type | f9_ref.csv | f9_finalT4.csv | paired Δ (95% CI) |
|---|---|---|---|
| Maki | 10.15 | 7.52 | -2.63 ± 0.71 |
| Tempura | 5.40 | 6.34 | +0.94 ± 0.65 |
| Sashimi | 6.16 | 4.68 | -1.48 ± 1.06 |
| Dumpling | 4.99 | 6.35 | +1.36 ± 0.69 |
| SquidNigiri | 5.41 | 5.80 | +0.38 ± 0.76 |
| SalmonNigiri | 7.85 | 7.74 | -0.10 ± 0.79 |
| EggNigiri | 1.61 | 1.62 | +0.01 ± 0.23 |
| Wasabi | 0.00 | 0.00 | +0.00 ± 0.00 |
| Chopsticks | 0.00 | 0.00 | +0.00 ± 0.00 |
| Pudding | 0.98 | 2.16 | +1.18 ± 0.37 |
| TOTAL | 39.68 | 41.25 | +1.57 ± 0.88 |

### 2^4 factorial analysis: R, M, K 0.5 and T 8 on top of HBDP (100 ms, seed 1, 16 cells x 500 paired games)

Cells (win % over 500 games):
| R | M | K 0.5 | T 8 | Win % | Score margin |
|---|---|---|---|---|---|
|  |  |  |  | 27.3 | -2.60 |
|  |  |  | ✓ | 32.7 | -1.65 |
|  |  | ✓ |  | 26.8 | -2.91 |
|  |  | ✓ | ✓ | 36.8 | -1.59 |
|  | ✓ |  |  | 28.5 | -2.94 |
|  | ✓ |  | ✓ | 35.1 | -1.44 |
|  | ✓ | ✓ |  | 35.7 | -1.37 |
|  | ✓ | ✓ | ✓ | 45.2 | -0.22 |
| ✓ |  |  |  | 39.0 | -1.19 |
| ✓ |  |  | ✓ | 40.2 | -1.00 |
| ✓ |  | ✓ |  | 41.3 | -1.16 |
| ✓ |  | ✓ | ✓ | 42.2 | -1.30 |
| ✓ | ✓ |  |  | 39.4 | -1.01 |
| ✓ | ✓ |  | ✓ | 39.9 | -1.09 |
| ✓ | ✓ | ✓ |  | 50.1 | +0.02 |
| ✓ | ✓ | ✓ | ✓ | 45.2 | -0.09 |

Effects (main effect = mean over cells with the factor on minus off; interactions in the same units):
| Effect | Δ win (points) | z | Δ score margin | z |
|---|---|---|---|---|
| R | +8.7 ± 2.1 | +8.06 | +0.99 ± 0.24 | +8.15 |
| M | +4.1 ± 2.0 | +4.04 | +0.66 ± 0.23 | +5.71 |
| K | +5.2 ± 2.2 | +4.69 | +0.54 ± 0.23 | +4.58 |
| T | +3.6 ± 2.0 | +3.54 | +0.60 ± 0.24 | +4.95 |
| R × M | -1.1 ± 2.1 | -1.06 | -0.04 ± 0.24 | -0.32 |
| R × K | -0.1 ± 2.1 | -0.08 | -0.10 ± 0.22 | -0.85 |
| R × T | -4.2 ± 2.1 | -3.97 | -0.63 ± 0.23 | -5.51 |
| M × K | +3.2 ± 2.2 | +2.83 | +0.67 ± 0.24 | +5.46 |
| M × T | -0.7 ± 2.1 | -0.67 | +0.02 ± 0.24 | +0.13 |
| K × T | +0.2 ± 2.0 | +0.22 | -0.04 ± 0.23 | -0.36 |

Reading the effects: a main effect is the average change in win rate (or score margin) when the change is switched
on, averaged over all settings of the other three. An interaction A × B is half the difference between the effect
of A with B on and with B off (Yates convention): R × T = −4.2 means that R adds about 12.9 points with T 4 but only
4.5 with T 8; both changes repair the same weakness (an optimistic value of unfinished sets in the middle of a round).
M × K = +3.2: max^n only pays off when K is small enough for the per-player values to steer the selection.
The best cell is R + M + K 0.5 with T 4 (50.1 %, against 45.2 % for the T 8 candidate). Because one cell of 16 can
be lucky, T 4 and T 8 were compared again on two new seed sets (tie-break above): T 4 was better on all three
seed sets (pooled +3.9 ± 3.4 points, z = 2.2), so the final agent uses the default heuristic (T 4).

### Timing (900 ms budget, focus = candidate T 8, fully loaded machine: 16 other games running)

| Setting | Decisions | CPU ms mean / max | Wall ms mean / p95 / max | Wall > 1000 ms |
|---|---|---|---|---|
| no wall-clock guard | 97 | 898.6 / 899.3 | 998.0 / 1117.5 / 1224.7 | 42 |
| guard 950 ms | 73 | 853.9 / 899.1 | 944.2 / 959.5 / 991.1 | 0 |

TAG's own per-player clock (`ElapsedCpuChessTimer`) measures CPU time like the budget, so the CPU budget is
safe under that clock. The guard only matters if a harness measures real time on a busy machine.

### Iterations per decision (100 ms, 3 games, `src/arena/Probe.java`)

| Agent | Iterations / decision | Determinisations / decision |
|---|---|---|
| v2 (H+A+B+D) | 2596 | 1.58 |
| HBDP | 3509 | 1.58 |
| candidate T 8 − R | 2857 | 1.62 |
| candidate T 8 | 2448 | 1.62 |

Rolling out to the end of the round costs about 14 % of the iterations; dropping A and adding P had made
the tree cheaper, so the agent runs about as many iterations as v2 (T only changes the leaf evaluation).
