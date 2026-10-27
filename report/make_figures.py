#!/usr/bin/env python3
"""Figures of the report (data copied from groupA_code/experiments_v3, _v4 and _v5 RESULTS.md)."""
import math
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch

plt.rcParams.update({"font.family": "Liberation Serif", "mathtext.fontset": "stix", "font.size": 7.5, "axes.linewidth": 0.6,
                     "xtick.major.width": 0.6, "ytick.major.width": 0.6, "savefig.dpi": 600})
COL = 3.39  # IEEE column width in inches (A4 template: 86 mm)


def wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return 100 * (c - h), 100 * (c + h)


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
        (xs[2], bot, "4  Roll out (B, R)", "\u03b5-greedy card values\nfor every player until\nthe round is scored"),
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
    fig.savefig("figures/fig1_iteration.png")
    plt.close(fig)


# ---------------------------------------------------------------- Fig. 2: win rate against the time budget
def fig_budget():
    budgets = [100, 500, 900]
    n = [500, 500, 250]
    runs = {  # development runs, 3 players, seed 1 (experiments_v4/RESULTS.md, "From v3 to v4")
        "v2": [24.8, 18.3, 18.8],
        "v3": [50.1, 40.7, 32.8],
        "v4": [55.7, 49.7, 47.6],
    }
    styles = {"v2": ("#9a9a9a", "s"), "v3": ("#d17a22", "^"), "v4": ("#1f5f9e", "o")}
    fig, ax = plt.subplots(figsize=(COL, 1.9))
    fig.subplots_adjust(left=0.12, right=0.985, bottom=0.215, top=0.97)
    xs = [0, 1, 2]
    for k, (name, ys) in enumerate(runs.items()):
        c, m = styles[name]
        lo = [y - wilson(y / 100, nn)[0] for y, nn in zip(ys, n)]
        hi = [wilson(y / 100, nn)[1] - y for y, nn in zip(ys, n)]
        off = (k - 1) * 0.06
        ax.errorbar([x + off for x in xs], ys, yerr=[lo, hi], color=c, marker=m, ms=3.6, lw=1.0, capsize=2, elinewidth=0.7,
                    label=name)
    ax.axhline(100 / 3, color="black", lw=0.6, ls=":")
    ax.text(2.28, 100 / 3 + 0.8, "chance", fontsize=6.5, ha="right", va="bottom")
    ax.set_xticks(xs, [f"{b} ms\n(n = {nn})" for b, nn in zip(budgets, n)])
    ax.set_xlim(-0.3, 2.3); ax.set_ylim(10, 68)
    ax.set_ylabel("win rate (%)")
    ax.grid(axis="y", lw=0.3, alpha=0.6)
    ax.legend(loc="upper center", fontsize=6.6, frameon=False, ncol=3, handlelength=1.8, borderaxespad=0.1, columnspacing=1.6)
    for s in ("top", "right"):
        ax.spines[s].set_visible(False)
    fig.savefig("figures/fig2_budget.png")
    plt.close(fig)


# ---------------------------------------------------------------- Fig. 3: paired effects (ablation and v5 additions)
def fig_effects():
    # label, d_win, ci_win, d_margin, ci_margin, group
    rows = [
        ("− H (TAG score heuristic)", -28.3, 5.8, -5.45, 0.71, 0),
        ("− S − I (= v3)", -10.2, 5.7, -1.75, 0.58, 0),
        ("− R (10-move rollouts)", -6.2, 5.9, -0.74, 0.61, 0),
        ("− B (random rollouts)", -4.3, 5.8, -0.98, 0.57, 0),
        ("− I (S with determinisation D)", -3.8, 5.7, -0.63, 0.55, 0),
        ("+ X3 (3-player card constants)", -1.3, 5.8, -0.57, 0.60, 1),
        ("+ N (denial, λ = 0.5)", -1.8, 5.8, -0.32, 0.59, 1),
        ("+ V2D (win-gap value, σ decay)", -0.6, 5.8, -0.60, 0.56, 1),
        ("+ W1 (leader weight w = 1)", 0.7, 5.8, -0.09, 0.58, 1),
        ("+ R3V2 (rollouts to game end)", -23.7, 5.8, -3.58, 0.65, 1),
    ]
    fig, (a1, a2) = plt.subplots(1, 2, figsize=(COL, 2.55), sharey=True, gridspec_kw={"width_ratios": [1.15, 1], "wspace": 0.08})
    fig.subplots_adjust(left=0.41, right=0.985, bottom=0.135, top=0.985)
    ys = [11, 10, 9, 8, 7, 4, 3, 2, 1, 0]
    for y, (lab, dw, cw, dm, cm, g) in zip(ys, rows):
        col = "#b23a3a" if g == 0 else "#1f5f9e"
        a1.errorbar(dw, y, xerr=cw, fmt="o", color=col, ms=2.8, capsize=1.6, elinewidth=0.7)
        a2.errorbar(dm, y, xerr=cm, fmt="o", color=col, ms=2.8, capsize=1.6, elinewidth=0.7)
    for a in (a1, a2):
        a.axvline(0, color="black", lw=0.6)
        a.grid(axis="x", lw=0.3, alpha=0.6)
        a.axhline(5.75, color="#777777", lw=0.5, ls="--")
        a.set_ylim(-0.7, 12.6)
        for s in ("top", "right"):
            a.spines[s].set_visible(False)
    group_y = [12.05, 5.05]
    a1.set_yticks(ys + group_y, [r[0] for r in rows] + ["ablation of the final agent (v4)", "added to the final agent (v5)"],
                  fontsize=6.4)
    for lab, col in zip(a1.get_yticklabels()[-2:], ("#b23a3a", "#1f5f9e")):
        lab.set_color(col); lab.set_fontstyle("italic"); lab.set_fontweight("bold")
    for tick in a1.yaxis.get_major_ticks()[-2:]:
        tick.tick1line.set_visible(False)
    for tick in a2.yaxis.get_major_ticks()[-2:]:
        tick.tick1line.set_visible(False)
    a1.set_xlabel("Δ win rate (points)")
    a2.set_xlabel("Δ score margin (points)")
    a1.set_xlim(-36, 8); a2.set_xlim(-6.6, 1.2)
    fig.savefig("figures/fig3_effects.png")
    plt.close(fig)


if __name__ == "__main__":
    fig_iteration(); fig_budget(); fig_effects()
    print("figures written")
