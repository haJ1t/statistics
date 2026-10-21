#!/usr/bin/env python3
"""2^4 factorial analysis of four changes on top of HBDP (100 ms, seed 1, 500 paired games per cell).

Factors: R = rollouts to the end of the round, M = max^n backups, K = exploration constant 0.5 (else 1.41),
T = heuristic completion horizon 8 (else 4). Every cell plays the same 500 deals, so for every game g the
effect contrast  c_g = mean(y | factor on) - mean(y | factor off)  over the 16 cells is computed and the effect
is the mean of c_g over the games, with a 95% CI from the standard error over games (paired analysis).
usage: factorial.py   (run in arena/results)
"""
import itertools, math
from compare import load

CELLS = {  # (R, M, K, T) -> CSV file
    (0, 0, 0, 0): "s2_base.csv",   (0, 0, 0, 1): "d_T8.csv",
    (0, 0, 1, 0): "s2_K0.5.csv",   (0, 0, 1, 1): "d_KT8.csv",
    (0, 1, 0, 0): "s2_M.csv",      (0, 1, 0, 1): "d_MT8.csv",
    (0, 1, 1, 0): "d_MK.csv",      (0, 1, 1, 1): "abl_noR.csv",
    (1, 0, 0, 0): "s2_R.csv",      (1, 0, 0, 1): "s3_R_T8.csv",
    (1, 0, 1, 0): "s2_R_K0.5.csv", (1, 0, 1, 1): "s4_K0.5_T8.csv",
    (1, 1, 0, 0): "s2_RM.csv",     (1, 1, 0, 1): "abl_K1.41.csv",
    (1, 1, 1, 0): "abl_T4.csv",    (1, 1, 1, 1): "s4_K0.5_T8_M.csv",
}
NAMES = "RMKT"


def main():
    data = {cell: load(f) for cell, f in CELLS.items()}
    games = sorted(set.intersection(*(set(d) for d in data.values())))
    win = {c: [float(data[c][g]["focusWin"]) for g in games] for c in CELLS}
    mar = {c: [float(data[c][g]["focusScore"]) - float(data[c][g]["bestOppScore"]) for g in games] for c in CELLS}

    print(f"Cells (win % over {len(games)} games):")
    print("| R | M | K 0.5 | T 8 | Win % | Score margin |")
    print("|---|---|---|---|---|---|")
    for c in sorted(CELLS):
        print(f"| {'✓' if c[0] else ''} | {'✓' if c[1] else ''} | {'✓' if c[2] else ''} | {'✓' if c[3] else ''} "
              f"| {100*sum(win[c])/len(games):.1f} | {sum(mar[c])/len(games):+.2f} |")

    def effect(y, idx):
        # per game: average of y over cells with sign = product of the factor levels (+1 on / -1 off), times 2/16
        vals = []
        for gi in range(len(games)):
            s = 0.0
            for c in CELLS:
                sign = 1
                for i in idx:
                    sign *= 1 if c[i] else -1
                s += sign * y[c][gi]
            vals.append(s / 8.0)   # = mean(on) - mean(off) for a main effect
        n = len(vals)
        m = sum(vals) / n
        sd = math.sqrt(sum((v - m) ** 2 for v in vals) / (n - 1))
        return m, 1.96 * sd / math.sqrt(n), m / (sd / math.sqrt(n))

    print("\nEffects (main effect = mean over cells with the factor on minus off; interactions in the same units):")
    print("| Effect | Δ win (points) | z | Δ score margin | z |")
    print("|---|---|---|---|---|")
    for k in (1, 2):
        for idx in itertools.combinations(range(4), k):
            mw, hw, zw = effect(win, idx)
            mm, hm, zm = effect(mar, idx)
            name = " × ".join(NAMES[i] for i in idx)
            print(f"| {name} | {100*mw:+.1f} ± {100*hw:.1f} | {zw:+.2f} | {mm:+.2f} ± {hm:.2f} | {zm:+.2f} |")


if __name__ == "__main__":
    main()
