# Check of draft 15 (rewritten sections only)

I didn't write `/tmp/claude-0/verify15/findings.md`: the file-writing tool refused report files from a subagent, and I didn't work around that. The full findings are below.

Every number in the changed parts is correct, and the layout passes all four checks. But the revision did introduce errors: five claim or consistency problems (B1–B5), a smaller overstatement in the abstract (B6), five language fixes (C1–C5), and a code bug that writes invalid XML into the .docx (D1).

Nothing under `/home/user/statistics` was changed. My recomputation script is `/tmp/claude-0/verify15/verify.py` (run with `python3 -I`). Page renders are `/tmp/claude-0/verify15/page-1.png` to `page-5.png`. I rebuilt the report in a sandbox copy, `/tmp/claude-0/verify15/sandbox/report/`; it reproduces the repository PDF text exactly, and I used it to test every replacement below.

## A. Numbers: all correct
I recomputed every number in the abstract, Table III (win rates, CIs, margins, ISMCTS column), IV-A, the end of IV-B, IV-C, IV-D, the Conclusions and Table II, and each matches to the printed precision. Some spot values:
- **Abstract lead:** 52.60 − 26.40 = 26.2; 44.80 − 36.29 = 8.5.
- **Table III, ISMCTS column:** seat-weighted shares are 26.40, 35.90, 36.29, 24.10 (748 seats), 32.09, 24.00 and 24.34 (600 + 400 seats).
- **IV-C:** dev rise +9.20 ± 10.42 (z 1.73); held-out difference of differences −0.80 ± 8.10 (z 0.19); pooled 500 ms −5.48 ± 4.19 (z 2.56); pooled 900 ms −4.88 ± 4.85 (z 1.97); ISMCTS +9.50 ± 5.71; iterations 2387 / 11865 / 21246.
- **IV-D:** kept settings +4.27 ± 4.32, +5.80 ± 7.39, +1.00 ± 7.86; pooled four-player −6.69 ± 4.11 (z 3.19); tree statistics 73 / 704 / 53% / 2, 4, 4, 7.
- **Factorial M ± 2.1:** matches the two-sample SE, which fits the independent deals the text states (`RESULTS.md` shows ± 2.0 from a per-game contrast). No change needed.
- **Pairing:** game-number pairing is valid (focus scores correlate 0.23–0.39 between paired runs, about 0 when misaligned), and each joined CSV equals its chunks.

## B. Claim and consistency problems

**B1. Table II, "Hardware, records" row** (`build_report.py` lines 687–689)
- **Current:** "development, ablation and seed-7 runs: 18-core laptop; other runs: 4-core machine; per-game results and logs of all 167 runs: folder results of our code package"
- **Why it is wrong:** nine seed-7 runs ran on the 4-core machine (`experiments_v5/README.md`): `v5_base3p100` (the 58.1% re-run), `r2_lead_3p100`, `v5_X3`, `v5_N05`, `v5_V2D`, `v5_W1`, `v5_R3V2`, `v5_4X3_4p100` and ISMCTS × 2 (`v5_field` + `r2_field_3p100b`, seed 307). The row contradicts IV-A ("re-run on the second machine"), IV-B ("on the same machine (58.1%)"), Fig. 2 (bottom) and Table III.
- **Replacement:** "development, ablation and first seed-7 runs: 18-core laptop; later runs: 4-core machine; per-game results and logs of all 167 runs: folder results of our code package"

**B2. Conclusions ¶2** (lines 1000–1001)
- **Current:** "H, B and R were ablated only with three players at 100 ms;"
- **Why it is wrong:** B and R were also ablated at 500 ms, in runs that are among the 167:
  - `f5_noB`: v3 without B, 3 players, 500 ms, −1.4 ± 6.0 pp (in both `RESULTS.md` files).
  - `q5_noB`: v4 without B, 4 players, 500 ms, −5.4 ± 8.3 pp.
  - `q5_FL8R3`: the four-player 500 ms agent with game-end rollouts, i.e. without R, −2.4 ± 8.1 pp.
- **Replacement:** "the leave-one-out ablation used only three players at 100 ms;"

**B3. IV-B and the Conclusions contradict each other on R**
- **Current IV-B** (line 860): "a 2^4 factorial around v3 (16 × 500 games, independent deals) confirmed R (+8.7 ± 2.1 pp) and M (+4.1 ± 2.1 pp)."
- **Current Conclusions** (lines 991–992): "round-end rollouts, informed rollouts and information sets alone raise the score margin but not detectably the win rate."
- **Why it is wrong:** IV-B gives R a win-rate gain with z ≈ 8. The Conclusions' claim holds only for the final agent after Holm (`A_noR` −6.2 ± 5.9, z 2.04). "Confirmed" also overstates: the factorial was v3's seed-1 development run, done before the final-agent ablation, and v3 is its best cell.
- **Replacements:**
  - IV-B: "…supported R (+8.7 ± 2.1 pp) and M (+4.1 ± 2.1 pp)."
  - Conclusions: "…alone raise the final agent's score margin but not detectably its win rate."

**B4. IV-D ¶2, first sentence** (lines 943–947)
- **Current:** "With four players our agent gained from its own extra time only up to 500 ms: against 500 ms opponents our 100 ms agent won 9.0 ± 7.7 pp less than our 500 ms agent and a tree cut to depth 1 lost 17.4 ± 8.2 pp, but 1000 ms…"
- **Why it is wrong:** the 9.0 pp is not a time effect, because the two agent files also differ in settings:
  - 100 ms file (four-player block): X0, K 0.5, w 0.7, no W2, no F + L.
  - 500 ms file: X0, K 1, W2, F + L8.
  - Development estimates at 500 ms for those differences: K +3.8 ± 8.5, W2 +3.4 ± 8.6, F + L +1.0 ± 7.9, about +8.2 pp in total.
  - The same configuration at 500 ms versus 100 ms gains only +3.6 ± 8.4 pp (`q5_X0` vs `q5_X0b100`, z 0.8).
- **Replacement:** "With four players, against 500 ms opponents our 100 ms agent (without W2, F + L and K = 1) won 9.0 ± 7.7 pp less than our 500 ms agent and a tree cut to depth 1 lost 17.4 ± 8.2 pp, but 1000 ms…"

**B5. IV-C ¶2, last sentence** (lines 920–922)
- **Current:** "…while ISMCTS, without such a model, keeps gaining, and our parameters were tuned at 100 ms."
- **Why it is wrong:** ISMCTS goes from 35.9% to 36.3% between 500 and 900 ms (+0.4 ± 6.0); it gained only up to 500 ms. The narrowing of our lead from 11.3 to 8.5 pp comes from our own decline. The last clause is a separate explanation joined on with "and".
- **Replacement:** "…while ISMCTS, without such a model, gained up to 500 ms; our parameters were also tuned at 100 ms."

**B6. Abstract, minor overstatement** (line 442)
- **Current:** "With more time ISMCTS catches up: our lead over it per seat…"
- **Why it is wrong:** at 900 ms we still lead by 8.5 pp (≈ ± 7.9, z ≈ 2.1). The abstract also never says that all agents get the extra time.
- **Replacement:** "With more time for all agents, ISMCTS closes in: our lead over it per seat…" The abstract stays at 15 lines.

## C. Language
**Should fix:**
- **C1. IV-D:** "Both agents did better on seed block 7 (46.8% and 41.4%)" reads in reverse, because the 500 ms agent is named first. Use "(100 ms: 46.8%, 500 ms: 41.4%)".
- **C2. IV-D:** "The tree statistics suggest why:" has no clear referent after the seed-block-7 sentence. Use "…suggest why 1000 ms did not help:".
- **C3. Conclusions:** "TAG's ISMCTS gained 9.5 pp of its seats" should be "won 9.5 pp more of its seats".
- **C4. Conclusions:** "(untested, in our code)" should be "(an untested option in our code)".
- **C5. IV-C ¶1:** E2 is no longer named anywhere in the Results or Conclusions. Use "The held-out deals do not support this (E2):".

**Optional:**
- "regret matching or EXP3 deserve" should be "and" (wording not changed by this commit).
- "2.4k to 11.9k and 21.2k" could be "2400 to 11 900 and 21 200" for IEEE style.
- "28.3 pp of wins without it" is elliptical.
- "informed rollouts" in the Conclusions is "card-value rollouts" in the abstract and Table I.
- X3 also changes the evaluation H, so "consistent with simulation balancing" is only one reading; the hedge makes it acceptable.
- "No four-player setting gained significantly" follows S + I's significant four-player gain (z 2.4); "four-player-only setting" would avoid a misreading.

## D. Layout and build
**D1. Code bug in `add_table`** (lines 1155–1158)
- **What happens:** `mar = OxmlElement("w:tcMar")` overwrites the new `mar` parameter before `str(mar)` uses it. Every cell margin is written as `w:w="<Element {…}tcMar at 0x…>"` (214 attributes across all three tables; draft 14 wrote `"50"`).
- **The .docx:** it is now schema-invalid, and Word is likely to report unreadable content.
- **The PDF:** LibreOffice reads the value as 0, so all tables have zero cell padding. An explicit `w:w="0"` rebuild reproduces the current PDF exactly.
- **Spare lines:** the commit's "4.5 lines spare" depends on the bug. With the intended 50/30 margins, about 3 lines remain and the GenAI heading moves to page 5.
- **Fix:** rename the local variable (for example `tcm`) and pass `mar=30` for all tables. That keeps identical line positions, and Table III stays at one line per row.

**D2. Layout checks: all pass as built.**
- The main text ends on page 4, about 4 lines above the column bottom.
- The GenAI heading and its first two lines sit at the foot of page 4; the rest of the statement and all 15 references end on page 5, about 58% down.
- Table III has one line per row.
- No "[pending]" remains; only the expected author placeholders.

**All fixes together:** with B1–B6 and C1–C5 applied in the sandbox, the main text still ends on page 4. About 1 line is spare with 30-twip margins; with 50/30 it fills page 4 exactly. The references stay on page 5.

## E. No problems found in
- Every number (section A).
- Table III's values, note and layout.
- The IV-A wording: "within sampling error" is fine, and "Pudding bonuses" is correct because the CSV Pudding points are only 0, 2, 3 or 6.
- The Introduction edits.
- The IV-C ¶1 logic, apart from C5.
- IV-D ¶1.
- The Conclusions' RQ3 sentence.
