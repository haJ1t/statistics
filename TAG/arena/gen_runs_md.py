#!/usr/bin/env python3
"""Writes drafts/runs.md: players, games, seed, focus agent and opponents of every run with a CSV in results/.
The specs were collected from the batch commands that started the runs (drafts/run_specs.json)."""
import json, glob, os

here = os.path.dirname(os.path.abspath(__file__))
specs = json.load(open(os.path.join(here, "drafts/run_specs.json")))
labels = sorted(os.path.basename(c)[:-4] for c in glob.glob(os.path.join(here, "results/*.csv")))
out = ["# Runs of the v3 and v4 studies", "",
       "Agent files are relative to the TAG root (`arena/` = this folder). Opponent seats alternate between the listed",
       "files. Seed = first game seed (game g uses seed + g).", "",
       "| Run | Players | Games | Seed | Focus agent | Opponents |", "|---|---|---|---|---|---|"]
for lab in labels:
    if lab not in specs:
        continue
    n, g, seed, foc, opp = specs[lab][:5]
    out.append(f"| `{lab}` | {n} | {g} | {seed} | `{foc}` | {', '.join(f'`{o}`' for o in opp.split(','))} |")
open(os.path.join(here, "drafts/runs.md"), "w").write("\n".join(out) + "\n")
print(len(out) - 7, "runs")
