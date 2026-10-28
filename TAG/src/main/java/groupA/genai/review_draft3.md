<!-- Independent review of report draft 3 (27 October 2026), written by a separate Claude instance against the
     module rubric. Kept as the record of the revision prompts mentioned in the paper's generative-AI statement. -->

# Independent assessment: ECS7032P Assignment 1, Group A
"Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!" (PDF and DOCX of 27 Oct, 5 pages)

I assessed the report against the brief, the rubric, the weights and the writing tips. Then I checked it against the raw data, the TAG source and the submitted code. My recomputations come from my own scripts in `/tmp/claude-0/review3/` (`verify.py`, `factorial_check.py`, `misc.py`, `studysize.py`), which read the per-game CSVs, logs and text files directly. I did not use RESULTS.md as a source. I compiled the `groupA` package against TAG and played one smoke game with each competition file (3 players at 1 s, 4 players at 500 ms); both ran correctly. I did not open `report/reviews/`.

---

## 0. Verdict

* **Estimated mark as it stands, with the pending numbers filled in: about 75% (plausible range 70–80%).** That is a clear first-class / distinction report, but not an 85+ one.
* **What it does well (why it is not lower):**
  * The experimental work is unusually strong for this module: a leave-one-out ablation, a 2^4 factorial, a held-out seed block, paired deals, Wilson intervals, a Holm correction, and comparisons against five framework agents plus two earlier versions.
  * The Method section matches the code closely.
  * Every number I could recompute is correct.
* **What holds it back:**
  1. Clear errors a marker will notice: the figure references are swapped, one 4-player comparison is misdescribed, and Table III has a 300-game row with no justification.
  2. Selective reporting: the factorial's 4th factor and its largest interaction are omitted, and the completed seed-7 runs at 500/900 ms are not reported.
  3. Several confounded or over-interpreted comparisons.
  4. Very dense, code-heavy prose. Results is about 950 words against a 500–700 guide.
  5. Originality is moderate. The search side re-creates options TAG's own MCTS already has, and the report says so.
  6. Compliance gaps in the generative-AI statement: the revision prompts are not included.
  7. The code is bloated: about 3,400 lines and 20+ switches, most of them unused by the final agent.

---

## 1. Rubric marking

Bands are those of the rubric (70–100 / 50–69 / <50).

### 1) Abstract (weight 5%): **74**

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Brief motivation | 70–100 | 72 | Correct and succinct: simultaneous picks from hidden, passed hands are not modelled by basic MCTS. It does not say why this matters beyond the game (e.g. drafting games as a testbed). |
| Brief summary | 70–100 | 76 | All key components are named. "Scoring model" is a vague label for the card-value model. P (action pruning) and the separate 4-player configuration are not mentioned. |
| Outline of findings | 70–100 | 74 | The right numbers are chosen: held-out 3p/100, 3p/900, 4p, the main ablation result and the 3p/4p trade-off. Problems: "44.4% with four players" omits the budget (100 ms). It headlines a non-significant trend ("grew with the budget, though not significantly"). "Four-player settings cost 7.7 percentage points with three players" is opaque to a reader who has not read the paper. 124 words, at the limit of the 75–125 guide. |

### 2) Problem definition (weight 15%): **73.6**

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Motivation | 70–100 | 72 | Sound general motivation (chance, hidden information, several players; TAG as a common interface), with citations [1]–[4]. The drafting-game link (7 Wonders [13]) appears only in Background. |
| Clarity of the problem | 70–100 | 80 | The best part of the introduction. Four concrete properties (hidden hands; simultaneity; delayed, conditional points; joint-move width with 729 / 4 096 joint moves) are mapped to RQ1–RQ3 and to the components. |
| Contributions | 50–69 | 68 | Four specific bullets, but their importance to knowledge is never argued: what does a reader learn beyond this agent? Bullet 4's "(162 runs, 69 350 games)" is padding. "A diagnosis of why extra time helps little" overclaims: it rests on 73 positions with no uncertainty. |
| Critical analysis of the literature | 50–69 | 68 | Accurate and relevant: PIMC flaws [8], Long et al.'s disambiguation [7], SO- vs MO-ISMCTS [3], DUCT convergence [10], max-n [12], simulation balancing [6]. A gap is stated. But it is mostly descriptive. Tak et al. [11] is cited selectively (see 3b). Klang et al.'s findings, which suggested better heuristics, are not discussed, although they motivate H directly. |
| Description of Sushi Go! | 70–100 | 80 | Complete and correct card values (checked against `SGParameters`). It gives the 12 − n hand size and explains why information is concentrated at the start of each round. |

### 3) Approach: Method + code (weight 30%): **76.7**

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Understanding and application | 70–100 | 77 | Correct implementation of DUCT with availability-count UCB, SO-ISMCTS on an open-loop tree, max-n, ε-greedy model rollouts to round end, and a verified fast simulator. This goes well beyond the lectures. The Method text matches the code (Section 3a), and the package compiles and runs. Deductions for code clarity: SushiTreeNode has 1,753 lines (BasicTreeNode has 316) and four separate iteration routines. About 10 switches are inactive in the final agent (G, U, Q, O, K, E, N, …), OpponentModel.java is unused, and 70 JSON files are mostly obsolete. Small omissions in the description: eq. (1) lacks π; T_m = 0 means "off"; the tree picks the second Chopsticks card greedily; M replaces BasicMCTS's paranoid backup. |
| Originality | 50–69/70 | 70 | The authors' own element is the card-value model shared by evaluation and every player's rollouts, plus per-table-size settings. The search components are literature techniques that TAG's MCTSPlayer already offers (information sets, MultiTree, `decoupled`), which the report states openly. The value lies in the measurement more than in new ideas. |
| Difficulty and ambition | 70–100 | 83 | Advanced tree search with substantial development: a re-implemented rules engine, parameter switching by table size, timing guards. |

### 4) Experimentation (weight 30%): **76.7**

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Adequacy and rigour | 70–100 | 78 | Against five framework agents (BasicMCTS, RHEA, OSLA, ISMCTS, DUCT) plus v2/v3. Leave-one-out ablation, a 2^4 factorial (on v3), a held-out seed block, paired deals, 100/500/900 ms, 3 and 4 players. Deductions: a 300-game row with no justification; 250-game runs behind RQ2/RQ3; development comparisons on independent deals; several confounded comparisons (3c); RQ3 is exploratory over 57 four-player runs. |
| Documentation | 70–100 | 78 | Table II plus Table I's constants are close to sufficient for replication. Small inaccuracies: "since v4" (canonical deals actually started mid-v4); "48–73 s" (actual about 45–70 s); the harness is described only, and the records are "available from the authors". |
| Critical analysis | 70–100 | 74 | Stated hypotheses H1–H3, good explanations (OSLA never reaches the reveal; disambiguation for I; the opponent's scaling with budget) and proper error analysis. Deductions: selective reporting (the factorial's T and R × T; the seed-7 500/900 ms runs; last-study margins); a misdescribed "100 ms" comparison; null results phrased as "did not help"; ratio claims with no uncertainty; 73-position diagnostics given without CIs; an unreconciled tension between the depth-1 loss (−17.4 pp) and "time rarely changes the decision"; H3 is post hoc. |

### 5) Conclusions (weight 10%): **73.5**

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Summary | 70–100 | 75 | Organised by RQ, with numbers and honest qualifiers. It inherits "a plain lead recovers half" (a ratio of two noisy, cross-machine estimates) and does not mention the 84–87% results against the simpler agents. |
| Future work | 70–100 | 72 | Three sensible items, two with reasons (hand-tuning has saturated; the best DUCT variant depends on the game). The opponent-model item has no reason given. Tak et al. is used to motivate replacing UCB, although its headline finding favoured decoupled UCT. |

### 6) General (weight 10%): **69.7**

| Item | Band | Mark | Reasons |
|---|---|---|---|
| Clarity | 50–69 | 66 | Logical structure, precise and concise, but very dense: almost every sentence carries 2–6 statistics. Internal codes run throughout (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, v2–v4, H1–H3, seed blocks). Figure references are swapped. "Our last study" is never defined. Phrasing is awkward in places ("a wrong model that more iterations exploit more", "supports H3 only one-sidedly", "it had won", nested parentheses). Results is 952 words (guide 500–700) and Method 878 (guide 700–800). |
| Figures, tables, legends | 50–69/70 | 69 | Fig. 1 is genuinely conceptual. Fig. 2, the paired forest plot with win and margin panels, is excellent. Fig. 3 is clear. But the text cites Fig. 3 for the ablation and Fig. 2 for the budget (wrong). Notes are set at 7 pt, below the template's 8 pt. Table I's footnote is a cryptic 11-tuple. Table II is prose in a table with no space after it. Fig. 3's legend has an odd 2-row layout. |
| Referencing | 70–100 | 74 | Consistent IEEE style; 15 references, the brief's maximum. All are relevant and mostly peer-reviewed, with correct details as far as I could check. Minor: Tak et al. cited selectively; [7] used for "ensemble of determinised trees" (Long et al. analyse PIMC, not tree ensembles); Holm, UCB-Tuned and RHEA uncited; progressive bias cited via the survey rather than Chaslot et al. 2008. |

### Weighted total

0.05×74 + 0.15×73.6 + 0.30×76.7 + 0.30×76.7 + 0.10×73.5 + 0.10×69.7 = 3.70 + 11.04 + 23.01 + 23.01 + 7.35 + 6.97 = **75.1%**

* A lenient marker who is impressed by the experiments: about 80.
* A marker put off by the density, the swapped figures and the AI-authored code/text disclosure: about 70.
* After the must-fix items (Section 6): about 77–78.
* After must + should items: about 82–84.

---

## 2. Number verification (recomputed from raw CSV / log / txt files)

Formulas used:
* Win% = mean of `focusWin` (ties count 1/k), with a Wilson 95% CI.
* Margin = mean of `focusScore − bestOppScore` ± 1.96 SE.
* Paired difference = mean of per-game differences ± 1.96 SE.
* Two-sample difference = difference of means with √(SE₁² + SE₂²).

✓ means the value matches to the reported precision.

### Table III (all rows)

| # | Report | Source | Recomputed | Outcome |
|---|---|---|---|---|
| 1 | 3p/100 held-out: n 500, 52.6 [48.2, 56.9], +0.73 ± 0.46 | v5 `r2_val_3p100.csv` | 52.60 [48.22, 56.94], +0.730 ± 0.462 | ✓ |
| 2 | 4p/100 held-out: 44.4 [40.1, 48.8], −0.61 ± 0.43 | `r2_val_4p100.csv` | 44.40 [40.10, 48.78], −0.614 ± 0.427 | ✓ |
| 3 | DUCT, ISMCTS: 55.0 [50.6, 59.3], +0.72 ± 0.40 | `r2_duct_3p100.csv` | 55.00 [50.62, 59.31], +0.722 ± 0.397 | ✓ |
| 4 | ISMCTS × 2: n 300, 54.2 [48.5, 59.7], +1.66 ± 0.69 | `v5_field_3p100.csv` | 54.17 [48.51, 59.72], +1.663 ± 0.694, n = 300 | ✓ (but n < 500; see 4) |
| 5 | BasicMCTS, RHEA: 84.2 [80.7, 87.1], +6.68 ± 0.60 | v4 `B_mcts_rhea.csv` | 84.20 [80.74, 87.13], +6.682 ± 0.603 | ✓ |
| 6 | OSLA, BasicMCTS: 87.2 [84.0, 89.8], +9.74 ± 0.75 | `B_osla_mcts.csv` | 87.20 [83.99, 89.85], +9.744 ± 0.754 | ✓ |

### IV-A text

| # | Report | Source | Recomputed | Outcome |
|---|---|---|---|---|
| 7 | Seed block 7: 55.0% | `V_3p100.csv` | 54.97 | ✓ |
| 8 | Re-run: 58.1% | `v5_base3p100.csv` | 58.10 | ✓ |
| 9 | Held-out lower by 2.4 ± 6.1 and 5.5 ± 6.1 pp | two-sample | 2.37 ± 6.14; 5.50 ± 6.09 | ✓ |
| 10 | DUCT wins 21.0% of its seats | `r2_duct_3p100.log` | 21.0% (55.0 + 21.0 + 24.0 = 100) | ✓ |
| 11 | BasicMCTS/RHEA/OSLA ≤ 12.8% of seats; OSLA 0 | v4 logs of `B_mcts_rhea`, `B_osla_mcts` | 9.0, 6.8, 12.8, 0.0% | ✓ |

### IV-B and Fig. 2 (all 11 points)

| # | Report | Source | Recomputed | Outcome |
|---|---|---|---|---|
| 12 | −H: −28.3 ± 5.8 pp | `A_noH` vs `V_3p100` (paired) | −28.27 ± 5.79, z = 9.57; margin −5.45 ± 0.71 | ✓ |
| 13 | −S−I: −10.2 ± 5.7 (z = 3.5) | `A_noSI` | −10.17 ± 5.75, z = 3.47; margin −1.75 ± 0.58 | ✓ |
| 14 | −R z = 2.0, −B 1.4, −I 1.3; I alone 3.8 ± 5.7 | `A_noR`, `A_noB`, `A_noI` | −6.17 (z = 2.04), −4.27 (1.43), −3.77 ± 5.68 (1.30) | ✓ |
| 15 | Holm: all five margin differences survive; on wins only H and S + I | my Holm, m = 5 | margin p: 0, 0, 0.0008, 0.016, 0.025 (all pass); win: −R p = 0.042 > 0.0167 | ✓ |
| 16 | Lead heuristic: 13.6 ± 5.9 (z = 4.5) | `r2_lead_3p100` vs `v5_base3p100` | −13.60 ± 5.88, z = 4.54; margin −1.45 ± 0.60 | ✓ |
| 17 | Factorial: R +8.7 ± 2.1 (z 8.1), K +5.2 ± 2.2, M +4.1 ± 2.0, M × K +3.2 ± 2.2 | the 16 v3 cell CSVs (`s2_base` … `s4_K0.5_T8_M`) | R +8.65 (z 8.1), K +5.15, M +4.10, M × K +3.17 (z 2.97), each ± about 2.1 | ✓, **but T +3.65 (z 3.4) and R × T −4.22 (z −3.96) exist and are not reported** |
| 18 | Progressive bias back: −1.2 ± 4.3 | `tb1/tb2_T4_A` vs `tb1/tb2_T4` | −1.18 ± 4.31 (n = 1000) | ✓ |
| 19 | X3 greedy 45.0%; default constants 7.3% | `greedy_duels.txt` | 45.00% (pool {X0}); 7.25% (pool {D, X0}) | ✓ (two different pools, see 3c) |
| 20 | Last study: X3 −1.3 ± 5.8; N −1.8; V2D −0.6; W1 +0.7; R3V2 −23.7 ± 5.8 (z 8.0) | `v5_*_3p100` vs base | −1.30 ± 5.84; −1.80; −0.60; +0.70; −23.70 ± 5.81 (z 8.00) | ✓ (margins of V2D z = −2.1 and X3 z = −1.9 are not reported) |

### IV-C and Fig. 3 (all 10 points)

| # | Report | Source | Recomputed | Outcome |
|---|---|---|---|---|
| 21 | v2 24.8 → v3 50.1% | `s1_ref`, `abl_T4` | 24.80, 50.10 | ✓ |
| 22 | Fig. 3 points | development CSVs | v2 24.8 [21.2, 28.8], 18.3 [15.2, 21.9], 18.8 [14.4, 24.1]; v3 50.1 [45.7, 54.5], 40.7 [36.4, 45.0], 32.8 [27.3, 38.8]; final 55.7 [51.3, 60.0], 49.7 [45.3, 54.1], 47.6 [41.5, 53.8]; held-out 52.6 | ✓ (figure matches) |
| 23 | v3 −17.3 ± 7.2; final −8.1 ± 7.5 pp | two-sample | −17.30 ± 7.22; −8.10 ± 7.52 (z = 2.11) | ✓ |
| 24 | S + I gain +5.6 ± 6.1 → +14.8 ± 8.4; rise +9.2 ± 10.4 (z 1.7) | two-sample | +5.60 ± 6.13; +14.80 ± 8.42; +9.20 ± 10.42 (z = 1.73, p = 0.08) | ✓ |
| 25 | Iterations at 900 ms: v3 19 696, final 21 246 | `probe_iterations.txt` | same (2-game probe on the 4-core machine) | ✓ (see 3c on its validity) |
| 26 | ISMCTS seats: 23.1/33.1/37.4 vs final; 33.0/40.0/46.0 vs v3 | logs of `d1_I`, `f5_I`, `f9_I`, `abl_T4`, `f5_finalT4`, `f9_finalT4` | same | ✓ |

### IV-D

| # | Report | Source | Recomputed | Outcome |
|---|---|---|---|---|
| 27 | 3p configuration in 4p: 46.4, 43.5, 38.4% | `q4_I`, `q4_Io`, `val_q5` | 46.43, 43.50, 38.40 | ✓ |
| 28 | X0 +4.3 ± 4.3 | pooled two-sample | +4.27 ± 4.32 (z = 1.94, p = 0.053) | ✓ (borderline) |
| 29 | X0 + W2 + K1 +5.8 ± 7.4; F + L +1.0 ± 7.9; F + L at 100 ms −6.6 ± 6.0 (z 2.2) | `val_q5`→`q5_W2`/`q5_W2r`; `q5_W2r`→`q5_FL8`; `q4_X0c`→`q4_X0FL8` | +5.80 ± 7.39; +1.00 ± 7.86; −6.57 ± 5.96 (z 2.16) | ✓ |
| 30 | 3p cost −7.7 ± 5.4 (z 2.8); interaction 13.5 ± 9.1 (z 2.9) | `f5_FL8`→`f5_W2FL8` | −7.70 ± 5.39 (z 2.80); 13.50 ± 9.14 (z 2.89) | ✓ |
| 31 | Depth 1 −17.4 ± 8.2 (z 4.2); "limited to 100 ms" −9.0 ± 7.7 (z 2.3); 1 000 ms −4.8 ± 8.1 | `q5_FL8d1`, `q5_X0b100`, `q5_FL8t1000K14` | −17.40 ± 8.19; −9.00 ± 7.75; −4.80 ± 8.09 | ✓ numbers; **description of the 100 ms run is wrong** (3c) |
| 32 | 73 positions, 704 children, 53% visited once, depth 1.5; 2.7 / 5.5 / 5.5 / 9.6% | `treestats4.txt`, `agree4.txt` | same (2, 4, 4, 7 of 73 positions) | ✓ (tiny counts) |

### Setup, abstract and conclusions

| # | Report | Recomputed | Outcome |
|---|---|---|---|
| 33 | ±4.4 pp at 500 games | 1.96·√(0.25/500) = 4.38 | ✓ |
| 34 | Paired difference about ±5.7 pp | observed 5.68–5.94 | ✓ |
| 35 | Correlation of paired outcomes 0.0–0.2 | observed 0.002–0.220 over 18 paired comparisons | ✓ (max 0.22) |
| 36 | "Score margin about twice as sensitive" | z(margin)/z(win) = 1.18–2.34, median 1.70 (ablations) | **overstated** (about 1.5–2 times) |
| 37 | "48–73 s of CPU time per game" | actual decisions × budget: 45.2 s (4p/500), 69.5 s (3p/900); the report's figures are nominal pick counts | **imprecise** |
| 38 | "162 runs, 69 350 games" | 162 distinct CSVs ≥ 200 games, 69 350 games (48 of them < 500 games: 41 × 250, 7 × 300) | ✓ |
| 39 | 729 / 4 096 joint moves; 108-card deck; Dumpling 1/3/6/10/15; Nigiri 2/3/1; Maki 6/3; Pudding ±6; 12 − n | arithmetic / `SGParameters`, `SGForwardModel` (nCards − n + 2) | ✓ |
| 40 | Abstract 52.6 / 44.4 / 7.7; Conclusions 28.3 / 10.2 / 8.7 / −8.1 vs −17.3 / 7.7 ± 5.4 | as above | ✓ |

### Mismatches and untraceable numbers

* **No numeric mismatch.** Every value printed in the report reproduces from the raw files at the stated precision.
* **Description mismatches:**
  * "our agent limited to 100 ms": the run `q5_X0b100` used the whole 100 ms configuration (X0 only, K 0.5, no W2/F/L), not the 500 ms agent with a 100 ms budget.
  * Table II "since v4 a fixed deck order": canonical deals began on 19 Oct, 01:37, part-way through v4. The v4 development runs `d1_I`, `f5_I`, `f9_I`, `q4_I` etc. are on TAG's own deals.
* **Imprecise:** "about twice as sensitive"; "48–73 s".
* **Cannot be traced to any file:** "about 40 four-player runs". runs.md lists 57 four-player runs, and no file records the order in which they were run.

### Are the build scripts' computations right?

Yes. `run`, `paired`, `two_sample`, `diff`, `paired_dd`, the Wilson formula, Holm (computed by hand in the text), `study_size` and the figure code all compute what they claim. I found no arithmetic error.

**But there are latent wording bugs that will fire once the pending runs land:**

* `text_budget()` prints "There the final agent **keeps its strength**" whatever `BUD` shows. It also prints "a rise of …, **in the direction of H2** but not significant" for any z < 1.96, **including a negative rise**. It prints `abs(z)`, which hides the sign.
* `text_conclusion_1()` prints "the gain … **grows** from X … to Y" unconditionally.
* `_rise_phrase()` (abstract) returns "grew with the budget, though not significantly" for any z < 1.96, including negative values.
* "We attribute the gain to v3's sequential tree…" is also unconditional.

Make these phrases conditional on the sign and on the CI, or re-read them by hand after the rebuild.

---

## 3. Technical correctness

### 3a. Does the Method describe what the code does?

| Claim in report | Code | Verdict |
|---|---|---|
| Modified copy of `players.basicMCTS`, no code from the advanced agent | No `players.mcts` import in `groupA`; the files are headed "copied from BasicTreeNode/BasicMCTSPlayer/Params" | ✓ |
| Eq. (1) Ŝ = S + αU + M + w_t·P; completion min(1, (r−m+1)/T); Wasabi 2ν; Chopsticks 1; w_t = t/3 | `CardValueModel.estimatedScore/potential/completion` (w_t = (round+1)/3) | ✓, but the code multiplies P by **π** (`puddingScale`), which eq. (1) omits (π = 1 by default, 1.25 in X0) |
| Footnote: Maki weight 1/(1 + r/T_m) | `makiValue`: T_m ≤ 0 means weight 1 | With the default T_m = 0 the printed formula gives weight 0. Add "(T_m = 0: weight 1)" |
| Eq. (2)–(3), σ = 15, w = 0.7 | `SushiGoHeuristic.evaluateState/valuesOf` | ✓ |
| mv = change of Ŝ when p plays c | `marginalValue` (both terms evaluated at r − 1) | ✓ |
| B: ε = 0.2 greedy policy for all players; Chopsticks second card the same way | `rolloutCard`, `playOut`, `greedyAction` | ✓ (in the **tree** the second Chopsticks card is always greedy; not stated) |
| R: rollout stops when the round is scored | `finishRollout` | ✓ |
| P: one branch per distinct card | `distinctActions`, `distinctCards` (key = type, icons, chopsticks) | ✓ |
| M: one value per player, each maximises its own | `backUpAll`, per-player `isVal` | ✓ (replaces BasicMCTS's paranoid backup; not stated) |
| S: one node per turn, UCB on own statistics, untried first, child of the joint action | `selectInformationSet`, key = packed joint card keys | ✓ |
| Eq. (4) with availability count N_p(a) | `val[j+2] += 1` for every held card | ✓ |
| I: new sample each iteration (`state.copy(me)`), open-loop, statistics only | `informationSetIteration`; TAG `redeterminise` reshuffles unknown hands only (`isHandKnown`) | ✓ |
| Most-visited root card | `bestAction` | ✓ |
| K = 0.5 (3p); X0, W2 (w 1, terminal tanh(gap/2) with Pudding tie-break, σ·(R/2)^0.5, opponents value lead over the others' mean), F, L = 8, K = 1 for 4p at ≥ 500 ms | JSON `fourPlayer` blocks; `terminalValues`, `roundScale`, `scoreValue` | ✓ (the switch is by player count at run time and by JSON file per budget, not a run-time budget test) |
| Competition: 900 ms / guard 925; 430 / 450 | `json/competition/*` | ✓ |
| Opponents (Table II) | `tagmcts.json` = TAG `SushiGo_3+P_HiBudget` (MultiTree, UCB_Tuned, K 1, rollout 300, ScoreHeuristic; also MAST "Both", exploreEpsilon 0.1, not mentioned); DUCT = `SushiGoDUCT.json` (`decoupled`, defaults K 1 and Information_Set, LeaderHeuristic, rollout 30) | ✓ |
| −H: TAG default heuristic = own score / 50 | `SGGameState._getHeuristicScore` | ✓; ablation files are single-switch diffs |
| Seating g mod n, 4p "2 + 1 swapped every 4 games"; canonical deal | `Arena.play`, `canonicalDeal` | ✓ |
| CPU time for all agents | TAG `ElapsedCpuTimer` uses thread CPU time on Linux (wall time on Windows) | ✓ |

### 3b. Literature claims

Mostly accurate and correctly attributed: [1], [2], [3], [5], [7] (disambiguation), [8] (strategy fusion, non-locality), [9] (MultiTree, CoG 2022 pp. 292–299 confirmed), [10], [12], [13] (CCIS 504 pp. 64–77 confirmed). Klang et al. [4] did implement Sushi Go! in TAG and study simultaneous selection and complete information (confirmed).

Issues:
* **Tak et al. [11] is cited selectively.** Their abstract says the variants' relative performance depends strongly on the game and opponent, **but overall decoupled UCT performed best despite its theoretical shortcomings**. The report keeps only the first half and uses it to motivate replacing UCB. The second half would actually support the choice of S. Page numbers: the Maastricht record gives proceedings pp. 232–239; IEEE Xplore paginates CIG 2014 papers 1–8. Check which one you cite.
* Klang et al. concluded that better heuristics should improve the agents. This is a direct motivation for H, and it is missing.
* [7] is used for the determinised ensemble D; Long et al. analyse PIMC, not ensembles of determinised trees.
* Silver & Tesauro: "showed that rollouts should be balanced" slightly overgeneralises small-board Go results. Acceptable.
* Uncited methods: the Holm correction, UCB-Tuned, RHEA. Progressive bias is cited via the survey rather than Chaslot et al. (2008).

### 3c. Are the conclusions supported by their statistics?

**Selective reporting**

1. **Factorial.** The 2^4 factorial's fourth factor (T = 8, +3.6 ± 2.0, z 3.4) and its largest interaction (R × T −4.2 ± 2.1, z −4.0) are omitted. Only R, K, M and M × K are reported, so the reader is left wondering what the fourth factor was.
2. **Seed-7 runs of the final agent at 500 and 900 ms** (`V_3p500` 49.4% [45.0, 53.8]; `V_3p900` 56.0% [49.8, 62.0], 250 games; `V_4p500` 41.4%) are complete and are not reported, while seed 7 at 100 ms is. They matter for RQ2: on seed 7 the final agent does **not** decline from 100 to 900 ms (paired +1.6 ± 8.7 pp over the first 250 games), unlike the development curve (−8.1 ± 7.5) shown in Fig. 3.
3. **Last-study margins.** Win-rate nulls are reported, but the margin, which the report calls the more sensitive screen, shows losses for V2D (−0.60 ± 0.56, z −2.1) and X3 (−0.57 ± 0.60, z −1.9).

**Confounds and misdescriptions**

4. "Our agent limited to 100 ms" (−9.0 pp) compares the 500 ms 4p agent (X0, W2, F, L8, K 1.0) with the 100 ms *configuration* (X0 only, K 0.5, no W2/F/L) at 100 ms. Budget and settings change together.
5. "1 000 ms (with K = 1.37) did not help": time and K change together, so the null result cannot isolate time.
6. X3 changes the **leaf evaluation** (e.g. π 1.5 vs 1 on the Pudding standing) as well as the rollout policy. "Consistent with simulation balancing" over-interprets a null (±5.8 pp) that is not a pure rollout-policy test.
7. "Valuing the lead recovers about half of H's effect" divides 13.6 ± 5.9 (paired with the 4-core re-run, 58.1%) by 28.3 ± 5.8 (paired with the 18-core run, 55.0%). The baselines and machines differ and the ratio's interval is very wide (roughly 0.25–0.8).
8. The −H ablation did not retune K, which the report itself says is mis-scaled. So 28.3 pp mixes the value of H with exploration mis-scaling. The cleaner estimate is the K-rescaled lead comparison (13.6 pp), yet the conclusions headline 28.3.
9. "It is not a lack of search" rests on a separate 2-game probe on the 4-core machine. The development runs it explains ran on the 18-core laptop with 6–16 parallel games, where v3's tree stores a full game state per node (about 1 GB of live data per game at 900 ms; garbage collection thrashed until the JVM was retuned, v3 README).
10. The X3 greedy comparison sets 45.0% (X3 against two X0) beside 7.3% (default constants in a {D, X0} pool). These are different pools, so it is not a like-for-like comparison.

**Inference**

11. The four-player diagnostics are percentages of 73 positions: 2, 4, 4 and 7 positions. A 95% CI for 2/73 is about 0.3–9.6%, so "no more often than a re-run" cannot be distinguished. Report counts and CIs.
12. Tension that is never reconciled: cutting the tree to depth 1 costs 17.4 pp (so depth matters), yet "extra time rarely changes the decision of the wide, shallow tree".
13. Holm is applied only to the ablation family. The text promises to "state where a conclusion survives a Holm correction". Elsewhere z ≈ 2.2–2.9 results come from about 57 four-player and about 10 three-player 500 ms runs. On the last-study family only R3V2 survives Holm. H3 is presented as an a-priori expectation, but the contrast was chosen after the runs (HARKing risk, partly admitted).
14. Null results are written as "did not help / did not change". Prefer "no detectable effect (95% CI …)": a ±5.8 pp interval cannot exclude gains of about 4–5 pp.
15. The abstract headlines "the tree's advantage grew with the budget" at z = 1.7.
16. Paired vs independent samples are handled correctly and labelled throughout. That is a strength.

**Supporting evidence the authors have but do not use**

* S alone (`A_noI` vs `A_noSI`, same deals): +6.4 ± 5.7 pp (z 2.2).
* The development S-only gain rose from +1.3 ± 6.1 (100 ms) to +6.2 ± 6.1 (500 ms).

Both support "modelling simultaneity matters more than modelling hidden hands" and the RQ2 attribution to v3's sequential tree.

---

## 4. Compliance

| Requirement | Status |
|---|---|
| 4-page main text; only gen-AI statement and references on p. 5 | ✓ Conclusions end at about 68% of p. 4's right column; the gen-AI statement starts on p. 4 and continues, with the references, on p. 5 (about 40% of p. 5 used). Space budget: about 21 lines of p. 4's right column follow the Conclusions; the pending text (+10 lines) leaves about 11 lines before the main text reaches the bottom of p. 4. The whole gen-AI statement then moves to p. 5, which has room for it, the references and about 40 more lines. |
| Template | ✓ IEEE Word template, A4, two columns, template styles. Margins equal the template's (54 pt top on body pages is the template's own value; left/right differ by 0.7 pt from the LibreOffice re-save, negligible). ✗ The PDF embeds **Liberation Serif** (a LibreOffice substitute) instead of Times New Roman; metric-compatible but not the template font. Table notes (7 pt) and the first-page note (7.5 pt) are below the template's 8 pt. |
| Word-count guide | Abstract 124 (75–125) ✓; Introduction 448 (350–450) ✓; Background 656 (650–850) ✓ low end; **Method 878 (700–800) over**; **Results 952 (500–700) about 36% over**; Conclusions 263 (200–300) ✓. Only a guide, but the over-run shows up as density. |
| Generative-AI rules | ✓ Acknowledged; Claude cited [15] in a page-1 footnote and in the statement; main prompt and last-study prompt quoted verbatim. ✗ **The revision prompts (the reviewer findings "archived with our records") produced text and are not included.** The brief requires the prompts used to generate text. Per-section citation is done by one global footnote; probably acceptable, but strictly the brief asks for a citation in each section that uses AI text. |
| References | ✓ 15 (the 6–15 maximum), one consistent IEEE style. |
| ≥ 500 games or justification | ✗ Table III "ISMCTS × 2", n = 300, no justification (the planned `r2_field_3p100b`, 200 more games, is not on your pending list). △ The 250-game runs (900 ms, 4p/500 ms) are justified only by "48–73 s of CPU time per game". |
| Experiments at 500 ms | ✓ Development 3p/500 at 500 games, 4p/500 at 250; held-out 500 ms runs pending; seed-7 500 ms runs exist but are unreported. |
| Code: package `group{LETTER}`, README, competition JSON, no `players.mcts` | ✓ Compiles against TAG; both competition files play correctly (smoke test). ✓ README explains construction and JSON. ⚠ **The README opens with "Rename first: replace every `groupA` … with your real group letter".** Confirm that your group really is A, and remove that banner. 68 of the 70 JSON files are obsolete. |
| AI-authorship risk (not a rubric line) | The statement says Claude wrote the code and drafted every section. That is allowed if acknowledged, but the brief warns that a from-scratch MCTS "will be interpreted" as LLM work. SushiTreeNode is about 5.5× BasicTreeNode and has four iteration routines. Expect scepticism about the "Understanding" item; the individual reflections should show the students' own understanding. |

---

## 5. Presentation

* **Figure references are wrong.** In IV-B, "Fig. 3 (top)" and "(Fig. 3, bottom)" should be Fig. 2. In IV-C, "(Fig. 2; independent samples)" should be Fig. 3. Fig. 2 is never cited correctly. The cause is in `build_report.py`: the hard-coded strings follow the file names `fig3_effects` / `fig2_budget`, not the order in which the figures appear.
* **Legibility at print size (rendered at 200 dpi):**
  * Fig. 1 is clear (7–7.5 pt).
  * Fig. 2 is clear; its x-axes have only two ticks each.
  * Fig. 3 is clear; its legend wraps "v3" under "v2"; v2's line is v2 against (v2, ISMCTS), which the caption does not say.
  * Table I's footnote packs 11 symbols, some defined only there, into 7 pt.
  * Table II's text runs straight into the next paragraph (no space after the bottom rule).
* **Captions** are informative. Fig. 3's caption promises "v3 on the held-out deals" (pending).
* **Undefined or opaque terms:** "UCB-Tuned"; "our last study" (= v5 sprint, never introduced); "seed block" (Table II defines s, not "block"); X0/X3/W2 names; "4-core machine" vs "same machine" (IV-A); EXP3 not expanded.
* **Consistency:** "seed 7" (Fig. 2) vs "seed block 7" (text, Table III); "scoring model" (abstract) vs "card-value model" (elsewhere); "final agent" vs "v4" (both used, defined).
* **Flow:** the Results read as chains of statistics. Typical example: "a 2^4 factorial … gave main effects of +8.7 ± 2.1 pp for R (z = 8.1), +5.2 ± 2.2 for K = 0.5 instead of 1.41 and +4.1 ± 2.0 for M, with an M × K interaction of +3.2 ± 2.2: max-n backups need less exploration." Fewer numbers per sentence, with the rest in tables or figures, would read better.
* **Title** stresses "Information-Set", but I alone is the weakest and non-significant component; decoupling carries the effect.

---

## 6. Prioritised changes

Space budget: after the pending text (+10 lines) there are about 11 free lines at the end of p. 4, because the gen-AI statement can move wholly to p. 5. The additions below come to about 13 lines and the cuts listed at the end free about 8 (net +5), which leaves a safety margin of about 6 lines.

### MUST FIX

**M1. Swapped figure references** (General: figures, clarity). In `build_report.py`:
* `text_ablation()`: "Fig. 3 (top) removes" → "**Fig. 2 (top)** removes".
* `text_last_study()`: "(Fig. 3, bottom)" → "**(Fig. 2, bottom)**".
* `text_budget()`: "(Fig. 2; independent samples)" → "**(Fig. 3; independent samples)**".

**M2. Misdescribed 4-player budget test** (Experimentation: critical analysis; correctness). Replace
"At 500 ms the four-player tree cut to depth 1 lost 17.4 ± 8.2 pp (z = 4.2) and our agent limited to 100 ms 9.0 ± 7.7 pp (z = 2.3), yet 1 000 ms (with K = 1.37) did not help (−4.8 ± 8.1 pp)."
with
"At 500 ms, cutting the four-player tree to depth 1 lost 17.4 ± 8.2 pp (z = 4.2), and the 100 ms configuration (X0 only, K = 0.5) run at 100 ms lost 9.0 ± 7.7 pp (z = 2.3); 1 000 ms with K = 1.37 showed no gain (−4.8 ± 8.1 pp). Both budget comparisons also change settings."
(+1 line.) Better still: rerun `SushiMCTS_500_FL8` with a 100 ms budget for 250 games on seed 2 (about 40 min on 4 threads) and report the clean number.

**M3. Report the whole factorial** (Experimentation: rigour, critical analysis). Replace from "but a 2^4 factorial…" to "…need less exploration." with
"but a 2^4 factorial on v3 (R, M, K = 0.5 vs 1.41 and completion horizon T = 8 vs 4; 16 × 500 games, independent deals) gave main effects of +8.7 ± 2.1 pp for R (z = 8.1), +5.2 ± 2.2 for K, +4.1 ± 2.0 for M and +3.6 ± 2.0 for T = 8. Of the six interactions only R × T (−4.2 ± 2.1: both repair optimistic values of unfinished sets, so T = 4 was kept after a tie-break on new deals) and M × K (+3.2 ± 2.2: max-n backups need less exploration) exceed |z| = 2."
(+2.5 lines.)

**M4. Table III row with 300 games** (Experimentation: rigour; the rubric's 500-game rule). Either finish `r2_field_3p100b` (200 games, seed 307, already planned in the v5 README) so the merged run has n = 500, or add to the table note: "ISMCTS × 2: 300 games (stopped at the tournament deadline; ±5.6 pp)". Otherwise drop the row.

**M5. Generative-AI prompts** (compliance; the brief says marks are lost). Reproduce the revision prompts on p. 5, which has room. For example, after "…were the prompts for the revisions":
"Each revision prompt was the reviewer's report preceded by '[exact instruction used]'; the three reports are reproduced in condensed form below [and in full in genai_prompts.pdf in our archive]."
Then list them. Optionally widen the footnote to "(Sections I–V, Tables I–III, Figs. 1–3)".

**M6. Package letter and README banner** (Approach: code; brief §3.4). Confirm the group letter. If it is not A, rename the package, the JSON `class` entries and the file names. Delete the "Rename first…" banner from README.md.

**M7. After the pending runs, re-check the auto-generated directional wording** (Experimentation: critical analysis). "keeps its strength", "grows", "in the direction of H2", "grew with the budget" and "We attribute the gain" are printed regardless of the sign of the result. Make them conditional, or edit by hand.

### SHOULD FIX

**S1. Report the seed-7 budget runs** (Experimentation; avoids selective reporting). Add to IV-C:
"On seed block 7 the final agent won 55.0%, 49.4% and 56.0% at 100, 500 and 900 ms (250 games at 900 ms), so its development decline at 900 ms did not replicate."
(+1.5 lines.) This also gives a completed 500 ms validation if the held-out 500 ms runs fail.

**S2. Use the S-alone evidence** (Experimentation: critical analysis; contributions). Add to IV-B after "…within n − 1 turns.":
"Decoupling alone (S with D) beats v3's sequential tree by 6.4 ± 5.7 pp (z = 2.2)."
Add to IV-C after "We attribute the gain…":
"; S alone gained +1.3 ± 6.1 pp at 100 ms and +6.2 ± 6.1 pp at 500 ms in development."
(+1.5 lines.)

**S3. Soften the over-interpretations** (Experimentation: critical analysis):
* X3: replace "…yet did not strengthen the search (−1.3 ± 5.8 pp), consistent with simulation balancing [6]." with "…showed no detectable gain in the search (−1.3 ± 5.8 pp); as X3 also changes the Pudding weight of the leaf evaluation, this only weakly echoes simulation balancing [6]."
* Lead: replace "recovers about half of H's effect" with "recovers roughly half of H's effect (two estimates against different baselines; wide uncertainty)". In the Conclusions, replace "(28.3 pp when removed; a plain lead recovers half of it)" with "(13.6 pp over TAG's lead heuristic with K rescaled; 28.3 pp over TAG's default heuristic without retuning K)".
* Probe: replace "It is not a lack of search: at 900 ms v3 ran…" with "It is not a lack of search: in a 2-game probe at 900 ms v3 ran…".
* Diagnostics: replace "changed the chosen card in 2.7% and 5.5% of the positions, no more often than a re-run with the same seed (5.5%; 9.6% with another seed)" with "changed the chosen card in 2 and 4 of the 73 positions, about as often as a re-run with the same seed (4) or another seed (7)".
* Nulls: "did not help / did not change" → "showed no detectable effect".

**S4. Holm scope** (Experimentation: critical analysis). Replace "We report unadjusted intervals and state where a conclusion survives a Holm correction." with "We report unadjusted intervals, apply a Holm correction within the ablation (Fig. 2, top) and the last study (Fig. 2, bottom), and treat the four-player comparisons as exploratory." In IV-B, add "(only the game-end rollouts survive a Holm correction)" after the last-study numbers.

**S5. Reconcile depth vs time** (critical analysis). After the diagnostics, add: "Depth still matters, since a depth-1 tree hands the next turn to the ε-greedy policy, but once the second turn is searched, doubling the iterations rarely changes the root card." (+1 line.)

**S6. Literature** (Problem definition: literature).
* In II-C replace "and Tak et al. [11] found that the best variant depends on the game;" with "and Tak et al. [11] found that the best variant depends on the game and opponent, although decoupled UCT was best overall;".
* In II-C's last paragraph, after "…focused on simultaneous action selection and complete information", add "and concluded that better heuristics would improve TAG's agents, which motivates our card-value model". (+1 line.)

**S7. Contributions' significance** (Problem definition). After the bullets add: "Together they suggest that in drafting games modelling simultaneity matters more than modelling hidden hands, and that search settings must be chosen per table size." Delete "(162 runs, 69 350 games)" or move it to Table II. (+0.5 line net.)

**S8. Justify keeping W2/F/L for four players** (Approach/Experimentation). Add to III-D or IV-D: "We kept W2, F and L for four players because each estimate was positive and no alternative did better, accepting that none of these gains is significant." (+1 line.)

**S9. Small factual fixes** (documentation):
* Table II Deals: "since v4" → "since 19 October (all seed-7 and held-out runs)".
* "about twice as sensitive" → "about 1.5–2 times as sensitive (in z)".
* "48–73 s" → "45–70 s".
* "(58.1% when re-run on the same machine)" → "(58.1% when re-run on the 4-core machine of the held-out runs)".
* Define "last study" in the Seeds row: "ablation and the final study (v5, five candidate changes) s = 7".

**S10. Abstract** (Abstract: findings). Replacement, 125 words, keeping the pending placeholder:
"In Sushi Go!, players pick cards simultaneously from hidden hands that are passed around, which basic Monte Carlo tree search (MCTS) does not model. Starting from BasicMCTS of the Tabletop Games (TAG) framework, we added a card-value model that drives evaluation and rollouts, round-end rollouts, max-n backups, decoupled selection for the simultaneous choices and information-set search. On held-out deals the agent wins 52.6% of 500 three-player games at 100 ms against TAG's information-set MCTS and our previous agent (chance 33.3%), [pending]% at 900 ms, and 44.4% of four-player games at 100 ms (chance 25%). Ablations show that the card-value model and decoupled selection matter most; settings tuned for four players cost 7.7 percentage points with three, so the best configuration depends on the table size."
If the held-out H2 test turns out significant, swap the last clause for it.

**S11. Report direct dominance in IV-A**: "TAG's ISMCTS won 26.4% of its seats and v2 21.0%", and "DUCT's 21.0% equals v2's in the same deals". (+0.5 line.)

**S12. Method precision** (Approach: understanding):
* Eq. (1) → "Ŝ_p = S_p + αU_p + M_p + π w_t P_p" (π = 1 by default).
* Table I note: add "(T_m = 0: weight 1)".
* III-C, after "every player maximises its own value [12]": add "instead of BasicMCTS's single paranoid value".
* After "[10];": add "a Chopsticks move's second card is the greedy one".

**S13. Code clarity** (Approach: code). Remove or isolate the switches the final agent never uses (G MAST, U MultiTree, Q OpponentModel, O, K, E, N, MAST code paths). Keep A and D only for the v2/v3 baselines and say so. Prune `json/` to the final agent, the ablation variants and `competition/`. Move "How to construct the agent" to the top of the README.

### NICE TO HAVE

* N1. Export the PDF with real Times New Roman (from Word, or with MS core fonts installed). Raise table and figure notes to 8 pt.
* N2. Fig. 3 legend on one row; label v2's line as "v2 (vs v2, ISMCTS)". Add a −10 tick to Fig. 2's win panel.
* N3. Behavioural insight in one sentence or a small table, from the held-out logs: compared with v2, the final agent takes far fewer Sashimi points (3.0 vs 9.1 per game) and far more Pudding (2.9 vs 0.5) and Dumpling (6.4 vs 3.9). This shows what the card-value model changed and would lift "insight".
* N4. Add a space after Table II. Use "seed block" consistently (Fig. 2 caption says "seed 7").
* N5. Cite Holm (1979). UCB-Tuned (Auer et al. 2002) and Chaslot et al. (2008) only if you can drop one reference, because the list is at the 15-reference cap. Use Cowling et al. [3] rather than [7] for the determinised ensemble D.
* N6. Consider retitling around "Decoupled MCTS", since I is the weakest component.

### Cuts to pay for the additions (about 8 lines)

* IV-D first sentence → "With four players the 3-player configuration won 38–46% in development runs (chance 25%)." (−1.5)
* IV-C last sentence → "Much of the fall comes from the opponent: TAG's ISMCTS won 23.1% of its seats against the final agent at 100 ms but 37.4% at 900 ms (v3: 33.0% → 46.0%)." (−1)
* IV-B greedy-duel aside → replaced by the S3 X3 text: "Card constants tuned for three players (X3), a much stronger greedy policy (45.0% against two X0 players without search), …" (−1.5)
* OSLA → "OSLA wins none: its one-step look-ahead never reaches the simultaneous reveal." (−0.5)
* Table II: shorten "Opponents" and merge "Hardware" + "Records" into one row. (−2)
* Contributions: drop "(162 runs, 69 350 games)". (−0.5)
* III-D: shorten the competition-file sentence → "The competition files use 900 ms (1 s limit) or 430 ms (500 ms limit) of CPU time with wall-clock guards." (−0.5)

---

## 7. Plain answers

**Would it receive 85% or more as it stands, with the pending numbers filled in?**
No. My estimate is about 75%, plausibly 70–80. A generous marker who weights the experimental rigour heavily might reach the low 80s. 85+ would need the must-fix and most of the should-fix items, plus a visible gain in readability.

**What stands between it and 90%+?**

1. **Errors and integrity of reporting.** The swapped figure references, the misdescribed "100 ms" comparison, the unreported factorial factor and interaction, the unreported seed-7 500/900 ms runs, and the 300-game row. Each is small, but together they cost credibility in the 30%-weighted Experimentation area.
2. **Analysis quality at the top of the band.** Every claim must be stated at the strength its statistics allow: confounds named, nulls bounded, ratios with uncertainty, small-sample diagnostics with counts, Holm applied consistently, H3 framed as post hoc. The S-alone evidence and a behavioural result (card-type points) should be used to explain *why* the agent wins.
3. **Readability.** Halve the numbers per sentence (move the rest to tables or figures), replace internal codes with words in the Results, and bring Results back towards 700 words. This is the largest single lever on the General mark and on how markers perceive the whole paper.
4. **Originality.** At 90+ a marker expects a contribution beyond re-creating TAG's decoupled / information-set options in BasicMCTS. The card-value model as a shared opponent and rollout model is the candidate. It needs to be framed and tested as the contribution, for example a direct test of the opponent model's prediction accuracy, or a principled fix for the 4-player width problem, which the code already explored with progressive widening and top-k opponents.
5. **Compliance hygiene.** Full prompts in the gen-AI statement, the correct package letter, Times New Roman, template font sizes, and a pruned, readable code package. The disclosure that Claude wrote the code and the text is honest and required, but it raises the bar for showing understanding: the individual reflections should carry that.
