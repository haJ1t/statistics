#!/usr/bin/env python3
"""Markdown tables of the v4 study from arena/results (run from arena/results): validation, development, ablation,
4-player experiments. Missing runs are skipped."""
import csv, math, os, re, glob


# Runs started before 01:37 on 19 Oct, when the arena did not yet deal canonically (Arena.canonicalDeal): their
# deals differ from those of later runs with the same seed, so a "paired" difference with them is an ordinary
# two-sample comparison (the CI is still valid, just without the benefit of pairing). Marked with a dagger.
OLD = {'abl_T4', 'd1_I', 'd1_I_K0.8', 'd1_P3', 'd1_S', 'd1_own', 'f5_I', 'f5_K1', 'f5_S', 'f5_finalT4', 'f5_noB',
       'f5_ref', 'f9_I', 'f9_finalT4', 'f9_ref', 'q4_I', 'q4_Ith8', 'q4_P3', 'q4_S', 'q4_X0', 'q4_X0V3', 'q4_final',
       'q4_own', 'q4_ref', 'q5_X0', 'q5_final', 'q5_ref', 's1_ref', 'val_q5'}


def load(path):
    with open(path) as f:
        return {int(r["game"]): r for r in csv.DictReader(f)}


def wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return c - h, c + h


def mean_ci(xs, z=1.96):
    n = len(xs)
    m = sum(xs) / n
    var = sum((x - m) ** 2 for x in xs) / (n - 1) if n > 1 else 0.0
    return m, z * math.sqrt(var / n)


def margin(r):
    return float(r["focusScore"]) - float(r["bestOppScore"])


def summary(label):
    runs = load(label + ".csv")
    n = len(runs)
    w = sum(float(r["focusWin"]) for r in runs.values()) / n
    lo, hi = wilson(w, n)
    m, hm = mean_ci([margin(r) for r in runs.values()])
    return n, 100 * w, 100 * lo, 100 * hi, m, hm


def paired(base, other):
    b, o = load(base + ".csv"), load(other + ".csv")
    common = sorted(set(b) & set(o))
    dw = [float(o[g]["focusWin"]) - float(b[g]["focusWin"]) for g in common]
    dm = [margin(o[g]) - margin(b[g]) for g in common]
    mw, hw = mean_ci(dw)
    mm, hm = mean_ci(dm)
    z = mw / (hw / 1.96) if hw > 0 else 0.0
    return 100 * mw, 100 * hw, z, mm, hm


def seat_wins(label):
    """per opponent file: win % over its seats, from the summary block after the RESULT line in the logs"""
    out = {}
    for log in glob.glob("*.log"):
        lines = open(log, errors="ignore").read().splitlines()
        for i, l in enumerate(lines):
            if l.startswith(f"RESULT {label} "):
                for x in lines[i + 1:i + 12]:
                    m = re.match(r"\s+(\S+\.json)\s+win ([\d.]+)% over (\d+) seats", x)
                    if m and "[focus]" not in x:
                        out[m.group(1)] = float(m.group(2))
                    if x.startswith("RESULT") or x.startswith("BATCH"):
                        break
    return out


def exists(label):
    return os.path.exists(label + ".csv")


def fmt_sum(label):
    n, w, lo, hi, m, hm = summary(label)
    return n, f"{w:.1f} [{lo:.1f}, {hi:.1f}]", f"{m:+.2f} ± {hm:.2f}"


def table_validation(rows):
    print("| Setting | Games | Win % [95 % CI] | Score margin | TAG ISMCTS seats | v2 seats |")
    print("|---|---|---|---|---|---|")
    for setting, label in rows:
        if not exists(label):
            continue
        n, w, m = fmt_sum(label)
        s = seat_wins(label)
        print(f"| {setting} | {n} | **{w}** | {m} | {s.get('tagmcts.json', float('nan')):.1f} % | {s.get('ref.json', float('nan')):.1f} % |")
    print()


def table_opponents(rows):
    print("| Opponents | Games | Our win % [95 % CI] | Score margin | Opponent seats: win % |")
    print("|---|---|---|---|---|")
    for setting, label in rows:
        if not exists(label):
            continue
        n, w, m = fmt_sum(label)
        s = seat_wins(label)
        opp = ", ".join(f"{k.replace('.json', '').replace('fw_', '')} {v:.1f} %" for k, v in s.items())
        print(f"| {setting} | {n} | **{w}** | {m} | {opp} |")
    print()


def table_runs(title, rows, base=None):
    print(f"**{title}**\n")
    print("| Agent | Games | Win % [95 % CI] | Score margin |" + (" Δ win vs base (paired) | Δ margin (paired) |" if base else ""))
    print("|---|---|---|---|" + ("---|---|" if base else ""))
    for label, desc in rows:
        if not exists(label):
            continue
        n, w, m = fmt_sum(label)
        line = f"| {desc}{' †' if label in OLD else ''} | {n} | {w} | {m} |"
        if base:
            if label == base:
                line += " base | – |"
            else:
                dw, hw, z, dm, hm = paired(base, label)
                line += f" {dw:+.1f} ± {hw:.1f} (z = {z:+.1f}) | {dm:+.2f} ± {hm:.2f} |"
        print(line)
    print()


if __name__ == "__main__":
    print("### Validation on fresh deals (seed 7)\n")
    table_validation([("3 players, 100 ms", "V_3p100"), ("3 players, 500 ms", "V_3p500"), ("3 players, 900 ms", "V_3p900"),
                      ("4 players, 100 ms", "V_4p100"), ("4 players, 500 ms", "V_4p500")])
    print("### Ablation of the 3-player agent (100 ms, seed 7, paired with the final agent)\n")
    table_runs("Leave one out", [("V_3p100", "final agent (H B P M R S I, K 0.5)"), ("A_noI", "− I (S with v3's determinisation D)"),
                                 ("A_noSI", "− S − I (= v3)"), ("A_noR", "− R (rollouts of 10 moves)"),
                                 ("A_noB", "− B (uniformly random rollouts)"), ("A_noH", "− H (TAG's own Sushi Go heuristic)")],
               base="V_3p100")
    print("### Development runs\n")
    table_runs("3 players, 100 ms (seed 1)", [("s1_ref", "v2"), ("abl_T4", "v3"), ("d1_S", "v3 + S"), ("d1_I", "v3 + S + I (= v4)"),
                                              ("d1_I_K0.8", "v4, K 0.8"), ("d1_P3", "v4 + card model P3"), ("d1_X0", "v4 + card model X0"),
                                              ("d1_own", "v4, ownCardTree"), ("d1_FW2", "v4 + X0 + W2 + F")])
    table_runs("3 players, 500 ms (seed 1)", [("f5_ref", "v2"), ("f5_finalT4", "v3"), ("f5_K1", "v3, K 1.0"), ("f5_noB", "v3 − B"),
                                              ("f5_S", "v3 + S"), ("f5_I", "v3 + S + I (= v4)"), ("f5_FL8", "v4 + F + L8"),
                                              ("f5_X0K10", "v4 + X0, K 1.0"), ("f5_FW2", "v4 + X0 + W2 + F"), ("f5_W2FL8", "v4 + X0 + W2 + F + L8")])
    table_runs("3 players, 900 ms (seed 1)", [("f9_ref", "v2"), ("f9_finalT4", "v3"), ("f9_I", "v4"), ("f9_X0K14", "v4 + X0, K 1.4")])
    table_runs("4 players, 100 ms (seed 1)", [("q4_ref", "v2"), ("q4_final", "v3"), ("q4_S", "v3 + S"), ("q4_I", "v3 + S + I (= v4)"),
                                              ("q4_Ith8", "v4, treeThreshold 8"), ("q4_own", "v4, ownCardTree"), ("q4_P3", "v4 + P3"),
                                              ("q4_X0", "v4 + X0"), ("q4_X0c", "v4 + X0 (repeat, canonical deals)"),
                                              ("q4_X0V3", "v4 + X0 + V (scale 3)"), ("q4_X0e1", "v4 + X0, ε 0.1"),
                                              ("q4_FK10", "v4 + X0 + F, K 1.0"), ("q4_FW2K11", "v4 + X0 + W2 + F, K 1.1"),
                                              ("q4_X0FL8", "v4 + X0 + F + L8"),
                                              ("q4_X0K3", "v4 + X0, opponents' 3 best cards (K)")])
    table_runs("4 players, 500 ms, seed 1 (paired with X0 K 1.0)",
               [("q5_ref", "v2"), ("q5_final", "v3"), ("val_q5", "v4"), ("q5_X0", "v4 + X0"), ("q5_X0K10", "v4 + X0, K 1.0"),
                ("q5_X0K14", "v4 + X0, K 1.4"), ("q5_X0U", "v4 + X0 + U"), ("q5_W2", "v4 + X0 + W2"),
                ("q5_W2S", "W2 + stopAtRoundEnd"), ("q5_FK10", "X0 K 1.0 + F (first version)"),
                ("q5_FW2", "W2 + F (first version)"), ("q5_FW2i", "W2 + F capped at 13k iterations")], base="q5_X0K10")
    table_runs("4 players, 500 ms, seed 2 (paired with W2 + F + L8)",
               [("q5_W2r", "W2 (repeat on seed 2)"), ("q5_FL8", "W2 + F + L8"), ("q5_FL8K07", "W2 + F + L8, K 0.7 for us"),
                ("q5_FL8K07b", "W2 + F + L8, K 0.7 for all"), ("q5_FL8oK14", "W2 + F + L8, opponentK 1.4"),
                ("q5_FL8R3", "W2 + F + L8, rollouts to the game end"), ("q5_FL8T1", "W2 + F + L8, softmax rollouts T 1"),
                ("q5_FL8t1000K14", "W2 + F + L8, our agent 1000 ms, K 1.37"), ("q5_FW2th8", "W2 + F, treeThreshold 8"),
                ("q5_FL8s7", "W2 + F + L8, σ 7"), ("q5_FL8s25", "W2 + F + L8, σ 25"),
                ("q5_FL8soft", "W2 + F + L8 without V and σ decay"), ("q5_FUL8", "W2 + F + L8 + U"),
                ("q5_X0b100", "the 100 ms agent with 100 ms"), ("q5_FL8d1", "W2 + F + L8, tree depth 1"),
                ("q5_FL8ens5", "W2 + F + L8, 5 trees (E)"), ("q5_FL8e35", "W2 + F + L8, ε 0.35"),
                ("q5_FL8oT15", "W2 + F + L8, opponents' rollout moves softmax T 1.5"),
                ("q5_FL8K3", "W2 + F + L8, opponents' 3 best cards (K)"),
                ("q5_FL8W25", "W2 + F + L8, progressive widening a = 0.25 (K)")], base="q5_FL8")
    print("### Other framework agents (3 players, 100 ms, seed 7)\n")
    table_opponents([("BasicMCTS + RHEA", "B_mcts_rhea"), ("OSLA + BasicMCTS", "B_osla_mcts")])
