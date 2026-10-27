#!/usr/bin/env python3
"""Make an agent JSON from a base JSON with changed parameters (v5 study).
usage: mk.py base.json out.json key=value ... ; 'key' changes the 3-player (top-level) parameters,
'4:key' the fourPlayer block, 'args=a,b,...' / '4:args=...' the heuristic args (empty = no args)."""
import json, sys

def val(s):
    if s in ("true", "false"): return s == "true"
    try:
        return int(s) if s.lstrip('-').isdigit() else float(s)
    except ValueError:
        return s

base, out = sys.argv[1], sys.argv[2]
d = json.load(open(base))
for kv in sys.argv[3:]:
    k, v = kv.split('=', 1)
    tgt = d
    if k.startswith('4:'):
        k = k[2:]
        tgt = d['fourPlayer']
    if k == 'args':
        if v: tgt['heuristic']['args'] = [float(x) for x in v.split(',')]
        else: tgt['heuristic'].pop('args', None)
    else:
        tgt[k] = val(v)
json.dump(d, open(out, 'w'), indent=1)
print(out)
