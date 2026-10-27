#!/usr/bin/env python3
"""merge_chunks.py out.csv part.csv:offset [part.csv:offset ...]

Joins the chunks of one long run. A chunk started with seed = first seed + offset, where offset is a multiple of 2n
(n players), plays the deals, focus seats and opponent order of games offset, offset + 1, ... of one long run
(arena.Arena: deal seed + g, focus seat g % n, opponent order rotated with (g / n) % 2). Its game numbers are shifted
by offset, so that the joined run pairs with other runs of the same seed block by game number. Long runs were split
into chunks after a container restart lost a running experiment (the arena writes its CSV only at the end).
"""
import csv
import sys

out, parts = sys.argv[1], sys.argv[2:]
rows, fields, seen = [], None, set()
for spec in parts:
    path, offset = spec.rsplit(":", 1)
    with open(path) as f:
        r = csv.DictReader(f)
        fields = fields or r.fieldnames
        for x in r:
            g = int(x["game"]) + int(offset)
            if g in seen:
                sys.exit(f"game {g} is in two chunks")
            seen.add(g)
            x["game"] = str(g)
            rows.append(x)
rows.sort(key=lambda x: int(x["game"]))
with open(out, "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=fields)
    w.writeheader()
    w.writerows(rows)
print(f"{out}: {len(rows)} games (game numbers {rows[0]['game']}–{rows[-1]['game']})")
