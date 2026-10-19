#!/bin/zsh
# Writes RESULTS.md (narrative + every table from the CSV files). usage: arena/make_results.sh [out.md]
DIR=${0:A:h}
OUT=${1:-$DIR/RESULTS.md}
{
cat <<'EOF'
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

EOF
"$DIR/make_tables.sh"
echo "### 2^4 factorial analysis: R, M, K 0.5 and T 8 on top of HBDP (100 ms, seed 1, 16 cells x 500 paired games)"
echo
( cd "$DIR/results" && python3 ../factorial.py )
echo
cat <<'EOF'
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
EOF
} > "$OUT"
echo "wrote $OUT"
