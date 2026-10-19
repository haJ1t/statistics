#!/usr/bin/env python3
"""Paired differences pooled over several seed sets (each pair of runs played the same deals).

usage: pooled.py "label=base1.csv:other1.csv,base2.csv:other2.csv" ...
For every label, the per-game differences (win and score margin) of all listed pairs are pooled;
mean, 95% CI (1.96 standard errors) and z are printed as a markdown table.
"""
import sys
from compare import load, mean_ci


def margin(r):
    return float(r["focusScore"]) - float(r["bestOppScore"])


def main():
    print("| Comparison | Paired games | Δ win (points) | z | Δ score margin | z |")
    print("|---|---|---|---|---|---|")
    for spec in sys.argv[1:]:
        label, pairs = spec.rsplit("=", 1)
        dw, dm = [], []
        for pair in pairs.split(","):
            b, o = (load(p) for p in pair.split(":"))
            for g in sorted(set(b) & set(o)):
                dw.append(float(o[g]["focusWin"]) - float(b[g]["focusWin"]))
                dm.append(margin(o[g]) - margin(b[g]))
        mw, hw = mean_ci(dw)
        mm, hm = mean_ci(dm)
        print(f"| {label} | {len(dw)} | {100*mw:+.1f} ± {100*hw:.1f} | {mw/(hw/1.96):+.2f} | {mm:+.2f} ± {hm:.2f} | {mm/(hm/1.96):+.2f} |")


if __name__ == "__main__":
    main()
