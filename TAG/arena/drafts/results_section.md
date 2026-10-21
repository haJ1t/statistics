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
Pairing helps little in Sushi Go (the win/loss correlation of two agents on the same deal is 0.1–0.2): a
difference between two 500-game runs has a CI of about ±5.5 win points, between two 250-game runs about ±8.
The score margin is about twice as precise and was used for screening.

**Development and validation.** Every decision was made on the deals of seeds 1 and 2. The final configuration
was then played on new deals (seed 7) that were not used for any decision, so the validation numbers are not
inflated by picking the best of many noisy runs.

**Number of games.** 500 games per condition, except 3 players / 900 ms and the 4-player / 500 ms development
runs, which have 250. A 3-player game at 900 ms needs about 3 × 27 decisions × 0.9 s ≈ 73 s of CPU time and a
4-player game at 500 ms 4 × 24 × 0.5 s ≈ 48 s; on our 18-core laptop a 250-game run took 45–75 minutes with
8 games in parallel, and the study has about 100 runs. We therefore used 250 games at these budgets and
report the correspondingly wider intervals (about ±6 instead of ±4.4 points for a win rate). The final
4-player / 500 ms agent was validated with 500 games.

### Validation of the final agent (seed 7, new deals)

‹VALIDATION›

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
10.2 win points on the new deals (5.6 † on the development deals of seed 1).

### Four players

The best settings for 3 and 4 players differ, so the agent switches parameters by table size (`fourPlayer`):

* **X0** (card model tuned for 4 players with a search-free tuner): +3.2 win points at 4 players / 100 ms
  (46.4 † → 49.6 %), but −4.9 at 3 players / 100 ms (55.7 † → 50.8 %).
* **W2** (value shape for 4 players): +3.4 ± 8.6 at 4 players / 500 ms (seed 1, 42.2 → 45.6 %), but with
  3 players at 500 ms adding X0 + W2 to v4 + F + L8 costs 7.7 ± 5.4 points (z = −2.8; 49.5 → 41.8 %).
* **F + L8** (fast simulator, 8 rollouts per leaf): +1.0 ± 7.9 at 4 players / 500 ms (seed 2, W2 42.8 → 43.8 %),
  −6.6 ± 6.0 (z = −2.2) at 4 players / 100 ms, −0.2 ± 6.3 † at 3 players / 500 ms. F makes 7–11 times more iterations per
  second, yet more iterations help little: at 4 players / 500 ms the tree has about 12 000 iterations, 700
  joint actions at the root, 45 % of the depth-1 nodes are visited only once and the mean depth is 1.6. The
  search is essentially a decoupled bandit at the root plus rollouts, and its quality is limited by the
  rollout policy and the evaluation, not by the number of iterations. L8 spends the extra speed on better leaf
  estimates instead.

Nothing else improved the 4-player agent at 500 ms (seed 2, 250 games each, paired with W2 + F + L8):

‹FOUR500›

Giving *only our agent* 1000 ms (and K 1.37) made it worse (score margin −1.30 ± 0.77). Every variant in
this table has a lower score margin than the base run (paired differences −0.33 to −2.67), so the base run
(−0.71) was probably lucky; the validation run on new deals (−1.53 ± 0.46) is the better estimate of the final
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

‹FRAMEWORK›

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

## TAG version

Requires a TAG version from November 2024 or later (`SGGameState.isHandKnown`). In older
versions the method was called `hasSeenHand` – pull the latest framework instead of renaming.
