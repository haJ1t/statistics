#!/bin/bash
# after the report-revision runs: joins the chunks of the long runs (merge_chunks.py) and the two parts of the
# two-ISMCTS run (merge_parts.py), rewrites RESULTS.md and copies this folder to groupA_code/experiments_v5
cd "$(dirname "$0")"
R=results
merge() {  # merge out.csv chunk.csv:offset ... (only when every chunk exists)
  out=$1; shift
  for p in "$@"; do [ -f "${p%%:*}" ] || return 0; done
  python3 merge_chunks.py "$out" "$@"
}
for L in r2_val_3p500 r2_val_3p900 r2_v3_3p500; do
  merge $R/$L.csv $R/${L}_c0.csv:0 $R/${L}_c1.csv:126 $R/${L}_c2.csv:252 $R/${L}_c3.csv:378
done
merge $R/r2_val_4p500.csv $R/r2_val_4p500_c0.csv:0 $R/r2_val_4p500_c1.csv:128 $R/r2_val_4p500_c2.csv:256 \
  $R/r2_val_4p500_c3.csv:384
[ -f $R/v5_field_3p100.csv ] && [ -f $R/r2_field_3p100b.csv ] && \
  python3 merge_parts.py $R/v5_field_3p100_all.csv $R/v5_field_3p100.csv $R/r2_field_3p100b.csv 300
./make_results.sh
bash sync_to_groupA_code.sh
