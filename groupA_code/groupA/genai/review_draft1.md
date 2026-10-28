<!-- Independent review of report draft 1 (27 October 2026), written by a separate Claude instance against the
     module rubric. Kept as the record of the revision prompts mentioned in the paper's generative-AI statement. -->

# Independent marking: ECS7032P Assignment 1, Group A
**Report:** "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!"

**What I read and checked.** I read the brief, the rubric, the assessment overview, the writing tips and the template. I went through the report as PDF, DOCX and page images. On the evidence side I read every RESULTS, README and runs file in experiments_v3 to v5, the agent code, TAG's Sushi Go rules, the opponent JSON files, and Labs 1 and 4. I recomputed every number in Table II, Fig. 3 and §IV-D from the per-game CSVs, using a scratch script at /tmp/claude-0/review1/scripts/check.py. The repository was not modified.

**Verdict.** The approach is ambitious and the experiments are unusually well instrumented. But the report's central methodological claim, validation on unseen deals, is false. Several results are stated more strongly than their confidence intervals allow, a few Method and analysis statements are technically wrong, and the Gen-AI note does not meet the brief's rules on citations and prompts. As submitted I would give about **69%**. After the P1–P8 fixes below I would expect mid-to-high 70s; 80+ also needs the optional experiments in P10.

---

## 1. Marks

| Area (weight) | Mark | Justification |
|---|---|---|
| Abstract (5%) | 70–74 → **72** | Clear motivation and a headline result with a CI. However, it leaves out the answers to two of its three research questions (4 players, time budget), packs five techniques into one list sentence, and uses OSLA (which wins 0% of its seats) to support "84–87%". |
| Problem definition (15%) | 64–69 → **67** | Good list of properties (i)–(iv), explicit research questions, correct rules. Against that: the research gap is overstated (TAG's own MCTS already offers decoupled UCT on information-set trees, and ISMCTS was developed on three-player Dou Di Zhu). One "contribution" is engineering. The intro cites only [1] and [2]. There is no Sushi Go, drafting-game or TAG-MCTS literature, and the intro has two factual slips. |
| Approach (30%) | 70–74 → **72** | An appropriate, well-motivated "level 4" approach (ISMCTS + DUCT + max-n + a domain model, built on BasicMCTS) with real depth. Held back by: a wrong description of the availability-based UCB, a wrong W2 formula, parts of the 4-player agent left undocumented, overstated originality, and a Gen-AI note saying Claude wrote the code (markers will look harder for evidence of understanding). |
| Experimentation (30%) | 65–70 → **68** | Strong design: paired seeds, Wilson CIs, leave-one-out ablation, a 2⁴ factorial, 500/900 ms runs, several framework opponents. But the "unseen" validation deals overlap the development deals. The key 4-player conclusions rest on non-significant 250-game differences. H2 is never tested, and v4's own significant decline is called "kept its strength". Several comparisons are confounded, unfavourable results are left out, and there is no correction for multiple comparisons. |
| Conclusions (10%) | 62–67 → **65** | Gives numbers and has a real limitations paragraph. But it repeats the overclaims, its central inference ("the model of the game is now the bottleneck") does not follow from the evidence, future work is thinly justified, and the regret-matching rationale contradicts §II. |
| General (10%) | 64–68 → **66** | A coherent single narrative (no patchwork effect) and good figure ideas. But: very dense prose with about 20 letter codes plus v2–v5; "points" means both percentage points and game points; acronyms left undefined; Table II cells wrap; "v5" in Fig. 3 is never defined; some citations misattributed and key ones missing; top margin on pp. 2–5 is half the template's. |
| **Overall** | **≈ 69%** (range 65–74) | 3.6 + 10.1 + 21.6 + 20.4 + 6.5 + 6.6 = 68.8 |

---

## 2. Factual and numerical errors

### Major

**E1. The validation deals are not unseen.**
- The report says:
  - Table II caption: "FINAL AGENT ON UNSEEN DEALS (SEED 7)".
  - §III-E: "Development used seeds 1 and 2 …; the final agent was validated on seed 7, which no decision used."
  - §IV-B: "Table II reports the final agent on deals not used during development."
  - Contributions: "validation on unseen deals".
- What is actually true:
  - Game g (g = 0…N−1) uses game seed s + g. The deal depends only on s + g: it is shuffled with `new Random(seed+g)`.
  - So seed 7 (game seeds 7–506) shares 494 of its 500 deals with every 500-game seed-1 run (game seeds 1–500), and 495 with seed-2 runs.
  - With 3 players the focus seat (g mod 3) and the opponent order also coincide: seed-7 game g is the same set-up as seed-1 game g+6.
  - Empirical check: focus-score correlation between V_3p100 game g and d1_X0 game g+6 is 0.30 (0.35 against d1_FW2). For every other offset it is about 0.
  - Decisions taken on these overlapping deals:
    - adopting X0 for 4 players (q4_X0c, seed 1);
    - choosing W2/F/L8/K for 4p/500 ms (seeds 1–2; covers 245 of the 500 validation deals);
    - rejecting X0/W2/F for 3 players (d1_X0, d1_FW2, f5_W2FL8).
  - In fairness, the 3-player configuration itself was fixed earlier on independent TAG deals (the † runs), so the 3p headline is not selection-biased. The claim as written is still false.
- Evidence:
  - /home/user/statistics/groupA_code/experiments_v4/src/arena/Arena.java lines 53, 131, 143, 145, 185
  - /home/user/statistics/groupA_code/experiments_v4/runs.md lines 15–19, 30, 36, 48, 71, 78
  - /home/user/statistics/groupA_code/experiments_v4/RESULTS.md lines 42–44, 82, 98–105, 111–112 (non-† runs)

**E2. The UCB used with I is described wrongly (§III-C, eq. 4).**
- The report says: "np(a) counts the iterations in which p held card a."
- In the code, the availability count (iterations in which p held a) replaces N inside the logarithm, and np(a) in the denominator is the number of times p chose a: Q + K·√(ln avail_p(a) / n_p(a)).
- Evidence: /home/user/statistics/groupA_code/groupA/SushiTreeNode.java lines 788, 821, 1136, 1157, 1189.

**E3. The explanation of the −H ablation is wrong (§IV-C).**
- The report says: "TAG's score heuristic ignores unfinished sets and the Maki and Pudding majorities that (1) values."
- R is on in every ablation configuration, so each rollout stops only after the round has been scored: Maki is banked, unfinished sets are discarded, and the next round is dealt. At the evaluated leaves there are therefore no unfinished sets and no unscored Maki.
- What −H really changes is:
  - the leaf value becomes the player's own absolute score/50 instead of tanh of its lead over the leader and the mean opponent;
  - the Pudding standing is lost;
  - the value scale changes relative to K = 0.5.
- Evidence:
  - /home/user/statistics/groupA_code/groupA/SushiTreeNode.java lines 503 and 1626–1634
  - /home/user/statistics/TAG/src/main/java/games/sushigo/SGForwardModel.java lines 110–129 and 144–168
  - /home/user/statistics/TAG/src/main/java/games/sushigo/SGGameState.java lines 210–215
  - /home/user/statistics/TAG/arena/agents/b100/a4_noH.json (has no heuristic entry, so the default is used)

**E4. H2 and "kept its strength" (§IV-A and contribution 2).**
- The report says: "whereas v4 kept its strength (55.7%, 49.7%, 47.6%), so the gain from S and I grows with the budget (+5.6, +9.0 and +14.8 points), as H2 predicts."
- v4 lost 8.1 ± 7.6 percentage points (pp) between 100 and 900 ms (two-sample z = 2.1, p ≈ 0.04). v3 lost 17.3 ± 7.3.
- The rise in the S+I gain is 9.2 ± 10.5 pp (z = 1.7; a weighted trend test also gives z = 1.7), so it is not significant. The +5.6 at 100 ms is not significant on its own either (± 6.2).
- Evidence: /home/user/statistics/groupA_code/experiments_v4/RESULTS.md lines 36–39, 50–55, 65–67. These are all † runs, so they are independent samples.

**E5. Non-significant 4-player differences are presented as gains (§IV-D).**
- The report says: "X0 raises it to 49.6% at 100 ms and X0 + W2 to 45.6% at 500 ms (250 games), and F + L gives 43.8% on other deals."
- The actual differences:
  - X0: +3.2 ± 6.2 pp (z = 1.0).
  - X0 + W2 + K = 1 against v4: +7.2 ± 8.6 (z = 1.6); paired against X0 alone: +3.4 ± 8.6.
  - F + L8: +1.0 ± 7.9 on the same deals. At 4p/100 ms it costs −6.6 ± 6.0 (z = −2.2), which the report leaves out.
- None of the 4-player additions is individually significant.
- Evidence:
  - /home/user/statistics/groupA_code/experiments_v4/RESULTS.md lines 77, 82, 96, 98, 101, 111–112
  - /home/user/statistics/groupA_code/groupA/README.md lines 193 and 343–348

**E6. Confounded comparisons are described as single changes.**
- (a) "adding X0 + W2 to F + L costs 7.7 ± 5.4 points": the two runs also differ in K (0.5 → 1.0) and in opponentScoreValue. Evidence: /home/user/statistics/TAG/arena/agents/b500/finalI_FL8.json against finalI_X0K10W2FL8.json.
- (b) "giving only our agent 1000 ms … (−4.8 ± 8.1)": that run also used K = 1.37. Evidence: /home/user/statistics/TAG/arena/agents/b500/finalI_X0K14W2FL8_t1000.json.
- (c) R3V2's −23.7 is used to conclude "rollouts must end with the round", but R3V2 changes both the rollout length and the terminal value scale. Evidence: /home/user/statistics/groupA_code/experiments_v5/README.md line 49.

**E7. Gap and novelty claims.**
- The report says (§II-C): "ISMCTS and DUCT have been studied … mostly in two-player games; how they combine … is not known".
- It also says (§III): "three elements are our own: … decoupled selection runs on an open-loop information-set tree … and the agent switches its settings with the number of players."
- ISMCTS [11] was developed and evaluated on Dou Di Zhu, which is a three-player game.
- The TAG version in this repository already supports decoupled UCT on single-tree information-set search for Sushi Go:
  - /home/user/statistics/TAG/src/main/java/players/mcts/MCTSParams.java lines 41–42 and 226–230
  - /home/user/statistics/TAG/json/players/gameSpecific/SushiGo/SushiGoDUCT.json
  - /home/user/statistics/TAG/src/test/java/players/mcts/SushiGoDecoupledTests.java
- TAG also has a LeaderHeuristic (score minus best opponent): /home/user/statistics/TAG/src/main/java/players/heuristics/LeaderHeuristic.java.
- Switching parameters by player count is not an algorithmic contribution.

### Minor or moderate

- **E8.** "With up to ten cards per hand and three or four players …" is wrong: hands hold 12 − n cards, so 9 with 3 players and 8 with 4. Raw joint moves are up to 729 (3p) or 4096 (4p). Evidence: SGForwardModel.java line 42; SGParameters.java line 44.
- **E9.** "Most points are delayed and conditional" is contradicted by your own data: at 500 ms the agent scored 23.0 of 42.1 points (55%) from Dumplings and Nigiri, which TAG banks on reveal. Evidence: /home/user/statistics/groupA_code/experiments_v3/RESULTS.md lines 137–147.
- **E10.** Game counts are misstated:
  - "measuring each in experiments of 500 games" (§I) and "Each condition has 500 games, except 250 at 900 ms and in some 4-player 500 ms runs" (§III-E) are both inaccurate.
  - Table II's ISMCTS × 2 row has 300 games (/home/user/statistics/groupA_code/experiments_v5/RESULTS.md line 34).
  - Every 4p/500 ms development run has 250 games (runs.md lines 77–111).
- **E11.** "100 ms for development and 500 and 900 ms for validation": development also used 500 and 900 ms (Fig. 2 runs and all of §IV-D). Evidence: v4 RESULTS.md lines 46–68 and 90–130.
- **E12.** "correlate by only 0.1–0.2": the measured win correlations between the final agent and its ablations are 0.01–0.14 (computed from experiments_v4/results/V_3p100.csv against A_*.csv). The conclusion stands; only the number is wrong.
- **E13.** "(about 100 runs on an 18-core laptop, 8 games in parallel)" sits awkwardly next to "161 runs", and the parallelism is wrong for most runs: v3 ran 16/10/6 games in parallel, v4 at 100 ms ran 8–16, and v5 ran 4 on a 4-core machine. Evidence: experiments_v3/README.md lines 50–53; experiments_v4/README.md lines 56–58; experiments_v5/README.md line 16.
- **E14.** W2 and X0 are under-specified:
  - The W2 formula "tanh((Si − maxj Sj)/2)" should use max over j ≠ i, and the code adds a 0.01 × Pudding tie-break (SushiTreeNode.java line 1450). With max over all j, the winner's value would always be 0.
  - W2 also includes opponentScoreValue (opponents in the max-n backup value their own lead over the others' mean), which the report never mentions (README line 162).
  - X0 also changes T from 4 to 6, and its constants are never given (README line 188).
- **E15.** Literature claims:
  - [14] does not show that max-n "generally outperforms" paranoid. It analyses UCT in multi-player games, and later MCTS work finds the best backup policy depends on the game.
  - The DUCT non-convergence result is due to Shafiei et al. (2009), and EXP3 in simultaneous-move MCTS to Teytaud & Flory (2011). [12] compares UCB, EXP3 and regret matching (RM).
  - §V says RM "suits simultaneous moves better than UCB [12]", which contradicts your own §II-C ("depends on the game [13]").
  - "plans around opponent cards it cannot know" describes clairvoyance or non-locality, not strategy fusion.
- **E16.** "all variants in our experiments run the same code": v2 is a frozen copy in package refv2 (experiments_v4/README.md lines 4–5 and 15).
- **E17.** The TAG ISMCTS description is incomplete:
  - Its rollouts are random to depth 300 (that is, to game end), evaluated with ScoreHeuristic, with exploreEpsilon 0.1.
  - MAST "Both" is enabled but the rollout policy is RANDOM.
  - The configuration is TAG's SushiGo_3+P_HiBudget.json, tuned for 400 ms, re-used at 100/500/900 ms. Evidence: /home/user/statistics/TAG/arena/agents/b100/tagmcts.json against /home/user/statistics/TAG/json/players/gameSpecific/SushiGo/SushiGo_3+P_HiBudget.json.
  - "TAG's score heuristic" refers to two different functions: score/50 in the −H ablation and ScoreHeuristic for OSLA.
- **E18.** The timing sentence reports only CPU time (true: maximum 901 ms). It leaves out:
  - wall-clock decisions over 1 s: 1 of 73 with the default JVM (max 1003.5 ms), 5 of 73 with a 2 GB heap (max 1455 ms), and 8 of 76 with F (max 1653 ms);
  - any mention of the 500 ms competition file.
  - Evidence: experiments_v5/results/probe_quiet.txt line 2; probe_quiet_defaultjvm.txt line 1; probe_quiet_F.txt line 1.
- **E19.** Gen-AI translation: "ornekler tipler" means "examples, tips", not "examples, templates".
- **E20.** Fig. 3's label "(v5)" is never defined. The bottom-panel effects are relative to a re-run of the final agent on another machine (58.1%), not the 55.0% base of the top panel; the caption does not say so.
- **E21.** Some numbers cannot be traced to any saved log, although the Gen-AI note says every number is taken from the experiment folders: "12 000 iterations … 700 joint actions … 45% … depth 1.6 … 93–96% of 74 positions" and "7–11 times more iterations" appear only as prose in /home/user/statistics/groupA_code/groupA/README.md (lines 348–352 and 389–394). No TreeStats4 or Agree4 output exists in the repository.

### Verified correct (no action needed)
- All 8 rows of Table II, all of Fig. 3, the IV-A factorial and v3 numbers, the IV-D win rates and paired deltas, and the IV-E numbers including the 45.0% greedy result.
- 161 runs and 67,525 games.
- The game rules: 108 cards, scoring, 12 − n cards per hand, the −6 Pudding penalty only with more than 2 players, scoring on reveal.
- The agent parameters (σ, w, α, T, ε, K, w_t) and the opponent parameters.
- All 15 references are bibliographically accurate; [1] and [2] match the citations in Labs 1 and 4.
- The 3p/500 ms margin of ±0.39 is 0.395 in the data, so it is fine.

---

## 3. Compliance with the brief and template

1. **Page limit: compliant.** The main text ends halfway down p. 4, right column. The Gen-AI note and references spill onto p. 5.
2. **Template.** The styles are identical to Paper_Template.docx. However, all five sections in the DOCX use a 540-twip (27 pt) top margin, whereas the template's two-column body section specifies 54 pt; the text on pp. 2–5 starts 26–27 pt from the top edge. LibreOffice renders the original template the same way (its re-save in build_report.py is the likely cause), but in Word the template's body pages use 54 pt. Restoring 54 pt still fits on 4 pages.
3. **Structure: compliant.** All required sections are present.
4. **Word counts** (the brief's counts are a guide, but these overruns are why the text is so compressed):

   | Section | Words | Guide | Status |
   |---|---|---|---|
   | Abstract | 124 | 75–125 | At the maximum |
   | Introduction | 479 | 350–450 | Over |
   | Background | 657 | 650–850 | Just above the minimum |
   | Method | ≈1015 | 700–800 | +27% |
   | Results | ≈802 | 500–700 | +15% |
   | Conclusions | 218 | 200–300 | OK |
   | References | 15 | 6–15 | At the maximum |

5. **Games per experiment.** Most conditions have 500 games. The 250-game runs are justified by CPU cost. The 300-game row is not acknowledged. The 4-player conclusions resting on 250-game runs will invite the rubric's "<500 games per pair is insufficient" rule unless they are hedged or re-run.
6. **Competition setting.** The brief contradicts itself (§3 says 3 players at 1 s; §4 says 4 players at 500 ms). The report mentions only "1 s".
7. **Figures and tables.** All are captioned and referenced in the text. Remaining issues:
   - Table II cells wrap ("3 p, 100 / ms", "+1.00 ± / 0.44").
   - Fig. 3 uses internal codes (X3, V2D, W1, R3V2, v4, v5).
   - Fig. 2 has no x-axis title.
   - Figures are embedded as 300-ppi JPEG.
8. **References.** The style is consistent IEEE. The problems are coverage and attribution (E15). Missing:
   - RHEA, OSLA, MAST, open-loop MCTS;
   - TAG's MultiTree MCTS (the opponent's configuration);
   - simultaneous-move MCTS non-convergence;
   - the original max-n and paranoid papers;
   - drafting-game AI.
   - Sushi Go! itself is cited only inline.
9. **Generative-AI note.** It is placed correctly and quotes two prompts. Gaps against brief §2:
   - (a) No section carries a citation to Claude, and Claude is not in the reference list, although the note says Claude drafted "the text, tables and figures".
   - (b) Follow-up and revision prompts, and the code-generation prompts, are missing.
   - (c) The translation error (E19).
   - (d) The claim that every number comes from the experiment folders is not quite true (E21).
10. **Acronyms and spelling.** UCT, UCB1, UCB-Tuned, MAST, RHEA and OSLA are never expanded, and "open-loop" is never defined. The text spells "determinization" while Fig. 3 says "determinisation".
11. **Evidence the marker cannot see.** experiments_v3–v5 are not part of the submission and are mentioned only in the Gen-AI note. Add a data/code availability statement.

---

## 4. Prioritised changes

**P1 (Experimentation, Conclusions; highest priority).**
- Re-run Table II on a disjoint seed block, e.g. `seed=100000`: 3p at 100/500 ms, 4p at 100/500 ms, 3p at 900 ms. This takes about 2.5–3 h on 8 threads.
- Rewrite §III-E as: "Game g uses seed s+g; development used s = 1, 2 (game seeds 1–501) and 1001–3001; validation used s = 100000, disjoint from every development deal."
- If you cannot re-run, change the caption to "SEED BLOCK 7", drop "unseen" everywhere, and add the overlap as a stated limitation, noting that the 4p rows are not a clean hold-out.

**P2 (Experimentation, Conclusions, Abstract).** Make every claim match its statistics.
- §IV-A: replace with "v3 lost 17.3 ± 7.3 pp from 100 to 900 ms, v4 8.1 ± 7.6 (z = 2.1). The S+I gain rose from +5.6 ± 6.2 to +14.8 ± 8.5 pp; the rise (9.2 ± 10.5, z = 1.7) is in the direction of H2 but not significant."
- Add the v3 + S data (+1.3 at 100 ms, +6.2 at 500 ms; v4 RESULTS.md lines 38 and 54) to separate the effect of S from I.
- §IV-C: "On the score margin all five removals remain significant after a Holm correction; on win rate only −H and −S−I do (−R z = 2.0, −B 1.4, −I 1.3). H1 holds for margin, only partly for wins."
- Explain why P and M cannot be removed from the final agent (S requires max-n values; I keys statistics by card type, which subsumes P), and cite their v3 effects (M main effect +4.1 ± 2.0; −P −2.1 ± 6.2).
- §IV-D: give each 4-player change its CI (E5), name K = 1 in the package and K = 1.37 in the 1000 ms run, and either justify keeping F+L or drop it.
- Add the omitted result: giving our agent only 100 ms in the 4p/500 ms setting cost 9.0 ± 7.7 pp (z = 2.3; v4 RESULTS.md line 124).
- Report H3 as an interaction: the 500 ms package is +7.2 ± 8.6 at 4p and −7.7 ± 5.4 at 3p, a difference of 14.9 ± 10.2 (z ≈ 2.9).
- Use "pp" for win-rate differences everywhere.

**P3 (Approach).** Correct the Method.
- Eq. (4): write "√(ln N_p(a)/n_p(a)), where n_p(a) is the number of times p chose a and N_p(a) its availability count [11]".
- W2: write max over j ≠ i, mention the Pudding tie-break, and add opponentScoreValue.
- Give the X0 constants (σ 15, w 0.7, α 0.5, T 6, β 0.5, T_m 3, κ 0.25, δ 1.5, π 1.25, average Nigiri 3, γ 3) in a footnote to Table I.
- Say how Chopsticks are handled: the second card is the best card by marginal value.
- Tone down "three elements are our own", and position the work against TAG's `decoupled` option, which the rules forbade you to use.

**P4 (Experimentation: critical analysis).**
- Replace the −H explanation (E3). Better still, add a 500-game run with TAG's LeaderHeuristic in place of H, which isolates what the card model adds beyond a lead-based value.
- Explain why T = 4 was chosen although T = 8 has a +3.6 main effect: the R × T interaction is −4.2 ± 2.1 (z = −4.0), and the 1,500-game tie-break gave T 4 +3.9 ± 3.4 (v3 RESULTS.md lines 94 and 195).
- Link the small I effect to Long et al.'s argument that hidden information is revealed quickly [9].
- For v3's decline with budget, consider the implementation alternative: its closed-loop tree holds 0.5–1 GB of live data per game, with garbage-collector thrashing (experiments_v3/README.md lines 50–53). Report iterations per decision at each budget.
- Note that the ISMCTS seats' win rate rises from 27.7% to 35.1% between 100 and 500 ms.
- State the multiple-testing position.
- Discuss the drop from development to validation at 4p/500 ms (45.6/43.8 → 41.4).

**P5 (Problem definition).**
- Correct the gap statement (E7). Add 3–4 targeted references (verify the details before citing): Goodman, Perez-Liebana & Lucas, "MultiTree MCTS in Tabletop Games" (CoG 2022); Whitehouse, Powley & Cowling (CIG 2011, Dou Di Zhu); Shafiei, Sturtevant & Schaeffer (2009); Nijssen & Winands, "Search policies in multi-player games" (ICGA J. 2013). Merge [4] and [5] to stay at 15.
- Replace the "settings and a fast simulator" contribution with "evidence that evaluation/exploration settings must differ between 3 and 4 players (H3)".
- Fix E8 and E9.
- Cite motivating literature in the Introduction.

**P6 (General: Gen-AI compliance).**
- Add a reference such as "[16] Anthropic, Claude (via Claude Code), 2026" and cite [16] in every section it drafted.
- List all prompts, including revisions and representative code prompts.
- Fix the translation.
- Add a sentence on what the group itself designed and verified.

**P7 (General: presentation).**
- Restore the 54 pt top margin.
- Reformat Table II (e.g. "3p/100", and the CI in its own column or in parentheses).
- In Fig. 3, rename (v4)/(v5) to "final agent" and "additions (last study)", and say in the caption that the bottom panel uses a different base.
- Add an x-axis title to Fig. 2.
- Expand all acronyms and make spelling consistent.
- Replace letter codes in §IV with short names.

**P8 (Experimentation: documentation).** Replace much of the §III-E prose with a one-column setup table. It should give:
- opponents with their source file and every parameter;
- 4p seating (opponents split 2 + 1, alternating);
- seeds, games, budgets, hardware, Java version and TAG version;
- the canonical-deal procedure;
- a code/data availability statement.

Also move the timing sentence (with the wall-clock tail from E18) here.

**P9 (Conclusions).**
- Restructure as answers to RQ1–RQ3 with CIs, then limitations (seed overlap if not re-run, opponent set, CPU vs wall-clock time, machine dependence), then future work, each item with a rationale.
- Remove "every component contributes", "is what lets…" and "model … is now the bottleneck". The negative results change the model (X3, W1, V2D) and still fail, so they suggest the search is insensitive to hand-tuned changes to the evaluation, which motivates a learned evaluation.
- Fix the regret-matching rationale.

**P10 (optional experiments, about 10 min each at 100 ms).**
- A 500-game run against TAG's DUCT configuration (SushiGoDUCT.json with an adjusted budget). This is the most relevant baseline for S+I.
- The LeaderHeuristic ablation from P4.
- Iterations per decision for v3 and v4 at each budget.

**P11 (Abstract).** Within 125 words: add the 4-player and budget answers, de-emphasise OSLA, and write "against one copy each of TAG's ISMCTS and our previous agent".

---

## 5. Overclaiming, unclear or unsupported statements

- **"supporting H1" / "In paired ablations every component contributes."** On win rate, R, B and I are not significant after a Holm correction.
- **"matching the tree to simultaneous moves is what lets the agent use longer budgets."** Causal language on unpaired, confounded comparisons; the trend is not significant (E4).
- **"negative results showing that the model of the game, not the search effort, now limits the agent" / "the search is close to converged".**
  - This rests only on 4p/500 ms evidence, which in part has no stored log (E21).
  - The −9.0 ± 7.7 result at 100 ms was omitted.
  - X3, V2D and W1 are themselves model changes, so the inference does not follow.
- **"The rollout policy thus has to give unbiased leaf values."** Bias was never measured, and R3V2 is confounded. Say "consistent with simulation balancing [8]".
- **"it trades margin for first places."** No evidence is given. A margin measured against the best of three opponents is mechanically lower than against the best of two.
- **"With four players the 3-player agent is weaker."** Relative to chance it is not: 46.4/25 = 1.86× against 55.7/33.3 = 1.67×.
- **"supporting H3."** Only one side is significant; present the interaction (P2).
- **"84–87% against … OSLA."** OSLA wins 0.0% of its seats (v4 RESULTS.md line 137), probably because a chosen card is usually not revealed until everyone has chosen, so its one-step look-ahead sees little. Explain this or drop OSLA from the headline.
- **"a win-focused value do[es] not help" (§V).** This conflicts with adopting the win-gap value W2 for 4 players.
- **Selective omissions:**
  - the R × T interaction;
  - the cost of F+L at 4p/100 ms;
  - the 100 ms result in the 4p/500 ms setting;
  - the README's own remark that the 4p development base "was probably lucky";
  - the v3 + S data.
- **Unclear wording:**
  - "treats the opponents as if they knew what the searcher knows" (say instead that opponents' choices are not conditioned on their own hidden hands);
  - "served for screening";
  - "changes as few decisions";
  - internal names X0, W2, R3V2;
  - Table I's row "W2, F, L" lumps three modifications together.
- **Breadth over depth.** More than 15 ideas (A, D, P, M, R, S, I, X0, W2, F, L, N, X3, V2D, W1, R3V2, O) squeezed into 4 pages. The brief explicitly favours fewer ideas analysed more deeply. Cut IV-E to one sentence plus Fig. 3, and spend the space on S, I and H.

---

## 6. Before submission
- Fill in the author block: names and student numbers for members 2 and 3, and the student number for Halit Ozger. The first author block also sits higher than the other two.
- Confirm the real group letter. /home/user/statistics/groupA_code/groupA/README.md line 3 says "Rename first: replace every groupA …". The package, the JSON `class` entries and the report file name must all match.
- Ask the module team which competition setting applies (brief §3 vs §4) and submit the matching JSON. Never submit the 1000 ms file to a 500 ms competition.
- Each member must submit an individual 300-word reflection PDF.
- After editing, re-render (ideally from Word), check the main text still ends on p. 4 and the Gen-AI note plus references end by p. 5, and regenerate every changed number from the CSVs.
