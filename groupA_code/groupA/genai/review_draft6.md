# Independent review: ECS7032P Assignment 1, Group A (draft 6)

**Report:** "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!" (5 pages, built 28 Oct 01:22)

**Scope.** I read the brief, the rubric, the assessment overview and the writing tips. I rendered every page and checked it, read `build_report.py` and `make_figures.py`, and read all of `TAG/src/main/java/groupA`. I recomputed every number in the report from the raw CSV files and logs with my own script, checked the literature claims, and built the PDF with synthetic pending results to test every conditional branch. I did not read `report/reviews/`. Nothing under `/home/user/statistics` was modified.

**Bottom line.** The report would get about **78%** (likely range 75–82). It would not get 85% as it stands. Section 7 says what stands between it and 90%.

---

## 1. Marks against the rubric

Weights come from the Assessment Overview: Abstract 5, Problem definition 15, Approach 30, Experimentation 30, Conclusions 10, General 10. Each area's mark is the mean of its rubric items.

| Area (weight) | Rubric item | Band | Mark | Reasons (quotes are from the PDF) |
|---|---|---|---|---|
| **1 Abstract (5%)** | Motivation | 70–100 | 74 | Correct, but it is a single clause: "…which basic Monte Carlo tree search (MCTS) ignores." |
| | Summary of work | 70–100 | 76 | All key components are named, and the length is right (125 words, the upper limit). |
| | Outline of findings | 70–100 | 72 | The findings are there but crammed in. "extra time from 100 to 500 ms **helps the opponents more** than our agent" is wrong for v2, whose seat share fell (21.0% → 16.9% held-out; 17.4% → 15.5% seed 7). Only TAG's ISMCTS gained (+9.5 ± 5.7 pp). The draft still shows "[pending]%" twice, and "four-player settings hurt with three" reads awkwardly. |
| | **Area** | | **74** | |
| **2 Problem definition (15%)** | Motivation | 70–100 | 72 | The four game properties motivate the work well. The wider importance (why separating the contributions matters beyond Sushi Go) is not argued, and the introduction cites only [1]–[4]. |
| | Problem clarity | 70–100 | 82 | Strong: four properties (i)–(iv) and RQ1–RQ3. |
| | Contributions | 70–100 | 70 | They are listed but not argued. The third bullet ("exploratory evidence that settings tuned for four players hurt with three…") is a weak research contribution. |
| | Critical literature analysis | 50–69 / 70 border | 68 | There is some real critique: DUCT's exploitability [10] against Tak's "best overall" [11], PIMC's flaws [8] against the conditions for its success [7], and SO- versus MO-ISMCTS. There is a gap statement. But each topic gets one or two sentences. There is no Sushi Go or drafting AI literature beyond [4] and [13], no opponent-modelling work (although the future work proposes it), no evaluation-learning work, and no RHEA citation. "Non-locality" is never defined. |
| | Game description | 70–100 | 76 | Accurate and compact: deck, scores, 12 − n cards and pass dynamics. I checked all the rules against `SGParameters`/`SGCard`. |
| | **Area** | | **74** | |
| **3 Approach (30%)** | Concepts applied (Methods + code) | 70–100 | 80 | The description matches the code exactly (§3.1): equations (1)–(4), B, R, P, M, S, I with availability-count UCB, W2, F and L. The concepts go beyond the lectures: SO-ISMCTS combined with DUCT and max-n, a fast simulator and multiple leaf rollouts. The code has comments but is bloated (see §4). |
| | Originality | 70–100 (low) | 72 | Honest but limiting: "The search techniques come from the literature; our own element is the card-value model shared by the evaluation and every player's simulation." Each component is tied to a game property, which the brief rewards, but nothing is new beyond the card-value model and F + L. |
| | Difficulty / ambition | 70–100 | 85 | A very ambitious agent and study. |
| | **Area** | | **79** | |
| **4 Experimentation (30%)** | Adequacy and rigour | 70–100 | 84 | Excellent coverage: v2 and v3 baselines and five framework agents; a leave-one-out ablation plus a 2⁴ factorial; held-out validation; Holm correction; paired designs; a power statement. The weaknesses are listed in §3.3: the four-player configuration was chosen on 250-game non-significant runs; the four-player 100 → 500 ms comparison is confounded; the held-out H2 test has about 12% power for the effect the development runs predict; and the ablation was run on deals that overlap development. |
| | Documentation | 70–100 | 80 | Table II plus Table I is enough to replicate the study. The code is only "from the authors", and a few details are loose (e.g. "validation on a 4-core machine" means the held-out validation). |
| | Critical analysis and error analysis | 70–100 | 80 | The statistical model is stated, and the hedging and selection-bias discussion are good ("the development rise is probably overstated"). The marks go on overclaims and selective presentation: "time hardly does", "helps the opponents", "S alone … beats v3" (unadjusted, contradicted by a replicate), "56.0% against 55.0%" (unequal game sets), and pending wording that can contradict itself (§3.4). |
| | **Area** | | **81** | |
| **5 Conclusions (10%)** | Summary | 70–100 | 75 | A concise summary per RQ with statistics. It will ignore the 900 ms and four-player 500 ms held-out results once they arrive. "as the opponents gained more from the time" is inaccurate (v2 lost seats) and causal. |
| | Future work | 70–100 | 74 | Three sensible items, each with a reason ([10], [11]). They are brief, with no concrete experimental plan (e.g. a properly powered H2 test at 900 ms). |
| | **Area** | | **74.5** | |
| **6 General (10%)** | Clarity | 50–69 / 70 border | 68 | Precise but very dense. There is a soup of about 13 letter codes (H B R P M S I X0 W2 F L A D) plus v2–v4 and "seed blocks". Sentences are long and nested: "it had won 55.0% at 100 ms (58.1% re-run on the held-out runs' machine): 2.4 ± 6.1 pp and 5.5 ± 6.1 pp more, neither significant". Notation clashes (§5). Colloquialisms: "Depth matters – … – but time hardly does". |
| | Figures and tables | 70–100 | 80 | Fig. 1 shows conceptual thinking. The Fig. 2 forest plot with Holm-filled markers is informative. Fig. 3 has minor problems (§5). The tables are clean. |
| | Referencing | 70–100 | 74 | IEEE style is consistent and the sources are relevant and peer-reviewed. [13] has the wrong page range (§3.2). RHEA is not cited. |
| | **Area** | | **74** | |

**Weighted total:** 0.05·74 + 0.15·74 + 0.30·79 + 0.30·81 + 0.10·74.5 + 0.10·74 = **77.6 ≈ 78%**.

The competition bonus (+15% or +5%) is separate.

**Risk not scored above.** A marker who reads "All sections, tables and figures of this paper were drafted with Claude" next to the quoted prompt ("It should be a report that gets 100 … do not leave the loop until you are sure it will get 100%") may examine originality and authorship harder. The disclosure itself is mostly compliant (§4).

---

## 2. Recomputation of every number

My script, written from scratch, is `/tmp/claude-0/review6/recompute.py`, and its output is `/tmp/claude-0/review6/recompute_out.txt`. Methods:

- Win rates use Wilson 95% intervals. Shared first places score 1/k; there are 8–10 per run, so the Wilson approximation is fine.
- Margins are mean ± 1.96 SE.
- Paired differences are per-game differences matched by game number.
- Independent runs use the two-sample SE; "pooled" runs are concatenated.
- Pooling across studies is inverse-variance; Holm is applied as the paper says.

I also verified the merges: `r2_val_3p500.csv` equals chunks c0–c3 shifted by 0/126/252/378 (seats g mod 3 hold), and `v5_field_3p100_all.csv` equals the 300-game part plus the 200-game part shifted by 300. ✓ means it matches to the printed precision.

**Abstract and Table III**

| Report | Recomputed | Source |
|---|---|---|
| 3p/100 held-out 52.6 [48.2, 56.9], +0.73 ± 0.46 | 52.60 [48.22, 56.94], +0.730 ± 0.462 ✓ | v5 `r2_val_3p100.csv` |
| 3p/500 47.2 [42.9, 51.6], −0.10 ± 0.40 | 47.20 [42.86, 51.58], −0.104 ± 0.400 ✓ | `r2_val_3p500.csv` (merged) |
| 4p/100 44.4 [40.1, 48.8], −0.61 ± 0.43 | 44.40 [40.10, 48.78], −0.614 ± 0.427 ✓ | `r2_val_4p100.csv` |
| DUCT row 55.0 [50.6, 59.3], +0.72 ± 0.40 | 55.00 [50.62, 59.31], +0.722 ± 0.397 ✓ | `r2_duct_3p100.csv` |
| ISMCTS × 2 51.3 [46.9, 55.7], +1.41 ± 0.51 | 51.30 [46.93, 55.65], +1.406 ± 0.508 ✓ | `v5_field_3p100_all.csv` |
| BasicMCTS + RHEA 84.2 [80.7, 87.1], +6.68 ± 0.60 | 84.20 [80.74, 87.13], +6.682 ± 0.603 ✓ | v4 `B_mcts_rhea.csv` |
| OSLA + BasicMCTS 87.2 [84.0, 89.8], +9.74 ± 0.75 | 87.20 [83.99, 89.85], +9.744 ± 0.754 ✓ | `B_osla_mcts.csv` |
| 3p/900 and 4p/500: pending | files do not exist yet ✓ | — |

**Section IV-A**

| Report | Recomputed | Source |
|---|---|---|
| Seed block 7: 55.0%; re-run 58.1% | 54.97%; 58.10% ✓ | `V_3p100.csv`, `v5_base3p100.csv` |
| "2.4 ± 6.1 pp and 5.5 ± 6.1 pp more" (two-sample) | +2.37 ± 6.13; +5.50 ± 6.09 ✓ | same |
| DUCT seats 21.0%, "the same share as v2" | 21.0% vs v2 21.0% ✓ | `r2_duct_3p100.log`, `r2_val_3p100.log` |
| BasicMCTS, RHEA, OSLA seats "at most 12.8%" | 9.0 / 6.8 and 12.8 / 0.0 ✓ | v4 `val16.log` |

**Fig. 2 and Section IV-B (paired, seed 7)**

| Report | Recomputed (win; margin) | Source |
|---|---|---|
| −H "28.3 ± 5.8" | −28.27 ± 5.79 (z −9.57); −5.45 ± 0.71 ✓ | `A_noH` vs `V_3p100` |
| −S−I "10.2 ± 5.7 (z = 3.5)" | −10.17 ± 5.75 (z −3.47); −1.75 ± 0.58 ✓ | `A_noSI` |
| −R "z = 2.0", −B "1.4", −I "1.3" | −6.17 ± 5.94 (2.04); −4.27 ± 5.83 (1.43); −3.77 ± 5.68 (1.30) ✓. Margins −0.74 ± 0.61, −0.98 ± 0.57, −0.63 ± 0.55 | `A_noR`, `A_noB`, `A_noI` |
| Holm: margin all 5; win only H and S + I | ✓ | — |
| Lead heuristic "13.6 ± 5.9 (z = 4.5)" | −13.60 ± 5.88 (4.54); −1.45 ± 0.60 ✓ | `r2_lead_3p100` vs `v5_base3p100` |
| S alone "+6.4 ± 5.7 (z = 2.2)" | +6.40 ± 5.70 (2.20) ✓ | `A_noSI` → `A_noI` |
| I "3.8 ± 5.7" | +3.77 ± 5.68 ✓ | `A_noI` → `V_3p100` |
| Candidates "between −1.8 and +0.7 (each ±5.8)" | X3 −1.30 ± 5.84; N05 −1.80 ± 5.79; V2D −0.60 ± 5.83; W1 +0.70 ± 5.78 ✓ | `v5_*_3p100` |
| R3V2 "23.7 ± 5.8 (z = 8.0)" | −23.70 ± 5.81 (8.00) ✓. Holm survivors are R3V2 (and the lead heuristic) with either family size ✓ | — |
| "45.0% of greedy games against two X0 players" | 45.00% ✓ | `greedy_duels.txt` |
| Factorial: R "8.7 ± 2.1", **M "4.1 ± 2.0"**, R×T "−4.2", M×K "+3.2", others below 2 | R +8.65 ± 2.09; **M +4.10 ± 2.09**; R×T −4.22 (z −3.96); M×K +3.17 (z +2.97); others \|z\| ≤ 1.06 | 16 cells: `s2_*`, `d_*`, `abl_*`, `s3_R_T8`, `s4_*` |

On the factorial: the "±2.0" for M comes from v3 `factorial.py`. That script uses a per-game "paired" contrast across cells that do not share deals (they were run before canonical deals existed). The correct independent-samples SE gives ±2.09 for every effect. The conclusions do not change, but the method is inconsistent with the paper's own statement that pre-19-October runs are independent samples.

**Section IV-C**

| Report | Recomputed | Source |
|---|---|---|
| v3 "lost 17.3 ± 7.2" (100 → 900 ms) | −17.30 ± 7.21 ✓ | `abl_T4` → `f9_finalT4` |
| Final agent "8.1 ± 7.5" | −8.10 ± 7.52 ✓ | `d1_I` → `f9_I` |
| S + I gain "+5.6 ± 6.1 → +14.8 ± 8.4, by +9.2 ± 10.4 (z = 1.7)" | +5.60 ± 6.13; +14.80 ± 8.42; +9.20 ± 10.42 (1.73) ✓ | — |
| "11 000–12 000 iterations" | 11 170 and 11 865 ✓ | `probe_iterations.txt` |
| "50.1% … best of the 16 cells (45.1–49.4%)" | 50.1 is the maximum; 45.10 / 47.47 / 49.40 ✓ | `tb2_T4`, `tb1_T4`, `rep_T4` |
| Seed 7, 100 → 500 ms "−5.6 ± 5.9" | −5.57 ± 5.92 ✓ | `V_3p100` → `V_3p500` |
| "56.0% against 55.0%; 250 games" | Rates ✓, but these are unequal game sets. Paired on the 250 common deals: **+1.60 ± 8.69** | `V_3p900` |
| Held-out "−5.4 ± 5.9 … (score margin −0.83 ± 0.58)" | −5.40 ± 5.93 (z −1.78); −0.83 ± 0.58 ✓ | `r2_val_3p100` → `r2_val_3p500` |
| Pooled "−5.5 ± 4.2 (z = 2.6)" | −5.48 ± 4.19 (−2.56) ✓; concatenating the 1 000 per-game differences gives the same | — |
| ISMCTS seats "26.4% and 35.9%" | 26.4%; 35.90% (chunk logs, seat-weighted) ✓. Change +9.5 ± 5.7 pp (z ≈ 3.3; not reported). v2 21.0% → 16.9% | logs |

**Section IV-D**

| Report | Recomputed | Source |
|---|---|---|
| X0 "+4.3 ± 4.3" (2 + 2 runs pooled) | +4.27 ± 4.32 ✓ | `q4_I`, `q4_Io` vs `q4_X0`, `q4_X0c` |
| X0 + W2 + K = 1 "+5.8 ± 7.4" | +5.80 ± 7.39 ✓ | `val_q5` vs `q5_W2` + `q5_W2r` |
| F + L "+1.0 ± 7.9" | +1.00 ± 7.86 ✓ | `q5_W2r` → `q5_FL8` |
| F + L at 100 ms "cost 6.6 ± 6.0" | −6.57 ± 5.96 ✓ | `q4_X0c` → `q4_X0FL8` |
| Three-player cost "7.7 ± 5.4 (z = 2.8)" | −7.70 ± 5.39 (2.80) ✓ | `f5_FL8` → `f5_W2FL8` |
| Interaction "13.5 ± 9.1 (z = 2.9)" | +13.50 ± 9.14 (2.89) ✓ | — |
| Depth 1 "17.4 ± 8.2" | −17.40 ± 8.19 ✓ | `q5_FL8d1` |
| 1 000 ms, K = 1.37, "−4.8 ± 8.1" | −4.80 ± 8.08 ✓; K = 1.37 in the JSON ✓ | `q5_FL8t1000K14` |
| Seed 7, four players, 100 → 500 ms "−5.4 ± 5.9" | −5.40 ± 5.91 ✓, but the two arms use different configurations (§3.3) | `V_4p100` → `V_4p500` |
| "73 … 704 … 53% … 1.5 … 2 and 4 … re-run (4)" | ✓ (97.3% / 94.5% / 94.5% same card). The re-run uses the **same seed**; a re-run with another seed changes **7 of 73** (90.4%) | `treestats4.txt`, `agree4.txt` |

**Section III-E and setup**

| Report | Recomputed |
|---|---|
| ±4.4 pp | 4.38 ✓ |
| Paired difference "about ±5.7 pp" | 5.39–5.96 across the 500-game paired comparisons ✓ |
| Correlation "0.0–0.2" | 0.009–0.220 ✓ (the maximum, 0.22, is `f5_FL8`/`f5_W2FL8`) |
| "8 pp with 80% power" | 8.15 ✓ |
| "59 of our 163 runs" | 163 runs, 59 four-player, 70 050 games ✓ |
| "45–70 s of CPU time per game" | Consistent with nominal decisions × budget (≈ 78 × 0.9 s; ≈ 89 × 0.5 s). It cannot be checked from the CSV files: `msPerDecision` is wall-clock time (e.g. 2.4 s per decision in early `V_3p900` games) |
| 729 / 4 096 joint moves | 9³ / 8⁴ ✓ |
| Rule numbers and Table I constants | ✓ against `SGParameters`, `CardValueModel` defaults and the final JSON files |

**Mismatches and untraceable numbers.**

- **(a)** Factorial M: ±2.0 should be ±2.1, and the factorial SEs were computed with an invalid paired method.
- **(b)** "56.0% against 55.0%" compares unequal game sets; it should be the paired +1.6 ± 8.7 pp.
- **(c)** "a re-run (4)" omits the different-seed re-run (7).
- **(d)** "45–70 s" is a nominal estimate, not measured.

Everything else matches. Several numbers are typed into `build_report.py` rather than computed: 28.3, 10.2, 3.8, 8.7, 4.1, −4.2, +3.2, 45.0, 45.1–49.4, 0.0–0.2, 16 × 500. All of them trace to the data. The GenAI statement says "every number is computed from the result files", which is true in substance.

---

## 3. Technical, literature and inference checks

### 3.1 Description against the Java code

All of the following match the code:

- **Model (1).** `CardValueModel.estimatedScore`: b = `getGameScore`; α·u with `potential()` giving Tempura, Sashimi (β), Wasabi 2ν and Chopsticks 1, each weighted by `completion()` = min(1, (r − k + 1)/T); m = Maki standing; π·w_t·d with w_t = (round + 1)/3.
- **(2)–(3).** `SushiGoHeuristic.valuesOf`.
- **mv.** Computed with r − 1 picks before and after.
- **B.** ε = 0.2 for every player; the Chopsticks second card is ε-greedy in `playOut`.
- **R.** `finishRollout`.
- **P.** Keys by (type, icons, chopsticks).
- **M.** `ucb()` uses the mover's own value; BasicMCTS is paranoid.
- **S.** Untried cards first; joint children.
- **I.** A fresh `copy(me)` every iteration, open-loop nodes, one node added per iteration, and UCB = Q + K√(ln availability / visits) with the availability count stored at `isVal[3j+2]`. The final move is the most-visited root card.
- **D.** Up to four trees, chosen adaptively from the number of unknown hands.
- **X0, W2, F, L, K.** `terminalScale` 2, `sigmaRoundDecay` 0.5 with ρ = max(0.5, rounds left), `opponentScoreValue` with rival weight 1 (lead over the others' mean), F + L8, K = 1. All as described.
- **Competition files.** 900/925 ms and 430/450 ms.

Small omission: inside the tree, the second card of a Chopsticks move is greedy (no ε, `greedyAction`). The paper describes this only for rollouts.

**Rules compliance:**

- `groupA` imports nothing from `players.mcts`.
- `SushiMCTSPlayer` and `SushiTreeNode` are copies of `BasicMCTSPlayer` and `BasicTreeNode`.
- The package is self-contained, with no dependency on `arena` or `refv2`.

Framework integrity: I cloned upstream TAG (sparse, read-only, into `/tmp/claude-0/review6/upstream`). `SGGameState`, `SGParameters`, `SGCard`, `ChooseCard`, BasicMCTS and the Sushi Go JSON files are identical to upstream. `SGForwardModel` matches upstream as it was before commit 667935d4 (29 September 2026), so it is the module's snapshot, not a group modification.

Low risk: the final agent's search loop (`informationSetIteration`, `simIteration`, `SushiSim`) is largely new code inside the BasicMCTS copy. A strict reading of "sensible modifications … from scratch is not advisable" could notice this. The paper should keep stressing what BasicMCTS code is reused.

### 3.2 Literature claims against the sources

Checked with search results and abstracts; the hosts serving the PDFs were blocked from this session, so I could not open full texts.

| Claim | Verdict |
|---|---|
| [4] Klang et al. introduced Sushi Go to TAG, studied simultaneous action and complete information, and suggest better heuristics | ✓ The abstract says further heuristic development "should prove to increase their performance". |
| [7] PIMC success linked to how fast hidden information is revealed | ✓ This is the disambiguation property. |
| [8] Strategy fusion and non-locality | ✓ (non-locality is undefined in the paper) |
| [3] ISMCTS avoids strategy fusion; SO does not model the opponents' private information; MO uses one tree per player | ✓ A reasonable paraphrase. |
| [9] MultiTree MCTS in TAG | ✓ (pp. 292–299 verified) |
| [10] Goofspiel: EXP3, RM, OOS; RM and OOS best; all less exploitable than UCT | ✓ Matches the abstract. |
| [11] Nine games; best variant depends on the game and the opponent; "Decoupled UCT performs best" | ✓ (pp. 232–239 verified) |
| [12] UCT computes a mixed-strategy equilibrium and performs as well as or better than existing algorithms | ✓ |
| [13] 7 Wonders: "many known results still hold" | ✓ in content, but **the pages are wrong**: dblp gives **pp. 45–63**, not "64–77". |
| [6] Simulation balancing | ✓ "Showed that rollouts should be balanced" is a slight overstatement; "argued, and showed in small-board Go" is safer. |

No misreporting at misconduct level. One bibliographic error ([13]).

### 3.3 Are the conclusions supported by the statistics next to them?

1. **"helps the opponents more" (abstract, IV-C, V).** Only ISMCTS gained seats (+9.5 ± 5.7 pp); v2 lost. A second explanation is not discussed: the three-player parameters (K, σ, w) were tuned at 100 ms.
2. **"Depth matters … but time hardly does".** The evidence is two non-significant estimates with ±8 pp and ±6 pp CIs. The second (four players, all agents 500 vs 100 ms) changes the agent's configuration at the same time (100 ms file: X0, K 0.5; 500 ms file: X0 + W2 + F + L8 + K = 1) and also changes the opponents' budget. Section III-D discloses the switch, but the sentence uses the comparison as if it isolated time. With a plausible pending 4p/500 result (40%), the pooled change is −4.9 ± 4.3 pp (z = 2.3) next to "time hardly does" (§3.4). The diagnostic (2 of 73 decisions change) supports only "more search changes few decisions", which the conclusion states correctly.
3. **"S alone … beats v3 by +6.4 ± 5.7 pp (z = 2.2)".** This is unadjusted and outside the Holm families. The development replicate (`d1_S` + `d1_S2` vs `abl_T4`) gives **+1.4 ± 5.3 pp**; the inverse-variance pool is +3.7 ± 3.9 pp (z ≈ 1.9).
4. **Lead heuristic, "recovers roughly half … (two estimates against different base runs)".** A direct paired estimate exists on the same deals: lead versus own score / 50 = **+17.8 ± 5.6 pp**, about 60% of H's effect.
5. **Seed 7 at 900 ms.** It is a raw-rate comparison on unequal sets (above) and has very low power.
6. **H2 held-out test.** The development rise of the S + I gain from 100 to 500 ms is only +3.4 pp (+5.6 → +9.0). The held-out test (four runs, SE ≈ 4.4 pp) has **≈ 12% power** for that effect. The text gives "80% power only for 12 pp", but never states the predicted effect, so a reader cannot see that the test cannot decide.
7. **Selection effects.** The ablation and the later candidates use seed block 7, which overlaps the development deals, so the effects may be inflated. Only the final agent's 100 ms rate (and, once pending, S + I) is re-estimated on held-out deals. The text should compare the held-out S + I gain with the 10.2 pp. The four-player configuration was chosen by positive point estimates on 250-game runs (disclosed).
8. **Multiplicity and power.** Holm is applied correctly where claimed. In Fig. 2 the bottom family has six tests (including the lead heuristic), while the text says "within the later candidate tests". Results are identical either way; "within each group of Fig. 2" is accurate.
9. **Confounds the paper handles well.** K not retuned for −H; the Pudding, mean and scale differences of the lead heuristic; F + L in both three-player arms; machine effects (+3.1 ± 5.8 pp, checked); X3 changing the Pudding weight.

### 3.4 Pending results: every branch tested

I built the PDF in `/tmp/claude-0/review6/scen/*` with synthetic runs for the following cases. All are 5 pages, and the main text always ends on page 4.

- **S1:** non-significant positive, +1.6 ± 8.5 pp.
- **S2:** significant positive, +9.6 ± 8.3 pp (z 2.3), with ISMCTS at 900 ms below its 100 ms share and four-player 500 ms at −10.4 pp.
- **S3:** non-significant negative, −2.4 pp; 900 ms at −8.6 ± 6.1 (z 2.7).
- **S4:** significant negative, −13.4 pp (z 3.0).
- **S5:** partial (only v3 at 100 ms; no 4p/500).

**Defects in the current branches:**

1. **`_abstract_end`, H2 significant (S2/S4).** It prints "…helps the opponents more than our agent but raises [and lowers] the gain from decoupling." This is grammatical, but it **drops the RQ3 finding** from the abstract, repeats the "opponents" error and calls S + I "decoupling". The `POOL500`-non-significant branch is dead code with the current data.
2. **`text_four`, when 4p/500 is present.** "time hardly does" is printed whatever the result. In S2 it reads "…−10.4 ± 6.1 pp on the held-out deals." and contradicts itself. With a realistic 40% (S1) the pooled estimate is significant. The confound is not stated. A zero difference prints as "+0.0 ± 6.1 pp".
3. **`text_conclusion_1`, non-significant positive.** "the gain from S and I **grew** … as H2 predicted but not significantly (z = 0.4…)" asserts growth for z = 0.4. This is an overclaim.
4. **`text_conclusion_1`, RQ2 sentence.** It never mentions the 900 ms held-out result. If 900 ms rises (S2, +2.4) or falls significantly (S3, z 2.7), the conclusion is silent. In IV-C the 900 ms change is printed without z.
5. **`text_budget`, ISMCTS sentence.** "The opponent gains more" is printed whenever ISMCTS's share rises; there is no check against our own change. When it is dropped (S2), the capitalisation fix works ✓.
6. **`text_budget`, partial state (S5).** Two consecutive sentences begin "On the held-out deals"; tense shifts ("gain" against "changed"); the result is not compared with the seed-7 10.2 pp.
7. **`text_budget`, H2 significantly negative (S4).** The unconditional hypothesis sentence ("a wrong model that more iterations exploit more") then stands refuted by the paper's own test without comment.
8. **Not defects.** `_rng`, `text_final_agent` (ranges update correctly) and `text_fig_budget_caption` ("the final agent and v3") are correct in every branch. Grammar is otherwise fine.
9. **Layout.** With the current wording the worst case leaves about 6 lines spare on page 4. Fig. 3's "chance" label can collide with a 900 ms held-out marker at about 35–40%.

**Proposed fix, already implemented and verified.** I patched a scratch copy of `build_report.py` and rebuilt all scenarios. The diff is `/tmp/claude-0/review6/build_report_proposed.diff`, and the patched file is `/tmp/claude-0/review6/work_patched/report/build_report.py`. With three small cuts it fits in every branch; the worst case leaves about 2–3 lines spare on page 4. The resulting texts are quoted in §6.

---

## 4. Compliance

- **Page limit ✓.** The main text ends on page 4, about 9 lines from the bottom (about 6 once the pending results are in). The GenAI statement and references run onto page 5. Total 5 pages.
- **Template ✓.** It uses the module's IEEE Word template; margins, columns and styles match the template's two-column sections (top margin 54 pt, as in the original file).
- **Word guidance (approximate).** Abstract 125 words (at the limit). Introduction 447 ✓. Background 674 (low end of 650–850; this is the section that earns literature marks). Method 882 plus two tables (above 700–800). **Results 977, rising to 1 030–1 090 with the pending results** (guide 500–700). Conclusions 263 ✓.
- **References.** 16, of which 15 are academic plus Claude ✓. IEEE style. One wrong page range. RHEA is uncited.
- **Games ✓.** Every main comparison has 500 games. The 250-game runs are justified by CPU cost. The competition-file runs (135 and 40 games) are correctly left out. The 500 ms requirement is met.
- **Generative AI, partly compliant.** Present: citation [16], the page-1 footnote, the statement, the main prompt (original and translation) and the candidate-test prompt. Missing: the brief asks that sections using AI text "include the prompt(s) used to generate that text". The revision prompts (the reviewer reports) are only summarised ("available on request"), and the development prompts are "not reproduced here".
- **Code package.** It is the `groupA` package, with a README giving the constructor, JSON use and parameters ✓; competition JSON ✓; based on BasicMCTS ✓; no `players.mcts` ✓. **Check the group letter:** the README opens with "Rename first: replace every groupA … with your real group letter, e.g. groupK". The report and file name say Group A.
- **Must fix before submission.** The author placeholders "[Group member 2]", "[Group member 3]" and "Student no. [to be completed]", and every "[pending]" or "pending".

---

## 5. Presentation

- **Density and codes.** The main text uses about 13 component letters, v2/v3/v4 and "seed block 7 / 100 000". Fig. 2 shows how much descriptive labels help; the running text should use words first ("the evaluation (H)").
- **Notation clashes:**
  - The hypotheses **H1–H3** clash with the component **H** ("so for these five components H1 holds…").
  - **n** means players, visit counts and the games column of Table III.
  - **k** means k-way ties and k cards needed.
  - **w** (leader weight) sits next to **w_t** (Pudding weight).
  - Switch **S** sits next to **Ŝ**.
- **Undefined terms.** "non-locality"; "information set" (one clause would help outsiders); "UCB-Tuned" (cited only).
- **Hard-to-parse sentences:**
  - III-D: "From 500 ms it also uses W2 – w = 1; finished games valued by … ; σ multiplied by … ; opponents in the backup valuing … – and F, …"
  - IV-A: "it had won 55.0% … (58.1% re-run on the held-out runs' machine): 2.4 ± 6.1 pp and 5.5 ± 6.1 pp more, neither significant."
  - IV-B: "P and M stay (S and I build on them)."
- **Fig. 3.** The caption should say each line is that agent's win rate as the focus player against v2 and ISMCTS. The v2 line otherwise looks like "v2 against v2". The open held-out markers sit on the development lines. Move the "chance" label to the left.
- **Table I.** "W2 — win-gap value" describes only one of W2's four changes. Footnote a's "γ: cards needed per missing card" is cryptic.
- **Experiment plan.** There is no explicit map from experiments to RQs. The brief asks "What experiments do you run, and why?"
- **Code clarity:**
  - `SushiTreeNode.java` is 1 753 lines with about 15 switches that are off in the final agent (A, O, G, U, K, Q, E, N, V, `treeThreshold`, `ownCardTree`, `maxChildShare`…).
  - Stale comments: `SushiMCTSPlayer` says "Only _getAction() is changed, to add Modification D", and the `SushiTreeNode` header describes the closed-loop BasicTreeNode first.
  - About 70 legacy JSON files in `groupA/json`.
  - A 431-line README with long result tables.

---

## 6. Prioritised changes

Line counts are for one column of the PDF. The verified patch is in the files named in §3.4.

### MUST

1. **Fill the authors and student numbers** (`AUTHORS`). 0 lines.

2. **No "[pending]" in the submitted PDF.** Run `finalize_r2.sh` once every chunk exists. If a run cannot finish, merge the chunks that did finish (Table III prints n), or remove that cell and its abstract mention. 0 lines.

3. **Abstract** (≤ 125 words; keeps RQ3 in every branch; names ISMCTS). Replace from "We extended…" to the end with:
   > We extended BasicMCTS of the Tabletop Games framework (TAG) with card-value rollouts, a lead-based evaluation, round-end rollouts, max-n backups and a decoupled information-set tree. On held-out deals against TAG's information-set MCTS (ISMCTS) and our previous agent it wins 52.6%, 47.2% and X% of three-player games at 100, 500 and 900 ms (chance 33.3%) and 44.4% and Y% of four-player games at 100 and 500 ms (chance 25%). All five ablated components raise the score margin significantly, the evaluation and decoupled tree also the win rate; 500 instead of 100 ms helped ISMCTS more than our agent, and four-player settings hurt with three (exploratory).

   This is 125 words. If H2 is significant, replace "500 instead of 100 ms helped ISMCTS more than our agent" with "the decoupled tree's gain grew [shrank] from 100 to 500 ms" (124 words). 0 lines.

4. **IV-C, opponents.** Replace "The opponent gains more from the extra time: on the held-out deals TAG's ISMCTS won 26.4% and 35.9% of its seats at 100 and 500 ms." with:
   > The extra time helped TAG's ISMCTS more than our agent: on the held-out deals it won 26.4% and 35.9% of its seats at 100 and 500 ms, v2 21.0% and 16.9%.

   Optionally add "(+9.5 ± 5.7 pp)". Print this only if ISMCTS's change exceeds ours (done in the patch). About +0.3 lines.

5. **IV-D, time.** Replace "but time hardly does: our agent alone with 1 000 ms (K = 1.37) changed its win rate by −4.8 ± 8.1 pp, and with every agent at 500 instead of 100 ms our win rate changed by −5.4 ± 5.9 pp on seed block 7." with:
   > but more time for our agent alone did not help (1 000 ms, K = 1.37: −4.8 ± 8.1 pp), and the 500 ms agent (W2, F + L, K = 1; all agents at 500 ms) changed our win rate against the 100 ms agent by −5.4 ± 5.9 pp on seed block 7 [and X on the held-out deals (pooled Y, z = Z)].

   About +1 line.

6. **Conclusion, RQ2.** Use:
   > For RQ2, the final agent itself won 5.5 ± 4.2 pp less at 500 than at 100 ms (z = 2.6; two paired deal sets pooled) [and A less/more at 900 ms (z = B)], while TAG's ISMCTS gained seats at 500 ms;

   Then, when H2 is not significant:
   > on the held-out deals the gain from S and I went from a at 100 ms to b at 500 ms (z = f), so this test, with 80% power only for N pp, neither confirms nor refutes H2.

   Keep the significant branches as they are. About +1 line.

7. **Conclusion, RQ3, when the pooled four-player change is significant.** Add:
   > ; the four-player 500 ms agent won X less than the 100 ms one (pooled), which confounds the budget with the switch to W2, F + L and K = 1

   About +1 line, only in that branch.

8. **IV-C, H2 paragraph.**
   - Give the 900 ms change with z ("−5.4 ± 5.9 pp (z = 1.8) at 500 ms and … (z = …) at 900 ms").
   - Replace "but not at 900 ms (56.0% against 55.0%; 250 games)" with "but by +1.6 ± 8.7 pp at 900 ms (its 250 deals)".
   - In the non-significant H2 branch write "…, not significant either way (development predicted +3.4 pp; 80% power only for 12 pp)".
   - Add "(seed block 7: 10.2 ± 5.7 pp)" after the held-out S + I gain.

   About +1 line.

9. **Reference [13].** Change "pp. 64–77" to "pp. 45–63". 0 lines.

10. **GenAI prompts.** Put every revision prompt (the reviewer reports) and the development-session prompts verbatim in, for example, `groupA/PROMPTS.md` inside the submitted package. In the statement, change "(available on request)" to "(reproduced in full in PROMPTS.md of our code package)", and change "are not reproduced here" to "are in PROMPTS.md". Page 5 only.

### SHOULD

11. **IV-B, S alone.** Use:
    > S alone … gains +6.4 ± 5.7 pp over v3 (z = 2.2, unadjusted; +1.4 ± 5.3 pp in development)

    About +0.5 lines.

12. **IV-B, lead heuristic.** Replace "valuing the lead recovers roughly half of H's effect (two estimates against different base runs)" with:
    > valuing the lead recovers about 60% of H's effect (+17.8 ± 5.6 pp over own score / 50 on the same deals)

    0 lines.

13. **IV-B, factorial.** Replace "in a 2⁴ factorial on v3 (R, M, K = 0.5 vs 1.41, T = 8 vs 4; 16 × 500 games) R added 8.7 ± 2.1 pp and M 4.1 ± 2.0 pp" with:
    > in a 2⁴ factorial of R, M, K = 0.5 vs 1.41 and T = 8 vs 4 on v2 + P − A (v3 is one cell; 16 × 500 independent games) R added 8.7 ± 2.1 pp and M 4.1 ± 2.1 pp

    Also fix `factorial.py` to use the two-sample variance. About +0.5 lines.

14. **Rename the hypotheses** to E1–E3 (in III-E, IV-B, IV-D and in the strings of `text_budget` and `text_conclusion_1`). 0 lines.

15. **Alternative explanation for RQ2.** Add to IV-C: "(the three-player parameters were tuned at 100 ms)". About +0.3 lines.

16. **III-D, W2 sentence.** Replace with:
    > From 500 ms it also uses W2 (w = 1; a finished game is valued tanh((b_i − max_{j≠i} b_j)/2), Pudding breaking ties; σ is multiplied by (ρ/2)^0.5 with ρ rounds left; in the backup the opponents value their lead over the others' mean) and F: TAG's rules on integer arrays, checked move by move against the forward model, whose speed buys L = 8 rollouts per leaf; K = 1.

    0 lines.

17. **Fig. 3.**
    - Caption: replace "Win rate with three players against v2 and TAG's ISMCTS" with "Win rate of each agent as the focus player against v2 and TAG's ISMCTS (three players)".
    - In `make_figures.py`, set `ax.text(-0.27, 100/3 + 0.8, "chance", ha="left")`.

    About +0.3 lines.

18. **Background.** Add two sentences of critique, for example:
    > Single-observer ISMCTS suits Sushi Go!: hands become known within n − 1 passes, so the opponents' private information that multiple-observer ISMCTS or MultiTree MCTS model is short-lived, while their per-player trees cost iterations. Neither decoupled UCT nor ISMCTS has been evaluated in drafting games, where the hidden information shrinks every turn.

    Also define non-locality in one clause, and cite RHEA. About +2.5 lines; pay for it with cuts.

19. **Experiment plan.** Add one sentence before Table II: "Ablations answer RQ1, the budget series (100/500/900 ms, development and held-out) RQ2, and the four-player runs and diagnostics RQ3." About +1 line.

20. **Notation.** Rename Table III's "n" to "Games". Use 1/j for ties. Use λ_t for the Pudding weight. 0 lines.

### NICE

21. **Explanatory analysis of where the points come from.** Use the per-card-type columns that are already in every CSV (`pts_*`). For example: points by card type for the final agent against v3 on the held-out deals, or for the −H and −B ablations. This is the most valuable addition for 90%+ because it explains how the agent wins. About +3–4 lines with matching cuts.
22. **Re-run baseline.** Add "(7 with another seed)" after "a re-run (4)".
23. **Seat-share uncertainty.** "21.0 ± 3.6% of its seats".
24. **Code.** Remove the stale comments, add a "final-agent code path" section to the README, and prune or hide the dead switches.
25. **Wording.** "Depth matters – … – but…" → "A depth-1 tree lost …, whereas…". "four-player settings hurt with three" → "settings tuned for four players lose with three players".

### Cuts to pay for the additions

- **C1.** IV-A: delete "(OSLA none: its one-step look-ahead never reaches the reveal)". −1 line (in the patch).
- **C2.** Fig. 2 caption: "against a re-run of the final agent on the machine of the held-out runs (58.1%)" → "against a re-run of the final agent (58.1%)". −0.5 lines (in the patch).
- **C3.** Introduction: "(250 in some runs at 900 ms and in four-player development at 500 ms)" → "(250 in some long runs, Table II)". −0.5 lines (in the patch).
- **C4.** IV-D: compress the four-player development sentence to:
  > In four-player development no change was significant at its own budget (X0 +4.3 ± 4.3 pp at 100 ms; X0 + W2 + K = 1 +5.8 ± 7.4 pp and F + L +1.0 ± 7.9 pp at 500 ms; F + L −6.6 ± 6.0 pp at 100 ms); we kept changes with positive estimates.

  −1.5 lines.
- **C5.** IV-B: "As X3 also changes the Pudding weight of the leaf evaluation, its null result only weakly echoes simulation balancing [6]." → "(X3 also changes the leaf evaluation, so this only weakly echoes simulation balancing [6])". −0.5 lines.
- **C6.** III-E: delete "(59 of our 163 runs)". −0.3 lines.

**Line budget.**

- **Musts 1–10.** With C1–C3, items 1–10 fit in every branch; the worst case leaves about 2–3 lines spare on page 4 (verified).
- **Shoulds 11–19.** They add about 6 lines and need C4–C6 (about 2.3 lines). Trim a further 3–4 lines from IV-B, or skip item 18 or 19.
- **Nice item 21.** It needs another 3–4 lines, which can only come from compressing the numeric detail in IV-B and IV-D.

---

## 7. Plain answer

**Would it get ≥ 85%?** No. A strict lecturer would give about **78%** (range 75–82). With the must-fixes and most of the should-fixes it could reach about 82–84.

**What stands between it and 90%+:**

1. **Readability.** It needs a clarity pass that replaces code letters with words in the running text, removes the notation clashes (H vs H1–H3, n, k, w vs w_t, S vs Ŝ) and breaks up the multi-clause numeric sentences. The General area and the clarity of the Problem definition and Approach are held below 75 by this alone.
2. **No statement stronger than its statistics, in every pending branch.** That means fixing "helps the opponents", "time hardly does" and the confounded four-player time comparison, "S alone beats v3", "grew … as H2 predicted but not significantly", and conclusions that ignore the 900 ms result.
3. **A decisive held-out design for the claims the paper cares about.** At about 12% power, the H2 test cannot decide. A top report would either power it (for example v3 against the final agent at 900 ms with enough games, or several deal sets pooled) or present it explicitly as inconclusive. The four-player claims rest on 250-game, post-hoc, non-significant comparisons.
4. **Insight, not only measurement.** A short explanatory analysis of how the agent wins would lift both Approach and Critical analysis. For example: card-type points from the `pts_*` columns, or which decisions decoupling changes.
5. **Deeper, critical literature positioning.** Two to four sentences that justify SO-ISMCTS plus DUCT for drafting games specifically, and that connect to opponent-modelling and evaluation-learning work.
6. **Housekeeping a marker will notice.** The author placeholders, "[pending]", reference [13]'s pages, the full prompts for the AI-generated text, and the group letter.

Even with all of these, 90%+ is rare in this module: the work is mainly a careful combination of known methods. The realistic ceiling after a strong revision is the mid-to-high 80s.

---

### Files produced (all under `/tmp/claude-0/review6`)

- `/tmp/claude-0/review6/recompute.py`: independent recomputation script.
- `/tmp/claude-0/review6/recompute_out.txt`: its output.
- `/tmp/claude-0/review6/tools/scenario.py`: builds the report with synthetic pending runs.
- `/tmp/claude-0/review6/tools/dump_text.py`: dumps the generated text.
- `/tmp/claude-0/review6/scen/`: built scenario PDFs and texts. S1–S5 use the original wording; P0–P5 and Q1–Q3 use the proposed wording.
- `/tmp/claude-0/review6/build_report_proposed.diff`: the proposed wording fixes, verified to build and fit.
- `/tmp/claude-0/review6/work_patched/report/build_report.py`: the patched builder.
- `/tmp/claude-0/review6/pages/`, `/tmp/claude-0/review6/hires/`: page renders.
