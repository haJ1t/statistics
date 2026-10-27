#!/usr/bin/env python3
"""v5 tables: table.py base.csv "label=run.csv" ...  -> one markdown row per run: games, win % [Wilson 95 % CI],
score margin (focus score - best opponent score, mean +- 1.96 SE) and the paired differences to the base run
(same seed = same deals and seats): win points and score margin, +- 1.96 SE of the per-game differences, z."""
import csv, math, sys, os


def load(path):
    return {int(r["game"]): r for r in csv.DictReader(open(path))}


def wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return c - h, c + h


def mean_se(xs):
    n = len(xs); m = sum(xs) / n
    return m, math.sqrt(sum((x - m) ** 2 for x in xs) / (n - 1) / n) if n > 1 else 0.0


def margin(r):
    return float(r["focusScore"]) - float(r["bestOppScore"])


args = sys.argv[1:]
base = load(args[0].split("=", 1)[-1])
print("| Agent | Games | Win % [95 % CI] | Score margin | Δ win vs base (paired) | Δ margin (paired) |")
print("|---|---|---|---|---|---|")
for a in args:
    lab, f = a.split("=", 1) if "=" in a else (os.path.basename(a)[:-4], a)
    if not os.path.exists(f):
        print(f"| {lab} | missing |"); continue
    run = load(f); n = len(run)
    p = sum(float(r["focusWin"]) for r in run.values()) / n
    lo, hi = wilson(p, n)
    mm, sm = mean_se([margin(r) for r in run.values()])
    common = sorted(set(base) & set(run))
    if run is base or f == args[0].split("=", 1)[-1]:
        dw = ds = "base"
    else:
        w, sw = mean_se([float(run[g]["focusWin"]) - float(base[g]["focusWin"]) for g in common])
        d, sd = mean_se([margin(run[g]) - margin(base[g]) for g in common])
        dw = f"{100*w:+.1f} ± {196*sw:.1f} (z = {w/sw if sw else 0:+.1f})"
        ds = f"{d:+.2f} ± {1.96*sd:.2f} (z = {d/sd if sd else 0:+.1f})"
    print(f"| {lab} | {n} | {100*p:.1f} [{100*lo:.1f}, {100*hi:.1f}] | {mm:+.2f} ± {1.96*sm:.2f} | {dw} | {ds} |")
