#!/bin/bash
# after the report-revision runs: joins the two-part runs (merge_parts.py), rewrites RESULTS.md and copies this
# folder to groupA_code/experiments_v5 (run from anywhere)
cd "$(dirname "$0")"
R=results
[ -f $R/r2_val_3p900.csv ] && [ -f $R/r2_val_3p900b.csv ] && \
  python3 merge_parts.py $R/r2_val_3p900_all.csv $R/r2_val_3p900.csv $R/r2_val_3p900b.csv 252
[ -f $R/v5_field_3p100.csv ] && [ -f $R/r2_field_3p100b.csv ] && \
  python3 merge_parts.py $R/v5_field_3p100_all.csv $R/v5_field_3p100.csv $R/r2_field_3p100b.csv 300
./make_results.sh
bash sync_to_groupA_code.sh
