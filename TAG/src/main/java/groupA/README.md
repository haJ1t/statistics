# groupA – SushiMCTS agent for Sushi Go! (ECS7032P Assignment 1)

> **Rename first:** replace every `groupA` (package line of the 7 `.java` files, the folder name,
> and the `class` / `heuristic` entries of the JSON files) with your real group letter, e.g. `groupK`.

This is **version 5** of the agent. Version 5 is the sprint before the in-class tournament (see
[Version 5](#version-5--sprint-for-the-in-class-tournament-27-october) below): it adds one more switch (N, denial,
off by default) and tested it together with five other ideas, none of which beat version 4, so **the competition
files still play exactly as version 4**. Version 3 (H + B + D + P + M + R, K 0.5) is also still inside unchanged: every new idea is a
switch in `SushiMCTSParams` that is off by default, and the final JSON files switch on the ones that were kept.

**Win rate of the final agent** against v2 (our previous agent) and TAG's tuned ISMCTS on seed block 7 (game
seeds 7–506; these deals overlap the development deals of seeds 1 and 2). The held-out validation on seed block
100 000, which shares no deal with any earlier run, is in `experiments_v5/RESULTS.md` and in the report:

| Setting | Games | **v4 (final)** win % [95 % CI] | Score margin | for comparison: v3, development deals |
|---|---|---|---|---|
| 3 players, 100 ms | 500 | **55.0** [50.6, 59.3] | +1.00 ± 0.44 | 50.1 (44.8 on the validation deals) |
| 3 players, 500 ms | 500 | **49.4** [45.0, 53.8] | +0.40 ± 0.39 | 40.7 |
| 3 players, 900 ms | 250 | **56.0** [49.8, 62.0] | +1.06 ± 0.62 | 32.8 |
| 4 players, 100 ms | 500 | **46.8** [42.5, 51.2] | −0.28 ± 0.46 | 38.6 |
| 4 players, 500 ms | 500 | **41.4** [37.2, 45.8] | −1.53 ± 0.46 | 35.4 |

Chance is 33.3 % with 3 players and 25 % with 4. *Score margin* = our score minus the best opponent's score
(points per game, ± 95 % CI). Our agent sits in every seat equally often; the other seats alternate between
v2 and TAG's ISMCTS.

## Version 5 – sprint for the in-class tournament (27 October)

Before the in-class tournament we had one 4.5-hour session to improve the agent further, on a 4-core machine
(the v4 study used an 18-core laptop), within the module rules: everything stays in this package, nothing from
`players.mcts` is used, the base agent is still `BasicMCTSPlayer`, and the competition files keep the 1 s / 500 ms
limits with the wall-clock guard. Every v5 idea is again a switch that is off by default, so **v4 is still inside
v5 unchanged** (with the 4-player competition settings the v4 and v5 builds make exactly the same decisions on fixed states, `EquivF`). Tools, agent files and per-game results are in `experiments_v5/`.

The v4 agent was first re-run on this machine (3 players, 100 ms, seed 7, the same deals as the v4 validation):
58.1 % [53.7, 62.3], score margin +1.16 ± 0.42, in line with 55.0 % / +1.00 on the old machine (paired difference
+3.1 ± 5.8 win points). Every candidate below played the same 500 deals and seats (paired comparison).

**Ideas tested** (3 players, 100 ms, seed 7, 500 games each, opponents v2 and TAG's ISMCTS as in v4):

| Agent | Games | Win % [95 % CI] | Score margin | Δ win vs base (paired) | Δ margin (paired) |
|---|---|---|---|---|---|
| v4 (final agent, base) | 500 | 58.1 [53.7, 62.3] | +1.16 ± 0.42 | base | base |
| + X3 (3-player card model) | 500 | 56.8 [52.4, 61.1] | +0.59 ± 0.48 | -1.3 ± 5.8 (z = -0.4) | -0.57 ± 0.60 (z = -1.9) |
| + N, λ 0.5 (denial) | 500 | 56.3 [51.9, 60.6] | +0.84 ± 0.47 | -1.8 ± 5.8 (z = -0.6) | -0.32 ± 0.59 (z = -1.1) |
| + V2D (terminal value scale 2, σ decay 0.5) | 500 | 57.5 [53.1, 61.8] | +0.57 ± 0.46 | -0.6 ± 5.8 (z = -0.2) | -0.60 ± 0.56 (z = -2.1) |
| + W1 (leader weight w 1.0) | 500 | 58.8 [54.4, 63.0] | +1.07 ± 0.47 | +0.7 ± 5.8 (z = +0.2) | -0.09 ± 0.58 (z = -0.3) |
| + R3V2 (rollouts to the game end, terminal value scale 2) | 500 | 34.4 [30.4, 38.7] | -2.42 ± 0.54 | -23.7 ± 5.8 (z = -8.0) | -3.58 ± 0.65 (z = -10.9) |

* **X3 – card-model constants tuned for 3 players.** With the v4 search-free tuner (`GreedyTune`, greedy card-value
  policies against each other, 20 000 deals per evaluation) we tuned the nine card-model constants for 3 players:
  X3 = α 0.5, T 4, β 0.8, T_m 4, κ 0.25, δ 1.0, π 1.5, ν 4, γ 3. As a *greedy policy* X3 is far stronger than the
  v4 3-player model (which wins only 7 % of greedy games against the default and X0 models) and than X0 (45.0 %
  against two X0 players, chance 33.3 %; `experiments_v5/results/greedy_duels.txt`). Inside the search it did
  **not** help (−1.3 ± 5.8 win points, score margin −0.57 ± 0.60, z = −1.9). Together with v4's X0 result at
  3 players this shows that the strength of the rollout policy is not what limits the agent: a rollout policy
  only has to give unbiased leaf values and a plausible model of the opponents, not to play well itself
  (simulation balancing, Silver and Tesauro 2009).
* **N – denial ("hate drafting"), new switch `rolloutDenial`.** The cards a player leaves in its hand go to the
  next player (TAG passes hand i to player i − 1). With N every simulated player adds λ times the loss of the next
  player to the value of that player's favourite card of the hand: λ · max(0, v_q(c) − max_{c' ≠ c} v_q(c')),
  with v_q the next player's marginal card value (`CardValueModel.addDenial`, used by the rollout policy). In greedy
  duels it adds about one win point at 3 players (λ 0.25–1); in the search it did not help (λ 0.5: −1.8 ± 5.8 win
  points, margin −0.32 ± 0.59).
* **V2D – value shape aimed at winning** (the 4-player W2 ideas, for 3 players): finished games valued by the gap to
  the winner, tanh((S_i − max_j S_j) / 2) (`terminalScale` 2), and σ multiplied by (R / 2)^0.5 with R rounds left
  (`sigmaRoundDecay` 0.5). Same win rate (−0.6 ± 5.8), lower score margin (−0.60 ± 0.56, expected when the value
  stops rewarding the size of a win).

* **W1 – only the lead over the best opponent counts** (w 1.0 instead of 0.7 in H): no difference (+0.7 ± 5.8 win
  points, margin −0.09 ± 0.58).
* **X3 with 4 players** (in the `fourPlayer` block of the 100 ms agent, instead of X0): 48.8 % [44.4, 53.2] against
  46.8 % for v4 on the same 500 deals (v4 validation run of the old machine): +2.0 ± 5.7 win points, margin
  −0.24 ± 0.57 – no clear gain, and this machine gave v4 itself +3.1 points at 3 players.

* **R3V2 – rollouts to the end of the game, valued by the gap to the winner** (`rolloutRounds` 3, `terminalScale` 2),
  so that every leaf value is about winning the game: **−23.7 ± 5.8 win points (z = −8.0)**. Simulating the later
  rounds with the rollout policy makes the leaf values far noisier than the round-end estimate of H, and the
  sharp terminal value (scale 2) adds more variance. This confirms the v3 result that rollouts must stop at the
  end of the round (R).
* K 0.35 was planned but not run: in the v3 study K 0.3 lost clearly to K 0.5 (34.5 % vs 42.2 %), and v4 found
  K 0.8 worse than 0.5, so 0.5 is close to the best value.

**Outcome of the sprint.** None of the ideas beat v4, so the competition files are unchanged and play exactly as
v4; N stays in the code as a switch that is off. The sprint still tells us where the agent's limits are *not*:
a much stronger rollout policy (X3), denial in the rollouts (N) and a value shape aimed at winning (V2D, W1) all
leave the win rate where it was (differences within ± 2 win points), and longer rollouts are much worse.

**The competition files on this machine.** Both files were played unchanged at their real limits (wall-clock guard
on, 3 game threads on the 4 cores, opponents with the same budget), on the deals of the v4 validation runs:

| File | Players | Games | Win % [95 % CI] | v4 validation run, same deals |
|---|---|---|---|---|
| `SushiMCTS_1000ms.json` | 3 | 135 | 51.1 [42.8, 59.4] | 52.6 % (900 ms, old machine) |
| `SushiMCTS_500ms.json` | 4 | 40 | 46.3 [31.8, 61.3] | 48.8 % (500 ms, old machine) |

Against a stronger field – the v4 agent against **two** TAG ISMCTS agents (3 players, 100 ms, seed 7, 300 games) – it won **54.2 %** [48.5, 59.7] (score margin +1.66 ± 0.69; each ISMCTS seat 22.9 %, chance 33.3 %), about as often as against one ISMCTS and v2, so its lead does not depend on the weaker v2 opponent.

**Timing on a small machine.** Every decision of our agent in 3 games per file (`arena.WallProbe`, the other
experiments paused), with the JVM's default settings (G1, default heap), as a plain TAG run gets them:

| File | Players | CPU time per decision: mean / max | Wall-clock: median / 95 % / max |
|---|---|---|---|
| 1000 ms | 3 | 845 / 900 ms | 925 / 926 / 1004 ms (1 of 73 decisions over 1 s) |
| 500 ms | 4 | 418 / 453 ms | 441 / 451 / 472 ms (none over 500 ms) |

With the probe settings of v4 (2 GB heap, ParallelGC with 2 threads) the wall-clock tail on this machine is much
longer (3 players: 95 % 1085 ms, max 1455 ms, 5 of 73 decisions over 1 s; 4 players: max 766 ms, 2 of 66 over
500 ms) while the CPU time stays within the budget: the long decisions are garbage-collection pauses of a small
heap, which hit every agent in the JVM. TAG's own time check counts CPU time, which never exceeded the budget.
Lowering the 1 s file to 850 ms (budget and guard) left the single decision just over 1 s (1002 ms), so that one is
most likely a one-off JVM pause (class loading or compilation early in the run) rather than the search, and fast
simulation (F) did not shorten the tail either; the competition files therefore keep v4's limits
(`experiments_v5/results/probe_*.txt`). On a busy or small machine, give the JVM a larger heap (for example
`-Xmx4g`) or keep TAG's default G1 settings.

## What the agent is

`SushiMCTSPlayer` is a modified copy of the framework's **`players.basicMCTS.BasicMCTSPlayer`** (an allowed
base agent). It does **not** use, copy or import anything from the advanced `players.mcts` package
(`MCTSPlayer`, `SingleTreeNode`, `MCTSParams`, `MCTSEnums`), and it does not change any framework file
(TAG's own ISMCTS appears only as a benchmark opponent, outside this folder).

| File | Copied from | Purpose |
|---|---|---|
| `SushiMCTSPlayer.java` | `BasicMCTSPlayer` | entry point: parameters per table size (`fourPlayer`), wall-clock guard, D, E, Q |
| `SushiTreeNode.java` | `BasicTreeNode` | the search: A, B, P, O, M, R (v3) and S, I, F, L, G, U, V, K (v4) |
| `SushiMCTSParams.java` | `BasicMCTSParams` | all original parameters + the switches of the study |
| `SushiGoHeuristic.java` | new | state evaluation H |
| `CardValueModel.java` | new | Sushi Go knowledge: estimated scores and marginal card values (used by H and B) |
| `SushiSim.java` | new | F: TAG's Sushi Go rules re-implemented on `int` arrays (checked move by move against the forward model) |
| `OpponentModel.java` | new | Q: card preferences of each opponent learned during a game (tested, off) |

## One decision of the final agent

1. **Sample (I).** Copy the position and deal every card we cannot see (opponent hands we have not seen yet and
   the draw pile) again at random.
2. **Select (S, M).** At each node (one node per turn) every player picks a card of its hand by UCB1 over its
   *own* statistics at that node; cards it has not tried there come first. The joint choice is played and leads
   to the child of that joint choice.
3. **Expand.** The first joint choice without a node adds one node.
4. **Roll out (B, R).** Every player plays its best card by the card-value model (a random card with
   probability 0.2) until the round has been scored. With 4 players at 500 ms or more: 8 rollouts from the
   leaf on the fast simulator, values averaged (F, L).
5. **Evaluate (H).** Each player gets tanh(Δ/σ) of its estimated final score against the others.
6. **Back up (M).** Each player's value is added to the card that player chose at every node of the path.

When the time is up the agent plays its card with the most visits at the root.

## Modifications

| Id | Modification | Where | Final agent |
|---|---|---|---|
| H | Sushi Go heuristic `tanh(Δ/σ)`, Δ = w·(lead over best opponent) + (1 − w)·(lead over mean opponent) | `SushiGoHeuristic` | yes |
| B | ε-greedy informed rollouts: every player plays its best marginal card, ε = 0.2 random | `SushiTreeNode.playOut` | yes |
| P | action pruning: one branch per distinct card | `CardValueModel.distinctActions` | yes |
| M | max^n backups (Sturtevant 2008): every player maximises its own value | `SushiTreeNode` | yes |
| R | rollouts to the end of the current round | `SushiTreeNode.finishRollout` | yes |
| **S** | **simultaneous moves – decoupled UCT** (Lanctot et al. 2013; Tak, Lanctot and Winands 2014). TAG asks the players of a turn one after another, so in v3's tree a later player "saw" the cards chosen before it in the same turn. With S there is one node per turn and each player selects by UCB over its own card statistics, as in the real game | `SushiTreeNode` | **yes** |
| **I** | **information sets** (single-observer ISMCTS; Cowling, Powley and Whitehouse 2012): every iteration starts from a new sample of the hidden cards and walks one shared open-loop tree (nodes keep statistics, no game state). Cards are identified by type; a card's exploration term counts the iterations in which the player held it. Replaces D | `informationSetIteration` | **yes** |
| **X** | **card-model constants for 4 players** (set X0, see below), found with a search-free tuner that plays greedy card-value policies against each other: Sashimi discount, Maki standing weight, Dumpling, Pudding and Nigiri values, counting the cards still in all hands | `CardValueModel.Config` | **4 players** |
| **W** | **value shape for 4 players** (W2): only the lead over the best opponent counts (w = 1); finished games valued by the gap to the winner (V, scale 2); σ shrinks in later rounds (σ·(R/2)^0.5, R = rounds left); opponents in max^n play for their own lead over the others' mean (`opponentScoreValue`); K = 1.0 | `SushiGoHeuristic`, `valuesOf` | **4 players** |
| **F** | **fast simulation**: iterations on `SushiSim` instead of copies of TAG's game state, 7–11× more iterations per second | `SushiSim`, `simIteration` | **4 players, ≥ 500 ms** |
| **L** | with F: 8 rollouts from every leaf, values averaged | `simLeafValues` | **4 players, ≥ 500 ms** |
| D | v3's information-aware determinisation (1–4 trees) | `SushiMCTSPlayer` | no (replaced by I) |
| A, O | progressive bias; opponents not searched | | no (v3 study) |
| G | MAST card values steer the rollouts | | no |
| U | one open-loop tree per player, keyed by its own cards | `multiTreeIteration` | no |
| Q | per-opponent card-type biases learned from its picks during the game | `OpponentModel` | no |
| E | ensemble: the budget is split between several independent trees whose root visits are summed (root parallelisation, Chaslot, Winands and van den Herik 2008, one tree after another) | `SushiMCTSPlayer` | no |
| K | opponents in the tree only consider their k best cards by the card model, fixed k or progressive widening k = ⌈(n + 1)^a⌉ (Coulom 2007; Chaslot et al. 2008) | `opponentCandidates` | no |
| N | **v5**: denial ("hate drafting") in the rollout policy: the cards a player leaves go to the next player, so every simulated player adds λ · max(0, v_q(c) − max_{c' ≠ c} v_q(c')) to the next player's favourite card c of the hand (v_q = that player's marginal card value) | `CardValueModel.addDenial`, `policyValues` | no (v5) |

Other switches that were tested and are off: `treeThreshold`, `ownCardTree`, `stopAtRoundEnd`,
`rolloutRounds` > 1, `rolloutTemperature`, `opponentK`, `opponentRivalWeight`, `maxChildShare`.

## Final configuration

One JSON file serves both table sizes: the top-level parameters are used with 2 or 3 players, the nested
`fourPlayer` block with 4 or more (always with the time budget of the top level).

| Setting | Search | Card model | Value | K |
|---|---|---|---|---|
| 3 players (every budget) | H B P M R + S + I | v3 (default) | v3 (w 0.7, σ 15) | 0.5 |
| 4 players, 100 ms (`agents_v4/SushiMCTS_100ms.json`) | H B P M R + S + I | X0 | v3 (w 0.7, σ 15) | 0.5 |
| 4 players, ≥ 500 ms (both competition files) | H B P M R + S + I + F + L8 | X0 | W2 | 1.0 |

X0 = heuristic `args` `[15, 0.7, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3]`
(σ, w, α, T, β, T_m, κ, δ, π, average Nigiri, γ; meaning in `SushiGoHeuristic` and `CardValueModel`);
W2 uses the same values with w = 1.

The 3- and 4-player settings must differ: the best 4-player setting (W2 + F + L8) loses 7.7 ± 5.4 win points
(z = −2.8, 500 paired games) with 3 players at 500 ms, and X0 + F + L8 loses 6.6 ± 6.0 points (z = −2.2)
against X0 alone with 4 players at 100 ms.

## Competition configuration

* `json/competition/SushiMCTS_1000ms.json` – 1 s per decision: budget 900 ms CPU, wall-clock guard 925 ms.
* `json/competition/SushiMCTS_500ms.json` – 500 ms per decision: budget 430 ms CPU, wall-clock guard 450 ms.

Both work for 3 and 4 players. **Our competition file is `SushiMCTS_1000ms.json`** (3 players, 1 s per decision,
section 3 of the guidelines). Section 4 of the guidelines also mentions 4 players with 500 ms per decision: for
that setting use `SushiMCTS_500ms.json`, and **never the 1000 ms file in a 500 ms competition**.

**Timing safety.** The budget is CPU time, as for every TAG agent, and TAG's own timeout check (`Game.java`,
`ElapsedCpuChessTimer`) also measures the thread's CPU time on Linux and macOS. With `wallTimeLimitMS` the search
also stops after that much real time. Measured on an otherwise idle machine (`arena/src/arena/WallProbe.java`,
4 games per file and table size, every decision of our agent):

| File | Players | CPU time per decision: mean / max | Wall-clock: median / 95 % / max |
|---|---|---|---|
| 1000 ms | 3 | 833 / 899 ms | 925 / 925 / 928 ms |
| 1000 ms | 4 | 816 / 899 ms | 925 / 1009 / 1279 ms |
| 500 ms | 3 | 370 / 429 ms | 450 / 470 / 516 ms |
| 500 ms | 4 | 406 / 429 ms | 450 / 459 / 642 ms |

The CPU time never exceeds the budget. The few longer wall-clock times (5 of 87, 2 of 99 and 1 of 86 decisions)
are garbage-collection pauses of the JVM, which the other agents' trees fill as well; they do not count as the
agent's CPU time. On a machine that runs many games at once the wall-clock times grow further (measured with 20
other game threads on 18 cores: median 926 ms, maximum 1833 ms for the 1000 ms file), while the CPU time stays
within the budget. Version 5 repeated the measurement on a 4-core machine with the JVM's default settings (1000 ms file: median 925 ms, maximum 1004 ms; 500 ms file: maximum 472 ms; CPU time always within the budget), see [Version 5](#version-5--sprint-for-the-in-class-tournament-27-october).

## How to construct the agent in code

```java
SushiMCTSPlayer agent = (SushiMCTSPlayer) PlayerFactory.createPlayer("src/main/java/groupA/json/competition/SushiMCTS_1000ms.json");
```

or by hand (3-player part of the final agent):

```java
SushiMCTSParams p = new SushiMCTSParams();
p.heuristic = new groupA.SushiGoHeuristic();     // defaults: sigma 15, w 0.7, alpha 0.5, T 4
p.K = 0.5;
p.useInformedRollout = true;  p.useActionPruning = true;  p.useMaxN = true;  p.rolloutToRoundEnd = true;
p.useDecoupledUCT = true;     p.useInformationSets = true;
p.budgetType = PlayerConstants.BUDGET_TIME;  p.budget = 900;  p.wallTimeLimitMS = 925;
AbstractPlayer agent = new SushiMCTSPlayer(p);
```

## Parameters added in v4 and v5

| JSON key | Default | Final (3 p / 4 p) | Meaning |
|---|---|---|---|
| `useDecoupledUCT` | false | true / true | S (needs `useMaxN`) |
| `useInformationSets` | false | true / true | I (needs S); D is then not used |
| `useFastSim` | false | false / true (≥ 500 ms) | F (needs I, `rolloutToRoundEnd` and `SushiGoHeuristic`) |
| `leafRollouts` | 1 | 1 / 8 | L: rollouts per leaf with F |
| `terminalScale` | 0 | 0 / 2 | V: finished games valued tanh((S_i − max_j S_j) / scale) (0 = off) |
| `sigmaRoundDecay` | 0 | 0 / 0.5 | σ multiplied by (R / 2)^d, R = rounds left at the leaf |
| `opponentScoreValue` | false | false / true | opponents in max^n value their own lead over the others' mean |
| `fourPlayer` | null | – / block | parameters for 4+ players (budget fields taken from the top level) |
| `useMultiTree` (U), `useMAST` (G), `useOpponentBias` (Q) | false | false | tested, off |
| `ensembleTrees` (E) | 1 | 1 | tested, off: number of independent trees (I) the budget is split between |
| `opponentTopK`, `opponentWidening` (K) | 0 | 0 | tested, off: an opponent's k best cards, or ⌈(n + 1)^a⌉ of them at a node with n visits |
| `opponentRolloutEpsilon`, `opponentRolloutTemperature` | −1 | −1 | tested, off: ε and softmax temperature of the other players' rollout moves with F (< 0 = as ours) |
| `rolloutDenial` (v5) | 0 | 0 / 0 | tested, off: λ of N (rollouts on TAG's game state, not with F) |
| `treeThreshold`, `ownCardTree`, `stopAtRoundEnd`, `rolloutRounds`, `rolloutTemperature`, `opponentK`, `opponentRivalWeight`, `maxChildShare` | 0 / false / 1 / −1 | defaults | tested, off |
| `heuristic.args` | – | default / X0 | up to 11 constants: σ, w, α, T, β, T_m, κ, δ, π, avgNigiri, γ |

The v3 parameters (`K`, `rolloutLength`, `maxTreeDepth`, `useProgressiveBias`, `useInformedRollout`,
`useDeterminisation`, `useActionPruning`, `useOpponentModel`, `useMaxN`, `rolloutToRoundEnd`, `wallTimeLimitMS`,
…) are unchanged.

## Results

All numbers: Sushi Go in TAG, played with the benchmark harness in `experiments_v4` (next to this
folder, not part of the submission). Budgets are CPU time per decision, the same for every agent at the table.
Our agent plays against **v2** (our previous agent, H + A + B + D) and **TAG's tuned ISMCTS** (`players.mcts`
configuration, used only as an opponent; nothing of it is in this folder). In game *g* it sits in seat *g* mod *n*;
the other seats alternate between the two opponents. A shared first place counts 1/k; chance is 33.3 % with
3 players and 25 % with 4. Brackets: Wilson 95 % CI; ±: 95 % CI (1.96 standard errors) of a mean or of a paired
difference; z = difference / standard error. *Score margin*: our score minus the best opponent's score.

**Deals and pairing.** Game *g* of a run uses game seed *seed + g*. TAG builds the deck by iterating a `HashMap`
whose order follows identity hash codes, so the same seed gave *different* deals in different runs. This also
holds for the v3 study, whose README says "same deals": its comparisons are two-sample comparisons, and its
intervals are valid as such. Since 19 October the harness re-deals every game from a deck in a fixed order,
shuffled with the game seed (`Arena.canonicalDeal`), so runs with the same seed play the same deals and seats.
Runs from before are marked †; a "paired" difference involving them is an ordinary two-sample difference.
Pairing helps little in Sushi Go (the win/loss correlation of two agents on the same deal is 0.0–0.2): a
difference between two 500-game runs has a CI of about ±5.7 win points, between two 250-game runs about ±8.
The score margin is usually the more sensitive measure and was used for screening.

**Development and validation.** Every decision was made on the deals of seeds 1 and 2. The final configuration
was then played on seed block 7. Game g of a run uses deal seed s + g, so seed block 7 (game seeds 7–506) overlaps
the development deals (seeds 1–501): these runs are a second measurement, not a clean hold-out. The held-out
validation uses seed block 100 000 (`experiments_v5`, runs `r2_val_*`), which no earlier run touched.

**Number of games.** 500 games per condition, except the 3-player / 900 ms runs in development and on seed
block 7 and the 4-player / 500 ms development runs, which have 250. A 3-player game at 900 ms needs about 3 × 27 decisions × 0.9 s ≈ 73 s of CPU time and a
4-player game at 500 ms 4 × 24 × 0.5 s ≈ 48 s; on our 18-core laptop a 250-game run took 45–75 minutes with
8 games in parallel, and the study has about 160 runs. We therefore used 250 games at these budgets and
report the correspondingly wider intervals (about ±6 instead of ±4.4 points for a win rate). The final
4-player / 500 ms agent was validated with 500 games.

### Final agent on seed block 7 (overlaps the development deals)

| Setting | Games | Win % [95 % CI] | Score margin | TAG ISMCTS seats | v2 seats |
|---|---|---|---|---|---|
| 3 players, 100 ms | 500 | **55.0 [50.6, 59.3]** | +1.00 ± 0.44 | 27.7 % | 17.4 % |
| 3 players, 500 ms | 500 | **49.4 [45.0, 53.8]** | +0.40 ± 0.40 | 35.1 % | 15.5 % |
| 3 players, 900 ms | 250 | **56.0 [49.8, 62.0]** | +1.06 ± 0.62 | 29.6 % | 14.4 % |
| 4 players, 100 ms | 500 | **46.8 [42.5, 51.2]** | -0.28 ± 0.46 | 23.9 % | 11.6 % |
| 4 players, 500 ms | 500 | **41.4 [37.2, 45.8]** | -1.53 ± 0.46 | 29.1 % | 10.0 % |

### From v3 to v4 (development deals, win %)

| Setting | v2 | v3 | v3 + S | v3 + S + I | final v4 |
|---|---|---|---|---|---|
| 3 players, 100 ms (seed 1) | 24.8 † | 50.1 † | 51.4 † | **55.7** † | = v3 + S + I |
| 3 players, 500 ms (seed 1) | 18.3 † | 40.7 † | 46.9 † | **49.7** † | = v3 + S + I |
| 3 players, 900 ms (seed 1, 250 games) | 18.8 † | 32.8 † | – | **47.6** † | = v3 + S + I |
| 4 players, 100 ms (seed 1) | 16.0 † | 38.6 † | 45.1 † | 46.4 † | + X0: **49.6** |
| 4 players, 500 ms (seed 1, 250 games) | 11.2 † | 35.4 † | – | 38.4 † | + X0 + W2: 45.6; + F + L8 (seed 2): 43.8 |

S and I are the main gains of v4. With 3 players the gain over v3 grows with the budget (+5.6, +9.0 and
+14.8 win points at 100, 500 and 900 ms): v3 got *worse* with more time (50.1 → 40.7 → 32.8 %), v4 hardly
(55.7 → 49.7 → 47.6 %). In v3's tree the players of a turn moved one after another, so a later player could
react to the card an earlier player had just chosen, which is impossible in the real game. The likely
explanation is that the more iterations v3 had, the more its play relied on this wrong model; decoupled
UCT (S) removes it. With 4 players the
3-player agent is not good enough at 500 ms (38.4 %); the card model X0 and the value shape W2 (both tuned
for 4 players, see below) bring it to about 44 %.

### Ablation (3 players, 100 ms, seed 7, 500 games each, paired with the final agent)

| Agent | Win % [95 % CI] | Score margin | Δ win (paired) | Δ score margin (paired) |
|---|---|---|---|---|
| final agent (H B P M R S I, K 0.5) | **55.0** [50.6, 59.3] | +1.00 ± 0.44 | – | – |
| − I (S with v3's determinisation D) | 51.2 [46.8, 55.6] | +0.37 ± 0.44 | −3.8 ± 5.7 (z = −1.3) | −0.63 ± 0.55 (z = −2.2) |
| − S − I (= v3) | 44.8 [40.5, 49.2] | −0.75 ± 0.48 | −10.2 ± 5.7 (z = −3.5) | −1.75 ± 0.58 (z = −5.9) |
| − R (rollouts of 10 moves) | 48.8 [44.4, 53.2] | +0.25 ± 0.46 | −6.2 ± 5.9 (z = −2.0) | −0.74 ± 0.61 (z = −2.4) |
| − B (uniformly random rollouts) | 50.7 [46.3, 55.1] | +0.02 ± 0.43 | −4.3 ± 5.8 (z = −1.4) | −0.98 ± 0.57 (z = −3.4) |
| − H (TAG's own Sushi Go heuristic) | 26.7 [23.0, 30.7] | −4.46 ± 0.61 | −28.3 ± 5.8 (z = −9.6) | −5.45 ± 0.71 (z = −15) |

Every component pulls its weight: each removal lowers both the win rate and the score margin, and on the
score margin every difference is significant (|z| ≥ 2.2). H matters most by far; S + I together are worth
10.2 win points on seed block 7 (5.6 † on the development deals of seed 1).

### Four players

The best settings for 3 and 4 players differ, so the agent switches parameters by table size (`fourPlayer`):

* **X0** (card model tuned for 4 players with a search-free tuner): +3.2 win points at 4 players / 100 ms
  (46.4 † → 49.6 %), but −4.9 at 3 players / 100 ms (55.7 † → 50.8 %).
* **W2** (value shape for 4 players): +3.4 ± 8.6 at 4 players / 500 ms (seed 1, 42.2 → 45.6 %), but with
  3 players at 500 ms adding X0 + W2 to v4 + F + L8 costs 7.7 ± 5.4 points (z = −2.8; 49.5 → 41.8 %).
* **F + L8** (fast simulator, 8 rollouts per leaf): +1.0 ± 7.9 at 4 players / 500 ms (seed 2, W2 42.8 → 43.8 %),
  −6.6 ± 6.0 (z = −2.2) at 4 players / 100 ms, −0.2 ± 6.3 † at 3 players / 500 ms. With F + L8 the 500 ms agent
  runs about 36 800 iterations of 8 rollouts per decision, against 10 200 single-rollout iterations without
  them, yet more search helps little: on 73 positions from random 4-player games the 500 ms tree has 704 joint
  actions at the root, 53 % of the depth-1 nodes are visited only once and the mean depth is 1.5, and doubling
  the time or the rollouts per leaf changes the chosen card in only 2.7 % and 5.5 % of the positions, no more
  often than re-running the same search (5.5 %; `experiments_v5/results/bench4.txt`, `treestats4.txt`,
  `agree4.txt`, made with `arena/v5/run_treestats.sh`). The search is essentially a decoupled bandit at the root
  plus rollouts, and its quality is limited by the rollout policy and the evaluation, not by the number of
  iterations. L8 spends the extra speed on better leaf estimates instead.

Nothing else improved the 4-player agent at 500 ms (seed 2, 250 games each, paired with W2 + F + L8):

| Agent | Games | Win % [95 % CI] | Score margin | Δ win vs base (paired) | Δ margin (paired) |
|---|---|---|---|---|---|
| W2 (repeat on seed 2) | 250 | 42.8 [36.8, 49.0] | -1.31 ± 0.61 | -1.0 ± 7.9 (z = -0.2) | -0.60 ± 0.75 |
| W2 + F + L8 | 250 | 43.8 [37.8, 50.0] | -0.71 ± 0.58 | base | – |
| W2 + F + L8, K 0.7 for us | 250 | 42.4 [36.4, 48.6] | -1.11 ± 0.65 | -1.4 ± 8.2 (z = -0.3) | -0.40 ± 0.79 |
| W2 + F + L8, K 0.7 for all | 250 | 44.0 [38.0, 50.2] | -1.04 ± 0.57 | +0.2 ± 8.0 (z = +0.0) | -0.33 ± 0.74 |
| W2 + F + L8, opponentK 1.4 | 250 | 41.0 [35.1, 47.2] | -1.69 ± 0.69 | -2.8 ± 8.4 (z = -0.7) | -0.98 ± 0.85 |
| W2 + F + L8, rollouts to the game end | 250 | 41.4 [35.5, 47.6] | -1.26 ± 0.64 | -2.4 ± 8.1 (z = -0.6) | -0.55 ± 0.82 |
| W2 + F + L8, softmax rollouts T 1 | 250 | 40.4 [34.5, 46.6] | -1.18 ± 0.60 | -3.4 ± 8.1 (z = -0.8) | -0.47 ± 0.79 |
| W2 + F + L8, our agent 1000 ms, K 1.37 | 250 | 39.0 [33.2, 45.2] | -2.02 ± 0.66 | -4.8 ± 8.1 (z = -1.2) | -1.30 ± 0.77 |
| W2 + F, treeThreshold 8 | 250 | 40.6 [34.7, 46.8] | -1.37 ± 0.62 | -3.2 ± 8.1 (z = -0.8) | -0.66 ± 0.74 |
| W2 + F + L8, σ 7 | 250 | 34.4 [28.8, 40.5] | -1.76 ± 0.58 | -9.4 ± 8.3 (z = -2.2) | -1.05 ± 0.76 |
| W2 + F + L8, σ 25 | 250 | 44.1 [38.1, 50.3] | -1.13 ± 0.70 | +0.3 ± 7.5 (z = +0.1) | -0.42 ± 0.75 |
| W2 + F + L8 without V and σ decay | 250 | 38.4 [32.6, 44.6] | -1.22 ± 0.65 | -5.4 ± 7.8 (z = -1.4) | -0.50 ± 0.81 |
| W2 + F + L8 + U | 250 | 36.0 [30.3, 42.1] | -2.08 ± 0.63 | -7.8 ± 7.8 (z = -2.0) | -1.37 ± 0.76 |
| the 100 ms agent with 100 ms | 250 | 34.8 [29.2, 40.9] | -1.77 ± 0.57 | -9.0 ± 7.7 (z = -2.3) | -1.06 ± 0.75 |
| W2 + F + L8, tree depth 1 | 250 | 26.4 [21.3, 32.2] | -3.38 ± 0.70 | -17.4 ± 8.2 (z = -4.2) | -2.67 ± 0.85 |
| W2 + F + L8, 5 trees (E) | 250 | 38.0 [32.2, 44.2] | -1.82 ± 0.67 | -5.8 ± 7.5 (z = -1.5) | -1.11 ± 0.77 |
| W2 + F + L8, ε 0.35 | 250 | 39.0 [33.2, 45.2] | -1.55 ± 0.59 | -4.8 ± 8.0 (z = -1.2) | -0.84 ± 0.76 |
| W2 + F + L8, opponents' rollout moves softmax T 1.5 | 250 | 41.8 [35.9, 48.0] | -1.16 ± 0.61 | -2.0 ± 7.8 (z = -0.5) | -0.45 ± 0.71 |
| W2 + F + L8, opponents' 3 best cards (K) | 250 | 42.2 [36.2, 48.4] | -1.58 ± 0.68 | -1.6 ± 8.3 (z = -0.4) | -0.86 ± 0.82 |
| W2 + F + L8, progressive widening a = 0.25 (K) | 250 | 37.8 [32.0, 44.0] | -1.72 ± 0.66 | -6.0 ± 7.8 (z = -1.5) | -1.00 ± 0.80 |

Giving *only our agent* 1000 ms (and K 1.37) made it worse (score margin −1.30 ± 0.77). Every variant in
this table has a lower score margin than the base run (paired differences −0.33 to −2.67), so the base run
(−0.71) was probably lucky; the seed-7 run (−1.53 ± 0.46) is the better estimate of the final
agent. Two results stand out: with tree depth 1 (bandits at the root only, rollouts from the second turn on) the
agent lost 17.4 ± 8.2 win points (z = −4.2), so the second turn of the tree, where the opponents answer our
card, matters a lot; and the 100 ms agent given 100 ms lost 9.0 ± 7.7 (z = −2.3). Limiting the opponents in
the tree to their 3 best cards (K) also lost at 4 players / 100 ms (49.6 → 43.7 %, −5.9 ± 5.9, score margin
−0.71 ± 0.58).

**At 4 players the model, not the search effort, limits the agent.** With the final settings, two searches
with different seeds choose the same card in 93–96 % of 74 test positions (taken from random games;
`Agree4` in `experiments_v4/dev`), and a search with 16 rollouts per leaf or with 1000 ms chooses the same
card as the 500 ms search just as often (93 %). More time, more rollouts per leaf or several trees therefore
change few decisions; the result depends on how well the rollout policy and the value function model the
opponents' play.

### Other framework agents (3 players, 100 ms, seed 7, 500 games each)

| Opponents | Games | Our win % [95 % CI] | Score margin | Opponent seats: win % |
|---|---|---|---|---|
| BasicMCTS + RHEA | 500 | **84.2 [80.7, 87.1]** | +6.68 ± 0.60 | BasicMCTS 9.0 %, RHEA 6.8 % |
| OSLA + BasicMCTS | 500 | **87.2 [84.0, 89.8]** | +9.74 ± 0.75 | BasicMCTS 12.8 %, OSLA 0.0 % |

Full tables of every run (development, ablation, 4 players), the per-game data and the tools are in
`experiments_v4/` (next to this folder, not part of the submission); `experiments_v4/RESULTS.md` has every
table.

## Running the experiments

Copy `experiments_v4` to `<TAG root>/arena`. Then, from the TAG root, after `mvn compile` (Java 21+, the
dependency classpath in `cp.txt`, e.g. `mvn dependency:build-classpath -Dmdep.outputFile=cp.txt`):

```
arena/build.sh
arena/run.sh players=3 games=500 threads=8 seed=7 focus=<agent.json> opps=<opp1.json>,<opp2.json> label=X out=X.csv
```

`experiments_v4/README.md` explains the harness, the batch script, the agent files of every run and the tools
that produced the tables.
The v5 sprint (bash versions of the scripts, a run queue, the greedy duel tools) is described in
`experiments_v5/README.md`; `experiments_v5/RESULTS.md` has its tables.

## TAG version

Requires a TAG version from November 2024 or later (`SGGameState.isHandKnown`). In older
versions the method was called `hasSeenHand` – pull the latest framework instead of renaming.
