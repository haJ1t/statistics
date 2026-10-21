#!/usr/bin/env python3
"""Builds the groupA README of v4: drafts/README_v4.md with its results placeholder replaced by
drafts/results_section.md, whose table placeholders are filled from make_v4_tables.py (run in results/).
usage: assemble_readme.py OUT.md"""
import os, subprocess, sys

here = os.path.dirname(os.path.abspath(__file__))
tables = subprocess.run([sys.executable, os.path.join(here, "make_v4_tables.py")], cwd=os.path.join(here, "results"),
                        capture_output=True, text=True, check=True).stdout


def block(start):
    """the first markdown table after the line that starts with `start`"""
    lines = tables.splitlines()
    i = next(k for k, l in enumerate(lines) if l.startswith(start))
    j = next(k for k in range(i + 1, len(lines)) if lines[k].startswith("|"))
    k = j
    while k < len(lines) and lines[k].startswith("|"):
        k += 1
    return "\n".join(lines[j:k])


res = open(os.path.join(here, "drafts/results_section.md")).read()
res = res.replace("‹VALIDATION›", block("### Validation on fresh deals"))
res = res.replace("‹FOUR500›", block("**4 players, 500 ms, seed 2"))
res = res.replace("‹FRAMEWORK›", block("### Other framework agents"))
readme = open(os.path.join(here, "drafts/README_v4.md")).read()
head, sep, tail = readme.partition("## Results\n\n‹results sections›")
assert sep, "placeholder missing"
out = head + res.rstrip() + "\n" + tail.lstrip("\n")
assert "‹" not in out, "unfilled placeholder"
open(sys.argv[1], "w").write(out)
print("written", sys.argv[1], len(out.splitlines()), "lines")
