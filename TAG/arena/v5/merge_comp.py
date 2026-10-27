#!/usr/bin/env python3
"""Merges the two 3-player 1 s competition-file batches into results/v5_comp_3p1000_all.csv: batch b (seed 97) plays
the deals of games 90.. of seed 7, so its game g becomes game 90 + g (same deals and seats as V_3p900 game 90 + g)."""
import csv
rows = list(csv.DictReader(open('results/v5_comp_3p1000.csv')))
fields = list(rows[0].keys())
for r in csv.DictReader(open('results/v5_comp_3p1000b.csv')):
    r['game'] = str(int(r['game']) + 90)
    rows.append(r)
with open('results/v5_comp_3p1000_all.csv', 'w', newline='') as f:
    w = csv.DictWriter(f, fieldnames=fields)
    w.writeheader()
    w.writerows(rows)
print(len(rows), 'games')
