#!/bin/zsh
# Prints every results table (markdown) from the arena CSV files. usage: arena/make_tables.sh > tables.md
cd "$(dirname "$0")/results"
S() { python3 ../summarise.py "$@"; }
have() { for f in "$@"; do [ -f "$f" ] || return 1; done; }

echo "### 1. Screening at 100 ms (seed 1, 500 games each, paired with v2)"
S "v2 = previous final agent (H+A+B+D)=s1_ref.csv" "v2 + P=s1_P.csv" "v2 + O=s1_O.csv" "v2 + P + O=s1_PO.csv" "v2 + P + O − A=s1_PO_noA.csv" \
   "HBDP (v2 + P − A)=s2_base.csv" "HBDP + K 0.5=s2_K0.5.csv" "HBDP + M=s2_M.csv" "HBDP + rollouts to game end=s2_rl100.csv" \
   "HBDP + R=s2_R.csv" "HBDP + R + M=s2_RM.csv" "HBDP + R, K 0.5=s2_R_K0.5.csv" \
   "HBDP + R, ε 0.1=s3_R_eps0.1.csv" "HBDP + R, ε 0.3=s3_R_eps0.3.csv" "HBDP + R − D=s3_R_noD.csv" \
   "HBDP + R, T 8=s3_R_T8.csv" "HBDP + R, α 1=s3_R_alpha1.csv" "HBDP + R, leaderWeight 0.5=s3_R_lw0.5.csv" \
   "HBDP + R, σ 8=s3_R_sigma8.csv" "HBDP + R, K 0.5, T 8=s4_K0.5_T8.csv" "HBDP + R, K 0.3, T 8=s4_K0.3_T8.csv" \
   "HBDP + R, K 0.5, α 0.25=s4_K0.5_alpha0.25.csv" "HBDP + R, K 0.5, T 8, ε 0.3=s4_K0.5_T8_eps0.3.csv" \
   "candidate = HBDP + R + M, K 0.5, T 8=s4_K0.5_T8_M.csv" "**final** = HBDP + R + M, K 0.5 (T 4)=abl_T4.csv"
echo
echo "### 2. Replication on new deals (seed 1001, 100 ms)"
args=("candidate T 8 (+ M)=rep_K0.5_T8_M.csv")
[ -f rep_T4.csv ] && args+=("**final (T 4)**=rep_T4.csv")
S "v2 (H+A+B+D)=rep_ref.csv" "HBDP + R, K 0.5, T 8=rep_K0.5_T8.csv" "${args[@]}"
echo
if have abl_noR.csv; then
  echo "### 3. Leave-one-out ablation around the T 8 candidate (seed 1, 100 ms, paired with it)"
  args=("− M=s4_K0.5_T8.csv")
  for a in noR noP noB noD plusA plusO K1.41 T4 noH; do
    [ -f abl_$a.csv ] && args+=("${${a/no/− }/plus/+ }=abl_$a.csv")
  done
  S "candidate (T 8)=s4_K0.5_T8_M.csv" "${args[@]}"
  echo
fi
if have tb1_ref.csv tb2_T4_noR.csv; then
  echo "### Tie-break on new deals (100 ms; seeds 2001 and 3001, 500 games each; base v2)"
  S "v2 (seed 2001)=tb1_ref.csv" "candidate T 8=tb1_final.csv" "**final = T 4**=tb1_T4.csv" "T 4 + A=tb1_T4_A.csv" "T 4 − R=tb1_T4_noR.csv"
  echo
  S "v2 (seed 3001)=tb2_ref.csv" "candidate T 8=tb2_final.csv" "**final = T 4**=tb2_T4.csv" "T 4 + A=tb2_T4_A.csv" "T 4 − R=tb2_T4_noR.csv"
  echo
  echo "Pooled paired comparisons:"
  tt=("T 4 vs T 8 (seeds 1, 2001, 3001; the tie-break)=s4_K0.5_T8_M.csv:abl_T4.csv,tb1_final.csv:tb1_T4.csv,tb2_final.csv:tb2_T4.csv")
  fv=("final (T 4) vs v2 (seeds 1, 2001, 3001)=s1_ref.csv:abl_T4.csv,tb1_ref.csv:tb1_T4.csv,tb2_ref.csv:tb2_T4.csv")
  if [ -f rep_T4.csv ]; then   # seed 1001 was run after the tie-break, as a further check
    tt+=("T 4 vs T 8 (seeds 1, 1001, 2001, 3001)=s4_K0.5_T8_M.csv:abl_T4.csv,rep_K0.5_T8_M.csv:rep_T4.csv,tb1_final.csv:tb1_T4.csv,tb2_final.csv:tb2_T4.csv")
    fv+=("final (T 4) vs v2 (seeds 1, 1001, 2001, 3001)=s1_ref.csv:abl_T4.csv,rep_ref.csv:rep_T4.csv,tb1_ref.csv:tb1_T4.csv,tb2_ref.csv:tb2_T4.csv")
  fi
  python3 ../pooled.py "${tt[@]}" \
    "T 4 + A vs T 4 (seeds 2001, 3001)=tb1_T4.csv:tb1_T4_A.csv,tb2_T4.csv:tb2_T4_A.csv" \
    "T 4 − R vs T 4 (seeds 1, 2001, 3001)=abl_T4.csv:d_MK.csv,tb1_T4.csv:tb1_T4_noR.csv,tb2_T4.csv:tb2_T4_noR.csv" \
    "${fv[@]}" \
    "candidate T 8 vs v2 (seeds 1, 1001, 2001, 3001)=s1_ref.csv:s4_K0.5_T8_M.csv,rep_ref.csv:rep_K0.5_T8_M.csv,tb1_ref.csv:tb1_final.csv,tb2_ref.csv:tb2_final.csv"
  echo
fi
if have f5_ref.csv f5_finalM.csv; then
  echo "### 4. Budget 500 ms (3 players, seed 1, 10 games in parallel, paired with v2)"
  args=("candidate T 8=f5_finalM.csv")
  [ -f f5_finalT4.csv ] && args+=("**final (T 4)**=f5_finalT4.csv")
  S "v2 (H+A+B+D)=f5_ref.csv" "${args[@]}"
  echo
fi
if have f5_finalT4.csv f5_K1.csv f5_noB.csv; then
  echo "### 4b. Changes aimed at long budgets, 500 ms (seed 1, paired with the final agent)"
  S "final (K 0.5, B on)=f5_finalT4.csv" "K 1.0=f5_K1.csv" "− B (uniformly random rollouts)=f5_noB.csv"
  echo
fi
if have f9_ref.csv f9_finalT4.csv; then
  echo "### 5. Budget 900 ms (3 players, seed 1, 250 games, 6 games in parallel, paired with v2)"
  S "v2 (H+A+B+D)=f9_ref.csv" "**final (T 4)**=f9_finalT4.csv"
  echo
fi
if have q4_ref.csv q4_final.csv; then
  echo "### 6. Four players, 100 ms (seed 1; the three opponent seats alternate v2 / TAG ISMCTS, 2 + 1, swapped every 4 games; chance 25 %)"
  S "v2 (H+A+B+D)=q4_ref.csv" "**final (T 4)**=q4_final.csv"
  echo
fi
if have q5_ref.csv q5_final.csv; then
  echo "### 7. Four players, 500 ms (seed 1, 250 games, 10 games in parallel; opponent seats as in 6; chance 25 %)"
  S "v2 (H+A+B+D)=q5_ref.csv" "**final (T 4)**=q5_final.csv"
  echo
fi
for pair in "f5_ref f5_finalT4 500" "f9_ref f9_finalT4 900"; do
  set -- ${=pair}
  if have $1.csv $2.csv; then
    echo "### Points per card type of the focus agent, $3 ms (v2 vs final, same deals)"
    python3 ../types.py $1.csv $2.csv
    echo
  fi
done
