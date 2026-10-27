<!-- Independent review of report draft 2 (27 October 2026), written by a separate Claude instance against the
     module rubric. Kept as the record of the revision prompts mentioned in the paper's generative-AI statement. -->

# Independent marking review: ECS7032P Assignment 1, Group A

**Report:** "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!"
(`report/ECS7032P_Assignment1_Report_GroupA.pdf` and `.docx`, built 27 Oct 2026, 20:50)

**What I examined**
- the brief, rubric, weights, writing tips and template;
- all 5 pages, rendered at 110 and 300 dpi;
- the `groupA` package: every Java file, the README and all JSON files. I compiled it stand-alone against TAG and ran a smoke test.
- `experiments_v3/v4/v5`: raw CSV, log and txt files;
- TAG's Sushi Go! rules, agents and configurations;
- `build_report.py` and `make_figures.py`.

Nothing under `/home/user/statistics` was modified. My scripts are in `/tmp/claude-0/review2/`: `verify.py`, `verify2.py` to `verify6.py`, `wc.py`, `fonts.py`, `tmpl.py`, plus `jbuild/` and `smoke/`. As instructed, the "pending" cells were not penalised.

---

## 0. Bottom line

- **Estimated mark as it stands: about 74% (plausible range 70–78%).** This is a strong, unusually rigorous student paper. It would not receive 85% as it stands.
- **What costs most marks:**
  1. The writing is very dense and full of codes. Results runs to 1,057 words against a 500–700 guide, with about 15 internal labels.
  2. Several conclusions go further than the statistics allow. Some rest on comparisons chosen from favourable replicates or comparators. Unreported runs in the repository contradict one narrative (performance falling with the time budget).
  3. There is no held-out evidence of at least 500 games at the competition settings (3 players at 1 s, 4 players at 500 ms). No ablation was run above 100 ms.
  4. The literature review is correct but short and descriptive. It misses work directly relevant to card drafting and to Sushi Go!.
  5. The four-player card model cannot be reproduced from the paper.
  6. There are smaller template and presentation faults.
- **Numbers:** I recomputed about 60 numbers from the raw files. Every table, every figure and each Results subsection is covered. All but one reproduce exactly:
  - **one numeric mismatch:** X0 at 4 players / 100 ms is +3.1 pp, not +3.2 pp;
  - **one understated range:** the pairing correlation is 0.0–0.22, not 0.0–0.15;
  - several numbers are arithmetically correct but chosen selectively (§3.3).
  - No number in the text is untraceable. The only claims without data behind them are qualitative ("about twice as sensitive").
- **Code:** The Method section describes what the code does. I checked H, B, R, P, M, S, I, X0, W2, F and L line by line. Nothing is imported from `players.mcts`. The package compiles against TAG alone. The competition JSON plays 3- and 4-player games without error.

---

## 1. Rubric marks

| Area (weight) | Item | Band | Mark | Main reasons |
|---|---|---|---|---|
| **1 Abstract (5%)** | Motivation, summary, findings | 70–100 (low) | **72** | See below |
| **2 Problem definition (15%)** | Motivation | 70+ | 70 | See below |
| | Problem clarity | 70+ | 76 | See below |
| | Contributions | 50–69 | 66 | See below |
| | Critical analysis of literature | 50–69 | 64 | See below |
| | Description of the game | 70+ | 78 | See below |
| | **Area mark** | | **71** | |
| **3 Approach (30%)** | Understanding and application | 70+ | 78 | See below |
| | Originality | 70 (borderline) | 70 | See below |
| | Difficulty and ambition | 70+ | 82 | See below |
| | **Area mark** | | **77** | |
| **4 Experimentation (30%)** | Adequacy and rigour | 70+ | 78 | See below |
| | Documentation | 70+ | 76 | See below |
| | Critical analysis | 70+ (low) | 72 | See below |
| | **Area mark** | | **75** | |
| **5 Conclusions (10%)** | Summary | 70+ | 74 | See below |
| | Future work | 70+ | 78 | See below |
| | **Area mark** | | **76** | |
| **6 General (10%)** | Clarity | 50–69 / 70 (borderline) | 68 | See below |
| | Figures and tables | 70+ | 72 | See below |
| | Referencing | 70+ | 74 | See below |
| | **Area mark** | | **71** | |

**Abstract (72).**
- The motivation is a single correct clause; there is nothing on why the problem matters beyond Sushi Go!.
- The summary of the work is complete at 122 words.
- Findings:
  - Held-out win rates are given with chance levels.
  - "the tree's advantage tends to grow with the time budget" is presented as a finding although the paper's own test gives z = 1.7.
  - "four players need different settings" overstates one-sided evidence.
  - TAG, UCT, ISMCTS and max-n are not defined in a stand-alone abstract.
  - "In Sushi Go! players …" reads as if the sentence ends at the "!".

**Motivation (70).** Properties (i)–(iv) motivate the methods well. The wider importance is barely argued. "Long-standing test bed" has no citation. No work on similar games, such as drafting games, is cited.

**Problem clarity (76).**
- RQ1–RQ3 are explicit, and hypotheses H1–H3 are stated in III-E.
- One slip: "up to 729 or 4 096 joint moves" ignores Chopsticks. TAG lists every card twice when Chopsticks are on the table (`SGForwardModel._computeAvailableActions`).

**Contributions (66).**
- Bullet 2 is a "re-implementation" of literature techniques, which is engineering rather than a research contribution.
- Bullet 3 rests on one-sided, post-hoc evidence (§3.3).
- Bullet 4 describes the size of the study.
- The introduction never says what knowledge the paper advances.

**Literature (64).**
- The summaries are accurate, with no misrepresentation found (§3.2). The remark that both DUCT studies used two-player games is genuine critique.
- Missing:
  - card drafting with MCTS: Robilliard, Fonlupt and Teytaud, "Monte-Carlo Tree Search for the game of '7 Wonders'", in *Computer Games (CGW 2014)*, Springer, doi 10.1007/978-3-319-14923-3_5; check the volume and pages before citing. That game uses the same pass-the-hand mechanism, and the paper notes that card values depend on the stage of the game and on the other players' picks.
  - Sushi Go!-specific MCTS work after Klang et al.: a Charles University thesis compared DUCT and EXP3 on Sushi Go! and found DUCT best in every experiment (dspace.cuni.cz/handle/20.500.11956/120975).
  - the origin of DUCT (Finnsson and Björnsson) and the result that UCB does not converge in simultaneous games (Shafiei et al. 2009);
  - opponent modelling.
- The gap claim "has not been studied" is therefore too strong.

**Game description (78).** It is concise and every rule matches TAG: 108 cards, all scoring values, 12 − n cards per hand, every hand seen after n − 1 passes, scoring on reveal, and the −6 Pudding penalty only with three or more players.

**Understanding and application (78).**
- The paper explains why each technique fits Sushi Go!, which is what the brief asks for at complexity levels 4–5:
  - hidden hands → ISMCTS;
  - simultaneous picks → DUCT;
  - delayed points → round-end rollouts and the score model;
  - several players → max-n.
- The code matches the description (§3.1).
- Deductions:
  - The card-value model is only partly specified, and Table I note a is cryptic.
  - `SushiTreeNode.java` is 1,753 lines with about 15 switches, most unused by the final agent (A, O, G, U, Q, E, K, N, V and others). This hurts code clarity.
  - Some comments are stale, for example "Only `_getAction()` is changed" in `SushiMCTSPlayer`.

**Originality (70).** The paper itself says the search techniques come from the literature. What is new is the shared card-value model, its integration, the four-player settings and the fast simulator. Nothing goes beyond the published methods.

**Difficulty (82).** Single-observer ISMCTS, DUCT, max-n and an exact simulator were re-implemented on top of BasicMCTS, and the agent was tested on 162 runs.

**A risk the rubric does not price.** The generative-AI statement says Claude wrote all the code and drafted all the text, and quotes the prompt "It should be a report that gets 100". This complies with the brief. But a marker may then look harder at understanding and originality, and at the individual reflections.

**Adequacy and rigour (78).**
- The agent is compared with:
  - BasicMCTS (84–87% win rate);
  - the previous version (v2);
  - four framework agents: ISMCTS, DUCT, RHEA and OSLA.
- There is a leave-one-out ablation plus a 2^4 factorial, and a held-out validation.
- Deductions:
  - Every ablation is at 100 ms.
  - The final agent at 500/900 ms has only 250-game held-out runs, still pending. Seed-7 runs at those budgets exist but are not reported.
  - RQ2 rests on development runs that are independent samples, with 250 games at 900 ms. RQ3 rests on 250-game runs.
  - The 300-game run against two ISMCTS agents is at 100 ms, where games are cheap, and is not justified.
  - The rubric deems fewer than 500 games insufficient.

**Documentation (76).**
- Table II is unusually complete, and I checked every opponent configuration against its JSON file.
- Missing:
  - the meaning and defaults of the card-model constants;
  - the submitted competition files: 900 and 430 ms of CPU time with 925 and 450 ms wall-clock guards;
  - JVM and garbage-collection settings, which mattered for v3 at long budgets.
- The "Records" row points to `experiments_v3`–`_v5`. Those folders are outside the package that will be submitted (their own READMEs say so), so a marker cannot reach them.

**Critical analysis (72).**
- Strengths: good error analysis (Wilson intervals, paired standard errors, z values, Holm correction for the ablation), hypotheses that are actually tested, honest negative results and diagnostics of the four-player tree.
- Deductions:
  - The 900 ms validation run that contradicts the decline narrative is omitted.
  - "S carries most of the budget dependence" rests on z = 1.1.
  - H3 is one-sided and confounded.
  - The decomposition of H's effect is never tested.
  - "No sign of overfitting" uses the more favourable comparator.

**Conclusions: summary (74).** RQ1–RQ3 are answered with numbers. RQ2 is correctly hedged; RQ3 is overstated. The limitations are specific and honest.

**Conclusions: future work (78).** There are three items, each justified by a result. The EXP3 item ignores the Sushi Go!-specific evidence that DUCT beat EXP3.

**Clarity (68).**
- The writing is precise but overloaded:
  - about 15 codes (H, B, R, P, M, S, I, X0, W2, F, L, A, D, O, v2–v4);
  - many sentences carry 4–6 intervals each;
  - Results tells the development history before the main result.
- "(mostly)" is informal.
- "the assignment does not allow it as a basis" mentions the assignment, which the writing tips advise against.

**Figures and tables (72).**
- Fig. 3 is a good paired-effects plot, and Fig. 2 is informative.
- Fig. 1 is a generic MCTS loop with letters added. It does not show the core idea, the decoupled information-set turn node.
- Text inside the figures is 6.1–7.5 pt.
- Table II is never referred to in the text.
- Table III is titled "held-out" although three of its rows use seed 7.
- Table I note a is unintelligible ("3 cards per missing card left in the hands"), and one cell leaves "(1)" alone on a line.

**Referencing (74).** There are 15 references (the maximum) in consistent IEEE style, with details correct as far as I checked. The key omissions are those listed under Literature. RHEA, UCB-Tuned and the Holm correction are used without citation.

**Weighted total.**

0.05·72 + 0.15·71 + 0.30·77 + 0.30·75 + 0.10·76 + 0.10·71 = **74.6, about 74–75%**.

The range of 70–78 reflects marker variance: Clarity and Literature are borderline, and the AI-authorship perception could lower Approach.

---

## 2. Number verification (recomputed from raw files)

**Method.**
- I wrote my own functions: Wilson interval; paired difference with the per-game standard error; two-sample difference with the binomial standard error p(1−p)/n.
- A win counts 1/k for a shared first place, as in the CSVs.
- I did not import the build script.
- Files are given by run label. The CSV is `<label>.csv` in `experiments_v4/results` or `experiments_v5/results`; summary logs are `*.log`.

| # | Report value (location) | Recomputed (source) | Outcome |
|---|---|---|---|
| 1 | Table III, 3p/100 held-out: 52.6 [48.2, 56.9], +0.73 ± 0.46, n = 500 | 52.60 [48.22, 56.94], +0.730 ± 0.462 (`r2_val_3p100`) | ✓ |
| 2 | Table III, 4p/100 held-out: 44.4 [40.1, 48.8], −0.61 ± 0.43 | 44.40 [40.10, 48.78], −0.614 ± 0.427 (`r2_val_4p100`) | ✓ |
| 3 | Table III, DUCT + ISMCTS: 55.0 [50.6, 59.3], +0.72 ± 0.40 | 55.00 [50.62, 59.31], +0.722 ± 0.397 (`r2_duct_3p100`) | ✓ |
| 4 | Table III, ISMCTS × 2: n = 300, 54.2 [48.5, 59.7], +1.66 ± 0.69 | 54.17 [48.51, 59.72], +1.663 ± 0.694 (`v5_field_3p100`) | ✓ |
| 5 | Table III, BasicMCTS + RHEA: 84.2 [80.7, 87.1], +6.68 ± 0.60 | 84.20 [80.74, 87.13], +6.682 ± 0.603 (`B_mcts_rhea`) | ✓ |
| 6 | Table III, OSLA + BasicMCTS: 87.2 [84.0, 89.8], +9.74 ± 0.75 | 87.20 [83.99, 89.85], +9.744 ± 0.754 (`B_osla_mcts`) | ✓ |
| 7 | Table III, n of the pending rows: 250 / 250 / 250 | `experiments_v5/q2.txt` queue | ✓ |
| 8 | Fig. 2, v2 at 100/500/900 ms: 24.8 / 18.3 / 18.8 | 24.80 / 18.30 / 18.80 (`s1_ref`, `f5_ref`, `f9_ref`) | ✓ |
| 9 | Fig. 2, v3: 50.1 / 40.7 / 32.8 | 50.10 / 40.67 / 32.80 (`abl_T4`, `f5_finalT4`, `f9_finalT4`) | ✓ |
| 10 | Fig. 2, v4: 55.7 / 49.7 / 47.6; n = 500 / 500 / 250 | 55.70 / 49.70 / 47.60 (`d1_I`, `f5_I`, `f9_I`) | ✓ (the v4 development files are functionally identical to the final 3-player JSON) |
| 11 | Fig. 2, open circle 52.6 | `r2_val_3p100` | ✓ |
| 12 | Fig. 3 / IV-C, −H: −28.3 ± 5.8 (z 9.6), margin −5.45 ± 0.71 | −28.27 ± 5.79 (z 9.57), −5.45 ± 0.71 (`A_noH` vs `V_3p100`) | ✓ |
| 13 | −S−I: −10.2 ± 5.7 (z 3.5) | −10.17 ± 5.75 (z 3.47) | ✓ |
| 14 | −R: z 2.0; −B: z 1.4; −I: −3.8 ± 5.7 (z 1.3) | −6.17 ± 5.94 (z 2.04); −4.27 ± 5.83 (z 1.43); −3.77 ± 5.68 (z 1.30) | ✓ |
| 15 | Holm: all five margin effects survive; for win rate only H and S + I | Margin p = 0.025, 0.016, 0.0008, ~0, ~0 → all survive. Win: the third smallest p is 0.041; ×3 = 0.12, which fails | ✓ |
| 16 | Lead heuristic: −13.6 ± 5.9 (z 4.5) | −13.60 ± 5.88 (z 4.54) (`r2_lead_3p100` vs `v5_base3p100`) | ✓ |
| 17 | X3 −1.3 ± 5.8; N −1.8 ± 5.8; win-gap −0.6; w = 1 +0.7; R3V2 −23.7 ± 5.8 (z 8.0) | −1.30 ± 5.84; −1.80 ± 5.79; −0.60; +0.70; −23.70 ± 5.81 (z 8.00) | ✓ |
| 18 | Fig. 3 caption bases: 55.0% and 58.1% | 54.97 (`V_3p100`), 58.10 (`v5_base3p100`) | ✓ |
| 19 | III-D: 36 806 vs 10 170 iterations per decision | `bench4.txt` | ✓ |
| 20 | III-E: ±4.4 pp at 50% with 500 games; about ±5.7 pp paired | 4.38 pp; observed paired 5.68–5.94 pp | ✓ |
| 21 | III-E: pairing correlation "0.0–0.15" | 0.002–0.220 across the paper's paired comparisons; 0.009–0.139 for the seed-7 3-player set | ✗ understated (should read 0.0–0.2) |
| 22 | IV-A: 24.8 → 50.1, +25.3 ± 5.8 | +25.30 ± 5.79 (independent samples) | ✓ |
| 23 | IV-A factorial: R +8.7 ± 2.1 (z 8.1), K +5.2 ± 2.2, M +4.1 ± 2.0, T +3.6 ± 2.0, M×K +3.2 ± 2.2, R×T −4.2 ± 2.1 | +8.65 ± 2.10 (8.06), +5.15 ± 2.15, +4.10 ± 1.99, +3.65 ± 2.02, +3.17 ± 2.20, −4.22 ± 2.09 (16 raw cells; same intervals whether treated as paired or independent) | ✓ |
| 24 | IV-A: progressive bias −1.2 ± 4.3 | −1.18 ± 4.31 (1 000 games, seeds 2001 and 3001, `tb*_T4_A` vs `tb*_T4`) | ✓ number; ✗ wording (§3.3 f) |
| 25 | IV-A: opponent model in the tree −16.9 ± 5.8 | −16.90 ± 5.77 (`abl_plusO` vs `s4_K0.5_T8_M`) | ✓ |
| 26 | IV-A: v3 −17.3 ± 7.3 and v4 −8.1 ± 7.6 from 100 to 900 ms | −17.30 ± 7.29; −8.10 ± 7.57 | ✓ |
| 27 | IV-A: S + I gain +5.6 ± 6.2 → +14.8 ± 8.5; rise +9.2 ± 10.5 (z 1.7) | +5.60 ± 6.18; +14.80 ± 8.50; +9.20 ± 10.51 (z 1.72) | ✓ |
| 28 | IV-A: S alone +1.3 (100 ms) and +6.2 (500 ms) | +1.30 ± 6.14 (a replicate run `d1_S2` gives +1.40); +6.23 ± 6.07. The difference is +4.9 ± 8.6, z 1.1 | ✓ (no intervals given in the text) |
| 29 | IV-A: 19 696 vs 21 246 iterations at 900 ms | `probe_iterations.txt` (2 games, about 49 decisions each) | ✓ |
| 30 | IV-A: ISMCTS seats won 27.7% at 100 ms and 35.1% at 500 ms | `V_3p100` and `V_3p500` logs | ✓ but selective: 29.6% at 900 ms, where our agent won 56.0%, is omitted |
| 31 | IV-B: seed-7 result 55.0%, held-out −2.4 ± 6.2 | −2.37 ± 6.14 | ✓ (comparator choice: §3.3 b) |
| 32 | IV-B: DUCT seats 21.0%, ISMCTS 24.0%, ours 55.0% | `r2_duct_3p100.log` | ✓ |
| 33 | IV-B: BasicMCTS, RHEA and OSLA at most 12.8%; OSLA 0% | 9.0 / 6.8 / 12.8 / 0.0% (`B_*` logs) | ✓ |
| 34 | IV-C: Sashimi −2.7, Maki −1.9, Dumpling +1.9, Pudding +1.3 per game | −2.68 ± 0.70, −1.88 ± 0.51, +1.89 ± 0.50, +1.30 ± 0.28 (point columns of `f5_ref` vs `f5_finalT4`) | ✓ |
| 35 | IV-D: 3-player configuration at 4 players: 46.4% (100 ms), 38.4% (500 ms) | 46.43 (`q4_I`), 38.40 (`val_q5`) | ✓ but an unreported replicate `q4_Io` (same JSON file) gives 43.50% |
| 36 | IV-D: X0 at 4p/100 **+3.2** ± 6.2 | **+3.13** ± 6.19 (`q4_I` 46.43 → `q4_X0c` 49.57). The 3.2 came from subtracting rounded values in RESULTS.md | **✗ should be +3.1** |
| 37 | IV-D: X0 + W2 + K 1 at 4p/500 +7.2 ± 8.6 | +7.20 ± 8.63 (`val_q5` → `q5_W2`) | ✓ but replicate `q5_W2r` gives 42.8% (pooled +5.8 ± 7.4) |
| 38 | IV-D: F + L +1.0 ± 7.9 at 500 ms; −6.6 ± 6.0 (z 2.2) at 100 ms | +1.00 ± 7.86 (seed 2, paired); −6.57 ± 5.96 (z 2.16) | ✓ |
| 39 | IV-D: 3p/500 X0 + W2 + K 1 −7.7 ± 5.4 (z 2.8) | −7.70 ± 5.39 (z 2.80), `f5_FL8` vs `f5_W2FL8`, with F + L8 in both arms | ✓ (the confound is not stated) |
| 40 | IV-D: interaction 14.9 ± 10.2 (z 2.9) | 14.90 ± 10.13 (z 2.88); with replicates pooled 13.50 ± 9.14 (z 2.89) | ✓ |
| 41 | IV-D: depth 1 −17.4 ± 8.2 (z 4.2); 100 ms agent −9.0 ± 7.7 (z 2.3); 1 000 ms −4.8 ± 8.1 | −17.40 ± 8.19; −9.00 ± 7.75; −4.80 ± 8.09 | ✓ |
| 42 | IV-D: 73 positions, 704 root children, 53% visited once, mean depth 1.5; 2.7%, 5.5% and re-run 5.5% | `treestats4.txt`, `agree4.txt`. Mean depth is the visit-weighted node depth. The "re-run" uses the same random seed; with another seed 9.6% of cards change | ✓ (wording: §3.3 h) |
| 43 | IV-E: X3 wins 45.0% of greedy games; denial gains about 1 pp | 45.00 ± 1.06% (8 000 games); +0.8 to +1.5 pp with three players (`greedy_duels.txt`) | ✓ |
| 44 | Contributions: 162 runs, 69 350 games | 162 distinct CSVs with ≥ 250 games and 69 350 games, deduplicated by md5, no duplicated content. This includes earlier runs of the v2 era | ✓ |
| 45 | Intro: 729 and 4 096 joint moves | 9³ and 8⁴ | ✓ arithmetic; ✗ "up to" ignores Chopsticks |
| 46 | Table II: 48–73 s of CPU time per game | 4 × 24 × 0.5 s = 48 s; 3 × 27 × 0.9 s = 72.9 s | ✓ (3p/500 is about 40 s, outside the quoted range) |
| 47 | Table II: "300 in one run" | One such run in the paper (`v5_field`); 7 of the 162 runs have 300 games | ✓ for the paper |
| 48 | Table II: opponent configurations | TAG ISMCTS = `SushiGo_3+P_HiBudget` with only the budget changed. DUCT = `SushiGoDUCT` with defaults K 1 and information sets. BasicMCTS K 1.41, 10-move rollouts. RHEA horizon 10, population 10. OSLA with the score heuristic | ✓ (MAST "Both" and exploreEpsilon 0.1 of ISMCTS omitted) |
| 49 | Table I: X0 constants; 3p/4p ticks | JSON `args` [15, 0.7, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3]. The 100 ms 4-player block uses X0 with K 0.5. Blocks from 500 ms use w 1, terminalScale 2, sigmaRoundDecay 0.5, opponentScoreValue and K 1. F + L8 are in `SushiMCTS_500_FL8.json` and both competition files | ✓ |
| 50 | Method constants: α 0.5, T 4, σ 15, w 0.7, ε 0.2, K 0.5 / 1, L 8 | `CardValueModel`, `SushiGoHeuristic`, `SushiMCTSParams`, JSON files | ✓ |
| 51 | Abstract and conclusions: 52.6%, 44.4%, 28.3, 10.2, 8.7, −8.1 / −17.3, z 2.9 | As above | ✓ |

**On the build script.**
- The helper functions in `build_report.py` are correct: Wilson interval, standard error, pairing by game index, Holm-free z, two-sample interval in percentage units, deduplicated study size, and log parsing.
- But most numbers in the Results text and the Fig. 2 points are typed in from RESULTS.md, not computed. That is how the +3.2 error got in.
- The docstring claims that every number comes from the result files; that is only true via RESULTS.md.
- `text_final_agent()` compares the held-out run with the old-machine seed-7 run rather than the same-machine re-run (§3.3 b).
- `text_treestats()` parses the different-seed agreement figure (90.4%) but never uses it.

---

## 3. Technical correctness

### 3.1 Does the Method describe the code?

**Yes, in substance.**

- **Equations (1)–(3), H:**
  - `CardValueModel.estimatedScore` computes banked + α·potential + Maki standing + w_t·Pudding standing, with w_t = (round + 1)/3.
  - The completion factor is min(1, (r − m + 1)/T), and 0 if r < m.
  - `SushiGoHeuristic.valuesOf` computes tanh((w(Ŝi − max) + (1 − w)(Ŝi − mean))/σ), with σ 15 and w 0.7.
- **Two omissions in (1):**
  - U_p also values each unused Wasabi as (3 − 1)·ν·c(r, k) with ν = 2, and Chopsticks as 1·c(r, 2). Neither is an "unfinished set".
  - The marginal value mv uses r − 1 picks on both sides of the difference.
- **Undefined constants.** Seven of the eleven X0 constants (β, T_m, κ, δ, π, ν, γ) appear only in Table I note a and are never defined. The four-player model cannot be reproduced from the paper.
- **B and R:**
  - `rolloutCard`, `greedyAction` and `policyValues` implement the ε-greedy choice with ε 0.2.
  - The second Chopsticks card is the greedy best in the tree. In rollouts it is the best card only with probability 1 − ε.
  - The rollout ends after `rolloutRounds` = 1 round has been scored.
- **P, M:** `distinctActions` and `distinctCards` implement P; `playerValues` and `valuesOf` implement M. Both ✓.
- **S and I:**
  - One node per turn; untried cards first; UCB on the player's own statistics; children keyed by the joint card choice, packed 8 bits per player.
  - `state.copy(me)` gives a fresh sample in each iteration, and the open-loop nodes hold statistics only.
  - Statistics are keyed by card type, Maki icons and Chopsticks use; the paper says "card type", which is close enough.
  - Equation (4) uses the availability count, as in `selectInformationSet`. ✓
- **Four players (W2, F, L):** W2 is implemented exactly as described:
  - finished games valued by tanh((Si − max Sj)/2), plus 0.01 × Pudding as the tie-break;
  - σ multiplied by (R/2)^0.5 with R = max(0.5, rounds left);
  - opponents value their lead over the others' mean;
  - K 1;
  - F is `SushiSim`, with L = 8 rollouts averaged per leaf.
- **Compliance with the brief:**
  - No `players.mcts` import (grep).
  - The package compiles against TAG's classes alone (javac, Java 21).
  - The 1 s competition file played 3 three-player and 3 four-player games against random players without error (40 ms test budget; it won all six).
- **Minor:** `expand()` keeps BasicTreeNode's `new Random(seed)` quirk, but only on the closed-loop path, which the final agent does not use.
- **README problems** (it is assessed together with the code):
  - It begins "Rename first: replace every `groupA` … with your real group letter". This instruction to the students is still in the deliverable.
  - It says the seed-7 validation used "fresh deals that were not used during development", which contradicts the report ("overlaps the development deals").
  - At 6,259 words it reads like a lab notebook.
  - It tells the students to "submit the file of the setting the module team confirms". The brief asks for one competition JSON.

### 3.2 Literature claims

| Claim | Verdict |
|---|---|
| [1] TAG splits a game into state, forward model and parameters; the agent receives a copy with unseen cards reshuffled | ✓ Matches `SGGameState.redeterminise` |
| [2] The four MCTS phases; heuristic rollouts as domain knowledge | ✓ |
| [3] ISMCTS avoids strategy fusion; samples anew every iteration; single-observer vs multiple-observer forms | ✓ But "developed for card games such as the three-player Dou Di Zhu" oversimplifies: they evaluated on a board game (Lord of the Rings: The Confrontation), Phantom (4,4,4) and Dou Di Zhu |
| [4] Klang et al. introduced the game to TAG and assessed simultaneous action selection and complete information; their study "predates the version with hidden hands" | ✓ consistent with the paper's framing (Sushi Go! "combines simultaneous action selection and complete information"). I could not open the full text (host blocked). Safer wording: "treated the game as one of complete information" |
| [5] UCT/UCB1 | ✓ |
| [6] Progressive bias | ✓ |
| [7] Silver and Tesauro: balanced rather than strong rollouts | ✓ Acceptable paraphrase. Their objective is a small error in the value estimate, i.e. errors that cancel out |
| [8] Long et al.: PIMC success linked to how quickly hidden information is revealed | ✓ (the "disambiguation factor") |
| [9] Strategy fusion and non-locality | ✓ |
| [10] MultiTree MCTS in TAG, one tree per player | ✓ |
| [11] DUCT; Lanctot et al. compared it with EXP3 and regret matching in Goofspiel | ✓ But DUCT originates with Finnsson and Björnsson, and non-convergence with Shafiei et al. 2009; neither is cited |
| [12] Tak et al.: the best variant depends on the game; two-player games | ✓ Note the Sushi Go!-specific thesis that found DUCT better than EXP3 |
| [13] Sturtevant: multi-player UCT computes a mixed-strategy equilibrium and played as well as or better than earlier algorithms | ✓ |
| [14] Wilson interval | ✓ |

**No fraudulent or inaccurate summary was found.** This matters because the brief treats an inaccurate AI-written summary as misconduct.

### 3.3 Are the conclusions supported by the statistics?

**a. Selective reporting on RQ2.**
- Seed-7 validation runs of the final agent exist: 55.0% at 100 ms, 49.4% at 500 ms and 56.0% at 900 ms (n = 500, 500, 250).
- At 900 ms the agent is not worse than at 100 ms (+1.0 ± 7.5 pp).
- The same agent at the same 900 ms budget scored 47.6% in development and 56.0% on seed 7: a difference of +8.4 ± 8.7 pp.
- The report cites the ISMCTS seat shares from those seed-7 runs at 100 and 500 ms (27.7% and 35.1%), but not at 900 ms (29.6%).
- Better and consistent evidence sits in the very runs plotted in Fig. 2. The ISMCTS seats won:
  - against v4: 23.1%, 33.1% and 37.4% at 100, 500 and 900 ms;
  - against v3: 33.0%, 40.0% and 46.0%.
- The word "validation" is also used for two different deal sets (seed 7 and seed 100 000).

**b. Choice of comparator.** The held-out run (52.6%) was played on the 4-core machine.

| Held-out result compared with | Difference | z |
|---|---|---|
| seed-7 run on the old machine (55.0%) | −2.4 ± 6.1 pp | −0.8 |
| seed-7 re-run on the same machine (58.1%) | −5.5 ± 6.1 pp | −1.8 |
| both seed-7 runs pooled | −3.9 ± 5.3 pp | −1.5 |

"We see no sign that the development overfitted its deals" is too strong.

**c. Unreported replicates.**
- `q4_Io` repeats the 3-player configuration at 4p/100 and gives 43.5%.
- `q5_W2r` repeats W2 at 4p/500 on seed 2 and gives 42.8%.
- The text uses the higher run each time.
- Pooling all four runs gives X0 +4.3 ± 4.3 pp (z 1.9), W2 +5.8 ± 7.4 pp (z 1.5), and an interaction of 13.5 ± 9.1 pp (z 2.9). The H3 contrast survives pooling, so reporting honestly costs nothing.

**d. H3 ("the best settings depend on the number of players").**
- The evidence is one-sided. The harm with three players is significant (−7.7 ± 5.4 pp); the gain with four players is not (+7.2 ± 8.6 pp, or +5.8 ± 7.4 pp pooled).
- It is confounded:
  - the 3-player contrast has F + L8 in both arms, the 4-player contrast has neither;
  - the seeds differ;
  - one 4-player arm was played on TAG's own random deals.
- It is post hoc, chosen after about 40 four-player runs. A Holm correction over 40 tests needs z ≳ 3.2.
- "None of the 4-player changes is individually significant" contradicts the next clause, "F + L cost 6.6 ± 6.0 pp (z = 2.2)".
- The paper promises to "state where a conclusion survives a Holm correction" but does so only for the ablation.

**e. RQ2 (H2).**
- IV-A correctly calls the rise (z 1.7) non-significant, but the abstract states it as a finding.
- "S alone … so the decoupled tree carries most of the budget dependence" compares +1.3 ± 6.1 with +6.2 ± 6.1 pp: a difference of +4.9 ± 8.6 pp (z 1.1).

**f. Progressive bias.** "Progressive bias no longer helped once informed rollouts were present" does not follow from the data. B was already present in v2, and the contrast (v3 with and without A, seeds 2001/3001) does not vary B.

**g. Decomposition of H.**
- The lead-heuristic run rescaled K to 7.5, while the −H run kept K = 0.5. The paper does acknowledge the scale mismatch.
- The two contrasts use different base runs (58.1% and 55.0%).
- "Accounts for about half … and the Pudding standing, the mean-opponent term and the bounded scale … the rest" attributes effects that were never tested separately.

**h. Agreement diagnostic (Agree4).**
- "Re-running the search (5.5%)" is a re-run with the same random seed. With another seed, 9.6% of the chosen cards change.
- The positions come from games played by random agents.
- Each percentage counts only 2–7 decisions out of 73, and no uncertainty is given.

**i. Pairing correlation.** The text says 0.0–0.15; the data give 0.0–0.2.

**j. Done correctly:**
- Paired and independent samples are correctly distinguished. Pairing on fixed deals starts with v4.
- The factorial intervals are valid. I checked them both per game and from independent cell means.
- Wilson intervals with ties counted as 1/k are an acceptable approximation.

---

## 4. Compliance

- **Page limit ✓.**
  - The main text ends on page 4: Conclusions end about two-thirds down the right column, and the lowest text on page 4 is at 765.5 pt against a 769.9 pt limit.
  - The generative-AI statement starts on page 4 and runs onto page 5; the references are on page 5. Five pages in total.
  - Page 5 is about half empty. The main text can therefore grow by about one third of a column (≈ 200 words); the AI statement and references would then move entirely to page 5 and still fit.
- **Template.**
  - Measured from the PDF: body 10 pt, captions and tables 8 pt, abstract 9 pt bold, title 24 pt, three author columns, the template's heading styles. ✓
  - Margins: top 27 pt (page 1) and 54 pt, bottom 72 pt. ✓ Left and right are 44.65 pt against the template's 45.35 pt for body sections (a LibreOffice round trip; 0.25 mm, negligible).
  - **Fonts:** the PDF embeds Liberation Serif, plus FreeSerif and OpenSymbol for symbols, not Times New Roman. The .docx asks for Times New Roman; LibreOffice substituted it.
  - Table notes are 7 pt (template table text: 8 pt). Text inside figures is 6.1–7.5 pt.
  - The .docx metadata still shows "lastModifiedBy: Usman Naeem", inherited from the template.
- **Word-count guide** (body text from the .docx, excluding tables, captions and equations):

  | Section | Words | Guide | Note |
  |---|---|---|---|
  | Abstract | 122 | 75–125 | ✓ |
  | Introduction | 455 | 350–450 | slightly over |
  | Background | 649 | 650–850 | ✓ |
  | Method | 861 | 700–800 | plus about 577 words inside Tables I–II |
  | Results | **1,057** | 500–700 | **+51%** |
  | Conclusions | 258 | 200–300 | ✓ |

- **Generative AI.**
  - Acknowledged: a first-page note covers "all sections, tables and figures" and cites Claude [15], and there is a full statement. ✓
  - The prompts are reproduced; the Turkish-to-English translation is accurate. ✓
  - Risks:
    - The rule asks for a citation in each section that used AI text; the paper relies on a single global note.
    - The text was revised in a loop driven by the findings of a second AI instance. Those revision prompts are neither included nor archived.
- **References:** 15, the maximum allowed; consistent IEEE style. ✓
- **At least 500 games, or a justification:**
  - Most conditions have 500 games.
  - The 250-game runs (900 ms, 4p/500 development, pending validation) are justified by CPU cost. The justification is weak: 500 games at 900 ms take about 2.5 h on 4 cores.
  - The 300-game run against two ISMCTS agents (100 ms) is not justified.
- **Experiments at 500 ms ✓:** development runs with 3 and 4 players, the 4-player diagnostics, and the pending held-out runs.
- **Package rules ✓:** package name, no advanced MCTS, a README with constructor instructions, competition JSONs. Fix the "Rename first" note and name a single competition file.

---

## 5. Presentation

- **Legibility.**
  - Figures are sharp, but text inside them is 6–7.5 pt at print size: Fig. 1 body text 6.1 pt, Fig. 2 legend 6.3 pt, Fig. 3 labels 6.3 pt. IEEE asks for at least 8 pt.
  - The Fig. 2 x-axis label is awkward ("CPU time per decision (development runs: n games)").
  - The caption should say that the open circle is the same agent as v4.
- **Captions** are informative.
- **Table II** is never referred to in the text; the writing tips require every table to be referred to.
- **Table III**'s title says "held-out", but three of its rows use seed 7.
- **Undefined acronyms:**
  - TAG, UCT and ISMCTS in the abstract;
  - UCB-Tuned (Table II);
  - EXP3 (Section V).
- **Internal codes:**
  - about 15 in total;
  - X0 and W2 are names never explained;
  - "v1" is never mentioned;
  - "O" is used in IV-A without a Table I entry;
  - "seed block" and "seed s" are used interchangeably;
  - "validation" means two different things.
- **Flow.**
  - Results open with the v2 → v3 history; the main result only comes in IV-B.
  - IV-C mixes v3-vs-v2 card statistics into the ablation of the final agent.
  - The IV-D paragraph packs in 9 intervals.
- **Mechanics.**
  - The informal "(mostly)".
  - The assignment is mentioned in the Background.
  - "Sushi Go!" followed by no comma reads as a sentence end.
  - The orphan "(1)" in Table I.

---

## 6. Prioritised changes

**Space budget.** About 200 words of slack remain at the bottom of page 4. Must-fix items add about 90 words net. Should-fix items need the cuts listed at the end of this section.

### MUST FIX

**M1. Never submit "pending".** (Abstract; Experimentation; General)

If the three held-out runs finish, rebuild with the script. It already fills Table III, the open circles in Fig. 2, the abstract and IV-B. Then apply §7.

If they do not finish, replace the three rows with the seed-7 runs:

| Setting | Opponents | n | Win % [95% CI] | Margin |
|---|---|---|---|---|
| 3p/500 ᵇ | v2, ISMCTS | 500 | 49.4 [45.0, 53.8] | +0.40 ± 0.40 |
| 3p/900 ᵇ | v2, ISMCTS | 250 | 56.0 [49.8, 62.0] | +1.06 ± 0.62 |
| 4p/500 ᵇ | v2, ISMCTS | 500 | 41.4 [37.2, 45.8] | −1.53 ± 0.46 |

Note b: "seed block 7, whose deals overlap the development seeds".

In the abstract, replace "and [pending] % at 900 ms" with "and 56.0% at 900 ms on a second deal set".

**M2. Correct the selective budget evidence.** (Critical analysis; Rigour)

In IV-A, replace:
> "Part of every agent's decline comes from the opponents: the TAG ISMCTS seats won 27.7% of v4's validation games at 100 ms but 35.1% at 500 ms."

with:
> "Part of the decline comes from the opponents: in the same runs the TAG ISMCTS seats won 23.1%, 33.1% and 37.4% of v4's games at 100, 500 and 900 ms. On the seed-7 deals the final agent did not decline at 900 ms (55.0%, 49.4% and 56.0%; n = 500, 500, 250), so the trend is not robust across deal sets."

(+27 words)

**M3. Rewrite the four-player paragraph** (IV-D), from "With four players the 3-player configuration…" to "…between the table sizes.". This fixes the +3.2 error, uses all replicates and states the evidence for H3 honestly. (Critical analysis; Rigour)

> "With four players the 3-player configuration won 46.4% and 43.5% in two runs at 100 ms and 38.4% at 500 ms (development; chance 25%). The 4-player changes were small at their own budgets: X0 added +4.3 ± 4.3 pp at 100 ms (two runs of each pooled); at 500 ms X0 with W2 and K = 1 added +5.8 ± 7.4 pp (two runs pooled) and F + L a further +1.0 ± 7.9 pp, whereas F + L cost 6.6 ± 6.0 pp at 100 ms (z = 2.2). With three players at 500 ms the same X0, W2 and K = 1 cost 7.7 ± 5.4 pp (z = 2.8; F + L in both arms), so the effect differs between table sizes by 13.5 ± 9.1 pp (z = 2.9). This supports H3 only one-sidedly: the loss with three players is clear, the gain with four is not, and the contrast was chosen after about 40 four-player runs."

(+25 words)

**M4. Align the RQ3 claims elsewhere.** (Abstract; Contributions; Conclusions)

- Abstract: replace "and that four players need different settings" with "and that settings tuned for four players cost 7.7 percentage points with three". A complete abstract of at most 125 words is given in §7.
- Contributions, bullet 3: "evidence that settings tuned for four players hurt with three, and a diagnosis of why extra time helps little with four (a wide, shallow joint-move tree);"
- Conclusions: replace "For RQ3, the best settings differ between three and four players (z = 2.9), and …" with "For RQ3, the four-player settings cost 7.7 ± 5.4 pp with three players while their gain with four was not significant, and …".

**M5. The overfitting sentence.** (Critical analysis)

In IV-B, replace:
> "On seed block 7, which overlaps the development deals, it had won 55.0% at 100 ms (−2.4 ± 6.2 pp), so we see no sign that the development overfitted its deals."

with:
> "On seed block 7, which overlaps the development deals, it had won 55.0% at 100 ms, and 58.1% when re-run on the machine used for the held-out games; the held-out result is 2.4 ± 6.1 and 5.5 ± 6.1 pp lower (neither significant)."

**M6. The progressive-bias sentence.** (Critical analysis)

Replace "Progressive bias no longer helped once informed rollouts were present (−1.2 ± 4.3 pp)" with "Adding progressive bias back to v3 did not help (−1.2 ± 4.3 pp, 1 000 games on two further deal sets)".

**M7. Make the records reachable.** (Documentation)

Table II "Records" row: "agent files, per-game results and analysis scripts of every run: <public repository or OneDrive URL> (outside the submitted package)". The submitted zip will contain only `groupA/`.

**M8. Refer to Table II in the text.** (General)

Start III-E with: "Table II summarises the setup. Each game is an independent trial …"

**M9. Fix the package README.** (Approach: code is assessed)

- Delete the "Rename first" note, or rename the package and files consistently to the real group letter.
- Replace "on fresh deals that were not used during development (validation, seed 7)" with the report's wording ("seed 7, whose deals overlap the development seeds").
- Name one competition file, e.g. "`SushiMCTS_1000ms.json` is our competition file (3 players, 1 s); `SushiMCTS_500ms.json` is for a 500 ms setting."

### SHOULD FIX

**S1. Restructure Results and cut the codes.** (Clarity; Critical analysis)

- Order the subsections as:
  1. Final agent (Table III)
  2. Ablation (Fig. 3)
  3. Time budget (Fig. 2, RQ2)
  4. Four players
  5. What did not help
- Fold the v2 → v3 history and the factorial into the time-budget subsection, in two sentences.
- Keep only the Table I letters H, B, R, P, M, S and I.
- Write "previous agent (v2)", "sequential-tree agent (v3)" and "final agent" instead of v2/v3/v4.
- Write "four-player constants" and "win-gap value" instead of X0 and W2.
- Name X3, denial and the other last-study variants in words only.

**S2. Specify the card model.** (Understanding; Documentation)

Add to III-A (≈ 60 words):
> "U_p also values each unused Wasabi as (3 − 1)ν per Nigiri it can still take (ν = 2) and unused Chopsticks as 1 point, both times the completion factor. The four-player constants X0 further discount a single Sashimi (β), blend the Maki standing with κ points per icon early in a round (horizon T_m), add δ times the next Dumpling, scale the Pudding standing (π) and require γ cards of a type per missing card in the hands."

Replace Table I note a with:
> "a X0 = (σ, w, α, T, β, T_m, κ, δ, π, ν, γ) = (15, 0.7, 0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3); default (15, 0.7, 0.5, 4, 1, 0, 0, 0, 1, 2, 0)."

**S3. Deepen the literature review.** (Literature; Motivation; Referencing)

- In II-C, before the gap sentence, add: "Card drafting has been studied with MCTS in 7 Wonders [Robilliard et al.], where card values depend on the stage of the game and on the other players' picks;" and soften "has not been studied" to "has not been measured".
- To stay within 15 references, cite progressive bias to the survey [2] and drop Chaslot [6]. That frees a slot for Robilliard et al.
- Optionally mention the Sushi Go! DUCT-vs-EXP3 thesis in the EXP3 future-work item. It is not peer-reviewed; it would need a further swap.

**S4. Figures.** (Figures/tables)

- Replace Fig. 1 with a conceptual drawing of one turn node: per-player card statistics with availability counts, and children keyed by the joint choice. Contrast it with v3's sequential tree, where player 2 "sees" player 1's card. This is the paper's core idea.
- Set figure text to at least 7.5–8 pt.
- Add the 500/900 ms validation points to Fig. 2.
- Retitle Table III "Final agent on held-out deals and against other opponents".

**S5. Describe and test what is actually submitted.** (Documentation; Rigour)

Add to III-D (≈ 45 words):
> "The submitted files use 900 ms (1 s limit) and 430 ms (500 ms limit) of CPU time with 925 and 450 ms wall-clock guards; on 135 deals of the seed-7 900 ms run the 1 s file won 51.1% [42.8, 59.4] (benchmark file on the same deals: 52.6%)."

Better still: run 500 held-out games of the 1 s file with 3 players. That takes about 2.5–3 h on 4 cores.

**S6. A 500 ms ablation of the two big components.** (Rigour)

Run H → own score/50 and −S−I at 500 ms, paired, 500 games each, about 1.5 h per run. It directly answers whether the 100 ms ablation holds at competition budgets.

**S7. Hedge the over-interpretations.** (Critical analysis)

- IV-A: "S alone added +1.3 ± 6.1 pp to v3 at 100 ms and +6.2 ± 6.1 pp at 500 ms; the decoupled tree may carry most of the budget dependence, but the difference is not significant (z = 1.1)."
- IV-C: "Replacing H by TAG's lead heuristic (own score minus the best opponent's, with K rescaled to 7.5) cost 13.6 ± 5.9 pp (z = 4.5), about half as much: valuing the lead rather than the own score recovers about half of H's effect; we did not separate the rest (Pudding standing, mean-opponent term, bounded scale, scale of K)."
- IV-D: "…no more often than re-running the search with the same seed (5.5%; 9.6% with another seed)."
- Abstract: "the tree's advantage grew with the time budget, though not significantly".

**S8. Small accuracy fixes.**

- "(0.0–0.15)" → "(0.0–0.2)".
- "give up to 729 or 4 096 joint moves per turn" → "give 729 or 4 096 joint moves per turn (more when a player can use Chopsticks)".
- OSLA: "OSLA wins none: its copy of the state hides the other players' choices, so its one-step look-ahead never reveals a card or changes the score and it plays at random." I checked `OSLAPlayer` and `SGGameState.redeterminise`.
- Background: replace "but the assignment does not allow it as a basis; BasicMCTS has none of these ideas" with "but we build on BasicMCTS, which has none of these ideas, so that each idea's contribution can be measured".
- Introduction: replace "experiments of (mostly) 500 games" with "experiments of 500 games per condition (250 at the longest budgets)".

**S9. Fonts.** (Template) Export the PDF with real Times New Roman: export from Word, or install the core fonts before running LibreOffice.

**S10. Generative-AI completeness.** (Compliance) Add one sentence: "The revision prompts were the second instance's written findings; they are archived at <URL>."

### NICE TO HAVE

- **N1.** State the strongest simulation-balancing evidence: the default 3-player card model wins only 7.3% of greedy games in a pool of default and X0 players (X0 wins 45.7%), yet it gives the strongest 3-player search. (Critical analysis)
- **N2.** Replace the style-of-play sentence about v3 vs v2 with the final agent's own profile from the held-out run:

  | Points per game | Final agent | v2 (same games) |
  |---|---|---|
  | Sashimi | 2.98 | 9.08 |
  | Maki | 7.90 | 10.33 |
  | Dumpling | 6.42 | 3.88 |
  | Pudding | 2.88 | 0.49 |

  Alternatively, cut the sentence (C1).
- **N3.** Report run-to-run noise as an empirical check of the intervals: 55.0% vs 58.1% on identical deals, +3.1 ± 5.8 pp.
- **N4.** Cite Holm, RHEA and UCB-Tuned if a reference slot frees up. Define EXP3.
- **N5.** Compute every number in the text from the CSVs inside `build_report.py`, instead of typing them in from RESULTS.md.
- **N6.** Use "validation" only for the held-out seed block.
- **N7.** Clean the .docx metadata, fix the orphan "(1)" in Table I, and write "In Sushi Go!, …".

### Cuts to pay for the additions

| Cut | Saving |
|---|---|
| **C1.** Delete the IV-C sentence "The components also change the style of play: … over the whole game." | 44 words |
| **C2.** Condense the factorial sentence in IV-A to "…main effects were +8.7 ± 2.1 pp for R (z = 8.1), +5.2 ± 2.2 for K = 0.5 and +4.1 ± 2.0 for M, and M × K = +3.2 ± 2.2: max-n backups need less exploration." Drop the T = 8 clauses | ≈ 25 words |
| **C3.** Shorten the Table II "Opponents" cell to "v2: our previous agent (H, progressive bias, B, D, K 1.41). TAG ISMCTS: SushiGo_3+P_HiBudget at our budget (one tree per player [10], UCB-Tuned, K 1, random rollouts to the end, score heuristic). TAG DUCT: SushiGoDUCT (K 1, lead heuristic, 30-move rollouts). BasicMCTS (K 1.41), RHEA (horizon 10, population 10), OSLA." | ≈ 3 table lines |
| **C4.** Contributions bullet 4: "leave-one-out and factorial ablations, held-out validation and negative results (162 runs, 69 350 games)." | ≈ 10 words |
| **C5.** If still needed, delete "It is not a lack of search: at 900 ms v3 ran 19 696 iterations per decision, v4 21 246." | 19 words |

---

## 7. How to handle the pending results

**Status.** The queue (`TAG/arena/v5/results/queue.log`) is running `r2_val_3p900`. It was at 50/250 games after 1,293 s, about 26 s per game. `r2_val_4p500` and `r2_val_3p500` follow, 250 games each. My estimate is that all three finish about 3.5 h after 21:10, provided the 4-core machine is not shared.

**When they finish, pre-commit to these readings:**
- Report each budget's held-out win rate with its Wilson interval (about ±6 pp at n = 250).
- Compare 500 and 900 ms with the 100 ms held-out run as independent samples (about ±7.5 pp). Call them confirmatory measurements of the final agent, not tests of H2: there is no v3 comparator on these deals.
- If the 900 ms result is at or above the 100 ms result, as the seed-7 run suggests, rewrite the "decline" sentence in IV-A and the RQ2 summary accordingly.
- The 4p/500 run uses the competition 4-player configuration (F + L8). Quote it in the abstract.
- Add the open circles to Fig. 2.

**Revised abstract** (125 words; fill in [x]):

> "In Sushi Go!, players pick cards simultaneously from hidden hands that are passed around, which basic Monte Carlo tree search (MCTS) does not model. Starting from BasicMCTS in the Tabletop Games (TAG) framework, we added a scoring model that drives evaluation and rollouts, round-end rollouts, max-n backups, decoupled selection for the simultaneous choices and information-set search. On held-out deals the agent wins 52.6% of 500 three-player games at 100 ms against TAG's information-set MCTS and our previous agent (chance 33.3%), [x]% at 900 ms and 44.4% with four players (chance 25%). Ablations show that the scoring model and the decoupled information-set tree matter most; the tree's advantage grew with the time budget, though not significantly, and four-player settings cost 7.7 percentage points with three players."

**If they do not finish:** use M1's seed-7 substitution, labelled clearly, and say in Section V that the long-budget held-out validation is outstanding. Do not leave any "pending" cell or "[pending]" token.

**Design caveat to state honestly.** These runs have 250 games, below the brief's 500 rule. Justify this once, in Table II, with the CPU cost.

---

## 8. Would it receive 85% or more? What stands between it and 90%+?

**No, not as it stands.** My estimate is about 74% (70–78). With all the must-fix and should-fix changes, I would expect about 80–84%.

**What separates it from 90%+:**
1. **Evidence at the competition settings.**
   - At least 500 held-out games for the submitted 1 s file with three players, and for the 500 ms file with four.
   - The main ablation (H and S + I) repeated at 500 ms or more.
2. **Statistical discipline.**
   - Pre-specified primary comparisons.
   - Every replicate reported.
   - A family-wise correction, or explicit "exploratory" labels, for the four-player study.
   - An H3 contrast free of confounds: the same context (F/L, deal generator, seed) at both table sizes.
3. **Readability.**
   - Results within about 700 words and no more than about 6 codes.
   - A conceptual figure of the decoupled information-set turn node.
   - Figure text at least 8 pt.
4. **Critical positioning in the literature.**
   - MCTS for drafting games (7 Wonders), Sushi Go!-specific work, and multi-player simultaneous-move search.
   - This would sharpen the contribution, e.g. "a shared card-value model plus a decoupled information-set tree explain most of the strength, while much stronger greedy policies (X3, X0) do not improve the search", quantified at both table sizes.
5. **Visible ownership.** The statement attributes all the code and all the text to Claude, with a prompt asking for "a report that gets 100". Even a flawless paper risks being capped on originality and understanding. The individual reflections need to show who did and understood what.
