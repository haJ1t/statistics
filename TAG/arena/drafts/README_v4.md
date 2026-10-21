# groupA – SushiMCTS agent for Sushi Go! (ECS7032P Assignment 1)

> **Rename first:** replace every `groupA` (package line of the 7 `.java` files, the folder name,
> and the `class` / `heuristic` entries of the JSON files) with your real group letter, e.g. `groupK`.

This is **version 4** of the agent. Version 3 (H + B + D + P + M + R, K 0.5) is still inside it unchanged:
every new idea is a switch in `SushiMCTSParams` that is off by default, and the final JSON files switch on the
ones that were kept.

**Win rate of the final agent** against v2 (our previous agent) and TAG's tuned ISMCTS, on fresh deals that
were not used during development (validation, seed 7):

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

Both work for 3 and 4 players. The guidelines mention both settings (3 players / 1 s and 4 players / 500 ms):
submit the file of the setting the module team confirms, and **never the 1000 ms file in a 500 ms competition**.

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
within the budget.

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

## Parameters added in v4

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
| `treeThreshold`, `ownCardTree`, `stopAtRoundEnd`, `rolloutRounds`, `rolloutTemperature`, `opponentK`, `opponentRivalWeight`, `maxChildShare` | 0 / false / 1 / −1 | defaults | tested, off |
| `heuristic.args` | – | default / X0 | up to 11 constants: σ, w, α, T, β, T_m, κ, δ, π, avgNigiri, γ |

The v3 parameters (`K`, `rolloutLength`, `maxTreeDepth`, `useProgressiveBias`, `useInformedRollout`,
`useDeterminisation`, `useActionPruning`, `useOpponentModel`, `useMaxN`, `rolloutToRoundEnd`, `wallTimeLimitMS`,
…) are unchanged.

## Results

‹results sections›
