#!/usr/bin/env python3
"""Figures of the report, computed from the per-game CSV files in groupA_code/experiments_v4/results and
groupA_code/experiments_v5/results (development runs of Fig. 2: seed 1, TAG's own deals; held-out runs: seed block
100 000; paired effects of Fig. 3: seed 7)."""
import csv
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.lines import Line2D
from matplotlib.patches import FancyBboxPatch, Circle

HERE = os.path.dirname(os.path.abspath(__file__))
R4 = os.path.join(HERE, "..", "groupA_code", "experiments_v4", "results")
R5 = os.path.join(HERE, "..", "groupA_code", "experiments_v5", "results")
plt.rcParams.update({"font.family": "Liberation Serif", "mathtext.fontset": "stix", "font.size": 8,
                     "axes.linewidth": 0.6, "xtick.major.width": 0.6, "ytick.major.width": 0.6, "savefig.dpi": 600})
COL = 3.39  # IEEE column width in inches (A4 template: 86 mm)
BLUE, ORANGE, GREY, RED = "#1f5f9e", "#d17a22", "#8a8a8a", "#b23a3a"


def wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return 100 * (c - h), 100 * (c + h)


def load(path):
    with open(path) as f:
        return {int(r["game"]): r for r in csv.DictReader(f)}


def win(path):
    """(win %, Wilson low, high) of a run, or None if the file does not exist yet"""
    if not os.path.exists(path):
        return None
    rows = load(path)
    p = sum(float(r["focusWin"]) for r in rows.values()) / len(rows)
    return (100 * p, *wilson(p, len(rows)))


def mean_se(xs):
    n = len(xs)
    m = sum(xs) / n
    return m, math.sqrt(sum((x - m) ** 2 for x in xs) / (n - 1) / n)


def holm(zs, alpha=0.05):
    """which two-sided tests with these z values are significant after a Holm correction"""
    ps = [math.erfc(abs(z) / math.sqrt(2)) for z in zs]
    sig = [False] * len(ps)
    for rank, i in enumerate(sorted(range(len(ps)), key=lambda j: ps[j])):
        if ps[i] > alpha / (len(ps) - rank):
            break
        sig[i] = True
    return sig


def paired(base, other):
    """(d_win in pp, 1.96 SE, d_margin, 1.96 SE) of run other against run base on their common games"""
    b, o = load(base), load(other)
    g = sorted(set(b) & set(o))
    mg = lambda r: float(r["focusScore"]) - float(r["bestOppScore"])
    w, sw = mean_se([float(o[k]["focusWin"]) - float(b[k]["focusWin"]) for k in g])
    d, sd = mean_se([mg(o[k]) - mg(b[k]) for k in g])
    return 100 * w, 196 * sw, d, 1.96 * sd


# ---------------------------------------------------------------- Fig. 1: sequential tree vs decoupled turn node
def node(ax, x, y, text, r=3.3, fc="#eef3fa", ec=BLUE):
    ax.add_patch(Circle((x, y), r, fc=fc, ec=ec, lw=0.7))
    ax.text(x, y, text, ha="center", va="center", fontsize=7.5)


def edge(ax, x0, y0, x1, y1, label=None, color=BLUE, side=-1):
    ax.plot([x0, x1], [y0, y1], color=color, lw=0.7, zorder=0)
    if label:
        ax.text((x0 + x1) / 2 + side * 1.6, (y0 + y1) / 2, label, fontsize=7, ha="center", va="center",
                style="italic", color="#333333")


def fig_turnnode():
    fig = plt.figure(figsize=(COL, 1.58))
    ax = fig.add_axes([0, 0, 1, 1])
    ax.set_xlim(0, 100); ax.set_ylim(0, 57.5); ax.axis("off")
    # left: v3, players of a turn in sequence
    ax.text(18, 54.5, "v3: one level per player", ha="center", va="center", fontsize=7.5, fontweight="bold",
            color="#1d3d5c")
    node(ax, 18, 45, "P1")
    for x2, lab in ((8, "a"), (28, "b")):
        edge(ax, 18, 45, x2, 31.5, lab, side=-1 if x2 < 18 else 1)
        node(ax, x2, 28.5, "P2")
    for x3, lab in ((3, "a"), (13, "c")):
        edge(ax, 8, 28.5, x3, 15.5, lab, side=-1 if x3 < 8 else 1)
        node(ax, x3, 12.5, "P3", r=2.6)
    edge(ax, 28, 28.5, 28, 15.5)
    node(ax, 28, 12.5, "P3", r=2.6)
    ax.text(18, 3.6, "P2 chooses knowing P1's card:\nimpossible in the real game", ha="center", va="center",
            fontsize=7, color=RED, linespacing=1.15)
    ax.plot([37.5, 37.5], [2, 56], color="#bbbbbb", lw=0.6, ls=":")
    # right: final agent, one decoupled node per turn
    ax.text(69, 54.5, "final agent: one node per turn", ha="center", va="center", fontsize=7.5, fontweight="bold",
            color="#1d3d5c")
    ax.add_patch(FancyBboxPatch((40.5, 27), 57, 23.5, boxstyle="round,pad=0.4,rounding_size=1.5", fc="#eef3fa",
                                ec=BLUE, lw=0.7))
    for k, x in enumerate((50, 69, 88)):
        ax.text(x, 47.2, f"P{k + 1}", ha="center", va="center", fontsize=7.5, fontweight="bold")
        ax.text(x, 37.5, "card $a$:\n$Q, n, N$", ha="center", va="center", fontsize=7, linespacing=1.15)
        ax.text(x, 30, "UCB pick $a_{%d}$" % (k + 1), ha="center", va="center", fontsize=7)
    ax.annotate("", xy=(69, 13.5), xytext=(69, 26.5),
                arrowprops=dict(arrowstyle="-|>", lw=0.8, color=BLUE, mutation_scale=7))
    ax.text(70.5, 20, "joint pick $(a_1, a_2, a_3)$", ha="left", va="center", fontsize=7)
    ax.add_patch(FancyBboxPatch((60, 6.5), 18, 6.5, boxstyle="round,pad=0.4,rounding_size=1.2", fc="#eef3fa",
                                ec=BLUE, lw=0.7))
    ax.text(69, 9.75, "next turn", ha="center", va="center", fontsize=7)
    ax.text(69, 1.6, "hidden cards re-sampled in every iteration", ha="center", va="center", fontsize=7,
            color="#333333")
    fig.savefig(os.path.join(HERE, "figures", "fig1_turnnode.png"))
    plt.close(fig)


# ---------------------------------------------------------------- Fig. 2: win rate against the time budget
def fig_budget():
    budgets = [100, 500, 900]
    dev = {"v2": ["s1_ref", "f5_ref", "f9_ref"], "v3": ["abl_T4", "f5_finalT4", "f9_finalT4"],
           "final (v4)": ["d1_I", "f5_I", "f9_I"]}
    styles = {"v2": (GREY, "s"), "v3": (ORANGE, "^"), "final (v4)": (BLUE, "o")}
    held = {"final (v4)": ["r2_val_3p100", "r2_val_3p500", "r2_val_3p900"],
            "v3": ["r2_v3_3p100", "r2_v3_3p500", None]}
    fig, ax = plt.subplots(figsize=(COL, 1.6))
    fig.subplots_adjust(left=0.115, right=0.985, bottom=0.22, top=0.8)
    xs = [0, 1, 2]
    for k, (name, labels) in enumerate(dev.items()):
        c, m = styles[name]
        pts = [win(os.path.join(R4, x + ".csv")) for x in labels]
        off = (k - 1) * 0.05 - 0.07
        ax.errorbar([x + off for x in xs], [q[0] for q in pts], yerr=[[q[0] - q[1] for q in pts],
                    [q[2] - q[0] for q in pts]], color=c, marker=m, ms=3.4, lw=1.0, capsize=1.8, elinewidth=0.7,
                    label=name)
    for name, labels in held.items():
        c, m = styles[name]
        hx, hy, lo, hi = [], [], [], []
        for x, lab in zip(xs, labels):
            if lab is None:
                continue
            cand = lab if isinstance(lab, list) else [lab]
            q = next((win(os.path.join(R5, f + ".csv")) for f in cand if os.path.exists(os.path.join(R5, f + ".csv"))),
                     None)
            if q:
                hx.append(x + 0.13); hy.append(q[0]); lo.append(q[0] - q[1]); hi.append(q[2] - q[0])
        if hx:
            ax.errorbar(hx, hy, yerr=[lo, hi], fmt=m, mfc="white", mec=c, color=c, ms=3.4, capsize=1.8,
                        elinewidth=0.7, label=f"{name.split(' ')[0]}, held-out")
    ax.axhline(100 / 3, color="black", lw=0.6, ls=":")
    ax.text(2.33, 100 / 3 + 0.8, "chance", fontsize=7, ha="right", va="bottom")
    ax.set_xticks(xs, [f"{b} ms" for b in budgets])
    ax.set_xlabel("CPU time per decision", fontsize=7.5, labelpad=1.5)
    ax.set_xlim(-0.3, 2.35); ax.set_ylim(10, 70)
    ax.set_ylabel("win rate (%)", fontsize=7.5)
    ax.tick_params(labelsize=7.5)
    ax.grid(axis="y", lw=0.3, alpha=0.6)
    # legend filled column by column: development runs in the first row, held-out markers below their agent
    h, lab = ax.get_legend_handles_labels()
    blank = Line2D([], [], alpha=0)
    want = ("v2", None, "v3", "v3, held-out", "final (v4)", "final, held-out")
    ax.legend([h[lab.index(x)] if x in lab else blank for x in want], [x if x in lab else "" for x in want],
              loc="lower center", bbox_to_anchor=(0.5, 1.0), fontsize=7, frameon=False, ncol=3, handlelength=1.5,
              borderaxespad=0.1, columnspacing=1.0, labelspacing=0.25)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    fig.savefig(os.path.join(HERE, "figures", "fig2_budget.png"))
    plt.close(fig)


# ---------------------------------------------------------------- Fig. 3: paired effects (ablation and last study)
def fig_effects():
    base4, base5 = os.path.join(R4, "V_3p100.csv"), os.path.join(R5, "v5_base3p100.csv")
    spec = [  # label, base, run, group
        ("− heuristic H (own score / 50)", base4, os.path.join(R4, "A_noH.csv"), 0),
        ("− decoupled tree S, info sets I", base4, os.path.join(R4, "A_noSI.csv"), 0),
        ("− round-end rollouts R (10 moves)", base4, os.path.join(R4, "A_noR.csv"), 0),
        ("− informed rollouts B (random)", base4, os.path.join(R4, "A_noB.csv"), 0),
        ("− info sets I (determinisation)", base4, os.path.join(R4, "A_noI.csv"), 0),
        ("H → TAG lead heuristic", base5, os.path.join(R5, "r2_lead_3p100.csv"), 1),
        ("+ 3-player constants X3", base5, os.path.join(R5, "v5_X3_3p100.csv"), 1),
        ("+ denial in rollouts", base5, os.path.join(R5, "v5_N05_3p100.csv"), 1),
        ("+ win-gap value, σ decay", base5, os.path.join(R5, "v5_V2D_3p100.csv"), 1),
        ("+ leader weight w = 1", base5, os.path.join(R5, "v5_W1_3p100.csv"), 1),
        ("+ game-end rollouts, win gap", base5, os.path.join(R5, "v5_R3V2_3p100.csv"), 1),
    ]
    rows = [(lab, *paired(b, r), g) for lab, b, r, g in spec if os.path.exists(r)]
    n0 = sum(1 for r in rows if r[5] == 0)
    n1 = len(rows) - n0
    ys0 = [n1 + 2.5 + i for i in range(n0)][::-1]
    ys1 = list(range(n1))[::-1]
    ys = ys0 + ys1
    fig, (a1, a2) = plt.subplots(1, 2, figsize=(COL, 2.1), sharey=True,
                                 gridspec_kw={"width_ratios": [1.15, 1], "wspace": 0.08})
    fig.subplots_adjust(left=0.45, right=0.975, bottom=0.17, top=0.985)
    sig = {}   # filled marker: significant after a Holm correction within its group, for each measure
    for g in (0, 1):
        idx = [i for i, r in enumerate(rows) if r[5] == g]
        for m, (d, c) in enumerate(((1, 2), (3, 4))):
            for i, x in zip(idx, holm([rows[i][d] / (rows[i][c] / 1.96) for i in idx])):
                sig[i, m] = x
    for i, (y, (lab, dw, cw, dm, cm, g)) in enumerate(zip(ys, rows)):
        col = RED if g == 0 else BLUE
        for m, (a, d, c) in enumerate(((a1, dw, cw), (a2, dm, cm))):
            a.errorbar(d, y, xerr=c, fmt="o", color=col, mfc=col if sig[i, m] else "white", mec=col, ms=2.8,
                       capsize=1.6, elinewidth=0.7)
    sep = n1 + 0.75
    top_label, bot_label = max(ys0) + 1.05, n1 - 0.05 + 0.75
    for a in (a1, a2):
        a.axvline(0, color="black", lw=0.6)
        a.grid(axis="x", lw=0.3, alpha=0.6)
        a.axhline(sep, color="#777777", lw=0.5, ls="--")
        a.set_ylim(-0.7, top_label + 0.6)
        a.tick_params(labelsize=7)
        for s in ("top", "right"):
            a.spines[s].set_visible(False)
    a1.set_yticks(ys + [top_label, bot_label],
                  [r[0] for r in rows] + ["removed from the final agent", "vs. a re-run of the final agent"],
                  fontsize=7)
    for lab, col in zip(a1.get_yticklabels()[-2:], (RED, BLUE)):
        lab.set_color(col); lab.set_fontstyle("italic"); lab.set_fontweight("bold")
    for a in (a1, a2):
        for tick in a.yaxis.get_major_ticks()[-2:]:
            tick.tick1line.set_visible(False)
    a1.set_xlabel("Δ win rate (pp)", fontsize=7.5)
    a2.set_xlabel("Δ margin (points)", fontsize=7.5)
    a1.set_xlim(-36, 8); a2.set_xlim(-6.6, 1.2)
    fig.savefig(os.path.join(HERE, "figures", "fig3_effects.png"))
    plt.close(fig)


if __name__ == "__main__":
    os.makedirs(os.path.join(HERE, "figures"), exist_ok=True)
    fig_turnnode(); fig_budget(); fig_effects()
    print("figures written")
