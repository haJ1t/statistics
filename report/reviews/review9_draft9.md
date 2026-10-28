# Review 9: ECS7032P Assignment 1, Group A ("Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!")

Independent strict marking of draft 9 (commit 1012d53, PDF built 28 Oct 04:13). I did not read `groupA/genai/review_draft*.md` or `report/reviews/`. All my work is in `/tmp/claude-0/review9/`; nothing under `/home/user/statistics` was modified, and I ran no Java while the held-out run is in progress, because the machine has 3.3 GB of free RAM and no swap.

- Recomputation script, written from scratch: `/tmp/claude-0/review9/recompute.py`. Output: `/tmp/claude-0/review9/recompute_out.txt`.
- Branch tests of the pending wording, using synthetic results in a copy of the build: `/tmp/claude-0/review9/sandbox/scenarios.py`. Outputs: `/tmp/claude-0/review9/sandbox/scen_a.txt`, `/tmp/claude-0/review9/sandbox/scen_b.txt`, and the per-scenario PDF texts `/tmp/claude-0/review9/sandbox/S*.txt`.

## 0. Verdict in one paragraph

The experiments are unusually rigorous for this module:
- held-out validation on disjoint deals;
- paired designs matched by game number (I checked that the chunk merges keep the numbering);
- a Holm correction where the paper says so;
- a power statement;
- leave-one-out ablations and a 2^4 factorial;
- five framework agents (ISMCTS, DUCT, BasicMCTS, RHEA, OSLA, plus a field of two ISMCTS) and two earlier versions of the agent.

Every number I could recompute from the raw files matches: 99 checks, no substantive mismatch. The technical description matches the Java code, and I found no misreported source.

What holds the mark down:
1. **The paper is not finished.** It still shows "[pending]" results and "[Group member 2/3]" / "Student no. [to be completed]".
2. **The abstract's budget claim is wrong in most outcomes.** "Longer budgets favoured ISMCTS over our agent" ignores 900 ms entirely. The seed-block-7 data already contradict it (900 ms: 56.0 % vs 55.0 %), and so does the first 126-game chunk of the pending 900 ms run (51.2 %, +0.8 ± 11.8 pp against 100 ms on the same games).
3. **One sentence is wrong now.** "pooled with seed block 7 (250 deals at 900 ms), 500 ms changed it by −5.5 ± 4.2 pp" attaches the 900 ms deal count to the 500 ms pool.
4. **A typo:** "held−out" is printed with a minus sign.
5. **The write-up is very dense.** The Results run to 1093 words against a 500–700 guide, carry 3–5 statistics per sentence, use many internal codes, and use n for three different things.

My estimate: **about 79 % as it stands**, about 80–81 % with only the must-fixes, and 83–85 % with the should-fixes. It is not ≥ 85 % today, and 90 %+ needs more than corrections (Section 8).

---

## 1. Rubric marking

Area weights are from "Assessment Overview": Abstract 5 %, Problem definition 15 %, Approach 30 %, Experimentation 30 %, Conclusions 10 %, General 10 %. Items are weighted equally within an area. Bands: A = 70–100, B = 50–69, C = < 50.

### 1) Abstract (5 %): area mark 72

**Brief motivation: A, 70.**
- The only motivation is "players pick cards simultaneously from hidden, rotating hands, which basic Monte Carlo tree search (MCTS) ignores."
- It is correct and succinct, but it never says why this matters (drafting games and imperfect-information, simultaneous-move search as a class).

**Brief summary of the work: A, 78.**
- All components are named.
- "card-value rollouts, a lead-based evaluation" hides the paper's own contribution (one model feeding both).

**Outline of findings: B, 68.**
- It contains "[pending]%".
- "Longer budgets favoured ISMCTS over our agent" generalises from 500 ms only (see 3.3).
- "Every ablated component raises the score margin significantly, the evaluation and decoupled tree also the win rate" is awkward gapping.

### 2) Problem definition (15 %): area mark 78

**Motivation of the work: A, 75.**
- The game's properties and BasicMCTS's three shortcomings are clearly set out ("TAG's BasicMCTS addresses none of the first three…").
- There is little on why the question matters beyond this game.

**Clarity of the problem definition: A, 82.**
- The four properties (i)–(iv) are crisp: 729 / 4 096 joint moves, delayed scoring.
- RQ1–RQ3 are explicit.

**Clarity of contributions: A, 76.**
- Four honest bullets; the third is labelled "exploratory".
- Their importance is argued only in the Background gap sentence.

**Critical analysis of the literature: A, 76.**
- Good critical points:
  - decoupled UCT's lack of convergence and exploitability [9];
  - "best variant depends on the game" [10];
  - PIMC's flaws versus Long et al.'s explanation of its success [7];
  - single-observer versus multiple-observer ISMCTS;
  - a clear gap statement ("we know of no study that separates the contributions…").
- Missing:
  - Cowling et al.'s own treatment of simultaneous / hidden moves as partially observable moves (ISMCTS + POM), the obvious alternative to decoupling;
  - any source for strategy fusion and non-locality.
- "We build on BasicMCTS … to measure each one's contribution" glosses over the fact that the module rules require it.

**Description of the game: A, 82.**
- Accurate. I checked it against `SGParameters` / `SGCard`: 108 cards, the Dumpling increments, the Pudding −6 rule only with more than two players, hands of 12 − n.
- It links the rotation of hands to how hidden information is resolved.

### 3) Approach (Methods + code) (30 %): area mark 80

**Understanding and application: A, 80.**
- Equations (1)–(4), B, R, P, M, S, I, X0, W2, F and L all match the code (Section 3.1).
- The availability-count UCB is implemented as described.
- Deductions:
  - eq. (4) is the S+I form only; S alone uses the node's visit count;
  - `SushiTreeNode.java` is 1 753 lines, with about 15 inactive experimental switches (A, O, G, U, K, Q, E, MAST and others), which hurts clarity;
  - the README's "construct by hand" example assigns fields directly, and those values are lost on `copy()` (TAG copies only registered parameter values).
- Risk: the GenAI statement credits Claude with all code and text and describes no human contribution. The brief warns about LLM-written MCTS, so the marker may discount "understanding". This is not a rubric deduction for acknowledged use, but it is a real risk.

**Originality: A, 74.**
- The paper itself says: "The search techniques come from the literature; our own element is the card-value model."
- The model, the four-player switch and the fast simulator with 8 rollouts per leaf (F + L) are genuine additions, but the core is a sensible combination of published methods.

**Difficulty and ambition: A, 85.**
- Decoupled UCT inside an open-loop single-observer ISMCTS tree, max-n backups, a domain model, a rules re-implementation checked against TAG, and 165 runs.

### 4) Experimentation (30 %): area mark 83

**Adequacy and rigour: A, 87.**
- Framework agents: ISMCTS, TAG's DUCT, BasicMCTS, RHEA and OSLA, plus a field of two ISMCTS.
- Base versions: v2 and v3.
- Leave-one-out ablation, a 2^4 factorial, five later candidates, held-out deals, three budgets and two player counts.
- Deductions:
  - several key runs have 250 games (justified in Table II, but the rubric's C column calls < 500 per pair insufficient);
  - the four-player settings were kept on non-significant 250-game estimates.

**Documentation: A, 82.**
- Table II gives opponents and their settings, budgets, seating, deal generation, seed blocks and hardware; Table I gives every constant (I checked them against the JSON files).
- Missing: the TAG revision; how the X0 tuner works (objective and search); where code and per-game data can be found.
- The "Hardware, records" row says nothing about records.

**Critical analysis of the results: A, 80.**
- Strong points:
  - the error model is explained;
  - the confound in the H ablation is admitted ("mixes the value of H with mis-scaled exploration") and partly resolved with the lead-heuristic run;
  - the selection bias of v3's best-of-16 factorial cell is noted;
  - points per card type and the four-player tree statistics are analysed.
- Deductions:
  - the RQ2 overgeneralisation;
  - the existing seed-7 900 ms data are omitted;
  - E2's held-out test is promised but pending;
  - the density hides the argument.

### 5) Conclusions (10 %): area mark 77.5

**Summary and conclusions: A, 75.**
- It answers RQ1–RQ3 in order and states limitations.
- It is a recap of numbers rather than an interpretation, and the RQ2 answer is incomplete while results are pending.

**Further work: A, 80.**
- Three items, each with a reason: learning the evaluation because hand-tuning has saturated; regret matching or EXP3 because of [9] and [10]; opponent models.

### 6) General (10 %): area mark 75

**Clarity: B, 64.**
- Telegraphic, heavily parenthesised, 3–5 statistics per sentence (example in Section 5).
- Codes: H, B, R, P, M, S, I, X0, X3, W2, F, L, A, D, v2/v3/v4, seed block, E1–E3.
- n means players, visits and games (Table III column).
- Placeholders in the author block, "[pending]", and "held−out".

**Figures and tables: A, 82.**
- Fig. 1 explains the core idea well. Fig. 2's paired-effects plot with Holm-filled markers is good. Fig. 3 is readable.
- Captions are informative. Fig. 2 has sparse ticks.

**Referencing: A, 80.**
- IEEE style is consistent; 15 references, 14 peer-reviewed. Details checked in Section 3.2.
- The Claude reference gives no model version. Strategy fusion has no source.

**Weighted total:** 0.05·72 + 0.15·78.2 + 0.30·79.7 + 0.30·83.0 + 0.10·77.5 + 0.10·75.3 = **79.4 %**.

---

## 2. Recomputation of every number

My script reads only the CSV files, summary logs and diagnostic text files. It computes:
- Wilson 95 % intervals;
- mean ± 1.96 SE (sample SD, n − 1);
- paired per-game differences matched by game number;
- two-sample SE for independent runs (concatenating runs where the paper pools runs);
- inverse-variance pooling;
- Holm at α = 0.05 with two-sided normal p-values.

All files are in `groupA_code/experiments_v{3,4,5}/results`; the 900 ms chunk is in `TAG/arena/v5/results`. I also checked that each joined run equals its chunks with the stated offsets: `r2_val_3p500`, `r2_val_4p500` and `v5_field_3p100_all` (500 games each) and `v5_comp_3p1000_all` (135 games). All OK.

**Result: every number in the text, the tables and the figures matches.** The script flagged five lines, all of which are formatting artefacts or method differences:
- "about 8" vs 8.1;
- 17.6 vs 17.62;
- "16 × 500";
- "each ±5.8" vs 5.8–5.8;
- M × K z = 2.8 (their per-game contrast SE) vs 3.0 (my independent-cells SE). Both are valid; this is not an error.

| Report item | Reported | Recomputed | Source |
|---|---|---|---|
| Table III 3p/100 | 52.6 [48.2, 56.9], +0.73 ± 0.46 | same | r2_val_3p100.csv |
| Table III 3p/500 | 47.2 [42.9, 51.6], −0.10 ± 0.40 | same | r2_val_3p500.csv (= chunks c0–c3) |
| Table III 4p/100 | 44.4 [40.1, 48.8], −0.61 ± 0.43 | same | r2_val_4p100.csv |
| Table III 4p/500 | 36.5 [32.4, 40.8], −1.83 ± 0.45 | same | r2_val_4p500.csv (= chunks) |
| Table III DUCT, ISMCTS | 55.0 [50.6, 59.3], +0.72 ± 0.40 | same | r2_duct_3p100.csv |
| Table III ISMCTS × 2 | 51.3 [46.9, 55.7], +1.41 ± 0.51 | same | v5_field_3p100_all.csv |
| Table III BasicMCTS, RHEA | 84.2 [80.7, 87.1], +6.68 ± 0.60 | same | B_mcts_rhea.csv |
| Table III OSLA, BasicMCTS | 87.2 [84.0, 89.8], +9.74 ± 0.75 | same | B_osla_mcts.csv |
| All n | 500 | 500 | (3p/900 shows "500" while pending) |
| ±4.4 pp at 50 %, n = 500 | 4.4 | 4.38 | formula |
| Paired CI of a difference | "about ±5.7" | 5.7–5.9 (mean 5.82) over 18 paired pairs | V_/A_/v5_/r2_ pairs |
| Correlation of paired outcomes | 0.0–0.2 | 0.01–0.14 | same 18 pairs |
| 80 % power | about 8 pp | 8.1 | (1.96 + 0.84) · 5.7 / 1.96 |
| Runs / four-player runs | 165 / 60 | 165 / 60 | ≥ 200-game CSVs, md5-deduplicated, chunks replaced by joined runs |
| Held-out ranges | 47.2–52.6 %; 36.5–44.4 % | same | r2_val_* |
| Seed 7 at 100 ms; re-run | 55.0 %; 58.1 % | same | V_3p100, v5_base3p100 |
| Seed 7 − held-out | 2.4 ± 6.1; 5.5 ± 6.1 | 2.4 ± 6.1 (z 0.76); 5.5 ± 6.1 (z 1.77) | two-sample |
| Nigiri points (ours, v2, ISMCTS) | 17.6, 15.2, 12.9 | 17.62, 15.25, 12.94 (CSV: 17.62) | r2_val_3p100.log / .csv |
| Pudding: ours higher | yes | 2.88 vs 0.49 / 2.50 | log |
| Sashimi v2 vs ours | 9.1 vs 3.0 | 9.08 vs 2.98 | log |
| DUCT seat share | 21.0 % [17.7, 24.8] | same; v2 share also 21.0 % | r2_duct / r2_val logs |
| Framework agents' best seat share | ≤ 12.8 %, OSLA 0 | 12.8 % (BasicMCTS), OSLA 0.0 | v4 logs |
| −H | 28.3 ± 5.8 | 28.3 ± 5.8 (z 9.6); margin −5.45 ± 0.71 | A_noH vs V_3p100 |
| −S−I | 10.2 ± 5.7 (z 3.5) | same; margin −1.75 ± 0.58 (z 5.9) | A_noSI |
| −R / −B / −I win z | 2.0 / 1.4 / 1.3 | same | A_noR / A_noB / A_noI |
| Holm, win | only H, S + I | H, S + I | 5 tests |
| Holm, margin | all five | all five (I: p = 0.025 vs threshold 0.05) | 5 tests |
| Lead heuristic vs re-run | 13.6 ± 5.9 (z 4.5) | same | r2_lead vs v5_base |
| Lead heuristic vs own score / 50 | 17.8 ± 5.6 | same | r2_lead vs A_noH (different machines; the paper says so) |
| S + I held-out 100 ms | +6.1 ± 5.9 (z 2.0) | same | r2_v3_3p100 vs r2_val_3p100 |
| S + I pooled | +8.2 ± 4.1 (z 3.9) | same | inverse-variance |
| S alone; I adds | +6.4 ± 5.7 (z 2.2); 3.8 ± 5.7 | same | A_noSI → A_noI → V_3p100 |
| v3 held-out | 46.5 % | 46.5 % | r2_v3_3p100 |
| Factorial | 16 × 500; R 8.7, M 4.1 (each ±2.1); R × T z 4.0; M × K z 2.8 | 16 × 500; 8.7 ± 2.1, 4.1 ± 2.1 (paired-contrast method: ±2.0); z 4.0; z 3.0 (2.83 their method) | v3 CSVs (cells as in factorial.py) |
| v3 best cell; new seeds | 50.1 %; 45.1–49.4 % | same | abl_T4; rep_T4, tb1_T4, tb2_T4 |
| Four later changes | −1.8 to +0.7 (±5.8) | same | v5_* vs v5_base |
| Game-end rollouts | 23.7 ± 5.8 (z 8.0) | same | v5_R3V2 |
| Fig. 2 bottom Holm | lead and R3V2 filled | lead, R3V2 significant (6-test family); with 5 tests only R3V2 | (family differs from text) |
| X3 greedy | 45.0 % vs two X0 | 45.00 % ± 1.06 | greedy_duels.txt (default model: 7.25 % vs D/X0 pool) |
| Development S + I gain, 100 → 900 ms | +5.6 ± 6.1 → +14.8 ± 8.4; rise +9.2 ± 10.4 (z 1.7) | same | abl_T4/d1_I; f9_finalT4/f9_I (two-sample) |
| v3 / final drop, 100 → 900 | 17.3 / 8.1 pp | same | development CSVs |
| Fig. 3 development points | — | v2 24.8 / 18.3 / 18.8; v3 50.1 / 40.7 / 32.8; v4 55.7 / 49.7 / 47.6 (CIs match) | s1/f5/f9 |
| Iterations at 500 ms | 11 000–12 000 | 11 170 / 11 865 | probe_iterations.txt |
| Held-out 500 vs 100 | −5.4 ± 5.9 (z 1.8) | same | paired |
| Pooled 500 vs 100 | −5.5 ± 4.2 (z 2.6) | same (seed 7: −5.6 ± 5.9) | inverse-variance |
| ISMCTS seats 100 / 500; change | 26.4 / 35.9 %; +9.5 ± 5.7 | same (z 3.3; two-sample on paired data, i.e. conservative) | logs, chunk-weighted |
| v2 seats | 21.0 / 16.9 % | same | logs |
| 4p X0 at 100 ms | +4.3 ± 4.3 | same (z = 1.94, p = 0.053; the canonical pair alone, paired: +6.1 ± 6.0, z 2.0) | q4_I + q4_Io vs q4_X0 + q4_X0c |
| 4p W2; F + L 500; F + L 100 | +5.8 ± 7.4; +1.0 ± 7.9; −6.6 ± 6.0 | same | as in build script |
| 3p cost; interaction | 7.7 ± 5.4 (z 2.8); 13.5 ± 9.1 (z 2.9) | same | f5_FL8 vs f5_W2FL8 |
| Depth 1; 1000 ms | 17.4 ± 8.2; −4.8 ± 8.1 | same | q5_FL8d1; q5_FL8t1000K14 (K 1.37 confirmed in the JSON) |
| 4p 500 vs 100 | −5.4 ± 5.9; −7.9 ± 5.7; pooled −6.7 ± 4.1 (z 3.2) | same | V_4p*, r2_val_4p* |
| 4p ISMCTS seats | 24.1 → 32.1 % | 24.1 → 32.1 % (+8.0 ± 4.5, z 3.5: worth printing) | logs |
| 4p seed 7 | 46.8 %, 41.4 % | same | V_4p100, V_4p500 |
| Tree diagnostics | 73 positions; 704 children; 53 % visited once; 2 and 4; 4 / 7 | same (704 is a mean over states, checked in TreeStats4.java) | treestats4 / agree4 |
| "nominally 45–70 s CPU per game" | 45–70 | 45.2 (4p 500 ms) and 69.5 (3p 900 ms) | nDecisions × budget |
| Machine check (README) | +3.1 ± 5.8 | same | paired |

**Hard-coded numbers.** All match, but they are not computed by `build_report.py`, so the GenAI statement's "every number is computed from the result files" is not literally true for them:
- 28.3 ± 5.8 and 10.2 ± 5.7 (z 3.5);
- −R/−B/−I z values;
- 3.8 ± 5.7;
- the factorial numbers;
- 45.0 % and 45.1–49.4 %;
- 0.0–0.2, ±4.4, ±5.7, 8 pp, 45–70 s.

**Existing data that the paper omits.**
- Seed block 7 at 900 ms: our agent 56.0 % (250 games), ISMCTS seats 29.6 %, v2 14.4 %. Paired against 100 ms on the 250 common deals: **+1.6 ± 8.7 pp**.
- In-progress `r2_val_3p900_c0` (126 games): 51.2 % [42.6, 59.8], ISMCTS 30.6 %, v2 18.3 %; against 100 ms on the same 126 games: **+0.8 ± 11.8 pp**.
- Neither shows the 500 ms dip. The "900 ms flat" branch is the most likely outcome.

---

## 3. Technical, literature and inference checks

### 3.1 Description against the Java code (all checked in `TAG/src/main/java/groupA`)

**Basis.**
- `SushiMCTSPlayer` / `SushiTreeNode` are copies of `players.basicMCTS` (same method skeleton).
- There is no import from `players.mcts`. `MCTSParams` / `SushiGo_3+P_HiBudget` and `SushiGoDUCT` appear only as opponents in `TAG/arena`.

**Equations.**
- (1): `CardValueModel.estimatedScore`, with α = 0.5, T = 4, π = 1, ν = 2, Chopsticks = 1, and ω_t = (round+1)/3.
- (2)–(3): `SushiGoHeuristic`, σ 15, w 0.7.
- The marginal value is "the change of Ŝ_p when p plays it" (`marginalValue`), with r − 1 picks before and after.

**Components.**
- B: ε = 0.2 for every player; the second Chopsticks card is chosen ε-greedily in rollouts and greedily in the tree.
- R: `finishRollout` stops at the end of the round.
- P: `distinctActions` / `distinctCards`.
- M: per-player values and backups.
- S + I: `informationSetIteration`. It samples with `copy(me)`, has each player choose by UCB over its own statistics keyed by (type, icons, Chopsticks), uses ln(availability)/visits, tries untried cards first, keys children by the joint choice, and adds one node per iteration.
- K = 0.5 (3p); 4p ≥ 500 ms: K 1, w 1 (W2), `terminalScale` 2 with Pudding tie-break, σ·(ρ/2)^0.5, `opponentScoreValue` (rival weight 1, i.e. lead over the others' mean), F + L8.
- X0 constants match the Table I footnote exactly. The competition JSONs match Table II (900 / 925 ms, 430 / 450 ms).

**Opponents.**
- TAG ISMCTS: MultiTree, UCB-Tuned, K 1, rollout length 300 (random), ScoreHeuristic, Information_Set.
- DUCT: decoupled, default K 1.0, default Information_Set, LeaderHeuristic, 30-move rollouts.
- LeaderHeuristic = own score minus best opponent (×1.5 at game end). BasicMCTS default heuristic = own score / 50 (`SGGameState._getHeuristicScore`).
- All as stated.

**State copies.** In the TAG copy used, `SGGameState.redeterminise` re-deals unseen hands and hides the other players' card choices, so "an agent's copy of the state re-deals every card it has not seen" holds.

**Seating and deals.** `Arena.play` / `canonicalDeal` match Table II. Chunk offsets are multiples of 2n, so seats and opponent order are preserved.

**Small imprecisions.**
- Eq. (4) holds for S + I. S without I (the "−I" ablation) uses ln(node visits + 1).
- "the opponents alternate" (3p): both opponents play every game and swap seats every n games.

### 3.2 Literature claims and references

Full texts could not be fetched: WebFetch to ludii.games and ieee-cog.org failed because they are outside the session's allowed network, so those hosts would have to be allowed. I verified via search-indexed abstracts and metadata instead.

| Ref | Claim in the paper | Finding |
|---|---|---|
| [4] Klang et al. | "described it as combining simultaneous action selection with complete information"; "concluded that better heuristics should improve the agents" | Consistent with the abstract (CoG 2021, DOI 10.1109/CoG52621.2021.9618987: mixed agent results; "further developing heuristics … should prove to increase their performance"). Authors "C.-M. E. Klang" correct. pp. 1–4 not verified. |
| [9] Lanctot, Lisý, Winands | Compared decoupled UCT with EXP3, regret matching and OOS in Goofspiel; "regret matching and outcome sampling played best, and decoupled UCT's strategies were the most exploitable" | Consistent: the abstract says "regret matching and OOS perform best and … all variants produce less exploitable strategies than UCT". |
| [10] Tak et al. | Nine games; best variant depends on game and opponent; decoupled UCT best overall | Consistent (CIG 2014, pp. 232–239 verified). |
| [11] Sturtevant | UCT with per-player maximisation computes a mixed-strategy equilibrium, "as well or better" than earlier algorithms | Consistent; page range not verified. |
| [12] Robilliard et al. | Many classic results held; card values depend on game stage and opponents' choices | Consistent (CCIS 504, pp. 64–77 verified). |
| [8] Goodman et al. | MultiTree MCTS in TAG | pp. 292–299 verified. |
| [7] Long et al. | Success related to how fast hidden information is revealed | Consistent with my knowledge of the paper (disambiguation factor); full text not reachable. AAAI-10 pp. 134–140 verified. |
| [2], [3], [5], [6], [13], [14], [1] | Standard | Bibliographic details correct as far as I know. |
| [15] Claude | — | Should name the model version (e.g. "Claude Opus 5.5"). |

No misreporting of sources was found.

### 3.3 Are the conclusions supported by the statistics next to them?

**Abstract: "Longer budgets favoured ISMCTS over our agent."**
- Supported only at 500 ms (pooled −5.5 ± 4.2 pp; ISMCTS +9.5 ± 5.7 pp).
- At 900 ms the paper's own seed-7 data (+1.6 ± 8.7 pp; ISMCTS 29.6 % vs 27.7 %) and the preliminary held-out chunk point the other way.
- This is an overgeneralisation and a selective omission. **Must fix.**

**IV-C: "pooled with seed block 7 (250 deals at 900 ms), 500 ms changed it by −5.5 ± 4.2 pp".** The 500 ms pool uses 500 seed-7 deals. The parenthetical belongs to the not-yet-printed 900 ms pool. **Wrong as printed.**

**"S and I … pooled +8.2 ± 4.1".** A valid pool of two confirmatory paired estimates.

**RQ3 / E3.**
- Post hoc, and different F + L in the two arms. The paper says so: "exploratory … only the three-player arms had F + L".
- "we kept each change where its estimate was positive" is selection on noise. The held-out drop for four players (36.5 % vs seed 7 41.4 %) is consistent with that, and the paper notes the deal overlap.

**"no change gave a significant gain" (four players).** X0 has z = 1.94 (p = 0.053), so the claim is borderline. Print the z.

**"DUCT … did not beat our earlier agent."** Correct as "not detectably different": 21.0 % vs 21.0 %, each about ±3.6.

**Multiplicity.**
- Holm is applied as stated. The Fig. 2 bottom family is six tests (lead heuristic + five candidates), not "within the later candidate tests".
- Many secondary z values are unadjusted, which is declared.

**Power.** Stated (8 pp); the E2 branch adds "80 % power only for X pp". Good.

**Confounds.** All declared:
- H versus K scale;
- machine (+3.1 ± 5.8);
- budget comparisons change all agents' budgets ("or our parameters … suit longer budgets less");
- four-player budget and settings mixed.

---

## 4. Compliance

**Page limit.**
- Main text ends on page 4 at y ≈ 632 pt (text area ends ≈ 770 pt), i.e. about 12 lines spare including the heading gap.
- GenAI statement on pages 4–5, references on page 5; 5 pages total. OK.
- With both pending results (worst synthetic case) the main text ends at y ≈ 726 pt, so **about 4 lines** remain. Still 5 pages in all ten scenarios.

**Template.**
- IEEE Word template of the module, two columns, styles kept.
- The body top margin of 54 pt matches the original template's two-column section. Left/right margins are 44.65 pt vs 45.35 pt in the template's two-column section (negligible).
- The PDF embeds Liberation Serif and FreeSerif instead of Times New Roman (metric-compatible substitute from a LibreOffice build). Export from Word or with Times New Roman installed.

**Section word guidance** (build counter):

| Section | Guide | Draft |
|---|---|---|
| Abstract | 75–125 | 114 (≤ 122 in all scenarios) |
| Introduction | 350–450 | 449 |
| Background | 650–850 | 695 |
| Method | 700–800 | 885 |
| Results | 500–700 | 1093, rising to 1156 with pending results |
| Conclusions | 200–300 | 270 |

The Results are far over the guide; this is a readability cost.

**References.** 15 (within 6–15), IEEE, cited in sentences, none in headings. OK.

**At least 500 games per experiment unless justified.**
- 47 runs have fewer than 500 games, including V_3p900 (250), the f9_* runs (250), and every q5_* run and val_q5 (250).
- Justified in Table II (CPU cost) and repeated as a limitation. All headline held-out runs have 500.
- The four-player tuning still rests on 250-game runs, which a strict marker may penalise under the rubric's "< 500 per pair" note.

**Generative AI.**
- The acknowledgement is present (first-page footnote and section).
- Citation [15]; the main prompt is given verbatim with an accurate English translation (I checked the Turkish).
- Reviewer prompts and reports are referenced as files in the package; the folder `groupA/genai/` exists with `review_draft1–8.md` and `reviewer_prompt_draft1–8.txt`, and `GENAI_PROMPTS.md` exists.
- Compliant. The statement credits Claude with all code, analysis and text and says nothing of the authors' own decisions. That is the authors' call, but it should be completed truthfully (see S9).

**Code package.**
- Only `groupA/` is needed; the submission copy `groupA_code/groupA` is identical to the TAG source.
- README with constructor and JSON instructions; competition JSON marked. No framework changes needed; based on BasicMCTS.
- **Check the group letter:** the README opens with "Rename first: replace every groupA … with your real group letter", so A may be a placeholder.
- README inconsistencies with the paper:
  - "about 160 runs" vs 165;
  - lines 397–402: "74 test positions", "93–96 %" vs the paper's 73 positions and 90.4 / 94.5 / 97.3 %;
  - the stronger-field run quoted as 300 games / 54.2 % vs the paper's 500 games / 51.3 %;
  - seed block 7 called "validation deals".

**Author block.** Placeholders. **Must fix.**

---

## 5. Presentation

**Density.** An example, from IV-B:

> "TAG's lead heuristic (own score minus the best opponent's, K rescaled to 7.5) in place of H costs 13.6 ± 5.9 pp (z = 4.5) against the re-run, but in place of own score / 50 it recovers 17.8 ± 5.6 pp of the 28.3 pp (same deals, second machine); as round-end leaves hold no unfinished sets, H differs from it mainly by the Pudding standing, the mean-opponent term and its bounded scale (not separated), while the card values act through B."

This is precise but very hard to read. Most Results sentences look like this.

**Notation clash.** n is:
- the number of players ("12 − n cards", "n − 1 passes", "seat g mod n");
- child visits in UCB1 ("n its visits"); and
- games (Table III column "n").

N is parent visits in Section II-B but availability N_p(a) in (4).

**Typos.**
- "On the held−out deals ISMCTS won 26.4%" (a U+2212 minus inside the word; produced by `.replace("-", "−")` on the whole sentence in `text_budget`).
- Table III shows n = 500 for the pending 3p/900 row.

**Gapping and ellipsis.** In the abstract ("the evaluation and decoupled tree also the win rate") and IV-D ("consistent with E3, but exploratory (chosen after …; four-player gains not significant; only …)").

**Undefined terms.** UCB-Tuned (Table II). "a re-run (4 with the same seed …)": why a same-seed re-run differs (TAG's unseeded redeterminisation RNG) is never said.

**Table II.** "Hardware, records" has no records content; "validation" is ambiguous (seed-7 validation ran on the laptop, held-out runs on the 4-core machine).

**Figures.** Good. Fig. 2 could use more ticks (−30, −10). Fig. 3's held-out markers are unconnected; that is fine.

---

## 6. Pending results: does every branch of the wording work?

I built the report in copies with synthetic `r2_val_3p900.csv` plus chunk logs and/or `r2_v3_3p500.csv`. The "E2 test" column is the verdict the text gives for the change in the S + I gain from 100 to 500 ms.

| Scenario | 900 ms (held-out) | v3 at 500 ms | E2 test (z) | Abstract budget sentence | Consistent with the paper's own stats? |
|---|---|---|---|---|---|
| S1 | 52.6 % (flat: +0.0 ± 5.9; pooled +0.5 ± 4.9) | pending | — | "Longer budgets favoured ISMCTS over our agent" | **No** |
| S2 | 42.0 % (−10.6; pooled −6.8, z 2.7) | pending | — | same | Yes |
| S3 | pending | 41 % | neutral (0.0) | same + "the decoupled tree's gain did not change detectably" | 500 ms only; "gain" has no reference point |
| S4 | flat | 32 % | grew (2.7) | "…; the decoupled tree's gain grew" | **No** (budget clause) |
| S5 | flat | 51.6 % | shrank (3.3) | "…; … shrank" | **No** (budget clause) |
| S6 | flat | 41 % | neutral | "…; … did not change detectably" | **No** |
| S7 | drop | 32 % | grew | "…; … grew" | Yes |
| S8 | 58.0 % (+5.4; pooled +4.2, z 1.7) | 41 % | neutral | "Longer budgets favoured ISMCTS over our agent" | **No** (contradicted) |
| S9 | drop | 51.6 % | shrank | "…; … shrank" | Yes |

Synthetic E2 intervals (±6.3–6.5 pp) are too narrow because the synthetic v3 file was derived from the 100 ms file; real intervals will be about ±8–9 pp. This does not affect the wording logic.

**Findings across all branches:**

1. **`_abstract_end()` ignores `POOL900` and the 900 ms ISMCTS share.** It is wrong in 5 of the 9 tested outcomes, including the most likely one (S1).
2. **`text_budget`, current branch:** "pooled with seed block 7 (250 deals at 900 ms), 500 ms changed it by …" is misattributed. It becomes correct only when the 900 ms pool is appended.
3. **`text_budget`, ISMCTS sentence:**
   - "v2 21.0% and 16.9% (900 ms: 30.0% and 17.4%)": placed after v2's numbers, the parenthetical reads as v2's, although it is (ISMCTS, v2).
   - The interpretation ("at 500 ms the extra time favoured ISMCTS …") never addresses 900 ms, even in S8 where our agent improves at 900 ms.
   - "held−out" typo.
4. **`text_budget`, held branch:** "S and I changed v3's win rate by +6.1 … at 100 ms and +15.2 … at 500 ms" is semantically wrong; S and I are added to v3. "not significant either way" is colloquial. The other branches ("which supports E2"; "which contradicts E2: the held-out test does not support the sequential-tree explanation"; "(… 80% power only for 9 pp)") are grammatical and consistent.
5. **`text_conclusion_1`:** grammatical in all branches. In S1, S4, S5, S6 and S8 the RQ2 sentence ("changed by −5.5 … to 500 ms and by +0.5 … to 900 ms …, while TAG's ISMCTS won 9.5 pp more of its seats at 500 ms") gives a non-monotonic pattern with no interpretation, and it contradicts the abstract.
6. **`text_final_agent` and `text_fig_budget_caption`:** correct in all branches; the range updates, e.g. "42.0–52.6%" or "47.2–58.0%". Fig. 3 renders correctly with all four extra held-out markers (checked in S7). Table III's 3p/900 row fills in.
7. **Unreachable branches.** "Longer budgets lowered our win rate" and "… did not change our win rate significantly" can no longer occur, because the 500 ms runs are final. Their wording would be acceptable, but they share the same 900 ms blindness.

---

## 7. Prioritised changes

Length effects are given in lines of an IEEE column (about 9–10 words per line).

### MUST

**M1. Author block.** Fill in "[Group member 2]", "[Group member 3]" and the three "Student no. [to be completed]". No length effect.

**M2. Group letter.** Confirm it. If it is not A, rename:
- the package folder and the 7 `package groupA;` lines;
- the `class` / `heuristic` entries in the JSONs;
- the README examples;
- the report file name.

No length effect.

**M3. Finish the pending runs and rebuild.** Wait for `r2_val_3p900` and `r2_v3_3p500`, run `TAG/arena/v5/finalize_r2.sh` (it joins the chunks and copies the chunk logs, which `seat_wins_chunks` needs), then `make_figures.py` and `build_report.py`. Check that no "pending" remains anywhere, including Table III. Effect: **+8.5 lines** (measured), leaving about 4.

**M4. Abstract: replace `_abstract_end()` and tighten the abstract.** This fixes the budget overclaim and makes the E2 clause self-contained.

```python
def _abstract_end():
    d5 = POOL500 is not None and POOL500["zw"] <= -1.96
    if POOL900 is None:
        s = ("At 500 ms ISMCTS gained more than our agent from the extra time" if d5 and ISM500 and ISM500["sig"]
             else "Our win rate fell at 500 ms" if d5 else "Our win rate did not change detectably at 500 ms")
    else:
        d9 = POOL900["zw"] <= -1.96
        s = ("Our win rate fell at 500 and 900 ms" if d5 and d9 else
             "Our win rate fell at 500 ms but not detectably at 900 ms" if d5 else
             "Our win rate fell at 900 ms but not detectably at 500 ms" if d9 else
             "Longer budgets did not change our win rate detectably")
    if RISE is not None:
        z = RISE["zw"]
        s += "; the decoupled tree's advantage " + ("grew with time" if z >= 1.96 else
                                                    "shrank with time" if z <= -1.96 else "did not change detectably")
    return s + "; four-player settings hurt three-player play (exploratory)."
```

Use this ABSTRACT body. It is at most 125 words in every branch (built with the same counter); X stands for the 900 ms result.

> "Sushi Go! combines hidden hands, simultaneous moves and delayed scoring, which basic Monte Carlo tree search (MCTS) ignores. We extend BasicMCTS of the Tabletop Games framework (TAG) with a card-value model for rollouts and evaluation, round-end rollouts, max-n backups and a decoupled information-set tree. On held-out deals it wins 52.6%, 47.2% and X% of three-player games (100, 500, 900 ms) against TAG's information-set MCTS and our previous agent, and 44.4% and 36.5% of four-player games (100, 500 ms; chance 33.3%, 25%). The evaluation and the decoupled tree significantly raise the win rate, every ablated component the score margin. [budget clause]; [E2 clause]; four-player settings hurt three-player play (exploratory)."

Main-text length: 0.

This abstract no longer defines "ISMCTS", so the 900-ms-pending branch of the code above should say "TAG's information-set MCTS". If you keep "(ISMCTS)" in the abstract, the longest branch reaches 126 words; drop "(exploratory)" there to get back to 125.

**M5. Pooled sentence in `text_budget`.** Replace

`"; pooled with seed block 7 (250 deals at 900 ms), " + _and([f"{b} ms changed it by {fpp(p)}" …])`

with

`"; pooled with seed block 7, " + _and([f"{b} ms" + (" (250 seed-7 deals)" if b == 900 else "") + f" changed it by {fpp(p)}" …])`.

Effect: −0.3 line now, +0.3 later.

**M6. "held−out".** In `text_budget`, build only the interval with `.replace("-", "−")`. No length effect.

**M7. ISMCTS sentence: unambiguous, and say what 900 ms shows.** Suggested text, with i9/r9 the 900 ms seat shares:

> "On the held-out deals ISMCTS won 26.4%, 35.9% and {i9}% of its seats at 100, 500 and 900 ms (500 vs 100 ms: +9.5 ± 5.7 pp, two-sample), v2 21.0%, 16.9% and {r9}%."

Then a conditional interpretation:
- POOL900 z ≤ −1.96: "so the extra time favoured ISMCTS at both budgets."
- Otherwise: "The dip at 500 ms is not seen at 900 ms (pooled {POOL900}), so it may be noise or specific to 500 ms; our parameters, tuned at 100 ms, may also suit longer budgets less."

Effect: +1 to +1.5 lines.

**M8. Seed-7 900 ms data.** Until the held-out 900 ms run is in, add to IV-C:

> "On seed block 7, 900 ms changed it by +1.6 ± 8.7 pp (250 deals; ISMCTS 29.6% vs 27.7% of its seats)."

Once POOL900 exists, the pooled value already contains this result; keep the ISMCTS share. Make CONCLUSION_1 follow the same branch logic as M4. Effect: +1 line, absorbed by the cuts below.

**M9. Held branch wording.** Replace "S and I changed v3's win rate by" with "adding S and I to v3 changed its win rate by", and "not significant either way" with "inconclusive". Effect: +0.2 line.

### SHOULD

**S1. Notation.**
- Use P for the number of players: "12 − P", "P − 1 passes", "seat g mod P".
- Rename Table III's column to "Games".
- Or keep n for players and write UCB1 as "Q + K√(ln V / v), where v is the child's visits and V its parent's".

No length effect.

**S2. Eq. (4).** Add after (4): "(without I, N_p(a) is replaced by the node's visit count)". Effect: +0.3 line.

**S3. Cuts to pay for M3, M7 and M8 and to improve readability** (about −5 lines in total):
- **IV-A seed-7 comparison** (−1 line). Old: "On seed block 7 it had won 55.0% at 100 ms (58.1% when re-run on the held-out runs' machine), 2.4 ± 6.1 pp and 5.5 ± 6.1 pp more than on the held-out deals, within chance." New: "On seed block 7 it had won 55.0% at 100 ms (58.1% when re-run on the second machine), within chance of the held-out result (z ≤ 1.8)."
- **IV-A DUCT sentence** (−1.2 lines). Old: "TAG's own DUCT configuration (decoupled selection and information sets, but its lead heuristic, K = 1 and 30-move random rollouts) wins … in place of v2, the share v2 wins: decoupling and information sets alone, untuned, did not beat our earlier agent." New: "TAG's own DUCT agent (Table II) wins 21.0% [17.7, 24.8] of its seats in place of v2, exactly v2's share: decoupling and information sets without our evaluation and rollouts did not beat our earlier agent."
- **IV-B factorial** (−0.8 line). New: "in a 2^4 factorial on v3 (R, M, K, T; 16 × 500 games) R added 8.7 ± 2.1 pp and M 4.1 ± 2.0 pp, with R × T and M × K interactions (z = 4.0, 2.8)."
- **IV-D** (−0.4 line): "(4 with the same seed, 7 with another)" → "(4–7 of 73)".
- **V** (−0.8 line): delete "; TAG's lead heuristic in its place still costs 13.6 pp".
- **I** (−1 line): merge bullet 4 into bullet 2: "…compared with TAG's own decoupled information-set MCTS and tested by leave-one-out and factorial ablations and on held-out deals;" and delete bullet 4.

**S4. Replicability row.** Retitle Table II's last row "Hardware" and add a row:

> "Records | per-game CSV files and logs of all 165 runs, agent JSON files (code package); TAG revision <hash>"

Effect: +1 line.

**S5. Literature.** One sentence in II-C:

> "Cowling et al. instead treat simultaneous or hidden moves as partially observable sequential moves (ISMCTS + POM) [3]; we decouple them at a turn node, as in [9]."

Also source strategy fusion and non-locality: cite Frank and Basin 1998, or attribute to [3] / [7]. Effect: +1.5 lines (references are free).

**S6. Holm family.** In III-E: "within the ablation and within the lead-heuristic and candidate tests (six tests)". No length effect.

**S7. X3 comparison.** "although X3 is the much stronger greedy policy (45.0% … players)" → add "; the default constants win only 7.3% of greedy games against default and X0 players". Effect: +0.6 line.

**S8. X0 borderline result.** "(X0: +4.3 ± 4.3 pp at 100 ms" → "(X0: +4.3 ± 4.3 pp, z = 1.9, at 100 ms". Effect: +0.2 line.

**S9. GenAI section** (does not count toward the page limit).
- Name the model version in [15].
- If true, add one sentence on what the authors decided or checked: choice of research questions, review of code and statistics.
- Keep it accurate. Markers will weigh "understanding" against the statement that Claude did everything.

**S10. Code package.**
- Fix the README inconsistencies listed in Section 4.
- Fix the by-hand construction example (use `p.setParameterValue("K", 0.5)` etc., or recommend only the JSON route).
- Consider moving inactive switches (A, O, G, U, K, Q, E, MAST) out of `SushiTreeNode`, or marking the final code path at the top of the class.

**S11. `build_report.py`.** Compute the hard-coded statistics (Section 2) from the CSV files, so that the GenAI claim "every number is computed from the result files" is literally true and nothing goes stale.

**S12. Font.** Final PDF in Times New Roman (Word export, or with the font installed).

### NICE

- N1. "(0.0–0.2)" → "(0.01–0.14 in our paired runs)".
- N2. Four-player ISMCTS seats: "24.1% to 32.1% (+8.0 ± 4.5 pp)".
- N3. Explain same-seed re-run variation (TAG's unseeded redeterminisation RNG).
- N4. Rounding: v2's Nigiri 15.25 → "15.3".
- N5. Table II seating: "the two opponents swap seats every n games (4 players: 2 + 1 …)".
- N6. III-E: "the outcomes of two versions of our agent on the same deal correlate only weakly".
- N7. "We build on BasicMCTS, which lacks these features …" → mention that the module requires BasicMCTS as the base.

**Net length.** M3 adds 8.5 lines; M7, M8, S2, S4 and S5 add about 5; S3 cuts about 5. Starting from 12.5 spare lines, about 4 remain, so the main text stays within 4 pages. Rebuild and check page 4 after every edit; the measured worst case already leaves only about 4 lines.

---

## 8. Plain answer

**Would it get ≥ 85 % now?** No.
- As submitted today it is incomplete ("[pending]", placeholder authors), and its abstract makes a budget claim that the paper's own data do not support.
- With the must-fixes I expect about 80–81 %; with the should-fixes, 83–85 %.
- The experimental work is first-class. The mark is held down by the write-up and by unresolved RQ2/RQ3 evidence, not by the science.

**What stands between it and 90 %+:**
1. **A complete, internally consistent paper.** Both held-out runs integrated, the E2 test reported, and abstract, Results and Conclusions saying the same thing about budgets for whatever outcome arrives (M3–M9).
2. **Readability at "very good write-up" level.**
   - Cut secondary statistics from the Results, which are 1 100+ words against a 500–700 guide.
   - Give each subsection one or two plain sentences of interpretation.
   - Fix the n / N notation and lean on the tables and figures for numbers.
   - This is the largest single gap: General clarity is in band B today.
3. **Claims matched to confirmatory evidence.**
   - RQ3 rests on 250-game, non-significant tuning runs and a post hoc interaction.
   - For 90 %+ either validate the four-player settings on held-out deals (with versus without W2 + F + L at 500 ms, 500 games each) or present RQ3 as exploratory everywhere, including the contributions.
4. **Deeper critical positioning.** ISMCTS + POM and other simultaneous-move information-set approaches; a source for strategy fusion; why the decoupled information-set tree helps more than determinisation here, beyond "consistent with Long et al."
5. **A credible account of the students' own role.** The GenAI statement attributes all code, analysis and text to Claude. That is allowed, but it invites the marker to discount "understanding" and "originality", and the individual reflections will have to carry this.
