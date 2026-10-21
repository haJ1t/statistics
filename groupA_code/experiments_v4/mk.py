#!/usr/bin/env python3
"""Generate agent JSON files for the arena. usage: mk.py  (writes agents/b{budget}/*.json)"""
import json, os, sys

def base(budget):
    return {"budgetType": "BUDGET_TIME", "budget": budget, "breakMS": 0}

def basic(b):   return {"class": "players.basicMCTS.BasicMCTSParams", "K": 1.41, "rolloutLength": 10, "maxTreeDepth": 100, **base(b)}
def ours(b, **kw):
    d = {"class": "groupA.SushiMCTSParams", "K": 1.41, "rolloutLength": 10, "maxTreeDepth": 100,
         "heuristic": {"class": "groupA.SushiGoHeuristic"},
         "useProgressiveBias": False, "useInformedRollout": False, "useDeterminisation": False,
         "adaptiveDeterminisation": True, "biasWeight": 1.0, "priorScale": 5.0, "rolloutEpsilon": 0.2,
         "nDeterminisations": 4, **base(b)}
    d.update(kw)
    return d
def ref(b, **kw):
    d = ours(b, **kw)
    d["class"] = "refv2.SushiMCTSParams"; d["heuristic"] = {"class": "refv2.SushiGoHeuristic"}
    for k in ("useActionPruning", "useOpponentModel", "opponentEpsilon"): d.pop(k, None)
    return d
def tagmcts(b): return {"class": "players.mcts.MCTSParams", "rolloutLength": 300, "opponentTreePolicy": "MultiTree",
                        "MASTGamma": 0, "heuristic": {"class": "players.heuristics.ScoreHeuristic"}, "K": 1,
                        "exploreEpsilon": 0.1, "treePolicy": "UCB_Tuned", "MAST": "Both", "rolloutType": "RANDOM",
                        "information": "Information_Set", "budgetType": "BUDGET_TIME", "budget": b}
def rhea(b):    return {"class": "players.rhea.RHEAParams", "horizon": 10, "discountFactor": 0.9, "populationSize": 10,
                        "childCount": 10, "crossoverType": "UNIFORM", "eliteCount": 1, "selectionType": "TOURNAMENT",
                        "tournamentSize": 5, **base(b)}

def write(path, d):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f: json.dump(d, f, indent=1)

if __name__ == "__main__":
    for b in (100, 250, 500, 1000):
        write(f"agents/b{b}/basic.json", basic(b))
        write(f"agents/b{b}/tagmcts.json", tagmcts(b))
        write(f"agents/b{b}/rhea.json", rhea(b))
        write(f"agents/b{b}/v2H.json", ours(b))
        write(f"agents/b{b}/v2final.json", ours(b, useProgressiveBias=True, useInformedRollout=True, useDeterminisation=True))
        write(f"agents/b{b}/v2HB.json", ours(b, useInformedRollout=True))
        write(f"agents/b{b}/ref.json", ref(b, useProgressiveBias=True, useInformedRollout=True, useDeterminisation=True))
    write("agents/b1000/v2final_900.json", ours(900, useProgressiveBias=True, useInformedRollout=True, useDeterminisation=True))
    write("agents/osla.json", {"class": "players.simple.OSLAHeuristic", "heuristic": {"class": "players.heuristics.ScoreHeuristic"}})
    print("ok")
