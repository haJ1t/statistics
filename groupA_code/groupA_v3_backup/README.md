# groupA – SushiMCTS agent for Sushi Go! (ECS7032P Assignment 1)

> **Rename first:** replace every `groupA` (package line of the 5 `.java` files, the folder name,
> and the `class` / `heuristic` entries of the JSON files) with your real group letter, e.g. `groupK`.

## What the agent is

`SushiMCTSPlayer` is a modified copy of the framework's **`players.basicMCTS.BasicMCTSPlayer`**
(an allowed base agent). It does **not** use, copy or import anything from the advanced
`players.mcts` package (`MCTSPlayer`, `SingleTreeNode`, `MCTSParams`, `MCTSEnums`), and it does not
change any framework file. Every modification is a switch in `SushiMCTSParams`, so all variants of the
ablation study run the same code.

| File | Copied from | Purpose |
|---|---|---|
| `SushiMCTSPlayer.java` | `BasicMCTSPlayer` | entry point; Modification D (information-aware determinisation) and the wall-clock guard in `_getAction` |
| `SushiTreeNode.java` | `BasicTreeNode` | MCTS tree; Modifications A, B, P, O, M, R |
| `SushiMCTSParams.java` | `BasicMCTSParams` | all original parameters + on/off switches for the ablation study |
| `SushiGoHeuristic.java` | new | state evaluation H(s, i) (constants configurable from JSON) |
| `CardValueModel.java` | new | shared Sushi Go knowledge: estimated scores and marginal card values |

## Modifications

| Id | Name | Where | In final agent |
|---|---|---|---|
| H | Sushi Go heuristic `tanh(Δ/σ)`, Δ = 0.7·(lead over best opponent) + 0.3·(lead over mean opponent) | `SushiGoHeuristic` | yes |
| A | Progressive bias from marginal card values | `SushiTreeNode.expand/ucb` | **no** (no gain: −1.2 ± 4.3 win points when added to the final agent, 1000 paired games) |
| B | ε-greedy informed rollouts (every player plays its best marginal card, ε = 0.2 random) | `SushiTreeNode.playOut` | yes |
| D | Information-aware determinisation (1–4 trees, depending on how many opponent hands are unknown) | `SushiMCTSPlayer._getAction` | yes |
| P | Action pruning: two copies of the same card lead to the same state, so one branch per distinct card | `SushiTreeNode.setState`, `CardValueModel.distinctActions` | yes |
| O | Opponent model in the tree (opponents play the card-value policy instead of being searched) | `SushiTreeNode.treePolicy` | **no** (−16.9 ± 5.8 win points when added to the T 8 candidate) |
| M | max^n backups (Sturtevant 2008): a value per player is backed up and whoever moves maximises its *own* value, instead of the paranoid "opponents minimise our value" | `SushiTreeNode.ucb/backUpAll` | yes |
| R | Rollouts to the end of the current round: the rollout stops when the round has been scored (Maki and all sets exact), not after 10 moves | `SushiTreeNode.finishRollout` | yes |

R is the largest gain: removing it from the final agent costs 13.2 ± 3.5 win points (1500 paired games,
z = −7.5). With R the heuristic only has to estimate Pudding and the next rounds, not the outcome of
half-finished sets in the current round.

**Safety (not an AI change).** The time budget is CPU time, as in every TAG agent (`ElapsedCpuTimer`).
On a busy machine a decision can take longer in real time; with `wallTimeLimitMS > 0` the search also
stops after that many milliseconds of wall-clock time. Measured at budget 900 ms on a fully loaded
machine (16 other games running): CPU time per decision at most 899 ms; wall time without the guard up
to 1225 ms (42 of 97 decisions over 1 s), with a 950 ms guard at most 991 ms (none over 1 s). The
competition file uses 925 ms for extra margin. Without competing load, real time is only slightly above
CPU time, so the 900 ms CPU budget normally runs out first and the guard rarely triggers.

## How to construct the agent in code

```java
SushiMCTSParams params = new SushiMCTSParams();
params.heuristic = new groupA.SushiGoHeuristic(); // defaults: sigma 15, leaderWeight 0.7, alpha 0.5, T 4
params.K = 0.5;
params.useInformedRollout = true;   // B
params.useDeterminisation = true;   // D
params.useActionPruning = true;     // P
params.useMaxN = true;              // M
params.rolloutToRoundEnd = true;    // R
params.budgetType = PlayerConstants.BUDGET_TIME;
params.budget = 900;                // ms of CPU time per decision
params.wallTimeLimitMS = 925;       // hard real-time limit per decision
AbstractPlayer agent = new SushiMCTSPlayer(params);
```

or from JSON: `PlayerFactory.createPlayer("src/main/java/groupA/json/competition/SushiMCTS_1000ms.json")`.

## Parameters

| JSON key | Default | Final | Meaning |
|---|---|---|---|
| `K` | 1.41 | 0.5 | UCB1 exploration constant |
| `rolloutLength` | 10 | 10 | rollout steps after leaving the tree (ignored when `rolloutToRoundEnd` is true) |
| `maxTreeDepth` | 100 | 100 | maximum tree depth |
| `heuristic` | – | `{"class": "groupA.SushiGoHeuristic"}` | default arguments; or with `"args": [sigma, leaderWeight, alpha, T]` |
| `useProgressiveBias` | false | false | Modification A |
| `biasWeight` | 1.0 | – | W in `W * b(a) / (n + 1)` |
| `priorScale` | 5.0 | – | `b(a) = tanh(mv(a) / priorScale)` |
| `useInformedRollout` | false | true | Modification B |
| `rolloutEpsilon` | 0.2 | 0.2 | probability of a random rollout move |
| `useDeterminisation` | false | true | Modification D |
| `nDeterminisations` | 4 | 4 | maximum number of determinisations M_max |
| `adaptiveDeterminisation` | true | true | M depends on number of unknown opponent hands (false = always M_max while any hand is unknown) |
| `useActionPruning` | false | true | Modification P |
| `useOpponentModel` | false | false | Modification O |
| `opponentEpsilon` | 0.2 | – | O: probability that a modelled opponent plays a random card |
| `useMaxN` | false | true | Modification M |
| `rolloutToRoundEnd` | false | true | Modification R |
| `wallTimeLimitMS` | 0 (off) | 925 | hard wall-clock limit per decision (BUDGET_TIME only) |
| `budgetType`, `budget`, `breakMS` | – | TIME, 900, 0 | as for every TAG agent |

Heuristic arguments (`"args"` of `groupA.SushiGoHeuristic`; the final agent uses the defaults `[15, 0.7, 0.5, 4]`):

| | Meaning |
|---|---|
| σ (`scoreScale`) | score gap in points that maps to tanh(1) = 0.76 |
| `leaderWeight` | weight of the lead over the best opponent (rest: lead over the mean opponent) |
| α (`potentialWeight`) | weight of the value of sets that are not complete yet |
| T (`completionHorizon`) | picks left at which completing a set counts as likely, c(r, m) = min(1, (r − m + 1)/T); the card values of B and A use the same constants |

With every new key at its default the agent runs exactly the previous version (v2): all new code is
behind the switches, and `experiments_v3/src/check/Equiv.java` confirms that heuristic and card values
are identical to v2 (168 125 checks in 400 random games, largest difference 0).

## Competition configuration

* `json/competition/SushiMCTS_1000ms.json` – 1 s per decision (budget 900 ms CPU, wall-clock guard 925 ms).
* `json/competition/SushiMCTS_500ms.json` – 500 ms per decision (budget 430 ms CPU, wall-clock guard 450 ms).

The guidelines mention both settings (3 players / 1 s and 4 players / 500 ms): use the one the
module team confirms. Submit only one file as *the* competition JSON.

## Results

All numbers: Sushi Go in TAG, 3 players and 500 games per condition unless stated, CPU-time budget per
decision. Our agent plays against **v2** (the previous version of this agent, H+A+B+D) and **TAG's tuned
ISMCTS** (`players.mcts` configuration, used only as a benchmark opponent; nothing of it is in this
folder). Game *g* uses game seed *seed + g* and our agent sits in seat *g* mod *n*, so two conditions with
the same seed play the **same deals** and are compared game by game (paired). A shared first place
counts 1/k; chance = 33.3 % (3 players), 25 % (4 players). Brackets: Wilson 95 % CI; ± : 95 % CI of the
paired difference. *Score margin* = our score minus the best opponent's score.

### v2 vs final (v3)

| Setting | Deals (seed) | v2 win % | final win % | Δ win (paired) | v2 margin | final margin |
|---|---|---|---|---|---|---|
| 100 ms | 1 | 24.8 [21.2, 28.8] | **50.1** [45.7, 54.5] | +25.3 ± 5.6 (z = 8.8) | −3.40 | +0.02 |
| 100 ms | 1001 | 22.9 [19.4, 26.8] | **49.4** [45.0, 53.8] | +26.5 ± 5.8 (z = 8.9) | −3.42 | −0.02 |
| 100 ms | 2001 | 23.5 [20.0, 27.4] | **47.5** [43.1, 51.8] | +24.0 ± 5.7 (z = 8.2) | −3.42 | −0.10 |
| 100 ms | 3001 | 24.4 [20.8, 28.4] | **45.1** [40.8, 49.5] | +20.7 ± 5.6 (z = 7.3) | −3.29 | −0.61 |
| 100 ms | pooled (2000 games) | 23.9 | **48.0** | **+24.1 ± 2.9 (z = 16.6)** | −3.38 | −0.18 |
| 500 ms | 1 | 18.3 [15.2, 21.9] | **40.7** [36.4, 45.0] | +22.4 ± 5.5 (z = 7.9) | −4.26 | −0.96 |
| 900 ms | 1 (250 games) | 18.8 [14.4, 24.1] | **32.8** [27.3, 38.8] | +14.0 ± 7.3 (z = 3.7) | −3.93 | −1.86 |
| 4 players, 100 ms | 1 | 16.0 [13.0, 19.5] | **38.6** [34.4, 42.9] | +22.6 ± 5.2 (z = 8.5) | −5.38 | −1.15 |
| 4 players, 500 ms | 1 (250 games) | 11.2 [7.9, 15.7] | **35.4** [29.7, 41.5] | +24.2 ± 6.9 (z = 6.9) | −6.39 | −1.57 |

At 500 ms TAG's ISMCTS wins 59.3 % of the games against v2 + v2, but only 40.0 % against final + v2:
the final agent (40.7 %) is level with it. The T 8 candidate scored the same at 500 ms (40.5 %; final −
candidate +0.2 ± 5.9), so at this budget the choice of T makes no measurable difference.

The gain over v2 is smaller at 900 ms (+14.0 points against +22 to +25 at 100 and 500 ms). TAG's ISMCTS
profits more from extra time than our agent: at the same table (final, v2, ISMCTS) it wins 31.5 % at
100 ms (mean of four seed sets), 40.0 % at 500 ms and 46.0 % at 900 ms. Two changes that might help at long
budgets were tested at 500 ms (500 paired games each, same deals as the final agent) and gave nothing:
exploration K 1.0 (−0.3 ± 5.9 win points) and uniformly random rollouts instead of B (−1.4 ± 5.9; own
score −0.8 ± 0.6). The final agent therefore uses the same settings at every budget.

With 4 players (chance 25 %) TAG's ISMCTS wins 41.4 % of its seats against v2s at 100 ms and 48.8 % at
500 ms; with the final agent at the table it wins 27.9 % and 34.6 %, while the final agent wins 38.6 % and
35.4 %.

### How the final agent was found (100 ms, seed 1, paired with v2)

| Agent | Win % | Δ win vs v2 | Score margin |
|---|---|---|---|
| v2 = H+A+B+D | 24.8 [21.2, 28.8] | – | −3.40 |
| + P | 28.1 [24.3, 32.2] | +3.3 ± 5.4 | −2.90 |
| + P + O | 23.3 [19.8, 27.2] | −1.5 ± 5.0 | −3.24 |
| + P − A (= HBDP) | 27.3 [23.6, 31.4] | +2.5 ± 5.3 | −2.60 |
| HBDP + rollouts to game end (`rolloutLength` 100) | 34.0 [30.0, 38.3] | +9.2 ± 5.4 | −1.91 |
| HBDP + **R** | 39.0 [34.8, 43.3] | +14.2 ± 6.0 (z = 4.6) | −1.19 |
| HBDP + R, K 0.5, T 8 | 42.2 [37.9, 46.6] | +17.4 ± 5.7 | −1.30 |
| HBDP + R + **M**, K 0.5, T 8 = candidate | 45.2 [40.9, 49.6] | +20.4 ± 5.7 (z = 7.0) | −0.09 |
| HBDP + R + M, K 0.5, T 4 = **final** | **50.1** [45.7, 54.5] | +25.3 ± 5.6 (z = 8.8) | +0.02 |

1. **Screening.** One change at a time on top of the best agent so far gave the candidate
   HBDP + R + M with K 0.5 and T 8. Rolling out to the end of the *game* is clearly worse than to the end
   of the *round*: two more rounds of random-ish play add more noise than they remove. O made the agent
   weaker; A gave nothing once P was on (v2 + P 28.1 % vs HBDP 27.3 %), so both are off.
2. **Leave-one-out ablation** of the candidate (table below): H is essential and O must stay off; D, M,
   P, B and K 0.5 each add a little score margin (0.6 to 1.2 points per game). But removing R cost nothing
   (0.0 ± 6.2), although adding R to HBDP had gained 11.7 points.
3. **2^4 factorial** over R, M, K and T (16 agents on the same 500 deals): R and T 8 do part of the same
   job, so with T 8 the agent hardly needs R and vice versa. The best of the 16 cells was R + M + K 0.5
   with the default T 4.
4. **Tie-break.** One cell of 16 can be lucky, so T 4 and T 8 were played again on two new seed sets:
   T 4 was better on all three (+4.9, +3.8, +2.9 win points), pooled +3.9 ± 3.4 (z = 2.2, 1500 paired
   games). The final agent therefore uses the default heuristic (T 4). A later check on a fourth seed set
   (1001) agreed: +7.5 ± 5.9; all four pooled +4.8 ± 3.0 (z = 3.2, 2000 paired games).

* Sensitivity of the other constants (one change each, win points paired with HBDP + R, 95 % CI about
  ±6): α = 1 −8.0 (z = −2.6, the only significant one), ε = 0.1 −4.7, leaderWeight 0.5 −4.1, K 0.3 + T 8
  −4.5, no D −1.1, ε = 0.3 +0.4, T = 8 +1.2, σ = 8 +1.7, K = 0.5 +2.3, K 0.5 + T 8 +3.2. So the defaults of
  H were already close to the best.

### 2^4 factorial: R, M, K 0.5 and T 8 on top of HBDP (100 ms, seed 1, 16 × 500 paired games)

| Effect | Δ win (points) | z | Δ score margin | z |
|---|---|---|---|---|
| R (rollouts to the round end) | +8.7 ± 2.1 | +8.1 | +0.99 ± 0.24 | +8.2 |
| K 0.5 instead of 1.41 | +5.2 ± 2.2 | +4.7 | +0.54 ± 0.23 | +4.6 |
| M (max^n) | +4.1 ± 2.0 | +4.0 | +0.66 ± 0.23 | +5.7 |
| T 8 instead of 4 | +3.6 ± 2.0 | +3.5 | +0.60 ± 0.24 | +5.0 |
| R × T | −4.2 ± 2.1 | −4.0 | −0.63 ± 0.23 | −5.5 |
| M × K | +3.2 ± 2.2 | +2.8 | +0.67 ± 0.24 | +5.5 |
| R × M, R × K, M × T, K × T | −1.1 … +0.2 | \|z\| ≤ 1.1 | −0.10 … +0.02 | \|z\| ≤ 0.9 |

A main effect is the average change when the factor is switched on, over all settings of the other three;
an interaction A × B is half the difference between the effect of A with B on and with B off. R × T = −4.2:
R adds 12.9 points with T 4 but only 4.5 with T 8. Without R the rollout stops in the middle of a round
and the heuristic has to value unfinished sets, where the cautious T 8 helps; with R the leaf is the end of
the round, every set is already scored, and T only steers the informed rollouts (B), where T 4 plays
better. M × K = +3.2: max^n only pays off when K is small enough for the per-player values to steer the
selection.

### Tie-break: T 4 vs T 8 on new deals (100 ms)

| Deals (seed) | v2 | candidate T 8 | **final T 4** | Δ T 4 − T 8 (paired) | T 4 + A | T 4 − R |
|---|---|---|---|---|---|---|
| 1 | 24.8 | 45.2 | **50.1** | +4.9 ± 6.0 | – | 35.7 |
| 2001 | 23.5 | 43.7 | **47.5** | +3.8 ± 6.1 | 42.1 | 33.8 |
| 3001 | 24.4 | 42.2 | **45.1** | +2.9 ± 5.7 | 48.1 | 33.7 |
| pooled (tie-break) | | | | **+3.9 ± 3.4 (z = 2.2)** | −1.2 ± 4.3 vs T 4 | −13.2 ± 3.5 vs T 4 (z = −7.5) |
| 1001 (later check) | 22.9 | 41.9 | **49.4** | +7.5 ± 5.9 | – | – |
| pooled, all four | | | | +4.8 ± 3.0 (z = 3.2) | | |

### Leave-one-out ablation of the T 8 candidate (100 ms, seed 1, 500 paired games each)

| Change | Win % | Δ win (paired) | Δ score margin |
|---|---|---|---|
| none (candidate, T 8) | 45.2 [40.9, 49.6] | – | – |
| − H (TAG's default Sushi Go heuristic instead: own score / 50) | 25.2 [21.6, 29.2] | −20.0 ± 5.9 (z = −6.6) | −5.42 ± 0.77 |
| + O | 28.3 [24.5, 32.4] | −16.9 ± 5.8 (z = −5.7) | −2.74 ± 0.66 |
| K 1.41 instead of 0.5 | 39.9 [35.7, 44.3] | −5.3 ± 6.2 | −1.00 ± 0.63 |
| − D | 41.9 [37.7, 46.3] | −3.3 ± 6.2 | −0.76 ± 0.63 |
| − M | 42.2 [37.9, 46.6] | −3.0 ± 6.1 | −1.21 ± 0.58 |
| − P | 43.1 [38.8, 47.5] | −2.1 ± 6.2 | −0.62 ± 0.63 |
| − R | 45.2 [40.9, 49.6] | +0.0 ± 6.2 | −0.13 ± 0.64 |
| − B | 45.7 [41.4, 50.1] | +0.5 ± 6.1 | −0.81 ± 0.65 |
| + A | 48.1 [43.8, 52.5] | +2.9 ± 6.3 | −0.04 ± 0.61 |
| T 4 instead of 8 (= **final**) | 50.1 [45.7, 54.5] | +4.9 ± 6.0 | +0.11 ± 0.61 |

### What changed in the play (500 ms, points per game of our agent, same deals)

| | Maki | Tempura | Sashimi | Dumpling | Nigiri (all) | Pudding | Total |
|---|---|---|---|---|---|---|---|
| v2 | 9.65 | 6.21 | 5.86 | 4.86 | 15.42 | 1.09 | 40.30 |
| final | 7.76 | 6.47 | 3.18 | 6.75 | 16.27 | 2.39 | 42.13 |
| paired Δ | −1.88 ± 0.51 | +0.26 ± 0.49 | −2.68 ± 0.70 | +1.89 ± 0.50 | +0.85 ± 0.75 | +1.30 ± 0.28 | +1.83 ± 0.60 |

The final agent scores fewer points where it has to win a race or finish a triple (Maki, Sashimi) and
more from Dumplings, Pudding and Nigiri. Its own score rises by 1.8 points per game, its margin over the
best opponent by 3.3 points.

Full tables, the raw per-game data and the benchmark tools are in `experiments_v3/` (next to this folder,
not part of the submission); `experiments_v3/RESULTS.md` has every table.

## Running the experiments

From the TAG root folder (working directory = repository root):

```
java -cp <classpath> evaluation.RunGames config=src/main/java/groupA/json/runs/E1_HA.json
```

Each `runs/*.json` file is a RunGames configuration (game, player count, focus player,
opponents, number of games, output folder). See the plan document, section 6.
The agent files of the v3 study are in `json/agents_v3/` (same settings as in the results above).

## TAG version

Requires a TAG version from November 2024 or later (`SGGameState.isHandKnown`). In older
versions the method was called `hasSeenHand` – pull the latest framework instead of renaming.
