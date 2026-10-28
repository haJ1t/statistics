# Review 8: ECS7032P Assignment 1, Group A (report draft 8 and code)

I marked this as a strict marker would, working only from the brief, the rubric, the overview, the writing tips, the report PDF and its generator scripts, the `groupA` code, and the raw CSV and log files. I did not open `report/reviews/`, and I left those files out of my sandbox copy. Nothing under `/home/user/statistics` was changed.

**Working files** (all in `/tmp/claude-0/review8/`):
- `recompute.py` and `recompute_out.txt`: my recomputation, written from scratch.
- `scenarios.py`, `scen_now_A.txt` and `scen_rest.txt`: tests of the pending-result branches with synthetic results.
- `sandbox/{now,A,B}/report/*.pdf`: full rebuilds of the report. The `now` rebuild gives text identical to the submitted PDF.
- `pages/` and `hi/`: page renders.

**Verdict in one line.** The experimental work is excellent: 165 runs of 200 or more games, about 71,000 games in all. Every number I recomputed matches. The report loses marks for four things:
- **Writing:** it is very dense and full of internal letter codes.
- **Reasoning:** a few claims go further than the statistics, and one is factually wrong (the "overlap" of seed block 7).
- **Pending-result logic:** the conditional text in `build_report.py` can print false or inconsistent wording for some outcomes.
- **Submission state:** there are unfinished items (author placeholders, "[pending]", and the prompt files referred to by the generative-AI statement).

As it stands I would mark it at **about 82%**.

---

## 1. Rubric marks

The weights come from `Assignment 1 - Assessment Overview.pdf`: Abstract 5%, Problem Definition 15%, Approach 30%, Experimentation 30%, Conclusions 10%, General 10%. Within each area I averaged the sub-items equally.

| Area (weight) | Item | Band | Mark | Reasons (quotes from the report) |
|---|---|---|---|---|
| **1 Abstract (5%)** | Motivation | 70–100 | 76 | Correct and concise: "players pick cards simultaneously from hidden hands that are passed on, which basic Monte Carlo tree search (MCTS) ignores". It is a single clause, and it never says why the problem matters beyond that. |
| | Summary | 70–100 | 78 | All the components are named, at the 125-word maximum. |
| | Findings | 70–100 | 72 | The numbers are good, but three things weaken them. **(a)** "[pending]%" is still in the text. **(b)** Two phrases are hard to read: "500 instead of 100 ms helped ISMCTS more than our agent" (it only shows a relative shift) and "four-player settings hurt with three". **(c)** It leaves out the confirmatory held-out test of E2, the main RQ2 hypothesis, and the large margin over BasicMCTS. |
| | **Area mark** | | **75** | |
| **2 Problem definition (15%)** | Motivation | 70–100 | 80 | Clear and well cited: TAG [1], MCTS [2], ISMCTS [3], Klang et al. [4]. The case for why this matters beyond Sushi Go! is thin. |
| | Problem clarity | 70–100 | 86 | Properties (i)–(iv) of the game, each mapped to a weakness of BasicMCTS, plus RQ1–RQ3. This is exemplary. |
| | Contributions | 70–100 | 78 | Listed. The third bullet is honestly hedged ("exploratory evidence"). They are modest, and the report never says why they advance knowledge. |
| | Critical literature analysis | 70–100 (low) | 75 | There is real critique: the flaws of PIMC [7, 8], single- versus multiple-observer ISMCTS [3], how exploitable DUCT is [10], game dependence [11], and simulation balancing [6]. A gap statement is present. Missing: AI work on drafting games (e.g. *Magic: The Gathering* drafting), the RL use of Sushi Go! in TAG (PyTAG), and convergence results for simultaneous-move MCTS. Also, the report never says outright that Klang et al. evaluated a version with all cards visible (Lab 4 says so), which is the obvious gap this work fills. |
| | Game description | 70–100 | 86 | Precise rules (checked against `SGParameters` and `SGCard`) and the properties that matter for AI ("hidden information is concentrated at the start of each round"). |
| | **Area mark** | | **81** | |
| **3 Approach (30%)** | Understanding and application | 70–100 | 87 | The code implements exactly what Method describes (see §3a): decoupled UCT (DUCT) with single-observer information sets and availability-count UCB (Eq. 4), max-n backups, round-end rollouts, the card-value model, and a fast simulator checked against TAG. This goes beyond the lectures. |
| | Originality | 70–100 | 82 | Literature techniques combined with the group's own card-value model, which is shared by the rollouts and the evaluation, and with four-player-specific settings. The report itself says "our own element is the card-value model". |
| | Ambition | 70–100 | 88 | Level 4–5 on the brief's scale. |
| | Code clarity (affects this area) | – | −1 | Correct and heavily commented. However, `SushiTreeNode.java` runs to 1,753 lines with about 15 experimental switches that are switched off (A, O, G, U, K, E, Q, N, treeThreshold, ownCardTree, maxChildShare, …). The README is about 35 KB and opens with "Rename first: replace every `groupA` …". |
| | **Area mark** | | **85** | |
| **4 Experimentation (30%)** | Adequacy and rigour | 70–100 | 88 | Comparisons with v2, v3, TAG's ISMCTS, TAG's DUCT, BasicMCTS, RHEA and OSLA. Leave-one-out and 2^4 factorial ablations. Held-out validation on deals never used before. Holm corrections, a power statement, paired designs, and budgets of 100/500/900 ms with 3 and 4 players. Weaknesses: one opponent field (v2 + ISMCTS) for nearly everything, a four-player part that is exploratory with 250-game runs, and ablations only at 3 players / 100 ms. |
| | Documentation | 70–100 | 85 | Table II (seeds, seating, deals, budgets, opponent configurations, hardware) would let someone replicate the study. The harness itself is not in the submission ("records from the authors"). |
| | Critical analysis | 70–100 (low) | 79 | The error analysis is very good: Wilson intervals, paired SE, z, Holm, power. It states hypotheses (E1–E3) and gives mechanistic evidence: tree statistics, card-type points, the winner's-curse caveat on v3. Against that: several over-reaches (§3c); the seed-block-7 "overlap" reasoning is wrong (§3c-1); "after a Holm correction" is combined with numbers from outside the corrected family; and the Results section (1,098 words against 500–700 recommended) is a dense list of figures. |
| | **Area mark** | | **84** | |
| **5 Conclusions (10%)** | Summary | 70–100 (low) | 75 | Organised by RQ with statistics. It mislabels results ("removing the evaluation H (13.6 pp over TAG's lead heuristic)"), mixes pooled and Holm-corrected figures, and depends on undefined codes ("switch Q"). |
| | Future work | 70–100 | 80 | Reasoned and tied to the literature: learn the evaluation, try regret matching/EXP3 [10, 11], fit opponent models. |
| | **Area mark** | | **78** | |
| **6 General (10%)** | Clarity | 50–69 / 70 border | 70 | The structure is logical, but the prose reads like a compressed lab notebook. About 15 letter codes are used in running text (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, Q), and symbols clash (§5). Many sentences pack 3–6 numbers into parentheses. Some phrases are unclear ("as in development", "the three-player arms"). Two author slots and three student numbers are still placeholders. |
| | Figures and tables | 70–100 | 86 | Fig. 1 is a real conceptual figure. Fig. 2 is a forest plot that marks Holm significance by filled markers. Fig. 3 shows trends with intervals. The captions are informative. Table III still shows "pending". |
| | Referencing | 70–100 | 84 | Consistent IEEE style, mostly peer-reviewed, and accurate wherever I could check (§3b). There are 16 references against the 6–15 guidance. |
| | **Area mark** | | **78** | |

**Weighted total:** 0.05·75 + 0.15·81 + 0.30·85 + 0.30·84 + 0.10·78 + 0.10·78 = 3.75 + 12.15 + 25.5 + 25.2 + 7.8 + 7.8 = **82.2 → 82%**.

Section word counts, from the generator's own count (guidance in brackets): Abstract 125 (75–125), Introduction 444 (350–450), Background 702 (650–850), Method 885 (700–800), Results 1,098 (500–700), Conclusions 270 (200–300).

---

## 2. Recomputation of every number (my own script, `recompute.py`)

**Methods used.**
- Win rate: the mean of `focusWin`, where a j-way shared win counts 1/j, with a Wilson 95% interval.
- Score margin: the mean of `focusScore − bestOppScore`, ± 1.96 SE.
- Paired differences: matched by game number.
- Two-sample SE: used for independent (pre-canonical or different-seed) runs.
- Pooling: inverse-variance.
- Holm: two-sided normal p-values.

**Merge checks.** `R5 = groupA_code/experiments_v5/results`, `R4 = …/experiments_v4/results`.
- `r2_val_3p500.csv` and `r2_val_4p500.csv` are exactly the chunk files `_c0`–`_c3` with offsets 0/126/252/378 and 0/128/256/384, all multiples of 2n. In every merged run the focus seat equals g mod n.
- `v5_field_3p100_all.csv` is exactly the 300-game run plus `r2_field_3p100b` shifted by 300.
- The copies in `TAG/arena/v5/results` and `groupA_code` are byte-identical, and so are the v3 CSVs copied into v4.

**Result: no numeric mismatch.** Every number in the text, tables and figures matches the raw files to the printed precision. The small notes are marked "note" below.

| Location | Report | Recomputed | Source |
|---|---|---|---|
| Abstract, Table III | 52.6% [48.2, 56.9], +0.73 ± 0.46 | 52.6 [48.2, 56.9], +0.73 ± 0.46 | R5/r2_val_3p100.csv |
| | 47.2% [42.9, 51.6], −0.10 ± 0.40 | identical | R5/r2_val_3p500.csv (merged) |
| | 3p/900 "pending" | file absent | (run `r2_val_3p900_c0` at 50/126 games when checked) |
| | 44.4% [40.1, 48.8], −0.61 ± 0.43 | identical | R5/r2_val_4p100.csv |
| | 36.5% [32.4, 40.8], −1.83 ± 0.45 | identical | R5/r2_val_4p500.csv (merged) |
| Table III | DUCT field 55.0 [50.6, 59.3], +0.72 ± 0.40 | identical | R5/r2_duct_3p100.csv |
| | ISMCTS × 2: 51.3 [46.9, 55.7], +1.41 ± 0.51 | identical | R5/v5_field_3p100_all.csv |
| | BasicMCTS + RHEA: 84.2 [80.7, 87.1], +6.68 ± 0.60 | identical | R4/B_mcts_rhea.csv |
| | OSLA + BasicMCTS: 87.2 [84.0, 89.8], +9.74 ± 0.75 | identical | R4/B_osla_mcts.csv |
| Abstract, §IV-B | all five ablations raise the margin significantly (Holm) | Holm over the margins: p = 1e−50, 3.8e−9, 7.7e−4, 0.016, 0.025 → all significant | R4/V_3p100 vs A_* |
| | win rate: only H and S + I (Holm) | H z = −9.57, S+I z = −3.47 significant; −R p = 0.042 > 0.05/3 → stop | same |
| §III-E | ±4.4 pp; paired difference ±5.7 pp; correlation 0.0–0.2; 8 pp at 80% power | 4.38; 5.68–5.94 for 500-game paired runs; correlation +0.009…+0.220 over 21 paired comparisons (note: one is 0.22); 2.8 × 2.91 = 8.1 | various |
| §III-E | "60 of our 165 runs" | 165 unique runs of 200+ games (deduplicated by hash, joined runs counted once), 60 with four players, 71,050 games | all results/*.csv |
| §IV-A | 47.2–52.6% (3p), 36.5–44.4% (4p) | yes | as above |
| | seed block 7: 55.0%; re-run 58.1% | 55.0 [50.6, 59.3]; 58.1 [53.7, 62.3] | R4/V_3p100, R5/v5_base3p100 |
| | 2.4 ± 6.1 and 5.5 ± 6.1 pp more (two-sample) | −2.37 ± 6.14; −5.50 ± 6.09 | same vs r2_val_3p100 |
| | Nigiri 17.6 / 15.2 / 12.9; Sashimi 9.1 vs 3.0 | 17.62 / 15.25 (note: could round to 15.3) / 12.94; 9.08 vs 2.98; ours cross-checked in the CSV (17.62, 2.98) | R5/r2_val_3p100.log |
| | Pudding bonuses (ours higher) | ours 2.88, v2 0.49, ISMCTS 2.50 points per game | same |
| | DUCT 21.0% [17.7, 24.8] of its seats; v2 21.0% | 21.0 (500 seats; Wilson 17.7–24.8); v2 21.0 | R5/r2_duct_3p100.log, r2_val_3p100.log |
| | BasicMCTS/RHEA/OSLA at most 12.8% | BasicMCTS 12.8 and 9.0, RHEA 6.8, OSLA 0.0 | R4/val16.log |
| §IV-B | −H: 28.3 ± 5.8 | −28.27 ± 5.79 (z −9.57) | R4/A_noH |
| | z for −R 2.0, −B 1.4, −I 1.3 | 2.04, 1.43, 1.30 | R4/A_noR, A_noB, A_noI |
| | lead heuristic: 13.6 ± 5.9 (z 4.5); base 58.1% | −13.60 ± 5.88 (z −4.54) | R5/r2_lead_3p100 vs v5_base3p100 |
| | "roughly half of the 28.3 pp" | derived: 28.3 − 13.6 = 14.7 (52%). Note: the direct same-deal comparison of lead heuristic against own-score/50 is +17.8 ± 5.6 pp, i.e. 63% (across machines) | R5/r2_lead vs R4/A_noH |
| | S + I: 10.2 ± 5.7 (z 3.5); held-out +6.1 ± 5.9; pooled +8.2 ± 4.1, z 3.9 | 10.17 ± 5.75 (3.47); 6.10 ± 5.87 (2.04); 8.18 ± 4.11 (3.90) | R4/A_noSI vs V_3p100; R5/r2_v3_3p100 vs r2_val_3p100 |
| | S alone +6.4 ± 5.7 (z 2.2); development +1.3 ± 6.1 | 6.40 ± 5.70 (2.20); 1.30 ± 6.14. Note: a replicate `d1_S2` gives +1.40 ± 6.16 and is not mentioned (it changes nothing) | R4/A_noI vs A_noSI; d1_S vs abl_T4 |
| | I adds 3.8 ± 5.7 | 3.77 ± 5.68 | R4/V_3p100 vs A_noI |
| | factorial: R 8.7, M 4.1, "each ±2.1" | 8.65 ± 2.10 and 4.10 ± 2.11 treating cells as independent (the v3 RESULTS paired analysis gives M ±2.0). Note: the R×T (−4.2, z −4.0) and M×K (+3.2, z 2.8) interactions are significant and not mentioned | R3/s2_*, d_*, s3_*, s4_*, abl_* |
| §IV-B, later changes | −1.8 to +0.7 pp, each ±5.8; R3V2 23.7 ± 5.8 (z 8.0) | X3 −1.30, N05 −1.80, V2D −0.60, W1 +0.70 (CIs 5.78–5.84); −23.70 ± 5.81 (z −8.00) | R5/v5_* vs v5_base3p100 |
| | X3 greedy 45.0% against two X0 | 45.00% ± 1.06 | R5/greedy_duels.txt |
| §IV-C | +5.6 ± 6.1 → +14.8 ± 8.4 (by +9.2 ± 10.4, z 1.7); v3 −17.3, final agent −8.1 | 5.60 ± 6.13; 14.80 ± 8.42; 9.20 ± 10.42 (1.73); −17.30; −8.10 | R4/abl_T4, d1_I, f9_finalT4, f9_I |
| | 11,000–12,000 iterations at 500 ms | 11,170 (v3), 11,865 (v4) | R5/probe_iterations.txt |
| | v3's 50.1% best of 16 cells; 45.1–49.4% on new seeds | best cell abl_T4 50.1; tb2_T4 45.1, tb1_T4 47.5, rep_T4 49.4 | R3 |
| | held-out: +6.1 ± 5.9 (z 2.0); v3 46.5% | 6.10 ± 5.87; 46.5 [42.2, 50.9] | R5 |
| | −5.4 ± 5.9 (z 1.8); seed 7 −5.6 ± 5.9; 900 ms +1.6 ± 8.7 (250 deals); pooled −5.5 ± 4.2 (z 2.6) | −5.40 ± 5.93 (1.78); −5.57 ± 5.92; +1.60 ± 8.69 (n = 250); −5.48 ± 4.19 (2.56) | R5/r2_val_3p100/500; R4/V_3p100/500/900 |
| | ISMCTS 26.4% → 35.9%; v2 21.0% → 16.9% | 26.4 → 35.90 (weighted over chunks); 21.0 → 16.90. Two-sample approximation: ISMCTS +9.5 ± 5.7 pp (z 3.3), v2 −4.1 ± 4.9 | R5 logs |
| §IV-D | X0 +4.3 ± 4.3 at 100 ms | +4.27 ± 4.32, **z = 1.94** (note: "no change gave a significant gain" is true, but only just) | R4/q4_I, q4_Io vs q4_X0, q4_X0c |
| | X0 + W2 + K1 +5.8 ± 7.4; F + L +1.0 ± 7.9; −6.6 ± 6.0 at 100 ms | 5.80 ± 7.39; 1.00 ± 7.86; −6.57 ± 5.96 | R4/val_q5, q5_W2, q5_W2r, q5_FL8, q4_X0c, q4_X0FL8 |
| | 3p cost 7.7 ± 5.4 (z 2.8); interaction 13.5 ± 9.1 (z 2.9) | −7.70 ± 5.39 (2.80); 13.50 ± 9.14 (2.89) | R4/f5_FL8 vs f5_W2FL8 |
| | depth 1 −17.4 ± 8.2; 1000 ms −4.8 ± 8.1 | −17.40 ± 8.19; −4.80 ± 8.09 | R4/q5_FL8d1, q5_FL8t1000K14 |
| | −5.4 ± 5.9 (seed 7), −7.9 ± 5.7 (held-out), pooled −6.7 ± 4.1 (z 3.2) | −5.40 ± 5.91; −7.90 ± 5.74; −6.69 ± 4.11 (3.19) | R4/V_4p*, R5/r2_val_4p* |
| | ISMCTS 24.1% → 32.1% | 24.1 → 32.09 (748 seats each) | R5 logs |
| | 73 positions, 704 root children, 53% visited once; 2 and 4 changed; re-runs 4 and 7 | treestats4: 73, 704, 53%; agree4: 97.3% and 94.5% same → 2 and 4; 94.5% → 4; 90.4% → 7 | R5/treestats4.txt, agree4.txt |
| §V | 13.6; 8.2; 5.5 ± 4.2 (z 2.6); 7.7 ± 5.4; 6.7 ± 4.1 | as above | – |
| Fig. 2 | 11 effect estimates (win, margin) and which markers are filled | all match; Holm fill pattern reproduced: top group all margins filled, wins filled only for H and S+I; bottom group lead heuristic and R3V2 filled on both measures | as above |
| Fig. 3 | v2 24.8/18.3/18.8; v3 50.1/40.7/32.8; final 55.7/49.7/47.6; held-out 52.6/47.2 and v3 46.5 | identical | R4, R5 |

**Values that can only be traced to READMEs or scripts, not result files** (plausible, consistent and not numeric results): "18-core laptop (6–16 games in parallel)", "from 19 October", "nominally 45–70 s of CPU time per game" (my arithmetic gives 48 s and 73 s).

---

## 3. Technical, literature and inference checks

### 3a. Method versus code (`TAG/src/main/java/groupA`, identical to `groupA_code/groupA`)

Checks that pass:
- **Lineage:** `SushiMCTSPlayer` and `SushiTreeNode` keep the structure of `BasicMCTSPlayer` and `BasicTreeNode`. Nothing is imported from `players.mcts`; the only `players.*` imports are `PlayerConstants` and `PlayerParameters`.
- **Eqs. (1)–(3):** match `CardValueModel.estimatedScore` and `SushiGoHeuristic.valuesOf`.
  - ω_t = (round+1)/3.
  - The completion weight is min(1, (r−k+1)/T).
  - Wasabi is valued at (3−1)·ν; Chopsticks at 1 with k = 2.
  - Maki standing uses TAG's exact tie rules.
  - One detail is not in the paper: the marginal value uses r−1 picks both before and after the card.
- **B:** ε-greedy with ε = 0.2 for every player, Chopsticks handled as described.
- **R:** `finishRollout` with `rolloutRounds = 1`.
- **P:** keys are (type, Maki icons, Chopsticks).
- **M:** per-player value vectors.
- **S:** decoupled, untried cards first.
- **I:** `selectInformationSet` uses mean + K·√(ln availability / visits), which is exactly Eq. (4). Each iteration re-samples with `state.copy(me)`, adds one node, and uses an open-loop tree.
- **D:** an ensemble of up to 4 trees (`nDeterminisations` 4, adaptive).
- **W2:** `terminalScale` 2, `sigmaRoundDecay` 0.5, `opponentScoreValue` with rival weight 1, w = 1.
- **F:** `SushiSim`, plus the `SushiSimCheck` development tool.
- **L = 8, K = 1:** as stated.
- **X0 constants:** match the footnote of Table I.
- **Competition files:** 900/925 ms and 430/450 ms.
- **Timing:** TAG's timeout check uses a CPU timer (`ElapsedCpuChessTimer`), as the README says.

Issues:
1. `json/agents_v4/SushiMCTS_900ms.json` leaves F and L out of its `fourPlayer` block. Table I footnote b says F and L are used at "500 ms or more", and the 1000 ms competition file does include them. Minor package inconsistency.
2. The code is hard to read because of the many disabled switches (see the rubric table). The README's first line asks the reader to rename the package to "your real group letter". The agent also depends on `SGGameState.isHandKnown` (TAG from November 2024 on; Lab 4 still calls it `hasSeenHand`), so it will not compile on an older TAG.
3. The comment `merge_parts.py` in experiments_v5 still describes the lost `r2_val_3p900_all` (harmless).

### 3b. Literature claims versus sources

arxiv.org and ludii.games are blocked from this session's network, so I could not read full texts. The checks below rest on abstracts and bibliographic records from web search, on Lab 4, on TAG's source, and on my own knowledge where stated.

| Ref | Claim in the report | Check | Verdict |
|---|---|---|---|
| [4] Klang et al., CoG 2021, pp. 1–4 | implemented Sushi Go! in TAG; studied simultaneous action selection and complete information; "concluded that better heuristics should improve the agents" | Abstract: "agents achieved mixed results … better agent heuristics would likely improve performance". Lab 4: that implementation let players "see" all cards; pages 01–04 | Accurate. The report should say the cards were visible (strengthens the gap). |
| [10] Lanctot, Lisý, Winands, CGW 2013, CCIS 408, pp. 28–43 | DUCT compared with EXP3, regret matching and OOS in Goofspiel; RM and OOS best; UCT strategies most exploitable | Abstract: "regret matching and OOS perform best and … all variants produce less exploitable strategies than UCT". EXP3 is included from memory. | Accurate |
| [11] Tak, Lanctot, Winands, CIG 2014, pp. 232–239 | nine games; best variant depends on game and opponent; DUCT best overall | Abstract and record match exactly ("Overall, Decoupled UCT performs best"), pp. 232–239 | Accurate |
| [12] Sturtevant, CG 2008, LNCS 5131 | UCT with max-n computes a mixed-strategy equilibrium; at least as good as earlier algorithms | Abstract matches. Pages 37–49 from memory (not confirmed online). | Accurate |
| [13] Robilliard et al., CGW 2014, CCIS 504 | known results hold in 7 Wonders; card values depend on stage of game and others' choices | Abstract: "card values are heavily dependent upon the stage of the game and upon the other player decisions … a lot of known results still hold" | Accurate |
| [7] Long et al., AAAI 2010, pp. 134–140 | PIMC success related to how fast hidden information is revealed | Pages confirmed. The disambiguation-factor property is from memory. | Accurate |
| [9] Goodman et al., CoG 2022, pp. 292–299 | MultiTree MCTS = one tree per player, offered by TAG | Bibliographic record confirms the pages. TAG's code (`OpponentTreePolicy.MultiTree`, `selfOnlyTree`) confirms one tree per player. I could not read the abstract. | Consistent |
| [3] Cowling et al. 2012; [8] Frank & Basin 1998; [6] Silver & Tesauro 2009; [1], [2], [5], [14], [15] | single- vs multiple-observer ISMCTS; strategy fusion and non-locality; balanced rollouts; standard facts | From memory, all standard and correct. "Silver and Tesauro [6] showed that rollouts should be balanced" is slightly strong: they proposed balance as the objective and showed it empirically. | Accurate |

I found no misreporting of sources.

### 3c. Do the conclusions follow from the statistics next to them?

1. **The seed-block-7 "overlap" claim is wrong (must fix).**
   - The report says: "On seed block 7, whose deals overlap the development deals, it had won 55.0% … neither is significant, but both have the sign a small selection bias from the overlap would produce."
   - Table II itself says that runs before 19 October "used TAG's own deal (independent samples)". All the three-player design decisions were made on such runs: the development runs for S and I (`d1_I`, `f5_I`, `f9_I`, `d1_S`, all in the pre-canonical `OLD` list of `make_v4_tables.py`), and the K = 0.5 tuning in the v3 study.
   - So the overlap is in seed numbers, not deals, and the three-player agent was never selected on seed-7 deals. The `experiments_v4` README says so correctly: "validated on seed 7, which no decision used".
   - The argument does hold for four players. X0, W2 and F + L were chosen on canonical seed-1 and seed-2 deals, which overlap seed block 7 (`q4_X0c`, `q5_W2`, `q5_W2r`, `q5_FL8`). There the held-out results are also lower: 44.4 against 46.8 and 36.5 against 41.4.
2. **The DUCT comparison claims more than it shows.**
   - The report says: "without our card-value model and round-end rollouts these techniques gave no stronger agent than v2".
   - TAG's DUCT also differs in its evaluation (the lead heuristic, ×1.5 at the end of the game), its rollouts (30 random moves), K (1 rather than 0.5), the absence of action pruning, and its implementation. It also played in a different field (DUCT + ISMCTS rather than v2 + ISMCTS).
   - One comparison of seat shares cannot attribute the difference to two named factors.
3. **The conclusion for RQ1 mixes up results.**
   - The report says: "after a Holm correction only removing the evaluation H (13.6 pp over TAG's lead heuristic) or the decoupled information-set tree (8.2 pp over v3's sequential tree, two deal sets pooled) also lowers the win rate".
   - The test that survived Holm for H was against own-score/50 (−28.3). For S + I the corrected test was the seed-7 one (−10.2). The 13.6 and 8.2 figures come from different tests or families.
4. **"500 instead of 100 ms helped ISMCTS more than our agent."**
   - Every agent's budget changed together, so only relative strength is measured; the evidence is the ISMCTS seat share, +9.5 pp (z ≈ 3.3), against our −5.5 pp.
   - The wording "helped … our agent" implies an absolute gain for our agent that was never measured. Better: "favoured ISMCTS over our agent". The conditional logic behind this sentence is also broken (§3d).
5. **"roughly half of the 28.3 pp comes from valuing the lead"** is a difference of differences across two base runs on two machines. The direct same-deal test (`r2_lead` against `A_noH`) gives +17.8 ± 5.6 pp, about 60%. Quote that instead.
6. **The factorial** reports main effects only, while R×T (z −4.0) and M×K (z 2.8) are significant. The main effects average over strong interactions; one clause would say so.
7. The other hedged claims are fine: "consistent with Long et al." from a non-significant +3.8 pp; "consistent with E3" (the report discloses that only the three-player arms had F + L); "1 000 ms … (with K = 1.37)". The choice of K = 1.37 is never explained, and it confounds the comparison.
8. The power and multiplicity handling is generally good: Holm for the ablation and candidates, "unadjusted" flagged, four players called exploratory, E2 tested on held-out deals, and the selection effect on v3 at 100 ms disclosed.

### 3d. The pending-result branches (synthetic outcomes, rebuilt on a copy under `/tmp/claude-0/review8/sandbox`)

I tested eight states. For each I printed the abstract, the final-agent paragraph, the budget paragraph, the Fig. 3 caption and the RQ2 conclusion:
- now;
- A: 900 ms result 50%, ISMCTS seat share 37%, v3 at 500 ms 42%;
- B: 900 ms result 40% (z −4.0), ISMCTS share falls to 25% below its 100 ms share, v3 at 500 ms 30% so E2 is supported (z 2.5);
- C: 900 ms result 56%, ISMCTS share 30%, v3 at 500 ms 52% so E2 is contradicted (z −2.4);
- D1: only the 900 ms run finished;
- D2: only v3 at 500 ms finished;
- E: no change at 900 ms;
- F: our agent +2.0 pp at 900 ms, ISMCTS +1.0 pp.

Every sentence came out grammatical, and the E2 verdicts ("supports" / "contradicts" / "not significant either way"; "grew … as E2 predicted" / "shrank … contrary to E2" / "neither confirms nor refutes E2") are correct. Five defects remain:

1. **"The extra time helped TAG's ISMCTS more than our agent" can be false, or can vanish (must fix).**
   - The condition is `more = all(ISM_share(b) − ISM_share(100) > our Δ(b))` over 500 and 900 ms.
   - **Scenario B** prints the sentence although ISMCTS fell from 26.4% to 25.0% at 900 ms: −1.4 > −12.6 makes the condition true.
   - **Scenario F** drops the sentence and the alternative explanation entirely, although the 500-ms evidence is unchanged and the abstract still says it.
   - **Scenarios C and E** assert it from noise: 3.6 against 3.4 pp, and 0.1 against 0.0 pp.
   - The test also compares a seat share with a win rate and carries no uncertainty.
   - `_ism_gained()` has the same coupling: in scenario B the conclusion silently drops "while TAG's ISMCTS gained seats" while the abstract keeps "helped ISMCTS".
2. **The 900 ms change in the conclusion uses a different basis.** "won 5.5 ± 4.2 pp less at 500 than at 100 ms (z = 2.6; two deal sets pooled) and 2.6 ± 6.3 pp less at 900 ms (z = 0.8)": the 900 ms figure is from the held-out deals only and is not pooled with the seed-7 900 ms result (+1.6 ± 8.7), whereas the 500 ms figure is. The results text has the same asymmetry ("pooled with seed block 7 (… +1.6 ± 8.7 pp at 900 ms …), 500 ms changed it by …").
3. **The abstract never reports the E2 test.** `_abstract_end()` depends only on `POOL500`, so the abstract stays the same whether E2 is confirmed (B) or refuted (C), although E2 is the paper's main confirmatory RQ2 test. In scenario C the hypothesis sentence ("We hypothesise that v3's decline comes from its sequential tree …") also stays unqualified while the conclusion says "contrary to E2".
4. **There is no fallback if the 900 ms run does not finish.** The abstract would print "and [pending]% of three-player games" and Table III "pending". Nothing in the code handles that case.
5. **Cosmetic:** with Δ = 0, the conclusion prints "0.0 ± 6.3 pp more at 900 ms" (scenario E).

**Length** (full rebuilds, measured on page 4): the main text now ends about 9 lines above the bottom. With the pending results it ends about 4 lines above (scenario A, where the generative-AI heading moves to page 5) or about 7 lines above (scenario B). It stays within 4 pages in both cases, and the whole report stays at 5 pages.

---

## 4. Compliance

| Requirement | Status |
|---|---|
| Template (IEEE Word, two columns) | Yes. The page geometry is the template's (left/right 44.65 pt against the template's 45.35 pt in the two-column section, a LibreOffice re-save artefact and negligible). Fonts and styles are the template's. |
| Main text ≤ 4 pages; generative-AI statement and references allowed onto page 5; total ≤ 5 pages | Yes now, and also with the pending results (§3d). |
| Section word guidance | Method 885 and Results 1,098 exceed it. This is only guidance, but the excess shows up as density. |
| References 6–15, relevant, consistent style | 16, IEEE, accurate. |
| 500 games per experiment unless justified | Yes. The 250-game runs (3p/900 ms, 4p/500 ms development) are justified by CPU cost (Table II). |
| ≥ some experiments at 500 ms | Yes (500 and 900 ms; 4p/500 ms validated on 500 games). |
| Generative AI: acknowledged, cited, prompts included | **Partly.** The first-page footnote and section cite Claude [16] and give the main report prompt verbatim with a translation. However, the statement says "Its written reports, one per draft, were the prompts for the revisions (all prompts: GENAI_PROMPTS.md in our code package)". `GENAI_PROMPTS.md` does not contain those reports; it says they "are kept with our experiment records", which are not part of the submission zip. Also, its "reviewer prompt" is an abridged paraphrase (I can compare it with the prompt for this review, which also lists files to read, statistical methods, pending branches, length limits and the output path), but it is presented as "the following". Since the brief requires the prompts used to generate text to be included, this needs fixing. |
| Agent based on BasicMCTS, no use of `players.mcts.MCTSPlayer` | Yes (§3a). |
| Package `group{LETTER}` with README (constructor, parameters) and competition JSON | Yes for `groupA`, provided the group really is A: the README's "Rename first … with your real group letter" must be resolved and removed. |
| Authorship | **Not submittable:** "[Group member 2]", "[Group member 3]", and "Student no. [to be completed]" ×3. |

---

## 5. Presentation

- **Internal codes.** H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D are defined in Table I. They are then used in prose such as "X0, W2 and K = 1 cost 7.7 ± 5.4 pp … only the three-player arms had F + L", which an outsider cannot follow without going back to the table. "switch Q" (§V) is never defined.
- **Notation clashes:**
  - n means players ("12 − n cards to each of n players"), visits (UCB1 "n its visits"), and games (Table III column "n").
  - s means both the state and the seed ("seed s + g").
  - Q means both the mean value (Eq. 4) and "switch Q".
  - The switch S sits next to Ŝ_p; j is both the tie count ("1/j") and the index in Eq. (3).
- **Unclear phrases:**
  - "(−5.6 ± 5.9 pp at 500 ms, as in development, …)": what is "as in development"?
  - "four-player settings hurt with three".
  - "changed our win rate against the 100 ms agent" reads as a head-to-head result; it is a comparison between fields.
  - "the three-player arms".
  - "v2 chases Sashimi" is informal.
- **Number density.** For example, the lead-heuristic sentence runs to 70 words with four numbers and three parentheses, and the four-player paragraph has 16 numbers. A 90+ write-up would put most per-variant numbers in Fig. 2 or a table and use prose for the argument.
- **Figures.** They are of high quality. Fig. 3's held-out markers are offset to the right without explanation, and its x-axis is categorical (100, 500, 900 are equally spaced); one caption clause would fix both. The Holm family for Fig. 2's lower panel (six tests, including the lead heuristic) differs from the text's "five later candidate changes … the only change that survives a Holm correction"; the result is the same, but the families should match.

---

## 6. Prioritised changes

Line counts are for one 10-pt column of about 57 characters. A negative count frees space.

### MUST (before submission)

**M1. Authors and group letter.** Fill in the names and student numbers. Confirm the group letter. If it is A, delete the README's "Rename first" block; if not, rename the package, the JSON `class` entries and the report file name. (0 lines.)

**M2. No "pending" in the PDF.** Finish both runs, or add a fallback in `build_report.py`. (0 lines.)

```python
def three_player_wins():
    if VAL["3p900"]:
        return f"{v('3p100')}%, {v('3p500')}% and {v('3p900')}% of three-player games at 100, 500 and 900 ms"
    return (f"{v('3p100')}% and {v('3p500')}% of three-player games at 100 and 500 ms "
            f"({OLD['3p900']['win']:.1f}% of 250 games at 900 ms on seed block 7)")
```

For Table III, if the run is missing, replace the 3p/900 row with the seed-7 row: `["3p/900", "v2, ISMCTS", "250", fmt_run(OLD['3p900']), fmt_margin(OLD['3p900'])]`, marked ᵃ.

**M3. Rewrite the ISMCTS sentence so it never depends on the 900 ms noise and never claims "helped" when the share fell.** In `text_budget()`, replace the `hs`/`more` block with this (about the same length; −0.2 lines now):

```python
i1, i5, i9 = (SEATS[b].get("tagmcts.json") for b in (100, 500, 900))
r1, r5, r9 = (SEATS[b].get("ref.json") for b in (100, 500, 900))
se = _math.sqrt(i1 * (100 - i1) / 500 + i5 * (100 - i5) / 500)
s += (f" On the held-out deals ISMCTS won {i1:.1f}% of its seats at 100 ms and {i5:.1f}% at 500 ms "
      f"({i5 - i1:+.1f} ± {1.96 * se:.1f} pp), v2 {r1:.1f}% and {r5:.1f}%"
      + (f" (900 ms: ISMCTS {i9:.1f}%, v2 {r9:.1f}%)" if i9 is not None else "")
      + (": at 500 ms the extra time favoured ISMCTS over our agent, or our parameters, tuned at 100 ms, "
         "suit longer budgets less." if i5 - i1 > 1.96 * se else "."))
```

With today's data this prints: *"On the held-out deals ISMCTS won 26.4% of its seats at 100 ms and 35.9% at 500 ms (+9.5 ± 5.7 pp), v2 21.0% and 16.9%: at 500 ms the extra time favoured ISMCTS over our agent, or our parameters, tuned at 100 ms, suit longer budgets less."*

In `text_conclusion_1()`, replace `", while TAG's ISMCTS gained seats;" if _ism_gained()` with a test on the 500 ms shares only: `", while TAG's ISMCTS won 9.5 pp more of its seats at 500 ms;"`.

**M4. Correct the seed-block-7 sentence** (−1.8 lines). Replace "On seed block 7, whose deals overlap the development deals, it had won 55.0% at 100 ms (58.1% re-run on the held-out runs' machine): 2.4 ± 6.1 pp and 5.5 ± 6.1 pp more; neither is significant, but both have the sign a small selection bias from the overlap would produce." with:

> "On seed block 7 it won 55.0% at 100 ms (58.1% when re-run on the held-out runs' machine), 2.4 ± 6.1 and 5.5 ± 6.1 pp more than on the held-out deals, within chance."

Also correct the same claim at the top of `groupA/README.md` ("these deals overlap the development deals").

**M5. Make the generative-AI statement match the package** (+0.3 lines, on page 5, which has space).
- Add the reviewers' reports and the verbatim reviewer prompts to the zip, e.g. `groupA/genai/review1.md … review8.md` and `groupA/genai/prompts/`.
- Replace "(all prompts: GENAI_PROMPTS.md in our code package)" with "(GENAI_PROMPTS.md and the folder genai/ in our code package hold every prompt, including these reports)".
- In `GENAI_PROMPTS.md`, change "Its prompt … was the following" to "Its prompt followed this outline (abridged; the verbatim prompt of each draft is in genai/prompts/)".

**M6. Conclusion, RQ1** (+0.5 lines). Replace the sentence quoted in §3c-3 with:

> "For RQ1, every ablation lowers the score margin; after a Holm correction, removing the evaluation H (−28.3 pp; TAG's lead heuristic in its place still costs 13.6 pp) or the decoupled information-set tree (−10.2 pp; +8.2 pp for it when pooled with the held-out replication) also lowers the win rate."

**M7. "switch Q"** (−0.1 lines). Replace "(switch Q implements one, not yet evaluated in full runs)" with "(an option in our code, not yet evaluated in full runs)".

**M8. Branch fixes from §3d** (+0.3 to +1 line once the 900 ms run exists).
- In the conclusion, write "and, on the held-out deals, X ± Y pp less at 900 ms".
- Pool the two 900 ms results as you do for 500 ms: `POOL900 = _pool(SEED7_900, BUD["3p900"])`, then "…, 500 ms changed it by −5.5 ± 4.2 pp (z = 2.6) and 900 ms by …".
- Print "changed by ±0.0 pp" rather than "0.0 pp more".
- In the z ≤ −1.96 branch of `text_budget()`, add "so the held-out test does not support the sequential-tree explanation".

### SHOULD

**S1. Hedge the DUCT conclusion** (≈0 lines). Replace the DUCT sentence with:

> "TAG's own DUCT configuration (decoupled selection and information sets, but its lead heuristic, K = 1 and 30-move random rollouts) wins 21.0% [17.7, 24.8] of its seats in place of v2, the share v2 wins: decoupling and information sets alone, untuned, did not beat our earlier agent."

**S2. Put the E2 outcome in the abstract** and remove the unclear phrases. The new abstract is ≤125 words in every branch (121–124 words):

> "In Sushi Go!, players pick cards simultaneously from hidden, rotating hands, which basic Monte Carlo tree search (MCTS) ignores. We extended TAG's BasicMCTS with a card-value model for rollouts and a lead-based evaluation, round-end rollouts, max-n backups and a decoupled information-set tree. On held-out deals against TAG's information-set MCTS (ISMCTS) and our previous agent it wins 52.6%, 47.2% and X% of three-player games at 100, 500 and 900 ms and 44.4% and 36.5% of four-player games at 100 and 500 ms (chance 33.3% and 25%). Every ablated component raises the score margin significantly, the evaluation and decoupled tree also the win rate. Longer budgets favoured ISMCTS over our agent; {time increased / time reduced / time did not detectably change} the decoupled tree's gain; four-player settings hurt three-player play (exploratory)."

Generate the E2 clause from `RISE["zw"]`. Write "favoured ISMCTS" only if ISMCTS's seat share at 500 ms exceeds its share at 100 ms (M3). (About 0 lines on page 1.)

**S3. Remove "as in development"** from the pooled-500 ms sentence (−0.3 lines).

**S4. Lead heuristic: quote the direct test** (−1 line). Replace "costs 13.6 ± 5.9 pp (z = 4.5) (base: the re-run, 58.1%), so roughly half of the 28.3 pp comes from valuing the lead on a suitable scale" with:

> "costs 13.6 ± 5.9 pp (z = 4.5) against the re-run, and in place of own score / 50 it recovers 17.8 ± 5.6 pp of the 28.3 pp (same deals, run on the second machine): most of H's effect comes from valuing the lead on a suitable scale".

**S5. Shorten the later-candidates paragraph** (−3 lines; the definitions stay). Replace it with:

> "Five later changes (Fig. 2, bottom) did not help: card constants tuned for three players (X3), denial (rollout players also value taking the next player's favourite card), a win-gap value with σ decay and w = 1 changed the win rate by −1.8 to +0.7 pp (each ±5.8), although X3 is the much stronger greedy policy (45.0% against two X0 players; it also changes the leaf evaluation, so this only weakly echoes simulation balancing [6]); rollouts to the game end cost 23.7 ± 5.8 pp (z = 8.0)."

State the Holm family once, in the Fig. 2 caption ("Holm within each panel").

**S6. Say what Klang et al. evaluated** (+0.2 lines). Replace "implemented the game in TAG to study its simultaneous action selection" with "implemented the game in TAG and studied its simultaneous action selection in a version in which all cards were visible".

**S7. Factorial interactions** (+0.5 lines). Append "; R × T and M × K also interact (z = −4.0, 2.8)".

**S8. Four-player development** (0 lines). Write "X0: +4.3 ± 4.3 pp (z = 1.9) at 100 ms", so readers see it is borderline. Explain or drop "with K = 1.37".

**S9. Readability pass in §IV–V** (net 0 lines with S5). On first use in each Results subsection, name the component with its letter: "the decoupled tree (S)". Turn the parenthetical number chains into short sentences. Rename the notation that clashes: number of players → *P* or "players"; seed → *b*; drop "switch Q".

**S10. Literature** (+1.5 lines; pay for it with S5). Add one sentence with one or two references on AI for drafting games (e.g. *Magic: The Gathering* drafting at CoG 2021) or on Sushi Go! in TAG for reinforcement learning (PyTAG, 2024), contrasted with this work.

### NICE

- **N1.** Code clarity: move switches that are off and not used in the paper (G, U, K, E, Q, O, A, `maxChildShare`, `ownCardTree`, …) into a separate experimental class or delete them; cut the README to what a user needs; add F and L to the `fourPlayer` block of `agents_v4/SushiMCTS_900ms.json`; note the `isHandKnown` TAG-version requirement in one line.
- **N2.** Fig. 3 caption: "held-out markers are offset to the right; budgets are equally spaced".
- **N3.** Mention the replicate `d1_S2` (+1.4 ± 6.2) next to "development: +1.3 ± 6.1 pp", or pool the two.
- **N4.** Table II, "Hardware, records" row: drop "records from the authors" (−1 table line).

**Length balance.** There are about 9 lines free now and the pending results take 2–5, leaving 4–7. The must-fixes are roughly length-neutral (−1.8 + 0.3 + 0.5 − 0.1 + up to 1). With the S3–S5 cuts (about −4.3) there is room for S6, S7 and S10 (about +2.2) and a margin of 3 or more lines. The cuts I would make: M4 (−1.8), S5 (−3), S4 (−1), S3 (−0.3), and N4 if needed.

---

## 7. Plain answer

**Would this get 85% or more?** Not as it stands. I would mark it at about **82%**, in a range of 79–86 depending on how much a marker rewards the very large experimental programme against the density of the prose. If M1–M8 are done, it becomes a solid **84–86**.

**What stands between it and 90% or more:**
1. **Clarity and flow.** This weighs most, because it affects General, Abstract, Conclusions and Critical analysis. The Results section is almost twice the guided length and reads as a stream of numbers and letter codes. A 90+ paper makes three or four points clearly, with the evidence in Fig. 2, Fig. 3 and Table III:
   - decoupling and information sets matter because moves are simultaneous and hands are hidden;
   - the card-value evaluation is the largest single factor;
   - longer budgets favour ISMCTS (and E2's held-out verdict);
   - four players need different settings because the joint-move tree is wide and shallow.
2. **Correctness of the reasoning.** Fix the seed-block-7 overlap argument, the attribution in the DUCT comparison, the RQ1 conclusion that mixes Holm-corrected and pooled figures, and the conditional ISMCTS and 900 ms wording that can print false statements for some outcomes.
3. **Completeness and compliance.** No "[pending]"; real author names and student numbers; a generative-AI statement whose cited prompt files actually contain the prompts (the reviewer reports) and that gives the reviewer prompt verbatim or labels it abridged.
4. **A little more critical depth in the literature,** mainly drafting-game AI and the fact that Klang et al.'s agents saw all cards. Pay for it with the S5 cut.

The experimental design, the statistics and the agent itself are already at the 90+ level. The write-up is not.
