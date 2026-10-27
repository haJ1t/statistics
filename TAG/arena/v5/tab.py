#!/usr/bin/env python3
"""tab.py [label=]file.csv ... : win % with Wilson 95% CI and score margin (mean +- 1.96 SE) of each run."""
import csv, math, sys, os


def stats(f):
    w = []; m = []
    for r in csv.DictReader(open(f)):
        w.append(float(r["focusWin"])); m.append(float(r["focusScore"]) - float(r["bestOppScore"]))
    n = len(w); p = sum(w) / n; z = 1.96
    c = (p + z * z / (2 * n)) / (1 + z * z / n); h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    mm = sum(m) / n; sm = math.sqrt(sum((x - mm) ** 2 for x in m) / (n - 1) / n)
    return n, 100 * p, 100 * (c - h), 100 * (c + h), mm, 1.96 * sm


for a in sys.argv[1:]:
    lab, f = a.split("=", 1) if "=" in a else (os.path.basename(a)[:-4], a)
    if not os.path.exists(f):
        print(f"{lab:18s} missing"); continue
    n, p, lo, hi, mm, sm = stats(f)
    print(f"{lab:18s} n={n:4d} win {p:5.1f} [{lo:4.1f}, {hi:4.1f}]  margin {mm:+.2f} ± {sm:.2f}")
