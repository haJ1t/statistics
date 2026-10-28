# Review 5: ECS7032P Assignment 1, Group A ("Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!")

I marked this independently, as the module lecturer would. I did not read `report/reviews/`. I read the brief, the rubric, the assessment overview, the writing tips, the PDF (rendered at 80 and 150 dpi), `build_report.py`, `make_figures.py`, the `groupA` Java, README and JSON files, and the v3/v4/v5 experiment records. I recomputed every number in the paper with my own script, written from scratch (`/tmp/claude-0/review5/recompute.py`; its output is in `/tmp/claude-0/review5/recompute_out.txt`). I also tested every branch of the pending-result wording by building the actual PDF in a sandbox with synthetic results for the four pending runs. Nothing under `/home/user/statistics` was modified.

**Verdict in one paragraph.** The experimental work is exceptional for this module: 163 complete runs and 70 050 games, a held-out deal block, paired designs, leave-one-out and factorial ablations, Holm corrections, power statements and six opponent types. Apart from two approximations, every number I could trace is correct. Two kinds of problem keep the mark from the top band:

- **Interpretation errors.** The paper misreads its own budget data ("its development decline did not replicate" is contradicted by seed block 7 and by the held-out deals). It also overclaims "every component", and it presents several non-significant or confounded results as findings.
- **Readability.** The Results are a wall of letter codes and intervals.

The pending-result wording has one wrong branch (the abstract), one asymmetric branch, and a hard-coded sentence that contradicts several outcomes. With the current wording, the pending text leaves only 1–4 lines of room on page 4.

**My mark now: 79/100**, assuming the pending numbers are filled in. **After the must-fixes: about 81–82. After the should-fixes as well: about 83–85.** A 90+ needs more than fixes; see Section 7.

---

## 1. Marks per rubric item

Weights come from *Assessment Overview*: Abstract 5%, Problem Definition (Introduction and Background) 15%, Approach (Methods and code) 30%, Experimentation 30%, Conclusions 10%, General 10%. The rubric gives no weights within an area, so I weight its items equally. Every item reaches the 70–100 band descriptor; the mark places it within that band.

| Area (weight) | Item | Band | Mark | Reasons (quotes from the PDF) |
|---|---|---|---|---|
| 1 Abstract (5%) | Brief motivation | 70–100 | 78 | "players pick cards simultaneously from hidden hands that are passed on, which basic Monte Carlo tree search (MCTS) does not model": correct and succinct. It states the problem but not why it matters beyond this game (simultaneous moves plus hidden information are common in tabletop games). |
| | Brief summary of the work | 70–100 | 80 | All components are listed. "To BasicMCTS of the Tabletop Games (TAG) framework we added …" is awkward. The abstract is at 125 words, the cap. |
| | Outline of findings | 70–100 | 72 | It gives held-out win rates and the ablation result, but "In leave-one-out ablations every component improves the score margin significantly" is false for P and M, which were never removed. It omits findings that are arguably more interesting (84–87% wins against BasicMCTS/RHEA/OSLA, the DUCT comparison, the loss of edge at longer budgets). The significant-H2 branch names the wrong baseline and runs to 129 words (Section 3.4). |
| 2 Problem definition (15%) | Motivation of the work | 70–100 | 76 | Motivated by MCTS's assumptions [2], [3] and TAG [1], [4]. Importance beyond Sushi Go! is only implied. |
| | Clarity of the problem definition | 70–100 | 82 | The four properties (i)–(iv) and RQ1–RQ3 are crisp. Minor: "give 729 or 4 096 joint moves per turn" holds only for the first turn of a round, before identical cards are merged. |
| | Clarity of contributions | 70–100 | 74 | The bullet list is clear. "leave-one-out and factorial ablations … that locate the agent's remaining limits" and "a diagnosis of why time beyond 500 ms helps little" are partly descriptive. The contribution to knowledge is argued only weakly. |
| | Critical analysis of the literature | 70–100 | 77 | Genuinely critical: DUCT's non-convergence and exploitability [10], game-dependence [11], PIMC properties [7], strategy fusion [8], SO- vs MO-ISMCTS [3], [9], and a gap statement. Breadth is limited: one prior Sushi Go! paper, 7 Wonders as the only drafting game, no opponent-modelling literature. The reference list is at the 15-reference cap. |
| | Description of the game | 70–100 | 83 | Accurate against `SGParameters`: 108 cards, Dumplings 1/3/6/10/15, Pudding ±6, 12 − n cards. Good point that hidden information is concentrated at the start of a round. |
| 3 Approach (30%) | Understanding and application (report and code) | 70–100 | 83 | The description matches the code (Section 3.1). The package compiles on its own against TAG and imports nothing from `players.mcts`. Imprecisions: "Statistics are keyed by card type"; eq. (4) is only the I form; the expansion rule is not stated; "P and M cannot be removed" is not explained. The code is very large (`SushiTreeNode.java` has 1 753 lines, about ten switches that are off) and is acknowledged as LLM-written. |
| | Originality | 70–100 | 74 | A sensible, well-argued combination of DUCT, SO-ISMCTS and max-n with a game-specific card-value model shared by the rollouts and the evaluation. The paper itself says "The search techniques come from the literature". The one new game-specific idea (denial) did not help, which is fine to report. |
| | Difficulty and ambition | 70–100 | 85 | Advanced tree search with substantial development: an exact fast simulator, 4-player settings and wall-clock guards. |
| 4 Experimentation (30%) | Adequacy and rigour | 70–100 | 87 | Held-out deal block, paired designs, five framework opponents (ISMCTS, DUCT, BasicMCTS, RHEA, OSLA) plus the base versions v2 and v3, leave-one-out ablation plus a 2⁴ factorial, 500 games per condition (250-game runs justified). Detractions: the confirmatory H2 test is underpowered by design (80% power only for about 11–12 pp, while development suggests about 3 pp at 500 ms); some 4-player conclusions rest on 250-game runs against a base run the team's own README calls "probably lucky". |
| | Documentation | 70–100 | 80 | Table II, seeds, deal procedure, seating and opponent settings would allow replication. Gaps: "Agent files, per-game results and scripts: from the authors" (no URL); no TAG version; "250 at 900 ms (development, s = 7)" is ambiguous. |
| | Critical analysis of results | 70–100 | 73 | The error analysis is excellent: Wilson intervals, paired SEs, power, selection effects ("the development rise is probably overstated"). But: (a) "its development decline did not replicate" is wrong (Section 3.3); (b) "so H1 holds for the margin" and "every/any component" overclaim; (c) "I adds a further 3.8 ± 5.7 pp, a small gain, as Long et al.'s analysis [7] suggests" treats a non-significant result as confirmation; (d) "This supports H3" despite a post-hoc contrast whose two arms differ in F + L; (e) "and so does time up to 500 ms … (z = 2.3)" is one of 19 comparisons against one base run, not significant after Holm, and the time effect is confounded with the agent's settings. |
| 5 Conclusions (10%) | Summary | 70–100 | 72 | Covers RQ1–RQ3 and the limitations. It repeats the overclaim ("removing any component lowers the final agent's score margin significantly"). It omits the final agent's own drop at 500 ms, the most robust RQ2 result in the data, and the 4-player 500 ms held-out result. |
| | Future work | 70–100 | 80 | Reasoned: a learned evaluation, because hand-tuning has saturated; RM/EXP3 justified by [10], [11]; opponent models fitted to observed picks. |
| 6 General (10%) | Clarity of expression | 70–100 | 71 | Logical structure and precise wording, but very dense. Results are about 1 030 body words against a 500–700 guide. Single-letter codes collide with symbols (Section 5). Author placeholders remain. |
| | Figures, tables, legends | 70–100 | 82 | Fig. 2 (both measures, Holm-filled markers, two families) is very good; Fig. 1 is a useful conceptual figure; Fig. 3 is clear. "denial in rollouts" is undefined. Fig. 3's caption promises v3 held-out markers that do not exist yet. The x axis is categorical (100/500/900 equally spaced). |
| | Referencing | 70–100 | 80 | Consistent IEEE style, relevant, and accurate against the sources (Section 3.2). No citation for the Holm procedure. |

Area means: Abstract 76.7, Problem definition 78.4, Approach 80.7, Experimentation 80.0, Conclusions 76.0, General 77.7.
**Weighted total = 0.05·76.7 + 0.15·78.4 + 0.30·80.7 + 0.30·80.0 + 0.10·76.0 + 0.10·77.7 = 79.2 ≈ 79.**

Risk outside the rubric: the GenAI statement says the paper "was generated" from a prompt asking for "a report that gets 100", and that the code was written by Claude. This is allowed when acknowledged, but some markers will let it colour their view of "Originality" and "Understanding". The individual reflections will have to describe real human contributions.

---

## 2. Recomputation of every number

**Method.** Win rate = mean of `focusWin` (1/k for a k-way shared first place), with a Wilson 95% interval. Score margin = mean of `focusScore − bestOppScore` ± 1.96 SE. Paired differences use per-game differences matched by `game`. Independent runs (pre-19-October runs on TAG's own deals) use the two-sample SE; pooled runs use the pooled sample variance. Holm is applied to two-sided normal p-values. I first checked the joined runs:

- My own merge of `r2_val_3p500_c0…c3` (offsets 0/126/252/378) is identical to `r2_val_3p500.csv`: games 0–499, and `focusSeat == g mod 3` throughout.
- `v5_field_3p100_all.csv` equals `v5_field_3p100.csv` plus `r2_field_3p100b.csv` shifted by 300.
- Every v5 CSV is byte-identical to its copy in `TAG/arena/v5/results`.
- Rounds 2 and 3 are dealt in order from the fixed draw pile (`SGForwardModel._startRound`: no reshuffle unless the pile is empty, which cannot happen), so "equal s, equal deals" holds for the whole game.

Status: ✓ = matches; ≈ = rounded or approximate but defensible; ✗ = wrong.

| # | Report value (location) | Recomputed | Source | Status |
|---|---|---|---|---|
| 1 | 52.6 [48.2, 56.9], +0.73 ± 0.46 (Abstract, Table III, Concl.) | 52.6 [48.2, 56.9], +0.73 ± 0.46 | `v5/results/r2_val_3p100.csv` | ✓ |
| 2 | 47.2 [42.9, 51.6], −0.10 ± 0.40 | same | `r2_val_3p500.csv` (= my chunk merge) | ✓ |
| 3 | 3p/900, 4p/500 "pending" | files absent | `r2_val_3p900*`, `r2_val_4p500*` | pending |
| 4 | 44.4 [40.1, 48.8], −0.61 ± 0.43 | same | `r2_val_4p100.csv` | ✓ |
| 5 | DUCT row 55.0 [50.6, 59.3], +0.72 ± 0.40 | same | `r2_duct_3p100.csv` | ✓ |
| 6 | ISMCTS × 2: 51.3 [46.9, 55.7], +1.41 ± 0.51 | same | `v5_field_3p100_all.csv` | ✓ |
| 7 | 84.2 [80.7, 87.1], +6.68 ± 0.60 | same | `v4/results/B_mcts_rhea.csv` | ✓ |
| 8 | 87.2 [84.0, 89.8], +9.74 ± 0.75 | same | `B_osla_mcts.csv` | ✓ |
| 9 | "7.7 percentage points" (Abstract); 7.7 ± 5.4 (z = 2.8) | −7.7 ± 5.4 (z = −2.80), paired | `f5_FL8` → `f5_W2FL8` | ✓ |
| 10 | 55.0% at 100 ms on seed block 7; 58.1% re-run | 55.0; 58.1 | `V_3p100`, `v5_base3p100` | ✓ |
| 11 | "2.4 ± 6.1 pp and 5.5 ± 6.1 pp … neither significant" | +2.4 ± 6.1 (z 0.76); +5.5 ± 6.1 (z 1.77), two-sample | as #1, #10 | ✓ (z = 1.8 is borderline: see should S10) |
| 12 | DUCT 21.0% of seats, "the same share as v2" | 21.0 and 21.0 | `r2_duct_3p100.log`, `r2_val_3p100.log` | ✓ |
| 13 | "at most 12.8% of their seats; OSLA none" | BasicMCTS 12.8 / 9.0, RHEA 6.8, OSLA 0.0 | `v4/results/val16.log` | ✓ |
| 14 | −H 28.3 ± 5.8; −S−I 10.2 ± 5.7 (z = 3.5); −I 3.8 ± 5.7 | −28.3 ± 5.8 (z 9.57); −10.2 ± 5.7 (z 3.47); −3.8 ± 5.7 (z 1.30) | `A_noH/A_noSI/A_noI` vs `V_3p100` | ✓ |
| 15 | "−R: z = 2.0, −B: 1.4, −I: 1.3" | 2.04, 1.43, 1.30 | `A_noR/A_noB/A_noI` | ✓ |
| 16 | Holm: all five margins survive; win only H, S + I | margin p = 0.0000, 0.0000, 0.0008 (B), 0.0164 (R), 0.0250 (I): all pass; win: R p = 0.042 > 0.0167 stops | same | ✓ |
| 17 | Lead heuristic 13.6 ± 5.9 (z = 4.5) | −13.6 ± 5.9 (z 4.54); margin −1.45 ± 0.60 | `r2_lead_3p100` vs `v5_base3p100` | ✓ |
| 18 | S alone +6.4 ± 5.7 (z = 2.2) | +6.4 ± 5.7 (z 2.20) | `A_noSI` → `A_noI` | ✓ |
| 19 | Factorial: R +8.7 (z = 8.1), K +5.2, M +4.1, T +3.6 "(each ±2.1)"; R × T −4.2, M × K +3.2 | R 8.65 ± 2.10 (z 8.06), K 5.15 ± 2.15, M 4.10 ± 1.99, T 3.65 ± 2.02; R×T −4.22 (z −3.97), M×K +3.17 (z 2.83); others \|z\| ≤ 1.06 | 16 cells (`s2_base` … `s4_K0.5_T8_M`, v3 results) | ≈ ("each ±2.1" is really ±2.0–2.2) |
| 20 | "best of the 16 factorial cells"; "45.1–49.4% on three new seed sets" | max cell `abl_T4` 50.1; 49.4 / 47.5 / 45.1 | `rep_T4`, `tb1_T4`, `tb2_T4` | ✓ |
| 21 | Four candidates "at most 1.8 pp (each ±5.8)"; R3V2 23.7 ± 5.8 (z = 8.0), only one surviving Holm | −1.3, −1.8, −0.6, +0.7 (±5.8 each); −23.7 ± 5.8 (z 8.00); Holm keeps only R3V2 (and the lead test in the figure's family) | `v5_*_3p100` | ✓ |
| 22 | "45.0% of greedy games against two X0 players" | X3 45.00% ± 1.06 (pool {X0}, n = 3) | `greedy_duels.txt` | ✓ |
| 23 | v2 24.8% → v3 50.1% | 24.8 → 50.1 (+25.3 ± 5.8) | `s1_ref`, `abl_T4` | ✓ |
| 24 | v3 lost 17.3 ± 7.2; final 8.1 ± 7.5 | −17.3 ± 7.2; −8.1 ± 7.5 (two-sample) | `abl_T4`→`f9_finalT4`; `d1_I`→`f9_I` | ✓ |
| 25 | S + I +5.6 ± 6.1 → +14.8 ± 8.4, rise +9.2 ± 10.4 (z = 1.7) | +5.6 ± 6.1; +14.8 ± 8.4; +9.2 ± 10.4 (z 1.73) | dev runs | ✓ |
| 26 | S alone +1.3 ± 6.1 (100) vs +6.2 ± 6.1 (500) | ✓; their difference is +4.9 ± 8.6 (z 1.1), not reported | `d1_S`, `f5_S` | ✓ (but over-read) |
| 27 | 11 170 and 11 865 iterations per decision | same | `probe_iterations.txt` | ✓ |
| 28 | Seed block 7: 55.0 / 49.4 / 56.0% | same | `V_3p100/500/900` | ✓ (but see 3.3) |
| 29 | Held-out −5.4 ± 5.9 pp at 500 ms; margin fell 0.83 ± 0.58 | −5.4 ± 5.9 (z −1.78); −0.83 ± 0.58 (z −2.82) | `r2_val_3p100` → `r2_val_3p500` | ✓ |
| 30 | ISMCTS seats 23.1% → 37.4%; vs v3 33.0% → 46.0% | same | `dev100c.log`, `dev900.log`, `ablation100.log`, `confirm900.log` | ✓ |
| 31 | 4p: X0 +4.3 ± 4.3; W2 +5.8 ± 7.4; F + L +1.0 ± 7.9; F + L −6.6 ± 6.0 at 100 ms | +4.3 ± 4.3 (z 1.94); +5.8 ± 7.4 (z 1.54); +1.0 ± 7.9; −6.6 ± 6.0 (z −2.16) | `q4_*`, `val_q5`, `q5_W2`, `q5_W2r`, `q5_FL8`, `q4_X0FL8` | ✓ |
| 32 | 13.5 ± 9.1 (z = 2.9) | +13.5 ± 9.1 (z 2.89) | #9 and #31 | ✓ |
| 33 | Depth-1 −17.4 ± 8.2; 100 ms −9.0 ± 7.7 (z = 2.3); 1 000 ms −4.8 ± 8.1 | −17.4 ± 8.2 (z −4.16); −9.0 ± 7.7 (z −2.28); −4.8 ± 8.1 | `q5_FL8d1`, `q5_X0b100`, `q5_FL8t1000K14` vs `q5_FL8` | ✓ |
| 34 | 73 positions; 704 children; 53% visited once; depth 1.5; 2, 4, (4), (7) | 73; 704; 53%; 1.53; 73·(1 − 0.973) = 1.97, 73·(1 − 0.945) = 4.02 (twice), 73·(1 − 0.904) = 7.01 | `bench4/treestats4/agree4.txt` | ✓ |
| 35 | ±4.4 pp for 500 games | 4.38 | formula | ✓ |
| 36 | Paired difference "about ±5.7 pp" | 5.4–6.0 across paired 500-game comparisons (5.7–5.9 in the ablation) | all paired pairs | ≈ |
| 37 | Correlation of outcomes "0.0–0.2" | +0.009 to +0.220 | same pairs | ✓ |
| 38 | "about 8 pp with 80% power" | 2.8 × 5.7/1.96 = 8.1 | formula | ✓ |
| 39 | Margin z "1.0–2.3 times as large in the ablation" | 1.18 (−R), 1.56 (−H), 1.70 (−S−I), 1.73 (−I), 2.34 (−B) | ablation runs | ✗ (should be 1.2–2.3) |
| 40 | "59 of our 163 runs" | 163 runs ≥ 200 games (deduplicated, joined runs counted once), 59 four-player; 70 050 games; 47 runs < 500 games | all CSVs | ✓ |
| 41 | "45–70 s of CPU time per game" | 4p/500: 90.4 decisions × 0.5 s = 45 s; 3p/900: 77.3 × 0.9 s = 70 s | `nDecisions` column | ✓ |
| 42 | 729 / 4 096 joint moves | 9³ / 8⁴ | arithmetic | ≈ (first turn of a round only) |
| 43 | Table I note: X0 tuple and default tuple | X0 = JSON `args`; default = `CardValueModel` constants | JSON, code | ✓ |
| 44 | Fig. 2 points and filled markers | all 11 rows and both Holm families match my values | as #14–#21 | ✓ |
| 45 | Fig. 3 points | 24.8/18.3/18.8; 50.1/40.7/32.8; 55.7/49.7/47.6; held-out 52.6, 47.2; Wilson bars | dev runs, held-out runs | ✓ |
| 46 | Table II opponent settings | ISMCTS = `SushiGo_3+P_HiBudget` at our budget (MultiTree, UCB-Tuned, K 1, random 300-move rollouts, score heuristic); DUCT = `SushiGoDUCT` (decoupled, `MCTSParams` defaults K 1.0 and Information_Set, LeaderHeuristic, 30-move rollouts) | arena agent JSONs, TAG `MCTSParams` | ✓ |

**Mismatches and untraceable numbers.** Only #39 is wrong (the lower bound 1.0 has no source; the data give 1.18). Two are approximations (#19 "each ±2.1"; #36 "±5.7"). Many numbers are hard-coded in `build_report.py` rather than computed: 28.3, 10.2, 3.8, the factorial effects, 45.0, 45.1–49.4, 0.0–0.2, 1.0–2.3, ±5.7, 8 pp and "16". I verified all of them except 1.0, but they will go stale silently if the data change, and #39 contradicts the GenAI statement that "every number is computed from the result files".

---

## 3. Technical, literature and inference checks

### 3.1 Description against the Java code (`TAG/src/main/java/groupA`, identical to `groupA_code/groupA`)

- **Eq. (1)–(3) match the code exactly.** `CardValueModel.estimatedScore` gives banked score + α·potential + Maki standing + π·w_t·Pudding standing, with w_t = (round + 1)/3, the completion factor min(1, (r − k + 1)/T) (0 if r < k), Wasabi (3 − 1)·ν, Chopsticks 1·c(r, 2), and the defaults α 0.5, T 4, π 1, ν 2. `SushiGoHeuristic` gives tanh(Δ/σ) with w = 0.7 and σ = 15.
- **B, R, P, M and S match.** B is ε = 0.2 greedy on the marginal value for every player, with the same ε for the second Chopsticks card. R ends at the round's scoring. P gives one branch per (type, icons, Chopsticks) key. M is per-player values, so no paranoid minimisation. S is one node per turn with per-player UCB and joint children.
- **I matches.** It uses `state.copy(me)` every iteration, an open-loop tree, availability counted per card in the sampled hand, `Q + K·sqrt(ln N_avail / n)`, untried cards first, and the most visited root card.
- **The 4-player settings match** the JSON and `valuesOf`/`terminalValues`/`roundScale`: X0, W2 (w = 1, tanh(Δ/2) with 0.01·Pudding tie-break, σ·(ρ/2)^0.5, opponents valuing their lead over the others' mean), F, L = 8, K = 1.
- **Imprecisions to fix:**
  - "Statistics are keyed by card type": the keys are (type, Maki icons, Chopsticks use).
  - Eq. (4) is the I form. S without I (the −I arm) uses ln(N_node + 1).
  - The expansion rule (one new node per iteration, at the first unseen joint choice) is never stated.
  - "P and M cannot be removed" needs its reason: S and I require M, and I's keys already merge identical cards.
  - Inside the tree the second Chopsticks card is the greedy card, not searched.
- **Code-package checks.**
  - `javac` of `groupA/*.java` alone against TAG's classes succeeds; no `players.mcts` import; no framework file changed after the import commit.
  - The README explains the constructor and the JSON files; the competition file is `json/competition/SushiMCTS_1000ms.json` (900 ms CPU, 925 ms guard).
  - Problems: the README opens with "**Rename first:** replace every `groupA` … with your real group letter" (contradicts "Group A"); several numbers differ from the paper ("about 100 runs" vs 163, "±5.5" vs "±5.7", 46.3 vs 46.2%); there are many legacy JSONs (`json/agents`, `runs`, `opponents`); `expand()` inherits BasicTreeNode's `new Random(seed)` quirk (not used by the final 3-player agent's main path).

### 3.2 Literature claims against the sources (no misreporting found)

| Claim in the paper | Source says | Verdict |
|---|---|---|
| Klang et al. [4] "introduced the game to TAG … concluded that better heuristics should improve the agents" | Abstract: implemented Sushi Go! in TAG; agents achieved mixed results; better heuristics expected to improve them | ✓ |
| Lanctot et al. [10]: RM and outcome sampling best, DUCT strategies most exploitable | Abstract: "regret matching and OOS perform best … all variants produce less exploitable strategies than UCT" | ✓ |
| Tak et al. [11]: nine games; best variant depends on game and opponent; DUCT best overall | Abstract states exactly this (CIG 2014, pp. 232–239) | ✓ |
| Sturtevant [12]: UCT in multi-player games computes a mixed-strategy equilibrium and does at least as well | Abstract states exactly this | ✓ |
| Long et al. [7]: success related to how quickly hidden information is revealed | Disambiguation factor (with leaf correlation and bias) | ✓ |
| Silver and Tesauro [6]: rollouts should be balanced / unbiased | Simulation balancing minimises the error of the mean outcome rather than maximising policy strength | ✓ |
| Frank and Basin [8]: strategy fusion and non-locality | ✓ | ✓ |
| Robilliard et al. [13]: 7 Wonders; "many results known from classic games held there too" | Abstract: "a lot of known results still hold" | ✓ |
| Cowling et al. [3]: SO-ISMCTS opponents not conditioned on their own private information; MO-ISMCTS has one tree per player at higher cost | A fair paraphrase of the SO/MO distinction | ✓ (acceptable) |

(Fetching ludii.games and mlanthology.org was blocked by the session's network settings; I checked against the published abstracts found through search.)

### 3.3 Conclusions against the statistics next to them

1. **RQ2 misreading (must fix).** The paper says: "On seed block 7 the final agent won 55.0%, 49.4% and 56.0% at 100, 500 and 900 ms: its development decline did not replicate." Seed block 7's own paired data show the same 500 ms drop as development:

   | Deal set | 500 ms − 100 ms win rate | Score margin |
   |---|---|---|
   | Development (seed 1, independent) | −6.0 ± 6.1 (z −1.9) | – |
   | Seed block 7 (paired) | −5.6 ± 5.9 (z −1.84) | −0.59 ± 0.54 (z −2.1) |
   | Held-out (paired) | −5.4 ± 5.9 | z −2.8 |
   | Seed 7 + held-out, inverse-variance pooled | **−5.5 ± 4.2 pp (z −2.6)** | – |

   Only the 250-game 900 ms point on seed block 7 shows no drop (+1.6 ± 8.7, paired). On the held-out deals TAG's ISMCTS seat share rises from 26.4% to 35.9% between 100 and 500 ms, and v2's falls from 21.0% to 16.9%; the paper cites only the development seat shares. So the robust RQ2 finding, which the abstract and conclusions never state, is that **the agent's edge shrinks with time because the opponent gains more**.
2. **"every component" / "any component" / "H1 holds for the margin" (must fix).** Only five removals were run (H, S + I, R, B, I). P and M were never removed from the final agent, and in the v3 study −P was −0.62 ± 0.63 on the margin, which is not significant.
3. **"I adds a further 3.8 ± 5.7 pp, a small gain, as Long et al.'s analysis [7] suggests"** presents a non-significant estimate as confirmation of theory (should).
4. **"This supports H3"** (should). The contrast was chosen after the four-player runs, the four-player gain is not significant (+5.8 ± 7.4), and the arms differ: the 3-player arms both have F + L; the 4-player arms (`val_q5` vs `q5_W2`/`q5_W2r`) have neither. "Consistent with H3, exploratory" is the most it supports.
5. **"and so does time up to 500 ms: with 100 ms (and its 100 ms settings) our agent won 9.0 ± 7.7 pp less (z = 2.3)"** (should).
   - In that 19-variant family against `q5_FL8`, only depth-1 survives Holm. The README itself says the base run "was probably lucky".
   - Only our agent had 100 ms; the opponents stayed at 500 ms, which the sentence does not say.
   - When everyone's budget changes (seed block 7, 4 players), the 500 ms configuration did *worse* than the 100 ms one: −5.4 ± 5.9 pp, margin −1.24 ± 0.61 (z −4.0).
6. **"four changed the win rate by at most 1.8 pp (each ±5.8)"** reads as bounding the effects. These are point estimates; the CIs allow about ±7 pp.
7. **"S alone gained +1.3 ± 6.1 pp at 100 ms but +6.2 ± 6.1 pp at 500 ms"** implies a contrast that is +4.9 ± 8.6 (z 1.1).
8. **Selection and multiplicity are otherwise handled well**: v3's 50.1% as the best factorial cell, the seed-7/development overlap, Holm in two families, the four-player work labelled exploratory. **Power:** state up front that the held-out H2 test can detect only about 11–12 pp (simulated 95% CI ±8.0–8.4 pp), while development suggested +3.4 ± 8.6 pp at 500 ms. A non-significant result is therefore expected and uninformative.

### 3.4 Pending-result wording, checked branch by branch

I built the real PDF with synthetic results for `r2_v3_3p100`, `r2_v3_3p500`, `r2_val_3p900` and `r2_val_4p500` in five scenarios (`/tmp/claude-0/review5/sim/run_scenarios.py`, `build_scenarios.py`).

| Outcome (RISE z) | `text_budget` verdict | `text_conclusion_1` | `_abstract_end` | Problems |
|---|---|---|---|---|
| Significant + (z = 2.6) | "which supports H2" ✓ | "grew … (z = 2.6)" ✓ | "**the decoupled information-set tree gains more over our previous agent** at 500 than at 100 ms" ✗ | "our previous agent" means v2 in the abstract, but the gain is over v3. The abstract becomes 129 words. It drops RQ3. |
| Non-significant + (z = 0.6) | "in the direction of H2 but not significant (this test detects only changes of about 11 pp or more)" ✓ | "grew …, as in development but not significantly (z = 0.6)" ✓ | four-player sentence ✓ | Power sentence lacks "with 80% power". The conclusion omits the final agent's own trend and the 4p/500 result. |
| Non-significant − (z = −1.1) | "so H2 is not supported on these deals" ✓, but prints "(z = 1.1)" for a negative difference although Table II defines z = difference/SE | "did not grow … (a difference that was not significant)" ✓, no z or power | four-player sentence ✓ | Only **1 line** of space left on page 4. |
| Significant − (z = −4.1) | "which contradicts H2" ✓ | "shrank … contrary to H2" ✓ | four-player sentence ✗ | A significant contradiction of the hypothesis is left out of the abstract while the positive case is put in (asymmetric reporting). The RQ1 sentence "10.2 pp over v3's … tree" then needs a reconciling clause. |
| 900 ms significantly lower (BUD z = −4.4) | — | — | — | "The final agent's own win rate … changed with the budget" contradicts the hard-coded "its development decline did not replicate" a few lines earlier; "changed" gives no direction; "its score margin fell by A and B" is printed even when B is not significant. |

Other branch issues:
- The abstract and conclusion hard-code 4p/100 only, so the 4p/500 held-out result never appears in prose.
- The Fig. 3 caption promises v3 held-out markers.
- `F3P900` prefers a stale `r2_val_3p900_all.csv`. `merge_parts.py`'s docstring describes offset 252 for it, which would skip games 250–251 and include 500–501, outside the held-out block.
- The interim branch prints "S and I gain −x" if only the 100 ms v3 run finishes.

---

## 4. Compliance

- **Page limit.** The main text ends on page 4 with **9.0 lines** to spare in the right column (measured from the PDF: line pitch 11.0 pt, bottom margin 72 pt). The GenAI statement starts on page 4 and runs onto page 5 with the references; total 5 pages ✓. With the current wording, the pending results leave **1–4 lines** across the five scenarios, and the GenAI heading moves to page 5, which is allowed. With my tested edit set (Section 6) they leave **7–9 lines**.
- **Template.** IEEE A4 Word template. Margins match the original strict-OOXML template (title 27 pt top, body 54 pt top, 44.65/45.35 pt sides, 72 pt bottom, 18 pt column gap). Liberation Serif is a metric-compatible substitute for Times ✓.
- **Section word guide (body words).** Abstract 125 (75–125 ✓, at the cap); Introduction 445 ✓; Background 685 ✓; Method 880 (700–800, plus Tables I–II); Results 1 032 (500–700, plus Table III and Figs. 2–3); Conclusions 251 ✓. Results is about 50% over the guide; this is the density problem.
- **References.** 15 (6–15 ✓, at the cap), consistent IEEE style. Holm (1979) is uncited.
- **≥ 500 games.** All held-out, ablation, candidate and validation runs have 500. The 250-game runs (900 ms development, seed block 7 at 900 ms, 4-player 500 ms development) are justified by CPU time ("45–70 s of CPU time per game"). That justification is thin given the scale of the study, but it is stated ✓.
- **Generative AI.** Citation [15] ✓; first-page note ✓; statement before the references ✓; main prompt verbatim with translation ✓; tournament prompt ✓. The revision prompts (the reviewer reports) are only summarised ("available on request"), and the next revision will be driven by this review too. For full compliance with "include the prompt(s) used to generate that text", put the verbatim review prompts and reports in the zip (for example `groupA/genai/`) and say so.
- **Code package.**
  - The package compiles alone; it is based on BasicMCTS (structure kept); `players.mcts` is used only as a benchmark opponent outside the package; README and competition JSON are present ✓.
  - **Must:** resolve the README's "Rename first" line, fill in the author block ("[Group member 2]", "Student no. [to be completed]") and replace every "pending".

---

## 5. Presentation

- **Readability.** The Results subsections pack 10–15 intervals per paragraph. Topic sentences rarely state the answer before the evidence. Codes (H, B, R, P, M, S, I, X0, X3, W2, F, L, v2/v3/v4) appear without reminders. For example, "X0 with W2 and K = 1 added +5.8 ± 7.4 pp at 500 ms (two runs pooled) and F + L a further +1.0 ± 7.9 pp, whereas F + L cost 6.6 ± 6.0 pp at 100 ms" is hard to parse. In "among them card constants tuned for three players (X3), a stronger greedy policy (45.0% …)" the apposition reads as a list of two items.
- **Undefined jargon.** "denial in rollouts" (Fig. 2) is never defined. "UCB-Tuned" (Table II) is undefined. "seed block" is only implicit. "Holm correction" has no citation.
- **Notation clashes.**
  - n is the number of players (§II-A, Table II), the visit count (UCB1, n_p(a)) and the number of games (Table III).
  - Modification codes collide with symbols: S/Ŝ, P/p, I/i, B/b_p, M/m_p, D/d_p.
  - w (leader weight) and w_t (Pudding weight) share a letter.
  - z is defined as difference/SE but printed as |z|.
- **Figures and tables.** Fig. 2 is excellent. Fig. 3 is clear, though its categorical x axis spaces 100/500/900 equally, and its caption depends on the pending v3 markers (regenerate with `make_figures.py`). Fig. 1 is a good conceptual figure. Table III is good. Table II is a table of prose; acceptable, but it is where to look for space.

---

## 6. Prioritised changes

**I implemented and tested all must-fixes and the length cuts as a patch to `build_report.py`:** `/tmp/claude-0/review5/proposed_build_report.diff` (sandbox result: `/tmp/claude-0/review5/sim/report/build_report_proposed.py`). I built it against five synthetic outcomes (`/tmp/claude-0/review5/sim/proposed_*.pdf`): the main text then ends on page 4 with **7–9 lines spare** in every case, against 1–4 with the current wording. The abstract stays at **121–124 words** in every branch. Line effects below are approximate per item; the measured total is what counts.

### Must (correctness or compliance)

| # | Change | Exact replacement text | Lines |
|---|---|---|---|
| M1 | Finish the pending runs. Run `finalize_r2.sh` and `make_figures.py`, rebuild, and make sure no "pending" remains. Fill in the author names and student numbers. Remove the README's "Rename first" line (or rename the package if the group letter is not A). | — | 0 |
| M2 | Abstract: five components, 4p/500, a correct and symmetric RQ2 ending, within 125 words | "In *Sushi Go!*, players pick cards simultaneously from hidden hands that are passed on, which basic Monte Carlo tree search (MCTS) ignores. We extended BasicMCTS of the Tabletop Games framework (TAG) with card-value rollouts, a lead-based evaluation, round-end rollouts, max-n backups, decoupled selection and information-set search. On held-out deals against TAG's information-set MCTS and our previous agent it wins {3p100}%, {3p500}% and {3p900}% of 500 three-player games at 100, 500 and 900 ms (chance 33.3%) and {4p100}% and {4p500}% of four-player games at 100 and 500 ms (chance 25%). Each of five components significantly improves the score margin; the evaluation and the decoupled information-set tree also the win rate." Then, if \|z_RISE\| ≥ 1.96: "Decoupling gained more/less over our sequential tree at 500 than 100 ms (\|z\| = …)." Otherwise: "Settings tuned for four players cost 7.7 percentage points with three (exploratory)." | −1 |
| M3 | RQ2: replace "On seed block 7 the final agent won 55.0%, 49.4% and 56.0% at 100, 500 and 900 ms: its development decline did not replicate." | "On seed block 7 it won 55.0%, 49.4% and 56.0% (900 ms: 250 games): its drop at 500 ms (−5.6 ± 5.9 pp, same deals) matched development, the 900 ms point did not." | +1 |
| M4 | RQ2: one sentence for the final agent across budgets (replaces "The final agent's own win rate on the held-out deals did not change significantly …") | "On these deals its own win rate changed by −5.4 ± 5.9 pp at 500 ms [and … at 900 ms] against 100 ms (score margin −0.83 ± 0.58 [and …]); pooled with seed block 7, 500 ms changed it by −5.5 ± 4.2 pp (z = 2.6)." Replace "Much of the fall of the curves comes from the opponent: TAG's ISMCTS won 23.1% … (against v3: 33.0% → 46.0%)." with "The opponent gains more from time: on the held-out deals TAG's ISMCTS won 26.4% of its seats at 100 ms and 35.9% at 500 ms." | −1 |
| M5 | Held-out H2 sentence, shorter and with explicit power | "On the held-out deals v3 won X% and Y% at 100 and 500 ms, so S and I gained A and B: a difference of D (z = …), {verdict} (80% power only for N pp or more)." | −1 |
| M6 | Conclusion RQ1/RQ2 | "For RQ1, removing any of five components lowers …"; the held-out sentence without "against TAG's ISMCTS and our previous agent" and with "and {4p100} and {4p500} of four-player games at 100 and 500 ms"; then "For RQ2, the final agent itself won 5.5 ± 4.2 pp less at 500 than at 100 ms (two paired deal sets pooled), while TAG's ISMCTS gained more from the time;" followed by one of four branches: "the gain from S and I grew {g} on the held-out deals (\|z\| = …), as H2 predicted." / "…shrank {g} … (\|z\| = …), contrary to H2." / "…grew {g} …, as H2 predicted but not significantly (\|z\| = …; 80% power only for N pp)." / "…did not grow on the held-out deals ({g}; \|z\| = …; 80% power only for N pp), so H2 is not supported." (The tested patch says "as TAG's ISMCTS gained"; I recommend the more neutral "while", same length.) | +2 |
| M7 | Results ablation: "so H1 holds for the margin and only partly for wins" | "so for these five components H1 holds for the margin and only partly for wins" | +0.5 |
| M8 | Setup: "(z values 1.0–2.3 times as large in the ablation)" | "(z values 1.2–2.3 times as large in the ablation)" | 0 |
| M9 | "and I adds a further 3.8 ± 5.7 pp, a small gain, as Long et al.'s analysis [7] suggests" | "and I adds a further, non-significant 3.8 ± 5.7 pp, as Long et al.'s analysis [7] predicts" | 0 |
| M10 | RQ3: "This supports H3, although the gains with four players were not significant; as the contrast was chosen after the four-player runs, it is exploratory." | "…different from that with four: consistent with H3, but exploratory (chosen after the four-player runs; four-player gains not significant; only the three-player arms had F + L)." | 0 |
| M11 | RQ3: "– and so does time up to 500 ms: with 100 ms (and its 100 ms settings) our agent won 9.0 ± 7.7 pp less (z = 2.3), but" | "– and time may matter up to 500 ms: with 100 ms (and its 100 ms settings) against opponents at 500 ms our agent won 9.0 ± 7.7 pp less (z = 2.3; one of 19 variants against this base run, not significant after Holm), but" | +1.5 |
| M12 | Later candidates (also defines "denial") | "…four had estimates within ±1.8 pp (each ±5.8, none significant), among them three-player card constants (X3, a far stronger greedy policy: 45.0% against two X0 players) and a denial term (rollout players also value taking the next player's best card); rollouts to the game end …" | +1 |
| M13 | GenAI statement: add this review round's prompt verbatim, and put all reviewer prompts and reports in the zip with a pointer ("verbatim in groupA/genai/") | — (page 5 has room) | 0 (main) |

### Cuts that pay for the additions (all in the tested patch)

| Cut | Replacement | Lines |
|---|---|---|
| C1 RQ2 opening: drop "v3 doubled v2's win rate at 100 ms (24.8% → 50.1%) but" (already visible in Fig. 3) | "In development runs (Fig. 3; independent samples) v3 lost 17.3 ± 7.2 pp from 100 to 900 ms, the final agent 8.1 ± 7.5 pp; …" | −1 |
| C2 Drop the over-read contrast "S alone gained +1.3 ± 6.1 pp at 100 ms but +6.2 ± 6.1 pp at 500 ms, and" | "…a wrong model that more iterations exploit more; v3 and the final agent ran similar numbers of iterations (11 170 and 11 865 per decision at 500 ms)." | −1.5 |
| C3 Factorial sentence (also fixes "on v3", "each ±2.1" and the unexplained "cannot be removed") | "P and M stay (S and I build on them); in a 2⁴ factorial on v3's base (R, M, K = 0.5 vs 1.41, T = 8 vs 4; 16 × 500 games) R added 8.7 ± 2.1 pp and M 4.1 ± 2.0 pp (main effects), and only the interactions R × T (−4.2) and M × K (+3.2) exceeded \|z\| = 2." | −1.5 |
| C4 Background | "We build on BasicMCTS, which, unlike TAG's advanced MCTS agent, has none of these features, so that each one's contribution can be measured." | −1 |
| C5 Results A | "TAG's DUCT agent (decoupled selection and information sets, but no card-value model or round-end rollouts) wins 21.0% of its seats in place of v2, the same share as v2. BasicMCTS, RHEA and OSLA win at most 12.8% of their seats (OSLA none: its one-step look-ahead never reaches the reveal)." | −1 |

### Should (precision, insight and documentation; about +4 lines in total, which fits in the 7–9 lines left)

- **S1 Notation.**
  - Use a different symbol for the number of players (for example m: "12 − m cards to each of m players", "m − 1 passes"), and head Table III's column "Games".
  - Typeset modification codes consistently in bold, so that S/Ŝ, P/p, I/i, B/b, M/m and D/d do not clash.
  - Change "z = difference/SE" to "|z| = |difference|/SE".
- **S2 Method precision.** "Statistics are keyed by card type" → "Statistics are keyed by card kind (type, Maki icons, use of Chopsticks)". Add "an iteration adds one node, at the first joint choice not yet in the tree; inside the tree the second card of a Chopsticks move is the greedy card." (+1 line)
- **S3 Introduction.** "give 729 or 4 096 joint moves per turn" → "give up to 729 or 4 096 joint moves at the first turn of a round".
- **S4 Setup.** Add after "is tested on the held-out deals": "at 100 vs 500 ms; with 500 games that test has 80% power only for rises of about 11 pp, whereas development suggested +3.4 ± 8.6 pp at 500 ms." (+1.5 lines)
- **S5 Table II.** "250 at 900 ms (development, s = 7)" → "250 at 900 ms (development and seed block 7)". Replace "from the authors" with a URL (or "in the submitted package"). Define "UCB-Tuned" or drop it. Say "game g uses seed s + g (seed block s)".
- **S6 Holm citation.** Add S. Holm, "A simple sequentially rejective multiple test procedure," *Scand. J. Statist.*, vol. 6, no. 2, pp. 65–70, 1979. That makes 16 references, which is acceptable because the brief says the count matters less than relevance; otherwise drop one marginal citation.
- **S7 Fig. 3.** Regenerate with the v3 held-out markers, and make the caption conditional in `text_fig_budget_caption()` (only mention v3 if `r2_v3_*` exist). Optionally note "(budgets not to scale)".
- **S8 Pipeline hygiene.** Remove the `r2_val_3p900_all.csv` preference in `F3P900` (and in `make_figures.py`), and correct `merge_parts.py`'s docstring. Compute the hard-coded numbers in `build_report.py` from the data, so the GenAI statement's "every number is computed" is literally true. Delete the dead `sig`/`marg` code the patch leaves in `text_budget`.
- **S9 README.** Align it with the paper (163 runs, ±5.7, 46.2%), or replace its results section with a pointer to the paper. Remove the "Every decision was made on the deals of seeds 1 and 2" claim, since the v5 decisions used seed 7.
- **S10 Result A.** Add "(the same-machine difference, 5.5 pp, z = 1.8, suggests the seed-7 estimates are somewhat optimistic)" (+1 line), or cut the 58.1% clause.

### Nice

- **N1 Behavioural insight** from the held-out logs, about +2 lines, which fits in the space: "At 100 ms on the held-out deals our agent scored 2.9 Pudding points per game against 0.5 for v2 and 2.5 for ISMCTS, but 3.0 Sashimi points against v2's 9.1: the lead-based evaluation trades risky sets for Pudding standing." (`r2_val_3p100.log`: our agent's Pudding 2.88, Sashimi 2.98; v2's Pudding 0.49, Sashimi 9.08.)
- **N2** Open each Results subsection with a one-line answer; spell out the codes in topic sentences.
- **N3** Prune the legacy JSONs and the off-by-default switches from the package (or move them to a `legacy/` note).
- **N4** Note that ties (8–13 per 500 games) make the Wilson interval approximate.

---

## 7. Plain answer

**Would this get ≥ 85%? Not as it stands.** A strict marker would land at about **79–80**. A generous one might give 84–86, because the experimental programme is far beyond the norm. With the must-fixes (Section 6, already tested to fit), I would expect **81–82**; with the should list as well, **83–85**.

**What stands between it and 90+:**

1. **Correct inference everywhere.** The RQ2 misreading ("did not replicate"), "every/any component", and non-significant or confounded results stated as findings (I's "small gain", "supports H3", "time up to 500 ms matters") are exactly what the "critical analysis" item punishes, and they show up in the abstract and conclusions.
2. **A clear, readable argument.** Each Results subsection needs a stated answer, fewer numbers in prose, codes spelled out where they matter, and no notation clashes. Today's "wall of intervals" caps "Clarity" in the low 70s.
3. **Insight, not only measurement.** A 90+ report explains *why*: why the agent beats ISMCTS at 100 ms (for example the card-type behaviour in N1) and why the edge shrinks with time (ISMCTS's seat share 26.4% → 35.9% on held-out deals while ours falls; pooled −5.5 ± 4.2 pp at 500 ms). This should be the headline answer to RQ2, ahead of the underpowered S + I trend.
4. **A more original, game-specific contribution, or a decisive confirmatory result.** The method is a well-executed combination of published techniques plus a hand-built card model. The only new game-specific idea (denial) failed. That caps "Originality" at about 75–80, and Approach carries 30% of the mark.
5. **Hygiene.** No placeholders or "pending", a README consistent with the paper, and complete GenAI prompts.

### Files produced (all under /tmp/claude-0/review5)

- `/tmp/claude-0/review5/review.md`: this review
- `/tmp/claude-0/review5/recompute.py` and `/tmp/claude-0/review5/recompute_out.txt`: independent recomputation of every number
- `/tmp/claude-0/review5/sim/run_scenarios.py` and `/tmp/claude-0/review5/sim/scenarios_out.txt`: branch-by-branch check of the current pending wording
- `/tmp/claude-0/review5/sim/build_scenarios.py`, `/tmp/claude-0/review5/sim/measure2.py`, `/tmp/claude-0/review5/sim/current_*.pdf`, `/tmp/claude-0/review5/sim/proposed_*.pdf`: real PDF builds per scenario with page-4 space measured
- `/tmp/claude-0/review5/proposed_build_report.diff`: the tested patch (must-fixes and cuts) against `report/build_report.py`
- `/tmp/claude-0/review5/sim/make_proposed.py`, `/tmp/claude-0/review5/sim/make_proposed2.py`, `/tmp/claude-0/review5/sim/make_proposed3.py`: scripts that generate the proposed build script
