#!/usr/bin/env python3
"""Builds the ECS7032P Assignment 1 report from the IEEE Word template of the module.

    python3 make_figures.py && python3 build_report.py      ->  report.docx and report.pdf

The module template (Report tips and templates/Paper_Template.docx) is stored in strict OOXML, which python-docx
cannot read, so it is first re-saved as a normal .docx with LibreOffice. Text markup used below: <i>..</i>,
<b>..</b>, <sub>..</sub>, <sup>..</sup>; {key} is a citation, numbered in order of first use (IEEE style).
Every number in the text comes from groupA_code/experiments_v3, _v4 and _v5 (RESULTS.md files).
"""
import copy
import os
import re
import subprocess
import sys

import docx
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt

HERE = os.path.dirname(os.path.abspath(__file__))
TEMPLATE = os.path.join(HERE, "..", "Report tips and templates", "Paper_Template.docx")
WORK = os.path.join(HERE, "build")
OUT = os.path.join(HERE, "ECS7032P_Assignment1_Report_GroupA")

# ----------------------------------------------------------------------------------------------- data
import csv as _csv
import math as _math

ROOT = os.path.normpath(os.path.join(HERE, ".."))
R4 = os.path.join(ROOT, "groupA_code", "experiments_v4", "results")
R5 = os.path.join(ROOT, "groupA_code", "experiments_v5", "results")


def _load(path):
    with open(path) as f:
        return {int(r["game"]): r for r in _csv.DictReader(f)}


def _wilson(p, n, z=1.96):
    c = (p + z * z / (2 * n)) / (1 + z * z / n)
    h = z * _math.sqrt(p * (1 - p) / n + z * z / (4 * n * n)) / (1 + z * z / n)
    return 100 * (c - h), 100 * (c + h)


def _mean_se(xs):
    n = len(xs)
    m = sum(xs) / n
    return m, _math.sqrt(sum((x - m) ** 2 for x in xs) / (n - 1) / n)


def run(path):
    """win %, Wilson CI, score margin and its 1.96 SE of a run (None if the run is still missing)"""
    if not os.path.exists(path):
        return None
    rows = _load(path)
    n = len(rows)
    p = sum(float(r["focusWin"]) for r in rows.values()) / n
    lo, hi = _wilson(p, n)
    m, se = _mean_se([float(r["focusScore"]) - float(r["bestOppScore"]) for r in rows.values()])
    return {"n": n, "win": 100 * p, "lo": lo, "hi": hi, "m": m, "mci": 1.96 * se}


def paired(base, other):
    """paired differences (same seed): win in percentage points and score margin, each with 1.96 SE and z"""
    if not (os.path.exists(base) and os.path.exists(other)):
        return None
    b, o = _load(base), _load(other)
    g = sorted(set(b) & set(o))
    mg = lambda r: float(r["focusScore"]) - float(r["bestOppScore"])
    w, sw = _mean_se([float(o[k]["focusWin"]) - float(b[k]["focusWin"]) for k in g])
    d, sd = _mean_se([mg(o[k]) - mg(b[k]) for k in g])
    return {"dw": 100 * w, "dwci": 196 * sw, "zw": w / sw, "dm": d, "dmci": 1.96 * sd, "zm": d / sd}


def seat_wins(log):
    """win % of every agent's seats from the summary block of an arena log"""
    out = {}
    if os.path.exists(log):
        for line in open(log):
            m = re.match(r"\s+(\S+\.json)(?: \[focus\])?\s+win ([\d.]+)% over", line)
            if m:
                out[m.group(1)] = float(m.group(2))
    return out


def fmt_run(r):
    return "pending" if r is None else f"{r['win']:.1f} [{r['lo']:.1f}, {r['hi']:.1f}]"


def fmt_margin(r):
    return "pending" if r is None else f"{r['m']:+.2f} ± {r['mci']:.2f}".replace("-", "−")


def pp(x, ci, z=None):
    s = f"{x:+.1f} ± {ci:.1f} pp".replace("-", "−")
    return s + (f" (<i>z</i> = {abs(z):.1f})" if z is not None else "")




import glob as _glob
import hashlib as _hashlib


def seat_wins_v4(label):
    """win % of the opponents' seats in a run of experiments_v4, from the summary block after its RESULT line"""
    out = {}
    for log in _glob.glob(os.path.join(R4, "*.log")):
        lines = open(log, errors="ignore").read().splitlines()
        for i, line in enumerate(lines):
            if line.startswith(f"RESULT {label} "):
                for x in lines[i + 1:i + 12]:
                    m = re.match(r"\s+(\S+\.json)\s+win ([\d.]+)% over (\d+) seats", x)
                    if m and "[focus]" not in x:
                        out[m.group(1)] = float(m.group(2))
                    if x.startswith("RESULT") or x.startswith("BATCH"):
                        break
    return out


def study_size():
    """complete runs (at least 250 games; identical copies in several folders counted once) and their games"""
    seen = {}
    for d in ("experiments_v3", "experiments_v4", "experiments_v5"):
        for f in _glob.glob(os.path.join(ROOT, "groupA_code", d, "results", "*.csv")):
            data = open(f, "rb").read()
            with open(f) as fh:
                r = _csv.DictReader(fh)
                if "game" not in (r.fieldnames or []):
                    continue
                n = sum(1 for _ in r)
            if n >= 250:
                seen[_hashlib.md5(data).hexdigest()] = n
    return len(seen), sum(seen.values())


def probe_iterations():
    """iterations per decision of v3 and v4 (results/probe_iterations.txt): {(budget, 'v3'): n}"""
    out = {}
    f = os.path.join(R5, "probe_iterations.txt")
    if os.path.exists(f):
        for line in open(f):
            m = re.match(r"(\d+) ms (v\d): .*iterations/decision=(\d+)", line)
            if m:
                out[(int(m.group(1)), m.group(2))] = int(m.group(3))
    return out


def diag4():
    """4-player search diagnostics of the final 500 ms agent (bench4.txt, treestats4.txt, agree4.txt)"""
    try:
        b, t, a = (open(os.path.join(R5, x)).read() for x in ("bench4.txt", "treestats4.txt", "agree4.txt"))
    except OSError:
        return None
    it = dict((k, int(v)) for k, v in re.findall(r"(\S+?\.json)\s+decisions=\d+\s+mean tree iterations=(\d+)", b))
    ts = re.search(r"states (\d+), iterations (\d+) .*root children (\d+), depth-1 visits 1:(\d+)%.*"
                   r"mean depth ([\d.]+)", t)
    seed = re.search(r"decisions (\d+), same card with another seed ([\d.]+)%", a)
    vs = dict((k, float(v)) for k, v in re.findall(r"vs (\S+?\.json)\s+([\d.]+)% same", a))
    if not (it and ts and seed and vs):
        return None
    return {"itF": it.get("arena/agents/final/SushiMCTS_500_FL8.json"), "itNoF": it.get("arena/v5/agents/b500_4noF.json"),
            "states": int(ts.group(1)), "iters": int(ts.group(2)), "kids": int(ts.group(3)), "once": int(ts.group(4)),
            "depth": float(ts.group(5)), "seedSame": float(seed.group(2)), "rerun": vs.get("SushiMCTS_500_FL8.json"),
            "L16": vs.get("b500_4L16.json"), "t1000": vs.get("b1000_4FL8.json")}


def num(x, d=0):
    """number with a thin space as thousands separator"""
    return f"{x:,.{d}f}".replace(",", " ")


VAL = {k: run(os.path.join(R5, f"r2_val_{k}.csv")) for k in ("3p100", "3p500", "3p900", "4p100", "4p500")}
DUCT = run(os.path.join(R5, "r2_duct_3p100.csv"))
DUCT_SEATS = seat_wins(os.path.join(R5, "r2_duct_3p100.log"))
LEAD = paired(os.path.join(R5, "v5_base3p100.csv"), os.path.join(R5, "r2_lead_3p100.csv"))
OLD = {k: run(os.path.join(R4, f"V_{k}.csv")) for k in ("3p100", "3p500", "3p900", "4p100", "4p500")}
OLDSEATS = {k: seat_wins_v4(f"V_{k}") for k in ("3p100", "3p500")}
FIELD = run(os.path.join(R5, "v5_field_3p100.csv"))
BMR = run(os.path.join(R4, "B_mcts_rhea.csv"))
BOM = run(os.path.join(R4, "B_osla_mcts.csv"))
BSEATS = {**seat_wins_v4("B_mcts_rhea"), **{"osla_" + k: x for k, x in seat_wins_v4("B_osla_mcts").items()}}
BASE5 = run(os.path.join(R5, "v5_base3p100.csv"))
LAST = {k: paired(os.path.join(R5, "v5_base3p100.csv"), os.path.join(R5, f"v5_{k}_3p100.csv"))
        for k in ("X3", "N05", "V2D", "W1", "R3V2")}
ITER = probe_iterations()
DIAG = diag4()
NRUNS, NGAMES = study_size()


def v(k, field="win", d=1):
    r = VAL[k]
    return "[pending]" if r is None else f"{r[field]:.{d}f}"


def dpp(d, z=True):
    """paired win-rate difference: '−1.3 ± 5.8 pp (z = 0.4)'"""
    return pp(d["dw"], d["dwci"], d["zw"] if z else None)


# ----------------------------------------------------------------------------------------------- content
TITLE = "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!"

AUTHORS = [  # three columns, as in the template; fill in the group's names and student numbers
    ["Halit Ozger", "Student no. [to be completed]", "Queen Mary University of London", "London, UK"],
    ["[Group member 2]", "Student no. [to be completed]", "Queen Mary University of London", "London, UK"],
    ["[Group member 3]", "Student no. [to be completed]", "Queen Mary University of London", "London, UK"],
]

FIRST_PAGE_NOTE = ("All sections, tables and figures of this paper were drafted with Claude {claude}; the prompts are "
                   "given in the statement on the use of generative AI at the end of the paper.")

ABSTRACT = (
    "In Sushi Go! players pick cards simultaneously from hidden hands that rotate around the table, which basic "
    "Monte Carlo tree search (MCTS) does not model. Starting from TAG's BasicMCTS, we added a model of the game's "
    "scoring that drives evaluation and rollouts, rollouts that end with the round, max-n backups, decoupled UCT for "
    "the simultaneous choices and information-set search. On held-out deals the agent wins "
    f"{v('3p100')}% of 500 three-player games at 100 ms against TAG's ISMCTS and our previous agent (chance 33.3%), "
    f"{v('4p100')}% with four players (chance 25%) and {v('3p900')}% at 900 ms. Ablations show that the heuristic "
    "and the simultaneous-move tree matter most, that the tree's advantage tends to grow with the time budget, and "
    "that four players need different settings.")

KEYWORDS = "Monte Carlo tree search, information sets, simultaneous moves, card games, Sushi Go!, TAG"

REFS = {
    "tag": "R. D. Gaina, M. Balla, A. Dockhorn, R. Montoliu, and D. Perez-Liebana, “TAG: A tabletop games "
           "framework,” in <i>Proc. AIIDE Workshop on Experimental AI in Games (EXAG)</i>, 2020.",
    "browne": "C. B. Browne <i>et al.</i>, “A survey of Monte Carlo tree search methods,” <i>IEEE Trans. Comput. "
              "Intell. AI Games</i>, vol. 4, no. 1, pp. 1–43, 2012.",
    "cowling": "P. I. Cowling, E. J. Powley, and D. Whitehouse, “Information set Monte Carlo tree search,” "
               "<i>IEEE Trans. Comput. Intell. AI Games</i>, vol. 4, no. 2, pp. 120–143, 2012.",
    "klang": "C.-M. E. Klang, V. Enhörning, A. Alvarez, and J. Font, “Assessing simultaneous action selection and "
             "complete information in TAG with Sushi Go!,” in <i>Proc. IEEE Conf. Games (CoG)</i>, 2021, pp. 1–4.",
    "kocsis": "L. Kocsis and C. Szepesvári, “Bandit based Monte-Carlo planning,” in <i>Proc. 17th Eur. Conf. "
              "Mach. Learn. (ECML)</i>, 2006, pp. 282–293.",
    "chaslot": "G. M. J.-B. Chaslot, M. H. M. Winands, H. J. van den Herik, J. W. H. M. Uiterwijk, and B. Bouzy, "
               "“Progressive strategies for Monte-Carlo tree search,” <i>New Math. Natural Comput.</i>, vol. 4, "
               "no. 3, pp. 343–357, 2008.",
    "silver": "D. Silver and G. Tesauro, “Monte-Carlo simulation balancing,” in <i>Proc. 26th Int. Conf. Mach. "
              "Learn. (ICML)</i>, 2009, pp. 945–952.",
    "long": "J. Long, N. R. Sturtevant, M. Buro, and T. Furtak, “Understanding the success of perfect information "
            "Monte Carlo sampling in game tree search,” in <i>Proc. 24th AAAI Conf. Artif. Intell.</i>, 2010, "
            "pp. 134–140.",
    "frank": "I. Frank and D. Basin, “Search in games with incomplete information: A case study using Bridge card "
             "play,” <i>Artif. Intell.</i>, vol. 100, no. 1–2, pp. 87–123, 1998.",
    "lanctot": "M. Lanctot, V. Lisý, and M. H. M. Winands, “Monte Carlo tree search in simultaneous move games "
               "with applications to Goofspiel,” in <i>Computer Games (CGW 2013)</i>, Commun. Comput. Inf. Sci., "
               "vol. 408. Cham, Switzerland: Springer, 2014, pp. 28–43.",
    "tak": "M. J. W. Tak, M. Lanctot, and M. H. M. Winands, “Monte Carlo tree search variants for simultaneous "
           "move games,” in <i>Proc. IEEE Conf. Comput. Intell. Games (CIG)</i>, 2014, pp. 1–8.",
    "sturtevant": "N. R. Sturtevant, “An analysis of UCT in multi-player games,” in <i>Computers and Games "
                  "(CG 2008)</i>, Lect. Notes Comput. Sci., vol. 5131. Berlin, Germany: Springer, 2008, pp. 37–49.",
    "goodman": "J. Goodman, D. Perez-Liebana, and S. Lucas, “MultiTree MCTS in tabletop games,” in <i>Proc. IEEE "
               "Conf. Games (CoG)</i>, 2022, pp. 292–299.",
    "wilson": "E. B. Wilson, “Probable inference, the law of succession, and statistical inference,” <i>J. Amer. "
              "Statist. Assoc.</i>, vol. 22, no. 158, pp. 209–212, 1927.",
    "claude": "Anthropic, “Claude” (large language model, used through the Claude Code tool), 2026. [Online]. "
              "Available: https://claude.ai",
}


B = []
h1 = lambda t: B.append(("h1", t))
h2 = lambda t: B.append(("h2", t))
p = lambda t: B.append(("p", t))
bullet = lambda t: B.append(("bullet", t))

# ------------------------------------------------------------------ I
h1("Introduction")
p("Tabletop games combine chance, hidden information and several interacting players within small, well-defined "
  "rule sets, which makes them a long-standing test bed for game AI; the Tabletop Games framework (TAG) {tag} gives "
  "many of them a common interface so that agents can be compared. Monte Carlo tree search (MCTS) {browne} is the "
  "usual starting point for such agents, but its basic form assumes that the state is fully observed and that "
  "players move one after another {cowling}. <i>Sushi Go!</i>, one of TAG's games, breaks both assumptions: every "
  "player secretly picks one card from its hand, all players reveal their cards together and pass the rest of "
  "their hands on, and after three rounds the highest score wins. Klang et al. {klang} introduced the game to TAG "
  "and assessed simultaneous action selection and complete information in it; the current version, which we use, "
  "shows an agent only the cards it has seen.")
p("Four properties make the game hard for a search agent. (i) The opponents' hands are hidden until they are "
  "passed on. (ii) Moves are simultaneous: no card can be chosen in reaction to an opponent's card of the same "
  "turn. (iii) Many points are delayed and conditional – sets pay out only when completed, Maki rolls at the end of "
  "the round and Pudding at the end of the game – so short simulations miss them and long ones are noisy. (iv) "
  "Hands of nine (three players) or eight cards (four players) give up to 729 or 4 096 joint moves per turn, with "
  "0.1–1 s per decision. TAG's BasicMCTS addresses none of the first three: it searches the one shuffled sample of "
  "the hidden cards it receives as if it were the true state, treats the players of a turn as moving one after "
  "another and evaluates random rollouts by the player's own score.")
p("We ask which adaptations of MCTS to these properties make an agent stronger (RQ1), and how their value changes "
  "with the time per decision (RQ2) and the number of players (RQ3). We extend BasicMCTS in steps, keeping every "
  "modification as a switch, and test each in experiments of (mostly) 500 games. Our contributions are:")
bullet("a model of Sushi Go!'s scoring that estimates every player's final score and every card's marginal value, "
       "used both to evaluate states and as the rollout policy of all players;")
bullet("a re-implementation on BasicMCTS of round-end rollouts, max-n backups, decoupled UCT and information-set "
       "search, combined with this model and compared with TAG's own decoupled information-set MCTS;")
bullet("evidence that the best settings differ between three and four players;")
bullet(f"a study of {NRUNS} runs ({num(NGAMES)} games) with leave-one-out ablations, a 2<sup>4</sup> factorial "
       "design, validation on held-out deals and negative results that locate the agent's remaining limits.")

# ------------------------------------------------------------------ II
h1("Background")
h2("TAG and Sushi Go!")
p("TAG {tag} splits a game into a state, a forward model and parameters; an agent receives a copy of the state, in "
  "which every card it has not seen is reshuffled, and uses the forward model to simulate moves. In TAG's Sushi Go! "
  "the 108-card deck holds Tempura (5 points per pair), Sashimi (10 per triple), Dumplings (1, 3, 6, 10 or 15 "
  "points for one to five), Salmon, Squid and Egg Nigiri (2, 3 and 1 points, tripled on an earlier Wasabi), Maki "
  "rolls with one to three icons (6 and 3 points for the most and second-most icons of a round), Chopsticks (two "
  "cards in a later turn) and Pudding (+6 for the most and, with three or more players, −6 for the fewest at the "
  "end of the game). Each round deals 12 − <i>n</i> cards to each of <i>n</i> players. As the hands rotate, every "
  "hand of a round has been seen after <i>n</i> − 1 passes, so hidden information is concentrated at the start of "
  "each round, while the value of a card depends on what the others will take from the hands we pass on.")
h2("Monte Carlo Tree Search")
p("MCTS {browne} builds a search tree by repeating selection, expansion, simulation (rollout) and "
  "backpropagation. Upper Confidence bounds applied to Trees (UCT) {kocsis} selects the child that maximises the "
  "upper confidence bound (UCB1) <i>Q</i> + <i>K</i>√(ln <i>N</i> / <i>n</i>), where <i>Q</i> is the child's mean reward, <i>n</i> its visits "
  "and <i>N</i> those of its parent. Domain knowledge can enter through heuristic rollout policies {browne} or "
  "priors such as progressive bias {chaslot}. A stronger rollout policy does not guarantee better estimates, "
  "however: Silver and Tesauro {silver} showed that rollouts should be balanced, i.e., give unbiased value "
  "estimates, rather than be played by a strong policy.")
h2("Hidden Information, Simultaneous Moves and Several Players")
p("Determinisation, or perfect-information Monte Carlo (PIMC) sampling, samples the hidden information, searches "
  "each sample as if it were the true state and combines the results. Long et al. {long} related its success in "
  "card games to properties such as how quickly hidden information is revealed during play, which in Sushi Go! "
  "happens within <i>n</i> − 1 turns. Its known flaws are strategy fusion – the search assumes that it can act "
  "differently in states it cannot tell apart – and non-locality {frank}. Information-set MCTS (ISMCTS) {cowling} "
  "avoids strategy fusion by searching one tree over the searcher's information sets and sampling the hidden "
  "information anew in every iteration; it was developed for card games such as the three-player Dou Di Zhu. In "
  "its single-observer form the opponents' choices in the tree are not conditioned on their own private "
  "information; the multiple-observer form models it with one tree per player at a higher cost {cowling}, an idea "
  "TAG's advanced MCTS agent offers as MultiTree MCTS {goodman}.")
p("Simultaneous moves are usually handled by decoupled UCT (DUCT): every player chooses its own action at a node "
  "from its own bandit statistics and the joint action leads to a child {lanctot}. UCB-based DUCT need not converge "
  "to an equilibrium, so Lanctot et al. {lanctot} compared it with EXP3 and regret-matching selection, and Tak et "
  "al. {tak} found that the best variant depends on the game; both studies used two-player games such as "
  "Goofspiel. In multi-player games, UCT with max-n backups, in which every player maximises its own value, "
  "computes a mixed-strategy equilibrium and played as well as or better than earlier multi-player search "
  "algorithms in Sturtevant's experiments {sturtevant}.")
p("TAG's advanced MCTS agent already offers information sets, one tree per player and a decoupled option, but the "
  "assignment does not allow it as a basis; BasicMCTS has none of these ideas. How they combine with domain "
  "knowledge in a multi-player drafting game, what each contributes, and how this depends on the time budget and "
  "the number of players has not been studied: the earlier Sushi Go! study {klang} predates the version with "
  "hidden hands. These are the questions this paper answers.")

# ------------------------------------------------------------------ III
h1("Method")
p("Our agent, SushiMCTS, is a modified copy of TAG's BasicMCTS (package <i>players.basicMCTS</i>) and uses no code "
  "of TAG's advanced MCTS agent. Every modification is a switch in its parameter class (Table I), so all variants "
  "run the same code; Fig. 1 shows one iteration of the final agent. The search techniques come from the "
  "literature; our own elements are the card-value model shared by the evaluation and every player's simulation, "
  "and its integration with these techniques on BasicMCTS.")
B.append(("table", "Modifications of BasicMCTS (✓ = used in the final agent with 3 / 4 players)",
          [0.5, 2.12, 0.76],
          ["Id", "Modification", "3 p / 4 p"],
          [["H", "heuristic (2)–(3) from the card-value model (1)", "✓ / ✓"],
           ["B", "ε-greedy card-value rollouts for every player", "✓ / ✓"],
           ["R", "rollouts end when the round is scored", "✓ / ✓"],
           ["P", "one branch per distinct card", "✓ / ✓"],
           ["M", "max-n backups {sturtevant}", "✓ / ✓"],
           ["S", "decoupled UCT, one node per turn {lanctot}", "✓ / ✓"],
           ["I", "single-observer information-set search {cowling}", "✓ / ✓"],
           ["X0", "card-model constants tuned for 4 players <sup>a</sup>", "– / ✓"],
           ["W2", "win-gap value for 4 players (Section III-D)", "– / ✓ <sup>b</sup>"],
           ["F, L", "fast simulator, 8 rollouts per leaf", "– / ✓ <sup>b</sup>"],
           ["A", "progressive bias {chaslot}", "dropped in v3"],
           ["D", "ensemble of up to 4 determinised trees {long}", "replaced by I"]],
          "<sup>a</sup> σ 15, <i>w</i> 0.7, α 0.5, <i>T</i> 6; Sashimi discount 0.5; Maki horizon 3 with 0.25 per "
          "icon; next-Dumpling weight 1.5; Pudding factor 1.25; average Nigiri 3; 3 cards per missing card left in "
          "the hands. <sup>b</sup> at 500 ms or more per decision."))
B.append(("fig", "figures/fig1_iteration.png", 3.39,
          "One iteration of the final agent. The letters refer to Table I; after the time budget the agent plays "
          "its most visited card at the root."))
h2("Card-Value Model and Heuristic (H)")
p("For a state <i>s</i> and player <i>p</i> the model estimates the final score")
B.append(("eq", "Ŝ<sub>p</sub> = <i>S<sub>p</sub></i> + α<i>U<sub>p</sub></i> + <i>M<sub>p</sub></i> + "
                "<i>w<sub>t</sub> P<sub>p</sub></i>,", 1))
p("where <i>S<sub>p</sub></i> is the banked score (TAG scores Tempura, Sashimi, Dumplings and Nigiri when they are "
  "revealed), <i>U<sub>p</sub></i> the value of unfinished sets, each weighted by a completion factor "
  "min(1, (<i>r</i> − <i>m</i> + 1)/<i>T</i>) (0 if <i>r</i> < <i>m</i>) for a set that needs <i>m</i> more cards "
  "with <i>r</i> picks left, <i>M<sub>p</sub></i> the Maki points <i>p</i> would get if the round ended now, and "
  "<i>P<sub>p</sub></i> the Pudding points if the game ended now, weighted by <i>w<sub>t</sub></i> = <i>t</i>/3 in "
  "round <i>t</i> (α = 0.5, <i>T</i> = 4). The value of <i>s</i> for player <i>i</i> is")
B.append(("eq", "<i>H<sub>i</sub></i>(<i>s</i>) = tanh(Δ<sub><i>i</i></sub> / σ),", 2))
B.append(("eq", "Δ<sub><i>i</i></sub> = <i>w</i>(Ŝ<sub><i>i</i></sub> − max<sub><i>j</i>≠<i>i</i></sub> "
                "Ŝ<sub><i>j</i></sub>) + (1 − <i>w</i>)(Ŝ<sub><i>i</i></sub> − mean<sub><i>j</i>≠<i>i</i></sub> "
                "Ŝ<sub><i>j</i></sub>),", 3))
p("with σ = 15 and <i>w</i> = 0.7: the first term rewards beating the leader, since only first place wins, and "
  "the second keeps a gradient when the agent is far ahead or behind. The marginal value "
  "<i>mv<sub>p</sub></i>(<i>c</i>) of a card <i>c</i> is the change of Ŝ<sub><i>p</i></sub> when <i>p</i> plays it.")
h2("Simulation (B, R)")
p("<b>B:</b> every player plays its card with the highest <i>mv</i>, or a random card with probability ε = 0.2 "
  "(with Chopsticks, the second card is the best remaining one); the same policy models the opponents. <b>R:</b> a "
  "rollout stops when the current round has been scored; only Pudding carries over, which (1) captures, so the "
  "leaves are valued by banked points and the Pudding standing.")
h2("Tree (P, M, S, I)")
p("<b>P:</b> identical cards lead to identical states, so a node has one branch per distinct card. <b>M:</b> a "
  "node stores one value per player and every player maximises its own value {sturtevant}. <b>S:</b> one node "
  "represents a whole turn; at a node every player <i>p</i> picks a card by")
B.append(("eq", "<i>a<sub>p</sub></i> = argmax<sub><i>a</i></sub> [<i>Q<sub>p</sub></i>(<i>a</i>) + "
                "<i>K</i>√(ln <i>N<sub>p</sub></i>(<i>a</i>) / <i>n<sub>p</sub></i>(<i>a</i>))]", 4))
p("from its own statistics, trying untried cards first; the joint choice is played in the forward model and leads "
  "to the child of that joint action {lanctot}. In v3 the players of a turn appeared one after another in the "
  "tree, so a later player “saw” the card an earlier player had just chosen, which is impossible in the real game. "
  "<b>I:</b> every iteration starts from a new sample of the cards we cannot see and walks one shared open-loop "
  "tree, whose nodes store statistics but no states {cowling}. Statistics are keyed by card type; "
  "<i>n<sub>p</sub></i>(<i>a</i>) counts how often <i>p</i> chose <i>a</i> at the node and the availability count "
  "<i>N<sub>p</sub></i>(<i>a</i>) how many iterations reached the node with <i>a</i> in <i>p</i>'s sampled hand. "
  "I replaces the determinisation ensemble D of v2 and v3, which split the budget between up to four independent "
  "trees. With three players <i>K</i> = 0.5.")
h2("Four Players")
p("With four players the agent switches parameters (Section IV-D). It uses the card constants X0 (Table I), found "
  "by a search-free coordinate tuner that plays greedy card-value policies against each other. From 500 ms it also "
  "uses <b>W2</b> – <i>w</i> = 1; finished games valued by tanh((<i>S<sub>i</sub></i> − "
  "max<sub><i>j</i>≠<i>i</i></sub> <i>S<sub>j</sub></i>)/2) with Pudding as tie-break; σ multiplied by "
  "(<i>R</i>/2)<sup>0.5</sup> for <i>R</i> rounds left; opponents in the backup valuing their lead over the others' "
  "mean – and <b>F</b>, TAG's rules re-implemented on integer arrays and checked move by move against the forward "
  "model, whose speed is spent on <b>L</b> = 8 rollouts per leaf, with <i>K</i> = 1. FSPEED_SENTENCE")
h2("Experimental Setup")
B.append(("table", "Experimental setup", [0.72, 2.66],
          ["Item", "Setting"],
          [["Framework", "TAG as distributed with the module, Sushi Go! with its default rules; OpenJDK 21"],
           ["Opponents",
            "v2 (frozen copy of our previous agent): H, progressive bias, B, D, <i>K</i> 1.41. TAG ISMCTS: TAG's "
            "SushiGo_3+P_HiBudget configuration, set for 400 ms and run at our budget (information sets, one tree per "
            "player {goodman}, UCB-Tuned, <i>K</i> 1, random rollouts to the game end, score heuristic). TAG DUCT: "
            "TAG's SushiGoDUCT configuration (decoupled UCT, <i>K</i> 1, information sets, lead heuristic, 30-move "
            "rollouts). BasicMCTS (<i>K</i> 1.41, "
            "10-move random rollouts), Rolling Horizon Evolutionary Algorithm (RHEA; horizon 10, population 10), "
            "One-Step Look-Ahead (OSLA, score heuristic)"],
           ["Budget", "CPU time per decision, equal for all agents: 100 ms (most development), 500 and 900 ms"],
           ["Seating", "our agent in seat <i>g</i> mod <i>n</i> in game <i>g</i>; the other seats alternate between "
                       "the two opponents (4 players: 2 + 1, swapped every 4 games)"],
           ["Deals", "game <i>g</i> uses seed <i>s</i> + <i>g</i>; since v4 a fixed deck order is shuffled with it, "
                     "so equal <i>s</i> gives equal deals (paired runs); earlier runs used TAG's own deal, which "
                     "differs between runs (independent samples)"],
           ["Seeds", "development <i>s</i> = 1, 2 (v3 also 1001–3001); ablation and last study <i>s</i> = 7; "
                     "held-out validation <i>s</i> = 100 000, disjoint from all other deals"],
           ["Games", "500 per condition; 250 at 900 ms, in 4-player 500 ms development and in the 500/900 ms "
                     "validation (48–73 s of CPU time per game); 300 in one run"],
           ["Hardware", "18-core laptop (6–16 games in parallel); last study and validation on a 4-core machine "
                        "(4 in parallel)"],
           ["Records", "agent files, per-game results and scripts of every run are kept with our code "
                       "(experiments_v3 to experiments_v5)"]],
          None))
p("Each game is an independent trial whose outcome for our agent is 1 for a win, 1/<i>k</i> for a <i>k</i>-way "
  "shared first place and 0 otherwise. Win rates are reported with Wilson 95% confidence intervals (CI) {wilson}; "
  "the score margin "
  "(our score minus the best opponent's) as mean ± 1.96 standard errors (SE); differences between runs as the mean "
  "of the per-game differences on the same deals ± 1.96 SE (for independent samples, the difference of the means "
  "with the two-sample SE) and <i>z</i> = difference/SE, in percentage points (pp) for win rates. With 500 games a "
  "win rate near 50% has a 95% interval of ±4.4 pp, and a difference between two runs about ±5.7 pp even when "
  "paired, because the outcomes of two agents on the same deal correlate only weakly (0.0–0.15); the score margin "
  "is about twice as sensitive and served to screen ideas. We report unadjusted intervals and state where a "
  "conclusion survives a Holm correction. We expected (H1) every component to improve both measures, (H2) the "
  "decoupled information-set tree to matter more at longer budgets and (H3) the best settings to depend on the "
  "number of players.")

# ------------------------------------------------------------------ IV
h1("Results")
h2("Development from v2 to v4 (RQ1, RQ2)")
p("Fig. 2 traces the development with three players (independent samples). Removing progressive bias from v2 and "
  "adding P, R, M and <i>K</i> = 0.5 doubled the win rate at 100 ms (24.8% → 50.1%, +25.3 ± 5.8 pp). In a "
  "2<sup>4</sup> factorial design on top of H + B + D + P (16 cells × 500 games) the main effects were "
  "+8.7 ± 2.1 pp for R (<i>z</i> = 8.1), +5.2 ± 2.2 for <i>K</i> = 0.5 instead of 1.41, +4.1 ± 2.0 for M and "
  "+3.6 ± 2.0 for a completion horizon <i>T</i> = 8; max-n backups needed less exploration (M × <i>K</i> "
  "+3.2 ± 2.2), and <i>T</i> = 8 was dropped because it overlaps with R (R × <i>T</i> −4.2 ± 2.1). Progressive bias no longer helped once informed rollouts were present "
  "(−1.2 ± 4.3 pp), and making the opponents in the tree follow the greedy card-value policy cost 16.9 ± 5.8 pp.")
B.append(("fig", "figures/fig2_budget.png", 3.39,
          "Win rate with three players against v2 and TAG's ISMCTS by CPU time per decision. Lines: development runs "
          "(seed 1, independent samples; v2 = H + A + B + D, v3 = H + B + D + P + R + M, v4 = v3 + S + I − D). "
          "Open circles: the final agent on held-out deals. Bars: Wilson 95% intervals."))
p("With longer budgets v3 lost 17.3 ± 7.3 pp from 100 to 900 ms but v4 only 8.1 ± 7.6 pp, so the gain from S and I "
  "rose from +5.6 ± 6.2 to +14.8 ± 8.5 pp; the rise (+9.2 ± 10.5 pp, <i>z</i> = 1.7) agrees with H2 but is not "
  "significant. S alone added +1.3 pp to v3 at 100 ms and +6.2 pp at 500 ms, so the decoupled tree carries most "
  "of the budget dependence. We attribute this to v3's sequential tree, in which later players react to cards "
  "chosen earlier in the same turn: a wrong model that more iterations exploit more. ITERATIONS_SENTENCE "
  "SEATS_SENTENCE")
h2("Final Agent on Held-Out Deals (RQ1)")
B.append(("table", "Final agent on held-out deals",
          [0.42, 1.06, 0.28, 0.94, 0.69],
          ["Setting", "Opponents", "<i>n</i>", "Win % [95% CI]", "Margin"],
          [["3p/100", "v2, ISMCTS", str(VAL['3p100']['n']) if VAL['3p100'] else "500", fmt_run(VAL['3p100']), fmt_margin(VAL['3p100'])],
           ["3p/500", "v2, ISMCTS", str(VAL['3p500']['n']) if VAL['3p500'] else "250", fmt_run(VAL['3p500']), fmt_margin(VAL['3p500'])],
           ["3p/900", "v2, ISMCTS", str(VAL['3p900']['n']) if VAL['3p900'] else "250", fmt_run(VAL['3p900']), fmt_margin(VAL['3p900'])],
           ["4p/100", "v2, ISMCTS", str(VAL['4p100']['n']) if VAL['4p100'] else "500", fmt_run(VAL['4p100']), fmt_margin(VAL['4p100'])],
           ["4p/500", "v2, ISMCTS", str(VAL['4p500']['n']) if VAL['4p500'] else "250", fmt_run(VAL['4p500']), fmt_margin(VAL['4p500'])],
           ["3p/100", "DUCT, ISMCTS", str(DUCT['n']) if DUCT else "500", fmt_run(DUCT), fmt_margin(DUCT)],
           ["3p/100", "ISMCTS × 2 <sup>a</sup>", str(FIELD['n']), fmt_run(FIELD), fmt_margin(FIELD)],
           ["3p/100", "BasicMCTS, RHEA <sup>a</sup>", str(BMR['n']), fmt_run(BMR), fmt_margin(BMR)],
           ["3p/100", "OSLA, BasicMCTS <sup>a</sup>", str(BOM['n']), fmt_run(BOM), fmt_margin(BOM)]],
          "Setting: players / ms per decision. Seed block 100 000 except <sup>a</sup> seed block 7 (used for no "
          "decision about these opponents). Margin: our score minus the best opponent's, ± 95% CI. Chance: 33.3% "
          "(3 players), 25% (4 players)."))
p("FINAL_AGENT_PARAGRAPH")
h2("Ablation (RQ1)")
B.append(("fig", "figures/fig3_effects.png", 3.39, "FIG3_CAPTION"))
p("Fig. 3 (top) removes one component at a time. Every removal lowers both measures; on the score margin all five "
  "differences survive a Holm correction, on the win rate only those of H and of S + I (−R: <i>z</i> = 2.0, −B: "
  "1.4, −I: 1.3), so H1 holds for the margin and only partly for wins. Without H the leaves are valued by TAG's "
  "default heuristic, the own score divided by 50, which ignores the opponents' scores – although only first place "
  "wins – and the Pudding standing, and does not match the scale of <i>K</i>: this costs 28.3 ± 5.8 pp. "
  "LEAD_SENTENCE S and I together are worth 10.2 ± 5.7 pp (<i>z</i> = 3.5) but I alone only 3.8 ± 5.7 pp, as Long "
  "et al.'s analysis {long} suggests for a game that reveals its hidden information within <i>n</i> − 1 turns. P "
  "and M cannot be removed from the final agent, since decoupled selection needs one value per player and the "
  "card-type statistics of I merge identical cards. The components also change the style of play: at 500 ms v3 "
  "scored fewer points than v2 from Sashimi (−2.7 per game) and Maki (−1.9) but more from Dumplings (+1.9) and "
  "Pudding (+1.3), sets that cannot fail or are decided over the whole game.")
h2("Four Players and the Limits of Search (RQ3)")
p("With four players the 3-player configuration won 46.4% at 100 ms and 38.4% at 500 ms (development; chance 25%). "
  "None of the 4-player changes is individually significant: X0 added +3.2 ± 6.2 pp at 100 ms; at 500 ms X0 with "
  "W2 and <i>K</i> = 1 added +7.2 ± 8.6 pp and F + L a further +1.0 ± 7.9 pp, whereas F + L cost 6.6 ± 6.0 pp at "
  "100 ms (<i>z</i> = 2.2). Together they support H3: adding X0, W2 and <i>K</i> = 1 to the 3-player agent at "
  "500 ms cost 7.7 ± 5.4 pp (<i>z</i> = 2.8), so their effect differs by 14.9 ± 10.2 pp (<i>z</i> = 2.9) between the table sizes. "
  "At 500 ms a 4-player tree cut to depth 1 lost 17.4 ± 8.2 pp (<i>z</i> = 4.2) and our agent limited to 100 ms "
  "9.0 ± 7.7 pp (<i>z</i> = 2.3), yet 1 000 ms (with <i>K</i> = 1.37) did not help (−4.8 ± 8.1 pp). "
  "TREESTATS_SENTENCE")
h2("What Did Not Help")
p("LAST_STUDY_PARAGRAPH")

# ------------------------------------------------------------------ V
h1("Conclusions and Future Work")
p("CONCLUSION_1")
p("CONCLUSION_2")

B.append(("h5", "Use of Generative AI"))
B.append(("pai", "We used Claude {claude} (Anthropic), through the Claude Code tool, throughout the project: to write "
          "and modify the Java code of our agent on top of BasicMCTS, to write and run the experiment and statistics "
          "scripts, and to draft all sections, tables and figures of this paper from the logs of our runs; every "
          "number comes from the result files in our repository (folders experiments_v3 to experiments_v5). The "
          "authors take full responsibility for the code and the paper, including any errors. The paper was "
          "generated with the prompt (original Turkish, then English translation): <i>“simdi reponun icindeki butun "
          "herseyi incele ve hocaya direk yuklyecegimiz raporuda olusutr repoda ornekler tipler brief hersey var 100 "
          "alcagimiz bir rapor olsun her raporu tamaladiginda baska bir agent spawn edip bagimsiz durusstce kontrol "
          "taki yuzde yuz alacagana emin olankadar looptan cikma”</i> – “Now examine everything in the repository and "
          "also create the report we will upload directly to the lecturer; the repository has examples, tips, the "
          "brief, everything. It should be a report that gets 100. Every time you finish the report, spawn another "
          "agent to check it independently and impartially, and do not leave the loop until you are sure it will get "
          "100%.” Following that prompt, each draft was marked by a second, independent Claude instance against the "
          "module's rubric, and its findings were used to revise the text and to run the held-out validation, the "
          "comparison with TAG's DUCT agent, the lead-heuristic ablation and the four-player diagnostics. The "
          "experiments of the last study were run with the prompt “The lecturer will hold a tournament of our agents "
          "in class; as before, improve the agents' performance quickly while following the lecturer's restrictions "
          "and recording what we did in the README; you have 4.5 hours, test everything in parallel.” The prompts of "
          "earlier development sessions are not reproduced here; they asked Claude to implement and test the "
          "modifications of Table I and to run the experiments of Section IV."))
B.append(("h5", "References"))
B.append(("refs",))


# ----------------------------------------------------------------------------------------------- dynamic text
def text_fspeed():
    if not DIAG or not DIAG["itF"] or not DIAG["itNoF"]:
        return ""
    return (f"At 500 ms this gives {num(DIAG['itF'])} iterations of 8 rollouts per decision against "
            f"{num(DIAG['itNoF'])} single-rollout iterations without F and L.")


def text_timing():
    return ("1 s limit: 900 ms CPU budget, 925 ms wall-clock guard; 500 ms limit: 430 and 450 ms. Default JVM: 1 of "
            "73 decisions over 1 s of wall-clock time (1.004 s); 2 GB heap: up to 1.46 s")


def text_iterations():
    b = [x for x in (100, 500, 900) if (x, "v3") in ITER and (x, "v4") in ITER]
    if not b:
        return ""
    x = b[-1]
    return (f"It is not a lack of search: at {x} ms v3 ran {num(ITER[(x, 'v3')])} iterations per decision, v4 "
            f"{num(ITER[(x, 'v4')])}.")


def text_seats():
    a, b = OLDSEATS["3p100"].get("tagmcts.json"), OLDSEATS["3p500"].get("tagmcts.json")
    if a is None or b is None:
        return ""
    return (f"Part of every agent's decline comes from the opponents: the TAG ISMCTS seats won {a:.1f}% of v4's "
            f"validation games at 100 ms but {b:.1f}% at 500 ms.")


def text_final_agent():
    if VAL["3p100"] is None:
        return "[results of the held-out runs pending]"
    three = [(k, ms) for k, ms in (("3p100", "100"), ("3p500", "500"), ("3p900", "900")) if VAL[k]]
    s = ("Table III reports the final agent on held-out deals. Against v2 and TAG's ISMCTS it wins "
         + ", ".join(f"{v(k)}%" for k, _ in three[:-1]) + (" and " if len(three) > 1 else "")
         + f"{v(three[-1][0])}% at " + ", ".join(ms for _, ms in three[:-1]) + (" and " if len(three) > 1 else "")
         + f"{three[-1][1]} ms with three players")
    four = [(k, ms) for k, ms in (("4p100", "100"), ("4p500", "500")) if VAL[k]]
    if four:
        s += (", and " + " and ".join(f"{v(k)}%" for k, _ in four) + " at " + " and ".join(ms for _, ms in four)
              + " ms with four")
    s += "."
    o = OLD["3p100"]
    d = VAL["3p100"]["win"] - o["win"]
    ci = 1.96 * _math.sqrt(VAL["3p100"]["win"] * (100 - VAL["3p100"]["win"]) / VAL["3p100"]["n"]
                           + o["win"] * (100 - o["win"]) / o["n"])
    s += (f" On seed block 7, which overlaps the development deals, it had won {o['win']:.1f}% at 100 ms "
          f"({pp(d, ci)}), so we see no sign that the development overfitted its deals.")
    if DUCT:
        dd, ii = DUCT_SEATS.get("b100_tagduct.json"), DUCT_SEATS.get("tagmcts.json")
        if dd is not None and ii is not None:
            s += (f" TAG's DUCT agent, which has decoupled selection and information sets but neither our card-value "
                  f"model nor round-end rollouts, wins only {dd:.1f}% of its seats when it replaces v2 (ISMCTS "
                  f"{ii:.1f}%, our agent {DUCT['win']:.1f}%).")
    m = max(BSEATS.get("fw_BasicMCTS.json", 0), BSEATS.get("fw_RHEA.json", 0), BSEATS.get("osla_fw_BasicMCTS.json", 0))
    s += (f" BasicMCTS, RHEA and OSLA win at most {m:.1f}% of their seats; OSLA wins "
          f"{'none' if BSEATS.get('osla_fw_OSLA.json', 1) == 0 else 'almost none'}, as cards are revealed only when "
          "all players have chosen, so its look-ahead rarely sees a change of score.")
    return s


def text_lead():
    if LEAD is None:
        return ""
    if LEAD["zw"] <= -2:
        return (f"Replacing H by TAG's lead heuristic (own score minus the best opponent's, <i>K</i> = 7.5) cost "
                f"{-LEAD['dw']:.1f} ± {LEAD['dwci']:.1f} pp (<i>z</i> = {abs(LEAD['zw']):.1f}), about half as much: "
                "valuing the lead instead of the own score accounts for about half of H's effect, and the Pudding "
                "standing, the term for the mean opponent and the bounded scale that H adds at the round-end leaves "
                "for the rest.")
    return (f"Replacing H by TAG's lead heuristic (own score minus the best opponent's, <i>K</i> = 7.5) changed the "
            f"win rate by {dpp(LEAD)}: valuing the lead recovers most of H's effect.")


def text_treestats():
    if not DIAG:
        return ""
    t1000, l16, rerun = 100 - DIAG["t1000"], 100 - DIAG["L16"], 100 - DIAG["rerun"]
    return (f"On {DIAG['states']} positions from random four-player games the 500 ms tree has on average "
            f"{DIAG['kids']} joint-move children at the root, {DIAG['once']}% of them visited once, and a mean depth "
            f"of {DIAG['depth']:.1f}; doubling the time or the rollouts per leaf changed the chosen card in "
            f"{t1000:.1f}% and {l16:.1f}% of the positions, no more often than re-running the search "
            f"({rerun:.1f}%).")


def text_last_study():
    x3, n05, v2d, w1, r3 = (LAST[k] for k in ("X3", "N05", "V2D", "W1", "R3V2"))
    s = ("The last study (three players, Fig. 3 bottom) changed the model and the value. Card constants tuned for "
         "three players "
         "(X3) make a far stronger greedy policy – it wins 45.0% of greedy games against two X0 players – yet did "
         f"not strengthen the search ({dpp(x3, False)}), consistent with simulation balancing {{silver}}. Denial "
         "(“hate drafting”: simulated players also value taking the next player's best card) gained about 1 pp in "
         f"greedy play and nothing in the search ({dpp(n05, False)}); a win-gap or leader-only value did not change "
         f"the win rate ({v2d['dw']:+.1f}, {w1['dw']:+.1f} pp), and rollouts to the game end with a win-gap value "
         f"cost {-r3['dw']:.1f} ± {r3['dwci']:.1f} pp (<i>z</i> = {abs(r3['zw']):.1f}).")
    return s.replace("(-", "(−").replace(", -", ", −")


def text_fig3_caption():
    return ("Paired effects with three players at 100 ms (seed 7, 500 games each), ± 95% CI. Top: one component "
            f"removed from the final agent (base: final agent, {OLD['3p100']['win']:.1f}%). Bottom: the lead-heuristic "
            f"ablation and the changes of our last study, against a re-run of the final agent on the 4-core machine "
            f"({BASE5['win']:.1f}%).")


def text_conclusion_1():
    s = ("We adapted TAG's BasicMCTS to Sushi Go! with a card-value model for evaluation and simulation, round-end "
         "rollouts, max-n backups, decoupled UCT and information-set search. For RQ1, the heuristic (28.3 pp when "
         "removed; a plain lead recovers half of it) and the decoupled information-set tree (10.2 pp) contribute "
         "most, and round-end rollouts led v3's factorial (+8.7 pp).")
    if VAL["3p100"]:
        s += f" On held-out deals the final agent wins {v('3p100')}% of three-player games at 100 ms"
        if VAL["3p900"]:
            s += f" and {v('3p900')}% at 900 ms"
        s += " against TAG's ISMCTS and our previous agent."
    s += (" For RQ2, the decoupled tree loses less strength than the sequential one at longer budgets (−8.1 against "
          "−17.3 pp from 100 to 900 ms), though not significantly. For RQ3, the best settings differ between three "
          "and four players (<i>z</i> = 2.9), and with four players extra time rarely changes the decision of the "
          "wide, shallow joint-move tree.")
    return s


def text_conclusion_2():
    return ("The study has limits: our opponents are framework agents and our own previous version; the long-budget "
            "validation and the 4-player 500 ms development rest on 250 games (about ±6 pp); the factorial and budget "
            "studies used independent rather than paired deals; and CPU-time budgets hide wall-clock effects such as "
            "garbage collection. As hand-tuned changes to the model no longer move the agent, future work should "
            "learn the evaluation from game outcomes, e.g. by regressing final ranks on features of round-end states. "
            "Regret matching or EXP3 could replace UCB in the decoupled selection {lanctot}, as the best variant "
            "depends on the game {tak}, and opponent models fitted to the observed picks of real opponents could "
            "replace the shared greedy policy, which models all players alike.")


PLACEHOLDERS = {
    "FINAL_AGENT_PARAGRAPH": text_final_agent, "LEAD_SENTENCE": text_lead, "ITERATIONS_SENTENCE": text_iterations,
    "SEATS_SENTENCE": text_seats, "TREESTATS_SENTENCE": text_treestats, "TIMING_SENTENCE": text_timing,
    "FSPEED_SENTENCE": text_fspeed, "LAST_STUDY_PARAGRAPH": text_last_study, "FIG3_CAPTION": text_fig3_caption,
    "CONCLUSION_1": text_conclusion_1, "CONCLUSION_2": text_conclusion_2,
}


# ----------------------------------------------------------------------------------------------- helpers
def number_citations(blocks):
    order = []

    def walk(text):
        for k in re.findall(r"\{(\w+)\}", text):
            if k not in REFS:
                sys.exit(f"unknown citation {k}")
            if k not in order:
                order.append(k)

    for b in blocks:
        if b[0] in ("p", "bullet", "pai"):
            walk(b[1])
        elif b[0] == "table":
            for row in b[4]:
                for c in row:
                    walk(c)
            walk(b[1])
        elif b[0] == "fig":
            walk(b[3])
    num = {k: i + 1 for i, k in enumerate(order)}
    unused = [k for k in REFS if k not in num]
    if unused:
        sys.exit(f"references never cited: {unused}")
    return order, num


def cite(text, num):
    text = re.sub(r"\{(\w+)\}, \{(\w+)\}", lambda m: f"[{num[m.group(1)]}], [{num[m.group(2)]}]", text)
    return re.sub(r"\{(\w+)\}", lambda m: f"[{num[m.group(1)]}]", text)


TOKEN = re.compile(r"(<i>|</i>|<b>|</b>|<sub>|</sub>|<sup>|</sup>)")


def add_runs(par, text, size=None, font=None, bold=None, italic=None):
    """Adds text with <i>, <b>, <sub>, <sup> markup as runs of paragraph par."""
    st = {"i": False, "b": False, "sub": False, "sup": False}
    for tok in TOKEN.split(text):
        if not tok:
            continue
        m = re.fullmatch(r"<(/?)(i|b|sub|sup)>", tok)
        if m:
            st[m.group(2)] = m.group(1) == ""
            continue
        r = par.add_run(tok)
        if st["i"] or italic:
            r.italic = True
        if st["b"] or bold:
            r.bold = True
        if st["sub"]:
            r.font.subscript = True
        if st["sup"]:
            r.font.superscript = True
        if size:
            r.font.size = Pt(size)
        if font:
            r.font.name = font
            r._element.rPr.rFonts.set(qn("w:cs"), font)
            r._element.rPr.rFonts.set(qn("w:eastAsia"), font)
    return par


def sty(doc, name):
    """Style by its displayed name (LibreOffice writes 'Heading 1', which python-docx's lookup does not find)."""
    for s in doc.styles:
        if s.name == name:
            return s
    raise KeyError(name)


def new_par(doc, anchor, style):
    par = doc.add_paragraph()
    par.style = sty(doc, style)
    anchor.addprevious(par._p)
    return par


def set_tabs(par, stops):
    ppr = par._p.get_or_add_pPr()
    tabs = OxmlElement("w:tabs")
    for kind, pos in [("clear", 720), ("clear", 2520), ("clear", 5040)] + stops:
        t = OxmlElement("w:tab")
        t.set(qn("w:val"), kind)
        t.set(qn("w:pos"), str(pos))
        tabs.append(t)
    ppr.append(tabs)


def border(el, tag, val="single", sz=6):
    b = OxmlElement(f"w:{tag}")
    b.set(qn("w:val"), val)
    b.set(qn("w:sz"), str(sz))
    b.set(qn("w:space"), "0")
    b.set(qn("w:color"), "000000")
    el.append(b)


def add_table(doc, anchor, caption, widths, header, rows, note, num):
    cap = new_par(doc, anchor, "table head")
    cap.add_run(caption)
    cap.paragraph_format.keep_with_next = True
    t = doc.add_table(rows=len(rows) + 1, cols=len(header))
    anchor.addprevious(t._tbl)
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    tblPr = t._tbl.tblPr
    borders = OxmlElement("w:tblBorders")
    border(borders, "top", sz=8)
    border(borders, "bottom", sz=8)
    tblPr.append(borders)
    t.autofit = False
    layout = OxmlElement("w:tblLayout")
    layout.set(qn("w:type"), "fixed")
    tblPr.append(layout)
    tblW = tblPr.find(qn("w:tblW"))
    if tblW is None:
        tblW = OxmlElement("w:tblW")
        tblPr.append(tblW)
    tblW.set(qn("w:w"), str(int(sum(widths) * 1440)))
    tblW.set(qn("w:type"), "dxa")
    grid = t._tbl.tblGrid
    for gc, w in zip(grid.findall(qn("w:gridCol")), widths):
        gc.set(qn("w:w"), str(int(w * 1440)))
    for ci, w in enumerate(widths):
        for row in t.rows:
            row.cells[ci].width = Inches(w)
    for ri, values in enumerate([header] + rows):
        for ci, v in enumerate(values):
            cell = t.rows[ri].cells[ci]
            par = cell.paragraphs[0]
            par.style = sty(doc, "table col head" if ri == 0 else "table copy")
            par.alignment = WD_ALIGN_PARAGRAPH.LEFT if ci <= 1 else WD_ALIGN_PARAGRAPH.CENTER
            add_runs(par, cite(v, num))
            tcPr = cell._tc.get_or_add_tcPr()
            mar = OxmlElement("w:tcMar")
            for side in ("left", "right"):
                e = OxmlElement(f"w:{side}")
                e.set(qn("w:w"), "50")
                e.set(qn("w:type"), "dxa")
                mar.append(e)
            tcPr.append(mar)
            if ri == 0:
                tb = OxmlElement("w:tcBorders")
                border(tb, "bottom", sz=6)
                tcPr.append(tb)
    for row in t.rows[:-1]:
        for cell in row.cells:
            for par in cell.paragraphs:
                par.paragraph_format.keep_with_next = True
        trPr = row._tr.get_or_add_trPr()
        cs = OxmlElement("w:cantSplit")
        trPr.append(cs)
    if note:
        for cell in t.rows[-1].cells:
            for par in cell.paragraphs:
                par.paragraph_format.keep_with_next = True
        np_ = new_par(doc, anchor, "table copy")
        np_.alignment = WD_ALIGN_PARAGRAPH.LEFT
        np_.paragraph_format.space_before = Pt(2)
        np_.paragraph_format.space_after = Pt(8)
        add_runs(np_, note, size=7)


def fill_placeholders():
    rep = {k: f() for k, f in PLACEHOLDERS.items()}

    def sub(t):
        for k, val in rep.items():
            t = t.replace(k, val)
        return re.sub(r"  +", " ", t).replace(" .", ".").strip()

    for i, b in enumerate(B):
        if b[0] in ("p", "pai"):
            B[i] = (b[0], sub(b[1])) + tuple(b[2:])
        elif b[0] == "fig":
            B[i] = (b[0], b[1], b[2], sub(b[3]))
        elif b[0] == "table":
            B[i] = (b[0], b[1], b[2], b[3], [[sub(c) for c in row] for row in b[4]], b[5])


def build():
    fill_placeholders()
    os.makedirs(WORK, exist_ok=True)
    subprocess.run(["soffice", "--headless", "--convert-to", "docx:MS Word 2007 XML", "--outdir", WORK, TEMPLATE],
                   check=True, capture_output=True)
    doc = docx.Document(os.path.join(WORK, "Paper_Template.docx"))
    order, num = number_citations(B)
    P = doc.paragraphs

    # title
    for r in P[0].runs[1:]:
        r._r.getparent().remove(r._r)
    P[0].runs[0].text = TITLE.split("Card-Value")[0]
    P[0].runs[0].font.name = "Times New Roman"
    P[0].runs[0]._element.rPr.rFonts.set(qn("w:cs"), "Times New Roman")
    P[0].runs[0]._element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")
    if "Card-Value" in TITLE:
        tail = copy.deepcopy(P[0].runs[0]._r)
        for t in tail.findall(qn("w:t")):
            tail.remove(t)
        t1 = OxmlElement("w:t")
        t1.text = "Card"
        tail.append(t1)
        tail.append(OxmlElement("w:noBreakHyphen"))
        t2 = OxmlElement("w:t")
        t2.set(qn("xml:space"), "preserve")
        t2.text = "Value" + TITLE.split("Card-Value")[1]
        tail.append(t2)
        P[0].runs[0]._r.addnext(tail)
    # subtitle note and empty author line of section 0
    for i in (1, 2):
        P[i]._p.getparent().remove(P[i]._p)
    # authors: three columns
    a_par = P[5]
    for i in (4, 6):
        P[i]._p.getparent().remove(P[i]._p)
    # one paragraph with column breaks between the authors, so that all three blocks start at the same height
    for r in list(a_par._p.findall(qn("w:r"))):
        a_par._p.remove(r)
    for k, lines in enumerate(AUTHORS):
        if k > 0:
            a_par.add_run().add_break(docx.enum.text.WD_BREAK.COLUMN)
        for j, line in enumerate(lines):
            r = a_par.add_run(line)
            r.font.size = Pt(9)
            r.italic = j >= 2
            if j < len(lines) - 1:
                r.add_break()
    # empty paragraphs of section 2
    for i in (8, 9):
        P[i]._p.getparent().remove(P[i]._p)
    # template body: everything between the abstract and the section break of the 2-column section
    sect_par = P[85]._p
    body = doc.element.body
    el = P[11]._p
    while el is not sect_par:
        nxt = el.getnext()
        body.remove(el)
        el = nxt
    anchor = sect_par
    # first-page footer: the IEEE copyright line of the template becomes our note on the use of generative AI
    fpars = doc.sections[0].first_page_footer.paragraphs
    for par in fpars:
        for r in par.runs:
            r.text = ""
    note = cite(FIRST_PAGE_NOTE, num)
    fp = fpars[0]
    fp.alignment = WD_ALIGN_PARAGRAPH.LEFT
    add_runs(fp, note, size=7.5)
    # top margins as in the module template: 27 pt above the title, 54 pt on the following (body) pages
    for k, sec in enumerate(doc.sections):
        sec.top_margin = Pt(27 if k == 0 else 54)

    ab = new_par(doc, anchor, "Abstract")
    r = ab.add_run("Abstract—")
    r.italic = True
    add_runs(ab, ABSTRACT)
    kw = new_par(doc, anchor, "Keywords")
    kw.add_run("Keywords—")
    r = kw.add_run(KEYWORDS)
    r.bold = False

    for b in B:
        kind = b[0]
        if kind == "h1":
            new_par(doc, anchor, "Heading 1").add_run(b[1])
        elif kind == "h2":
            new_par(doc, anchor, "Heading 2").add_run(b[1])
        elif kind == "h5":
            hp = new_par(doc, anchor, "Heading 5")
            hp.paragraph_format.keep_with_next = True
            hp.add_run(b[1])
        elif kind == "p":
            add_runs(new_par(doc, anchor, "Body Text"), cite(b[1], num))
        elif kind == "pai":
            add_runs(new_par(doc, anchor, "Body Text"), cite(b[1], num))
        elif kind == "bullet":
            par = new_par(doc, anchor, "bullet list")
            ppr = par._p.get_or_add_pPr()
            numpr = OxmlElement("w:numPr")
            for tag, val in (("w:ilvl", "0"), ("w:numId", "2")):
                e = OxmlElement(tag)
                e.set(qn("w:val"), val)
                numpr.append(e)
            ppr.append(numpr)
            ind = OxmlElement("w:ind")
            ind.set(qn("w:left"), "576")
            ind.set(qn("w:hanging"), "288")
            ppr.append(ind)
            add_runs(par, cite(b[1], num))
        elif kind == "eq":
            par = new_par(doc, anchor, "equation")
            set_tabs(par, [("center", 2438), ("right", 4877)])
            par.paragraph_format.space_before = Pt(4)
            par.paragraph_format.space_after = Pt(4)
            par.add_run("\t")
            add_runs(par, b[1], font="Times New Roman")
            r = par.add_run(f"\t({b[2]})")
            r.font.name = "Times New Roman"
        elif kind == "table":
            add_table(doc, anchor, b[1], b[2], b[3], b[4], b[5], num)
        elif kind == "fig":
            par = new_par(doc, anchor, "Normal")
            par.alignment = WD_ALIGN_PARAGRAPH.CENTER
            par.paragraph_format.space_before = Pt(6)
            par.paragraph_format.keep_with_next = True
            par.add_run().add_picture(os.path.join(HERE, b[1]), width=Inches(b[2]))
            add_runs(new_par(doc, anchor, "figure caption"), cite(b[3], num))
        elif kind == "refs":
            for k in order:
                add_runs(new_par(doc, anchor, "references"), REFS[k])

    doc.core_properties.title = TITLE
    doc.core_properties.author = "ECS7032P Group A"
    doc.save(OUT + ".docx")
    pdf_filter = ('pdf:writer_pdf_Export:{"UseLosslessCompression":{"type":"boolean","value":"true"},'
                  '"ReduceImageResolution":{"type":"boolean","value":"false"}}')
    subprocess.run(["soffice", "--headless", "--convert-to", pdf_filter, "--outdir", HERE, OUT + ".docx"],
                   check=True, capture_output=True)
    print("written", OUT + ".docx", OUT + ".pdf")
    words = lambda t: len(re.sub(r"<[^>]+>|\{\w+\}", "", t).split())
    print("abstract words:", words(ABSTRACT))
    sec, counts = None, {}
    for b in B:
        if b[0] in ("h1", "h5"):
            sec = b[1]
        elif b[0] in ("p", "bullet") and sec:
            counts[sec] = counts.get(sec, 0) + words(b[1])
    print("words per section:", counts)


if __name__ == "__main__":
    build()
