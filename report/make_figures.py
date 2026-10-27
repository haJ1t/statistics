#!/usr/bin/env python3
"""Figures of the report. Paired effects and held-out results are computed from the per-game CSV files in
groupA_code/experiments_v4/results and groupA_code/experiments_v5/results; the development win rates of Fig. 2
are those of experiments_v4/RESULTS.md ("From v3 to v4"; runs before the canonical deal)."""
import csv
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

HERE = os.path.dirname(os.path.abspath(__file__))
R4 = os.path.join(HERE, "..", "groupA_code", "experiments_v4", "results")
R5 = os.path.join(HERE, "..", "groupA_code", "experiments_v5", "results")
plt.rcParams.update({"font.family": "Liberation Serif", "mathtext.fontset": "stix", "font.size": 7.5,
                     "axes.linewidth": 0.6, "xtick.major.width": 0.6, "ytick.major.width": 0.6, "savefig.dpi": 600})
COL = 3.39  # IEEE column width in inches (A4 template: 86 mm)


def wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return 100 * (c - h), 100 * (c + h)


def load(path):
    with open(path) as f:
        return {int(r["game"]): r for r in csv.DictReader(f)}


def mean_se(xs):
    n = len(xs)
    m = sum(xs) / n
    return m, math.sqrt(sum((x - m) ** 2 for x in xs) / (n - 1) / n)


def paired(base, other):
    """(d_win in pp, 1.96 SE, d_margin, 1.96 SE) of run other against run base on their common games"""
    b, o = load(base), load(other)
    g = sorted(set(b) & set(o))
    mg = lambda r: float(r["focusScore"]) - float(r["bestOppScore"])
    w, sw = mean_se([float(o[k]["focusWin"]) - float(b[k]["focusWin"]) for k in g])
    d, sd = mean_se([mg(o[k]) - mg(b[k]) for k in g])
    return 100 * w, 196 * sw, d, 1.96 * sd


# ---------------------------------------------------------------- Fig. 1: one iteration of the final agent
def fig_iteration():
    fig = plt.figure(figsize=(COL, 1.66))
    ax = fig.add_axes([0.005, 0.01, 0.99, 0.98])
    ax.set_xlim(0, 100); ax.set_ylim(0, 50); ax.axis("off")
    w, h = 29.5, 21
    xs = [0.8, 35.25, 69.7]
    top, bot = 27, 1.5
    boxes = [  # x, y, title, body
        (xs[0], top, "1  Sample (I)", "re-deal every card\nwe cannot see"),
        (xs[1], top, "2  Select (S, P, M)", "each player: UCB1 on\nits own cards; joint\npick leads to a child"),
        (xs[2], top, "3  Expand", "add one node for the\nfirst new joint pick"),
        (xs[2], bot, "4  Roll out (B, R)", "ε-greedy card values\nfor every player until\nthe round is scored"),
        (xs[1], bot, "5  Evaluate (H)", "$H_i=\\tanh(\\Delta_i/\\sigma)$\nfor every player $i$"),
        (xs[0], bot, "6  Back up (M)", "each player's value\ngoes to the card it\nchose at each node"),
    ]
    for x, y, t, b in boxes:
        ax.add_patch(FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.3,rounding_size=1.5",
                                    fc="#eef3fa", ec="#2f5d8a", lw=0.7))
        ax.text(x + w / 2, y + h - 2.6, t, ha="center", va="top", fontsize=6.9, fontweight="bold", color="#1d3d5c")
        ax.text(x + w / 2, y + h - 8.2, b, ha="center", va="top", fontsize=6.1, linespacing=1.18)
    arrow = dict(arrowstyle="-|>", lw=0.8, color="#2f5d8a", mutation_scale=7)
    my, ly = top + h / 2, bot + h / 2
    pairs = [((xs[0] + w + 0.9, my), (xs[1] - 0.9, my)), ((xs[1] + w + 0.9, my), (xs[2] - 0.9, my)),
             ((xs[2] + w / 2, top - 0.9), (xs[2] + w / 2, bot + h + 0.9)),
             ((xs[2] - 0.9, ly), (xs[1] + w + 0.9, ly)), ((xs[1] - 0.9, ly), (xs[0] + w + 0.9, ly)),
             ((xs[0] + w / 2, bot + h + 0.9), (xs[0] + w / 2, top - 0.9))]
    for (x0, y0), (x1, y1) in pairs:
        ax.annotate("", xy=(x1, y1), xytext=(x0, y0), arrowprops=arrow)
    fig.savefig(os.path.join(HERE, "figures", "fig1_iteration.png"))
    plt.close(fig)


# ---------------------------------------------------------------- Fig. 2: win rate against the time budget
def fig_budget():
    budgets = [100, 500, 900]
    n = [500, 500, 250]
    runs = {"v2": [24.8, 18.3, 18.8], "v3": [50.1, 40.7, 32.8], "v4": [55.7, 49.7, 47.6]}  # development, seed 1
    styles = {"v2": ("#9a9a9a", "s"), "v3": ("#d17a22", "^"), "v4": ("#1f5f9e", "o")}
    fig, ax = plt.subplots(figsize=(COL, 1.85))
    fig.subplots_adjust(left=0.12, right=0.985, bottom=0.273, top=0.97)
    xs = [0, 1, 2]
    for k, (name, ys) in enumerate(runs.items()):
        c, m = styles[name]
        lo = [y - wilson(y / 100, nn)[0] for y, nn in zip(ys, n)]
        hi = [wilson(y / 100, nn)[1] - y for y, nn in zip(ys, n)]
        off = (k - 1) * 0.06
        ax.errorbar([x + off for x in xs], ys, yerr=[lo, hi], color=c, marker=m, ms=3.6, lw=1.0, capsize=2,
                    elinewidth=0.7, label=name)
    # held-out validation of the final agent (seed block 100 000)
    hx, hy, hlo, hhi = [], [], [], []
    for x, b in zip(xs, budgets):
        f = os.path.join(R5, f"r2_val_3p{b}.csv")
        if os.path.exists(f):
            rows = load(f)
            p = sum(float(r["focusWin"]) for r in rows.values()) / len(rows)
            lo, hi = wilson(p, len(rows))
            hx.append(x + 0.14); hy.append(100 * p); hlo.append(100 * p - lo); hhi.append(hi - 100 * p)
    if hx:
        ax.errorbar(hx, hy, yerr=[hlo, hhi], fmt="o", mfc="white", mec="#1f5f9e", color="#1f5f9e", ms=3.6,
                    capsize=2, elinewidth=0.7, label="final, held-out")
    ax.axhline(100 / 3, color="black", lw=0.6, ls=":")
    ax.text(2.33, 100 / 3 + 0.8, "chance", fontsize=6.5, ha="right", va="bottom")
    ax.set_xticks(xs, [f"{b} ms\n(n = {nn})" for b, nn in zip(budgets, n)])
    ax.set_xlabel("CPU time per decision (development runs: n games)", fontsize=6.8)
    ax.set_xlim(-0.3, 2.35); ax.set_ylim(10, 70)
    ax.set_ylabel("win rate (%)")
    ax.grid(axis="y", lw=0.3, alpha=0.6)
    ax.legend(loc="upper center", fontsize=6.3, frameon=False, ncol=4, handlelength=1.6, borderaxespad=0.1,
              columnspacing=1.1)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    fig.savefig(os.path.join(HERE, "figures", "fig2_budget.png"))
    plt.close(fig)


# ---------------------------------------------------------------- Fig. 3: paired effects (ablation and last study)
def fig_effects():
    base4, base5 = os.path.join(R4, "V_3p100.csv"), os.path.join(R5, "v5_base3p100.csv")
    spec = [  # label, base, run, group
        ("− heuristic H (own score / 50)", base4, os.path.join(R4, "A_noH.csv"), 0),
        ("− decoupled tree S and info sets I", base4, os.path.join(R4, "A_noSI.csv"), 0),
        ("− round-end rollouts R", base4, os.path.join(R4, "A_noR.csv"), 0),
        ("− informed rollouts B", base4, os.path.join(R4, "A_noB.csv"), 0),
        ("− info sets I (determinisation D)", base4, os.path.join(R4, "A_noI.csv"), 0),
        ("H → TAG lead heuristic (K 7.5)", base5, os.path.join(R5, "r2_lead_3p100.csv"), 1),
        ("+ 3-player card constants X3", base5, os.path.join(R5, "v5_X3_3p100.csv"), 1),
        ("+ denial in the rollouts (λ 0.5)", base5, os.path.join(R5, "v5_N05_3p100.csv"), 1),
        ("+ win-gap value, σ decay", base5, os.path.join(R5, "v5_V2D_3p100.csv"), 1),
        ("+ leader weight w = 1", base5, os.path.join(R5, "v5_W1_3p100.csv"), 1),
        ("+ rollouts to game end, win-gap value", base5, os.path.join(R5, "v5_R3V2_3p100.csv"), 1),
    ]
    rows = [(lab, *paired(b, r), g) for lab, b, r, g in spec if os.path.exists(r)]
    n0 = sum(1 for r in rows if r[5] == 0)
    n1 = len(rows) - n0
    ys0 = [n1 + 2.5 + i for i in range(n0)][::-1]
    ys1 = list(range(n1))[::-1]
    ys = ys0 + ys1
    fig, (a1, a2) = plt.subplots(1, 2, figsize=(COL, 2.55), sharey=True,
                                 gridspec_kw={"width_ratios": [1.15, 1], "wspace": 0.08})
    fig.subplots_adjust(left=0.43, right=0.985, bottom=0.135, top=0.985)
    for y, (lab, dw, cw, dm, cm, g) in zip(ys, rows):
        col = "#b23a3a" if g == 0 else "#1f5f9e"
        a1.errorbar(dw, y, xerr=cw, fmt="o", color=col, ms=2.8, capsize=1.6, elinewidth=0.7)
        a2.errorbar(dm, y, xerr=cm, fmt="o", color=col, ms=2.8, capsize=1.6, elinewidth=0.7)
    sep = n1 + 0.75
    top_label, bot_label = max(ys0) + 1.05, n1 - 0.05 + 0.75
    for a in (a1, a2):
        a.axvline(0, color="black", lw=0.6)
        a.grid(axis="x", lw=0.3, alpha=0.6)
        a.axhline(sep, color="#777777", lw=0.5, ls="--")
        a.set_ylim(-0.7, top_label + 0.6)
        for s in ("top", "right"):
            a.spines[s].set_visible(False)
    a1.set_yticks(ys + [top_label, bot_label],
                  [r[0] for r in rows] + ["removed from the final agent", "vs. a re-run of the final agent"], fontsize=6.3)
    for lab, col in zip(a1.get_yticklabels()[-2:], ("#b23a3a", "#1f5f9e")):
        lab.set_color(col); lab.set_fontstyle("italic"); lab.set_fontweight("bold")
    for a in (a1, a2):
        for tick in a.yaxis.get_major_ticks()[-2:]:
            tick.tick1line.set_visible(False)
    a1.set_xlabel("Δ win rate (pp)")
    a2.set_xlabel("Δ margin (points)")
    a1.set_xlim(-36, 8); a2.set_xlim(-6.6, 1.2)
    fig.savefig(os.path.join(HERE, "figures", "fig3_effects.png"))
    plt.close(fig)


if __name__ == "__main__":
    os.makedirs(os.path.join(HERE, "figures"), exist_ok=True)
    fig_iteration(); fig_budget(); fig_effects()
    print("figures written")
