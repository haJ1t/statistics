#!/usr/bin/env python3
"""merge_parts.py out.csv first.csv second.csv offset

Joins a run and its continuation into one CSV. The continuation was started with seed = first seed + offset, where
offset is a multiple of 2n (n players), so its game g has the deal, focus seat and opponent order of game g + offset
of one long run (arena.Arena: deal seed + g, focus seat g % n, opponent order rotated with (g / n) % 2). Its game
numbers are shifted by offset, so that paired comparisons with other runs of the same seed block (pairing by game
number) stay valid.

  r2_val_3p900_all.csv   = r2_val_3p900 (seed 100 000, games 0-249) + r2_val_3p900b (seed 100 252)   offset 252
  v5_field_3p100_all.csv = v5_field_3p100 (seed 7, games 0-299) + r2_field_3p100b (seed 307)        offset 300
"""
import csv
import sys

out, first, second, offset = sys.argv[1], sys.argv[2], sys.argv[3], int(sys.argv[4])
with open(first) as f:
    r = csv.DictReader(f)
    fields, rows = r.fieldnames, list(r)
seen = {int(x["game"]) for x in rows}
with open(second) as f:
    for x in csv.DictReader(f):
        x["game"] = str(int(x["game"]) + offset)
        if int(x["game"]) in seen:
            sys.exit(f"game {x['game']} is in both parts")
        rows.append(x)
rows.sort(key=lambda x: int(x["game"]))
with open(out, "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=fields)
    w.writeheader()
    w.writerows(rows)
print(f"{out}: {len(rows)} games")
