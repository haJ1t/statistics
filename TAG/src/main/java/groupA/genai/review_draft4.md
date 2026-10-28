# Independent marking review: "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!" (ECS7032P, Assignment 1, Group A)

Assessed: the PDF and DOCX in `report/`, the `groupA` package (7 Java files, README, JSON), against the brief, the rubric, the weights and the writing tips. Every number was recomputed from the raw per-game CSVs and arena logs with a script written from scratch (`/tmp/claude-0/review4/verify.py`; output in `verify_out.txt`). I did not read `report/reviews/`. Nothing under `/home/user/statistics` was modified.

---

## 0. Bottom line

* **Estimated mark: 78 %** (plausible range **73–85 %**), assuming the pending cells are filled in.
* The experimental work is the strongest part. It has paired designs on fixed deals, a held-out seed block, leave-one-out and 2^4 factorial ablations, Holm corrections and an explicit error model. All 120+ numbers I checked are right. The Method section matches the code.
* Marks are lost for four reasons:
  1. **Readability.** The paper is very dense. It leans on internal codes (H/B/R/P/M/S/I/X0/X3/W2/F/L, v2–v5, "last study") and reuses letters: S, M, P and R are modification codes and also symbols in eq. (1) and Section III-D.
  2. **Claims stronger than the statistics in the paper.** These are the "matter most" / "contribute most" ranking, the closing sentence of the Introduction, the attribution of v3's budget decline (which uses a selection-biased baseline), and an exploratory H3 contrast stated as a finding in the abstract.
  3. **A mostly descriptive literature review.**
  4. **Smaller compliance and documentation gaps.** Examples: a 300-game run with no justification, an undefined "v5", revision prompts left out of the AI statement, and one branch of the conditional wording for pending results that contradicts the paper's own statistics.
* **Would it get ≥ 85 % as it stands? Probably not** (I put it at roughly 1 in 5). Section 7 says what stands between it and 90 %+.

---

## 1. Rubric marks

Weights from the Assessment Overview: Abstract 5 %, Problem definition 15 %, Approach 30 %, Experimentation 30 %, Conclusions 10 %, General 10 %.

| Area (weight) | Item | Band | Mark | Reasons |
|---|---|---|---|---|
| **1 Abstract (5 %)** | Motivation | 70–100 | 80 | Gives in one clause why plain MCTS fits poorly (simultaneous picks from hidden, passed hands). |
| | Summary | 70–100 | 74 | All components are named, but the sentence "To BasicMCTS of the Tabletop Games (TAG) framework we added a card-value model that drives evaluation and rollouts, round-end rollouts, …" is awkward. It also reads as if the model "drives" round-end rollouts. |
| | Findings | 70–100 | 76 | Gives concrete held-out numbers. "Ablations show that the card-value model and decoupled selection matter most" is an unsupported ranking (see §3.3). "so the best configuration depends on the table size" presents an exploratory, post-hoc contrast as a result. It leaves out 500 ms, the budget the brief stresses. 124 words (guide 75–125). |
| | **Abstract overall** | | **77** | |
| **2 Problem definition (15 %)** | Motivation | 70–100 | 78 | Clear and cites TAG, the MCTS survey and ISMCTS. Says little about why the questions matter beyond this game, e.g. for drafting or simultaneous-move games in general. |
| | Problem clarity | 70–100 | 82 | Four properties (hidden hands, simultaneous moves, delayed scoring, joint branching) map cleanly onto RQ1–RQ3 and onto the modifications. Very good. |
| | Contributions | 70–100 | 71 | Four bullets, but why they matter is not argued. Bullet 3 ("evidence that settings tuned for four players hurt with three") rests on one contrast the paper itself calls exploratory. The closing claim, "simultaneous moves matter more to the search than the hidden hands", is not tested (§3.3). |
| | Literature (critical) | 70–100 (low) | 70 | The coverage is right: PIMC, strategy fusion, SO-/MO-ISMCTS, DUCT vs EXP3/RM, max-n, 7 Wonders and Klang et al. Every claim I checked is accurate (§3.2). But the review is mostly description. It does not report that Lanctot et al. found regret matching and OOS better and UCT more exploitable in Goofspiel, which bears directly on choosing DUCT. Robilliard et al.'s findings are not given. The gap statement is generic. |
| | Game description | 70–100 | 78 | Rules are compact and correct against TAG (108 cards, 12 − n cards, all scoring values, −6 Pudding only with 3+ players). Hidden information shrinking within n − 1 passes is noted well. The Pudding tie-break is missing. |
| | **Problem definition overall** | | **75** | |
| **3 Approach (30 %)** | Understanding / application | 70–100 | 82 | The description matches the code (§3.1). That covers the availability-count UCB for ISMCTS, the max-n vectors, the decoupled turn nodes, round-end rollouts and the fast simulator checked against the forward model. The work goes well beyond the lectures. Against it: `SushiTreeNode.java` is 1 753 lines with about 15 switches that are tested and off (U, G, E, K, Q, O, A, treeThreshold, …), so the final code path is hard to read. Also, under R the leaf value of H reduces to banked score plus the Pudding standing, which the paper never says (§3.3, item 5). |
| | Originality | 70–100 (low) | 74 | The paper says honestly that "the search techniques come from the literature; our own element is the card-value model". The tuned constants (X0) and the integer-array simulator add some originality. The combination is sensible, but little goes beyond published methods. |
| | Difficulty / ambition | 70–100 | 85 | High: five interacting search changes, separate four-player settings, a fast simulator, a factorial design and held-out validation. |
| | **Approach overall** | | **80** | |
| **4 Experimentation (30 %)** | Adequacy and rigour | 70–100 | 85 | Meets the top band in full: framework agents (TAG ISMCTS, DUCT, BasicMCTS, RHEA, OSLA), earlier versions (v2, v3), leave-one-out plus a 2^4 factorial, paired deals, held-out validation, and 500 games per condition with the 250-game runs justified. Deductions: a 300-game run (ISMCTS × 2) has no justification. The four-player settings were adopted on non-significant gains ("we kept each change where its estimate was positive"). |
| | Documentation | 70–100 | 78 | Table II plus the Table I footnote make replication largely possible. Gaps: "Agent files, per-game results and scripts: from the authors". The "Games" row misses the 250-game seed-7 validation at 900 ms and the 300-game run. "v5" is undefined. What −R is replaced by (10-move rollouts) is never stated. |
| | Critical analysis | 70–100 | 80 | Error analysis is excellent: Wilson CIs, paired standard errors, the measured pairing correlation, Holm corrections, and paired versus independent samples kept apart. The explanations go beyond the obvious: OSLA never reaching the reveal, the ISMCTS opponent getting stronger with time, Long et al. explaining why I adds little, the simulation-balancing caveat, the wide shallow four-player tree. Deductions are the claims in §3.3: ranking claims, the attribution of v3's decline from a selection-biased baseline, Holm applied to some results but not others, H2 not flagged as post hoc, and no statement of power for the null results. |
| | **Experimentation overall** | | **81** | |
| **5 Conclusions (10 %)** | Summary | 70–100 | 74 | Organised by RQ and concise, with limitations. It repeats "contribute most" (S at 6.4 pp is the same size as R at 6.2 pp). It quotes "28.3 pp over its default" without the K-scaling caveat given in Results. It leaves out the four-player and 500 ms held-out results. |
| | Future work | 70–100 | 78 | Three reasoned proposals: learning the evaluation, RM/EXP3 with the reason [11], and fitted opponent models. It does not mention that an online opponent model (Q, `OpponentModel.java`, "tested, off") is already in the code. |
| | **Conclusions overall** | | **76** | |
| **6 General (10 %)** | Clarity | 70–100 (low) / 60s | 68 | Logical structure and technically precise. But it is very dense: one sentence in IV-B holds 11 numbers. It relies on internal codes and course jargon ("last study", "v5", "in-class tournament", "seed block", "the 4-core machine"). Notation is reused (S, M, P, R; see §5). Several sentences are hard to parse (III-A definition of U_p, III-D, IV-D). |
| | Figures and tables | 70–100 | 80 | Fig. 1 is a real concept figure (sequential v3 tree vs decoupled turn node). Fig. 2 is an informative two-panel forest plot. Fig. 3 is clear, with a chance line. Captions are informative and everything is referenced in order. Minor: 7 pt figure text, Fig. 3 legend order, no Holm marks, no units on Table III's margin column. |
| | Referencing | 70–100 | 78 | 15 references in consistent IEEE style, relevant and almost all peer-reviewed, with accurate claims. Minor: [15] has no model version or access date; [15] is cited first (page-1 footnote) but numbered last. |
| | **General overall** | | **74** | |

**Weighted total:** 0.05·77 + 0.15·75 + 0.30·80 + 0.30·81 + 0.10·76 + 0.10·74 = **78.4 % ≈ 78 %**. A lenient marker who mainly rewards rigour could give about 84–85. A strict marker who penalises density, the overclaims and AI-generated prose could give about 73.

---

## 2. Number verification (recomputed from raw files)

Method: Wilson 95 % CI. Mean ± 1.96·SE with sample SD (n − 1). Paired differences are per-game differences on common game numbers. Independent comparisons use the two-sample SE (runs pooled where the report pools them). Holm uses two-sided normal p-values. Seat win rates come from the arena logs; the CSVs do not hold them. R4 = `experiments_v4/results`, R5 = `experiments_v5/results`, R3 = `experiments_v3/results`.

| # | Report value | Recomputed | Source | OK |
|---|---|---|---|---|
| **Table III** | | | | |
| 1 | 3p/100 held-out 52.6 [48.2, 56.9], +0.73 ± 0.46 | 52.60 [48.22, 56.94], +0.730 ± 0.462 | R5 r2_val_3p100.csv | ✓ |
| 2 | 4p/100 held-out 44.4 [40.1, 48.8], −0.61 ± 0.43 | 44.40 [40.10, 48.78], −0.614 ± 0.427 | R5 r2_val_4p100.csv | ✓ |
| 3 | DUCT, ISMCTS 55.0 [50.6, 59.3], +0.72 ± 0.40 | 55.00 [50.62, 59.31], +0.722 ± 0.397 | R5 r2_duct_3p100.csv | ✓ |
| 4 | ISMCTS × 2, n = 300, 54.2 [48.5, 59.7], +1.66 ± 0.69 | 54.17 [48.51, 59.72], +1.663 ± 0.694 | R5 v5_field_3p100.csv | ✓ |
| 5 | BasicMCTS, RHEA 84.2 [80.7, 87.1], +6.68 ± 0.60 | 84.20 [80.74, 87.13], +6.682 ± 0.603 | R4 B_mcts_rhea.csv | ✓ |
| 6 | OSLA, BasicMCTS 87.2 [84.0, 89.8], +9.74 ± 0.75 | 87.20 [83.99, 89.85 → 89.847], +9.744 ± 0.754 | R4 B_osla_mcts.csv | ✓ |
| **IV-A** | | | | |
| 7 | 55.0 % at 100 ms (seed 7) | 54.97 % | R4 V_3p100.csv | ✓ |
| 8 | 58.1 % (re-run) | 58.10 % | R5 v5_base3p100.csv | ✓ |
| 9 | 2.4 ± 6.1 pp more, not significant | +2.37 ± 6.14 (z 0.76), two-sample | V_3p100 vs r2_val_3p100 | ✓ |
| 10 | 5.5 ± 6.1 pp more, not significant | +5.50 ± 6.09 (z 1.77) | v5_base vs r2_val_3p100 | ✓ |
| 11 | DUCT 21.0 % of seats, "the same share as v2" | 21.0 % and 21.0 % | r2_duct_3p100.log, r2_val_3p100.log | ✓ |
| 12 | BasicMCTS/RHEA/OSLA "at most 12.8 %", OSLA none | 9.0, 6.8; 12.8, 0.0 | B_mcts_rhea / B_osla_mcts logs | ✓ |
| **IV-B, Fig. 2 top** (paired with V_3p100) | | | | |
| 13 | −H −28.3 ± 5.8 pp (z 9.6); margin −5.45 ± 0.71 | −28.27 ± 5.79 (z 9.57); −5.45 ± 0.71 | A_noH.csv | ✓ |
| 14 | −S−I 10.2 ± 5.7 (z 3.5); margin −1.75 ± 0.58 | −10.17 ± 5.75 (z 3.47); −1.75 ± 0.58 | A_noSI.csv | ✓ |
| 15 | −R z = 2.0; margin −0.74 ± 0.61 | −6.17 ± 5.94 (z 2.04); −0.74 ± 0.61 | A_noR.csv | ✓ |
| 16 | −B z = 1.4; margin −0.98 ± 0.57 | −4.27 ± 5.83 (z 1.43); −0.98 ± 0.57 | A_noB.csv | ✓ |
| 17 | −I z = 1.3; "I adds only 3.8 ± 5.7" | −3.77 ± 5.68 (z 1.30); margin −0.63 ± 0.55 | A_noI.csv | ✓ |
| 18 | Holm on wins: only H and S + I survive | p = 1e−21, 5.3e−4 pass; R p = 0.042 > 0.0167 fails | computed | ✓ |
| 19 | Holm on margin: all five survive | B p 7.7e−4 < 0.0167; R 0.0164 < 0.025; I 0.0250 < 0.05 (just) | computed | ✓ |
| 20 | S alone +6.4 ± 5.7 (z 2.2) | +6.40 ± 5.70 (z 2.20) | A_noSI → A_noI | ✓ |
| 21 | Lead heuristic −13.6 ± 5.9 (z 4.5); "roughly half" | −13.60 ± 5.88 (z 4.54); 48 % of 28.3 | r2_lead_3p100 vs v5_base | ✓ |
| 22 | Factorial R +8.7 (z 8.1), K +5.2, M +4.1, T +3.6, each ±2.1 | +8.65 (z 8.1), +5.15, +4.10, +3.65, ±2.09 | 16 cells in R3 (s2_*, d_*, s3/s4_*, abl_*) | ✓ (rounding) |
| 23 | R×T −4.2, M×K +3.2 the only interactions with \|z\| > 2 | −4.22 (z −3.96), +3.17 (z +2.97); others \|z\| ≤ 1.1 | same | ✓ |
| 24 | X3 −1.3 ± 5.8; N −1.8, V2D −0.6, W1 +0.7 (each ±5.8) | −1.30 ± 5.84; −1.80 ± 5.79; −0.60 ± 5.83; +0.70 ± 5.78 | v5_*_3p100 vs v5_base | ✓ |
| 25 | Game-end rollouts −23.7 ± 5.8 (z 8.0), the only one surviving Holm | −23.70 ± 5.81 (z 8.00); next p = 0.54 | v5_R3V2_3p100 | ✓ |
| 26 | X3 wins 45.0 % of greedy games vs two X0 | 45.00 % (traced, not recomputable without the Java tool) | greedy_duels.txt | ✓ traced |
| **IV-C, Fig. 3** | | | | |
| 27 | v2 24.8 % → v3 50.1 % at 100 ms | 24.80 → 50.10 | s1_ref, abl_T4 | ✓ |
| 28 | v3 lost 17.3 ± 7.2 pp from 100 to 900 ms | −17.30 ± 7.22 (two-sample) | abl_T4 → f9_finalT4 | ✓ |
| 29 | Final agent lost 8.1 ± 7.5 pp | −8.10 ± 7.52 | d1_I → f9_I | ✓ |
| 30 | S + I gain +5.6 ± 6.1 → +14.8 ± 8.4; rise +9.2 ± 10.4 (z 1.7) | +5.60 ± 6.13; +14.80 ± 8.42; +9.20 ± 10.42 (z 1.73) | same four runs | ✓ |
| 31 | Seed 7: 55.0, 49.4, 56.0 % at 100/500/900 ms | 54.97, 49.40, 56.00 (n = 500, 500, **250**) | V_3p100/500/900 | ✓ |
| 32 | ISMCTS seats 23.1 → 37.4 % (vs final); 33.0 → 46.0 % (vs v3) | 23.1, 37.4; 33.0, 46.0 | d1_I, f9_I, abl_T4, f9_finalT4 logs | ✓ |
| 33 | Fig. 3 points | v2 24.8/18.3/18.8; v3 50.1/40.7/32.8; v4 55.7/49.7/47.6; held-out 52.6 | s1/f5/f9 runs, r2_val_3p100 | ✓ |
| **IV-D** | | | | |
| 34 | 3-player config with four players: 46.4, 43.5 (100 ms), 38.4 % (500 ms) | 46.43, 43.50, 38.40 | q4_I, q4_Io, val_q5 | ✓ |
| 35 | X0 +4.3 ± 4.3 (pooled) | +4.27 ± 4.32 (z 1.94) | q4_I + q4_Io vs q4_X0 + q4_X0c | ✓ |
| 36 | X0 + W2 + K 1: +5.8 ± 7.4 at 500 ms | +5.80 ± 7.39 (z 1.54) | val_q5 vs q5_W2 + q5_W2r | ✓ |
| 37 | F + L a further +1.0 ± 7.9 | +1.00 ± 7.86 (paired) | q5_W2r → q5_FL8 | ✓ |
| 38 | F + L cost 6.6 ± 6.0 at 100 ms | −6.57 ± 5.96 (z 2.16) | q4_X0c → q4_X0FL8 | ✓ |
| 39 | 3 players: cost 7.7 ± 5.4 (z 2.8); differs from four players by 13.5 ± 9.1 (z 2.9) | −7.70 ± 5.39 (z 2.80); +13.50 ± 9.14 (z 2.89) | f5_FL8 → f5_W2FL8 | ✓ |
| 40 | Depth 1 −17.4 ± 8.2; 1 000 ms (K 1.37) −4.8 ± 8.1 | −17.40 ± 8.19; −4.80 ± 8.09; K = 1.37 in the JSON | q5_FL8d1, q5_FL8t1000K14 | ✓ |
| 41 | 73 positions, 704 root children, 53 % visited once, depth 1.5 | 73 / 704 / 1:53 % / 1.53 | treestats4.txt | ✓ traced |
| 42 | Changed card in 2 and 4 positions; re-run 4, other seed 7 | 97.3 % → 2.0; 94.5 % → 4.0; 94.5 % → 4.0; 90.4 % → 7.0 of 73 | agree4.txt (`Agree4.java` semantics checked) | ✓ |
| **Method / setup** | | | | |
| 43 | Wilson half-width ±4.4 pp at p = .5, n = 500 | 4.37 | formula | ✓ |
| 44 | Paired 500-game differences about ±5.7 pp | 5.68–5.94 observed | all seed-7 pairs | ✓ |
| 45 | Win correlation on a deal 0.0–0.2 | 0.002–0.220 over 15 pairs | computed | ✓ (one at 0.22) |
| 46 | Score margin "1.5–2 times as sensitive in z" | \|z_m / z_w\| for clear effects: 1.04 (lead), 1.18 (−R), 1.36, 1.48, 1.56, 1.70, 1.73, 2.34 (−B) | computed | ≈ (range too narrow) |
| 47 | 45–70 s of CPU per long game | decisions × budget: 45 s (4p/500), 70 s (3p/900) | nDecisions in CSVs | ✓ |
| 48 | Table I constants (X0 and defaults), K, ε, σ, w, α, T, π, ν, L, K = 1.37 | match JSON and `CardValueModel` constants | competition and final JSON | ✓ |
| 49 | Table II opponent settings, competition budgets (900/925 ms, 430/450 ms) | match TAG `SushiGo_3+P_HiBudget.json` (budget 400, MultiTree, UCB_Tuned, K 1, RANDOM, ScoreHeuristic), `SushiGoDUCT.json`, `fw_*.json` | TAG json, arena agents | ✓ |
| 50 | 108 cards; 12 − n cards; scoring values; 729 / 4 096 joint moves | `SGParameters`, `SGForwardModel` (nCards − n + 2); 9³, 8⁴ | TAG source | ✓ |
| 51 | Abstract 52.6 %, 44.4 %, 7.7 pp | as above | | ✓ |

**Mismatches:** no numerical errors. Small inaccuracies:

* (a) "1.5–2 times as sensitive in z" overstates how uniform the ratio is. Observed ratios run from 1.0 to 2.3.
* (b) Table II "Games: 500 per condition; 250 in development runs at 900 ms and with four players at 500 ms" leaves out two runs. One is the 250-game seed-7 validation at 900 ms (V_3p900, quoted as "56.0 %" in IV-C, and not a development run). The other is the 300-game ISMCTS × 2 run. The Introduction's "(250 in some long-budget development runs)" has the same gap.
* (c) "development s = 1, 2 (v3 also 1001–3001)" should read 1001, 2001, 3001.

**Numbers I could not trace to a data file:** none. Hardware and dates ("18-core laptop", "4-core machine", "19 October") come from the experiment READMEs. The 45.0 % greedy result and the 73-position diagnostics come from tool outputs (txt). They are traceable but cannot be recomputed without re-running the Java tools.

**The scripts' computations** (`build_report.py`, `make_figures.py`) are correct: Wilson, the n − 1 SE, the per-game paired differences, the two-sample SE over pooled runs, the difference of independent estimates, and the paired difference-of-differences over four runs on the same deals. Three minor points:

* Pooling q4_Io with q4_X0c treats runs on the same canonical deals as independent. That is conservative.
* Several results are computed but never used: `DEVD['s100'/'s500'/'srise']`, `FOUR['b100']`, `ITER`, `NRUNS/NGAMES` (162 runs, 69 350 games). Some of them would strengthen the analysis (§6, S2–S3).
* `_rise_phrase()` is dead code.

---

## 3. Technical correctness

### 3.1 Method vs code

| Report statement | Code | Verdict |
|---|---|---|
| Copy of BasicMCTS; no code from `players.mcts` | No `players.mcts` imports in the 7 files; the structure follows BasicTreeNode/BasicMCTSPlayer | ✓ |
| Eq. (1) Ŝ = S + αU + M + π w_t P; w_t = t/3; completion min(1, (r − m + 1)/T), 0 if r < m; Wasabi 2ν, Chopsticks 1 | `CardValueModel.estimatedScore`, `potential`, `completion`; puddingWeight = (round + 1)/nRounds | ✓ |
| Eqs. (2)–(3), σ 15, w 0.7 | `SushiGoHeuristic.valuesOf` | ✓ |
| mv = change of Ŝ when the card is played | `marginalValue` (immediate points + Δ potential + Δ Maki + Δ Pudding, with r − 1 picks on both sides) | ✓ (simplified) |
| B: greedy on mv, ε = 0.2, every player; Chopsticks second card the same way | `rolloutCard`, `playOut`, `chopsticksValue` | ✓ |
| R: rollout ends when the round is scored | `finishRollout` | ✓ |
| P: one branch per distinct card | `distinctActions`; I statistics keyed by (type, icons, chopsticks) | ✓ |
| M: per-player values, each player maximises its own (vs paranoid) | `backUpAll`, `ucb()` with max-n | ✓ |
| S + I, eq. (4) with availability count N_p(a); untried first; joint choice → child | `selectInformationSet` / `selectSim` (`log(val[j+2])/val[j]`, availability incremented for every card held) | ✓ |
| I: new sample per iteration, open-loop nodes with statistics only | `informationSetIteration` (`state.copy(me)`), stateless child constructor | ✓ |
| Most-visited root card | `bestAction()` | ✓ |
| D: up to 4 determinised trees, split budget | `numberOfDeterminisations` (adaptive, max 4) | ✓ |
| Four players: X0, and from 500 ms W2 (w = 1, terminal tanh(·/2) with Pudding tie-break, σ·(R/2)^0.5, opponents valuing their lead over the mean), F, L = 8, K = 1 | Competition JSON `fourPlayer` block; `terminalValues`, `roundScale`, `scoreValue`, `simLeafValues` | ✓ (depends on the config file, not automatic by budget) |
| F checked move by move against the forward model | `SushiSimCheck` exists in `experiments_v4/dev` (no stored output) | ✓ (claim plausible) |
| −H uses "own score / 50" | `a4_noH.json` sets no heuristic, so `SGGameState._getHeuristicScore` = score / 50 | ✓ |
| Lead heuristic, K 7.5 | `b100_leadK75.json`, `LeaderHeuristic` (own score minus best other) | ✓ |
| OSLA "never reaches the simultaneous reveal" | `OSLAPlayer` applies one action to its observation, in which `redeterminise` hides the other players' choices, so all actions tie and it plays at random | ✓ (good explanation) |

Not described in the paper: the second Chopsticks pick at the root uses the closed-loop tree, and the 900 ms benchmark file's `fourPlayer` block lacks F + L, unlike the competition file. Neither affects a reported result.

### 3.2 Literature claims (checked against the abstracts or the papers' known content)

| Ref | Claim in report | Verdict |
|---|---|---|
| [4] Klang et al. 2021 | Implemented Sushi Go! in TAG; studied simultaneous action selection and complete information; concluded better heuristics should help | ✓ (matches their abstract) |
| [7] Long et al. 2010 | PIMC success related to properties including how fast hidden information is revealed | ✓ (the disambiguation factor) |
| [8] Frank & Basin 1998 | Strategy fusion and non-locality | ✓ |
| [3] Cowling et al. 2012 | ISMCTS; SO vs MO (one tree per player) | ✓ |
| [9] Goodman et al. 2022 | MultiTree MCTS in TAG; pp. 292–299 | ✓ |
| [10] Lanctot et al. 2013/14 | DUCT; compared with EXP3 and RM in Goofspiel | ✓ but incomplete: it omits their finding that RM and OOS did best and that every variant was less exploitable than UCT |
| [11] Tak et al. 2014 | Nine games; best variant depends on game and opponent; DUCT best overall; pp. 232–239 | ✓ |
| [12] Sturtevant 2008 | Multi-player UCT converges to a mixed-strategy equilibrium and performs at least as well as earlier algorithms | ✓ |
| [6] Silver & Tesauro 2009 | A stronger policy is not necessarily better; balance matters | ✓ (reasonable summary) |
| [13] Robilliard et al. 2014 | MCTS applied to 7 Wonders; CCIS 504 pp. 64–77 | ✓ (what they found is not reported) |
| [5], [2], [1], [14] | UCT; survey; TAG; Wilson | ✓ |

No misreported sources were found. Plagiarism and misconduct risk from the literature summaries is low.

### 3.3 Are the conclusions supported by the statistics?

1. **"The card-value model and decoupled selection matter most"** (abstract) and **"decoupled selection (6.4 pp over v3's sequential tree) contribute[s] most"** (conclusions).
   * S alone gains +6.4 ± 5.7 (z = 2.2, unadjusted, measured on v3's base). Removing R costs −6.2 ± 5.9 (z = 2.0). The factorial gives R a main effect of +8.7 pp on v3. The ranking of S over R is not supported.
   * Holm is also applied unevenly: R's z = 2.0 is dismissed by the correction, while S alone (z = 2.2, which would fail Holm inside the ablation family) is used as a headline.
   * Only H and S + I survive Holm on the win rate. That is the defensible statement.
2. **"In a drafting game the simultaneous moves matter more to the search than the hidden hands"** (Introduction). The S-versus-I difference (6.4 vs 3.8 on seed 7) is never tested. In development at 100 ms the order reverses: S +1.3 ± 6.1, then I +4.3 ± 6.1. I does not add "handling hidden hands" from nothing; it replaces determinisation D, so the claim confuses *how* hidden cards are sampled with *whether* they matter.
3. **v3's budget decline.**
   * "We attribute v3's decline to its sequential tree" is a hypothesis stated as a conclusion.
   * More importantly, v3's 100 ms development point (50.1 %) **is the very run that won the 16-cell factorial**, so it is the maximum of 16 noisy estimates (winner's curse). On fresh deals v3 won 49.4, 47.5, 45.1 % (seeds 1001, 2001, 3001) and 44.8 % (seed 7). With a baseline of about 46.7 %, the development "rise" shrinks from +9.2 to about +6 pp (z ≈ 1.2). The seed-7 S + I gain at 100 ms (10.2 pp) already exceeds the "development" gain at 100 ms (5.6 pp), and the paper does not reconcile the two.
   * Supporting evidence that the paper computes but does not report: S alone gives +1.3 at 100 ms and +6.2 ± 6.1 at 500 ms in development, and v3 and v4 run similar iteration counts (11 170 vs 11 865 at 500 ms). Both point to the tree structure rather than to search speed.
4. **H3 / "best configuration depends on the table size"** (abstract). The 7.7 pp cost is a single contrast that the body calls exploratory and chosen after the four-player runs. It compares effects on different bases: with F + L at 3 players and 500 ms, against without F + L at 4 players. The body says "H3 holds only one-sidedly", which the abstract drops. The text never says the 7.7 pp was measured at 500 ms. A cleaner exploratory contrast is in the data and unused: X0 alone at 100 ms gives +4.3 with four players and −4.9 ± 6.1 with three (interaction ≈ 9.2 ± 7.5, z ≈ 2.4).
5. **Crediting "the card-value model".** Under R every leaf is at a round end, where U_p = M_p = 0. H's leaf value therefore reduces to tanh of the w-weighted lead in banked score plus the Pudding standing. The −H and lead-heuristic ablations mainly test the shape of the value function (lead over best and mean, Pudding standing, bounded scale), as IV-B's own list "(Pudding standing, mean-opponent term, bounded scale)" admits. The model's set and Maki terms act only through B, and −B is −4.3 ± 5.8 (not significant). The abstract and conclusions should credit "the evaluation H", not "the card-value model".
6. **H2 is presented as an a-priori expectation** ("We expected … (H2)"), but it came from the development curves. Say so; the held-out test is then the real test.
7. **Power.**
   * The null results ("no detectable gain", ±5.8 pp) give no minimum detectable effect. Roughly 8 pp is detectable at 80 % power for a paired 500-game comparison.
   * The held-out H2 test (difference of differences over four 500-game runs, 100 vs 500 ms) has SE ≈ 4.5 pp. It detects only changes of about 12 pp or more, against a development estimate of about +3 pp from 100 to 500 ms. Say this, or a null held-out result will read as evidence against H2.
8. **Four-player settings adopted on non-significant evidence.** X0 is z = 1.94, X0 + W2 + K 1 is z = 1.5, F + L is z = 0.25. About 57 four-player runs were made with no multiplicity control, which is acceptable only because they are labelled exploratory. State how many variants were tried.
9. **Selective reporting.** Mild. The omissions are mostly unfavourable or neutral results that are visible elsewhere:
   * S at 100 ms in development (+1.3);
   * the four-player 100 → 500 ms gain, −9.0 ± 7.7 for the 100 ms agent at 100 ms (z = 2.3), which would usefully qualify "extra time hardly does";
   * the seed-7 four-player validation (46.8 %, 41.4 %);
   * the 4p X3 test (+2.0 ± 5.7);
   * about 20 further four-player variants.

   None changes a conclusion. Adding two of them (S2) would strengthen RQ2 and RQ3.

### 3.4 Conditional wording for the pending results (`build_report.py`)

| Location | Outcome | Current wording | Verdict |
|---|---|---|---|
| `text_budget`, verdict | RISE z ≥ 1.96 / > 0 n.s. / ≤ 0 | "which supports H2" / "in the direction of H2 but not significant" / "so H2 is not supported on these deals" | OK, except a **significant negative** RISE should read "which contradicts H2". |
| `text_budget` | Gains negative | "so S and I gained −x ± y pp and …" | Awkward. Use "changed v3's win rate by". |
| `text_budget`, BUD sentence | 500 + 900 done but no v3 runs | "The final agent's own win rate **there** …" | "there" would then refer to seed block 7. The queue order makes this unlikely, but replace with "on the held-out deals". |
| `text_conclusion_1`, else branch | RISE ≤ 0 | "although v3 lost **far more** than the final agent from 100 to 900 ms in development" | **Wrong.** The paper itself reports that difference as not significant (z = 1.7), and it is selection-biased. Must hedge. |
| `text_conclusion_1` | RISE > 0 n.s. | "as in development but not significantly" | OK. Add the power caveat. |
| `_abstract_end` | RISE z ≥ 1.96 | "…matter most and that the advantage of **decoupled selection** grows with the time budget" | Misattributes. RISE measures S + I against v3 at 100 vs 500 ms, it is not an ablation, and the budget range is unstated. |
| `text_final_agent` | Any | Neutral listing of win rates | ✓ |
| Fig. 3 | Any | Adds open markers and a "v3, held-out" legend entry | ✓ |

An interim result, for information only and not penalised: chunks c0 and c1 of the held-out 3p/500 run (in `TAG/arena/v5/results`, not yet synced to `groupA_code`, 252 games) give 50.6 %. Paired against 100 ms on the same deals that is +0.6 ± 8.0 pp, so the BUD branch will most likely print "did not change significantly".

---

## 4. Compliance

| Requirement | Status |
|---|---|
| 4 pages of main text; only the AI statement and references on page 5 | ✓ Main text ends about 57 % down the right column of page 4. About 25 free lines remain, about 16 after the roughly 8 pending lines. Page 5 is about half used. |
| Template | ✓ The IEEE A4 template: margins 44.65/27/54/72 pt, two columns 18 pt apart, Times 10 pt body, 9 pt abstract, 8 pt captions, tables and references. ⚠ The PDF embeds **Liberation Serif** (metric-compatible substitute) instead of Times New Roman. Export from Word or a machine with TNR. |
| Structure | ✓ Abstract, Keywords, I–V, AI statement, References. |
| Word guide | Abstract 124 (75–125) ✓; Introduction 453 (350–450) ≈; Background 654 (650–850) ✓ (low end); Method 834 + tables (700–800) ≈; **Results 943 (500–700), rising to ≈ 1 050 with the pending text** (over); Conclusions 228 (200–300) ✓. |
| References 6–15, one style | ✓ 15, IEEE, consistent. Add a version and access date to [15]. Citing [15] in the page-1 footnote breaks citation-order numbering (nit). |
| ≥ 500 games or justification | ✓ Mostly, and the 250-game runs are justified (45–70 s CPU per game). ⚠ **ISMCTS × 2 has 300 games with no justification.** The continuation `r2_field_3p100b` is queued; make sure it finishes, so the row reads n = 500. Otherwise justify it. The rubric deems < 500 games per pairing insufficient. |
| Experiments at 500 ms | ✓ (V_3p500, f5_*, q5_*, held-out pending). |
| Generative-AI rules | ✓ Acknowledged, before the references, with Claude cited [15] and the main prompts quoted. ⚠ (i) A single page-1 footnote stands in for per-section citations ("Any sections … shall have a citation"). It probably satisfies the rule because it says "All sections", but it is the minimum. (ii) **The prompts for the revisions are not included.** "Its three written reports (archived with our records) were the prompts for the revisions" means those prompts are summarised, not given. Include them in the submission archive and say so. (iii) The count "three" will be out of date once this review is used. (iv) The very candid prompts ("a report that gets 100") are required disclosure, but they invite closer scrutiny. |
| Code package | ✓ `groupA`, README with constructor and JSON usage, competition JSON for 1 s/3 players and 500 ms/4 players, comments explaining the critical parts. ⚠ The README opens with "Rename first: replace every groupA … with your real group letter". Confirm the group really is A; otherwise rename the package, JSON class names, report metadata and filename. |
| Brief's ban on using or copying `players.mcts` | ✓ Not imported. TAG ISMCTS is used only as an opponent. |

---

## 5. Presentation

* **Notation reuse (should fix).** In eq. (1) S_p (banked score), M_p (Maki) and P_p (Pudding) share letters with modifications S, M and P. III-D uses "R rounds left" next to modification R. In Fig. 1, P1–P3 are players. Use lowercase symbols (s_p, u_p, m_p, d_p) and ρ for rounds left.
* **Internal or course jargon to remove or define:**
  * "v5" (Table II, defined nowhere);
  * "last study" (Fig. 2 caption, IV-B);
  * "before the in-class tournament", "competition files", "TAG as distributed with the module" (the tips say not to mention the assignment);
  * "seed block";
  * "the machine of the held-out runs" / "the 4-core machine" used as a run identifier;
  * "H3 holds only one-sidedly";
  * "1.5–2 times as sensitive in z".
* **Density.** IV-B's last-study sentence packs 11 numbers. The III-A definition of U_p has three nested parentheticals. III-D is one 70-word sentence joined by dashes. IV-D is a run of figures with little interpretation. Shorter sentences and one interpretive sentence per paragraph would raise Clarity more than anything else.
* **Figures.**
  * Fig. 1 is excellent and conceptual.
  * Fig. 2 is good. Label −R as "(10-move rollouts)" and −B as "(random rollouts)", and mark Holm-significant effects (e.g. filled vs open markers).
  * Fig. 3: the legend order is v2 / final (v4) / final, held-out / v3. Order it v2, v3, final, held-out. The dev 900 ms points are 250 games; the caption could say so.
  * Fonts are 7–7.5 pt at column width: legible but at the lower limit.
* **Tables.**
  * Table III's "Seed block 100 000, a 7" is cryptic. Use "Held-out seed block 100 000 unless marked a (seed block 7)". Write "Margin (points)".
  * Table II's Opponents cell is long; it can be compressed (see the cuts in §6).
* **Consistency.** *Sushi Go!* is italic only once. "final agent / v4 / SushiMCTS" are three names for one agent (defined, but use one). "(rise +9.2 ± 10.4 pp (z = 1.7))" has double parentheses.
* **Flow.** The Results subsections map onto RQ1–RQ3 and the conclusions follow the RQs. Good. But each Results subsection lacks a closing "what this means" sentence; most interpretation sits in subordinate clauses.

---

## 6. Prioritised changes

Line budget: after the pending text about 16 free lines remain in the right column of page 4. The **must** items add about 5 lines net, and the proposed cuts (C1–C4) free about 7. The page-5 material (AI statement and references) can grow by up to about 30 lines and still fit.

### Must fix

**M1. Conditional wording** (Critical analysis, Conclusions). In `build_report.py`:
* `text_conclusion_1`, else branch: replace `"although v3 lost far more than the final agent from 100 to 900 ms in development."` with `"although in development v3 had lost more than the final agent from 100 to 900 ms (a difference that was not significant)."`
* `text_budget` verdict: add a first case `"which contradicts H2" if RISE["zw"] <= -1.96`. Replace `"so S and I gained {…} and {…}"` with `"so S and I changed the win rate by {…} and {…}"`.
* BUD sentence: replace `there` with `on the held-out deals`.
* `_abstract_end()` significant branch: replace `"and that the advantage of decoupled selection grows with the time budget."` with `"and that the advantage of the decoupled information-set tree over v3 grows from 100 to 500 ms."`
* When RISE is not significant, append to the held-out H2 sentence: `" (this test detects only changes above about 12 pp)"`.

**M2. Remove the overclaims** (Abstract, Contributions, Critical analysis, Conclusions).
* *Abstract.* Replace it with the following 125-word version:
  > "In Sushi Go!, players pick cards simultaneously from hidden hands that are passed around, which basic Monte Carlo tree search (MCTS) ignores. We extend BasicMCTS of the Tabletop Games (TAG) framework with a card-value model for evaluation and rollouts, round-end rollouts, max-n backups, decoupled selection for simultaneous moves and information-set search. On held-out deals the agent wins 52.6%, X% and Y% of 500 three-player games at 100, 500 and 900 ms against TAG's information-set MCTS and our previous agent (chance 33.3%), and 44.4% of four-player games at 100 ms (chance 25%). In ablations only the evaluation function and the decoupled information-set tree raise the win rate significantly after correction (by 13.6 and 10.2 percentage points); four-player settings cost 7.7 percentage points with three players (exploratory)."

  In the RISE-significant case, the final clause becomes the M1 clause.
* *Introduction.* Delete "Together they suggest that in a drafting game the simultaneous moves matter more to the search than the hidden hands." (saves about 2.5 lines).
* *Conclusions.* Replace "For RQ1, the card-value heuristic (13.6 pp over TAG's lead heuristic, 28.3 pp over its default) and decoupled selection (6.4 pp over v3's sequential tree) contribute most." with:
  > "For RQ1, every component improves the score margin, but only the evaluation H (13.6 pp over TAG's lead heuristic) and the decoupled information-set tree (10.2 pp over v3's sequential, determinised tree) raise the win rate significantly after a Holm correction."

**M3. v3's decline: hedge it and correct for selection** (Critical analysis, Results IV-C). Replace "We attribute v3's decline to its sequential tree, in which later players react to cards chosen earlier in the same turn: a wrong model that more iterations exploit more." with:
> "We hypothesise that v3's decline comes from its sequential tree, in which later players react to cards chosen earlier in the same turn – a wrong model that more iterations exploit more. v3's 50.1% at 100 ms was the best of 16 factorial cells in development (44.8–49.4% on new deals), so this development rise is probably overstated."

This adds about 1.5 lines. In III-E, add "(H2 arose from the development runs; the held-out runs test it)" after H2.

**M4. Undefined and out-of-scope terms; the Games row** (Documentation, Clarity).
* Table II, Seeds: replace with "development s = 1, 2 (v3 also 1001, 2001, 3001); ablation and final candidate tests s = 7; held-out s = 100 000, disjoint from all other deals".
* Table II, Games: replace with "500 per condition; 250 at 900 ms (development and seed-7 validation) and in four-player development at 500 ms (45–70 s of CPU time per game)". If the ISMCTS × 2 continuation does not finish, append "; 300 in the ISMCTS × 2 run".
* Fig. 2 caption: "the changes of our last study" → "five later candidate changes".
* IV-B: "The five changes of our last study" → "Five later candidate changes".
* Introduction: "(250 in some long-budget development runs)" → "(250 in some runs at 900 ms and in four-player development at 500 ms)".

**M5. Make sure the ISMCTS × 2 run reaches 500 games** (Rigour; rubric < 500 rule). `r2_field_3p100b` is queued. If it cannot finish, add a one-clause justification in the Table III note.

**M6. State the condition of the 7.7 pp result** (Critical analysis). In IV-D, "With three players, X0, W2 and K = 1 cost 7.7 ± 5.4 pp" → "With three players at 500 ms (both arms with F + L), X0, W2 and K = 1 cost 7.7 ± 5.4 pp".

**M7. Generative-AI statement** (compliance; risk of losing marks).
* "Its three written reports (archived with our records)" → "Its four written reports (included in the submitted archive)". Then actually include them, since they were prompts.
* [15] → "Anthropic, "Claude" [Large language model; give the model name/version], used through Claude Code, Oct. 2026. [Online]. Available: https://claude.ai (accessed Oct. 2026)."

### Should fix

* **S1. Notation** (Clarity, Approach). In eq. (1) and the text: Ŝ_p = s_p + α u_p + m_p + π w_t d_p, with s banked, u unfinished, m Maki, d Pudding. In III-D: "σ multiplied by (ρ/2)^0.5 for ρ rounds left". No line cost.
* **S2. Report the computed but unused evidence** (Critical analysis, RQ2/RQ3; about 2 lines).
  * After the M3 sentence, add: "S alone gained +1.3 ± 6.1 pp at 100 ms but +6.2 ± 6.1 pp at 500 ms, and both trees run similar numbers of iterations (11 170 vs 11 865 at 500 ms)."
  * In IV-D, before "but extra time hardly does", add: "giving our agent 100 instead of 500 ms cost 9.0 ± 7.7 pp (z = 2.3, with its 100 ms settings)". Then the claim becomes "time helps up to 500 ms but not beyond".
* **S3. Size of the search space of experiments** (Documentation, multiple comparisons; 1 line). In III-E add: "The study comprises 162 runs (69 350 games), 57 of them with four players; the list is in the README."
* **S4. Explain why T = 4 was kept** (Clarity; 1 line). Replace "(−4.2; T = 4 was kept)" with "(−4.2: with R, T = 8 no longer helps; T = 4 won on all three seed sets of a tie-break, +3.9 ± 3.4 pp pooled)". The source is experiments_v3 RESULTS (seeds 1, 2001 and 3001, 1 500 games).
* **S5. Literature depth** (Literature; about 2 lines). In II-C, after the Lanctot sentence add: "there regret matching and online outcome sampling played best and UCB was the most exploitable". For [13] add what was found, e.g. "and found that many results known from classic games carried over". End II with a sharper gap: "none of these studies separates the contributions of decoupled selection, information sets and domain-specific evaluation in one agent or examines how they scale with time and player count".
* **S6. Power statement** (Error analysis; 1 line). In III-E: "a paired 500-game comparison detects a difference of about 8 pp with 80% power; smaller effects are reported as 'no detectable gain'".
* **S7. Credit the evaluation, not "the model"** (Technical precision; 1 line). In IV-B after the lead sentence add: "with round-end rollouts the leaves have no unfinished sets, so H differs from TAG's lead heuristic only through these three terms; the card values themselves act through the rollout policy B".
* **S8. Fig. 2 labels.** "− round-end rollouts R (10-move rollouts)", "− informed rollouts B (random)".
* **S9. Availability of materials** (Documentation). Replace "Agent files, per-game results and scripts: from the authors" with "agent files, per-game results and scripts: [archive/link]".
* **S10. Mention the existing opponent model** (Future work). Append to the last sentence: "a first online version (switch Q in the code) was implemented but not evaluated in games".
* **S11. Consistency of the validation summary** (Results IV-A). Either give the seed-7 four-player values (46.8 %, 41.4 %) next to the three-player ones, or drop the seed-7 list to one sentence.
* **S12. Replace the vague sensitivity claim.** "the score margin, 1.5–2 times as sensitive in z" → "the score margin, whose z values were typically 1.2–2.3 times larger".

### Nice to have

* N1. Fig. 3: reorder the legend; note "900 ms: 250 games". Fig. 2: mark Holm-significant effects. Table III: "Margin (points)" and the clearer seed note.
* N2. Make *Sushi Go!* italics consistent; use one name for the final agent; remove the double parentheses in IV-C.
* N3. Export the PDF with Times New Roman embedded.
* N4. Code clarity: add a short "final-agent path" comment at the top of `SushiTreeNode` (`informationSetIteration` / `simIteration` → `selectInformationSet` → `playOut` → `valuesOf`). Consider moving tested-off switches (U, G, E, K, Q) out of the submission.
* N5. Keep the README's experiment narrative in `experiments_*/`, and leave the package README as run instructions plus the configuration table.
* N6. Add the Pudding tie-break to the rules.

### Cuts to pay for the additions (about 7 lines)

* **C1.** Delete the Introduction's closing sentence (M2): −2.5 lines.
* **C2.** Shorten IV-B's last-study sentence to:
  > "Of five later candidate changes (Fig. 2, bottom), four changed the win rate by at most 1.8 pp (each ±5.8), including card constants tuned for three players (X3) that win 45.0% of greedy games against two X0 players; rollouts to the game end cost 23.7 ± 5.8 pp (z = 8.0), the only change that survives a Holm correction."

  Saves about 1.5 lines.
* **C3.** Table II Opponents: "TAG ISMCTS: SushiGo_3+P_HiBudget (set for 400 ms) at our budget: one tree per player [9], UCB-Tuned, K 1, random rollouts to the end, score heuristic." → "TAG ISMCTS: SushiGo_3+P_HiBudget at our budget (MultiTree [9], UCB-Tuned, K 1, random rollouts, score heuristic)." Saves about 1 line.
* **C4.** IV-D, last sentence: "On 73 positions from random games the 500 ms tree has 704 joint-move children at the root, 53% visited once (mean depth 1.5); doubling the time or the rollouts per leaf changed the card in 2 and 4 positions, no more than re-running the search (4)." Saves about 1 line; it drops the "another seed (7)" detail.

---

## 7. Plain answer

**Would it receive ≥ 85 % as it stands (pending numbers filled in)?** Probably not. My estimate is 78 % (73–85 %). The experiments and statistics are first-class and every number checks out. The writing is not: it is hard to read, and several headline sentences claim more than the statistics shown alongside them. A strict marker will mark Clarity, Contributions, Literature and Critical analysis down from the top of the band.

**What stands between it and 90 %+:**

1. **Readability.** Fewer internal codes, no reused letters, no course jargon, and one plain-language interpretive sentence per Results paragraph.
2. **Claims that match the evidence:**
   * no unsupported rankings (S vs R);
   * no untested "simultaneous > hidden" claim;
   * v3's decline framed as a hypothesis and corrected for the winner's-curse baseline;
   * H3 marked exploratory in the abstract;
   * the evaluation, not "the model", credited for the −H effect.
3. **A properly critical literature review.** Lanctot's RM/OOS result, what 7 Wonders MCTS found, and a sharper gap.
4. **Power and multiplicity made explicit.** Minimum detectable effects, the number of variants tried, and H2 declared post hoc and tested on held-out deals.
5. **Complete compliance:**
   * every run ≥ 500 games or justified;
   * the AI statement with every prompt (including the reviewer reports) and a versioned citation;
   * Times New Roman embedded;
   * the correct group letter.
6. **Luck in the pending runs.** A 90 % paper needs at least one clear held-out answer to RQ2. The planned held-out H2 test is underpowered (it detects only changes of about 12 pp). Whatever the outcome, the paper must say what the test could and could not detect.
