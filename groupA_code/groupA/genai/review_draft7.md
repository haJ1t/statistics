# Independent marking: ECS7032P Assignment 1, Group A
Report draft 7 (`report/ECS7032P_Assignment1_Report_GroupA.pdf`, built 28 Oct 02:28) and the `groupA` agent package (v5, which plays as v4).
I did not open `report/reviews/`, so this reading is not anchored on earlier reviews.

## What I did

- Read the brief, rubric, assessment overview and writing tips (`pdftotext -layout`). Rendered the PDF at 80, 200 and 300 dpi (`/tmp/claude-0/review7/pages`, `/tmp/claude-0/review7/hi`).
- Read `build_report.py`, `make_figures.py`, all of `TAG/src/main/java/groupA/` (the search, the heuristic, the card model, the player, the parameters, the README, the JSON files), the arena harness (`Arena.java`, `merge_chunks.py`, `merge_parts.py`) and the TAG code the paper describes: Sushi Go! rules, `redeterminise`, the time measurement, TAG's MCTS, `LeaderHeuristic` and OSLA.
- Wrote my own recomputation script from scratch: `/tmp/claude-0/review7/recompute.py`, with output in `/tmp/claude-0/review7/recompute_out.txt`. It covers Wilson intervals, mean ± 1.96 SE, paired differences matched by game number, two-sample SE, inverse-variance pooling, Holm, outcome correlations and run counts. It also re-joins the chunked runs and compares them with the joined CSV files.
- Tested every branch of the pending-result wording on a sandbox copy (`/tmp/claude-0/review7/sandbox`) with synthetic results, in 10 scenarios:
  - `scenarios.py`; outputs from the current code are in `scenarios_out.txt`.
  - I built one complete PDF (`scenD.pdf`, synthetic numbers, not for use) to measure the real change in length.
  - I prototyped fixes and tested them on the same 10 scenarios: `build_report_fixes.diff`, `make_figures_fixes.diff`, output in `scenarios_final.txt`.
- Compiled `groupA` against this TAG build with `javac`. It compiles cleanly.
- Checked the literature claims against the papers' abstracts on the web. ludii.games, which hosts the Klang et al. PDF, is blocked in this session, so I could not read the Klang full text (details in §3.3).
- Nothing under `/home/user/statistics` was modified (`git status` is clean). I did not touch the queued runs.
  - The v3 run at 100 ms finished at 02:44 during the review. It is in `TAG/arena/v5/results` and not yet synced to `groupA_code`.
  - Result: v3 wins **46.5 % [42.2, 50.9]**. On the held-out deals S + I therefore gains **+6.1 ± 5.9 pp (z = 2.0)**, and **+8.2 ± 4.1 pp (z = 3.9)** pooled with seed block 7.
  - `r2_val_3p900_c0` started at 02:44.

---

## 0. Verdict in brief

**Mark: 79 % (strict). Plausible range 76–85 %, depending on how much the marker tolerates the density of the writing.**

**Strengths**
- The experimental work is exceptional for the module: v2, v3, TAG ISMCTS, TAG DUCT, BasicMCTS, RHEA and OSLA; leave-one-out and 2⁴ factorial ablations; held-out deals; paired designs; Holm corrections; a power statement.
- The numbers are faultless. All ~110 numbers I recomputed reproduce exactly from the raw CSV files and logs.
- The agent is well motivated, correctly implemented and correctly described.
- The code complies with the brief.

**What keeps it below 85**
1. The writing is over-compressed:
   - The Results section is 1,015 words against the 500–700 guidance.
   - Its five paragraphs carry 36 "±" intervals and 12 z-values.
   - Code letters are everywhere (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, v2–v4, E1–E3).
2. The Background is accurate but descriptive rather than critical.
3. A few analyses are inconsistent or confounded:
   - The lead-heuristic numbers come from two machines and two baselines.
   - "did not help" is used for a confounded, non-significant test.
   - The RQ1 claims ignore the held-out replication of S + I, which now exists.
   - The test of E2 can hardly detect the effect it was meant to find.
4. The conditional wording for the pending runs has eight bugs that would ship in some outcomes. One of them, the intermediate state, is the state the next rebuild will be in.
5. Fig. 3 overprints a data point.
6. Compliance loose ends: the author names are placeholders, and the revision prompts are only "available on request".

---

## 1. Marks per rubric item

**Weights.** These come from the Assessment Overview: Abstract 5 %, Problem Definition 15 %, Approach 30 %, Experimentation 30 %, Conclusions 10 %, General 10 %. Within an area I weight the items equally; this is my assumption, as the rubric gives no sub-weights.

### Abstract (5 %): area mark 74

**Brief motivation: 70 (band 70–100, low end).**
- "In Sushi Go!, players pick cards simultaneously from hidden hands that are passed on, which basic Monte Carlo tree search (MCTS) ignores."
- This states the problem but not why it matters. Delayed and conditional scoring, the third hard property, is missing.

**Summary of the work: 78.**
- The components are listed.
- "card-value rollouts, a lead-based evaluation" hides the paper's own main element: one model drives both the rollouts and the evaluation.

**Outline of findings: 74.**
- The numbers are given. The last sentence is cryptic: "All five ablated components raise the score margin significantly, the evaluation and decoupled tree also the win rate; 500 instead of 100 ms helped ISMCTS more than our agent, and four-player settings hurt with three (exploratory)."
- "Score margin" is undefined.
- There are four single removals plus S + I removed together, not five components.
- The agent's own significant decline at 500 ms is phrased as ISMCTS being "helped".
- The abstract is 125 words (the upper limit) and still contains "[pending]".

### Problem definition: Introduction + Background (15 %): area mark 74

**Motivation of the work: 74.** It cites TAG, MCTS, ISMCTS and Klang et al. Why the problem matters beyond Sushi Go! (drafting games, simultaneous hidden-information games) is not argued.

**Clarity of the problem definition: 80.** Properties (i)–(iv), each mapped to a failure of BasicMCTS, are crisp and accurate (checked against TAG).

**Clarity of the contributions: 72.**
- Bullets 3–4 are weak as contributions: "leave-one-out and factorial ablations, held-out validation and negative results that locate the agent's remaining limits" is methodology.
- "a diagnosis of why time beyond 500 ms helps little with four" rests on 73 positions from random-play games.
- The importance of the contributions is not stated.

**Critical analysis of the literature: 66 (band 50–69).**
- The summaries are accurate (§3.3) but mostly one line each.
- 7 Wonders, the closest related game, gets one sentence.
- How DUCT's exploitability and ISMCTS's handling of strategy fusion bear on Sushi Go!'s structure only appears later, in Results.
- The gap claim is strong and partly unverified: "We know of no study that separates the contributions of decoupled selection, information sets and domain knowledge … or measures how they change with the time budget and the number of players".

**Description of the game: 80.** Complete and correct: 108 cards, the scoring, 12 − n cards per hand, the Pudding tie-break, and −6 only with three or more players (checked in `SGParameters`, `SGCard`, `SGGameState`).

### Approach: Method + code (30 %): area mark 82

**Understanding and application: 84.**
- I verified the agent line by line (§3.1): decoupled UCT on an open-loop single-observer information-set tree with availability-count UCB, max-n, ε-greedy card-value rollouts, round-end rollouts and a bounded evaluation.
- Deductions are for code clarity:
  - The 1,753-line `SushiTreeNode` carries about 10 variants that are switched off (O, G, U, K, Q, E, N, MAST, multi-tree…).
  - A stale class comment: "Only _getAction() is changed, to add Modification D" (`SushiMCTSPlayer`).
  - The README note "Rename first: replace every groupA …".

**Originality: 76.**
- The paper's own element is the shared card-value model; W2 and the fast simulator are also its own.
- Otherwise it combines known techniques, and says so honestly: "The search techniques come from the literature; our own element is the card-value model".

**Difficulty and ambition: 86.** Advanced tree search with substantial development.

### Experimentation (30 %): area mark 81

**Adequacy and rigour: 84.**
- Seven comparison agents, leave-one-out plus factorial ablations, held-out deals, pairing, Holm and power all exceed the top band.
- Deductions:
  - The ablation exists only at 3 players and 100 ms.
  - Several RQ3 statements rest on 250-game runs.
  - The held-out test of E2 is placed at 500 ms, where development predicted only +3.4 pp. It has 80 % power only for about 12 pp.
  - The four-player settings were kept on non-significant gains (acknowledged).

**Quality of the documentation: 83.**
- Table II is thorough, the constants are given, and replication is feasible.
- Omitted:
  - v3's K (0.5) and T (4).
  - TAG ISMCTS's 300-move (effectively game-end) rollouts and MAST statistics.
  - Data are only "from the authors".

**Critical analysis of the results: 77.**
- The error analysis is excellent, and the hypotheses are hedged.
- Lapses (detailed in §3.4):
  - The two-baseline lead-heuristic arithmetic.
  - "did not help" for a test that is confounded with a change of K and not significant.
  - "the same share as v2" without an interval.
  - The selection effect (held-out lower than both seed-7 runs) is not interpreted.
  - The four-player decline is attributed only to the change of settings, although ISMCTS also gains seats with time at four players.
  - The opponent model Q is unreported.

### Conclusions (10 %): area mark 75

**Summary and conclusions: 74.** Accurate, but a numeric recap. The take-home messages are implicit, and the DUCT comparison is missing.

**Further work: 76.** Three motivated directions. The third (opponent models fitted to observed picks) is already implemented as switch Q and is not disclosed.

### General (10 %): area mark 75

**Clarity of ideas and expression: 68 (band 50–69, top).** The structure is logical and the language precise, but the flow suffers. Examples are in §6.

**Figures, tables and legends: 76.**
- Fig. 1 shows conceptual thinking, and Fig. 2 is informative (filled markers = Holm-significant).
- Problems:
  - In Fig. 3 the label "chance 33.3%" overprints v3's 900 ms marker and error bar.
  - Held-out v3 markers will be drawn at the same x as the final agent's.
  - Fig. 2 has sparse ticks, and the footnote of Table I is dense.

**Correct referencing: 80.** Consistent IEEE style, relevant sources and accurate summaries. A few minor omissions; 15 academic references plus Claude.

### Weighted total

0.05·74 + 0.15·74 + 0.30·82 + 0.30·81 + 0.10·75 + 0.10·75 = 3.70 + 11.10 + 24.60 + 24.30 + 7.50 + 7.50 = **78.7 ≈ 79 %**

---

## 2. Every number recomputed from raw files

**Method.**
- Win rate: the mean of `focusWin` (1, 1/k or 0), with a Wilson 95 % interval.
- Margin: the mean of `focusScore − bestOppScore` ± 1.96 SE (sample SD).
- Paired differences: matched by game number.
- Two-sample differences: SE = √(s₁²/n₁ + s₂²/n₂).
- Pooling: inverse-variance.
- Holm: two-sided normal p-values.
- Sources: G4 = `groupA_code/experiments_v4/results`, G5 = `groupA_code/experiments_v5/results`, G3 = `…_v3/results`.
- Chunk integrity:
  - I re-joined `r2_val_3p500_c0–c3` (offsets 0/126/252/378) and `r2_val_4p500_c0–c3` (0/128/256/384) myself. Both are identical to the joined CSV files, and the focus seat is g mod n in every game.
  - `v5_field_3p100_all` = `v5_field_3p100` + `r2_field_3p100b` shifted by 300, identical.

### Abstract, Table III, §IV-A

| Report | Recomputed | Source |
|---|---|---|
| 3p/100: 52.6 [48.2, 56.9], +0.73 ± 0.46 | 52.6 [48.2, 56.9], +0.73 ± 0.46 | G5 `r2_val_3p100.csv` |
| 3p/500: 47.2 [42.9, 51.6], −0.10 ± 0.40 | identical | G5 `r2_val_3p500.csv` (joined) |
| 3p/900: pending | file absent (run queued) | — |
| 4p/100: 44.4 [40.1, 48.8], −0.61 ± 0.43 | identical | G5 `r2_val_4p100.csv` |
| 4p/500: 36.5 [32.4, 40.8], −1.83 ± 0.45 | identical | G5 `r2_val_4p500.csv` (joined) |
| DUCT, ISMCTS: 55.0 [50.6, 59.3], +0.72 ± 0.40 | identical | G5 `r2_duct_3p100.csv` |
| ISMCTS × 2: 51.3 [46.9, 55.7], +1.41 ± 0.51 | identical | G5 `v5_field_3p100_all.csv` |
| BasicMCTS, RHEA: 84.2 [80.7, 87.1], +6.68 ± 0.60 | identical | G4 `B_mcts_rhea.csv` |
| OSLA, BasicMCTS: 87.2 [84.0, 89.8], +9.74 ± 0.75 | identical | G4 `B_osla_mcts.csv` |
| ranges "47.2–52.6%", "36.5–44.4%" | ✓ | as above |
| seed 7: 55.0 %; re-run 58.1 % | 55.0 [50.6, 59.3]; 58.1 [53.7, 62.3] | G4 `V_3p100`, G5 `v5_base3p100` |
| "2.4 ± 6.1 pp and 5.5 ± 6.1 pp more, neither significant" | −2.4 ± 6.1 (z 0.76); −5.5 ± 6.1 (z 1.77), two-sample | as above |
| Nigiri 17.6, 15.2, 12.9 | 17.62, 15.25, 12.94 | G5 `r2_val_3p100.log` (focus also from the CSV: 17.62) |
| Pudding 2.9, 0.5, 2.5 | 2.88, 0.49, 2.50 | same log |
| Sashimi 9.1 vs 3.0 | 9.08 vs 2.98 | same log |
| DUCT 21.0 % of seats; v2 21.0 % | 21.0 (Wilson [17.7, 24.8]); 21.0 | `r2_duct_3p100.log`, `r2_val_3p100.log` |
| "at most 12.8 % … OSLA none" | BasicMCTS 9.0, RHEA 6.8; BasicMCTS 12.8, OSLA 0.0 | G4 `val16.log` |
| 729 / 4 096 joint moves | 9³, 8⁴ | — |

### §III-E

| Report | Recomputed | Source |
|---|---|---|
| ±4.4 pp at 50 % for 500 games | 4.38 | — |
| ±5.7 pp for a paired difference | 5.4–5.9 in all 500-game pairs | recompute §8 |
| correlation "0.0–0.2" | 0.002–0.220 (f5_FL8/f5_W2FL8 0.22) | recompute §8 |
| 8 pp at 80 % power | 2.80 × 2.91 = 8.1 | — |
| "60 of our 164 runs" | 166 distinct ≥200-game CSVs − 2 parts of a joined run = 164; 60 four-player | G3–G5 |
| "nominally 45–70 s of CPU time per game" | 4p/500: 90.5 decisions × 0.5 s = 45 s; 3p/900: 77.3 × 0.9 s = 70 s | `V_4p500`, `V_3p900` `nDecisions` |
| "6–16 games in parallel" | partly traceable (harness notes mention 6, 8, 10, 16 threads) | v3/v4 READMEs |

### Fig. 2 and §IV-B

All are paired, 500 games; seed 7, base `V_3p100`.

| Report | Recomputed (win; margin) | Holm |
|---|---|---|
| −H −28.3 ± 5.8 (z 9.6) | −28.3 ± 5.8 (z 9.57); −5.45 ± 0.71 | win ✓, margin ✓ |
| −S−I −10.2 ± 5.7 (z 3.5) | −10.2 ± 5.7 (z 3.47); −1.75 ± 0.58 | ✓, ✓ |
| −R z = 2.0 | −6.2 ± 5.9 (z 2.04, p 0.042); −0.74 ± 0.61 | ✗ (needs p ≤ 0.0167), ✓ |
| −B z = 1.4 | −4.3 ± 5.8 (z 1.43); −0.98 ± 0.57 | ✗, ✓ |
| −I z = 1.3; "3.8 ± 5.7" | −3.8 ± 5.7 (z 1.30); −0.63 ± 0.55 (p 0.025) | ✗, ✓ (last step: 0.025 ≤ 0.05) |
| "on the score margin all five … win rate only H and S + I" | confirmed | — |
| lead heuristic −13.6 ± 5.9 (z 4.5) | −13.6 ± 5.9 (z 4.54); −1.45 ± 0.60 | ✓ (vs `v5_base3p100`) |
| "recovers 17.8 ± 5.6 pp" | +17.8 ± 5.6 (`r2_lead` − `A_noH`; **runs from two machines**) | see §3.4-1 |
| S alone +6.4 ± 5.7 (z 2.2) | +6.4 ± 5.7 (z 2.20) (`A_noI` − `A_noSI`) | not adjusted |
| "development: +1.3 ± 6.1" | +1.3 ± 6.1 (`d1_S` − `abl_T4`, two-sample) | — |
| X3 / N05 / V2D / W1 | −1.3, −1.8, −0.6, +0.7, each ±5.8 | all ✗ |
| R3V2 −23.7 ± 5.8 (z 8.0) | −23.7 ± 5.8 (z 8.00); −3.58 ± 0.65 | ✓ (with or without the lead heuristic in the family) |
| "45.0 % of greedy games against two X0 players" | X3 vs pool {X0}, 3 players: 45.00 % | G5 `greedy_duels.txt` |
| factorial: R +8.7, M +4.1 (each ±2.1); R×T −4.2, M×K +3.2; other interactions \|z\| < 2 | R +8.65 ± 2.10, M +4.10 ± 2.11, R×T −4.22, M×K +3.17; others \|z\| ≤ 1.05. Not reported: K +5.15, T +3.65 | G3/G4 `s2_*`, `d_*`, `s3_*`, `s4_*`, `abl_*` |
| caption: 55.0 %, 58.1 % | ✓ | — |

### Fig. 3 and §IV-C

| Report | Recomputed | Source |
|---|---|---|
| development points | v2 24.8 / 18.3 / 18.8; v3 50.1 / 40.7 / 32.8; final 55.7 / 49.7 / 47.6 (n 500/500/250) | G4 `s1_ref`, `f5_ref`, `f9_ref`, `abl_T4`, `f5_finalT4`, `f9_finalT4`, `d1_I`, `f5_I`, `f9_I` |
| v3 lost 17.3 ± 7.2; final 8.1 ± 7.5 | −17.3 ± 7.2; −8.1 ± 7.5 (two-sample) | same |
| S + I +5.6 ± 6.1 → +14.8 ± 8.4; by +9.2 ± 10.4 (z 1.7) | +5.6 ± 6.1; +14.8 ± 8.4; +9.2 ± 10.4 (z 1.73) | same |
| "11 000–12 000 iterations" at 500 ms | v3 11,170, v4 11,865 | G5 `probe_iterations.txt` |
| "best of 16 factorial cells (45.1–49.4 % on new seeds)" | v3 cell 50.1 % = max of the 16; `tb2_T4` 45.1, `tb1_T4` 47.5, `rep_T4` 49.4 | G3/G4 |
| held-out 500 vs 100: −5.4 ± 5.9 (z 1.8) | −5.4 ± 5.9 (z 1.78) | G5 `r2_val_3p100/500` |
| seed 7: −5.6 ± 5.9 at 500; +1.6 ± 8.7 at 900 | −5.6 ± 5.9; +1.6 ± 8.7 (250 common games) | G4 `V_3p*` |
| pooled −5.5 ± 4.2 (z 2.6) | −5.5 ± 4.2 (z 2.56) | — |
| ISMCTS 26.4 % / 35.9 %; v2 21.0 % / 16.9 % | 26.4 / 35.90 (seat-weighted over the chunk logs); 21.0 / 16.90 | G5 logs |

### §IV-D

| Report | Recomputed | Source |
|---|---|---|
| X0 +4.3 ± 4.3 at 100 ms | +4.3 ± 4.3 (z 1.94) | G4 `q4_I+q4_Io` → `q4_X0+q4_X0c` |
| X0, W2, K = 1 +5.8 ± 7.4 at 500 ms | +5.8 ± 7.4 | `val_q5` → `q5_W2+q5_W2r` |
| F + L +1.0 ± 7.9; −6.6 ± 6.0 at 100 ms | +1.0 ± 7.9; −6.6 ± 6.0 | `q5_W2r`→`q5_FL8`; `q4_X0c`→`q4_X0FL8` |
| 3p cost 7.7 ± 5.4 (z 2.8) | −7.7 ± 5.4 (z 2.80) | `f5_FL8`→`f5_W2FL8` |
| difference 13.5 ± 9.1 (z 2.9) | 13.5 ± 9.1 (z 2.89) | — |
| depth 1: 17.4 ± 8.2 | −17.4 ± 8.2 | `q5_FL8d1` |
| 1 000 ms, K 1.37: −4.8 ± 8.1 | −4.8 ± 8.1 | `q5_FL8t1000K14` |
| 4p 500 vs 100: −5.4 ± 5.9 (s7), −7.9 ± 5.7 (held-out), pooled −6.7 ± 4.1 (z 3.2) | identical (z 3.19) | `V_4p*`, `r2_val_4p*` |
| 73 positions, 704 root children, 53 % visited once; 2 and 4 changes; re-run 4 / other seed 7 | 73 × (1 − 0.973) = 2.0; × 0.055 = 4.0; × 0.055 = 4.0; × 0.096 = 7.0 | G5 `treestats4.txt`, `agree4.txt` |

Table I footnote constants match the JSON `args` and the `CardValueModel` constants. The budgets and guards (900/925, 430/450) match the competition JSON files.

### Mismatches or untraceable numbers

- **No numeric mismatch.**
- **Presentational inconsistency.** "costs 13.6 ± 5.9 pp … but recovers 17.8 ± 5.6 pp of H's 28.3 pp". The three numbers use two baselines: 13.6 against 58.1 % (4-core machine), 28.3 against 55.0 % (18-core machine). 17.8 is computed across machines. 13.6 + 17.8 = 31.4 ≠ 28.3, which a careful marker will notice.
- **"704 joint-move children at the root"** is a mean over the 73 positions.
- **"73 random positions"** are every 7th position of 6 games played by `RandomPlayer`s (`Agree4`, `TreeStats4`).
- **"6–16 games in parallel"** is only partly traceable.
- **15.25 is printed as "15.2"** (round half to even). This is acceptable.

---

## 3. Technical description, literature and inferences

### 3.1 Paper against code (all verified)

- **Eq. (1)** matches `CardValueModel.estimatedScore`: w_t = (roundCounter + 1)/3. u_p comes from `potential()`: the k-th unused Wasabi needs k Nigiri, worth 2ν; Chopsticks are worth 1 with k = 2; completion = min(1, (r−k+1)/T), or 0 if r < k.
- **Eqs. (2)–(3)** match `SushiGoHeuristic.valuesOf` (σ 15, w 0.7). The marginal value mv is computed with r − 1 picks on both sides.
- **B, R, P, M:**
  - B: ε-greedy rollouts with ε = 0.2 for every player; the second Chopsticks card is ε-greedy in rollouts and greedy in the tree.
  - R: `finishRollout` ends at the round change.
  - P: `distinctCards` gives one branch per distinct card.
  - M: per-player values in `backUpAll` and `informationSetIteration`.
- **S, I and Eq. (4)** match `selectInformationSet`:
  - untried cards first, then Q + K·√(ln availability / visits);
  - a new `state.copy(me)` sample in every iteration;
  - an open-loop tree whose children are keyed by the joint card keys;
  - one node added per iteration;
  - the final move is the most visited root card.
- **Four players:** the `fourPlayer` JSON block uses X0, w = 1, terminal tanh(gap/2) with 0.01·Pudding as tie-break, σ·(ρ/2)^0.5, opponents valuing their lead over the others' mean, F + L8 and K = 1, as described.
- **Ablation and candidate configs** differ from the final agent only in the stated switches (diffed: `a4_no*`, `b100_leadK75`, `X3`, `N05`, `V2D`, `W1`, `R3V2`). −H uses TAG's default own score/50.
- **OSLA "never reaches the reveal"** is correct. `redeterminise` clears the other players' choices, so OSLA's own pick never completes a turn, and every action evaluates the same.
- **Budgets are thread CPU time** for every agent (`ElapsedCpuTimer` in our agent, BasicMCTS and TAG's `SingleTreeNode`). The CSV column `msPerDecision` is wall-clock (≈1.8 s per decision at 900 ms on the laptop), which does not affect fairness.
- **Small discrepancies** (nice-to-fix):
  - The stale header comment of `SushiMCTSPlayer`.
  - The README says Q is "tested, off", but its only game run stopped at 50/250 games (`experiments_v4/results/q5_FL8Q.err`).
  - `LeaderHeuristic` also multiplies terminal values by 1.5, which "H differs from it only by…" omits.

### 3.2 The brief's rules for the code

- **Based on BasicMCTS:** ✓ copies of `BasicMCTSPlayer`, `BasicTreeNode` and `BasicMCTSParams`. Nothing from `players.mcts` is imported in `groupA` (grep).
- **Self-contained package:** ✓ it compiles against this TAG build. The framework is unchanged since the import commit (only `groupA` was touched afterwards).
- **README and JSON:** ✓ README with constructor and parameters; competition JSON at `json/competition/SushiMCTS_1000ms.json` (and `_500ms`).
- **Risk: the group letter.** The README begins "Rename first: replace every `groupA` … with your real group letter". If the group really is A, delete the note. If not, the package, folder and JSON class names must be renamed before submission.

### 3.3 Literature claims

| Ref | Claim in the paper | Check |
|---|---|---|
| [1] Gaina et al. 2020 | generic description of TAG | ✓; bibliographic data ✓ |
| [2] Browne et al. 2012 | 4 MCTS steps, UCB1, progressive bias, UCB-Tuned | ✓ |
| [3] Cowling et al. 2012 | ISMCTS avoids strategy fusion; SO vs MO (one tree per player) | ✓ |
| [4] Klang et al. 2021 | "introduced the game to TAG"; "focused on simultaneous action selection and complete information and concluded that better heuristics should improve the agents" | ✓ against the abstract (implemented Sushi Go! in TAG; mixed results; better heuristics should help). **Unverified:** "the current version shows an agent only the cards it has seen" implies the earlier version showed all hands. Their "complete information" may be the game-theoretic sense, which is not perfect information. Full text unreachable. Reword (S9). |
| [5] Kocsis & Szepesvári | UCT | ✓ |
| [6] Silver & Tesauro | a stronger rollout policy ≠ better estimates; balanced rollouts | ✓ ("unbiased" slightly simplifies "minimise the error of the mean outcome") |
| [7] Long et al. | success related to how quickly hidden information is revealed (disambiguation) | ✓ |
| [8] Frank & Basin | strategy fusion, non-locality | ✓ (non-locality is undefined in the paper) |
| [9] Goodman et al., CoG 2022, pp. 292–299 | MultiTree = one tree per player | ✓; bibliographic data ✓. Could not check whether it already compares IS-MCTS variants across budgets or player counts in TAG games, which matters for the gap claim. Soften (S5). |
| [10] Lanctot et al., CGW 2013, CCIS 408 | RM and OOS best; DUCT most exploitable | ✓ (abstract: "regret matching and OOS perform best … all variants produce less exploitable strategies than UCT") |
| [11] Tak et al., CIG 2014 | nine games; depends on game and opponent; DUCT best overall | ✓ (pages not verified) |
| [12] Sturtevant 2008 | UCT in multi-player games computes a mixed-strategy equilibrium; as good or better | ✓ |
| [13] Robilliard et al. 2014 | "many results known from classic games held there too" | ✓. Unused but relevant: they note that card values depend on the game stage and the other players' choices. |
| [14] Wilson, [15] Holm, [16] Claude | — | ✓ |

**No misreporting found among the claims I could verify.**

### 3.4 Are the conclusions supported? Overclaims, selection, multiplicity, power, confounds

1. **Lead heuristic (must).** "costs 13.6 ± 5.9 pp (z = 4.5) but recovers 17.8 ± 5.6 pp of H's 28.3 pp (paired)" mixes machines and baselines (§2). Present one comparison on one baseline.
2. **RQ1 ignores the held-out replication (must).** The held-out v3 run is the replication of the S + I ablation on clean deals. It is now in: +6.1 ± 5.9 pp (z 2.0); pooled with seed block 7, +8.2 ± 4.1 pp (z 3.9). The paper uses it only for E2. RQ1 (abstract, §IV-B, §V) still quotes seed block 7 alone, and no branch reacts if the replication fails (scenario H, §4).
3. **E2 is nearly untestable as designed.** Development predicts +3.4 pp from 100 to 500 ms (z 0.78). The held-out test has 80 % power only for about 12 pp. The pending wording says this when the result is non-significant. It should be said up front in §III-E, or v3 should be run at 900 ms on the held-out deals, where development predicted +9.2 pp. That run takes about 3 h on the 4-core machine, similar to `r2_val_3p900`.
4. **"more time for our agent alone did not help (1 000 ms, K = 1.37: −4.8 ± 8.1 pp)":** K changed as well, and the interval reaches +3.3. Write "brought no detectable gain".
5. **"a diagnosis of why time beyond 500 ms helps little with four (a wide, shallow joint-move tree)":** 2 and 4 changed decisions out of 73 positions from random-play games show that time rarely changes the decision. They do not establish why. Write "evidence that …".
6. **Selection effect.** The final agent's held-out 100 ms result (52.6 %) is below both seed-7 runs (55.0 %, 58.1 %). The paper says only "neither significant". The new v3 run makes the point sharper:
   - v3 scores *higher* on the held-out deals (46.5 %) than on seed block 7 (44.8 %), so the held-out deals are not simply harder.
   - The S + I gain shrinks from +10.2 pp (seed 7) to +6.1 pp (held-out).
   - Both have the sign expected from selecting the configuration on deals that overlap seed block 7 (regression to the mean). One clause should say so (S3).
7. **"wins 21.0% of its seats in place of v2, the same share as v2":** give the interval [17.7, 24.8]. Also state the inference the comparison supports: TAG's DUCT, which has S and I but no card-value model, is no stronger than v2. This is a missed argument for the card-value model.
8. **Seat-share reasoning.** "The extra time helped TAG's ISMCTS more than our agent" is a relative statement about shares that sum to 100 %, which is fine. But the same pattern holds at four players, and the paper omits it: ISMCTS rose from 24.1 % to 32.1 % held-out and from 23.9 % to 29.1 % on seed 7 between 100 and 500 ms. The four-player decline (−6.7 pp) is therefore not only "mixes the budget with W2, F + L and K = 1"; it repeats the three-player finding.
9. **"All five ablated components" / "removing any of five components":** the five ablations are −H, −R, −B, −I and −S−I. S alone is only a derived, unadjusted comparison (z 2.2; development +1.3). "S alone … beats v3 by +6.4 ± 5.7 pp" should say "unadjusted".
10. **"I adds a further, non-significant 3.8 ± 5.7 pp, in line with Long et al.'s analysis"** is an absence of evidence. Write "consistent with", and note that the power is about 8 pp.
11. **Unreported opponent model.** Q (an opponent model fitted to observed picks) is in the submitted code but absent from the paper, and it is then proposed as future work.

---

## 4. The conditional wording for the pending results

**Tested branches.** Every branch of `_abstract_end`, `text_final_agent`, `text_budget`, `text_conclusion_1` and `text_fig_budget_caption`, in 10 scenarios:
- current;
- v3 at 100 ms only (the state of the next rebuild);
- v3 at 100 ms plus 900 ms;
- all runs with E2 non-significant, significant positive and significant negative;
- our agent better at 900 ms while ISMCTS falls;
- v3 beating v4 on the held-out deals;
- a crash at 900 ms;
- v3 at 100 and 500 ms only.

**Unreachable branches.** With the existing data, the else branch of `_abstract_end` ("did not change our win rate significantly") and the "For RQ2, …" path without `POOL500` cannot trigger. Both are grammatical.

**Bugs found:**

| # | Function / outcome | Problem |
|---|---|---|
| B1 | `text_budget`, `more` false (e.g. our agent +3.4 pp at 900 ms, ISMCTS down to 24 %) | "…v2 21.0%, 16.9% and 20.0%. Alternatively, our parameters, tuned at 100 ms, may suit longer budgets less." The dangling "Alternatively" now contradicts the 900 ms result. |
| B2 | `text_conclusion_1` | ", while TAG's ISMCTS gained seats;" is hard-coded and false in the outcome above. |
| B3 | RQ1 in the abstract, §IV-B and §V | Never uses the held-out S + I gain. If v3 beats v4 on held-out deals, §IV-C reports "S and I changed v3's win rate by −3.4 ± 6.2 pp" while the abstract still says the decoupled tree raises the win rate. |
| B4 | `text_budget`, v3 at 100 ms only (**the next rebuild**) | "On the held-out deals S and I gain +6.1 … (v3: 46.5%). On the held-out deals the final agent's own win rate changed…": present tense, and two consecutive sentences with the same opener. |
| B5 | `_abstract_end`, E2 significant | The abstract replaces the agent's own significant decline (pooled z 2.6, the main RQ2 result) with the E2 outcome. |
| B6 | `text_budget`, E2 non-significant | "(development predicted +3.4 pp …)": the 100→500 ms development value appears nowhere else (the text reports 100→900 = +9.2). |
| B7 | Fig. 3 (`make_figures`) | v3 and final held-out markers are both drawn at x + 0.13 and overlap at 100 ms. "chance 33.3%" already overprints v3's 900 ms marker and will hit the 900 ms held-out bar if that result is near 40 %. |
| B8 | `seat_wins_chunks` | Reads any chunk logs present. If two 900 ms chunks are synced before the join, the text quotes 900 ms seat shares while Table III says "pending" (reproduced). |
| B9 (should) | Large 900 ms effect | E.g. −14.6 pp (z 4.8): it never reaches the abstract, and 900 ms is never pooled with seed block 7 (+1.6 ± 8.7), unlike 500 ms. |
| B10 (nice) | `text_conclusion_1`, non-significant branch | "(z = 0.6)" follows the two gains, so it is unclear that it refers to their difference. |

**Fixes for B1–B8** are in `/tmp/claude-0/review7/build_report_fixes.diff` (115 lines) and `/tmp/claude-0/review7/make_figures_fixes.diff`. All 10 scenarios produce correct text with them (`scenarios_final.txt`). The fixes:
- `SI_POOL`: S + I pooled from seed block 7 and the held-out run, with `SI_SIG` driving the RQ1 wording in the abstract and the conclusion, and a held-out clause in §IV-B.
- `_ism_gained()`: replaces the hard-coded ISMCTS clause.
- "Alternatively" is printed only when `more` is true.
- Past tense and " There" in the intermediate state.
- "(development, 100 to 500 ms: …)".
- The E2 branch is removed from the abstract.
- `seat_wins_chunks` returns nothing until the joined CSV exists.
- Separate held-out offsets (+0.09 for v3, +0.17 for the final agent) and the chance label moved to the left. The rendered result is clean (`/tmp/claude-0/review7/patched_fig_budget.png`).

**Length.** The full PDF built with all pending results (scenario D) ends the main text about 4 lines above the bottom of page 4, against about 10 now. The pending results therefore cost about 6 lines, in line with your estimate. The abstract stays at 125 words.

---

## 5. Compliance

| Rule | Status |
|---|---|
| Template (Word IEEE) | ✓. Margins as in the module template (54 pt top on body pages, 44.65 pt sides, 2 columns). Liberation Serif is embedded instead of Times New Roman (metric-compatible). |
| Main text ≤ 4 pages; GenAI statement + references may use page 5; total ≤ 5 | ✓ now and with the pending results (scenario D) |
| Section word guidance (abstract 75–125, intro 350–450, background 650–850, method 700–800, results 500–700, conclusions 200–300) | 125 / 438 / 674 / 885 / **1,015** / 279. Results is far over the guidance, and Method is over it. |
| References 6–15 | 15 academic + Claude; consistent IEEE style |
| ≥ 500 games per experiment unless justified | ✓ for all headline claims. 250-game runs (41 of 164) are justified by CPU time in Table II; several RQ3 statements rest on them (acknowledged). |
| At least some experiments at 500 ms | ✓ |
| GenAI acknowledged; AI-written sections cite the system and give the prompts | Citation ✓ (footnote on page 1 + [16]). Main prompt ✓ verbatim + translation. **The revision prompts (the marker reports) are "available on request"**, which falls short of "include the prompt(s)". |
| Authors | **Placeholders**: "[Group member 2]", "[Group member 3]", "Student no. [to be completed]" ×3 |
| Code: group package, README with constructor and parameters, competition JSON, no `players.mcts` | ✓ (see the "Rename first" note, §3.2) |

**Risk, not a rule breach.** The GenAI statement says Claude wrote the code and the paper from the prompt "It should be a report that gets 100", in a loop with an AI marker. This is honest and allowed. A lecturer may still probe the students' understanding; the individual reflections should state factually what each member did.

---

## 6. Presentation

- **Density.** Three sentences a marker must parse twice:
  - "From 500 ms it also uses W2 – w = 1; finished games valued by tanh((bi − maxj≠i bj)/2) with Pudding as tie-break; σ multiplied by (ρ/2)^0.5 for ρ rounds left; opponents in the backup valuing their lead over the others' mean – and F, …, with K = 1."
  - "In four-player development no change gave a significant gain at its own budget (X0: +4.3 ± 4.3 pp at 100 ms; X0, W2 and K = 1: +5.8 ± 7.4 pp at 500 ms, F + L a further +1.0 ± 7.9 pp, but −6.6 ± 6.0 pp at 100 ms); we kept each change where its estimate was positive."
  - The abstract's last sentence (quoted in §1).
- **Internal codes.** X3, V2D, W1, N05 and R3V2 appear only in Fig. 2 labels or in passing. "Score margin" is undefined in the abstract.
- **Candidate list.** "a stronger greedy policy (45.0% …)" reads as a separate candidate; it describes X3. Two of the four candidates (σ decay, w = 1) are unnamed in the text.
- **Notation clashes:**
  - w (leader weight) vs w_t (Pudding round weight).
  - k (cards still needed) vs the k-way tie in §III-E.
  - n (players) vs n (visits, §II-B) vs n (games, Table III).
  - π used as a variable.
- **Undefined terms.** Non-locality, UCB-Tuned, regret matching, EXP3 and progressive bias are named but not defined. This is acceptable for the audience, but noted.
- **Table order.** Table II is cited in the Introduction before Table I is cited.
- **Figures:**
  - Fig. 3: the label collision (B7).
  - Fig. 2: only two ticks per axis (−20/0 and −5/0); the panels have no titles.
  - Fig. 1 is good, but its caption (6 lines) repeats §III-B/C.
- **Typography.** The italic "Sushi Go!" is consistent. The Turkish prompt keeps its original typos, which is correct for a verbatim quote.

---

## 7. Prioritised changes

Length estimates use what I measured on the PDF: about 8–9 words per body line and about 13 per caption line.
Current spare space is about 10 lines on page 4. The pending results take about 6, leaving about 4.

### Must

**M1. Authors.** Fill in `AUTHORS` (names and student numbers). 0 lines.

**M2. Apply the branch fixes (§4: B1–B8) before the next rebuild.** The next rebuild will be in the "v3 at 100 ms only" state (B4). With the real result, the §IV-C sentence becomes "On the held-out deals S and I gained +6.1 ± 5.9 pp (z = 2.0) at 100 ms (v3: 46.5%). There the final agent's own win rate…". Length: +3 lines; the RQ1 held-out clauses in §IV-B and §V account for most of it.

**M3. Lead heuristic (§IV-B), one baseline.** +1 line.
- Replace: "TAG's lead heuristic (own score minus the best opponent's, K rescaled to 7.5) costs 13.6 ± 5.9 pp (z = 4.5) but recovers 17.8 ± 5.6 pp of H's 28.3 pp (paired); as round-end leaves …"
- With: "Replacing H by TAG's lead heuristic (own score minus the best opponent's, K rescaled to 7.5) costs 13.6 ± 5.9 pp (z = 4.5; base: the re-run, 58.1%), so about half of the 28.3 pp comes from valuing the lead on a suitable scale; at round-end leaves …" (rest unchanged).
- In code: drop `LEADH` from `text_ablation`.

**M4. GenAI prompts.**
- Replace "Its written reports, one per draft (available on request), were the prompts for the revisions;"
- With: "Its written reports, one per draft, were the prompts for the revisions; they are reproduced in full in GENAI_PROMPTS.md in our code package;"
- Add that file to `groupA/`.
- Length: +0.5 line, on page 5 (not counted).

**M5. Fig. 3.** Apply `make_figures_fixes.diff`. 0 lines.

**M6. Fill the pending numbers** once both runs are joined. The run order is: the 3p/900 chunks first, then the v3/500 chunks. Rebuild and re-check the layout.

### Should

**S1. Abstract (123 words; −2 words, ±0 lines).**

> Sushi Go! combines simultaneous moves, hidden hands that are passed on and delayed set scoring, which basic Monte Carlo tree search (MCTS) ignores. We extended BasicMCTS of the Tabletop Games framework (TAG) with a card-value model for all players' rollouts and a bounded lead-based evaluation, round-end rollouts, max-n backups and a decoupled information-set tree. On held-out deals against TAG's information-set MCTS (ISMCTS) and our previous agent it wins 52.6%, 47.2% and X% of three-player games (100, 500, 900 ms) and 44.4% and 36.5% of four-player games (100, 500 ms; chance 33.3% and 25%). In ablations, the evaluation and decoupled tree matter most. At 500 rather than 100 ms our win rate fell 5.5 percentage points while ISMCTS gained; four-player settings hurt three-player play.

If adopted, `_abstract_end` and the `SI_SIG` switch should drive "the evaluation and decoupled tree" in the same way.

**S2. Conclusion, paragraph 1 (165 words instead of 189; −2 lines).**

> For RQ1, Sushi Go! knowledge in the evaluation matters most: replacing H by TAG's lead heuristic costs 13.6 pp of win rate, and the decoupled information-set tree adds [10.2 / pooled value] pp over v3's sequential, determinised tree; every ablation lowers the score margin, but only these two also lower the win rate after a Holm correction. For RQ2, more time did not help our agent: its win rate fell by 5.5 ± 4.2 pp from 100 to 500 ms (two deal sets pooled) while TAG's ISMCTS gained, and the development trend that the decoupled tree gains more with time is not significant. For RQ3, settings tuned for four players cost 7.7 ± 5.4 pp with three (exploratory), and with four players a wide, shallow joint-move tree means that time beyond 500 ms rarely changes the decision; the four-player 500 ms agent won 6.7 ± 4.1 pp less than the 100 ms one, a comparison that mixes the budget with W2, F + L and K = 1.

The pending-result clauses (900 ms, E2) still need to be appended as `text_conclusion_1` does now.

**S3. Selection effect (§IV-A; +2 lines).**
- Replace "2.4 ± 6.1 pp and 5.5 ± 6.1 pp more, neither significant."
- With: "2.4 ± 6.1 pp and 5.5 ± 6.1 pp more; neither difference is significant, but both have the sign that a small selection bias from the overlap would produce."

**S4. Move the RQ2/RQ3 numbers into a compact Table IV (one column, about 16 lines)** and rewrite §IV-C/D as interpretation. This removes about 20 lines of number-laden text, so net −4 lines. It is the largest single improvement to Clarity and to Critical analysis. Rows (all values verified in §2):
- v3 100→900 (dev.†): −17.3 ± 7.2
- final 100→900 (dev.†): −8.1 ± 7.5
- S + I at 100/900 (dev.†): +5.6 ± 6.1 / +14.8 ± 8.4
- final 100→500: held-out −5.4 ± 5.9, seed 7 −5.6 ± 5.9, pooled −5.5 ± 4.2
- final 100→900: seed 7 +1.6 ± 8.7 (+ held-out)
- 4p X0 at 100 (dev.†): +4.3 ± 4.3
- 4p X0, W2, K 1 at 500 (dev.†, 250 games): +5.8 ± 7.4
- 4p +F + L at 500 / 100: +1.0 ± 7.9 / −6.6 ± 6.0
- 3p X0, W2, K 1 with F + L at 500: −7.7 ± 5.4
- 4p depth-1 tree / 1 000 ms for us: −17.4 ± 8.2 / −4.8 ± 8.1
- 4p 500 vs 100 agent: seed 7 −5.4 ± 5.9, held-out −7.9 ± 5.7, pooled −6.7 ± 4.1

**S5. Background critique (+3 lines).**
- After the 7 Wonders sentence add: "They also note that a card's value depends on the game stage and on the other players' choices [13], which our card-value model estimates explicitly."
- Replace "We know of no study that separates …" with "For Sushi Go! and similar drafting games we know of no study that separates …".

**S6. Hedges (+1 line in total):**
- "but more time for our agent alone did not help (1 000 ms, K = 1.37: −4.8 ± 8.1 pp)" becomes "but 1 000 ms for our agent alone (with K = 1.37) brought no detectable gain (−4.8 ± 8.1 pp)".
- In the third contribution bullet, "a diagnosis of why time beyond 500 ms helps little with four (a wide, shallow joint-move tree)" becomes "evidence that with four players time beyond 500 ms rarely changes the decision of a wide, shallow joint-move tree".
- "On 73 random positions the 500 ms tree has 704 joint-move children" becomes "On 73 positions from random-play games the 500 ms tree has on average 704 joint-move children".
- "S alone … beats v3 by +6.4 ± 5.7 pp (z = 2.2;" becomes "… (z = 2.2, unadjusted;".

**S7. Four players: the same ISMCTS pattern as with three (+1.5 lines).**
- In §IV-D/§V, replace "which mixes the budget with W2, F + L and K = 1;"
- With: "which mixes the budget with W2, F + L and K = 1, while TAG's ISMCTS rose from 24.1% to 32.1% of its seats, as with three players;".

**S8. Candidate list (§IV-B; +1 line).**
- Replace "among them card constants tuned for three players (X3), a stronger greedy policy (45.0% of greedy games against two X0 players), and denial (rollout players also value taking the next player's favourite card);"
- With: ": card constants tuned for three players (X3; as a greedy policy it wins 45.0% of games against two X0 players), denial (rollout players also value taking the next player's favourite card), a win-gap value with σ decay, and w = 1;".

**S9. Klang et al. (+1.5 lines).**
- Replace "Klang et al. [4] introduced the game to TAG; the current version shows an agent only the cards it has seen."
- With: "Klang et al. [4] implemented the game in TAG to study its simultaneous action selection; in the TAG version we use, an agent's copy of the state re-deals every card it has not seen."

**S10. Disclose Q (§V; +1 line).**
- Replace "and opponent models fitted to observed picks could replace the single greedy policy that now models all players."
- With: "and opponent models fitted to observed picks (switch Q implements one, not yet evaluated in full runs) could replace the single greedy policy that now models all players."
- In the package README, change Q's status to "implemented; full-run test incomplete".

**S11. DUCT inference (§IV-A; +1 line).**
- Replace "wins 21.0% of its seats in place of v2, the same share as v2."
- With: "wins 21.0% [17.7, 24.8] of its seats in place of v2, the same share as v2, so decoupling and information sets without the card-value model do not make a strong agent."

**S12. Code package.**
- Delete the "Rename first" note (or rename the package if the group is not A).
- Fix the `SushiMCTSPlayer` header comment.
- 0 lines in the report.

**S13 (optional experiment).** Run v3 at 900 ms on the held-out deals (about 3 h), so E2 is tested where development predicts the largest effect. The text cost is +1 line, or 0 if it replaces the 500 ms test sentence.

### Nice

- **N1.** Fig. 2: ticks at −30/−20/−10/0 and −6/−4/−2/0, and panel titles ("win rate", "score margin").
- **N2.** Introduction: "(250 in some long runs, Table II)" becomes "(250 in some long runs)". This fixes the table order. −0.3 line.
- **N3.** Rename w_t to ω_t in Eq. (1), and write a "j-way" tie in §III-E. 0 lines.
- **N4.** Split the W2/F sentence of §III-D (+0.5 line): "From 500 ms it also uses W2 and F. W2 sets w = 1, values finished games by tanh((bi − maxj≠i bj)/2) with Pudding as tie-break, multiplies σ by (ρ/2)^0.5 when ρ rounds are left and lets opponents in the backup value their lead over the others' mean. F re-implements TAG's rules on integer arrays, checked move by move against the forward model; its speed is spent on L = 8 rollouts per leaf, with K = 1."
- **N5.** Pool 900 ms with seed block 7 when it arrives, and mention it in the abstract if |z| ≥ 2 (B9).
- **N6.** Mention the factorial's K and T main effects (+5.2, +3.7) or drop them from the parenthesis.
- **N7.** Export with Times New Roman installed.
- **N8.** Trim the 34 KB README to running instructions and a short summary.

### Cuts to pay for the additions

- **C1. Fig. 1 caption (−3 lines):** "The sequential tree of v3 (left) and a turn node of the final agent (right), where every player picks by UCB on its own card statistics (S, P) and the hidden cards are re-sampled in every iteration (I)."
- **C2. Card points, §IV-A (−1 line):** "At 100 ms it scores more from Nigiri (17.6 points per game; v2 15.2, ISMCTS 12.9) and Pudding, while v2 chases Sashimi."
- **C3. Conclusion paragraph 1 (S2):** −2 lines.
- **C4. Last contribution bullet (−1 line):** "leave-one-out and factorial ablations with validation on held-out deals."
- **C5. Factorial interactions (−1.5 lines):** drop "and only the interactions R × T (−4.2) and M × K (+3.2) exceeded |z| = 2".
- **C6. Table II (−1 line):** tighten the "Games" and "Hardware, records" cells.

### Line budget

| Item | Lines |
|---|---|
| Spare after the pending results | 4 |
| Musts | −4 |
| Shoulds S3, S5–S11 | about −12 |
| Cuts C1–C6 | about +9.5 |
| **Net without S4** | about −2.5: drop S7 or S11, or shorten S5 |
| **Net with S4 (Table IV)** | about +1.5 to +2 (S4 recovers about 4 lines) |

---

## 8. Plain answer

**Would it get ≥ 85 % as it stands?** Probably not. I mark it 79 %. A lenient marker impressed by the experiments could reach the mid-80s, but I would not count on it.

**What stands between it and 90 %+:**
1. **Readability.** The Results must read as an argument, with the numbers in a table (S4). Each sentence should carry at most one interval. The code letters need defining once or replacing with words.
2. **A critical Background.** Explain why DUCT's exploitability and ISMCTS's handling of strategy fusion matter or do not matter in Sushi Go!. Link 7 Wonders to the card-value model. State the gap precisely.
3. **Analyses free of inconsistencies.** One baseline for the lead heuristic. RQ1 claims that use the held-out replication (+6.1 pp; pooled +8.2 ± 4.1). Hedged statements where tests are confounded or not significant. A sentence on selection.
4. **An RQ2 test that can answer E2.** Either declare it underpowered up front or test at 900 ms.
5. **Pending wording that is correct in every outcome.** Apply the tested diff.
6. **Polished figures and complete compliance.** Fig. 3, the authors, the revision prompts, and the README note.

With M1–M6 and S1–S12 done well, 84–87 % is realistic. 90 %+ needs the writing itself to be excellent, which means fewer ideas discussed in more depth within the four pages. As the brief warns, "Better marks are often obtained by teams that try fewer approaches".
