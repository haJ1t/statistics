# groupA – SushiMCTS agent for Sushi Go! (ECS7032P Assignment 1)

> **Rename first:** replace every `groupA` (package line of the 5 `.java` files, the folder name,
> and the `class` / `heuristic` entries of the JSON files) with your real group letter, e.g. `groupK`.

## What the agent is

`SushiMCTSPlayer` is a modified copy of the framework's **`players.basicMCTS.BasicMCTSPlayer`**
(an allowed base agent). It does **not** use, copy or import anything from the advanced
`players.mcts` package (`MCTSPlayer`, `SingleTreeNode`, `MCTSParams`, `MCTSEnums`).

| File | Copied from | Purpose |
|---|---|---|
| `SushiMCTSPlayer.java` | `BasicMCTSPlayer` | entry point; Modification D (information-aware determinisation) in `_getAction` |
| `SushiTreeNode.java` | `BasicTreeNode` | MCTS tree; Modification A (progressive bias) in `expand`/`ucb`, Modification B (informed rollouts) in `rollOut` |
| `SushiMCTSParams.java` | `BasicMCTSParams` | all original parameters + on/off switches for the ablation study |
| `SushiGoHeuristic.java` | new | state evaluation H(s, i) |
| `CardValueModel.java` | new | shared Sushi Go knowledge: estimated scores and marginal card values |

## How to construct the agent in code

```java
SushiMCTSParams params = new SushiMCTSParams();
params.heuristic = new groupA.SushiGoHeuristic();
params.useProgressiveBias = true;   // A
params.useInformedRollout = true;   // B
params.useDeterminisation = true;   // D
params.budgetType = PlayerConstants.BUDGET_TIME;
params.budget = 900;                // ms per decision
AbstractPlayer agent = new SushiMCTSPlayer(params);
```

or from JSON: `PlayerFactory.createPlayer("src/main/java/groupA/json/competition/SushiMCTS_1000ms.json")`.

## Parameters

| JSON key | Default | Meaning |
|---|---|---|
| `K` | 1.41 | UCB1 exploration constant |
| `rolloutLength` | 10 | rollout steps after leaving the tree |
| `maxTreeDepth` | 100 | maximum tree depth |
| `heuristic` | – | `{"class": "groupA.SushiGoHeuristic"}` |
| `useProgressiveBias` | false | Modification A |
| `biasWeight` | 1.0 | W in `W * b(a) / (n + 1)` |
| `priorScale` | 5.0 | `b(a) = tanh(mv(a) / priorScale)` |
| `useInformedRollout` | false | Modification B |
| `rolloutEpsilon` | 0.2 | probability of a random rollout move |
| `useDeterminisation` | false | Modification D |
| `nDeterminisations` | 4 | maximum number of determinisations M_max |
| `adaptiveDeterminisation` | true | M depends on number of unknown opponent hands (false = always M_max while any hand is unknown) |
| `budgetType`, `budget`, `breakMS` | – | as for every TAG agent |

## Competition configuration

* `json/competition/SushiMCTS_1000ms.json` – 1 s per decision (budget 900 ms, safety margin).
* `json/competition/SushiMCTS_500ms.json` – 500 ms per decision (budget 430 ms).

The guidelines mention both settings (3 players / 1 s and 4 players / 500 ms): use the one the
module team confirms. Submit only one file as *the* competition JSON.

## Running the experiments

From the TAG root folder (working directory = repository root):

```
java -cp <classpath> evaluation.RunGames config=src/main/java/groupA/json/runs/E1_HA.json
```

Each `runs/*.json` file is a RunGames configuration (game, player count, focus player,
opponents, number of games, output folder). See the plan document, section 6.

## TAG version

Requires a TAG version from November 2024 or later (`SGGameState.isHandKnown`). In older
versions the method was called `hasSeenHand` – pull the latest framework instead of renaming.
