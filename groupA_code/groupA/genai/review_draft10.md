# ECS7032P Assignment 1 – Group A: independent marking (review of draft 10)

**Marked:** `report/ECS7032P_Assignment1_Report_GroupA.pdf` (5 pages, built 28 Oct 05:18), `report/build_report.py`, `report/make_figures.py`, the package `TAG/src/main/java/groupA/` (identical to `groupA_code/groupA`), and the records in `groupA_code/experiments_v3`, `_v4`, `_v5` and `TAG/arena/v5`. I did not open `groupA/genai/review_draft*.md` or `report/reviews/`.

**Method.**
- I recomputed every number in the report with my own script, written from scratch: `/tmp/claude-0/review10/recompute.py`, output in `/tmp/claude-0/review10/recompute_out.txt`.
- I tested every branch of the pending-result wording on a copy in `/tmp/claude-0/review10/bt`. The driver is `/tmp/claude-0/review10/branch_test.py`; I built four PDFs with synthetic 900 ms and v3/500 ms results.
- I measured page fit from PDF text coordinates with `/tmp/claude-0/review10/lines_left.py`.
- I did not run Java, and nothing under `/home/user/statistics` was modified.

**Bottom line.** This is a rigorous, unusually well-controlled study.
- Every quoted statistic but two traces exactly to the raw files.
- The technical description matches the Java code.
- The literature claims I could check match the sources.

Five things hold it below 85:
- density and internal-code notation;
- three overstated or false sentences;
- conditional "pending" wording that breaks for the most likely outcome of the running 900 ms run;
- package/placeholder admin errors;
- modest originality.

My strict mark is **79%**; a likely range across markers is 76–83.

---

## 1. Marks by rubric item

**Weights** come from the Assessment Overview: Abstract 5%, Problem definition 15%, Approach 30%, Experimentation 30%, Conclusions 10%, General 10%. Items within an area are weighted equally.

### 1.1 Abstract (5%) – 76

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Brief motivation | 70–100 (low) | 72 | Motivation is one clause: "Sushi Go! combines hidden hands, simultaneous moves and delayed scoring, which basic Monte Carlo tree search (MCTS) ignores." It does not say why this matters beyond one game (drafting games as a testbed for simultaneous, imperfect-information search). |
| Brief summary | 70–100 | 80 | All key components are listed, at an appropriate length. |
| Outline of findings | 70–100 (low) | 76 | It contains "[pending]%". The most interesting finding is compressed into "500 instead of 100 ms favoured ISMCTS over our agent", which does not say that all agents got the longer budget. When the 900 ms run lands, the generated ending changes to "Our win rate fell at 500 ms but not detectably at 900 ms" (Section 6). That wording is wrong for the most likely outcome and loses the ISMCTS finding. |

### 1.2 Problem definition (15%) – 76.8

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Motivation (Introduction) | 70–100 (low) | 75 | Clear: basic MCTS assumes observability and sequential moves; cites [1]–[4]. Broader significance is thin. |
| Clarity of problem definition | 70–100 | 82 | The four properties (i)–(iv) and RQ1–RQ3 are crisp and map onto the modifications. |
| Clarity of contributions | 70–100 (low) | 74 | Bullet 2 is an experiment list, not a contribution: "its combination with … compared with TAG's own decoupled information-set MCTS and tested by leave-one-out and factorial ablations and on held-out deals". The importance to knowledge is not argued. |
| Critical analysis of the literature | 70–100 (low) | 73 | Good, critical coverage of PIMC/strategy fusion, SO/MO-ISMCTS, DUCT vs EXP3/RM, max-n and simulation balancing, with an explicit gap statement. Gaps: (a) it misses that Robilliard et al. [12] already used one tree with per-descent determinisations in a drafting game, close to I; (b) no other Sushi Go! or drafting AI work beyond [4]; (c) no Whitehouse et al. 2011 (determinisation vs ISMCTS in Dou Di Zhu), the natural reference for D vs I. |
| Description of the game | 70–100 | 80 | Concise and correct against `SGParameters` (108 cards, scoring, hand sizes). It highlights why the game is interesting (hidden information concentrated at round start; delayed scoring). |

### 1.3 Approach (30%) – 81.3

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Understanding and application (Methods + code) | 70–100 | 83 | DUCT on single-observer ISMCTS with availability counts, max-n, round-end rollouts and an exact fast simulator are implemented correctly, beyond lecture material (Section 3.1). Code is well commented. Against it: `SushiTreeNode` is 1753 lines carrying about ten disabled experimental switches (U, G, K, E, Q, N, …), which hurts the brief's "clarity" criterion. One BasicMCTS quirk is inherited: `expand()` makes `new Random(seed)` on every call. |
| Originality | 70–100 (low) | 75 | The paper says itself: "The search techniques come from the literature; our own element is the card-value model". TAG's own DUCT config already combines decoupling and information sets. The novelty is a hand-tuned domain model plus careful integration. |
| Difficulty and ambition | 70–100 | 86 | Many modifications, a verified integer-array simulator, a search-free tuner, a 2⁴ factorial, leave-one-out ablations, held-out validation, three budgets and two table sizes. |

### 1.4 Experimentation (30%) – 81.3

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Adequacy and rigour | 70–100 | 85 | Compared against v2, v3, TAG ISMCTS, TAG DUCT, BasicMCTS, RHEA and OSLA; paired leave-one-out with Holm; factorial; held-out deals; 500 games per key condition, with 250-game runs justified. Weaknesses: P never ablated in the final agent (inherent in I); four-player settings chosen on non-significant gains ("we kept each change where its estimate was positive"); the three-player time study changes every agent's budget, with no "our agent alone gets more time" arm; multiplicity outside Fig. 2 uncorrected; two runs pending. |
| Quality of documentation | 70–100 (low) | 79 | Table II is thorough (seating, seeds, deals, budgets). Against it: (a) "per-game results and logs of all 165 runs: our code package" is false for a `groupA`-only zip (Section 4); (b) the card model is not reproducible without the code (the Table I footnote is cryptic); (c) TAG version/commit not given ("TAG as provided for the module"); (d) "from 19 October" is a date where a definition is needed; (e) the TAG ISMCTS description omits `MAST: Both` and `exploreEpsilon 0.1`; (f) "nominally 45–70 s" contradicts the README's own 48–73 s. |
| Critical analysis of results | 70–100 (low) | 80 | Error analysis is strong: Wilson intervals, paired SEs, power, inverse-variance pooling, Holm, honest hedges ("probably overstated", "exploratory", "mixes the value of H with mis-scaled exploration"), and tree diagnostics for a mechanism. Three statements are wrong or overstated (Section 3.3): the correlation "at most 0.14"; "More time did not help the final agent"; "decoupling and information sets alone, untuned, did not beat our earlier agent". The main RQ2 finding (ISMCTS gains from time, ours does not) gets no mechanism. Paragraphs are number lists with little interpretation. |

### 1.5 Conclusions (10%) – 76

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Summary and conclusions | 70–100 (low) | 74 | A per-RQ recap of numbers rather than a critical synthesis. "(−10.2 pp; +8.2 ± 4.1 pp for it when pooled with the held-out replication)" flips sign inside one parenthesis. "For RQ2, more time did not help our agent" is ambiguous, since all agents got more time. There is no take-home message, e.g. "domain knowledge buys strength at low budgets but caps what extra search can add". |
| Future work | 70–100 | 78 | Three items, each with a reason tied to the results or literature (learned evaluation; RM/EXP3 via [9], [10]; opponent models). |

### 1.6 General (10%) – 76.7

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Clarity of ideas and expression | 50–69 / 70 border | 68 | Very dense: Method about 938 words (guide 700–800), Results about 1066 (guide 500–700). Heavy single-letter IDs collide with variables (S vs Ŝ vs s, P vs p vs P1–P3, M vs m_p, R vs r, D vs d_p, T/T_m/t, K/k, N/n, B vs b_p, W2 vs w). Long sentences, e.g. the 60-word W2 sentence in III-D. Assignment talk: "(the module does not allow building on TAG's advanced MCTS agent)", "TAG as provided for the module". Placeholder authors: "[Group member 2]", "Student no. [to be completed]". |
| Figures, tables, legends | 70–100 | 82 | Fig. 1 is conceptual and useful. Fig. 2 (paired effects with Holm-filled markers) is excellent. Fig. 3 is clear. Table I's footnote is unreadable, and Table III mixes deal sets, machines and opponent sets in one list. |
| Referencing | 70–100 | 80 | Consistent IEEE style, 15 relevant, mostly peer-reviewed references. Every claim I could check agrees with its source (Section 3.2). Minor issues: [10] pages (dblp lists pp. 1–8; the paper gives 232–239), [15] names no model or version, and the [12] clause on card values is unverified. |

### 1.7 Weighted total

| Area | Weight | Mark | Contribution |
|---|---|---|---|
| Abstract | 5% | 76.0 | 3.80 |
| Problem definition | 15% | 76.8 | 11.52 |
| Approach | 30% | 81.3 | 24.40 |
| Experimentation | 30% | 81.3 | 24.40 |
| Conclusions | 10% | 76.0 | 7.60 |
| General | 10% | 76.7 | 7.67 |
| **Total** | 100% | | **79.4 → 79%** |

With every "must" and "should" fix of Section 7, I would expect about 82–85.

---

## 2. Recomputation of every number

All values were recomputed from the CSV and log files by `/tmp/claude-0/review10/recompute.py`. Conventions:
- win = mean of `focusWin`, with Wilson 95% interval;
- margin = `focusScore − bestOppScore`, mean ± 1.96 SE;
- paired = per-game differences matched by game number;
- two-sample = difference of means with √(s₁²/n₁ + s₂²/n₂);
- pools = inverse variance;
- Holm at α = 0.05 within each group and measure.

Paths below are relative to `groupA_code/`: v5 = `experiments_v5/results`, v4 = `experiments_v4/results`, v3 = `experiments_v3/results`.

**Joins verified.**
- `r2_val_3p500.csv` and `r2_val_4p500.csv` equal their four chunks, shifted by 0/126/252/378 and 0/128/256/384. The focus seat is g mod n for all 500 games.
- `v5_field_3p100_all.csv` equals the 300-game run plus the 200-game run shifted by 300.

**Status key:** ✓ = matches; ✗ = mismatch; ~ = trivially inconsistent rounding/convention.

### 2.1 Abstract, Introduction, Table III, Results A

| Report value | Recomputed | Source | Status |
|---|---|---|---|
| 52.6% [48.2, 56.9], margin +0.73 ± 0.46 (3p/100 held-out) | 52.60 [48.22, 56.94], +0.730 ± 0.462 | v5 `r2_val_3p100.csv` | ✓ |
| 47.2% [42.9, 51.6], −0.10 ± 0.40 (3p/500) | 47.20 [42.86, 51.58], −0.104 ± 0.400 | v5 `r2_val_3p500.csv` (joined) | ✓ |
| 3p/900 "[pending]" | – | run in progress (`TAG/arena/v5`, chunks 0–1 done) | pending |
| 44.4% [40.1, 48.8], −0.61 ± 0.43 (4p/100) | 44.40 [40.10, 48.78], −0.614 ± 0.427 | v5 `r2_val_4p100.csv` | ✓ |
| 36.5% [32.4, 40.8], −1.83 ± 0.45 (4p/500) | 36.50 [32.40, 40.81], −1.832 ± 0.447 | v5 `r2_val_4p500.csv` (joined) | ✓ |
| DUCT, ISMCTS: 55.0 [50.6, 59.3], +0.72 ± 0.40 | 55.00 [50.62, 59.31], +0.722 ± 0.397 | v5 `r2_duct_3p100.csv` | ✓ |
| ISMCTS × 2: 51.3 [46.9, 55.7], +1.41 ± 0.51 | 51.30 [46.93, 55.65], +1.406 ± 0.508 | v5 `v5_field_3p100_all.csv` | ✓ |
| BasicMCTS, RHEA: 84.2 [80.7, 87.1], +6.68 ± 0.60 | 84.20 [80.74, 87.13], +6.682 ± 0.603 | v4 `B_mcts_rhea.csv` | ✓ |
| OSLA, BasicMCTS: 87.2 [84.0, 89.8], +9.74 ± 0.75 | 87.20 [83.99, 89.85], +9.744 ± 0.754 | v4 `B_osla_mcts.csv` | ✓ |
| "47.2–52.6% … 36.5–44.4%" | as above | – | ✓ |
| Seed 7: 55.0%; re-run 58.1%; "z ≤ 1.8" vs held-out | 54.97; 58.10; two-sample z = 0.76 and 1.77 | v4 `V_3p100`, v5 `v5_base3p100` | ✓ |
| Nigiri 17.6 / 15.3 / 12.9; Sashimi 9.1 vs 3.0; Pudding bonuses | 17.62 / 15.25 / 12.94; 9.08 vs 2.98; Pudding 2.88 vs 0.49 / 2.50 | v5 `r2_val_3p100.log` | ✓ |
| DUCT seat 21.0% [17.7, 24.8], "exactly v2's share" | 21.0 [17.66, 24.78]; v2 21.0 | v5 `r2_duct_3p100.log`, `r2_val_3p100.log` | ✓ |
| BasicMCTS/RHEA/OSLA "at most 12.8%" | 9.0, 6.8; 12.8, 0.0 | v4 `val16.log` | ✓ |
| 729 / 4096 joint moves; 9 / 8 cards | 9³, 8⁴; hand = 10 − n + 2 (`SGForwardModel`) | TAG source | ✓ |
| 108 cards and all scoring values | 8+12+6+4+14+14+14+5+10+5+6+10 = 108; values match | `SGParameters.java` | ✓ |
| Abstract: "Every ablated component raises the score margin significantly" | all 5 margin effects Holm-significant within the top group | Fig. 2 runs | ✓ (if both Fig. 2 groups were one family of 11 tests, R and I would lose significance on the margin) |
| "the evaluation and the decoupled tree also raise the win rate" | H, S+I Holm-significant; pooled S+I z = 3.90 | – | ✓ |
| "500 instead of 100 ms favoured ISMCTS" | ours −5.48 ± 4.19 (z −2.56); ISMCTS seats +9.50 ± 5.71 (z 3.26) | see 2.3 | ✓ |

### 2.2 Method / Experimental setup

| Report value | Recomputed / source | Status |
|---|---|---|
| α 0.5, T 4, π 1, ν 2, σ 15, w 0.7, ε 0.2, K 0.5 (3p) | `CardValueModel`, `SushiGoHeuristic`, `SushiMCTSParams` defaults; `final/*.json` | ✓ |
| X0 = (15, 0.7, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3); default tuple | `final/SushiMCTS_100.json` `fourPlayer.heuristic.args`; code constants | ✓ |
| W2 (w = 1, tanh(gap/2), σ·(ρ/2)^0.5, opponents' lead over mean), L = 8, K = 1 | `final/SushiMCTS_500_FL8.json`, competition files; `roundScale`, `terminalValues`, `scoreValue` | ✓ |
| v2 K 1.41; TAG ISMCTS K 1, UCB-Tuned, rollouts 300, ScoreHeuristic, MultiTree | `b100/ref.json`, `b100/tagmcts.json` | ✓ (omits MAST "Both", exploreEpsilon 0.1) |
| TAG DUCT: K 1, information sets, LeaderHeuristic, 30-move rollouts | `b100_tagduct.json` + `MCTSParams` defaults (K 1.0, Information_Set) | ✓ |
| BasicMCTS K 1.41 / 10; RHEA horizon 10, population 10; OSLA score heuristic | `fw_*.json` | ✓ |
| 900 ms / 430 ms with guards | competition JSON (guards 925 / 450 ms) | ✓ |
| Seating g mod n; swap every n games; 2 + 1 copies | `Arena.play` (`shift = (g / n) % 2`) | ✓ |
| Deals / seeds / held-out disjoint | `Arena.canonicalDeal`; `q2.txt`, `q3.txt`, `runs.md` | ✓ |
| "nominally 45–70 s of CPU time per game" | README: 3 × 27 × 0.9 ≈ 73 s; 4 × 24 × 0.5 ≈ 48 s | ✗ should be about 48–73 s |
| "165 runs", "60 of our 165" | 167 distinct CSVs ≥ 200 games, minus the two parts of the joined field run = 165; 60 four-player | ✓ |
| ±4.4 pp at 500 games; paired difference about ±5.7 pp; 8 pp at 80% power | 4.38; 500-game paired CIs 5.57–5.94; 2.80 × 2.91 = 8.1 | ✓ |
| **"correlate only weakly (at most 0.14 in our paired runs)"** | **r = 0.220** for `f5_FL8`~`f5_W2FL8` (the quoted 7.7 pp comparison); **0.175** for `q5_W2r`~`q5_FL8` (the quoted F + L test); max among 3p/100 seed-7 pairs 0.139; range −0.011 to 0.220 | **✗ false** |

### 2.3 Results B–E, Conclusions, figures

| Report value | Recomputed | Source | Status |
|---|---|---|---|
| Fig. 2 top, win: H −28.3, S+I −10.2, R −6.2, B −4.3, I −3.8 (filled: H, S+I) | −28.27 ± 5.79, −10.17 ± 5.75, −6.17 ± 5.94, −4.27 ± 5.83, −3.77 ± 5.68; Holm: H, S+I only | v4 `A_*` vs `V_3p100` | ✓ |
| Fig. 2 top, margin: all five filled | −5.45, −1.75, −0.74, −0.98, −0.63 (p 1e−50 … 0.025): all Holm-significant | v4 | ✓ |
| Fig. 2 bottom, win: lead −13.6, X3 −1.3, N −1.8, V2D −0.6, W1 +0.7, R3V2 −23.7 (filled: lead, R3V2) | identical; Holm: lead, R3V2 | v5 vs `v5_base3p100` | ✓ |
| Fig. 2 bottom, margin (filled: lead, R3V2) | −1.45, −0.57, −0.32, −0.60 (p 0.035, not Holm-significant), −0.09, −3.58 | v5 | ✓ |
| 28.3 ± 5.8 | 28.27 ± 5.79 | v4 | ✓ |
| Lead heuristic recovers 17.8 ± 5.6; still 13.6 ± 5.9 (z 4.5) below H; K 7.5 | +17.80 ± 5.57; −13.60 ± 5.88 (z 4.54); `b100_leadK75.json` K 7.5 | v4 `A_noH`, v5 `r2_lead_3p100` | ✓ |
| S + I 10.2 ± 5.7 (z 3.5); held-out +6.1 ± 5.9; pooled +8.2 ± 4.1 (z 3.9) | +10.17 ± 5.75 (3.47); +6.10 ± 5.87; +8.18 ± 4.11 (3.90); heterogeneity z = 0.97 | v4 `A_noSI`/`V_3p100`; v5 `r2_v3_3p100`/`r2_val_3p100` | ✓ |
| S alone +6.4 ± 5.7 (z 2.2); I adds 3.8 ± 5.7 | +6.40 ± 5.70 (2.20); +3.77 ± 5.68 | v4 `A_noSI`, `A_noI`, `V_3p100` | ✓ |
| Factorial: R +8.7 ± 2.1, M +4.1 ± 2.1; R×T, M×K z = 4.0, 2.8 | per-game contrasts: R ±2.10, **M ±1.99**, R×T z 3.97, M×K z 2.83. Cell-variance SE (correct for the independent v3 deals): M ±2.09, **M×K z 2.97** | v3 16 cells | ~ mixes conventions: write M ± 2.0, or keep ± 2.1 and z = 3.0 |
| Five later changes −1.8 to +0.7 (each ±5.8); R3V2 23.7 ± 5.8 (z 8.0); X3 greedy 45.0% | −1.80…+0.70, CIs 5.78–5.84; −23.70 ± 5.81 (8.00); 45.00% | v5; `greedy_duels.txt` | ✓ |
| Dev S+I +5.6 ± 6.1 → +14.8 ± 8.4; rise +9.2 ± 10.4 (z 1.7); v3 −17.3, final −8.1 | +5.60 ± 6.13; +14.80 ± 8.42; +9.20 ± 10.42 (1.73); −17.30; −8.10 | v4 `abl_T4`, `d1_I`, `f9_*` (two-sample) | ✓ |
| 11 000–12 000 iterations at 500 ms | 11 170 (v3), 11 865 (v4) | v5 `probe_iterations.txt` | ✓ |
| v3 50.1% best of 16; 45.1–49.4% on new seeds | max cell 50.1; 49.4 / 47.5 / 45.1 | v3 | ✓ |
| Held-out S+I +6.1 ± 5.9 (z 2.0); v3 46.5% | +6.10 ± 5.87 (2.04); 46.50 | v5 | ✓ |
| 500 vs 100 ms: −5.4 ± 5.9 (z 1.8); pooled −5.5 ± 4.2 (z 2.6) | −5.40 ± 5.93 (1.78); seed 7 −5.57 ± 5.92; pooled −5.48 ± 4.19 (2.56); seed-7 vs held-out difference z = 0.04 | v4 `V_3p*`, v5 `r2_val_3p*` | ✓ |
| ISMCTS 26.4% / 35.9% (+9.5 ± 5.7); v2 21.0% / 16.9% | 26.4 / 35.90 (+9.50 ± 5.71, two-sample); 21.0 / 16.90 | v5 logs (chunk-weighted) | ✓ |
| X0 +4.3 ± 4.3 (z 1.9); X0+W2+K1 +5.8 ± 7.4; F+L +1.0 ± 7.9 / −6.6 ± 6.0 | +4.27 ± 4.32 (1.94); +5.80 ± 7.39; +1.00 ± 7.86; −6.57 ± 5.96 | v4 `q4_*`, `val_q5`, `q5_*` | ✓ |
| 3p cost 7.7 ± 5.4 (z 2.8); interaction 13.5 ± 9.1 (z 2.9) | −7.70 ± 5.39 (2.80); +13.50 ± 9.14 (2.89) | v4 `f5_FL8`, `f5_W2FL8` | ✓ |
| Depth 1 −17.4 ± 8.2; 1000 ms alone (K 1.37) −4.8 ± 8.1 | −17.40 ± 8.19; −4.80 ± 8.08; K 1.37 in JSON | v4 `q5_FL8*` | ✓ |
| 4p 500 vs 100: 6.7 ± 4.1 less (z 3.2); ISMCTS 24.1 → 32.1% | seed 7 −5.40 ± 5.91, held-out −7.90 ± 5.74, pooled −6.69 ± 4.11 (3.19); 24.1 → 32.09 | v4, v5 | ✓ |
| 46.8%, 41.4% on seed 7 | 46.80, 41.40 | v4 `V_4p*` | ✓ |
| 73 positions, 704 children, 53% once; 2 and 4 vs 4 and 7 changed | `treestats4.txt`, `agree4.txt` (97.3 / 94.5 / 94.5 / 90.4% same) | v5 | ✓ |
| Fig. 3 points (dev and held-out) | v2 24.8/18.3/18.8; v3 50.1/40.7/32.8; v4 55.7/49.7/47.6; held-out 52.6, 47.2; v3 46.5 (intervals as in the CSVs) | v4, v5 | ✓ |
| Conclusions: −28.3, −10.2, +8.2 ± 4.1, −5.5 ± 4.2, 9.5, 7.7 ± 5.4, 6.7 ± 4.1 | as above | – | ✓ |

**Mismatches:**
1. The correlation claim, which is false.
2. "45–70 s" (should be 48–73 s).
3. The factorial CI/z convention mix (cosmetic).

**Untraceable numbers:** none. Ties are 0.5 outcomes in 1–2% of games, so Wilson with fractional wins is a harmless approximation.

---

## 3. Technical, literature and inferential checks

### 3.1 Description vs code (accurate)

- **(1) Card-value model** matches `CardValueModel.estimatedScore`:
  - Ŝ = b + α·u + m + π·ω_t·d, with ω_t = (round + 1)/3;
  - Wasabi bonus (3 − 1)·ν = 2ν;
  - Chopsticks value 1, with completion min(1, (r − k + 1)/T).
- **(2)–(3) Heuristic** matches `SushiGoHeuristic.valuesOf`.
- **B:** ε-greedy for all players, second Chopsticks card greedy in the tree (`greedyAction`) and ε-greedy in rollouts (`playOut`).
- **R:** stops when the round counter advances.
- **P:** `distinctActions`. Under I it is inherent, because statistics are keyed by `(type·4 + icons)·2 + chopsticks`, so "P and M stay (S and I build on them)" is correct.
- **M:** per-player values; the paranoid negation replaced.
- **S:** `selectDecoupled` tries untried cards first, then UCB on the player's own statistics with ln(N + 1).
- **I:** `informationSetIteration` re-samples via `state.copy(me)` in each iteration; open-loop nodes; one node per iteration; UCB uses `ln(availability)/visits`, exactly eq. (4) and the text.
- **Root move:** most root visits.
- **Base agent:** copied from `players.basicMCTS`; no `players.mcts` import anywhere in the package.
- **Framework:** files untouched since the import commit.
- **Not stated in the paper** (worth half a line): the final move is the card with the most root visits; the Chopsticks second-card decision uses the closed-loop tree.

### 3.2 Literature claims

Fetching was blocked for ludii.games and ieee-cog.org, so I checked these against abstracts in search results only.

- **[4] Klang et al.** The abstract describes Sushi Go! as mixing simultaneous action selection and complete information, reports mixed agent results, and says better heuristics would likely help. Both report sentences match. Author initials "C.-M. E. Klang" match Carl-Magnus Embring Klang.
- **[9] Lanctot et al.** "regret matching and OOS perform best and … all variants produce less exploitable strategies than UCT"; CCIS 408, pp. 28–43. Matches.
- **[10] Tak et al.** Nine games; best variant depends on game and opponent; "Overall, Decoupled UCT performs best". Matches. dblp gives pp. 1–8 (DOI 10.1109/CIG.2014.6932889), not 232–239.
- **[11] Sturtevant.** Mixed-strategy equilibrium; "as well or better than existing algorithms". Matches.
- **[7] Long et al.** Properties predicting PIMC success, including disambiguation; AAAI 2010, pp. 134–140. Matches.
- **[8] Goodman et al.** CoG 2022, pp. 292–299. Matches.
- **[12] Robilliard et al.** "many known results still hold" matches. "**card values depend on the stage of the game and on the other players' choices [12]**" is not in the abstract and I could not verify it. Either confirm it in the full text or reword as your own observation. The search snippets also show that this paper keeps one tree and draws a determinisation per descent, essentially single-observer ISMCTS in a drafting game. Mentioning this would sharpen the gap statement.
- **[3], [6], [2]:** consistent with my knowledge of the papers (not re-fetched).

**Misreporting risk:** low. The only unverified attribution is the [12] clause.

### 3.3 Are the conclusions supported? Overclaims, selection, multiplicity, power, confounds

1. **False statement:** "(at most 0.14 in our paired runs)". Two paired comparisons quoted in the paper have r = 0.22 and 0.175. The README already says "0.0–0.2".
2. **RQ2 overclaim:** "More time did not help the final agent" and "For RQ2, more time did not help our agent".
   - Every agent received the longer budget, so the data show a fall *relative to opponents*: ours −5.5 ± 4.2 pp while ISMCTS gained 9.5 pp of its seats.
   - Absolute strength was not measured. There is no three-player "our agent alone gets more time" arm, unlike the four-player 1000 ms run.
   - The text half-admits this ("or our parameters, tuned at 100 ms, suit longer budgets less"), but the lead sentences overstate it.
3. **DUCT inference:** "decoupling and information sets alone, untuned, did not beat our earlier agent".
   - DUCT never played v2. The two shares come from different tables (same deals).
   - TAG's DUCT also differs in heuristic (LeaderHeuristic) and rollouts (30 random moves), so "alone" is not established.
   - The difference is 0.0 ± 5.0 pp (two-sample).
4. **Four players, time:** "With four players, more time did not help."
   - The only own-time test (1000 ms, 250 games) also changes K (1.37) and has 80% power only for differences of about 11 pp.
   - The pooled 500-vs-100 comparison mixes budget and settings, as the conclusion admits.
   - "We found no gain from more time" is supportable; "did not help" is not.
5. **Selection effects.**
   - Acknowledged: v3's 50.1% is the best of 16 cells.
   - Under-discussed: the four-player 500 ms settings were kept on positive but non-significant estimates. The held-out result (36.5%) is below seed 7 (41.4%) and development (about 44%), as winner's-curse shrinkage would predict.
   - The control that would test them (the 100 ms four-player settings given 500 ms) is missing. Say so in the limitations.
6. **Multiplicity.**
   - Holm covers Fig. 2 only.
   - About 40 other z-tests are reported unadjusted, and conclusions lean on z ≈ 2 results: S alone (z 2.2, labelled unadjusted, good), the 3p cost of four-player settings (z 2.8, exploratory, good), and the pooled 500 ms dip (z 2.6).
   - Acceptable as flagged, but the abstract presents the 500 ms result without a hedge.
7. **Power.** Well handled for 500-game pairs (8 pp). Missing for every 250-game "no detectable" statement (about 11–12 pp).
8. **Confounds already acknowledged:** H ablation with unscaled K; lead heuristic across machines; X3 changing evaluation as well as rollouts; 4p budget × settings. Good practice.

---

## 4. Compliance

| Rule | Status |
|---|---|
| Main text ≤ 4 pages; GenAI and references may spill to page 5; total ≤ 5 | ✓ now (main text ends about 8 lines above the column bottom). With results, my builds leave **2.6–5.6 lines**. The most likely outcome (Section 6) leaves 2.6, less than the 3.5–6.5 you measured. |
| Template | ✓ IEEE Word template. Liberation Serif substituted for Times New Roman (metric-compatible, invisible). The template's first-page copyright footer is replaced by the GenAI note (fine). |
| Section word guide | Abstract 115 (125 in long branches, at the cap); Intro 453; Background 720; **Method 938 (700–800)**; **Results 1066 (500–700)**; Conclusions 263. Over by about 20–50% in the two densest sections. |
| References 6–15, consistent format | ✓ 15, IEEE. [15] should name the model/version. |
| ≥ 500 games or justified | ✓ All held-out and key runs have 500. The 250-game 900 ms and four-player development runs are justified by CPU time ("nominally 45–70 s" should read 48–73 s). |
| GenAI: acknowledge, cite, give prompts | ✓ First-page note, statement, [15], verbatim prompt with translation, reviewer prompts and reports in `groupA/genai/` (reviews 1–9 and prompts 1–9; 8 of 9 prompts contain the quoted phrase verbatim, draft 1 paraphrases it). Issues: "(all prompts and these reports …)" contradicts "The prompts of earlier development sessions are not reproduced here"; if you revise after this review, add `review_draft10.md` and `reviewer_prompt_draft10.txt`. |
| Code: package `group{LETTER}`, README with constructor, competition JSON, no `MCTSPlayer` code, BasicMCTS base | ✓ Two admin risks: the README opens with "**Rename first:** replace every `groupA` … with your real group letter", so confirm the letter and rename the package, JSON `class` entries and report file name; and the `json/` folder holds about 90 legacy configs (clutter). |
| **Claims about the package** | ✗ Table II: "per-game results and logs of all 165 runs: our code package". The brief limits the zip to the `groupA` package, and your own README says the experiments are "next to this folder, not part of the submission". Either copy the CSVs and logs into the package (about 7 MB) or change the sentence. |
| Author block | ✗ "[Group member 2]", "[Group member 3]", "Student no. [to be completed]". |

---

## 5. Presentation

- **Density.** Results paragraphs read as number lists: about 25 statistics in two Budget paragraphs. Move secondary numbers to a table or drop them, and give each paragraph one claim plus its evidence.
- **Internal codes and notation clashes.**
  - Table I defines 12 one- or two-letter IDs, which then collide with mathematical symbols: S (decoupled UCT) vs Ŝ_p (estimated score) vs s (state); P (pruning) vs p (player) vs P1–P3 (Fig. 1 players); M vs m_p; R vs r; D vs d_p; B vs b_p; K vs k; N vs n; T vs T_m vs t; W2 vs w.
  - Cheapest fix: rename the estimate Ŝ_p to Ê_p and the dessert term d_p to q_p. Better: use mnemonic IDs (EVAL, ROLL, ROUND, PRUNE, MAXN, DUCT, IS).
- **Jargon a non-member cannot parse.** "seed block 7", "second machine", "from 19 October", "TAG as provided for the module", "SushiGo_3+P_HiBudget" (a file name), and the Table I footnote ("β: Sashimi triple needing two cards; … κ per icon otherwise; δ: next Dumpling; γ: cards needed per missing card").
- **Assignment talk in a research paper** (the tips forbid it): "(the module does not allow building on TAG's advanced MCTS agent)"; "uses no code of TAG's advanced MCTS agent".
- **Figures.**
  - Fig. 2 is the best figure.
  - Fig. 3 would gain from putting the held-out markers in the legend title ("held-out deals, 500 games").
  - Fig. 1 is clear.
  - Captions are informative. Once the held-out 900 ms and v3/500 ms markers appear, the Fig. 3 caption should say they are 500 games each.
- **Table III** mixes held-out and seed-7 deals and two machines. Split it into "held-out deals" and "seed block 7, other opponents", or add a "deals" column.

---

## 6. The pending-result wording: every branch tested

**Set-up.** I copied `report/`, the three `results/` folders and the template to `/tmp/claude-0/review10/bt`. I generated synthetic `r2_val_3p900.csv` (500 independent games at a target win rate) with chunk logs `r2_val_3p900_c0..c3.log`, and synthetic `r2_v3_3p500.csv`. Then I imported `build_report` in a fresh process for each scenario, and built four PDFs. With no synthetic files the copy reproduces the current text exactly.

**Interim real data.** Chunks c0–c1 (252 games) of the 900 ms run are finished in `TAG/arena/v5/results`:
- the 900 ms agent won **43.5%**;
- the 100 ms agent won 50.0% on the same deals, a paired change of **−6.5 ± 8.2 pp**;
- ISMCTS took about 38% of its seats.

The most likely final state is therefore: held-out 900 ms change negative and perhaps significant on its own, while the pool with the 250 seed-7 deals (+1.6 ± 8.7) is **not** significant. Do not change the analysis plan because of this interim look.

**Results** (abbreviations: p900 = synthetic 900 ms win rate; RISE = held-out E2 test):

| Scenario | Abstract ending produced | Budget paragraph / Conclusions | Verdict |
|---|---|---|---|
| 900 fell (pooled z −3.3), E2 grew (z 2.1) | "Our win rate fell at 500 and 900 ms; the decoupled tree's advantage grew with time; …" | "Our win rate fell at 900 ms as well." / "more time lowered our agent's win rate, by 5.5 … and by 8.4 …" / "… as E2 predicted." | correct |
| 900 rose (z 2.6), E2 shrank (z −2.5) | "… fell at 500 ms but rose at 900 ms; … shrank with time; …" | "**At 900 ms it rose instead**, so the dip …" (the "it" follows a sentence about ISMCTS and "our parameters") / neutral conclusion | ambiguous pronoun |
| 900 ≈ 0 (z 0.1), E2 inconclusive | "… fell at 500 ms but not detectably at 900 ms; **the decoupled tree's advantage did not change detectably**; …" (125 words) | "… a difference of +0.1 ± 8.5 pp (z = 0.0), **inconclusive** (development …; 80% power only for 12 pp)." | grammatical but clipped; "did not change detectably" lacks "from 100 to 500 ms" |
| **900 = 45% (most likely): held-out −7.6 ± 6.3 (z 2.4), pooled −4.5 ± 5.1 (z 1.7)** | "Our win rate fell at 500 ms but **not detectably at 900 ms**" | "**At 900 ms the pooled change was not significant, so the dip at 500 ms may be a chance result or specific to that budget.**" | **inconsistent with the paper's own statistics.** The 900 ms estimate points the same way (held-out alone is larger than the 500 ms dip and significant), so the "chance result" inference is a non sequitur. The abstract calls a held-out −7.6 pp "not detectable". |
| 900 = 59%: held-out +6.4 ± 6.2 (z 2.0), pooled +4.8 ± 5.0 (z 1.9) | "… fell at 500 ms but not detectably at 900 ms" | The Results paragraph drops "More time did not help" (it tests the held-out z), but the **Conclusions say "For RQ2, more time did not help our agent … and by +4.8 ± 5.0 pp to 900 ms"** (they test the pooled z only) | **inconsistent between sections** |
| 900 pending, v3 done | "500 instead of 100 ms favoured ISMCTS over our agent; the decoupled tree's advantage did not change detectably; …" | E2 text as above | same clipped phrase |
| 900 fell, E2 shrank / 900 rose, E2 grew | as expected | as expected | correct |

**Other conditional points.**
- **If the 900 ms run is not finished at submission**, the abstract and Table III print "[pending]". There is no fallback that drops 900 ms.
- Method III-E says "E2 … **is tested** on the held-out deals" unconditionally, so it is false if `r2_v3_3p500` never completes.
- Everything else is correct in all branches:
  - Table III (switches to a Games column only if n ≠ 500);
  - `text_final_agent` ranges;
  - the Fig. 3 caption;
  - `seat_wins_chunks` (`finalize_r2.sh` → `sync_to_groupA_code.sh` copies the chunk logs, and `!*.log` keeps them tracked);
  - run counts (165 → 167, 60 four-player).

**Page fit with results** (lines left at the bottom of page 4):

| Scenario | Lines left |
|---|---|
| fell / grew | 5.6 |
| rose / shrank | 4.6 |
| ≈0 / inconclusive | 2.6 |
| 45% / inconclusive (most likely) | 2.6 |

Any net addition must therefore come with cuts (Section 7).

**Fix: one verdict function used by the abstract, Results and Conclusions.** Paste into `build_report.py` after `POOL900`:

```python
D95 = paired(VALF["3p500"], VALF["3p900"]) if BUD["3p900"] else None   # 900 vs 500 ms, same held-out deals

def v900():
    """one verdict on the 900 ms result for every section: fell, rose, down (n.s., same sign as 500 ms),
    flat (n.s., not negative) or split (pooled n.s. but the held-out deals alone significant)"""
    if POOL900 is None:
        return None
    z, zh = POOL900["zw"], BUD["3p900"]["zw"]
    if z <= -1.96: return "fell"
    if z >= 1.96: return "rose"
    if abs(zh) >= 1.96: return "split"
    return "down" if POOL900["dw"] < 0 else "flat"
```

Branch wording:

| Verdict | Abstract (`_abstract_end`) | Results (end of `text_budget2`) | Conclusions RQ2 lead |
|---|---|---|---|
| fell | "our win rate fell at 500 and 900 ms" | " Our win rate fell at 900 ms as well." | "giving every agent more time lowered our win rate, by" |
| rose | "our win rate fell at 500 ms but rose at 900 ms" | " At 900 ms our win rate rose instead, so the dip at 500 ms may be specific to that budget." | neutral |
| down | "our win rate fell at 500 ms (at 900 ms not significantly)" | f" At 900 ms the change points the same way but is not significant ({fpp(POOL900)})." | "giving every agent more time did not help ours" |
| flat | "our win rate fell at 500 ms but not at 900 ms" | f" At 900 ms the pooled change is not significant, and 900 ms differs from 500 ms by {fpp(D95)} on the held-out deals." | "giving every agent more time did not help ours" |
| split | "our win rate fell at 500 ms; at 900 ms the deal sets disagree" | f" At 900 ms the held-out deals ({fpp(BUD['3p900'])}) and the 250 seed-7 deals disagree; pooled, the change is not significant." | neutral |

The most likely outcome maps to these verdicts:
- held-out 900 ms change significant on its own, pooled change not significant (as in the 45% scenario: −7.6 pp held-out vs +1.6 pp on seed 7) → **split**;
- held-out change negative but not significant on its own → **down**.

Both wordings are then true to the numbers, and the Conclusions can no longer contradict the Results.

Also:
- `_abstract_end` E2 clause: replace "with time" and the bare "did not change detectably" with "from 100 to 500 ms" in all three branches (+2 words).
- `text_budget` inconclusive verdict: `"inconclusive"` → `"which is inconclusive"`.
- Fallback when 900 ms is missing at submission: in `ABSTRACT`, emit the 900 ms clause only `if VAL["3p900"]`; drop the 3p/900 row of `_T3` when `VAL["3p900"] is None`; make the Method sentence "E2 … is tested on the held-out deals" conditional on `RISE`.
- Keep the abstract ≤ 125 words; the build prints the count. The "down" branch with the E2 clause is about 128, so drop the E2 clause from the abstract unless E2 is significant.

---

## 7. Prioritised changes

Line estimates assume about 8 words per column line. Current worst-case slack is 2.6 lines.

### MUST (wrong, non-compliant, or will be marked as careless)

1. **Correlation claim** (`build_report.py`, III-E paragraph). Length 0.
   - Old: "(at most 0.14 in our paired runs)"
   - New: "(0.00–0.22 in our paired runs)"
2. **Pending-result branches.** Install the `v900()` logic above, the "from 100 to 500 ms" E2 wording, "which is inconclusive", and the 900 ms fallback. Length 0 (the replacements are as long as the originals).
3. **Fill or remove "[pending]"** and finish the runs. Then re-run `finalize_r2.sh`, `make_figures.py` and `build_report.py`, and re-measure page 4.
4. **RQ2 lead sentences** (all agents got more time). Length +1 word each.
   - `text_budget2` old: "More time did not help the final agent. On the held-out deals its win rate" → new: "Giving every agent more time did not help ours: on the held-out deals our win rate".
   - `text_conclusion_1`: "more time did not help our agent" → "giving every agent more time did not help ours".
   - `text_conclusion_1`: "more time lowered our agent's win rate" → "giving every agent more time lowered our win rate". Adjust the `down` test to `"lowered" in lead`.
5. **DUCT inference** (`text_final_agent`). +3 words.
   - Old: ", exactly v2's share: decoupling and information sets alone, untuned, did not beat our earlier agent."
   - New: ", the same share as v2 in that seat (difference 0.0 ± 5.0 pp): TAG's decoupled information-set search, with its lead heuristic and random rollouts, did no better than our previous agent."
6. **Package claim** (Table II, Hardware/records). Length 0 either way.
   - Either copy `experiments_v3/4/5/results` (CSV and logs) into `groupA/results/` and fix the README sentence "not part of the submission";
   - or write: "per-game results and logs of all 165 runs: available from the authors".
7. **Author block:** names and student numbers. Confirm the group letter and rename package, JSON `class` entries and file name if it is not A.
8. **Assignment talk** (Background, last paragraph). Saves about 1.5 lines.
   - Old: "We build on BasicMCTS, which lacks these features (the module does not allow building on TAG's advanced MCTS agent), so that each one's contribution can be measured."
   - New: "We build on the minimal BasicMCTS, which lacks these features, so that each one's contribution can be measured."
   - Table II: "TAG as provided for the module" → "TAG (commit/date …)" and state the commit.

### SHOULD (raise the mark, small length cost)

9. **Hedge the four-player time claim** (`text_four2`). +1 line.
   - Old: "With four players, more time did not help."
   - New: "With four players we found no gain from more time."
   - Old: "brought no detectable gain ({…})" → New: "brought no detectable gain ({…}; 250 games detect only ≥ 11 pp)"
10. **Give the main RQ2 finding a mechanism** (append to `text_budget2` after the ISMCTS sentence). +2.3 lines.
    - Add: "Plausibly, extra iterations cannot correct the bias of our hand-made rollout policy and evaluation, while ISMCTS's unbiased random rollouts keep gaining from more samples."
11. **Conclusions RQ1 sign flip** (`text_conclusion_1`). Length 0.
    - Old: "removing the evaluation H (−28.3 pp) or the decoupled information-set tree (−10.2 pp; +8.2 ± 4.1 pp for it when pooled with the held-out replication) also lowers the win rate."
    - New: "removing the evaluation H or the decoupled information-set tree also lowers the win rate (by 28.3 and 10.2 pp; pooled with the held-out replication the tree is worth 8.2 ± 4.1 pp)."
12. **Small corrections.** Length 0.
    - "nominally 45–70 s" → "about 48–73 s".
    - "M 4.1 ± 2.1 pp" → "M 4.1 ± 2.0 pp".
    - "from 19 October (all seed-7 and held-out runs) a fixed deck order" → "for all seed-7 and held-out runs a fixed deck order".
    - [10] pages "pp. 1–8" (IEEE Xplore pagination).
13. **Missing limitation** (Conclusions paragraph 2). +1.5 lines.
    - Add: "the four-player 500 ms settings were kept on non-significant gains and were never compared with the 100 ms settings at 500 ms".
14. **Literature** (Background C, 7 Wonders sentence). +1 line; references stay on page 5.
    - Confirm the [12] card-value clause, and add that [12] already searched one tree with a new determinisation per descent (close to I).
    - Optionally swap in Whitehouse et al. 2011 (Dou Di Zhu) for the D vs I discussion.
15. **GenAI statement** (page 5, outside the limit).
    - "(all prompts and these reports: …)" → "(the report prompts and these reports: …)".
    - Name the model and version in [15].
    - Shorten the list of reviewer requests (about 80 words) while keeping the verbatim prompts.
16. **Abstract motivation and RQ2 ending** (at most 125 words). Proposed text for the likely final state (114 words; add the 900 ms value):

> *Card-drafting games such as Sushi Go! combine hidden hands, simultaneous moves and delayed scoring, which basic Monte Carlo tree search (MCTS) ignores. We extended BasicMCTS of the Tabletop Games framework (TAG) with card-value rollouts, a lead-based evaluation, round-end rollouts, max-n backups and a decoupled information-set tree. On held-out deals it wins 52.6%, 47.2% and X% of three-player games (100, 500, 900 ms) against TAG's information-set MCTS (ISMCTS) and our previous agent, and 44.4% and 36.5% of four-player games (100, 500 ms; chance 33.3%, 25%). Every ablated component significantly raises the score margin; the evaluation and the decoupled tree also raise the win rate. Longer budgets for all agents helped ISMCTS more than ours; four-player settings hurt three-player play (exploratory).*

Generate the ending only when `_ism_gained()` is true and our pooled changes are ≤ 0; otherwise use the Section 6 verdict wording.

### NICE

17. Mnemonic IDs, or at least Ŝ_p → Ê_p and d_p → q_p (0 lines).
18. Rewrite the Table I footnote in words (+1 line). Example: "β multiplies a Sashimi triple that still needs two cards; the Maki standing gets weight 1/(1 + r/T_m) and κ points per icon the rest; δ weights the next Dumpling's points; a set counts as likely only if γ matching cards per missing card remain in the hands."
19. State the root rule: append to the I description "; the agent plays the card with the most root visits." (+0.5 line).
20. Split Table III by deal set; prune `json/` to the files the README uses.

### Cuts to pay for items 9, 10, 13, 14, 19 (about +6.5 lines) and restore slack

| Cut | Exact change | Saves |
|---|---|---|
| A | Results A, `text_final_agent`: "Table III reports the final agent: on the held-out deals it wins {…} of three-player and {…} of four-player games against v2 and TAG's ISMCTS." → "Table III reports the final agent." | about 2.5 lines |
| B | MUST 8 (Background) | about 1.5 lines |
| C | Results A: "At 100 ms it scores more from Nigiri (17.6 points per game, Wasabi included; v2 15.3, ISMCTS 12.9) and Pudding bonuses, while v2 collects Sashimi (9.1 against our 3.0)." → "At 100 ms it scores more from Nigiri (17.6 points per game against 15.3 and 12.9) and Pudding, v2 more from Sashimi (9.1 against 3.0)." | about 1 line |
| D | III-E: delete "Table II summarises the setup." | about 0.5 line |
| E | Intro ¶1: "; in the TAG version we use, an agent's copy of the state re-deals every card it has not seen" → "; in current TAG an agent's copy re-deals all unseen cards" | about 0.7 line |
| F | Last-study paragraph: "denial (rollout players also value taking the next player's favourite card)" → "denial in the rollouts" | about 1 line |

Total saved: about 7.2 lines. Net effect: about +0.7 lines of slack, so the worst case moves from 2.6 to about 3.3 lines.

If you skip item 10, slack rises to about 5.6 lines. Item 10 is the single best use of space for the "critical analysis" item.

---

## 8. Plain answer

**Would this get ≥ 85%? No, not as it stands.** My strict mark is 79 (likely range 76–83).

**After the must and should fixes:** about 82–85. The experimental rigour would be in the 85+ band, but the writing would still hold it back.

**What stands between it and 90%+:**
1. **Correctness of every sentence.**
   - Remove the false "at most 0.14";
   - reword the RQ2, DUCT and four-player-time overclaims;
   - fix the pending-result branches. For the most likely outcome they currently print "not detectably at 900 ms" and "the dip at 500 ms may be a chance result", which contradict the paper's own numbers; the Conclusions and Results can also disagree.
2. **Finish and complete:**
   - the 900 ms and v3/500 ms results;
   - authors;
   - group letter;
   - the package actually containing what Table II says it contains.
3. **Readability, the biggest remaining gap.**
   - Cut Method and Results by about 20% (move side results such as the five later changes and the four-player development numbers to a table or drop them);
   - replace the clashing one-letter codes;
   - one claim per paragraph;
   - no assignment talk.
4. **Interpretation over enumeration.** One central explanation tying RQ1–RQ3 together: hand-made knowledge gives strength at low budgets but caps what extra search adds, while unbiased ISMCTS keeps improving. The mechanism sentence (item 10) and the tree diagnostics are the start of this.
5. **Novelty.** The search methods are from the literature, and the new element is a hand-tuned card model. A 90+ paper in this module would usually have a sharper, game-specific algorithmic idea (for example a learned evaluation or an evaluated opponent model). That cannot be added honestly in a revision without new experiments.

**Outside the rubric:** the generative-AI statement shows the report and code were produced from a single prompt ("It should be a report that gets 100"). It is honest and required, so keep it. But a lecturer may discount originality, or question each member about the work, so every member must be able to explain the code and statistics, and the individual reflections must be truthful.

---

*Files: `/tmp/claude-0/review10/recompute.py` (recomputation), `/tmp/claude-0/review10/recompute_out.txt` (its output), `/tmp/claude-0/review10/branch_test.py` (branch driver on the copy `/tmp/claude-0/review10/bt`), `/tmp/claude-0/review10/lines_left.py` (page-4 slack). Sources consulted online (abstracts via search only): Lanctot et al., Springer CCIS 408 (https://link.springer.com/chapter/10.1007/978-3-319-05428-5_3); Tak et al., dblp (https://dblp.org/rec/conf/cig/TakLW14.html); Klang et al., IEEE Xplore 9618987 (https://ieeexplore.ieee.org/document/9618987/); Robilliard et al. (https://link.springer.com/chapter/10.1007/978-3-319-14923-3_5); Sturtevant (https://www.cs.du.edu/~sturtevant/papers/sturtevant2008analysis.html); Long et al. (https://ojs.aaai.org/index.php/AAAI/article/view/7562); Goodman et al., QMUL listing (https://researchpublications.its.qmul.ac.uk/publications/staff/42633.html).*
