# groupW – SushiMCTS, an MCTS agent for Sushi Go!

ECS7032P Assignment 1, group W: Hakan Coskun (241076864), Halit Ozger (230625112) and Weronika Diana Podgorska
(230394735).

`SushiMCTSPlayer` plays Sushi Go! in TAG with decoupled information-set MCTS and a model of the game's scoring. It is
a modified copy of TAG's `players.basicMCTS.BasicMCTSPlayer` (BasicMCTS in the brief's list of permitted base
agents). It does not use, copy or import anything from the advanced MCTS agent (`players.mcts.MCTSPlayer`), and
everything it needs is in this package: no file of the framework is changed. Our report describes the method and the
experiments.

## Files

| File | Contents |
|---|---|
| `SushiMCTSPlayer.java` | the agent (copied from `BasicMCTSPlayer`): parameters for the table size, search loop, time guard |
| `SushiTreeNode.java` | the search (copied from `BasicTreeNode`); changes are marked `MODIFICATION <id>` or named after their switch |
| `SushiMCTSParams.java` | the parameters (copied from `BasicMCTSParams`); every modification is a switch |
| `SushiGoHeuristic.java` | the evaluation H of a position |
| `CardValueModel.java` | the model of Sushi Go!'s scoring: estimated final scores and marginal card values |
| `SushiSim.java` | TAG's Sushi Go! rules re-implemented on integer arrays, for fast simulation (F) |
| `OpponentModel.java` | an online opponent model (Q; implemented, off in the final agent) |
| `json/competition/SushiMCTS_1000ms.json` | **the competition file**: 1 s per decision |
| `json/SushiMCTS_500ms.json` | the same agent for a limit of 500 ms per decision |
| `json/SushiMCTS_100ms.json` | the final agent at 100 ms per decision, the budget of most experiments in the report |

## Requirements

A TAG version that has `SGGameState.isHandKnown` and `SGGameState.getCurrentSimultaneousPlayers`, such as the copy
provided for the module, and Java 21, as for TAG itself. Copy this folder to `src/main/java/groupW` in TAG and compile
TAG as usual (`mvn compile` or the IDE). Nothing else is needed.

## Running the agent

**Competition file.** `json/competition/SushiMCTS_1000ms.json` is the agent for the competition of the guidelines
(three players, 1 s per decision). Its search gets 900 ms of CPU time per decision and also stops after 925 ms of
real time (`wallTimeLimitMS`), so that a busy machine cannot push a decision over the limit. If the limit is 500 ms
per decision instead (the guidelines also mention four players with 500 ms), use `json/SushiMCTS_500ms.json`
(430 ms of CPU time, 450 ms of real time), never the 1 s file. Both files play with three and with four players: the
`fourPlayer` block holds the settings for four or more players, and the time budget always comes from the top level.

From code (`core.AbstractPlayer`, `players.PlayerFactory`; paths relative to the TAG root):

```java
AbstractPlayer agent = PlayerFactory.createPlayer("src/main/java/groupW/json/competition/SushiMCTS_1000ms.json");
```

From the command line, for example a three-player tournament between all agent files in one folder (copy the
competition file into it; `mvn package` builds `target/TAG.jar`):

```
java -jar target/TAG.jar RunGames game=SushiGo nPlayers=3 playerDirectory=<folder> mode=random matchups=100
```

**Constructor.** `new SushiMCTSPlayer(SushiMCTSParams params)`. Set the parameters with `setParameterValue`, not the
fields, so that copies of the parameter object keep them. This builds the three-player part of the competition
agent:

```java
import core.AbstractPlayer;
import groupW.*;
import players.PlayerConstants;

SushiMCTSParams p = new SushiMCTSParams();
p.setParameterValue("heuristic", new SushiGoHeuristic());
p.setParameterValue("K", 0.5);
for (String s : new String[]{"useInformedRollout", "useActionPruning", "useMaxN", "rolloutToRoundEnd",
        "useDecoupledUCT", "useInformationSets"})
    p.setParameterValue(s, true);
p.setParameterValue("budgetType", PlayerConstants.BUDGET_TIME);
p.setParameterValue("budget", 900);
p.setParameterValue("wallTimeLimitMS", 925);
AbstractPlayer agent = new SushiMCTSPlayer(p);
```

Without a `fourPlayer` block this agent plays four-player games with the same settings; the four-player settings
are easiest to take from the JSON files. `new SushiMCTSPlayer()` has every modification switched off and plays like
BasicMCTS with the default `BasicMCTSParams`.

## How the agent chooses a card

Each iteration of the search:

1. **Sample (I).** Copy the position and deal the cards we cannot see (the opponents' hands we have not seen yet and
   the draw pile) again at random.
2. **Select (S, M).** At each node (one node per turn) every player picks a card from its hand by UCB1 over its own
   statistics at that node; cards it has not tried there come first. The joint choice leads to the next node.
3. **Expand.** The first joint choice without a node adds one node.
4. **Roll out (B, R).** Every player plays its best card by the card-value model (a random card with probability
   0.2) until the round has been scored. With four players and 500 ms or more, 8 rollouts are run from the leaf on
   the fast simulator and their values averaged (F, L).
5. **Evaluate (H).** Each player gets tanh(Δ/σ) of its estimated final score against the other players' scores.
6. **Back up (M).** Each player's value is added to the statistics of the card that player chose at every node of
   the path.

When the time is up, the agent plays the card with the most visits at the root.

## Modifications

Every modification is a switch in `SushiMCTSParams` that is off by default, so all variants run the same code. The
ids are those of Table I of the report.

| Id | Modification | Where | Final agent, 3 / 4 players |
|---|---|---|---|
| H | evaluation tanh(Δ/σ) with Δ = w · (lead over the best opponent) + (1 − w) · (lead over the mean opponent), from the card-value model's estimated scores | `SushiGoHeuristic`, `CardValueModel` | yes / yes |
| B | ε-greedy rollouts: every player plays its card with the highest marginal value, a random card with probability ε = 0.2 | `SushiTreeNode.playOut` | yes / yes |
| R | rollouts end when the current round has been scored | `SushiTreeNode.finishRollout` | yes / yes |
| P | one branch per distinct card | `CardValueModel.distinctActions` (with I: `distinctCards`) | yes / yes |
| M | max^n backups: every player maximises its own value | `SushiTreeNode` | yes / yes |
| S | decoupled UCT with one node per turn: each player selects by UCB over its own card statistics | `SushiTreeNode` | yes / yes |
| I | single-observer information-set search: one open-loop tree, hidden cards dealt again in every iteration | `SushiTreeNode.informationSetIteration` | yes / yes |
| X0 | card-model constants tuned for four players | `heuristic.args` of the `fourPlayer` block | – / yes |
| W2 | value for four players: w = 1, finished games valued by the gap to the winner (V, `terminalScale`), σ smaller in later rounds (`sigmaRoundDecay`), opponents play for their lead over the others' mean (`opponentScoreValue`) | `SushiGoHeuristic.valuesOf`, `SushiTreeNode` | – / yes (≥ 500 ms) |
| F, L | fast simulation on `SushiSim`, 8 rollouts per leaf | `SushiSim`, `SushiTreeNode.simIteration` | – / yes (≥ 500 ms) |
| A | progressive bias | `SushiTreeNode` | no |
| D | up to 4 determinised trees (replaced by I) | `SushiMCTSPlayer` | no |

Further switches, tested and off in the final agent: O (opponents not searched), U (one tree per player), K (opponents
limited to their best cards), E (several independent trees) and N (denying the next player its favourite card in the
rollouts); G (MAST rollouts) and Q (online opponent model) are implemented but were not evaluated. Our previous version
v3 (H, B, D, P, R, M) is a configuration of the same code; v2 (H, A, B, D), the opponent in our experiments, was a
separate copy of our earlier code and is not needed to run the agent.

## Parameters

| JSON key | Default | 3 players | 4 players (`fourPlayer`) | Meaning |
|---|---|---|---|---|
| `budgetType`, `budget` | `BUDGET_FM_CALLS`, 4000 | `BUDGET_TIME`, 900 (1 s file) or 430 (500 ms file) | as for 3 players | CPU time per decision in ms |
| `wallTimeLimitMS` | 0 (off) | 925 or 450 | as for 3 players | the search also stops after this much real time |
| `K` | √2 | 0.5 | 1.0 | UCB1 exploration constant |
| `heuristic` | TAG's game heuristic | `groupW.SushiGoHeuristic` with its default constants | the same class with `args` [15, 1, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3] | H; `args` = (σ, w, α, T, β, T_m, κ, δ, π, ν, γ), default (15, 0.7, 0.5, 4, 1, 0, 0, 0, 1, 2, 0), see Table I of the report |
| `useInformedRollout`, `rolloutEpsilon` | false, 0.2 | true, 0.2 | true, 0.2 | B |
| `rolloutToRoundEnd` | false | true | true | R (`rolloutLength` is then not used) |
| `useActionPruning` | false | true | true | P |
| `useMaxN` | false | true | true | M |
| `useDecoupledUCT` | false | true | true | S (needs M) |
| `useInformationSets` | false | true | true | I (needs M) |
| `terminalScale`, `sigmaRoundDecay`, `opponentScoreValue` | 0, 0, false | 0, 0, false | 2, 0.5, true | W2 (with w = 1 in `args`) |
| `useFastSim`, `leafRollouts` | false, 1 | false, 1 | true, 8 | F and L (F needs I, R and `SushiGoHeuristic`) |
| `fourPlayer` | none | the block | – | parameters for games with four or more players |

The other keys of the JSON files are BasicMCTS's own (`rolloutLength`, `maxTreeDepth`), TAG's (`breakMS`) or switches
of modifications that are off, explained next to their fields in `SushiMCTSParams`.

## Time limits

TAG measures a player's thinking time as the CPU time of its thread (real time on Windows). The budget in our files is
CPU time, as in BasicMCTS, and the wall-clock guard also stops the search after 925 ms (450 ms) of real time. In our
timing runs, which recorded every decision of our agent, a decision with the 1 s file used at most about 900 ms of
CPU time and took 925 ms of real time at the median; the rare longer ones were pauses of the Java virtual machine
(garbage collection, compilation), which a larger heap, for example `-Xmx4g`, makes rarer when many games run at
once.

## Results

The final agent on deals not used in development (500 games each, against our earlier agent v2 and TAG's ISMCTS
agent; Table III of the report):

| Setting | Win % [95% CI] | Score margin | Chance |
|---|---|---|---|
| 3 players, 100 ms | 52.6 [48.2, 56.9] | +0.73 ± 0.46 | 33.3% |
| 3 players, 500 ms | 47.2 [42.9, 51.6] | −0.10 ± 0.40 | 33.3% |
| 3 players, 900 ms | 44.8 [40.5, 49.2] | −0.35 ± 0.39 | 33.3% |
| 4 players, 100 ms | 44.4 [40.1, 48.8] | −0.61 ± 0.43 | 25% |
| 4 players, 500 ms | 36.5 [32.4, 40.8] | −1.83 ± 0.45 | 25% |

Score margin: our score minus the best opponent's score (± 95% CI). The 100 ms rows used `json/SushiMCTS_100ms.json`
(with four players it has K = 0.5 and X0 with w = 0.7, without W2, F and L); the other rows used the settings of the
competition files with that CPU budget and no wall-clock guard.
