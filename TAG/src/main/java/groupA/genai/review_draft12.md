# ECS7032P Assignment 1: independent marking of Group A (report draft 12 and code)

**How this review was done.** I read the brief, rubric, overview and writing tips (`pdftotext -layout`). I read the report text and rendered every page at 80 and 200 dpi. I read `build_report.py`, `make_figures.py`, all seven Java files, the README, the JSON files and `GENAI_PROMPTS.md`, and I checked that the `genai/` folder exists. I did **not** open `genai/review_draft*.md` or `report/reviews/`: the sandbox copy of `report/` had `reviews/` deleted before anything was read. I recomputed every number with my own scripts, which import nothing from the repository. I tested the conditional wording by building the PDF on a copy (`/tmp/claude-0/review12/sandbox`) with synthetic results for the pending run. **No Java was run.** Nothing under `/home/user/statistics` was changed (`git status` is clean). All my files are in `/tmp/claude-0/review12/`:

- `recompute.py` and its output `recompute_out.txt`
- `numbers_table.py` and `numbers_table.md`
- `scenarios.py`, `build_scen.sh`, `space2.py`
- `patch_sandbox2.py`, which holds the tested edits
- `builds_unpatched/` and `builds_patched/`, which hold the scenario PDFs

---

## 0. Verdict in brief

- **Estimated mark as it stands: 81%** (plausible range 78–84%). The experimental work and the implementation are clearly top-band. The paper is held below 85 by three things:
  - the writing is very dense and full of codes;
  - two of the three contributions are framed more strongly than the tests support;
  - the critical analysis of the literature is thin.
- **Numbers:** all nine rows of Table III (win rate, interval, margin), all 22 plotted effects in Fig. 2, all 13 points in Fig. 3 and every derived number in the text reproduce from the raw files, with these exceptions:
  - **two hard-coded factorial values are wrong**: M is 4.1 ± **2.0**, not ± 2.1; the M × K interaction has z = **2.8**, not 3.0;
  - **one replicate run is left out**: the four-player S + I gain at 100 ms is +6.4 ± 5.2 pp, not +7.8 ± 6.0 pp, once the replicate `q4_Io` is pooled;
  - **one heterogeneous component of a headline claim is not shown**: the seed-7 900 ms change is **+1.6 ± 8.7 pp**, while the pooled change is −4.9 ± 4.8 pp, z = 1.97;
  - **one mixed baseline makes two numbers look inconsistent**: "recovers 17.8 … but still wins 13.6 less" adds up to 31.4, not 28.3.
- **Code vs paper:** the technical description matches the Java exactly. Nothing from `players.mcts` is used, and the agent is a modified BasicMCTS.
- **Literature:** every claim I could check agrees with the cited paper's abstract. I could not open the full texts: `ludii.games` and `ieee-cog.org` are blocked in this session.
- **Compliance:**
  - Page limits are met in every branch.
  - **The author block is unfinished** ("[Group member 2]", "Student no. [to be completed]" three times).
  - The code README begins with "**Rename first:** replace every `groupA` …".
- **Pending run (v3 at 500 ms, held-out deals):**
  - Every branch is grammatical and every branch still fits on page 4 (3.5–4.5 lines spare).
  - **"Contradicts E2" can only happen if v3 beats the final agent at 500 ms.** In that branch the abstract says the decoupled tree "gained less at 500 ms", which would be false. The unqualified claims in the abstract, in contribution 2 and in the RQ1 conclusion would also be contradicted.
  - The "supports E2" clause in the abstract is elliptical.

---

## 1. Rubric marks

**Weights** (Assessment Overview): Abstract 5%, Problem definition 15%, Approach 30%, Experimentation 30%, Conclusions 10%, General 10%. Within an area I weight the items equally; the rubric gives no item weights.

| Area | Item | Band | Mark | Reasons (quotes from the PDF) |
|---|---|---|---|---|
| **1 Abstract (5%)** | Brief motivation | 70–100 | 80 | "Card-drafting games such as Sushi Go! combine hidden hands, simultaneous moves and delayed scoring, which basic Monte Carlo tree search (MCTS) handles poorly." Correct and succinct, but nothing on why the answer matters beyond this one game (drafting games, TAG agents in general). |
| | Brief summary | 70–100 | 78 | The five modifications are named. The design is only implied by "On held-out deals": the leave-one-out ablation, the opponents and the 500 games are not mentioned, and a reader of the abstract alone cannot know what "held-out" means. |
| | Outline of findings | 70–100 (low) | 75 | Seven percentages are packed into one sentence: "wins 52.6%, 47.2% and 44.8% of three-player games (100, 500, 900 ms) … and 44.4% and 36.5% of four-player games (100, 500 ms; chance 33.3%, 25%)". The most interesting findings get two short clauses. "Longer budgets favoured ISMCTS over our agent" rests partly on a borderline 900 ms result (§3.3, C1). |
| **2 Problem definition (15%)** | Motivation | 70–100 | 78 | Clear chain from TAG, to MCTS's assumptions, to the four properties of Sushi Go!. The Introduction cites few works "that study similar concepts" (only [1]–[4]). The motivation is about this game rather than about advancing knowledge. |
| | Problem clarity | 70–100 | 82 | Properties (i)–(iv) and "which adaptations … make an agent stronger (RQ1), and how their value changes with the time per decision (RQ2) and the number of players (RQ3)" are precise. |
| | Contributions | 70–100 (low) | 72 | Bullet 1 calls the model "the component our agent depends on most", but the tests show something narrower (§3.3, C3). Bullet 2 ("TAG's own decoupled information-set agent, without it, is not detectably better") attributes a confounded comparison to the model (C4). Bullet 3 is "exploratory". The importance to knowledge is not argued. |
| | Critical analysis of literature | 70–100 (low) | 73 | **Good:** the DUCT tension (Lanctot: most exploitable; Tak: best overall), the flaws of PIMC via Long et al., SO- vs MO-ISMCTS, and Silver & Tesauro used to set up the X3 result.<br>**Missing:** Tak et al. also compared *sequential* UCT, the direct precedent for v3 against S, and this is not mentioned.<br>**Inverted:** Robilliard et al. cite stage- and opponent-dependent card values as the reason a hand-made evaluation is *hard*; the paper uses them as support ("which our card-value model estimates explicitly"), although its own RQ2 result points to exactly that risk.<br>**Thin:** the gap statement is generic. |
| | Description of the game | 70–100 | 83 | Rules and scoring are complete and correct: I verified the 108 cards, all values, the Maki and Pudding rules and hand sizes 9 and 8 against `SGParameters`. The information structure is spelled out: "every hand of a round has been seen after two passes". |
| **3 Approach (30%)** | Understanding and application | 70–100 | 86 | The description matches the code line by line (§3.1): availability-count UCB, decoupled turn nodes, max-n, an open-loop tree keyed by card kind, round-end rollouts, F checked against the forward model, and 4-player value shaping. This goes beyond the lectures.<br>A minor point: "the second keeps a gradient when the agent is far ahead or behind" is loosely argued. dΔ/dŜᵢ = 1 for any w, so the mean term only changes the sensitivity to the opponents who are not leading; the saturation comes from tanh. |
| | Originality | 70–100 (low) | 76 | The search parts come from the literature, and TAG's `MCTSPlayer` already offers decoupled and information-set search. The paper says so honestly: "The search techniques come from the literature; our own element is the card-value model". The original parts (card-value model, 4-player win-gap value with σ decay, integer simulator with L rollouts) are sensible but modest. The 3-player constants D make a weak greedy policy: 7.25% of greedy games in a pool of D and X0 players, against chance 33.3% (`greedy_duels.txt`). |
| | Difficulty and ambition | 70–100 | 86 | About 3,400 lines in 7 classes, many switches, and careful engineering (simulator checked against the forward model, wall-clock guard). |
| **4 Experimentation (30%)** | Adequacy and rigour | 70–100 | 88 | Five framework agents plus v2 and v3. A five-component leave-one-out ablation with Holm, a lead-heuristic substitution and a 2⁴ factorial on v3. Held-out deals disjoint from all others, and a re-run on a second machine. Budgets of 100, 500 and 900 ms, with 3 and 4 players. 500 games per main condition; the 250-game runs are justified by CPU time.<br>**Missing:** the reason P and M are not ablated (C10), a 3-player run in which only our agent gets more time (C2), and the 4-player settings rest on non-significant 250-game gains (acknowledged). |
| | Quality of documentation | 70–100 | 80 | Table II is unusually complete: seeds, seating, budgets, opponents, hardware, and a pointer to the per-game data in the package.<br>**Gaps:** the harness with the fixed deck order (`Arena.canonicalDeal`) is not in the submitted package (the README points to `experiments_v4` "next to this folder in our repository"). No TAG commit or version is given. ISMCTS's MAST "Both" and exploreEpsilon 0.1 are omitted. The method of pooling is never stated in III-E. |
| | Critical analysis of results | 70–100 | 81 | Excellent error analysis: Wilson intervals, paired SEs, correlation "at most 0.22" (verified: max 0.220), power, Holm, the selection-bias caveat on v3's 50.1%, and the overlap of seed block 7 with tuning deals. Explanations are offered and hedged.<br>**Deductions:** the 900 ms verdict (C1); RQ2 confounds our agent's scaling with the opponents' (C2); the contribution overclaims (C3, C4); one replicate is omitted (C6); two factorial values are misquoted (C8). |
| **5 Conclusions (10%)** | Summary and conclusions | 70–100 | 78 | A faithful RQ-by-RQ summary, but essentially a list of numbers. There is no plain-language statement of what was learned about search in drafting games. "Lowered our win rate … by 4.9 ± 4.8 pp to 900 ms" is stated without a hedge. |
| | Future work | 70–100 | 82 | Concrete and motivated: "learn the evaluation from game outcomes"; "regret matching or EXP3 deserve a test, as decoupled UCT is best overall but not in every game [10] and was the most exploitable in Goofspiel [9]"; opponent models from observed picks. |
| **6 General (10%)** | Clarity of ideas and expression | 70–100 (low) | 72 | **Strengths:** the structure is logical and the wording precise.<br>**Density:** more than 15 single-letter codes (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, E1–E3) collide with the maths symbols: S vs Ŝ; P vs p and "P1–P3" in Fig. 1; B vs bₚ; M vs mₚ; D vs dₚ; R vs r; L as a code and L = 8. A typical sentence: "No four-player setting gained significantly in development (X0: +4.3 ± 4.3 pp, z = 1.94, at 100 ms; X0, W2 and K = 1: +5.8 ± 7.4 pp at 500 ms; F + L: +1.0 ± 7.9 pp at 500 ms, −6.6 ± 6.0 pp at 100 ms); we kept each where its estimate was positive."<br>**Length:** Results run to 1,085 words against the 500–700 guidance.<br>**Unfinished:** the author block still has placeholders. |
| | Figures, tables and legends | 70–100 | 80 | **Figures:** Fig. 1 is conceptual and helpful, Fig. 2 (a paired forest plot with Holm fill) is excellent, and Fig. 3 is clear. Fig. 2 and Fig. 3 use 7 pt text, and Fig. 3 spaces 100/500/900 ms equally.<br>**Tables:** footnote a of Table I and Table II are walls of abbreviations. |
| | Correct referencing | 70–100 | 80 | IEEE style, consistent; 15 relevant, mostly peer-reviewed sources. The claims I checked match their abstracts. Minor gaps: Tak's sequential UCT is not discussed, the origin of DUCT is not cited, and Claude is cited without a model or version. |

**Area means:** Abstract 77.7; Problem definition 77.6; Approach 82.7; Experimentation 83.0; Conclusions 80.0; General 77.3.

**Weighted total:** 0.05·77.7 + 0.15·77.6 + 0.30·82.7 + 0.30·83.0 + 0.10·80.0 + 0.10·77.3 = **80.9 → 81%**.

---

## 2. Recomputation of every number

**Method.** My scripts were written from scratch:

- Wilson 95% intervals with z = 1.95996.
- Score margin as mean ± 1.96 SE, using n − 1 in the variance.
- Paired designs: per-game differences matched by the `game` column (seed-7 and held-out runs, and the later development runs).
- Independent runs (before 19 October, TAG's own deal, listed in `make_v4_tables.py:OLD`): two-sample SE, pooling replicate runs where the paper does.
- Pooling of two deal sets: inverse-variance weights, plus Cochran's Q.
- Holm: two-sided normal p-values.

**Checks of the raw files:**

- Every chunk join (`r2_val_3p500`, `_3p900`, `_4p500`) is identical row by row to its chunks shifted by the stated offsets.
- `focusSeat == g mod n` in every joined and held-out run.
- `v5_field_3p100_all` is `v5_field_3p100` followed by `r2_field_3p100b` shifted by 300.
- The package copy `groupA/results/` equals the experiment folders, apart from the `.err` files (not copied) and `tune_X3.log` (the copy lacks a JVM banner line).

### 2.1 Text and Table III

The full table is reproduced from `numbers_table.md`; all source files are in `groupA_code/experiments_v{3,4,5}/results`.

| # | Where | Report | Recomputed | Source | Status |
|---|---|---|---|---|---|
| 1–2 | Table III 3p/100 held-out | 52.6 [48.2, 56.9]; +0.73 ± 0.46 | same | r2_val_3p100.csv | ✓ |
| 3–4 | 3p/500 held-out | 47.2 [42.9, 51.6]; −0.10 ± 0.40 | same | r2_val_3p500.csv (joined) | ✓ |
| 5–6 | 3p/900 held-out | 44.8 [40.5, 49.2]; −0.35 ± 0.39 | same | r2_val_3p900.csv (joined) | ✓ |
| 7–8 | 4p/100 held-out | 44.4 [40.1, 48.8]; −0.61 ± 0.43 | same | r2_val_4p100.csv | ✓ |
| 9–10 | 4p/500 held-out | 36.5 [32.4, 40.8]; −1.83 ± 0.45 | same | r2_val_4p500.csv (joined) | ✓ |
| 11–12 | 3p/100 DUCT, ISMCTS | 55.0 [50.6, 59.3]; +0.72 ± 0.40 | same | r2_duct_3p100.csv | ✓ |
| 13–14 | 3p/100 ISMCTS × 2 (seed 7) | 51.3 [46.9, 55.7]; +1.41 ± 0.51 | same | v5_field_3p100_all.csv | ✓ |
| 15–16 | BasicMCTS, RHEA | 84.2 [80.7, 87.1]; +6.68 ± 0.60 | same | B_mcts_rhea.csv | ✓ |
| 17–18 | OSLA, BasicMCTS | 87.2 [84.0, 89.8]; +9.74 ± 0.75 | same | B_osla_mcts.csv | ✓ |
| — | Abstract: "52.6%, 47.2% and 44.8% … 44.4% and 36.5%"; "significantly above chance" | | same; every lower Wilson bound is above 33.3% or 25% | as above | ✓ |
| 19–20 | IV-A: seed 7 at 100 ms; re-run | 55.0%; 58.1% | 55.0%; 58.1% | V_3p100, v5_base3p100 | ✓ |
| 21 | "within chance of the held-out result (z ≤ 1.8)" | z ≤ 1.8 | z = 0.76 and 1.77 (two-sample) | as above | ✓ |
| 22–24 | Nigiri / Pudding / Sashimi points per game | 17.6, 15.3, 12.9; Pudding "more than both"; 9.1 vs 3.0 | 17.62, 15.25, 12.94; 2.88 vs 0.49 and 2.50 (bonuses only); 9.08 vs 2.98 | r2_val_3p100.log | ✓ |
| 25–26 | DUCT seat share; minus v2 | 21.0% [17.7, 24.8]; 0.0 ± 5.0 pp | same (two-sample over 500 seats; conservative on paired deals) | r2_duct_3p100.log, r2_val_3p100.log | ✓ |
| 27 | "at most 12.8% of their seats" | 12.8% | BasicMCTS 9.0 / 12.8, RHEA 6.8, OSLA 0.0 | experiments_v4/results/val16.log | ✓ |
| 28 | IV-B −H | 28.3 ± 5.8 pp | 28.3 ± 5.8 (z = 9.6) | A_noH vs V_3p100 | ✓ |
| — | Holm on wins: only H and S + I significant | | H p ≈ 0, S+I p = 0.0005, R p = 0.042, B p = 0.15, I p = 0.19; Holm keeps H and S + I | | ✓ |
| — | Holm on margins: all five significant | | p = 0, 0, 0.0164 (R), 0.0008 (B), 0.0250 (I); all pass | | ✓ |
| 29 | S + I on seed 7 | 10.2 ± 5.7 (z = 3.5) | same | A_noSI vs V_3p100 (A_noSI's JSON = v3's finalT4.json) | ✓ |
| 30–31 | Lead heuristic: "recovers" / "less than H" | 17.8 ± 5.6; 13.6 ± 5.9 (z = 4.5) | same | r2_lead vs A_noH; r2_lead vs v5_base3p100 | ✓, but the two use different baselines (row 32) |
| 32 | 17.8 + 13.6 = 31.4, not 28.3 | — | Against the original base V_3p100 the remaining gap is −10.5 ± 5.8 (z = 3.5) | | **presentation issue** |
| 33–34 | S + I held-out; pooled | +6.1 ± 5.9; +8.2 ± 4.1, z = 3.9 | same (Q = 0.94, p = 0.33) | r2_v3_3p100 vs r2_val_3p100; inverse-variance pool | ✓ |
| 35–36 | S alone; I on top | +6.4 ± 5.7 (z = 2.2); 3.8 ± 5.7 | same | A_noI vs A_noSI; V_3p100 vs A_noI | ✓ |
| 37 | Factorial R | 8.7 ± 2.1 | 8.7 ± 2.1 | experiments_v3, 16 cells (cells as in factorial.py) | ✓ |
| 38 | Factorial M | **4.1 ± 2.1** | **4.1 ± 2.0** (± 1.99; RESULTS.md also says ± 2.0) | experiments_v3 | **MISMATCH** |
| 39 | R × T z | 4.0 | 3.97 (−4.2 pp) | experiments_v3 | ✓ |
| 40 | M × K z | **3.0** | **2.83** (+3.2 pp; RESULTS.md also says 2.83) | experiments_v3 | **MISMATCH** |
| 41–42 | Later changes; game-end rollouts | −1.8 to +0.7 (each ± 5.8); 23.7 ± 5.8 (z = 8.0) | same | v5_* vs v5_base3p100 | ✓ |
| 43 | X3 greedy duel | 45.0% | 45.00 ± 1.06 | greedy_duels.txt | ✓ |
| 44–46 | IV-C development gains; change; drops | +5.6, +9.0, +14.8; +9.2 ± 10.4 (z = 1.7); 17.3, 8.1 | same | d1_I/abl_T4, f5_I/f5_finalT4, f9_I/f9_finalT4 (two-sample) | ✓ |
| 47–48 | S alone 100 / 500 | +1.3 ± 6.1; +6.2 ± 6.1 | same; their difference is +4.9 ± 8.6 (z = 1.1), **not reported** | d1_S, f5_S | ✓ |
| 49–50 | v3 50.1% is the best of 16 cells; new seeds | 50.1%; 45.1–49.4% | max of 16 cells: yes; 49.4, 47.5, 45.1 | abl_T4; rep_T4, tb1_T4, tb2_T4 | ✓ |
| 51 | Held-out S + I at 100 ms; v3 | +6.1 ± 5.9 (z = 2.04); 46.5% | same | | ✓ |
| 52–53 | Held-out 500 − 100; 900 − 100 | −5.4 ± 5.9 (z = 1.8); −7.8 ± 5.8 (z = 2.6) | same | paired | ✓ |
| 54–55 | Pooled 500; pooled 900 | −5.5 ± 4.2 (z = 2.6); −4.9 ± 4.8 (z = 1.97) | same; 500 Q = 0.00; **900 Q = 3.10 (p = 0.08)** | | ✓ |
| 56 | Seed-7 900 − 100 on its own | — | **+1.6 ± 8.7** (56.0% vs 54.4% on games 0–249) | V_3p900 vs V_3p100 | **omitted** |
| 57–59 | ISMCTS and v2 seat shares; ISMCTS 500 − 100 | 26.4/35.9/36.3; +9.5 ± 5.7; 21.0/16.9/18.9 | same (chunk logs weighted by seats) | | ✓ |
| 60 | 4-player S + I at 100 ms | +7.8 ± 6.0 | +7.8 ± 6.0 from q4_I alone; **+6.4 ± 5.2** with the replicate q4_Io (same JSON), which the paper pools elsewhere | q4_final vs q4_I (+ q4_Io) | **replicate omitted** |
| 61–66 | 500 ms S + I; X0; X0+W2+K1; F + L; cost with 3 players; interaction | +3.0 ± 8.4; +4.3 ± 4.3 (z = 1.94); +5.8 ± 7.4; +1.0 ± 7.9 and −6.6 ± 6.0; 7.7 ± 5.4 (z = 2.8); 13.5 ± 9.1 (z = 2.9) | same | val_q5/q5_final; q4_X0(c)/q4_I(o); q5_W2(r)/val_q5; q5_FL8/q5_W2r; q4_X0FL8/q4_X0c; f5_W2FL8/f5_FL8 | ✓ |
| 67–69 | Depth 1; 1000 ms for ours only; 4-player 500 vs 100 ms agent | 17.4 ± 8.2; −4.8 ± 8.1 (80% power only for 12 pp); 6.7 ± 4.1 (z = 3.2) | same; minimum detectable at 80% power 11.6 pp; K = 1.37 is in the JSON | q5_FL8d1, q5_FL8t1000K14, V_4p*, r2_val_4p* | ✓ |
| 70–72 | 4-player ISMCTS seats; seed-7 4-player; tree statistics | 24.1 → 32.1; 46.8, 41.4; 73 positions, 704 children, 53% visited once, 2 and 4 vs 4 and 7 | same (73 × 2.7% = 2; 5.5% → 4; 5.5% → 4; 9.6% → 7) | r2_val_4p100.log + chunk logs; treestats4.txt, agree4.txt | ✓ |
| — | III-E statistics: ±4.4; ±5.7; "at most 0.22"; 8 pp at 80% power; "60 of our 166 runs" | | 4.38; paired 500-game CIs are 5.68–5.94; max ρ = 0.220 (f5_FL8 vs f5_W2FL8); 8.15; 166 unique runs of ≥ 200 games, 60 with 4 players, 71,550 games | all CSVs | ✓ |

### 2.2 Figure values

- **Fig. 2, top** (paired against V_3p100; Δwin / Δmargin):

  | Removed | Δwin (pp) | Δmargin (points) |
  |---|---|---|
  | H | −28.3 ± 5.8 ● | −5.45 ± 0.71 ● |
  | S, I | −10.2 ± 5.7 ● | −1.75 ± 0.58 ● |
  | R | −6.2 ± 5.9 ○ | −0.74 ± 0.61 ● |
  | B | −4.3 ± 5.8 ○ | −0.98 ± 0.57 ● |
  | I | −3.8 ± 5.7 ○ | −0.63 ± 0.55 ● |

- **Fig. 2, bottom** (paired against v5_base3p100):

  | Change | Δwin (pp) | Δmargin (points) |
  |---|---|---|
  | lead heuristic | −13.6 ± 5.9 ● | −1.45 ± 0.60 ● |
  | X3 | −1.3 ± 5.8 ○ | −0.57 ± 0.60 ○ |
  | denial | −1.8 ± 5.8 ○ | −0.32 ± 0.59 ○ |
  | win-gap value with σ decay | −0.6 ± 5.8 ○ | −0.60 ± 0.56 ○ (unadjusted p = 0.036; not significant after Holm) |
  | w = 1 | +0.7 ± 5.8 ○ | −0.09 ± 0.58 ○ |
  | game-end rollouts | −23.7 ± 5.8 ● | −3.58 ± 0.65 ● |

  ● = filled marker, significant after Holm within the group. The filled and open markers in the figure are exactly these. ✓
- **Fig. 3** (Wilson intervals). The plot matches. ✓
  - v2: 24.8 [21.2, 28.8], 18.3 [15.2, 21.9], 18.8 [14.4, 24.1]
  - v3: 50.1 [45.7, 54.5], 40.7 [36.5, 45.0], 32.8 [27.3, 38.8]
  - final: 55.7 [51.3, 60.0], 49.7 [45.3, 54.1], 47.6 [41.5, 53.8]
  - held-out final: 52.6, 47.2, 44.8
  - held-out v3: 46.5 [42.2, 50.9]

### 2.3 Constants and settings checked against code and JSON (all ✓)

- **Evaluation:** σ 15, w 0.7, α 0.5, T 4, π 1, ν 2 (`SushiGoHeuristic`, `CardValueModel`).
- **Table I footnote:** both the X0 tuple and the default tuple.
- **Search settings:** ε 0.2; K 0.5 with 3 players; K 1, terminalScale 2, sigmaRoundDecay 0.5, opponentScoreValue, F and L = 8 with 4 players at 500 ms or more (both competition JSONs); X0 with w 0.7 and K 0.5 in the 4-player 100 ms file.
- **Competition files:** 900/925 ms and 430/450 ms.
- **Our earlier versions:** v2 = H, A, B, D with K 1.41 (`ref.json`); v3 = H, B, D, P, R, M with K 0.5.
- **TAG opponents:**
  - ISMCTS: MultiTree, UCB_Tuned, K 1, RANDOM rollouts of 300 moves, ScoreHeuristic.
  - DUCT: decoupled, LeaderHeuristic, 30 moves, default K 1, Information_Set.
- **Framework agents:** BasicMCTS K 1.41 with 10-move rollouts; RHEA horizon 10, population 10; OSLA with ScoreHeuristic.
- **Special runs:** K 7.5 for the lead-heuristic run and 1.37 for the 1000 ms run.
- **Game facts:** 108 cards, the scoring values, hand size = 10 − n + 2, 729 and 4096 joint moves.
- **CPU time per game:** 45–70 s = 77–90 decisions × budget.

**Not traceable to data, only to the READMEs:** "18-core laptop (6–16 games in parallel)", "OpenJDK 21", "TAG … unchanged". The git history shows TAG imported in one commit and never edited outside `groupA`.

---

## 3. Technical and scientific checks

### 3.1 Description vs Java code: all consistent

- **I** (`informationSetIteration`): `state.copy(me)` re-deals the unseen cards every iteration. The tree is open-loop and keyed by card kind, `(type·4 + icons)·2 + chopsticks`. Exactly one node is added per iteration.
- **Equation (4)** (`selectInformationSet`): untried cards first, then UCB with `log(availability)/visits`, where availability is incremented for every card the player holds in that sample. Without I, `selectDecoupled` uses `ln(N + 1)`. Both match "(without I, the node's visits)".
- **Root decision**: the most-visited own card at the root.
- **M**: per-player value sums with no paranoid sign. **B**: ε-greedy rollouts on marginal values, the Chopsticks second card greedy in the tree. **R**: `finishRollout` stops when the round counter changes.
- **Equation (1)**:
  - Ŝ = banked + α·potential + Maki standing + π·ω_t·Pudding, with ω_t = (round + 1)/3.
  - The Wasabi term is (3 − 1)·ν, i.e. 2ν.
  - Chopsticks count 1, weighted by completion(r, 2).
  - TAG scores Tempura, Sashimi, Dumplings and Nigiri on reveal (`SGCard.onReveal`).
- **Equations (2)–(3)**: match `valuesOf`.
- **W2**: `terminalValues` gives tanh((x_i − max x_j)/2) with x = score + 0.01·Puddings; `roundScale` gives (ρ/2)^0.5; `scoreValue` makes opponents value their lead over the others' mean.
- **F**: `SushiSim` exists, and `experiments_v4/dev/groupA/SushiSimCheck.java` compares it with the forward model; I did not run it.
- **Base agent**: no imports from `players.mcts`; `SushiMCTSPlayer` and `SushiTreeNode` are documented copies of `BasicMCTSPlayer` and `BasicTreeNode`.
- **JSON files**: all 53 SushiMCTS JSON files in `groupA/json` use only registered parameters.
- **Two technical facts the paper does not use.** Both make P and M impossible to remove from the final agent on their own:
  - S and I require M (the `decoupled` flag and the I root both test `useMaxN`).
  - P has no effect once I is on, because `distinctCards` always merges identical cards.

  "P and M were not ablated in the final agent" should give this reason.

### 3.2 Literature claims vs the cited papers

Abstracts were checked via search results. The full texts could not be opened because `ludii.games` and `ieee-cog.org` are blocked here; I have not guessed beyond the abstracts.

- **[4] Klang et al.** The abstract frames Sushi Go! as combining "simultaneous action selection and complete information", and says the agents "perform with mixed results" and that further heuristics "should prove to increase their performance". The paper's paraphrase is accurate. I could not see how the full text defines "complete information".
- **[9] Lanctot et al.** The abstract says "regret matching and OOS perform best and … all variants produce less exploitable strategies than UCT". The paper's "decoupled UCT's strategies were the most exploitable" is accurate.
- **[10] Tak et al.** The abstract covers nine games and the variants Decoupled UCT, **Sequential UCT**, Exp3 and Regret Matching; it finds that performance "depends strongly on the game and the opponent" and that "Decoupled UCT performs best". The paraphrase is accurate, but the Sequential UCT comparison, which is directly relevant to v3, is not mentioned. Pages 232–239 ✓.
- **[11] Sturtevant.** The abstract says multi-player UCT computes a mixed-strategy equilibrium and does "as well or better" than existing algorithms. Accurate.
- **[12] Robilliard et al.** The abstract says card values depend on the game stage and on opponents' choices, which makes a hand-made evaluation hard, and that many known MCTS results still hold. The paper reports the dependence correctly, but turns a warning into support (see §1).
- **[7] Long et al.** The paper's "how quickly hidden information is revealed" corresponds to Long et al.'s disambiguation property. Accurate.
- **[8] Goodman et al.** Pages 292–299 ✓. Its content was not reachable.
- **[2], [3], [5], [6], [13], [14].** The claims are standard and correct to my knowledge: UCB1, progressive bias and UCB-Tuned in the survey; ISMCTS and strategy fusion; SO- vs MO-ISMCTS; Silver & Tesauro's simulation balancing; Wilson; Holm.

**No misreporting found.**

### 3.3 Conclusions vs the statistics next to them

| # | Issue | Evidence | Severity |
|---|---|---|---|
| C1 | "Giving every agent more time lowered our win rate … 900 ms … by −4.9 ± 4.8 pp (z = 1.97)". The abstract says "Longer budgets favoured ISMCTS over our agent". | The pooled 900 ms estimate is borderline (p = 0.049) and comes from **heterogeneous** deal sets: seed 7 alone gives +1.6 ± 8.7 and held-out −7.8 ± 5.8 (Q = 3.1, p = 0.08). The seed-7 number is not shown. Holm over the four separate budget tests keeps only held-out 900 ms. The ISMCTS seat gains (+9.5 and +9.9 pp) are solid, and so is the 500 ms pooled estimate (z = 2.56, Q ≈ 0). | must (wording) |
| C2 | "Plausibly, more iterations cannot correct the bias of our hand-made rollouts and evaluation, while ISMCTS … keeps gaining". | Every agent gets the same budget, so our agent's own scaling is confounded with ISMCTS's. There is no 3-player run in which only our agent gets more time. The paper's iteration probe (2,387, 11,865 and 21,246 iterations per decision at 100/500/900 ms) would at least show that the extra time is used. | should |
| C3 | Contribution 1: the model is "the component our agent depends on most". | The ablation removes the *evaluation* H. With R on, the leaves are at round ends, where uₚ = mₚ = 0, so H sees banked points, the Pudding standing and the bounded lead shape. III-B says this itself. The model's card values in the rollouts (B) cost −4.3 ± 5.8 pp, not significant on wins. The lead-heuristic test (−13.6) cannot tell the Pudding term from the shape or the K scaling. | must (wording) |
| C4 | Contribution 2: "TAG's own decoupled information-set agent, without it, is not detectably better". | DUCT also differs in its rollouts (random, 30 moves), evaluation (lead), K and implementation. The Results section's own hedge, "with its own heuristic and rollouts", is the correct version. | must (wording) |
| C5 | Lead heuristic: "recovers 17.8 ± 5.6 pp of this loss … but still wins 13.6 ± 5.9 pp less than H". | The two numbers use different baselines: 55.0% on the first machine and 58.1% on the second. The visible arithmetic does not add up (17.8 + 13.6 ≠ 28.3). | must |
| C6 | Four-player S + I at 100 ms is +7.8 ± 6.0. | The replicate q4_Io (same JSON) is omitted, although the X0 comparison pools q4_I with q4_Io. Pooled, the gain is +6.4 ± 5.2. The GenAI statement says "all replicate runs" were requested. | must |
| C7 | S alone "gained +1.3 … but +6.2 … at 500 ms, as this predicts". | The rise is +4.9 ± 8.6 (z = 1.1). The paragraph does hedge ("probably overstated"), but "as this predicts" reads as confirmation. | should |
| C8 | Factorial: "M 4.1 ± 2.1 pp … (z = 4.0, 3.0)". | The correct values are ± 2.0 and z = 2.8. Both are hard-coded in `text_ablation()`, while every other number is computed. | must |
| C9 | "With four players we found no gain from more time" | The 500 vs 100 ms comparison also changes the agent's settings (W2, F + L, K = 1). The paper lists these settings, but the sentence presents the result as a pure time effect. | nice |
| C10 | "P and M were not ablated" | The reason (§3.1) is missing. | should |
| C11 | Multiplicity | Holm is applied only within Fig. 2. Several borderline z values (1.94, 1.97, 2.04, 2.2) carry the narrative. III-E does state "We report unadjusted intervals". | nice |
| C12 | E2 held-out test (pending) | The 80%-power minimum detectable effect is about 12 pp. The development rise from 100 to 500 ms was +3.4 pp, so "inconclusive" is the most likely outcome. The text handles this correctly by printing the power. | — |

---

## 4. Compliance

- **Page limit.** ✓ The main text ends on page 4 with 6.5 lines spare. The Generative-AI statement and the references run onto page 5; the total is 5 pages. With the pending result, 3.5–4.5 lines remain, and the Generative-AI heading moves to page 5, which is allowed (measured in `builds_unpatched/`).
- **Template.** ✓ The IEEE Word template of the module is used. Two trivial deviations:
  - The PDF fonts are Liberation Serif, metric-compatible with Times New Roman, which is not installed.
  - In the two-column section the side margins are 44.65 pt, against 45.35 pt in the template; LibreOffice changed this when the template was re-saved.

  The first-page copyright footer is re-used for the AI note, which is acceptable.
- **Word guidance** (from the build's own count). Under or within guidance: Abstract 117 (75–125 ✓), Introduction 449 (350–450 ✓), Background 702 (650–850 ✓), Conclusions 277 (200–300 ✓). Over guidance: **Method 901** (700–800, about 13% over) and **Results 1,085** (500–700, about 55% over). Packing this much in makes the paper denser and harder to read.
- **References.** 15, within the 6–15 range. IEEE style, numbered by first use, no citations in headings.
- **≥ 500 games.** ✓ for every headline comparison. The 250-game runs are justified in Table II ("45–70 s of CPU time per game"). Under the rubric's bracket note ("less than 500 games per pair … insufficient"), the RQ3 statements that rest on 250-game runs remain the weakest part.
- **Generative-AI rules.**
  - ✓ Each section is attributed to the AI by the first-page note ("All sections, tables and figures of this paper were drafted with Claude [15]").
  - ✓ The statement comes before the references and gives the report prompt verbatim with a translation.
  - ✓ The reviewer prompts and reports (11 of each) are referenced in `GENAI_PROMPTS.md` and `genai/`, which exist.
  - The development prompts for the code are not reproduced. The brief requires prompts only for report text, so this is acceptable.
  - Keep `genai/` complete: this review and its prompt belong there if they are used.
  - **Risk outside the rubric:** the verbatim prompt ("It should be a report that gets 100 … do not leave the loop until you are sure it will get 100%") and the statement that Claude wrote all the code and text are honest and allowed. They may still colour a marker's reading, and the brief warns that a from-scratch MCTS will be read as "you just asked an LLM to do it". If the students made or checked decisions themselves, the statement may say so truthfully; it must not claim more than happened.
- **Code package rules.**
  - ✓ Package `groupA`, with a README (constructor, initialisation, parameters, competition files) and JSON files.
  - ✓ No changes outside the package, and no `players.mcts`.
  - ✗ The README opens with "**Rename first:** replace every `groupA` … with your real group letter". Remove the note if the group really is A; otherwise rename everything.
  - The README's headline table shows the **seed-7** win rates (56.0% at 900 ms), not the held-out ones in the report (44.8%). It also says "about 165 runs", where the report says 166.
  - The package carries 6.5 MB of results and the `genai/` folder. That is allowed, but well beyond "just the package directory that contains your code changes".

---

## 5. Presentation

1. **Unfinished author block:** "[Group member 2]", "[Group member 3]", "Student no. [to be completed]" three times.
2. **Codes and notation clashes.** Seventeen codes are used (H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D and E1–E3), several of which collide with the maths:
   - S (decoupled UCT) vs Ŝₚ (estimated score);
   - P (pruning) vs p (player index) and "P1–P3" in Fig. 1, whose caption says "(S, P)";
   - B, M, D, R vs bₚ, mₚ, dₚ, r;
   - L as a code and L = 8;
   - "X0" (four-player constants) has no stated reason for its name;
   - v2/v3/v4 and E1–E3 add to the load.
3. **Number-packed sentences.** Typical cases are the first paragraph of IV-D, the second paragraph of IV-C, III-D ("Its files for 500 ms or more also use W2 – w = 1; finished games valued by tanh((bᵢ − maxⱼ≠ᵢ bⱼ)/2) with Pudding as tie-break; σ multiplied by (ρ/2)^0.5 for ρ rounds left; …") and footnote a of Table I.
4. **Undefined terms:**
   - "non-locality" (II-C) is never explained;
   - "within chance of" (IV-A) is jargon;
   - "which (1) captures" (III-B) is awkward;
   - "an agent's copy re-deals all unseen cards" (I) leaves out "of the game state".
5. **Figures.**
   - Fig. 2 and Fig. 3 use 7 pt tick and label text; IEEE prefers 8 pt or more.
   - Fig. 3 places 100, 500 and 900 ms at equal spacing; a log axis would show the real spacing.
   - Fig. 3's caption "win rate against v2 and TAG's ISMCTS" also covers the v2 line, which is v2 playing against v2 and ISMCTS. That is fine, but worth saying.
6. **Tables.** Table II is a wall of abbreviations in a narrow column. Table III is clear.
7. **Positives.** The structure follows the brief. Every figure and table is referenced in the text. The captions are informative, and Fig. 2 is a model of how to show paired effects with multiplicity.

---

## 6. Pending run (v3 at 500 ms on the held-out deals): every branch checked

I generated synthetic `r2_v3_3p500.csv` files for v3 win rates from 28% to 55% (`scenarios.py`). For each one I printed `_abstract_end()`, the E2 sentence of III-E, `text_budget()` and `text_conclusion_1()`, and built full PDFs for representative cases. Because RISE equals GAIN500 − GAIN100 exactly, with GAIN100 = +6.1 and SE(RISE) ≈ 4.3, the outcome regions are fixed in advance:

| v3 win % at 500 ms | GAIN500 | RISE verdict | Text produced (current code) | Problems |
|---|---|---|---|---|
| ≲ 32.7 | ≳ +14.5 | supports (z ≥ 1.96) | Abstract adds "; the decoupled tree gained more at 500 ms" (125 words). IV-C: "a difference of +13.1 ± 8.3 pp (z = 3.1), which supports E2". Conclusions: "the held-out test supported E2 (…)". | "gained more at 500 ms" never says *than what*, nor that the gain is over v3. Contribution 2 could then say "at 100 and 500 ms". |
| 32.7–47.2 (most likely; the development analogue is 37–41) | 0 to +14.5 | inconclusive | IV-C: "a difference of +4.1 ± 8.6 pp (z = 0.9), which is inconclusive (80% power only for 12 pp)". Conclusions: "the held-out test was inconclusive for E2". | Grammatical and consistent. Only "was inconclusive for E2" is awkward; use "did not settle E2". |
| 47.2–≈49.5 | ≤ 0 | inconclusive | As above, with a negative GAIN500, e.g. "+1.2 ± 6.2" turning into "−1.8 ± 6.2 pp at 500 ms". | The abstract's "the decoupled tree also raise[s] the win rate", contribution 2's "beats a determinised sequential tree" and RQ1's "also lowers the win rate" now hold **only at 100 ms** but are unqualified. |
| ≳ 49.5 | ≲ −2.4 (necessarily) | contradicts (z ≤ −1.96) | Abstract: "; the decoupled tree gained less at 500 ms". IV-C: "…which contradicts E2: the held-out test does not support the sequential-tree explanation". Conclusions: "contradicted E2". | **"gained less" is false**, since the tree *lost* to v3 at 500 ms. The same unqualified claims as the row above apply, now possibly contradicted significantly (at 55%, GAIN500 = −7.8 ± 6.2, z = −2.5). |

- **The E2 sentence in III-E** ("E2 arose from the development runs and is tested on the held-out deals.") is correct in every branch. **The Fig. 3 caption and the figure** (the v3 marker at 500 ms appears) are correct.
- **Page budget** (main text, measured):

  | Build | Supports | Inconclusive / contradicts |
  |---|---|---|
  | Current code | 4.5 lines spare | 3.5 lines spare |
  | With all my edits (§7) | 3.5 lines spare | 2.5 (inconclusive) / 1.5 (contradicts) lines spare |

  Page 4 is never exceeded.

**Recommended code** for the abstract clause in `_abstract_end()`. It is tested, and is part of `patch_sandbox2.py`:

```python
g5 = GAIN["3p500"]
if RISE is not None and g5 is not None and g5["dw"] <= 0:
    s += "; at 500 ms the decoupled tree no longer beat v3"
elif RISE is not None and abs(RISE["zw"]) >= 1.96:
    s += "; the decoupled tree's edge over v3 " + ("grew" if RISE["zw"] > 0 else "shrank") + " at 500 ms"
```

Add the same `g5["dw"] <= 0` test to append " at 100 ms" in three places:

- after "also raise the win rate" (abstract);
- in contribution 2 (or "at 100 and 500 ms" when GAIN500 is significant);
- in RQ1's "also lowers the win rate".

In `text_conclusion_1()` change 'was inconclusive for' to 'did not settle'. With these changes the abstract has 127 words in the supports branch and 130 in the GAIN500 ≤ 0 branches, slightly over the approximate 125 limit.

---

## 7. Prioritised changes

**How the edit set was tested.** I applied the whole set below (M2–M7, S1–S5, S9 and cuts K1–K4) to the sandbox copy (`/tmp/claude-0/review12/patch_sandbox2.py`; every old string is asserted to occur exactly once). Results:

- Current results: the main text ends with **3.5 lines** spare, down from 6.5.
- With the pending result: **2.5 lines** (inconclusive), **3.5** (supports), **1.5** (contradicts).

Word counts per line are about 8.5 in the body and about 7 in the bullets. The length given for each single item is an estimate; only the combined effect was measured. All quotes below are as they render in the PDF.

### MUST

| # | Change | Old → new | Length |
|---|---|---|---|
| M1 | Author block (`AUTHORS`) | Fill in names and student numbers; remove "[Group member 2/3]" and "[to be completed]". | 0 (separate section) |
| M2 | Factorial values (`text_ablation`) | "R added 8.7 ± 2.1 pp and M 4.1 ± **2.1** pp, with R × T and M × K interactions (z = 4.0, **3.0**)" → "… M 4.1 ± **2.0** pp … (z = 4.0, **2.8**)". Better still, compute them from the CSVs like everything else. | 0 |
| M3 | Lead heuristic: one baseline per comparison (`text_ablation`) | "TAG's lead heuristic (own score minus the best opponent's, K rescaled to 7.5) recovers 17.8 ± 5.6 pp of this loss (same deals, second machine) but still wins 13.6 ± 5.9 pp (z = 4.5) less than H." → "TAG's lead heuristic (own score minus the best opponent's, K rescaled to 7.5) won 44.5% (own score / 50: 26.7%), 13.6 ± 5.9 pp (z = 4.5) less than H re-run on the same, second machine (58.1%)." | +0.3 line |
| M4 | Pool the four-player replicate (`SI4[100]`: add `q4_Io.csv`) | IV-D "+7.8 ± 6.0 pp over v3 at 100 ms" → "+6.4 ± 5.2 pp"; Conclusions "(7.8 and 3.0 pp …)" → "(6.4 and 3.0 pp …)". | 0 |
| M5 | 900 ms: show the heterogeneity (`text_budget2`, `text_conclusion_1`) | IV-C "and 900 ms (250 seed-7 deals) by −4.9 ± 4.8 pp (z = 1.97)." → "and 900 ms by −4.9 ± 4.8 pp (z = 1.97; seed block 7 alone: +1.6 ± 8.7 pp)."; Conclusions "and by 4.9 ± 4.8 pp to 900 ms" → "and, less clearly, by 4.9 ± 4.8 pp to 900 ms". | +0.5 line |
| M6 | Contribution bullets (`bullet(...)` ×2) | (1) "a model of Sushi Go!'s scoring that values every card for all players' rollouts and every player's final score for a bounded, lead-based evaluation, the component our agent depends on most;" → "a model of Sushi Go!'s scoring that values every card for all players' rollouts and gives a bounded, lead-based evaluation of every player's estimated score; this evaluation is the component our agent depends on most;"<br>(2) "… beats a determinised sequential tree, while TAG's own decoupled information-set agent, without it, is not detectably better than our previous agent;" → "… beats a determinised sequential tree at 100 ms [or 'at 100 and 500 ms', computed], while TAG's own decoupled information-set agent, with random rollouts and a plain lead heuristic, is not detectably better than our previous agent;" | +1.5 lines |
| M7 | Branches for the pending run | See §6: the abstract clause, the "at 100 ms" qualifiers when GAIN500 ≤ 0, and "did not settle". | 0 to +0.3 line |
| M8 | Code package | Remove or act on the README's "Rename first" note. Make the README's headline table show the held-out results (52.6/47.2/44.8/44.4/36.5) with seed 7 as secondary, and change "about 165 runs" to 166. | none in the report |

### SHOULD

| # | Change | Text | Length |
|---|---|---|---|
| S1 | Name the pooling method (IV-B, first use) | "(pooled +8.2 ± 4.1 pp, z = 3.9)" → "(inverse-variance pooled: +8.2 ± 4.1 pp, z = 3.9)" | +0.2 line |
| S2 | Why P and M are not ablated (`text_ablation`) | "P and M were not ablated in the final agent; in a 2⁴ factorial on v3 (R, M, K = 0.5 or 1.41, T = 8 or 4; 16 × 500 games, independent deals) …" → "P and M cannot be removed alone (I's card-kind statistics merge identical cards; S and I need M); in a 2⁴ factorial on v3 (R, M, K, T; 16 × 500 games, independent deals) …" | ≈0 |
| S3 | RQ2 confound in the limitations (`text_conclusion_2`) | "…250-game gains, and CPU-time budgets hide wall-clock effects." → "…250-game gains, CPU-time budgets hide wall-clock effects, and equal budgets confound our agent's scaling with that of ISMCTS." | +0.8 line |
| S4 | Critical literature (II-C) | (a) "Tak et al. [10] found across nine games that the best variant depends on the game and the opponent, although decoupled UCT was best overall." → "Tak et al. [10] compared decoupled and sequential UCT, EXP3 and regret matching in nine games: the best variant depended on the game and the opponent, although decoupled UCT was best overall."<br>(b) "…on the other players' choices [12], which our card-value model estimates explicitly." → "…on the other players' choices, which makes a hand-made evaluation hard [12]; our card-value model attempts one." | +1.5 lines |
| S5 | Hedge (IV-C) | "as this predicts" → "in the direction this predicts" | +0.3 line |
| S6 | Notation (no length change) | In Fig. 1, rename P1–P3 to "player 1–3". Explain or rename X0/X3 (e.g. C4 = constants for 4 players, C3 = constants for 3 players). Typeset modification codes in bold everywhere, not only in III-B to III-D. Define "non-locality" or drop it. "which (1) captures" → "which (1) includes". | 0 |
| S7 | Figures | Log-scale x axis in Fig. 3; text at 8 pt or more in Figs. 2–3. | 0 to +1 mm |
| S8 | Replicability | Table II: add the TAG commit, ISMCTS "MAST Both, explore ε 0.1", and where the harness lives. Put the harness (`Arena.java` with `canonicalDeal`, `run.sh`) in the package or give a repository URL. | +1 line in the table |
| S9 | Wording (IV-A) | "within chance of the held-out result" → "consistent with the held-out result" | 0 |
| S10 | RQ2 evidence (if space allows) | After "our parameters were also tuned at 100 ms" add: "(our agent ran 2.4k, 11.9k and 21.2k iterations per decision at 100, 500 and 900 ms)". | +1 line |

### Cuts used to pay for the above (all in the tested set)

| # | Cut | Old → new | Saving |
|---|---|---|---|
| K1 | IV-D development list | "No four-player setting gained significantly in development (X0: +4.3 ± 4.3 pp, z = 1.94, at 100 ms; X0, W2 and K = 1: +5.8 ± 7.4 pp at 500 ms; F + L: +1.0 ± 7.9 pp at 500 ms, −6.6 ± 6.0 pp at 100 ms); we kept each where its estimate was positive." → "No four-player setting gained significantly in development (X0, W2 and K = 1: +5.8 ± 7.4 pp at 500 ms; other gains z < 2; F + L lost 6.6 ± 6.0 pp at 100 ms); we kept each where its estimate was positive." | −1.3 lines |
| K2 | IV-A card points | "At 100 ms it scores more than both opponents from Nigiri (17.6 points per game against 15.3 and 12.9) and Pudding, v2 more from Sashimi (9.1 against 3.0)." → "At 100 ms it outscores both opponents on Nigiri and Pudding; v2 collects more Sashimi." | −1.6 lines |
| K3 | II-C redundant sentence | Delete "We build on the minimal BasicMCTS, which lacks these features, so that each one's contribution can be measured." The Introduction and III already say this. | −2 lines |
| K4 | IV-B later changes; the figure already names them | "Five later changes (Fig. 2, bottom) brought no detectable gain: card constants tuned for three players (X3), denial of the next player's favourite card in the rollouts, a win-gap value with σ decay and w = 1 changed the win rate by −1.8 to +0.7 pp (each ±5.8), although X3 is the much stronger greedy policy (45.0% of games against two X0 players); rollouts to the game end with a win-gap value cost 23.7 ± 5.8 pp (z = 8.0)." → "Four later changes (Fig. 2, bottom) moved the win rate by −1.8 to +0.7 pp (each ±5.8), although the three-player constants X3 are the much stronger greedy policy (45.0% of games against two X0 players); rollouts to the game end cost 23.7 ± 5.8 pp (z = 8.0)." | −2.6 lines |

**Further cuts if S10 or a Discussion paragraph is wanted:**

- Shorten the factorial sentence to "In a 2⁴ factorial on v3 (16 × 500 games) R added 8.7 ± 2.1 pp and M 4.1 ± 2.0 pp." The interactions stay in the README. Saves about 1.5 lines.
- Merge Table II's "Hardware, records" row into one line. Saves 1 table line.
- Drop "(same deals, second machine)"-style asides once M3 is in.

### NICE

- Name the Claude model and version in [15].
- Install Times New Roman before the PDF build.
- Restore the template's 45.35 pt side margins (only if the page budget allows).
- Add "(unadjusted)" to the borderline z values used in the narrative (1.94, 1.97, 2.04).
- C9: write "the 500 ms agent with its 4-player settings" so the result is not read as a pure time effect.
- If space allows, add a 3–4 line Discussion that pulls the mechanisms together:
  - the evaluation's lead shape and Pudding standing drive play;
  - decoupled information-set search fixes an impossible model;
  - extra time helps the model-free opponent more;
  - the 4-player tree is a wide root bandit.

---

## 8. Plain answer

**Would it get ≥ 85%? Not reliably.** As it stands I would mark it at **81%** (a strict marker 78%, a generous one 84%).

- **The experimental protocol and the implementation are at or near 90-level work.** That covers the held-out deals, paired designs, ablations with Holm, power and selection-bias caveats, and a description that matches the code exactly.
- **What stands between it and 90+:**
  1. **Readability.** The paper reads like a dense statistical appendix. Results run 55% over guidance, with sentences of six or more estimates, seventeen internal codes and clashing notation, so a reader "outside this class" cannot follow it (General, Abstract, Conclusions). Fixing this needs a rewrite of III-D, IV-B to IV-D and the abstract around a few key results, with secondary numbers cut (K1–K4 plus the further cuts free about 8 lines).
  2. **Claims that outrun the tests:**
     - contribution 1 credits the card-value model with what the tests show for the evaluation (C3);
     - contribution 2's "without it" (C4);
     - the 900 ms verdict with its omitted seed-7 component (C1);
     - the mixed lead-heuristic baselines (C5).
  3. **Critical depth in the background:** Tak's sequential-UCT comparison, and Robilliard's warning about hand-made evaluations, which the RQ2 finding actually supports.
  4. **Small but visible errors:**
     - two misquoted factorial values (C8);
     - one omitted replicate (C6);
     - an unfinished author block;
     - the "Rename first" note in the code README;
     - in the pending-run branches, a false "gained less" and unqualified claims.
- **Expected effect of the fixes:** the MUST items take it to about **83%**; MUST plus SHOULD to about **84–85%**. A genuine readability rewrite within the page limit could reach **86–88%**. **90%+** would also need the contributions framed around what the evidence shows (the evaluation shape and the decoupled information-set tree), a sharper statement of what is new beyond the literature, and a discussion that interprets rather than lists. That is achievable only with substantial cuts of secondary results, and is rarely awarded to a four-page student paper.

---

### Sources consulted for the literature checks (abstracts only; full texts blocked)

- Klang et al. 2021: [IEEE Xplore 9618987](https://ieeexplore.ieee.org/document/9618987/), [Semantic Scholar](https://www.semanticscholar.org/paper/Assessing-Simultaneous-Action-Selection-and-in-TAG-Klang-Enh%C3%B6rning/04c410caf8b39ffabdc9e8887c438be6c801a345). The PDF hosts `ludii.games` and `ieee-cog.org` are blocked in this session and must be allowed in the network settings to check the full text.
- Robilliard et al. 2014: [Springer](https://link.springer.com/doi/10.1007/978-3-319-14923-3_5), [HAL](https://hal.archives-ouvertes.fr/hal-01406496)
- Lanctot et al. 2013/2014: [Maastricht CRIS](https://cris.maastrichtuniversity.nl/en/publications/monte-carlo-tree-search-in-simultaneous-move-games-with-applicati/), [ML Anthology](https://mlanthology.org/ijcai/2013/lanctot2013ijcai-monte)
- Tak et al. 2014: [Maastricht CRIS](https://cris.maastrichtuniversity.nl/portal/en/publications/monte-carlo-tree-search-variants-for-simultaneous-move-games(5e4c8118-4db7-49bf-b12d-d16ee90fe167).html)
- Sturtevant 2008: [author page](https://www.cs.du.edu/~sturtevant/papers/sturtevant2008uct.html)
- Long et al. 2010: [AAAI](https://ojs.aaai.org/index.php/AAAI/article/view/7562), [lecture slides on its properties](https://www.cs.du.edu/~sturtevant/w13-games/Lecture10.pdf)
- Goodman et al. 2022 (page numbers only): [QMUL publications](https://researchpublications.qmul.ac.uk/publications/staff/42633.html)
