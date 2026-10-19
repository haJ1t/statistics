#!/usr/bin/env python3
"""Points per card type of the focus agent: two runs on the same deals, paired difference per type.

usage: types.py base.csv other.csv   (CSV files written by arena.Arena with pts_<Type> columns)
"""
import sys
from compare import load, mean_ci


def main():
    base, other = load(sys.argv[1]), load(sys.argv[2])
    common = sorted(set(base) & set(other))
    types = [c[4:] for c in next(iter(base.values())).keys() if c.startswith("pts_")]
    print(f"| Card type | {sys.argv[1].split('/')[-1]} | {sys.argv[2].split('/')[-1]} | paired Δ (95% CI) |")
    print("|---|---|---|---|")
    for t in types + ["TOTAL"]:
        if t == "TOTAL":
            b = [float(base[g]["focusScore"]) for g in common]
            o = [float(other[g]["focusScore"]) for g in common]
        else:
            b = [float(base[g]["pts_" + t]) for g in common]
            o = [float(other[g]["pts_" + t]) for g in common]
        md, hd = mean_ci([y - x for x, y in zip(b, o)])
        print(f"| {t} | {sum(b)/len(b):.2f} | {sum(o)/len(o):.2f} | {md:+.2f} ± {hd:.2f} |")


if __name__ == "__main__":
    main()
