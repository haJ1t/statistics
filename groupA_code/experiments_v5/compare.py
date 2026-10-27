#!/usr/bin/env python3
"""Paired comparison of arena runs that used the same seeds (same deals, same seats).

usage: compare.py base.csv other.csv [other2.csv ...]
Prints, for every 'other' run: win rate with Wilson 95% CI, the paired difference to 'base'
(mean of per-game differences, 95% CI from the paired standard error) and the paired
difference in (focus score - best opponent score).
"""
import csv, math, sys


def load(path):
    with open(path) as f:
        rows = list(csv.DictReader(f))
    return {int(r["game"]): r for r in rows}


def wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return c - h, c + h


def mean_ci(xs, z=1.96):
    n = len(xs)
    m = sum(xs) / n
    var = sum((x - m) ** 2 for x in xs) / (n - 1) if n > 1 else 0.0
    return m, z * math.sqrt(var / n)


def describe(name, runs):
    n = len(runs)
    w = sum(float(r["focusWin"]) for r in runs.values()) / n
    lo, hi = wilson(w, n)
    d = [float(r["focusScore"]) - float(r["bestOppScore"]) for r in runs.values()]
    md, hd = mean_ci(d)
    print(f"{name:40s} n={n:4d} win={100*w:5.1f}% [{100*lo:5.1f},{100*hi:5.1f}]  diffBest={md:+6.2f}±{hd:4.2f}")


def main():
    base = load(sys.argv[1])
    describe(sys.argv[1].split("/")[-1], base)
    for path in sys.argv[2:]:
        other = load(path)
        common = sorted(set(base) & set(other))
        describe(path.split("/")[-1], other)
        dw = [float(other[g]["focusWin"]) - float(base[g]["focusWin"]) for g in common]
        ds = [(float(other[g]["focusScore"]) - float(other[g]["bestOppScore"]))
              - (float(base[g]["focusScore"]) - float(base[g]["bestOppScore"])) for g in common]
        mw, hw = mean_ci(dw)
        ms, hs = mean_ci(ds)
        z = mw / (hw / 1.96) if hw > 0 else float("inf")
        print(f"{'':40s} paired vs base ({len(common)} games): dWin={100*mw:+5.1f} ±{100*hw:4.1f} pts (z={z:+.2f})  dDiffBest={ms:+5.2f}±{hs:4.2f}")


if __name__ == "__main__":
    main()
