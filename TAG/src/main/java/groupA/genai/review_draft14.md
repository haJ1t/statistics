# Review 13 – ECS7032P Assignment 1, Group A: "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!"

Independent marking of the PDF of 28 Oct 2026 (5 pages), its build scripts, the `groupA` package and the experiment
records. I did not read `groupA/genai/review_draft*.md` or `report/reviews/`. Nothing under `/home/user/statistics`
was changed; all work is in `/tmp/claude-0/review13` (recomputation script `recompute.py`, its output
`recompute_out.txt`, page renders `pages/`, `hi/`).

## 0. Verdict in five lines

* **Numbers: all correct.** I recomputed every number in the text, the three tables and both data figures from the
  per-game CSV files and arena logs with my own script: no mismatch, nothing untraceable. Chunk joins, pairing by game
  number and the run counts also check out.
* **Technical description: matches the Java code**, and every literature claim I could check against the sources'
  abstracts or listings is reported correctly. No misreporting found.
* **Experiments: excellent for a student report.** The paired, held-out, Holm-corrected design with stated power is
  the report's strongest point.
* **Writing: the main weakness.** The Results prose runs to 1,115 words (guidance 500–700), holds 125 decimal
  statistics together with its captions, and runs on letter codes. Two conclusions overreach (four-player time
  scaling, RQ3 "did not grow"), and one hypothesis is left standing against the held-out evidence.
* **Mark: 81% (likely range 78–86).** With the must/should text changes below, about 85–87%. Above 90% would need the
  Results and Conclusions to explain why things happen, not just list numbers. That is mostly writing, but the
  100 ms-only ablation and the weak v2 opponent in every main field cap the Experimentation mark.

---

## 1. Marks per rubric item

Weights (Assessment Overview): Abstract 5%, Problem definition 15%, Approach 30%, Experimentation 30%, Conclusions 10%,
General 10%. Within an area, items are weighted equally.

### 1) Abstract – 78 (band 70–100)

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Brief motivation | 70–100 | 82 | "Card-drafting games such as Sushi Go! combine hidden hands, simultaneous moves and delayed scoring, which basic Monte Carlo tree search (MCTS) handles poorly." It is clear, correct and short. |
| Brief summary of the work | 70–100 | 80 | Every component is named. The evaluation design is only hinted at ("On held-out deals"). |
| Outline of the findings | 70–100 (low) | 72 | Five bare win rates with no opponent context; the reader cannot tell how far ahead of ISMCTS the agent is. "(100, 500 ms; chance 33.3%, 25%)" puts both chance levels after the four-player numbers, which is ambiguous. "Every ablated component raises the score margin significantly" has no scope (five components, 3 players, 100 ms, one deal set). The last slot goes to the least important finding, "four-player settings hurt three-player play (exploratory)". The most interesting result (ISMCTS catches up with time) is squeezed into seven words. |

### 2) Problem definition – 79 (band 70–100)

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Motivation (Introduction) | 70–100 | 78 | There is a clean chain from MCTS's assumptions [2], [3] to the game's four properties, and Klang et al. [4] is placed correctly. Why the answer matters beyond this game (drafting games, simultaneous moves with hidden information) is left implicit. |
| Clarity of problem definition | 70–100 | 84 | "Four properties make the game hard for a search agent. (i) … (iv)" plus RQ1–RQ3 is a model problem statement. |
| Clarity of contributions | 70–100 (low) | 74 | Bullet 1 is a hand-made heuristic, which is engineering rather than a research contribution. Bullet 3 is "exploratory". Half of bullet 2 is a negative result ("is not detectably better than our previous agent"). Bullet 2's "beats a determinised sequential tree" has no scope: it holds at 100 ms (pooled +8.2 ± 4.1 pp), but at 500 ms it is +5.3 ± 5.5 pp. The report never argues why these findings advance knowledge. |
| Critical analysis of the literature | 70–100 (low) | 76 | It makes real critical points: DUCT need not converge and was the most exploitable [9]; the best variant depends on the game [10]; why PIMC succeeds [7]; simulation balancing [6]. It also states the gap explicitly ("we know of no study that separates …"). But it is compact (about 700 words including the rules). It does not cover ISMCTS in other card games or opponent modelling, and "non-locality" is used without definition. |
| Description of Sushi Go! | 70–100 | 84 | The scoring is complete, hand sizes and the joint-action count are given, and "hidden information is concentrated at the start of each round" is a good AI-relevant observation. Card counts per type are missing, which is minor. |

### 3) Approach – 83 (band 70–100)

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Understanding and application (Methods + code) | 70–100 | 86 | Decoupled UCT (DUCT) over a single-observer open-loop ISMCTS tree with availability counts, max-n backups, round-end rollouts and a shared card-value model is well beyond the lectures. It is correctly implemented and correctly described (Section 3a). The package compiles on its own against TAG, and both competition JSON files ran a full game in my smoke test (3p/1000 ms, 4p/500 ms). Deductions: K = 0.5 and σ were tuned at 100 ms only and never revisited for longer budgets, which the paper itself suspects ("our parameters were also tuned at 100 ms"). The Method never says why UCB was chosen over EXP3 or regret matching; that only appears in Future Work. |
| Originality | 70–100 (low) | 76 | The paper says so itself: "The search techniques come from the literature; our own element is the card-value model shared by the evaluation and every player's simulation." TAG already contains a decoupled information-set MCTS (SushiGoDUCT), and the paper's own comparison shows that the gain comes from the domain model, not the search. The fast simulator (F + L) is engineering. |
| Difficulty and ambition | 70–100 | 88 | High: two search variants from the literature, a max-n backup, a card model, separate 3- and 4-player parameters, an integer-array simulator, and wall-clock guards. |
| (Code clarity, part of this area) | – | – | Comments are thorough (78 "MODIFICATION" tags) and the code does no forbidden `players.mcts` imports. But `SushiTreeNode.java` has 1,753 lines and about 20 switches (A, B, P, O, M, R, S, I, G, U, F, L, K, V, E, N, Q, …). Most are off in the final agent, which makes the code hard to read. The class comment justifies this ("so that the ablation study runs exactly the same code"), but a marker will still find it heavy. |

### 4) Experimentation – 84 (band 70–100)

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Adequacy and rigour | 70–100 | 86 | The final agent is compared with v2, v3, TAG ISMCTS, TAG DUCT, BasicMCTS, RHEA, OSLA and a 2 × ISMCTS field. There is a leave-one-out ablation, a 2⁴ factorial and a held-out deal set disjoint from all development deals. Runs are paired by deal, every headline run has 500 games, there are three budgets including 500/900 ms, and both 3 and 4 players are covered. Gaps: (a) the ablation was run only with 3 players at 100 ms, so RQ2 ("how their value changes with the time per decision") is answered for S + I only; (b) the weak v2 sits in every main field (it wins 10–21% of its seats); (c) the only ISMCTS-only field is at 100 ms; (d) 47 of 167 runs have 250 or 300 games, a cost justified by CPU time, but the four-player 500 ms settings were adopted on non-significant 250-game gains (which the paper admits). |
| Quality of documentation | 70–100 | 88 | Table II covers seeds, seating, deals, budgets and opponent configurations, and the package holds every per-game file. I could reproduce every number from it, so the work is replicable externally. |
| Critical analysis of the results | 70–100 (low) | 78 | The error analysis is exemplary: Wilson intervals, paired SEs, inverse-variance pooling, Holm, power statements, and the best-of-16 selection bias. Against that: (i) the development hypothesis for v3's decline is kept although the held-out runs show v3 and the final agent losing about the same at 500 ms (−4.6 vs −5.4 pp); (ii) "With four players we found no gain from more time" overgeneralises and leaves out the one run that contradicts it (Section 3c); (iii) "did not grow" (RQ3) overclaims a null (Section 3c); (iv) the explanation of the time-scaling result is untested, although data in the repository would support it (iterations per decision, Section 6); (v) there is little evidence about mechanisms: one sentence on card types is the only look at how the agent plays differently. |

### 5) Conclusions – 78 (band 70–100)

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Summary and conclusions | 70–100 (low) | 74 | It is accurate but restates 12 statistics without synthesis. It never says where the agent stands overall (well ahead of ISMCTS at 100 ms, level with it per seat at 4p/500 ms). It contains the RQ3 overclaim. "the held-out test did not settle E2" is fair. |
| Further work | 70–100 | 82 | Each item has a reason: learn the evaluation, because hand-tuned changes no longer move the agent; try regret matching or EXP3, because DUCT is exploitable [9], [10]; fit opponent models to observed picks. |

### 6) General – 78 (band 70–100)

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Clarity of ideas and expression | 50–69 / 70 boundary | 70 | The structure is logical and the language precise. But Results runs to 1,115 words against 500–700 guidance, and 125 decimal statistics (over 200 numbers in all) sit in its prose and captions. There are many internal codes (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, E1–E3, v2–v4, "seed block 7/100 000", "3p/100"), and several letters clash with the mathematical notation (Section 5). Some sentences are very hard to parse, for example "No four-player setting gained significantly in development (X0 +4.3 ± 4.3 pp, z = 1.94, at 100 ms; X0, W2 and K = 1 +5.8 ± 7.4 pp and F + L +1.0 ± 7.9 pp at 500 ms, but F + L −6.6 ± 6.0 pp at 100 ms); we kept each where its estimate was positive." |
| Figures, tables, legends | 70–100 | 82 | Fig. 1 is a real conceptual figure. Fig. 2 (paired effects, with filled markers for Holm-significant effects) and Fig. 3 (development lines plus held-out markers) are informative, and their captions are complete. The weaknesses are sparse axis ticks in Fig. 2, an undefined label ("+ denial in rollouts"), a cryptic footnote to Table I, and a prose-heavy Table II. |
| Correct referencing | 70–100 | 82 | IEEE style is used consistently, all 15 references are relevant, and the details are correct where I could check them (Section 3b). Minor gaps: nothing on ISMCTS applications to other card games, no RHEA citation, and [15] gives no model version. |

### Weighted total

0.05 × 78 + 0.15 × 79 + 0.30 × 83 + 0.30 × 84 + 0.10 × 78 + 0.10 × 78 = 3.90 + 11.85 + 24.90 + 25.20 + 7.80 + 7.80 =
**81.45 ≈ 81%**. There is no tournament bonus in this figure. Area marks are the means of their items: abstract
(82 + 80 + 72)/3 = 78; problem definition (78 + 84 + 74 + 76 + 84)/5 = 79; approach (86 + 76 + 88)/3 = 83;
experimentation (86 + 88 + 78)/3 = 84; conclusions (74 + 82)/2 = 78; general (70 + 82 + 82)/3 = 78.

---

## 2. Recomputation of every number

**Method.** `recompute.py` was written from scratch and run with `python3 -I`. It reads only CSV files and logs.
* Outcome per game: `focusWin` (1/k for a k-way shared first place; 1.0–2.0% of games in the held-out runs).
* Margin per game: `focusScore − bestOppScore`.
* Win rates: Wilson 95% interval with z = 1.96. Means: ±1.96 SE using the sample SD.
* Paired comparisons: matched by game number. I checked that focus seats are equal game by game and that scores
  correlate (Section 2.1).
* Independent runs: two-sample SE √(s₁²/n₁ + s₂²/n₂). Pooling across deal sets: inverse variance.
* Holm step-down at α = 0.05 within each Fig. 2 group, separately for each measure.
* Seat shares: from the "win x% over n seats" lines of the logs, seat-weighted over the chunk logs for chunked runs.

**Result: every reported value is reproduced exactly at the printed precision. There are no mismatches and no
untraceable numbers.**

### 2.1 Data-integrity checks

| Check | Result |
|---|---|
| Own merge of `r2_val_3p500`, `r2_val_3p900`, `r2_v3_3p500` chunks (offsets 0/126/252/378) and `r2_val_4p500` chunks (0/128/256/384) vs the joined CSVs | identical; 500 games each, numbered 0–499; focus seat = g mod n in every game |
| `v5_field_3p100_all` = `v5_field_3p100` (300) + `r2_field_3p100b` (200, offset 300) | identical |
| Claimed paired runs share deals | focus seats equal game by game in all 28 paired comparisons; focus-score correlations are +0.17 to +0.40 for paired runs, against −0.03 to +0.04 for the † runs on TAG's own deals and +0.006 for seed block 7 vs held-out, so the pairing claims hold |
| "167 runs", "60 four-player" | 169 distinct CSV contents with ≥ 200 games, minus the two parts of the joined field run = 167; four-player: 60 ✓. Of the 167, 120 have 500 games, 41 have 250 and 6 have 300 |
| Package `results/` = `experiments_v3–v5/results` | identical except the `.err` files, plus one log differing only by a JVM banner line |

### 2.2 Abstract, Introduction, Section III-E (statistics paragraph), Table II

| Report text | Reported | Recomputed | Source |
|---|---|---|---|
| Held-out 3p wins | 52.6 / 47.2 / 44.8% | 52.6 / 47.2 / 44.8 | `r2_val_3p100/500/900.csv` |
| Held-out 4p wins | 44.4 / 36.5% | 44.4 / 36.5 | `r2_val_4p100/500.csv` |
| "Every ablated component raises the score margin significantly" | – | Holm on margins: all 5 significant (z = 14.97, 5.89, 3.36, 2.40, 2.24) | `A_no*.csv` vs `V_3p100.csv` |
| "the evaluation and the decoupled tree also raise the win rate" | – | Holm on wins: only H (z 9.57) and S + I (z 3.47); R (z 2.04, p 0.042) fails | same |
| Joint moves | 729 / 4096 | 9³ / 8⁴ (hand = 10 − n + 2) | `SGForwardModel` |
| 95% interval at 50%, n = 500 | ±4.4 pp | ±4.38 | formula |
| Paired difference between two runs | "about ±5.7 pp" | ±5.4 to ±5.9 over the 500-game pairs | all paired comparisons |
| Win correlation in paired runs | "at most 0.22" | max 0.220 (`f5_FL8` vs `f5_W2FL8`), min −0.011 | 28 pairs |
| 80% power | "about 8 pp" | 8.1 | 2.8 × 5.7/1.96 |
| Four-player runs / all runs | 60 / 167 | 60 / 167 | all results folders |
| CPU time per game | 45–70 s | 45.2 s (90.5 decisions × 0.5 s), 69.5 s (77.3 × 0.9 s) | `nDecisions` in `q5_FL8`, `V_4p500`, `V_3p900`, `r2_val_3p900` |
| Opponent settings in Table II (v2 K 1.41; ISMCTS = SushiGo_3+P_HiBudget, MultiTree, UCB-Tuned, K 1, 300-move random rollouts, score heuristic; DUCT K 1 default, information sets, LeaderHeuristic, 30-move rollouts; BasicMCTS K 1.41, 10 moves; RHEA horizon 10, population 10; OSLA score heuristic) | – | all match the JSON files | `agents/b100/*.json`, TAG `MCTSParams` defaults |
| Competition files | 900 ms (guard 925) / 430 ms (guard 450) | match | `json/competition/*.json` |
| Table I constants X0 / default | (15, 0.7, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3) / (15, 0.7, 0.5, 4, 1, 0, 0, 0, 1, 2, 0) | match JSON `args` and the `CardValueModel` constants | |

### 2.3 Table III and Section IV-A

| Row / claim | Reported | Recomputed | Source |
|---|---|---|---|
| 3p/100 v2, ISMCTS | 52.6 [48.2, 56.9], +0.73 ± 0.46 | identical | `r2_val_3p100.csv` |
| 3p/500 | 47.2 [42.9, 51.6], −0.10 ± 0.40 | identical | `r2_val_3p500.csv` |
| 3p/900 | 44.8 [40.5, 49.2], −0.35 ± 0.39 | identical | `r2_val_3p900.csv` |
| 4p/100 | 44.4 [40.1, 48.8], −0.61 ± 0.43 | identical | `r2_val_4p100.csv` |
| 4p/500 | 36.5 [32.4, 40.8], −1.83 ± 0.45 | identical | `r2_val_4p500.csv` |
| 3p/100 DUCT, ISMCTS | 55.0 [50.6, 59.3], +0.72 ± 0.40 | identical | `r2_duct_3p100.csv` |
| 3p/100 ISMCTS × 2 | 51.3 [46.9, 55.7], +1.41 ± 0.51 | identical | `v5_field_3p100_all.csv` |
| 3p/100 BasicMCTS, RHEA | 84.2 [80.7, 87.1], +6.68 ± 0.60 | identical | `B_mcts_rhea.csv` |
| 3p/100 OSLA, BasicMCTS | 87.2 [84.0, 89.8], +9.74 ± 0.75 | identical | `B_osla_mcts.csv` |
| "500 games each" | – | all 500 | |
| "significantly above chance" | – | lower Wilson bounds 48.2 / 42.9 / 40.5 / 46.9 / 50.6 / 80.7 / 84.0 > 33.3; 40.1 / 32.4 > 25 ✓ | |
| Seed block 7, 100 ms; re-run | 55.0%; 58.1% | 55.0; 58.1 | `V_3p100`, `v5_base3p100` |
| "within chance of the held-out result (z ≤ 1.8)" | z ≤ 1.8 | two-sample z = 0.76 and 1.77 | vs `r2_val_3p100` |
| Nigiri points per game: ours, v2, ISMCTS | 17.6 / 15.3 / 12.9 | 17.62 / 15.25 / 12.94 | `r2_val_3p100.log` |
| Pudding ours vs v2 / ISMCTS; Sashimi | "more … Pudding, v2 more from Sashimi" | Pudding 2.88 vs 0.49 / 2.50 (the log counts **bonuses only**: per-card totals 44.16 vs actual mean score 43.43); Sashimi 2.98 vs 9.08 / 4.42 ✓ | same |
| DUCT seats | 21.0% [17.7, 24.8] | 21.0 over 500 seats, [17.7, 24.8] | `r2_duct_3p100.log` |
| DUCT minus v2 seats | 0.0 ± 5.0 pp | +0.0 ± 5.0 (two-sample; v2 21.0% in `r2_val_3p100.log`) | logs |
| BasicMCTS, RHEA, OSLA seats | ≤ 12.8% | BasicMCTS 9.0 / 12.8, RHEA 6.8, OSLA 0.0 | `val16.log` |

### 2.4 Fig. 2 and Section IV-B (paired, seed block 7, 100 ms)

| Effect | Δ win (z) | Δ margin (z) | Holm, wins / margin | Figure fill |
|---|---|---|---|---|
| − H (own score / 50) | −28.3 ± 5.8 (9.57) | −5.45 ± 0.71 (14.97) | yes / yes | filled / filled ✓ |
| − S − I (= v3) | −10.2 ± 5.7 (3.47) | −1.75 ± 0.58 (5.89) | yes / yes | ✓ |
| − R | −6.2 ± 5.9 (2.04) | −0.74 ± 0.61 (2.40) | no / yes | open / filled ✓ |
| − B | −4.3 ± 5.8 (1.43) | −0.98 ± 0.57 (3.36) | no / yes | ✓ |
| − I | −3.8 ± 5.7 (1.30) | −0.63 ± 0.55 (2.24) | no / yes | ✓ |
| H → lead heuristic | −13.6 ± 5.9 (4.54) | −1.45 ± 0.60 (4.73) | yes / yes | ✓ |
| + X3 | −1.3 ± 5.8 (0.44) | −0.57 ± 0.60 (1.86) | no / no | ✓ |
| + denial N 0.5 | −1.8 ± 5.8 (0.61) | −0.32 ± 0.59 (1.06) | no / no | ✓ |
| + V2D | −0.6 ± 5.8 (0.20) | −0.60 ± 0.56 (2.10; p 0.036 fails Holm) | no / no | ✓ |
| + W1 | +0.7 ± 5.8 (0.24) | −0.09 ± 0.58 (0.30) | no / no | ✓ |
| + R3V2 | −23.7 ± 5.8 (8.00) | −3.58 ± 0.65 (10.86) | yes / yes | ✓ |

Text values: 28.3 ± 5.8 ✓; lead heuristic 44.5% ✓, 13.6 ± 5.9 (z 4.5) ✓; own score / 50 at 26.7% ✓ (lead vs own
score / 50 on the same deals: +17.8 ± 5.6); S + I 10.2 ± 5.7 (z 3.5) ✓; held-out S + I +6.1 ± 5.9 ✓ (`r2_v3_3p100` →
`r2_val_3p100`); pooled +8.2 ± 4.1, z 3.90 ✓; S alone +6.4 ± 5.7 (z 2.20) ✓ (`A_noSI` → `A_noI`); I alone 3.8 ± 5.7 ✓;
four later changes −1.8 to +0.7, each ±5.8 ✓; X3 at 45.0% against two X0 ✓ (`greedy_duels.txt`); R3V2 23.7 ± 5.8
(z 8.0) ✓.

**2⁴ factorial** (v3 study, seed 1, TAG's own deals, so cells are independent; my two-sample computation): R +8.65 ±
2.09 pp (z 8.11), M +4.10 ± 2.09 (z 3.85), R × T −4.22 (z −3.96), M × K +3.17 (z 2.97). The report's "8.7 ± 2.1", "4.1 ±
2.1" and "z = 4.0, 3.0" are correct. The v3 `RESULTS.md` prints ±2.0 and z 2.83 from a paired formula; the report rightly
uses the independent version. Best cell = v3 (`abl_T4`) at 50.1%; v3 on new seeds 49.4 / 47.5 / 45.1%, so "45.1–49.4%" ✓.

### 2.5 Fig. 3 and Section IV-C

| Claim | Reported | Recomputed | Source |
|---|---|---|---|
| Development win rates (v2 / v3 / final at 100, 500, 900 ms) | Fig. 3 lines | 24.8, 18.3, 18.8 / 50.1, 40.7, 32.8 / 55.7, 49.7, 47.6 (900 ms: 250 games) | `s1_ref f5_ref f9_ref`, `abl_T4 f5_finalT4 f9_finalT4`, `d1_I f5_I f9_I` |
| Held-out points | open markers | final 52.6, 47.2, 44.8; v3 46.5, 41.9 | `r2_val_*`, `r2_v3_*` |
| S + I gain in development | +5.6, +9.0, +14.8 | +5.6 ± 6.1, +9.0 ± 6.1, +14.8 ± 8.4 | two-sample |
| 100 → 900 ms rise | +9.2 ± 10.4, z 1.7 | +9.2 ± 10.4, z 1.73 | difference of the two |
| v3 and final losses, 100 → 900 ms | 17.3 / 8.1 pp | −17.3 / −8.1 | |
| S alone, development | +1.3 ± 6.1 / +6.2 ± 6.1 | identical (z 0.41 / 2.01). An unused replicate `d1_S2` gives the same +1.3 ± 5.3 | `d1_S`, `f5_S` |
| Held-out S + I at 100 / 500 ms | +6.1 ± 5.9 / +5.3 ± 5.5 | identical (z 2.04 / 1.88) | paired |
| Difference of differences | −0.8 ± 8.1 (z 0.2), power 12 pp | −0.8 ± 8.1 (z 0.19), power 11.6 | per deal, 4 runs |
| Held-out 500 / 900 vs 100 ms | −5.4 ± 5.9 (1.8) / −7.8 ± 5.8 (2.6) | identical (1.78 / 2.62) | paired |
| Pooled with seed block 7 | −5.5 ± 4.2 (2.6) / −4.9 ± 4.8 (1.97) | identical (2.56 / 1.97) | inverse variance |
| Seed block 7 alone at 900 ms | +1.6 ± 8.7 | +1.6 ± 8.7 (250 common deals) | `V_3p100`/`V_3p900` |
| ISMCTS seat shares, held-out | 26.4, 35.9, 36.3% | 26.4, 35.90, 36.29 (seat-weighted chunk logs) | logs |
| ISMCTS 500 vs 100 ms | +9.5 ± 5.7 | +9.5 ± 5.7 (z 3.26) | two-sample, 500 seats each |

### 2.6 Section IV-D and Conclusions

| Claim | Reported | Recomputed | Source |
|---|---|---|---|
| S + I over v3 with 4 players | +6.4 ± 5.2 / +3.0 ± 8.4 | identical | `q4_final` vs `q4_I + q4_Io`; `q5_final` vs `val_q5` |
| X0 at 4p/100 | +4.3 ± 4.3, z 1.94 | identical | `q4_I + Io` vs `q4_X0 + X0c` |
| X0, W2, K = 1 at 4p/500 | +5.8 ± 7.4 | identical | `val_q5` vs `q5_W2 + W2r` |
| F + L at 500 / 100 ms | +1.0 ± 7.9 / −6.6 ± 6.0 | identical | `q5_W2r→q5_FL8`; `q4_X0c→q4_X0FL8` |
| Cost with 3 players | 7.7 ± 5.4 (z 2.8) | identical (2.80) | `f5_FL8→f5_W2FL8` |
| Interaction | 13.5 ± 9.1 (z 2.9) | identical (2.89) | |
| Depth 1 / 1000 ms alone (K 1.37) | −17.4 ± 8.2 / −4.8 ± 8.1, power 12 | identical, power 11.6; K = 1.37 in `finalI_X0K14W2FL8_t1000.json` | seed 2, 250 games |
| 500 ms agent vs 100 ms agent, pooled | −6.7 ± 4.1 (z 3.2) | −6.7 ± 4.1 (3.19): seed 7 −5.4 ± 5.9, held-out −7.9 ± 5.7 | `V_4p*`, `r2_val_4p*` |
| ISMCTS seats with 4 players | 24.1 → 32.1% | 24.1 → 32.09 (748 seats each) | logs |
| Seed block 7, 4p | 46.8 / 41.4% | identical | `V_4p100/500` |
| Tree statistics | 73 positions, 704 children, 53% visited once | identical | `treestats4.txt` |
| Changed card | 2 / 4 positions; reruns 4 / 7 | 1.97 / 4.01; 4.01 / 7.01 of 73 | `agree4.txt` |
| Conclusions | 28.3; 13.6; 10.2; 8.2 ± 4.1; 5.5 ± 4.2; 4.9 ± 4.8; 9.5; −0.8 ± 8.1; 6.4 / 3.0; 7.7 ± 5.4 | all identical to the above | |

**Traceability note.** `build_report.py` computes most numbers, but about a dozen are typed in by hand: 28.3 ± 5.8,
10.2 ± 5.7 (z = 3.5), 3.8 ± 5.7, 8.7 ± 2.1, 4.1 ± 2.1, z = 4.0 and 3.0, 16 × 500, 45.0%, 45.1–49.4%, 45–70 s, ±4.4, ±5.7
and "8 pp". All of them are correct today. The GenAI statement's claim "every number comes from the result files of our
runs" is true in substance, but these values would not update if a file changed.

---

## 3. Technical, literature and inference checks

### 3a. Description vs Java code (all consistent)

| Report statement | Code |
|---|---|
| Copy of BasicMCTS, no advanced-MCTS code | No `players.mcts` import in any of the 7 files; the class structure is BasicTreeNode's; no package JSON references `players.mcts` |
| Eq. (1): banked + α·unfinished sets (Wasabi 2ν, Chopsticks 1, weight min(1, (r − k + 1)/T)) + Maki standing + π·ω_t·Pudding, ω_t = t/3 | `CardValueModel.estimatedScore` l.179, `potential` l.194–212, `completion` l.95, `puddingWeight` l.145; at terminal states the banked score only (no double-counted Pudding) |
| Eqs. (2)–(3), σ = 15, w = 0.7 | `SushiGoHeuristic.valuesOf` l.126–141 |
| B: ε = 0.2 greedy for every player; second Chopsticks card the same way, greedy in the tree | `rolloutCard` l.1503, `playOut` l.1474–1486, `greedyAction` in the tree l.488 |
| R: rollout ends when the round is scored | `finishRollout` l.1626–1635 |
| S: per-player UCB at a turn node, untried cards first, child by joint action | `selectDecoupled` l.338, `expandJoint` |
| I: new sample each iteration, open-loop tree, one node per iteration, keys by card kind, availability count, play most-visited card | `informationSetIteration` l.454–525 (`expanded`), `selectInformationSet` l.1157 (availability += 1), l.1189 (UCB with ln(availability)), `bestAction` l.1667 |
| D replaced by I | `numberOfDeterminisations` returns `ensembleTrees` (= 1) when I is on |
| 4p: X0, W2 (w = 1, tanh((b_i − max b_j)/2) with Pudding tie-break, σ·(ρ/2)^0.5, opponents' lead over the mean), F + L8, K = 1 | competition JSON `fourPlayer` block; `terminalValues` l.1445, `roundScale` l.1425, opponent values l.1396 (`opponentRivalWeight` = 1) |

Small precision points, none of which is an error. Without I, the exploration term is ln(N + 1), not ln N. "Pudding" in
the card-points sentence means bonuses only (TAG's per-type counters omit the −6). The 4p/100 ms file keeps K = 0.5,
which the paper does not say.

### 3b. Literature claims vs sources

I could not reach the full texts in this session: ludii.games (Klang et al. PDF) and lamsade.dauphine.fr (CCIS 504
front matter) failed to resolve, and the proxy's log shows denials for IEEE Xplore, arXiv export, dblp, Semantic
Scholar and ResearchGate. I could therefore check only abstracts and
bibliographic listings found through web search. Within that limit:

* [4] Klang et al. The abstract says Sushi Go! "combines simultaneous action selection and complete information", that
  the authors implemented it in TAG, and that "further developing heuristics … should prove to increase their
  performance". The report's two statements match. CoG 2021, pp. 1–4 matches the DBLP listing. ✓
* [9] Lanctot et al. The abstract says "regret matching and OOS perform best and … all variants produce less exploitable
  strategies than UCT". The report matches; CCIS 408, pp. 28–43 is confirmed. That EXP3 was among the variants compared
  is plausible but I could not confirm it from the full text.
* [10] Tak et al. Nine games; DUCT, sequential UCT, Exp3 and regret matching; "depends strongly on the game and the
  opponent … Decoupled UCT performs best". The report matches. pp. 232–239 per the Maastricht repository. ✓
* [11] Sturtevant. The abstract says UCT in multi-player games "is computing a mixed-strategy equilibrium, as opposed to
  maxn" and "performs as well or better". The report matches. Its wording "UCT with max-n backups … computes a
  mixed-strategy equilibrium" is defensible, but the paper contrasts UCT with max^n search; see the nice-to-have list.
* [12] Robilliard et al. The abstract says a hand-made evaluation is hard because card values depend on the game phase
  and other players' choices, and that many known results carry over. The report matches; pp. 64–77 is confirmed. ✓
* [6] Silver & Tesauro. The paper optimises balance rather than strength. The report's gloss is fair. ✓
* [7] Long et al. The paper relates PIMC success to properties including how fast information is disambiguated. The
  report matches; pp. 134–140. ✓
* [8] Goodman et al., CoG 2022, pp. 292–299. ✓
* [3], [5], [13], [14] are standard; their content and the cited details agree with my knowledge, but I did not re-read
  the full texts.

**No misreporting found.**

### 3c. Do the conclusions follow from the statistics next to them?

1. **Overclaim and omission (four players).** "With four players we found no gain from more time." The repository holds
   a paired run in which our 100 ms file at 100 ms, playing 500 ms opponents, lost **9.0 ± 7.7 pp (z = 2.28)** against
   the 500 ms file (`q5_FL8` → `q5_X0b100`, seed 2, 250 games). The README reports it, the paper does not. The
   comparison is confounded, since the files differ in W2/F/L/K. Even so, the general claim is contradicted by the
   study's own data. The evidence supports only "no gain beyond 500 ms" (1000 ms: −4.8 ± 8.1). The Conclusions and the
   contribution bullet already use that scope; Section IV-D does not.
2. **Overclaim of a null (RQ3).** "For RQ3, the gain from S and I did not grow with time with four players (6.4 and 3.0
   pp …)". The difference is −3.4 ± 9.9 pp on independent samples with 250 games at 500 ms. That shows no evidence
   either way; it does not show "did not grow".
3. **Hypothesis left standing against the held-out data.** "We hypothesise that v3's decline comes from its sequential
   tree … S alone … consistent with this explanation." On the held-out deals, v3 lost 4.6 pp from 100 to 500 ms
   (46.5 → 41.9) and the final agent lost 5.4 pp. The decline is common to both agents, which is what the −0.8 ± 8.1
   difference of differences says. The text should say so plainly and demote the hypothesis.
4. **RQ2 is answered for one component only.** RQ2 asks how the adaptations' value changes with time, but only S + I
   (and S alone, in development) were measured at more than one budget. The limitations should name this.
5. **The competitive standing in the competition setting is never stated.** On a per-seat basis our agent leads TAG's
   ISMCTS by 26.2 pp at 3p/100, 11.3 at 3p/500, 8.5 at 3p/900, 20.3 at 4p/100, but only **4.4 pp at 4p/500**
   (36.5% vs 32.1%; approximate z ≈ 1.6, optimistic because the two outcomes are negatively correlated within a game).
   The brief's Experiments section names 4p/500 as the competition setting. Readers deserve this sentence.
6. **Other inferences are properly hedged.** These include the 900 ms pooled change (z = 1.97, with the heterogeneous
   seed-7 estimate disclosed), the DUCT equivalence, the lead-heuristic arm, the K mis-scaling caveat for −H, the
   best-of-16 selection bias, the post hoc label on E3, and "exploratory" for all four-player tests.
7. **Multiplicity.** Holm is applied only within Fig. 2. The abstract's time claims rest on unadjusted tests, but the
   key one (ISMCTS +9.5 pp, z = 3.26) would survive any reasonable correction.
8. **Confounds already disclosed.** RQ2 measures relative scaling, since every agent gets the extra time.
   "X0, W2 and K = 1" is a bundle. F + L is in the three-player arms only.

---

## 4. Compliance

| Rule | Status |
|---|---|
| IEEE Word template (A4, margins, 2 columns, headings) | ✓ built from the module template. The PDF embeds Liberation Serif rather than Times New Roman: same metrics and layout, slightly different glyphs. Exporting from Word on a machine with Times New Roman removes this (nice). |
| Main text ≤ 4 pages; GenAI + references on page 5; total ≤ 5 | ✓ the main text ends about 3.5–3.7 lines above the bottom of page 4; page 5 holds the GenAI statement and references |
| Section word guidance (abstract 75–125; intro 350–450; background 650–850; method 700–800; results 500–700; conclusions 200–300) | 117 ✓; 453 (≈); 704 ✓; 904 (over by 13%, including the setup paragraph); **1,115 (over by 59%)**; 293 ✓ |
| References 6–15, one style | 15, IEEE ✓ |
| ≥ 500 games per experiment unless justified | every headline and validation run has 500 games. 47 of 167 runs have 250 or 300 games (900 ms development and seed 7, four-player development at 500 ms, some 3-player development), justified by 45–70 s of CPU time per game, which is acceptable. The four-player settings were adopted on such runs, and the paper discloses this. |
| Some experiments at 500 ms | ✓ (500 and 900 ms, 3 and 4 players) |
| GenAI: acknowledgement, citation, prompts | ✓ a first-page footnote cites [15]. The statement reproduces the report prompt verbatim (Turkish plus translation), the reviewer prompt outline and the tournament-sprint prompt; the reviewer reports, which served as revision prompts, are in `genai/` and `GENAI_PROMPTS.md`. Weak points: the development-session prompts behind the code are "not reproduced" (the brief requires prompts for AI-generated text, so this is tolerable); [15] names no model version. |
| Code package: `group{X}`, README with constructor and parameters, competition JSON, nothing outside the package | ✓ compiles standalone and both competition files run. **But the README begins "Rename first: replace every groupA … with your real group letter".** If the group is not A, the package line in 7 files, the folder, the JSON `class`/`heuristic` names, the README and the PDF metadata ("ECS7032P Group A") must be changed and the note deleted. |
| Forbidden MCTSPlayer | ✓ not used, copied or modified; it appears only as a benchmark opponent outside the package |
| Framework unchanged | No file under `TAG/src/main/java` outside `groupA` changed after the import commit. I could not compare the imported copy with the module's distribution (GitHub access to the upstream repository is not enabled in this session). |
| Author names and student numbers | placeholders; to be filled by the students |

---

## 5. Presentation

* **Density.** 125 decimal statistics (204 numbers in all) in the Results prose and captions, besides the 46 in Table III; four- and five-number parentheticals inside single sentences;
  Conclusion 1 is a list of 12 statistics. Readers lose the thread.
* **Internal codes.** H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, E1–E3, RQ1–3, v2–v4, "seed block 7/100 000", "3p/100".
  Codes are fine in Table I and Fig. 2 but should give way to names in the running text ("the decoupled
  information-set tree", "round-end rollouts").
* **Notation clashes.** s is the state (eq. 2) and the seed block (Table II), S is a modification and Ŝ the score
  estimate. B / b_p (banked), M / m_p (Maki), D / d_p (dessert), R / r (picks left), I / i (player), P / p / pp / P1
  (Fig. 1), and N is parent visits in Section II-B but availability in eq. (4) (explained, but the clash remains).
* **Undefined terms.** "denial" (Fig. 2 label "+ denial in rollouts", never explained), "non-locality", "UCB-Tuned",
  "exploitable", "online outcome sampling". The Table I footnote is cryptic: "β: Sashimi triple needing two cards; …
  γ: cards needed per missing card".
* **Odd phrases.** "within chance of the held-out result" (meaning "within sampling error"). "Every ablated component
  raises the score margin" (the removals lower it). The abstract's "chance 33.3%, 25%" sits after the four-player
  numbers.
* **Ambiguity.** Table II "Hardware, records: … candidate tests and validation: 4-core machine". The seed-7 validation
  ran on the 18-core laptop and the held-out validation on the 4-core machine.
* **Figures.** Fig. 2 has only two ticks per axis (−20, 0 and −5, 0), so values cannot be read off. Otherwise the
  figures are print-quality (600 dpi), legible at column width, and correctly referenced in order.
* **Positives.** Logical IMRaD structure, no padding, precise claims with intervals, honest limitations, consistent
  captions, cohesive (no patchwork).

---

## 6. Prioritised changes (with replacement text and length effect)

Line estimates use about 56 characters per column line. The main text now ends about 3.5 lines short of the bottom of
page 4. **The package below cuts about 8.6 lines and adds about 10.4–12.4 (the Table III column may cost up to 2), a net
of about +2 to +4 lines, which roughly fills the reserve. Plan on also making the N4 and factorial cuts at the end of
this section (about −2.5 lines) to be safe.**

### MUST

**M1. Four-player time claim** (Section IV-D, second paragraph; +1.8 lines). Replace the first two sentences, from
"With four players we found no gain from more time." up to "…, as with three players.", by:

> With four players our agent gained from its own extra time only up to 500 ms: against 500 ms opponents our 100 ms file
> won 9.0 ± 7.7 pp less than our 500 ms file and a tree cut to depth 1 lost 17.4 ± 8.2 pp, but 1000 ms (with *K* = 1.37)
> brought no detectable gain (−4.8 ± 8.1 pp; 80% power only for 12 pp; 250 development deals each). Our 500 ms file with every agent at
> 500 ms won 6.7 ± 4.1 pp less than our 100 ms file with every agent at 100 ms (*z* = 3.2; seed block 7 and held-out
> deals pooled), while TAG's ISMCTS rose from 24.1% to 32.1% of its seats.

Source for the new number: `experiments_v4/results/q5_FL8.csv` → `q5_X0b100.csv`, paired, −9.0 ± 7.7, z = 2.28.

**M2. RQ3 null** (Conclusions; 0 lines in isolation, absorbed by S3). Replace "the gain from S and I did not grow with
time with four players (6.4 and 3.0 pp at 100 and 500 ms, development)" by "the gain from S and I showed no sign of
growing with time with four players (6.4 and 3.0 pp at 100 and 500 ms, difference −3.4 ± 9.9 pp, development)".

**M3. Group letter and names** (outside the main text). Confirm the group letter. If it is not A, rename the package,
the JSON `class`/`heuristic` entries, the README and the PDF metadata/filename, and delete the "Rename first" note. Fill
in the authors and student numbers.

### SHOULD

**S1. Confront the v3-decline hypothesis with the held-out data** (Section IV-C, first paragraph; −3.1 lines).
Replace the whole paragraph by:

> In development (Fig. 3; independent samples) the gain from S and I grew from +5.6 to +9.0 and +14.8 pp at 100, 500
> and 900 ms (100 to 900 ms: +9.2 ± 10.4 pp, *z* = 1.7), suggesting that v3's sequential tree, in which later players
> react to cards chosen earlier in the same turn, is a wrong model that more iterations exploit more. The held-out
> deals do not support this: adding S and I to v3 gained +6.1 ± 5.9 pp at 100 ms and +5.3 ± 5.5 pp at 500 ms
> (difference −0.8 ± 8.1 pp, *z* = 0.2; 80% power only for 12 pp), as v3 and the final agent both lost about 5 pp at
> 500 ms. The development rise is probably overstated: v3's 50.1% at 100 ms was the best of 16 factorial cells
> (45.1–49.4% on new seeds).

**S2. Condense the four-player development paragraph** (Section IV-D, first paragraph; −1.6 lines). Replace it by:

> With four players S and I added +6.4 ± 5.2 pp over v3 at 100 ms and +3.0 ± 8.4 pp at 500 ms (development,
> independent samples). No four-player setting gained significantly in development (+1.0 to +5.8 pp at the budgets
> where we kept them, each ±4–8 pp; F + L lost 6.6 ± 6.0 pp at 100 ms); we kept each where its estimate was positive. The same settings (X0, W2, *K* = 1) cost 7.7 ± 5.4 pp
> with three players at 500 ms, 13.5 ± 9.1 pp (*z* = 2.9) different from their effect with four: consistent with E3,
> but post hoc and with F + L in the three-player arms only.

**S3. Make Conclusion 1 a synthesis** (−2.6 lines; includes M2's intent). Replace it by:

> For RQ1, the card-value evaluation matters most (28.3 pp of wins without it, 13.6 pp with TAG's lead heuristic
> instead), then the decoupled information-set tree (+8.2 ± 4.1 pp over v3's sequential determinised tree, two deal
> sets pooled); round-end rollouts, informed rollouts and information sets alone raise the score margin but not
> detectably the win rate. For RQ2, our agent's lead shrinks with time: with every agent at 500 ms it won 5.5 ± 4.2 pp
> less than at 100 ms, while TAG's ISMCTS gained 9.5 pp, and the decoupled tree's value showed no sign of growing
> (−0.8 ± 8.1 pp, held-out). For RQ3, settings tuned for four players hurt with three, and with four players time
> beyond 500 ms rarely changes the decision of the wide, shallow joint-move tree (both exploratory).

**S4. State the competitive standing against ISMCTS** (+0.9 to +2.9 lines in total).
* Add a column "ISMCTS seats %" to Table III: 26.4, 35.9, 36.3, 24.1, 32.1, 24.0, 24.3, –, –. The values are in
  `r2_val_*` logs, `r2_duct_3p100.log`, and the two field logs (22.9% over 600 seats and 26.5% over 400 → 24.3%).
  Make room by narrowing Opponents to 0.95 in and Margin to 0.62 in.
* In Section IV-C, replace "On the held-out deals ISMCTS won 26.4%, 35.9% and 36.3% of its seats at 100, 500 and
  900 ms (500 against 100 ms: +9.5 ± 5.7 pp): the extra time favoured ISMCTS over our agent." by "ISMCTS's share of its
  seats rose by 9.5 ± 5.7 pp from 100 to 500 ms (Table III): the extra time favoured ISMCTS over our agent." (−0.9 lines)
* Append to M1's paragraph: "At 500 ms, the competition setting, our 36.5% is no longer clearly above ISMCTS's 32.1% per
  seat." (+1.8 lines)

**S5. Abstract** (−0.2 lines; 124 words, within the 75–125 guidance). Replace everything from "On held-out deals …" to the end by:

> On held-out deals it wins 52.6%, 47.2% and 44.8% of three-player games (100, 500, 900 ms; chance 33.3%) against TAG's
> information-set MCTS (ISMCTS) and our previous agent, and 44.4% and 36.5% of four-player games (100, 500 ms;
> chance 25%). At 100 ms, the evaluation and the decoupled tree each raise its win rate significantly. With more time
> ISMCTS catches up: our lead over it per seat falls from 26 to 8 percentage points (three players, 100–900 ms).

52.6 − 26.4 = 26.2 and 44.78 − 36.29 = 8.5, so "26 to 8".

**S6. Define denial and link X3 to simulation balancing** (Section IV-B, last paragraph; +2.5 lines). Replace it by:

> Five later changes (Fig. 2, bottom) brought no detectable gain: four changed the win rate by −1.8 to +0.7 pp (each
> ±5.8), although one of them, card constants tuned for three players (X3), is the much stronger greedy policy (45.0%
> of games against two X0 players), consistent with simulation balancing [6]; another, denial, lets every simulated player
> value keeping the next player's best card from it. Rollouts to the game end with a win-gap value cost 23.7 ± 5.8 pp
> (*z* = 8.0).

**S7. Limitations** (Conclusion 2; +1.1 lines). After "Our opponents were framework agents and our own previous
version;" insert "H, B and R were ablated only with three players at 100 ms;".

**S8. Support the time-scaling explanation with data already logged** (Section IV-C, second paragraph; +1.7 lines).
Before "Plausibly, more iterations cannot correct the bias…", insert "Our agent's iterations per decision rose from
2.4k to 11.9k and 21.2k (`probe_iterations.txt`), yet its win rate fell." Values: 2,387 / 11,865 / 21,246 iterations
per decision, 2 games each.

**S9. Justify UCB-based decoupling in the Method** (Section III-C; +1.5 lines). After "…leads to the child of that
joint action [9]", add "(we use UCB, the best variant overall in [10], rather than EXP3 or regret matching)".

**S10. Table II "Hardware, records"** (0 lines). Change to "development, ablation and seed-7 validation runs: 18-core
laptop; the seed-7 re-run, later candidate tests and held-out runs: 4-core machine".

**S11. IV-A wording** (−0.2 lines). Replace "On seed block 7 it had won 55.0% at 100 ms (58.1% when re-run on the second
machine), within chance of the held-out result (z ≤ 1.8)." by "On seed block 7 it had won 55.0% (58.1% on the second
machine), within sampling error of the held-out result (z ≤ 1.8)." In the next sentence, write "Pudding bonuses" for
"Pudding".

**Net effect of M1 + S1–S11:** cuts −8.6 (S1 −3.1, S2 −1.6, S3 −2.6, S4 −0.9, S5 −0.2, S11 −0.2); additions +10.4
(M1 +1.8, S4 +1.8, S6 +2.5, S7 +1.1, S8 +1.7, S9 +1.5), plus 0–2 for the Table III column. The net of +1.8 to +3.8
lines roughly fills the 3.5-line reserve, but paragraph endings can round either way. If it overflows, apply N4 (−1) and the
factorial cut (−1.5) below.

### NICE

* **N1.** Replace codes with names in the running text (Results and Conclusions). Keep codes in Table I and Fig. 2.
  About neutral in length if done with S1–S3.
* **N2.** Rename the seed-block symbol (s → b) in Table II and Section III-E to remove the s/S/Ŝ clash.
* **N3.** Fig. 2: add ticks at −30, −20, −10, 0 and −6, −4, −2, 0. Optionally print the effect values beside the markers.
* **N4.** Rewrite the Table I footnote in words, or move it to the README. This can save one line.
* **N5.** Drop "and non-locality" in Section II-C or define it. Define "UCB-Tuned" or drop it.
* **N6.** Section II-C: "multi-player UCT, in which every player maximises its own value, computes a mixed-strategy
  equilibrium (max^n search computes a pure one) and played at least as well as earlier algorithms [11]".
* **N7.** In "a 2⁴ factorial on v3", write "around v3 (HBDP with or without R, M, K = 0.5, T = 8)", and give the
  interaction directions (R × T negative: R matters less when T = 8).
* **N8.** [15]: name the Claude model version(s) used.
* **N9.** Generate the hand-typed numbers in `build_report.py` from the data, and export the PDF with Times New Roman.
* **N10.** README: tiny inconsistencies (4p competition file 46.3% vs 46.2% in `RESULTS.md`; ±0.39 vs ±0.40 for
  `V_3p500`). Also, "no three-player decision saw the deals of seed block 7" is not strictly true: `d1_X0`, on fixed
  seed-1 deals, informed the decision not to use X0 with three players.
* **N11.** Optional limitation: "with CPU-time budgets, garbage collection, which made wall-clock time 1.4–1.8 × the
  budget in our runs, is charged to no agent". Source: `msPerDecision` in the CSVs.

### Cuts if more space is needed

* N4 (Table I footnote to README): about −1 line.
* Factorial sentence (Section IV-B; about −1.5 lines). Replace "P and M were not ablated in the final agent, as S
  requires M and I already merges identical cards; in a 2⁴ factorial on v3 (R, M, K = 0.5 or 1.41, T = 8 or 4;
  16 × 500 games, independent deals) R added 8.7 ± 2.1 pp and M 4.1 ± 2.1 pp, with R × T and M × K interactions
  (z = 4.0, 3.0)." by "P and M were not ablated in the final agent (S requires M; I already merges identical cards);
  a 2⁴ factorial around v3 (16 × 500 games, independent deals) confirmed R (+8.7 ± 2.1 pp) and M (+4.1 ± 2.1 pp)."
* DUCT sentence (Section IV-A; about −0.5 line). Replace "wins 21.0% [17.7, 24.8] of its seats in place of v2, the
  same share as v2 (0.0 ± 5.0 pp)" by "won 21.0% of its seats in place of v2, the same as v2 (0.0 ± 5.0 pp)".

---

## 7. Plain answer

**Would this get ≥ 85%?** As it stands, probably not with a strict marker: about **81%** (I would not be surprised by
78–86). The experiments and their statistics are at distinction-plus level and every number is right. The marks are
lost on readability, two overreaching statements and a conclusion that lists numbers instead of drawing lessons.

**What stands between it and 90%+:**
1. **Readability.** The Results must tell four or five findings with one or two numbers each, not 125
   decimal statistics. Codes go out of the prose, the four-player and budget paragraphs get condensed (S1, S2, N1), and the
   section moves toward the 500–700-word guidance.
2. **Correct scoping of claims.** Four-player time "beyond 500 ms" plus the omitted −9.0 ± 7.7 pp run (M1); RQ3
   "no sign of growing" (M2); ablation scope stated (S5, S7).
3. **Critical analysis that resolves its own tensions.** Retire the v3-decline hypothesis using the held-out evidence
   (S1). Show where the agent stands against the strongest framework agent in each setting, including the lack of a
   clear lead at 4p/500 (S4). Back the bias explanation of the time result with the iteration counts already logged (S8).
4. **Conclusions as synthesis (S3).** What the study teaches about search in drafting games, not a restatement.
5. **What text cannot fix.** The ablation exists only at 3p/100 ms, the weak v2 sits in every main field, and the
   four-player tuning rests on 250-game non-significant runs. These limit Experimentation to about 88 even after
   rewriting. Disclosing them, as S7 does, keeps the mark from dropping further.

With M1–M3 and S1–S11 applied, I would expect **about 85–87%**. 90%+ would additionally need the prose quality of a
published short paper and one or two insights into why the components work in Sushi Go (for example from the
card-type points and tree statistics already in the repository). It is within reach, but it is a rewrite of Results
and Conclusions, not a polish.
