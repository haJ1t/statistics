#!/usr/bin/env python3
"""Markdown table of arena runs, each compared (paired, same deals and seats) with a base run.

usage: summarise.py ["label="]base.csv "label=other.csv" ["label2=other2.csv" ...]
Columns: games, win rate with Wilson 95% CI, paired win difference to the base (95% CI, z),
score margin (focus score - best opponent score) and its paired difference to the base.
"""
import sys
from compare import load, wilson, mean_ci


def stats(runs):
    n = len(runs)
    w = sum(float(r["focusWin"]) for r in runs.values()) / n
    lo, hi = wilson(w, n)
    md, hd = mean_ci([float(r["focusScore"]) - float(r["bestOppScore"]) for r in runs.values()])
    return n, w, lo, hi, md, hd


def margin(r):
    return float(r["focusScore"]) - float(r["bestOppScore"])


def main():
    name, base_path = sys.argv[1].rsplit("=", 1) if "=" in sys.argv[1] else (None, sys.argv[1])
    base = load(base_path)
    print("| Variant | Games | Win % [95% CI] | Δ win vs base (paired) | Score margin | Δ margin vs base |")
    print("|---|---|---|---|---|---|")
    n, w, lo, hi, md, hd = stats(base)
    name = name or base_path.split("/")[-1].removesuffix(".csv")
    print(f"| {name} (base) | {n} | {100*w:.1f} [{100*lo:.1f}, {100*hi:.1f}] | – | {md:+.2f} ± {hd:.2f} | – |")
    for spec in sys.argv[2:]:
        label, path = spec.rsplit("=", 1) if "=" in spec else (spec.split("/")[-1].removesuffix(".csv"), spec)
        other = load(path)
        n, w, lo, hi, md, hd = stats(other)
        common = sorted(set(base) & set(other))
        mw, hw = mean_ci([float(other[g]["focusWin"]) - float(base[g]["focusWin"]) for g in common])
        ms, hs = mean_ci([margin(other[g]) - margin(base[g]) for g in common])
        z = mw / (hw / 1.96) if hw > 0 else float("inf")
        print(f"| {label} | {n} | {100*w:.1f} [{100*lo:.1f}, {100*hi:.1f}] | {100*mw:+.1f} ± {100*hw:.1f} (z = {z:+.2f}) "
              f"| {md:+.2f} ± {hd:.2f} | {ms:+.2f} ± {hs:.2f} |")


if __name__ == "__main__":
    main()
