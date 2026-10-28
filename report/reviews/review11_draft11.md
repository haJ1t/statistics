# ECS7032P Assignment 1, Group A: independent marking of report draft 11 and code

## 0. Verdict

- **Estimated mark: about 80% under strict marking (plausible range 77–86%).** On a strict reading this is **not ≥85%** yet. The statistics and the experimental protocol are well above what this module usually sees. The marks are lost on readability, on research questions that the evidence only partly answers, on the framing of the contributions, on a few overstated sentences, and on placeholders.
- **Every number in the paper reproduces from the raw files.** I recomputed more than 100 values with my own script. There are no numerical errors. The only nits are a standard-error method used in the factorial and some rounding and labelling (§3).
- **The text for the pending results has four wording defects, two of which are outright contradictions.** They appear only for some outcomes of the 900 ms run, but they would be printed automatically. I give a patch and re-tested it on every failing branch and on the current text (§7).
- **Compliance is fine on page limit and generative-AI disclosure.** Two things must be done before submission: fill in the author placeholders and confirm the group letter (§5).

---

## 1. Scope and method

- **What I read.** The brief, rubric, assessment overview and writing tips (`pdftotext -layout`). The report PDF, rendered at 80 and 200 dpi. `report/build_report.py` and `report/make_figures.py`, in full. The Java package, `README.md`, `GENAI_PROMPTS.md`, the JSON files and `results/`. The three experiment folders and `TAG/arena/v5`. For cross-checks I also read the TAG framework sources: Sushi Go rules, `BasicTreeNode`, the timer, and the TAG MCTS/DUCT configs.
- **What I did not read.** `genai/review_draft*.md` and `report/reviews/`. One repository-wide grep about "games in parallel" briefly printed three lines from `groupA_code/groupA/genai/review_draft*.md`. I did not open those files or use those lines. All later searches excluded `genai/` and `reviews/`.
- **What I ran** (all in `/tmp/claude-0/review11`, no Java started):
  - `/tmp/claude-0/review11/integrity.py` checks every CSV: unique, contiguous game numbers; seat = g mod n; win/margin consistency. 0 problems in all 181 distinct files.
  - `/tmp/claude-0/review11/recompute.py` is my own recomputation, written from scratch. Its output is in `/tmp/claude-0/review11/recompute_out.txt`.
  - `/tmp/claude-0/review11/synth_gen.py`, `synth_text.py` and `synth_search.py` build 12 synthetic scenarios for the two pending runs on copies under `/tmp/claude-0/review11/synth/`. Two of them (S9, S12) were built to PDF with an isolated LibreOffice profile, and `/tmp/claude-0/review11/fit.py` measures the free space left on page 4.
  - `/tmp/claude-0/review11/patched_build_report.py` is the build script with my proposed fixes applied and tested (§7).
- **Sources.** I could **not open the cited papers**: the session's network allowlist blocks arxiv.org and ludii.games. For the literature checks (§4.2) I used search-engine abstracts and snippets, two repository records (Maastricht CRIS for Tak et al.; QMUL publication pages for Goodman et al.) and my own knowledge of these papers. Where that is the only basis, I say so.

---

## 2. Rubric marking

**Weights** come from the Assessment Overview: Abstract 5%, Problem Definition 15%, Approach 30%, Experimentation 30%, Conclusions 10%, General 10%. The rubric gives no sub-weights, so I weight the items within each area equally.

### 1) Abstract (5%): 76

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Brief motivation | 70–100 | 75 | The motivation is clear and short: "Card-drafting games such as Sushi Go! combine hidden hands, simultaneous moves and delayed scoring, which basic Monte Carlo tree search (MCTS) ignores." But "ignores" is wrong for delayed scoring: BasicMCTS handles it through rollouts, just badly, and the Introduction itself says "addresses none of the first three". The abstract also never says why the problem matters beyond this one game. |
| Brief summary of the work | 70–100 | 80 | It names the five modifications and the held-out evaluation. The ablation design and the opponents are only implied ("Every ablated component …"). |
| Outline of findings | 70–100 (low) | 74 | The key numbers are there. Three problems: (a) "Longer budgets helped ISMCTS more than ours" is misleading, because our win rate *fell* significantly (pooled −5.5 ± 4.2 pp, z = 2.6), while the sentence implies it was helped a little. (b) "four-player settings hurt three-player play (exploratory)" is cryptic. (c) "(… 100, 500 ms; chance 33.3%, 25%)" puts the three-player chance level inside the four-player clause. |

### 2) Problem definition (15%): 75.6

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Motivation | 70–100 (low) | 74 | TAG and the assumptions of MCTS are cited [1]–[4], and Sushi Go!'s properties are spelled out. The wider importance (drafting games, simultaneous moves combined with hidden information) is asserted rather than argued, and only [4] is cited on Sushi Go!. |
| Clarity of problem definition | 70–100 | 82 | The four properties (i)–(iv) are crisp; (iv) is quantified ("729 or 4096 joint moves"). But RQ2 and RQ3 ask "how their value changes with the time per decision … and the number of players", and the Results do not measure that. RQ3 reports no four-player value of H, B, R, S or I. RQ2 measures a relative win rate while every agent's budget changes. |
| Clarity of contributions | 70 (boundary) | 70 | The bullets describe artefacts and tests, not knowledge gained. Bullet 2 mixes contribution and evaluation ("its combination with round-end rollouts, max-n backups, decoupled UCT and information sets, compared with TAG's own decoupled information-set MCTS and tested by leave-one-out and factorial ablations and on held-out deals"). Bullet 3 is "exploratory evidence". None says why it advances knowledge. |
| Critical analysis of the literature | 70–100 (low) | 74 | It covers the right ground: the flaws of perfect-information sampling, single- vs multiple-observer ISMCTS, DUCT vs EXP3/regret matching/outcome sampling [9], [10], max-n UCT [11] and 7 Wonders [12], and it states a gap. But it is short and descriptive. It never says why the single-observer variant should suit Sushi Go!, beyond cost. There is no work on opponent modelling or other drafting agents. "UCB-based DUCT need not converge to an equilibrium" has no source; it is usually attributed to Shafiei, Sturtevant and Schaeffer (2009). |
| Description of the game | 70–100 | 78 | Accurate: I checked every value against `SGParameters` and `SGCard`, the 108-card deck, the hand sizes 10 − n + 2 and TAG's `isHandKnown`. But it is one 70-word sentence, with no example turn and no picture of the hand passing. |

### 3) Approach (30%): 82.3

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Understanding and application (report + code) | 70–100 | 84 | Every mechanism I checked matches the code (§4.1): decoupled turn nodes, UCB with an availability count, open-loop single-observer ISMCTS adding one node per iteration, max-n value vectors, ε-greedy card-value rollouts for all players, rollouts to the round end, and the most-visited card at the root. It goes well beyond the lectures. Deductions: `SushiTreeNode.java` is 1,753 lines with about 20 switches that are off in the final agent (A, O, G, U, E, K, Q, N, …), which hides the final algorithm. The statement also says Claude wrote the code, so the students' own understanding rests on the individual reflections. |
| Originality | 70–100 (low) | 77 | The paper's own words: "The search techniques come from the literature; our own element is the card-value model shared by the evaluation and every player's simulation." The combination is sensible and goes beyond the course, but each piece is known. |
| Difficulty and ambition | 70–100 | 86 | Advanced tree search with substantial development: an exact fast simulator checked move by move against TAG, held-out validation, and a factorial design. |

### 4) Experimentation (30%): 82.3

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Adequacy and rigour | 70–100 | 83 | **Strengths:** five framework agents (TAG ISMCTS, TAG DUCT, BasicMCTS, RHEA, OSLA) plus two base versions (v2, v3); a leave-one-out ablation and a 2⁴ factorial; held-out deals; paired deals; 500 games except 250-game runs that are justified. **Gaps:** no four-player ablation of H/B/R/S/I (RQ3); the budget experiments change every agent's budget at once, so they measure relative strength only, with no three-player run where only our agent gets more time; all ablations use one opponent table (v2 + ISMCTS); the four-player settings were kept on non-significant 250-game gains (stated); and the held-out 3p/900 run and the E2 test are still pending. |
| Documentation | 70–100 | 82 | Table II is unusually complete (seeds, seat rotation, deal procedure, budgets, hardware, records). Table I's footnote gives every constant. The JSON files and all per-game CSVs are in the package (I checked: 251/251 CSVs identical). **Gaps:** the paired-deal harness (`Arena.canonicalDeal`) and the v2 opponent (`refv2`) are not in the submitted package, so the deal procedure cannot be re-run from the zip. The "Deals" row of Table II misdescribes which development runs are paired (§4.1). No TAG version or commit is given. |
| Critical analysis of the results | 70–100 | 82 | **Excellent error analysis:** Wilson intervals, paired standard errors, z, power, Holm, inverse-variance pooling, the selection bias of v3's best-of-16 cell, the K-scale confound behind "−H", and the machine re-run. There are real explanations (sequential tree; bias of a hand-made model; a wide, shallow four-player tree). **Deductions:** several overstatements (§4.3); the explanation for the budget effect is untested; and the density hides the main insights. |

### 5) Conclusions (10%): 79

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Summary and conclusions | 70–100 | 78 | Organised by research question, with numbers. But H's effect is given as "28.3" without the confound the Results admit. The RQ3 summary is about settings, not components, and the RQ2 summary depends on runs not yet finished. |
| Further work | 70–100 | 80 | Three items, each with a reason: learn the evaluation; test regret matching or EXP3 ([9], [10]); test the opponent models already in the code. It does not prioritise them or say what result would change the design. |

### 6) General (10%): 79.3

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Clarity of ideas and expression | 70 (boundary) | 72 | The structure is logical and the language precise, but the paper is very dense. The Results are 1,079 words of prose against a 500–700 guide, nearly every clause carries "± … pp (z = …)", and the reader has to keep about 20 internal codes in mind (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, E1–E3, v2–v4, "seed block 7", "second machine"). Example: "No four-player setting gave a significant gain in development: X0 changed the win rate by +4.3 ± 4.3 pp (z = 1.9) at 100 ms, X0 with W2 and K = 1 by +5.8 ± 7.4 pp at 500 ms, and F + L a further +1.0 ± 7.9 pp at 500 ms but −6.6 ± 6.0 pp at 100 ms; we kept each change where its estimate was positive." |
| Figures, tables and legends | 70–100 | 84 | Fig. 1 is conceptual and clear. Fig. 2, a two-measure forest plot with Holm-filled markers, is excellent. Fig. 3 is clear and the captions are informative. Fonts in the figures are about 7 pt, and Table II is a wall of text. |
| Referencing | 70–100 | 82 | 15 references in consistent IEEE style, mostly peer-reviewed, and as far as I could check described accurately (§4.2). Small omissions: a source for DUCT's non-convergence, and Frank and Basin for strategy fusion (covered indirectly via [3], [7]). |

### Weighted total

0.05 × 76.3 + 0.15 × 75.6 + 0.30 × 82.3 + 0.30 × 82.3 + 0.10 × 79.0 + 0.10 × 79.3 = 3.82 + 11.34 + 24.69 + 24.69 + 7.90 + 7.93 = **80.4%**.

---

## 3. Recomputation of every number

Methods:
- Win rate: mean of `focusWin`, with shared first places counted as 1/j.
- Wilson 95% interval with z = 1.96.
- Margin: `focusScore − bestOppScore`, mean ± 1.96 SE using the n − 1 standard deviation.
- Paired differences: per-game differences matched by game number.
- Independent runs: difference of means with the two-sample standard error.
- Pooling: inverse-variance.
- Holm: per measure, within each group of Fig. 2.
- Seat shares: from the arena logs; the chunked runs are seat-weighted over the chunk logs.

`v4/` means `groupA_code/experiments_v4/results`, and similarly for `v3/` and `v5/`.

| Report value | Recomputed | Source | Status |
|---|---|---|---|
| **Abstract / Table III** 3p/100: 52.6 [48.2, 56.9], +0.73 ± 0.46 | 52.60 [48.22, 56.94], +0.730 ± 0.462 | v5/r2_val_3p100.csv | ✓ |
| 3p/500: 47.2 [42.9, 51.6], −0.10 ± 0.40 | 47.20 [42.86, 51.58], −0.104 ± 0.400; the joined file equals chunks c0–c3 with offsets 0/126/252/378 | v5/r2_val_3p500.csv | ✓ |
| 4p/100: 44.4 [40.1, 48.8], −0.61 ± 0.43 | 44.40 [40.10, 48.78], −0.614 ± 0.427 | v5/r2_val_4p100.csv | ✓ |
| 4p/500: 36.5 [32.4, 40.8], −1.83 ± 0.45 | 36.50 [32.40, 40.81], −1.832 ± 0.447; joined file equals chunks with offsets 0/128/256/384 | v5/r2_val_4p500.csv | ✓ |
| DUCT row 55.0 [50.6, 59.3], +0.72 ± 0.40 | 55.00 [50.62, 59.31], +0.722 ± 0.397 | v5/r2_duct_3p100.csv | ✓ (held-out, as the missing ᵃ says) |
| ISMCTS × 2: 51.3 [46.9, 55.7], +1.41 ± 0.51 | 51.30 [46.93, 55.65], +1.406 ± 0.508; equals 300 + 200 games, offset 300 | v5/v5_field_3p100_all.csv | ✓ |
| BasicMCTS + RHEA 84.2 [80.7, 87.1], +6.68 ± 0.60 | 84.20 [80.74, 87.13], +6.682 ± 0.603 | v4/B_mcts_rhea.csv | ✓ |
| OSLA + BasicMCTS 87.2 [84.0, 89.8], +9.74 ± 0.75 | 87.20 [83.99, 89.847], +9.744 ± 0.754 | v4/B_osla_mcts.csv | ✓ |
| **IV-A** seed 7: 55.0% at 100 ms; re-run 58.1% | 54.97; 58.10 | v4/V_3p100.csv; v5/v5_base3p100.csv | ✓ |
| "z ≤ 1.8" (held-out vs seed 7) | two-sample z = 0.76 (old machine), 1.77 (new machine) | as above | ✓ |
| Nigiri 17.6 vs 15.3 and 12.9; Sashimi 9.1 vs 3.0; "and Pudding" | 17.62 / 15.25 / 12.94; 9.08 / 2.98; Pudding 2.88 vs 0.49 / 2.50 | v5/r2_val_3p100.log (focus Nigiri also from the CSV: 17.624) | ✓; nits: 15.25 is three 2-decimal log values summed, so "15.2" vs "15.3" cannot be decided. The per-type Pudding points exclude the −6 penalty: TAG's per-type counter never goes negative (0 negative values in 1,500 focus games). |
| DUCT seats 21.0 [17.7, 24.8]; vs v2 0.0 ± 5.0 pp | 21.0 [17.7, 24.8]; +0.0 ± 5.0 (binomial two-sample) | v5/r2_duct_3p100.log, r2_val_3p100.log | ✓ |
| "at most 12.8% of their seats (OSLA none)" | BasicMCTS 9.0, RHEA 6.8; BasicMCTS 12.8, OSLA 0.0 | RESULT blocks in the v4 batch logs | ✓ |
| **IV-B** −H: 28.3 ± 5.8 | −28.27 ± 5.79 (z 9.57) | V_3p100 → A_noH | ✓ |
| Lead heuristic recovers 17.8 ± 5.6 | +17.80 ± 5.57 | A_noH → v5/r2_lead_3p100 | ✓ |
| Lead vs H: 13.6 ± 5.9 (z = 4.5) | −13.60 ± 5.88 (z 4.54) | v5_base3p100 → r2_lead | ✓ |
| S + I: 10.2 ± 5.7 (z = 3.5) | −10.17 ± 5.749 (z 3.47) | V_3p100 → A_noSI | ✓ |
| Held-out S + I: +6.1 ± 5.9; pooled +8.2 ± 4.1, z = 3.9 | +6.10 ± 5.87 (z 2.04); +8.18 ± 4.11 (z 3.90) | r2_v3_3p100 → r2_val_3p100; pooled | ✓ |
| S alone +6.4 ± 5.7 (z = 2.2); I adds 3.8 ± 5.7 | +6.40 ± 5.70 (z 2.20); +3.77 ± 5.68 (z 1.30) | A_noSI → A_noI → V_3p100 | ✓ |
| Holm: margin all five; win only H and S + I | reproduced (noI margin z 2.24 passes at the last Holm step) | — | ✓ |
| Factorial R 8.7 ± 2.1, M 4.1 ± 2.0; R × T, M × K z = 4.0, 2.8 | per-game contrast (authors' method): 8.7 ± 2.1, 4.1 ± 2.0, z −3.97, +2.83. Independent-samples SE: 8.65 ± 2.09, 4.10 ± 2.09, z −3.96, **+2.97** | v3/ (16 cells) | ✓ values; **method nit**: these runs predate the fixed deal (independent deals), and §III-E promises two-sample SEs for independent samples |
| Best of 16 cells 50.1%; "45.1–49.4% on new seeds" | max 50.10 (abl_T4); 49.40 / 47.47 / 45.10 | v3/; rep_T4, tb1_T4, tb2_T4 | ✓ |
| Later changes "−1.8 to +0.7 pp (each ±5.8)" | X3 −1.30 ± 5.84; N05 −1.80 ± 5.79; V2D −0.60 ± 5.83; W1 +0.70 ± 5.78 | v5/v5_*_3p100 vs v5_base3p100 | ✓ |
| X3 "45.0% of games against two X0 players" | 45.00 ± 1.06 (8,000 games) | v5/greedy_duels.txt | ✓ |
| Game-end rollouts 23.7 ± 5.8 (z = 8.0) | −23.70 ± 5.81 (z 8.00) | v5_R3V2_3p100 | ✓ |
| **IV-C** dev S + I +5.6 ± 6.1 → +14.8 ± 8.4; rise +9.2 ± 10.4, z = 1.7 | +5.60 ± 6.13; +14.80 ± 8.42; +9.20 ± 10.42 (z 1.73) | abl_T4/d1_I; f9_finalT4/f9_I | ✓ |
| v3 lost 17.3 pp, final 8.1 pp | −17.30 ± 7.21; −8.10 ± 7.52 | v4 dev | ✓ |
| Held-out 500 vs 100: −5.4 ± 5.9 (z = 1.8); pooled −5.5 ± 4.2 (z = 2.6) | −5.40 ± 5.93 (z 1.78); −5.48 ± 4.19 (z 2.56) | r2_val; V_3p100/V_3p500 | ✓ |
| ISMCTS 26.4% → 35.9% (+9.5 ± 5.7); v2 21.0 → 16.9 | 26.4 → 35.90; +9.50 ± 5.71; 21.0 → 16.90 | logs (chunks seat-weighted) | ✓ |
| **IV-D** X0 +4.3 ± 4.3 (z = 1.9) | +4.27 ± 4.32 (z 1.94) | q4_I + q4_Io vs q4_X0 + q4_X0c | ✓ |
| W2 +5.8 ± 7.4; F + L +1.0 ± 7.9 (500 ms), −6.6 ± 6.0 (100 ms) | +5.80 ± 7.39; +1.00 ± 7.86; −6.57 ± 5.96 | val_q5 vs q5_W2 + q5_W2r; q5_W2r/q5_FL8; q4_X0c/q4_X0FL8 | ✓ |
| Three-player cost 7.7 ± 5.4 (z = 2.8); interaction 13.5 ± 9.1 (z = 2.9) | −7.70 ± 5.39 (z 2.80); 13.50 ± 9.14 (z 2.89) | f5_FL8/f5_W2FL8 | ✓ |
| Depth 1: 17.4 ± 8.2; t1000 −4.8 ± 8.1; "80% power only for 12 pp" | −17.40 ± 8.19; −4.80 ± 8.08; 2.80 SE = 11.6 | q5_FL8 vs d1 / t1000K14 | ✓ |
| 500 ms agent 6.7 ± 4.1 less (z = 3.2) | seed 7 −5.40 ± 5.91, held-out −7.90 ± 5.74, pooled −6.69 ± 4.11 (z 3.19) | V_4p*, r2_val_4p* | ✓ |
| ISMCTS 24.1% → 32.1% | 24.1 (748 seats) → 32.09 | logs | ✓ |
| Seed 7: 46.8 and 41.4 | 46.80, 41.40 | V_4p100, V_4p500 | ✓ |
| 73 positions, 704 root children, 53% visited once, 2 and 4 changed, repeats 4 and 7 | treestats4: 73 / 704 / 53%. agree4: 97.3% → 2, 94.5% → 4 (L16), 94.5% → 4 (rerun), 90.4% → 7 | v5/treestats4.txt, agree4.txt | ✓ |
| **III-E** ±4.4 pp; ±5.7 paired; "at most 0.22"; 8 pp at 80% power | 4.38; 5.57–5.94 over 500-game pairs; max r = 0.220 (f5_FL8/f5_W2FL8); 2.80 × 2.91 = 8.1 | — | ✓ |
| "60 of our 165 runs" | 165 distinct runs of ≥ 200 games (joined runs counted once), 60 four-player | all results | ✓ |
| "45–70 s of CPU time per game" | 3p/900: 77.3 decisions × 0.9 s = 69.5 s; 4p/500: 90.5 × 0.5 s = 45.2 s (nominal) | CSV `nDecisions` | ✓ |
| 729 / 4096; deck and card values | 9³, 8⁴; `SGParameters` | TAG source | ✓ |
| **Fig. 2** points and filled markers | as in IV-B; fills reproduced (V2D margin z = 2.10 correctly open) | — | ✓ |
| **Fig. 3** points | dev v2 24.8/18.3/18.8; v3 50.1/40.7/32.8; v4 55.7/49.7/47.6; held-out 52.6, 47.2; v3 46.5 | v4, v5 | ✓ |

**Untraceable to result files.** These are traceable only to the READMEs and scripts; they are plausible and not results: "18-core laptop", "4-core machine", "6–16 games in parallel" (v3/v4 READMEs) and "OpenJDK 21" (no version string in any log).

**Display nit.** The z values have one decimal, so z = 2.04 (significant) and 1.95 (not significant) both print as "z = 2.0". The current text has "(z = 2.0)" for the held-out S + I gain (2.04). My synthetic scenarios S6 and S8 print "(z = 2.0)" next to "not significant".

---

## 4. Method vs code, literature, and claims vs statistics

### 4.1 Technical description vs Java code

**Verified as described:**
- Eq. (1) is `CardValueModel.estimatedScore`: α·potential, the Maki standing, π·ω_t·Pudding with ω_t = (round + 1)/3, Wasabi 2ν, Chopsticks 1, and completion min(1, (r − k + 1)/T).
- Eqs. (2)–(3) are `SushiGoHeuristic` (σ 15, w 0.7).
- B and R are `rolloutCard`/`greedyAction`, ε 0.2, with the Chopsticks second card ε-greedy in rollouts and greedy in the tree; `finishRollout` stops when the round counter changes.
- P is `distinctActions`; M is per-player value vectors.
- S is `selectDecoupled`: untried first, then UCB on each player's own statistics.
- I is `informationSetIteration`: a re-sample every iteration, an open-loop tree, one node added per iteration, keys (type, Maki icons, Chopsticks), availability `val[3j+2]` used in ln N_p(a).
- The final move is the most-visited root card.
- W2, V, σ decay and the opponents' score value match the code and JSON.
- X0 matches the args in Table I's footnote.
- The competition JSONs are 900/925 ms and 430/450 ms.
- No `players.mcts` import anywhere in the package. The structure is still `BasicTreeNode`'s (mctsSearch / treePolicy / expand / ucb / rollOut / backUp / bestAction).
- The opponent configs in Table II match TAG's `SushiGo_3+P_HiBudget.json` and `SushiGoDUCT.json`. The ISMCTS opponent also has `MAST: Both, exploreEpsilon 0.1`, which is not stated; this is immaterial with random rollouts.

**Discrepancies and imprecisions:**
1. **Table II, "Deals".** It says "in all seed-7 and held-out runs a fixed deck order is shuffled with it (equal s, equal deals: paired runs); earlier runs used TAG's own deal (independent samples)." In fact every run after 19 October used the fixed deal. That includes the four-player development runs q4_Io, q4_X0c, q4_X0FL8, q5_W2, q5_W2r and q5_FL8…, the three-player runs f5_FL8 and f5_W2FL8, and d1_X0 (`make_v4_tables.py`, list `OLD`). The paired SEs the paper reports for F + L, depth 1, t1000 and the three-player cost depend on exactly those runs being paired. They are, but the table says they are not.
2. **"P and M stay; in a 2⁴ factorial on v3 …".** P is neither ablated in v4 nor part of the factorial (R, M, K, T), so the sentence suggests evidence for P that the paper does not have. In v3, removing P cost −2.1 ± 6.2 pp, not significant.
3. **"From 500 ms it also uses W2 …".** W2, F and L come from the 500 ms and competition JSON files; the agent does not switch automatically by budget. Harmless, but say "the 500 ms and competition files".

### 4.2 Literature claims

| Claim in the report | Verdict | Basis |
|---|---|---|
| [3] ISMCTS "avoids strategy fusion"; single-observer vs multiple-observer; "partially observable moves" | Correct in substance; "avoids" is slightly strong (Cowling et al. say ISMCTS *addresses* strategy fusion) | my knowledge of the paper |
| [4] Klang et al. "described it as combining simultaneous action selection with complete information … concluded that better heuristics should improve the agents" | Consistent with the indexed abstract ("mix of simultaneous moves and full information"; "better heuristics … should improve"). Full text not accessible here. pp. 1–4 not verified | search-engine abstract snippets (IEEE Xplore, Semantic Scholar) |
| [6] Silver and Tesauro: rollouts "should be balanced, i.e., give unbiased value estimates" | A fair gloss: they minimise the error of the mean outcome instead of maximising policy strength | my knowledge |
| [7] Long et al.: success linked to how fast hidden information is revealed; strategy fusion and non-locality | Correct (the "disambiguation factor") | my knowledge |
| [8] Goodman et al. MultiTree, CoG 2022, pp. 292–299 | Pages confirmed | QMUL publication pages (search snippet) |
| [9] Lanctot et al.: "regret matching and outcome sampling played best, and decoupled UCT's strategies were the most exploitable" | Matches the abstract as I recall it ("regret matching and OOS perform best … all variants produce less exploitable strategies than UCT"). Vol. 408, pp. 28–43 confirmed. **Paper not opened; the authors should check this sentence against the PDF** | memory + a citing reference list |
| [10] Tak et al.: nine games, best variant depends on game and opponent, DUCT best overall; pp. 232–239 | Matches the abstract ("Overall, Decoupled UCT performs best despite its theoretical shortcomings"); pages confirmed | Maastricht CRIS record (search snippet) |
| [11] Sturtevant: UCT in multi-player games computes a mixed-strategy equilibrium, as good as or better than existing algorithms | Correct | my knowledge of the abstract |
| [12] Robilliard et al.: card values depend on stage and others' choices; many known results hold | Correct | my knowledge of the abstract |
| [2], [5], [13], [14], [15] | Correct as cited | — |

I found nothing that misreports a source. Two sentences still need the PDFs to be fully sure: the Lanctot et al. sentence and the Klang et al. "complete information" characterisation.

### 4.3 Do the conclusions follow from the statistics next to them?

- **Overstatements:**
  - Abstract, "Longer budgets helped ISMCTS more than ours." Our agent was not helped; its pooled change was −5.5 ± 4.2 pp.
  - Conclusions, "removing the evaluation H … lowers the win rate (by 28.3 …)". The Results say this "mixes the value of H with mis-scaled exploration". The clean estimate is 13.6 ± 5.9 pp against TAG's lead heuristic with K rescaled.
  - IV-A, "TAG's decoupled information-set search … did no better than our previous agent." With 0.0 ± 5.0 pp, that means "not detectably different", not equivalent.
  - "P and M stay": see §4.1.
- **Selection effects** are handled well: the best-of-16 v3 cell, the overlap of seed block 7 with tuning deals, and four-player settings kept on non-significant gains are all stated. One residual: in the current (pending) text, the development time trend is quoted only for 100 → 900 ms (+9.2 ± 10.4). The 100 → 500 ms value (+3.4 ± 8.6) appears only in one future branch, which looks selective.
- **Multiplicity:** Holm is used within the Fig. 2 groups; everything else is labelled unadjusted or exploratory. Acceptable.
- **Power:** stated (8 pp at 500 games; 12 pp for t1000). Good.
- **Confounds** are stated: K scale with −H, K = 1.37 with t1000, F + L only in the three-player arms, X3 changing both the rollout policy and the evaluation, and the machine difference. **Not stated:** RQ2 changes every agent's budget, so it measures *relative* strength, and the explanation "more iterations cannot correct the bias of our hand-made rollouts" is never tested at three players (no run where only our agent gets more time).
- **RQ3 is not answered for components.** The data exist and are unused. S + I over v3 at four players: +7.8 ± 6.0 pp at 100 ms (q4_final → q4_I) and +3.0 ± 8.4 pp at 500 ms (q5_final → val_q5, 250 games), against +5.6 ± 6.1 and +9.0 ± 6.1 pp with three players (all independent samples). This fits the paper's "wide, shallow tree" story.
- **v3's decline:** the hypothesis is reasonable, and the development data hold unused support for it. v3 + S (which keeps v3's determinisation) declines less than v3: 51.4 → 46.9% against 50.1 → 40.7%. S alone gains +1.3 ± 6.1 pp at 100 ms and +6.2 ± 6.1 pp at 500 ms.

---

## 5. Compliance

| Requirement | Status |
|---|---|
| Main text ≤ 4 pages; generative-AI statement and references may spill to p. 5; total ≤ 5 | ✓ The main text ends on p. 4, about 10 lines from the bottom (measured: 10.1 lines). The statement starts on p. 4 and continues on p. 5 with the references; 5 pages in total. With synthetic pending results the slack drops to 2.9–4.1 lines (§7) and the statement's heading moves to p. 5. Still compliant. |
| Template | ✓ Built from the module's Word template (IEEE two-column A4). Liberation Serif stands in metrically for Times New Roman. The template's copyright footer is replaced with the AI note. |
| Section word guide (approximate) | Abstract 114 ✓ (all branches ≤ 124); Introduction 444 ✓; Background 702 ✓; Method 898 (guide 700–800, includes about 230 words of experimental setup); **Results 1,079 (guide 500–700)**; Conclusions 247 ✓ |
| References 6–15, one style | ✓ 15, IEEE |
| ≥ 500 games per experiment unless justified | ✓ 250-game runs (3p/900 development and seed 7; 4p/500 development) are justified by 45–70 s of CPU time per game; the held-out runs are 500 |
| Some experiments at 500 ms | ✓ |
| Generative-AI acknowledgement: citation and prompts | ✓ First-page note, statement before the references, citation [15], the verbatim Turkish prompt with a translation, the candidate-change prompt, and the reviewer prompts and reports referenced in `GENAI_PROMPTS.md` and `genai/` (the folder exists, with review_draft1–10 and reviewer_prompt_draft1–10). **Keep it complete:** if this review is used, add `reviewer_prompt_draft11.txt` and its report to `genai/`, and add any further user messages to `GENAI_PROMPTS.md`. |
| Authors | **✗ Placeholders:** "[Group member 2]", "[Group member 3]", "Student no. [to be completed]" (`AUTHORS` in `build_report.py`) |
| Code package: named group{LETTER}, README with constructor and JSON, competition JSON, nothing from `players.mcts`, no framework changes | ✓ README, two competition JSONs, no `players.mcts`, no framework file changed since import (git history). **Check the letter:** the README itself says "Rename first: replace every `groupA` … with your real group letter". If the group is not A, rename the package, the JSON `class` strings, the README and the report file name. |
| Package self-contained | ✓ Imports only TAG core, games, players and utilities. The package is 7.5 MB, mostly `results/`. 251/251 per-game CSVs match the experiment folders. Legacy `json/agents*`, `json/runs` and `json/opponents` folders are clutter but harmless. |

---

## 6. Presentation

- **Readability.** The Results read as a ledger. The values Fig. 2 already shows are repeated in IV-B, and parentheticals pile up, for example "TAG's lead heuristic (own score minus the best opponent's, K rescaled to 7.5) recovers 17.8 ± 5.6 pp of this loss (same deals, second machine) but still wins 13.6 ± 5.9 pp (z = 4.5) less than H; at round-end leaves, which hold no unfinished sets, H differs from it mainly by …".
- **Internal codes and jargon.** All codes are defined (Table I, Table II, §III-E), but there are many. "Second machine" is never named; Table II says "4-core machine". The list "card constants tuned for three players (X3), denial of the next player's favourite card in the rollouts, a win-gap value with σ decay and w = 1 changed the win rate …" reads as four items, of which the last two run together.
- **Notation clash.** *s* is both the state (Eqs. (1)–(2), H_i(s)) and the seed in Table II ("game g uses seed s + g (seed block s)"). Smaller overlaps (S the modification vs Ŝ the score, N the parent visits vs N_p(a)) are explained.
- **Figures and tables.** Fig. 2 is the best element of the paper. Fig. 3 combines development lines with held-out markers well. Figure fonts are about 7 pt. Table III's DUCT row (55.0%) has the same value as the seed-7 base quoted just below ("On seed block 7 it had won 55.0%"), which invites confusion; the row is held-out. "For these five components E1 holds" should be "these five ablations" (S + I is two components).

---

## 7. Conditional wording for the two pending runs

**State of the runs at review time.** `r2_val_3p900_c3` is running (started 05:59); the queue then runs `r2_v3_3p500_c0..c3`. So the 900 ms result will arrive before v3 at 500 ms. The possible states at submission are: neither run, the 900 ms run only, or both.

**Method.** I copied the report scripts, template and results to `/tmp/claude-0/review11/synth/`. For each scenario I wrote a synthetic 500-game `r2_val_3p900.csv` with matching chunk logs (chosen ISMCTS seat share) and, where relevant, `r2_v3_3p500.csv`. I then imported the authors' unmodified `build_report.py` and printed the abstract, Table III rows, `text_final_agent`, `text_budget`, `text_budget2`, the Fig. 3 caption, `text_conclusion_1` and the E2 sentence of the method.

| Scenario | v900 verdict | win at 900 / ISMCTS at 900 / E2 (RISE) | Result |
|---|---|---|---|
| base (current) | none | — | ✓ reproduces the current PDF |
| S1 | fell | 43.0 / 40.0 / – | ✓ wording; abstract understates ("helped ISMCTS more than ours") |
| **S2** | fell | 43.0 / **24.0** / – | **✗** `text_budget2` prints "while ISMCTS … keeps gaining from more samples" right after ISMCTS's 900 ms share (24.0%) falls below its 100 ms share (26.4%) |
| **S3** | rose | 62.0 / 30.0 / – | **✗** "the extra time favoured ISMCTS over our agent" (ours +9.4 pp, ISMCTS +3.6) and "Our parameters, tuned at 100 ms, may also suit longer budgets less" right after "our win rate rose" |
| **S4** | split_up | 58.6 / 28.0 / – | **✗** abstract "at 900 ms the two deal sets disagree": both estimates are positive (+1.6 and +6.0; heterogeneity z ≈ 0.8). Same two contradictions as S3 in `text_budget2` |
| S5 | split_down | 44.2 / 38.0 / – | ✓ |
| S6 | down | 46.4 / 36.0 / – | ✓; display nit: held-out "(z = 2.0)" next to "not significant" (z = 1.95) |
| S7, S7b | flat | 51.8 / 33.0; 56.0 / 30.0 | ✓ (S7b: "favoured ISMCTS" is borderline, ISMCTS +3.6 vs ours +3.4) |
| **S8** | down | 46.4 / **25.0** / – | **✗** same "keeps gaining" contradiction as S2 |
| S9 | down | 46.4 / 36.0 / +0.5 ± 8.5 (inconclusive) | ✓ "which is inconclusive (… 80% power only for 12 pp)"; the method's E2 sentence becomes "… and is tested on the held-out deals." |
| S10 | down | 46.4 / 36.0 / +11.1 ± 8.4 | ✓ "supports E2"; abstract adds "the decoupled tree's advantage grew at 500 ms" (124 words) |
| S11 | down | 46.4 / 36.0 / −11.3 ± 8.5 | ✓ "contradicts E2: the held-out test does not support the sequential-tree explanation" |
| S12 | split_down | 44.2 / 38.0 / +0.5 ± 8.5 | ✓ wording; built to PDF to measure the worst-case page fit |

Other checks:
- The Table III 3p/900 row appears and is formatted correctly when the run exists, and the "500 games each" note stays valid.
- `text_final_agent` correctly drops "significantly above chance" if the 900 ms lower bound falls below 33.3%.
- The Fig. 3 caption and markers are correct.
- `NRUNS` updates automatically (167).
- Abstract length stays at or below 124 words in every branch.

**Page fit** (with `fit.py`): current PDF 10.1 lines free; S9 (900 ms + inconclusive E2) **4.1 lines**; S12 (split_down + inconclusive E2) **2.9 lines**, with the statement's heading moving to p. 5. Still within 4 pages, but any net addition must be offset.

**Fix**, applied to `/tmp/claude-0/review11/patched_build_report.py` and re-tested on S2, S3, S4, S6, S8. All contradictions disappear; S6 and the current text are unchanged apart from the abstract clause.

```python
# _abstract_end
        s = "longer budgets helped ISMCTS but not our agent"            # was "... more than ours"
             "split_up": "our win rate fell at 500 ms but rose at 900 ms on the held-out deals",   # was "... the two deal sets disagree"
# text_budget2
    i = ISM500
    favoured = bool(i and i["sig"] and _ism_gained() and all(BUD[k]["dw"] <= 0 for k in ks))
    ...  + (": the extra time favoured ISMCTS over our agent." if favoured else "."))
    ...
    if favoured and V900 in (None, "fell", "down", "split_down"):
        s += (" Plausibly, more iterations cannot correct the bias of our hand-made rollouts and evaluation, while "
              "ISMCTS, without such a model, keeps gaining from more samples; our parameters were also tuned at "
              "100 ms.")
    elif V900 in ("fell", "down", "split_down", "flat"):
        s += " Our parameters were tuned at 100 ms."
    # (rose / split_up: no closing sentence; "may also suit longer budgets less" is removed)
```

**Workflow risk.** `finalize_r2.sh` joins the chunks and syncs `groupA_code/experiments_v5`. It does **not** run `sync_results_to_package.sh`, `make_figures.py` or `build_report.py`. If those are forgotten, the package `results/` and the figures will lack the new runs.

---

## 8. Prioritised changes

Length is given in column lines, about 9–10 words per line.

### Must (before submission)

1. **Authors.** Fill in `AUTHORS` (two names and three student numbers). 0 lines.
2. **Group letter.** Confirm it. If it is not A, rename the package line in the 7 `.java` files, the folder, the JSON `class` and `heuristic` strings, the README and the report file name. 0 lines.
3. **Abstract clause, printed now.** In `_abstract_end`, change "longer budgets helped ISMCTS more than ours" → "**longer budgets helped ISMCTS but not our agent**". +1 word, 0 lines.
4. **Conditional-wording fixes** from §7 (tested). 0 to −1 line depending on the branch.
5. **Final build checklist** once both runs end:
   - Run `finalize_r2.sh` → `sync_results_to_package.sh` → `make_figures.py` → `build_report.py`.
   - Check that the main text still ends on p. 4 (expect 3–4 free lines) and that the total is ≤ 5 pages.
   - Check that the Table III 3p/900 row is present.
   - Update `GENAI_PROMPTS.md` and `genai/` with this round.
   If the v3/500 run is not finished, the current fallback wording is correct (S1–S8 branches with the fix applied).

### Should

6. **Answer RQ3 for components** (the strongest single improvement for Experimentation and Problem Definition). Prepend to `text_four()`:
   ```python
   si4 = {100: two_sample([R4f("q4_final.csv")], [R4f("q4_I.csv")]), 500: two_sample([R4f("q5_final.csv")], [R4f("val_q5.csv")])}
   ... f"With four players S and I added {fpp(si4[100], z=False)} over v3 at 100 ms but only {fpp(si4[500], z=False)} at 500 ms (development, independent samples; three players: {fpp(DEVD['si100'], z=False)} and {fpp(DEVD['si500'], z=False)}). " + existing text
   ```
   This renders as "With four players S and I added +7.8 ± 6.0 pp over v3 at 100 ms but only +3.0 ± 8.4 pp at 500 ms (development, independent samples; three players: +5.6 ± 6.1 pp and +9.0 ± 6.1 pp)." **+4 lines.**
7. **Conclusion: caveat on H.** In `text_conclusion_1`, "removing the evaluation H or the decoupled information-set tree also lowers the win rate (by 28.3 and 10.2 pp" → "… also lowers the win rate (**H: 28.3 pp, or 13.6 pp against TAG's lead heuristic with K rescaled; tree: 10.2 pp**". The rest of the sentence ("; pooled with the held-out replication …") is unchanged. **+0.8 line.**
8. **Table II, Deals row.** "… in all seed-7 and held-out runs a fixed deck order is shuffled with it (…); earlier runs used TAG's own deal …" → "… **from 19 October (all seed-7 and held-out runs, later development runs)** a fixed deck order is shuffled with it (equal *s*, equal deals: paired runs); earlier runs used TAG's own deal (independent samples)". +0.5–1 table line.
9. **Factorial sentence**, which also removes the unsupported "P … stays" and is a cut. Replace "P and M stay; in a 2⁴ factorial on v3 (…; 16 × 500 games) R added 8.7 ± 2.1 pp and M 4.1 ± 2.0 pp, with R × T and M × K interactions (z = 4.0, 2.8)." with "In a 2⁴ factorial around v3 (R, M, K = 0.5 or 1.41, T = 8 or 4; 16 × 500 games, independent deals) R added 8.7 ± 2.1 pp and M 4.1 ± 2.1 pp; P, which only merges identical cards, was not ablated." **−0.5 line.**
10. **z display.** Add `zfmt(z)`, giving two decimals when 1.90 ≤ |z| < 2.05, and use it in `pp`, `fpp` and the five explicit `:.1f` z formats (`text_ablation`, `text_budget`, `text_four`, `text_four2`, `text_final_agent`). 0 lines.
11. **Abstract wording.**
    - "which basic Monte Carlo tree search (MCTS) ignores" → "**which basic Monte Carlo tree search (MCTS) does not address**". This matches the Introduction's "addresses none of the first three".
    - "four-player settings hurt three-player play (exploratory)" → "**settings tuned for four players hurt with three (exploratory)**".
    +2 words, 0 lines.
12. **DUCT sentence.** "did no better than our previous agent" → "**was not detectably better than our previous agent**". 0 lines.
13. **Time trend, for balance.** In `text_budget`, replace "the gain from S and I rose from +5.6 ± 6.1 pp at 100 ms to +14.8 ± 8.4 pp at 900 ms (by +9.2 ± 10.4 pp, z = 1.7)" with "the gain from S and I was **+5.6, +9.0 and +14.8 pp at 100, 500 and 900 ms (100 to 900 ms: +9.2 ± 10.4 pp, z = 1.7)**". In the inconclusive E2 branch, drop the then-redundant "development, 100 to 500 ms: … pp;". **−0.5 line.**
14. **RQ2 is relative** (if space allows). In `text_budget2`, "Giving every agent more time did not help ours: on the held-out deals our win rate" → "Giving every agent more time did not raise our win rate, a relative measure: on the held-out deals it". +0.5 line.
15. **Replicability.** Ship the harness (`Arena.java` with `canonicalDeal`) and the v2 opponent (`refv2`) inside the zip, for example as `groupA/experiments/…` with adjusted package lines and a compile check. Otherwise state in Table II that they are in the authors' repository. 0–1 table line.
16. **Literature.** Add the source of DUCT's non-convergence (M. Shafiei, N. Sturtevant and J. Schaeffer, "Comparing UCT versus CFR in simultaneous games," IJCAI GIGA Workshop, 2009), *after reading it*. The reference goes on p. 5; 0 main-text lines.

### Nice

17. Rename the seed letter in Table II (*s* → *k*) to avoid clashing with the state *s*.
18. Table III: write "DUCT, ISMCTS (held-out)" or reorder rows so that the 55.0% is not confused with seed block 7.
19. "for these five components E1 holds" → "for these five ablations E1 holds".
20. Figure fonts ≥ 7.5–8 pt.
21. Rewrite the contribution bullets as findings, for example: "(1) a card-value model whose evaluation beats TAG's lead heuristic by 13.6 ± 5.9 pp and whose rollout policy adds 0.98 ± 0.57 points of score margin; (2) decoupled selection and information sets together add 8.2 ± 4.1 pp (pooled over two deal sets) …". Note that the 13.6 pp swaps only the evaluation; both arms keep the card-value rollouts. This is roughly length-neutral if bullet 3 is shortened.

### Cuts to pay for items 6, 7 and 14

- C1: in `text_final_agent`, keep only "BasicMCTS, RHEA and OSLA win at most 12.8% of their seats." and drop "(OSLA none: its one-step look-ahead never reaches the reveal)". **−1 line.**
- C2: in `text_four`, replace "This is consistent with E3 but exploratory: the test was chosen after the four-player runs, whose gains were not significant, and only the three-player arms had F + L." with "…different from that with four: consistent with E3, but post hoc and with F + L in the three-player arms only." **−1 line.**
- C3: item 9 (factorial), **−0.5 line.**
- C4, if more space is needed: delete the card-points sentence in IV-A ("At 100 ms it scores more than v2 and ISMCTS from Nigiri … (9.1 against 3.0)."). **−3.5 lines.** It is a nice qualitative insight, so cut it last.

**Budget.**
- Items 6 + 7 + 13 + 14: about +4.8 lines.
- C1 + C2 + C3: −2.5 lines.
- Net: about +2.3 lines, against 2.9–4.1 free lines in the worst and typical pending cases. That is too tight; with C4 as well, the net is about −1.2 lines.
- If item 14 is skipped, items 6 + 7 + 13 net about +1.8 lines after C1–C3, leaving about 1 line in the worst case. Adding C4 is the safe choice.

---

## 9. Plain answer

**Would it get ≥ 85%? Probably not from a strict marker.** I put it at about 80%; a generous marker reading mainly for rigour might give the mid-80s. The numbers are all correct and the protocol is excellent; that is not what is holding it back.

**What stands between it and 90%+:**
1. **Readability (General/clarity).** The Results are about 1,080 words of number-dense prose with about 20 internal codes. Cut figures already shown in Fig. 2, shorten the parentheticals, and lead each subsection with its finding.
2. **Research questions only partly answered.** RQ3 has no component-level evidence at four players, although the development data exist (item 6). RQ2 measures relative strength with every budget changed and never tests the stated explanation.
3. **Contributions and literature framing** (Problem Definition, 15%). The contributions describe what was built rather than what was learned, and the literature is surveyed rather than analysed critically. The authors' own framing of originality ("our own element is the card-value model") caps that item at the high 70s.
4. **Overstated sentences.** "helped ISMCTS more than ours", the unqualified 28.3 pp for H in the Conclusions, "P and M stay", and "did no better".
5. **Pending-run text.** Four defects, two of them contradictions; fix before the final build (§7). Finish both runs if at all possible, so that E2 is tested rather than hypothesised.
6. **Placeholders and package letter.** These must be done.

Items 1, 3 and 5 decide whether it can reach the mid-to-high 80s. Reaching 90 would also need a sharper statement of what the study teaches about search in drafting games, plus the RQ3 evidence.
