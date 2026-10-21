# groupA – SushiMCTS agent for Sushi Go! (ECS7032P Assignment 1)

> **Rename first:** replace every `groupA` (package line of the 7 `.java` files, the folder name,
> and the `class` / `heuristic` entries of the JSON files) with your real group letter, e.g. `groupK`.

This is **version 4** of the agent. Version 3 (v3: H + B + D + P + M + R) is unchanged inside it: every new
idea is a switch that is off by default, and the final JSON files turn the chosen ones on.

| Setting (opponents: v2 + TAG's ISMCTS) | v2 | v3 | **v4 (final)** |
|---|---|---|---|
| 3 players, 100 ms | 23.9 % | 48.0 % | **‹55.0›** % |
| 3 players, 500 ms | 18.3 % | 40.7 % | **‹›** % |
| 3 players, 900 ms | 18.8 % | 32.8 % | **‹›** % |
| 4 players, 100 ms | 16.0 % | 38.6 % | **‹›** % |
| 4 players, 500 ms | 11.2 % | 35.4 % | **‹›** % |

Win rate of our agent (chance: 33.3 % with 3 players, 25 % with 4). v4 numbers: validation on fresh deals that
were never used during development (details and confidence intervals in [Results](#results)).

## What the agent is

`SushiMCTSPlayer` is a modified copy of the framework's **`players.basicMCTS.BasicMCTSPlayer`**
(an allowed base agent). It does **not** use, copy or import anything from the advanced
`players.mcts` package (`MCTSPlayer`, `SingleTreeNode`, `MCTSParams`, `MCTSEnums`), and it does not
change any framework file. Every modification is a switch in `SushiMCTSParams`, so all variants of the
study run the same code.

| File | Copied from | Purpose |
|---|---|---|
| `SushiMCTSPlayer.java` | `BasicMCTSPlayer` | entry point: parameters per table size (`fourPlayer`), wall-clock guard, D, Q |
| `SushiTreeNode.java` | `BasicTreeNode` | the search: A, B, P, O, M, R (v3) and S, I, F, L, G, U, V (v4) |
| `SushiMCTSParams.java` | `BasicMCTSParams` | all original parameters + the switches of the study |
| `SushiGoHeuristic.java` | new | state evaluation H |
| `CardValueModel.java` | new | Sushi Go knowledge: estimated scores and marginal card values (used by H and B) |
| `SushiSim.java` | new | F: exact Sushi Go simulator on `int` arrays |
| `OpponentModel.java` | new | Q: card preferences of each opponent learned during a game (tested, off) |

## One decision of the final agent

1. **Sample (I).** Copy the position and deal every card we cannot see (the opponent hands we have not seen
   yet and the draw pile) again at random.
2. **Select (S, M).** At every node (one node per turn) each player picks a card of its hand by UCB1 over its
   *own* statistics at that node; cards it has not tried there come first. The joint choice is played and leads
   to the child of that joint choice.
3. **Expand.** The first joint choice without a node adds one node.
4. **Roll out (B, R).** Every player plays its best card by the card-value model (a random card with
   probability 0.2) until the round has been scored.
5. **Evaluate (H).** Every player gets the value tanh(Δ/σ) of its estimated final score against the others.
6. **Back up (M).** Each player's value is added to the card that player chose at every node of the path.

When the time is up, the agent plays its card with the most visits at the root.

## Modifications

| Id | Modification | Where | In final agent |
|---|---|---|---|
| H | Sushi Go heuristic `tanh(Δ/σ)`, Δ = w·(lead over best opponent) + (1 − w)·(lead over mean opponent) | `SushiGoHeuristic` | yes |
| B | ε-greedy informed rollouts: every player plays its best marginal card, ε = 0.2 random | `SushiTreeNode.playOut` | yes |
| P | action pruning: one branch per distinct card | `CardValueModel.distinctActions` | yes |
| M | max^n backups (Sturtevant 2008): every player maximises its own value | `SushiTreeNode` | yes |
| R | rollouts to the end of the current round | `SushiTreeNode.finishRollout` | yes |
| **S** | **simultaneous moves – decoupled UCT** (Lanctot et al. 2013): TAG asks the players of a turn one after the other, so in v3's tree a later player "saw" the earlier players' cards. With S there is one node per turn and each player selects by UCB over its own card statistics, as in the real game | `SushiTreeNode` | **yes** |
| **I** | **information sets** (single-observer ISMCTS, Cowling, Powley and Whitehouse 2012): a new sample of the hidden cards in every iteration and one shared open-loop tree (nodes keep statistics, no game state); cards are identified by type, and a card's exploration term counts the iterations in which the player held it. Replaces D | `informationSetIteration` | **yes** |
| **X** | **card-model constants for 4 players** (set X0, below), found with a search-free duel tuner: Sashimi discount, Maki standing weight, Dumpling, Pudding and Nigiri values, and counting the cards still in all hands | `CardValueModel.Config` | **4 players** |
| **W** | **value shape for 4 players** (W2): only the lead over the best opponent counts (w = 1), finished games are valued by the gap to the winner (V, scale 2), σ shrinks in later rounds (σ·(R/2)^0.5, R = rounds left), and the opponents in max^n play for their own lead over the others' mean (`opponentScoreValue`); K = 1.0 | `SushiGoHeuristic`, `valuesOf` | **4 players, ≥ 500 ms** |
| F | fast simulation: iterations on `SushiSim` (an exact copy of TAG's Sushi Go rules on `int` arrays) instead of copies of TAG's game state, 7–11× more iterations | `SushiSim`, `simIteration` | ‹4p 500› |
| L | with F: m rollouts from every leaf, values averaged | `simLeafValues` | ‹4p 500› |
| D | v3's information-aware determinisation (1–4 trees) | `SushiMCTSPlayer` | no (replaced by I) |
| A | progressive bias | | no (v3 study) |
| O | opponents not searched (play the card-value policy) | | no (v3 study) |
| G | MAST card values steer the rollouts | | no |
| U | one open-loop tree per player (multiple-observer ISMCTS style) | `multiTreeIteration` | no |
| Q | per-opponent card-type biases learned from the opponent's picks during the game | `OpponentModel` | no |

Further switches that were tested and are off: `treeThreshold`, `ownCardTree`, `stopAtRoundEnd`,
`rolloutRounds` > 1, `rolloutTemperature`, `opponentK`, `opponentRivalWeight`, `maxChildShare`.

## Final configuration

One JSON file serves both table sizes: the top-level parameters are used with 2 or 3 players, the nested
`fourPlayer` block with 4 or more players (always with the time budget of the top level).

| Setting | Search | Card model | Value | K |
|---|---|---|---|---|
| 3 players (every budget) | H B P M R + S + I | v3 (default) | v3 (w 0.7, σ 15) | 0.5 |
| 4 players, 100 ms | H B P M R + S + I | X0 | v3 (w 0.7, σ 15) | 0.5 |
| 4 players, 500 ms | H B P M R + S + I ‹+ F + L8› | X0 | W2 | 1.0 |
| 4 players, 900–1000 ms | H B P M R + S + I | X0 | W2 | 1.0 |

X0 = heuristic `args` `[15, 0.7, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3]` (σ, w, α, T, β, T_m, κ, δ, π, avgNigiri, γ;
see `SushiGoHeuristic`); W2 uses the same with w = 1.

The 3-player and 4-player settings have to differ: W2 + F + L8, the best 4-player setting at 500 ms, loses
7.7 ± 5.4 win points (z = −2.8) with 3 players; X0 + F + L8 at 4 players / 100 ms loses 6.6 ± 6.0 (z = −2.2)
against X0 alone.

‹results, safety, parameters, competition, running›
