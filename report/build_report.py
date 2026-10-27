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

# ----------------------------------------------------------------------------------------------- content
TITLE = "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!"

AUTHORS = [  # three columns, as in the template; fill in the group's names and student numbers
    ["Halit Ozger", "Student no. [to be completed]", "Queen Mary University of London", "London, UK"],
    ["[Group member 2]", "Student no. [to be completed]", "Queen Mary University of London", "London, UK"],
    ["[Group member 3]", "Student no. [to be completed]", "Queen Mary University of London", "London, UK"],
]

ABSTRACT = (
    "In the card-drafting game Sushi Go! two to five players pick cards simultaneously from hidden hands that are "
    "passed around the table, so a planning agent faces hidden information, simultaneous moves and delayed "
    "scoring. Starting from TAG's BasicMCTS, we added a model of the game's scoring that drives both "
    "the evaluation and the rollouts, rollouts that end with the round, max-n backups, decoupled UCT for the "
    "simultaneous choices and single-observer information-set search. In 500 three-player games at 100 ms per "
    "decision the agent wins 55.0% [50.6, 59.3] against TAG's tuned ISMCTS and our previous agent (chance 33.3%) "
    "and 84–87% against BasicMCTS, RHEA and OSLA. Paired ablations show that the heuristic and the game-matched "
    "tree matter most, whereas a much stronger rollout policy does not help.")

KEYWORDS = "Monte Carlo tree search, information sets, simultaneous moves, card games, Sushi Go!, TAG"

REFS = {
    "tag": "R. D. Gaina, M. Balla, A. Dockhorn, R. Montoliu, and D. Perez-Liebana, “TAG: A tabletop games "
           "framework,” in <i>Proc. AIIDE Workshop on Experimental AI in Games (EXAG)</i>, 2020.",
    "klang": "C.-M. E. Klang, V. Enhörning, A. Alvarez, and J. Font, “Assessing simultaneous action selection and "
             "complete information in TAG with Sushi Go!,” in <i>Proc. IEEE Conf. Games (CoG)</i>, 2021, pp. 1–4.",
    "browne": "C. B. Browne <i>et al.</i>, “A survey of Monte Carlo tree search methods,” <i>IEEE Trans. Comput. "
              "Intell. AI Games</i>, vol. 4, no. 1, pp. 1–43, 2012.",
    "kocsis": "L. Kocsis and C. Szepesvári, “Bandit based Monte-Carlo planning,” in <i>Proc. 17th Eur. Conf. "
              "Mach. Learn. (ECML)</i>, 2006, pp. 282–293.",
    "auer": "P. Auer, N. Cesa-Bianchi, and P. Fischer, “Finite-time analysis of the multiarmed bandit problem,” "
            "<i>Mach. Learn.</i>, vol. 47, no. 2–3, pp. 235–256, 2002.",
    "gelly": "S. Gelly and D. Silver, “Combining online and offline knowledge in UCT,” in <i>Proc. 24th Int. Conf. "
             "Mach. Learn. (ICML)</i>, 2007, pp. 273–280.",
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
    "cowling": "P. I. Cowling, E. J. Powley, and D. Whitehouse, “Information set Monte Carlo tree search,” "
               "<i>IEEE Trans. Comput. Intell. AI Games</i>, vol. 4, no. 2, pp. 120–143, 2012.",
    "lanctot": "M. Lanctot, V. Lisý, and M. H. M. Winands, “Monte Carlo tree search in simultaneous move games "
               "with applications to Goofspiel,” in <i>Computer Games (CGW 2013)</i>, Commun. Comput. Inf. Sci., "
               "vol. 408. Cham, Switzerland: Springer, 2014, pp. 28–43.",
    "tak": "M. J. W. Tak, M. Lanctot, and M. H. M. Winands, “Monte Carlo tree search variants for simultaneous "
           "move games,” in <i>Proc. IEEE Conf. Comput. Intell. Games (CIG)</i>, 2014, pp. 1–8.",
    "sturtevant": "N. R. Sturtevant, “An analysis of UCT in multi-player games,” in <i>Computers and Games "
                  "(CG 2008)</i>, Lect. Notes Comput. Sci., vol. 5131. Berlin, Germany: Springer, 2008, pp. 37–49.",
    "wilson": "E. B. Wilson, “Probable inference, the law of succession, and statistical inference,” <i>J. Amer. "
              "Statist. Assoc.</i>, vol. 22, no. 158, pp. 209–212, 1927.",
}

# Body: list of blocks. ("h1", text) ("h2", text) ("p", text) ("bullet", text) ("eq", runs, number)
# ("table", caption, widths, header, rows) ("fig", path, width_in, caption) ("h5", text) ("refs",)
B = []
h1 = lambda t: B.append(("h1", t))
h2 = lambda t: B.append(("h2", t))
p = lambda t: B.append(("p", t))
bullet = lambda t: B.append(("bullet", t))

h1("Introduction")
p("Tabletop games are a long-standing test bed for game AI: they combine chance, hidden information and several "
  "interacting players within small, well-defined rule sets. The Tabletop Games framework (TAG) {tag} gives such "
  "games a common Java interface so that agents can be compared on them. One of its games is <i>Sushi Go!</i> "
  "(P. Walker-Harding, Gamewright, 2013): every player secretly picks one card from its hand, all players reveal "
  "their cards at the same time and pass the rest of their hands to the next player, and after three rounds the "
  "player with the most points wins. Klang et al. {klang} introduced the game to TAG and assessed simultaneous "
  "action selection and complete information in it; the current version, which we use, shows an agent only the "
  "cards it has seen.")
p("Four properties make Sushi Go! hard for a search agent. (i) The opponents' hands are hidden until they are "
  "passed on, so the true state is unknown at the start of every round. (ii) Moves are simultaneous: no card can be "
  "chosen in reaction to an opponent's card of the same turn. (iii) Most points are delayed and conditional – sets "
  "pay out only when completed, Maki rolls at the end of the round and Pudding at the end of the game – so short "
  "simulations miss them and long ones are noisy. (iv) With up to ten cards per hand and three or four players a "
  "turn has hundreds of joint moves, and an agent has 0.1–1 s per decision. TAG's BasicMCTS addresses none of the "
  "first three: it searches the single shuffled sample of the hidden cards it receives as if it were the true "
  "state, treats the players of a turn as moving one after another and evaluates random rollouts with the raw "
  "score.")
p("We ask which adaptations of Monte Carlo tree search (MCTS) to these properties make an agent stronger, and how "
  "their value changes with the time per decision and the number of players. We answer this by extending "
  "BasicMCTS in steps, keeping every modification as a switch, and measuring each in experiments of 500 games. "
  "Our contributions are:")
bullet("a model of Sushi Go!'s scoring that estimates every player's final score and every card's marginal "
       "value, used both to evaluate states and as the rollout policy of all players;")
bullet("a search matched to the game – rollouts that end with the round, max-n backups, decoupled UCT and "
       "single-observer information-set MCTS – built on BasicMCTS, which keeps its strength at long budgets, "
       "unlike our sequential-tree agent, whose win rate fell from 50% to 33% between 100 and 900 ms;")
bullet("settings and a fast simulator for four players;")
bullet("an experimental study of 161 runs (67 500 games) with leave-one-out ablations, a 2<sup>4</sup> "
       "factorial design, validation on unseen deals and negative results showing that the model of the game, not "
       "the search effort, now limits the agent.")

h1("Background")
h2("TAG and Sushi Go!")
p("TAG {tag} splits a game into a state, a forward model and parameters; an agent receives a copy of the state, in "
  "which every card it has not seen has been reshuffled, and uses the forward model to simulate moves. In TAG's "
  "Sushi Go! the 108-card deck holds Tempura (5 points per pair), Sashimi (10 per triple), Dumplings (1, 3, 6, 10 "
  "or 15 points for one to five), Salmon, Squid and Egg Nigiri (2, 3 and 1 points, tripled on an earlier Wasabi), "
  "Maki rolls with one to three icons (6 and 3 points for the most and second-most icons of a round), Chopsticks "
  "(two cards in a later turn) and Pudding (+6 for the most and, with three or more players, −6 for the fewest at "
  "the end of the game). Each "
  "round deals 12 − <i>n</i> cards to each of <i>n</i> players. As the hands rotate, every hand of a round has been "
  "seen after <i>n</i> − 1 passes, so hidden information is concentrated at the start of each round, while the "
  "value of a card depends on what the others will take from the hands we pass on.")
h2("Monte Carlo Tree Search")
p("MCTS {browne} builds a search tree by repeating four steps: selection, expansion, simulation (rollout) and "
  "backpropagation. UCT {kocsis} selects children with the UCB1 rule {auer}, maximising "
  "<i>Q</i> + <i>K</i>√(ln <i>N</i> / <i>n</i>) for a child with mean reward <i>Q</i> visited <i>n</i> of "
  "<i>N</i> times, which balances exploitation and exploration. Domain knowledge can enter through heuristic "
  "rollout policies {gelly} or through priors such as progressive bias {chaslot}. A stronger rollout policy does "
  "not guarantee better estimates, however: Silver and Tesauro {silver} showed that rollout outcomes need to be "
  "unbiased rather than produced by a strong player (simulation balancing).")
h2("Hidden Information, Simultaneous Moves and Several Players")
p("Determinization, or perfect-information Monte Carlo (PIMC) sampling, samples the hidden information, "
  "searches each sample as if it were the true state and combines the results. Long et al. {long} related its "
  "success in card games to properties such as how quickly hidden information is revealed during play, which in "
  "Sushi Go! happens within <i>n</i> − 1 turns. Its known flaw is strategy fusion: the search assumes that it can "
  "act differently in states it cannot tell apart {frank}, and in a drafting game it plans around opponent cards it "
  "cannot know. Information-set MCTS (ISMCTS) {cowling} avoids this by searching one tree over the searching "
  "player's information sets and sampling the hidden information anew in every iteration; it was developed for "
  "card games such as Dou Di Zhu. Its single-observer form treats the opponents as if they knew what the searcher "
  "knows, whereas the multiple-observer form models their knowledge with one tree per player at a higher cost "
  "{cowling}. In Sushi Go! every player knows its own hand at the start of a round and all hands after <i>n</i> − 1 "
  "passes, so the single-observer approximation errs only early in each round.")
p("Simultaneous moves are usually handled by decoupled UCT (DUCT): every player chooses its own action at a node "
  "from its own bandit statistics and the joint action leads to a child {lanctot}. With UCB selection DUCT is not "
  "guaranteed to converge to an equilibrium, so variants with EXP3 or regret matching were proposed {lanctot}, and "
  "which variant works best depends on the game {tak}; these studies used two-player games such as Goofspiel. In "
  "multi-player games the max-n backup, in which every player maximises its own value, generally outperforms the "
  "paranoid assumption that all opponents play against us {sturtevant}.")
p("BasicMCTS, the starting point allowed for this work, uses none of these ideas, and the earlier Sushi Go! study "
  "in TAG {klang} predates the version of the game with hidden hands. ISMCTS and DUCT have been studied separately "
  "and mostly in two-player games; how they combine with domain knowledge in a multi-player drafting game, what "
  "each contributes, and how this depends on the time budget and the number of players is not known – the "
  "questions this report answers.")

h1("Method")
p("Our agent, SushiMCTS, is a modified copy of TAG's BasicMCTS (package <i>players.basicMCTS</i>) and uses no code "
  "of TAG's advanced MCTS agent. Every modification is a switch in its parameter class, so all variants in our "
  "experiments run the same code. Table I lists the modifications and Fig. 1 shows one iteration of the final "
  "agent. Beyond the published techniques we combine, three elements are our own: one card-value model serves "
  "both the evaluation and every player's simulation, decoupled selection runs on an open-loop information-set "
  "tree whose statistics are keyed by card type, and the agent switches its settings with the number of "
  "players.")
B.append(("table", "Modifications of BasicMCTS (✓ = used in the final agent with 3 / 4 players)",
          [0.62, 2.0, 0.76],
          ["Id", "Modification", "3 p / 4 p"],
          [["H", "heuristic (2)–(3) from the card-value model (1)", "✓ / ✓"],
           ["B", "ε-greedy card-value rollouts for every player {gelly}", "✓ / ✓"],
           ["R", "rollouts end when the round is scored", "✓ / ✓"],
           ["P", "one branch per distinct card", "✓ / ✓"],
           ["M", "max-n backups {sturtevant}", "✓ / ✓"],
           ["S", "decoupled UCT, one node per turn {lanctot}", "✓ / ✓"],
           ["I", "single-observer information-set search {cowling}", "✓ / ✓"],
           ["X0", "card-model constants tuned for 4 players", "– / ✓"],
           ["W2, F, L", "win-gap value, fast simulator, 8 rollouts per leaf", "– / ✓ <sup>a</sup>"],
           ["A", "progressive bias {chaslot}", "dropped in v3"],
           ["D", "ensemble of up to 4 determinized trees {long}", "replaced by I"],
           ["N", "denial in the rollouts (Section IV-E)", "not adopted"]],
          "<sup>a</sup> at 500 ms or more per decision."))
B.append(("fig", "figures/fig1_iteration.png", 3.39,
          "One iteration of the final agent. The letters refer to Table I; after the time budget the agent plays "
          "its most visited card at the root."))
h2("Card-Value Model and Heuristic (H)")
p("For a state <i>s</i> and player <i>p</i> the model estimates the final score")
B.append(("eq", "Ŝ<sub>p</sub> = <i>S<sub>p</sub></i> + α<i>U<sub>p</sub></i> + <i>M<sub>p</sub></i> + "
                "<i>w<sub>t</sub> P<sub>p</sub></i>,", 1))
p("where <i>S<sub>p</sub></i> is the banked score (TAG scores Tempura, Sashimi, Dumplings and Nigiri when they are "
  "revealed), <i>U<sub>p</sub></i> the value of unfinished sets, each weighted by a completion factor "
  "min(1, (<i>r</i> − <i>m</i> + 1)/<i>T</i>) (0 if <i>r</i> < <i>m</i>) for a set that needs <i>m</i> more "
  "cards with <i>r</i> picks left, "
  "<i>M<sub>p</sub></i> the Maki points <i>p</i> would get if the round ended now, and <i>P<sub>p</sub></i> the "
  "Pudding points if the game ended now, weighted by <i>w<sub>t</sub></i> = <i>t</i>/3 in round <i>t</i> "
  "(α = 0.5, <i>T</i> = 4). The value of <i>s</i> for player <i>i</i> is")
B.append(("eq", "<i>H<sub>i</sub></i>(<i>s</i>) = tanh(Δ<sub><i>i</i></sub> / σ),", 2))
B.append(("eq", "Δ<sub><i>i</i></sub> = <i>w</i>(Ŝ<sub><i>i</i></sub> − max<sub><i>j</i>≠<i>i</i></sub> "
                "Ŝ<sub><i>j</i></sub>) + (1 − <i>w</i>)(Ŝ<sub><i>i</i></sub> − mean<sub><i>j</i>≠<i>i</i></sub> "
                "Ŝ<sub><i>j</i></sub>),", 3))
p("with σ = 15 and <i>w</i> = 0.7: the first term rewards beating the leader, since only first place wins, and "
  "the second keeps a gradient when the agent is far ahead or behind. The marginal value "
  "<i>mv<sub>p</sub></i>(<i>c</i>) of a card <i>c</i> is the change of Ŝ<sub><i>p</i></sub> when <i>p</i> plays it.")
h2("Simulation (B, R)")
p("<b>B:</b> every player plays its card with the highest <i>mv</i>, or a random card with probability "
  "ε = 0.2; the same policy is our model of the opponents. <b>R:</b> a rollout stops when the current round has "
  "been scored. After that only Pudding carries over, which (1) captures, whereas simulating the later rounds "
  "would add variance.")
h2("Tree (P, M, S, I)")
p("<b>P:</b> identical cards lead to identical states, so a node has one branch per distinct card. <b>M:</b> a "
  "node stores one value per player and every player maximises its own value {sturtevant}. <b>S:</b> one node "
  "represents a whole turn. At a node every player <i>p</i> chooses a card")
B.append(("eq", "<i>a<sub>p</sub></i> = argmax<sub><i>a</i></sub> [<i>Q<sub>p</sub></i>(<i>a</i>) + "
                "<i>K</i>√(ln <i>N</i> / <i>n<sub>p</sub></i>(<i>a</i>))]", 4))
p("from its own statistics, trying untried cards first; the joint choice is played in the forward model and leads "
  "to the child of that joint action {lanctot}. In our previous agent (v3) the players of a turn appeared one after "
  "another in the tree, so a later player “saw” the card an earlier player had just chosen, which is impossible in "
  "the real game. <b>I:</b> every iteration starts from a new sample of the cards we cannot see and walks one "
  "shared open-loop tree whose nodes store statistics but no states {cowling}. Statistics are keyed by card type, "
  "and <i>n<sub>p</sub></i>(<i>a</i>) counts the iterations in which <i>p</i> held card <i>a</i>. I replaces the "
  "determinization ensemble D of v2 and v3, which split the budget between up to four independent trees. With "
  "three players <i>K</i> = 0.5; after the budget the agent plays its most visited root card.")
h2("Four Players")
p("With four players the best settings differ (Section IV-D), so the agent switches parameters by table size. "
  "<b>X0:</b> card-model constants (Sashimi discount, Maki standing weight, Dumpling, Pudding and Nigiri values, "
  "counting the cards still in the hands) found by a search-free coordinate tuner that plays greedy card-value "
  "policies against each other. At 500 ms or more the 4-player agent also uses <b>W2:</b> <i>w</i> = 1, finished "
  "games valued by tanh((<i>S<sub>i</sub></i> − max<sub><i>j</i></sub> <i>S<sub>j</sub></i>)/2) and σ shrinking "
  "with the rounds left; <b>F:</b> TAG's Sushi Go! rules re-implemented on integer arrays and checked move by move "
  "against the forward model, which runs 7–11 times more iterations, spent on <b>L</b> = 8 rollouts per leaf; and "
  "<i>K</i> = 1.")
h2("Experimental Protocol")
p("The opponents are our previous agent v2 (H with progressive bias, B and D; <i>K</i> = 1.41) and TAG's tuned "
  "ISMCTS (<i>players.mcts</i> with information sets, one tree per player, UCB-Tuned, <i>K</i> = 1, MAST and "
  "random rollouts), used only as an opponent; further runs use BasicMCTS (<i>K</i> = 1.41, 10-move random "
  "rollouts), RHEA (horizon 10, population 10) and OSLA with TAG's score heuristic. All agents get the same "
  "CPU-time budget per decision: 100 ms for development and 500 and 900 ms for validation (the competition allows "
  "1 s). In game <i>g</i> of a run our agent sits in seat <i>g</i> mod <i>n</i>, the other seats alternate "
  "between the opponents, and the game uses seed <i>s</i> + <i>g</i>. Since the v4 study the cards are dealt from "
  "a fixed deck order shuffled with this seed, so runs with the same seed play identical deals and seats and are "
  "compared game by game; earlier runs (all v3 results and Fig. 2) used TAG's own deal, which differs between "
  "runs, and are compared as independent samples. Development used seeds 1 and 2 (and 1001–3001 in v3); the final "
  "agent was validated on seed 7, which no decision used. Each condition has 500 games, except "
  "250 at 900 ms and in some 4-player 500 ms runs, where one game costs 48–73 s of CPU time (about 100 runs on an "
  "18-core laptop, 8 games in parallel).")
p("Each game is an independent trial whose outcome for our agent is 1 for a win, 1/<i>k</i> for a <i>k</i>-way "
  "shared first place and 0 otherwise. Win rates are reported with Wilson 95% intervals {wilson}, the score margin "
  "(our score minus the best opponent's) as mean ± 1.96 standard errors (SE), and differences between runs on the "
  "same deals as the mean of the per-game differences ± 1.96 SE, with <i>z</i> = mean/SE. With 500 games a win "
  "rate near 50% is known to ±4.4 points; as the outcomes of two agents on the same deal correlate by only "
  "0.1–0.2, a paired difference has a confidence interval of about ±5.7 points. The score margin is about twice "
  "as sensitive and served for screening. We expected (H1) every component to improve both measures, (H2) the "
  "game-matched tree to matter more at longer budgets and (H3) the best settings to depend on the number of "
  "players.")

h1("Results")
h2("From v2 to v4")
p("Fig. 2 traces the development with three players. Removing progressive bias from v2 and adding P, R, M and "
  "<i>K</i> = 0.5 doubled the win rate at 100 ms (24.8% → 50.1%). In a 2<sup>4</sup> factorial design on top of "
  "H + B + D + P (16 cells × 500 games at 100 ms) the main effects were +8.7 ± 2.1 points for R (<i>z</i> = 8.1), "
  "+5.2 ± 2.2 for <i>K</i> = 0.5 instead of 1.41, +4.1 ± 2.0 for M and +3.6 ± 2.0 for a completion horizon "
  "<i>T</i> = 8, with a positive M × <i>K</i> interaction (+3.2 ± 2.2): max-n backups need less exploration. "
  "Progressive bias did not help once B was present (−1.2 ± 4.3 points over 2 × 500 games), and letting the "
  "opponents in the tree follow the greedy card-value policy cost 16.9 ± 5.8 points.")
B.append(("fig", "figures/fig2_budget.png", 3.39,
          "Win rate with three players against v2 and TAG's ISMCTS by time budget (development runs, seed 1, independent "
          "deals). "
          "v2 = H + A + B + D, v3 = H + B + D + P + R + M, v4 = v3 + S + I − D. Bars: Wilson 95% intervals."))
p("v3, however, got worse as the budget grew (50.1% → 40.7% → 32.8% at 100, 500 and 900 ms), whereas v4 kept "
  "its strength (55.7%, 49.7%, 47.6%), so the gain from S and I grows with the budget (+5.6, +9.0 and +14.8 "
  "points), as H2 predicts. Our explanation is that v3's sequential tree let later players react to the cards "
  "earlier players had chosen in the same turn; the more iterations, the more its decisions relied on this wrong "
  "model, which decoupled UCT removes.")
h2("Final Agent")
B.append(("table", "Final agent on unseen deals (seed 7)",
          [0.58, 1.06, 0.28, 0.9, 0.6],
          ["Setting", "Opponents", "<i>n</i>", "Win % [95% CI]", "Margin"],
          [["3 p, 100 ms", "v2, ISMCTS", "500", "55.0 [50.6, 59.3]", "+1.00 ± 0.44"],
           ["3 p, 500 ms", "v2, ISMCTS", "500", "49.4 [45.0, 53.8]", "+0.40 ± 0.39"],
           ["3 p, 900 ms", "v2, ISMCTS", "250", "56.0 [49.8, 62.0]", "+1.06 ± 0.62"],
           ["4 p, 100 ms", "v2, ISMCTS", "500", "46.8 [42.5, 51.2]", "−0.28 ± 0.46"],
           ["4 p, 500 ms", "v2, ISMCTS", "500", "41.4 [37.2, 45.8]", "−1.53 ± 0.46"],
           ["3 p, 100 ms", "ISMCTS × 2 <sup>a</sup>", "300", "54.2 [48.5, 59.7]", "+1.66 ± 0.69"],
           ["3 p, 100 ms", "BasicMCTS, RHEA", "500", "84.2 [80.7, 87.1]", "+6.68 ± 0.60"],
           ["3 p, 100 ms", "OSLA, BasicMCTS", "500", "87.2 [84.0, 89.8]", "+9.74 ± 0.75"]],
          "Chance: 33.3% (3 players), 25% (4 players). Margin: our score minus the best opponent's, ± 95% CI. "
          "<sup>a</sup> run on a 4-core machine (Section IV-E)."))
p("Table II reports the final agent on deals not used during development. Against v2 and TAG's ISMCTS it wins "
  "49–56% of the games with three players (chance 33.3%) and 41–47% with four (chance 25%); against two TAG ISMCTS "
  "agents it still wins 54.2%, and against BasicMCTS, RHEA and OSLA 84–87%. With four players its mean margin to "
  "the best opponent is slightly negative although it wins far more often than chance: it trades margin for first "
  "places.")
h2("Ablation")
B.append(("fig", "figures/fig3_effects.png", 3.39,
          "Paired effects on the final agent with three players at 100 ms (seed 7, 500 games each), ± 95% CI. Top: "
          "one component removed. Bottom: additions tested in our last study (Section IV-E)."))
p("Fig. 3 (top) removes one component at a time from the final agent. Every removal lowers both measures, and on "
  "the score margin every difference is significant (|<i>z</i>| ≥ 2.2), supporting H1. The heuristic matters most "
  "(−28.3 ± 5.8 points without it): TAG's score heuristic ignores unfinished sets and the Maki and Pudding "
  "majorities that (1) values. S and I together are worth 10.2 ± 5.7 points (<i>z</i> = 3.5) and R 6.2 ± 5.9. "
  "The components also change the style of play: at 500 ms (500 games each) v3 scored fewer points per game than "
  "v2 from Sashimi (−2.7) and Maki (−1.9) but more from Dumplings (+1.9) and Pudding (+1.3), sets that cannot fail "
  "or are decided over the whole game.")
h2("Four Players and the Limits of Search")
p("With four players the 3-player agent is weaker (46.4% at 100 ms, 38.4% at 500 ms; chance 25%); X0 raises it "
  "to 49.6% at 100 ms and X0 + W2 to 45.6% at 500 ms (250 games), and F + L gives 43.8% on other deals. These "
  "settings do not transfer back: with three players at 500 ms, adding X0 + W2 to F + L costs 7.7 ± 5.4 points "
  "(<i>z</i> = 2.8), supporting H3. More search helps little. At four players and 500 ms the tree has about 12 000 iterations but "
  "700 joint actions at the root, 45% of the depth-1 nodes are visited once and the mean depth is 1.6; two searches "
  "with different seeds choose the same card in 93–96% of 74 test positions, doubling the time or the rollouts per "
  "leaf changes as few decisions, and giving only our agent 1000 ms did not raise its win rate (−4.8 ± 8.1). Yet "
  "cutting the tree to depth 1 costs 17.4 ± 8.2 points (<i>z</i> = 4.2): the opponents' answer in the next turn "
  "matters, but beyond it the evaluation, not the search, limits the agent.")
h2("What Did Not Help")
p("Our last study (Fig. 3, bottom; seed 7, 500 paired games each on a 4-core machine, where the final agent won "
  "58.1%) tested five additions. Card-model constants tuned for three players with the same greedy tuner (X3) make "
  "a far stronger greedy policy – 45.0% against two X0 players, chance 33.3% – but did not strengthen the search "
  "(−1.3 ± 5.8 points). Denial (“hate drafting”), in which simulated players also value taking the next player's "
  "best card, gained one point in greedy play but nothing in the search (−1.8 ± 5.8). A value aimed at the win gap "
  "(V2D) or only at the leader (W1) left the win rate unchanged, and rollouts to the end of the game valued by the "
  "win gap cost 23.7 ± 5.8 points (<i>z</i> = 8.0). The rollout policy thus has to give unbiased leaf values, not "
  "to play well {silver}, and rollouts must end with the round. In all timing measurements the CPU time per "
  "decision stayed below 0.91 s with the 1 s competition file (budget 900 ms, search also stopped after 925 ms of "
  "real time).")

h1("Conclusions and Future Work")
p("We adapted TAG's BasicMCTS to the structure of Sushi Go!: a card-value model for evaluation and simulation, "
  "rollouts that end with the round, max-n backups, decoupled UCT for simultaneous moves and information-set "
  "search over hidden hands. In paired ablations every component contributes and the heuristic most; matching "
  "the tree to simultaneous moves is what lets the agent use longer budgets, and four players need different "
  "settings. The final agent wins 55% of three-player games against TAG's tuned ISMCTS and our previous agent, and "
  "over 84% against BasicMCTS, RHEA and OSLA.")
p("The study has limits. All opponents are framework agents or our own, so the agent's strength against other "
  "designs is unknown; runs at 900 ms and some 4-player runs have 250 games (about ±6 points); and our last study "
  "reused the validation deals. The negative results show where progress must come from: the search is close to "
  "converged, and a stronger rollout policy, denial or a win-focused value do not help, so the model of the game is "
  "now the bottleneck. Future work should learn the evaluation from game outcomes, for example by regressing final "
  "ranks on features of round-end states; tune the model's constants by the results of the search rather than by "
  "greedy strength; and test regret-matching selection, which suits simultaneous moves better than UCB "
  "{lanctot}.")

B.append(("h5", "Use of Generative AI"))
B.append(("pai", "Claude (Anthropic), used through the Claude Code tool, was used throughout this project: to write "
          "and modify the Java code of our agent on top of BasicMCTS, to write and run the experiment and statistics "
          "scripts, and to draft the text, tables and figures of this report from the logs of our runs (repository "
          "folders experiments_v3 to experiments_v5, from which every number is taken). The group reviewed the code "
          "and the report and takes full responsibility for both. Prompt used to generate this report (original "
          "Turkish, then English translation): <i>“simdi reponun icindeki butun herseyi incele ve hocaya direk "
          "yuklyecegimiz raporuda olusutr repoda ornekler tipler brief hersey var 100 alcagimiz bir rapor olsun her "
          "raporu tamaladiginda baska bir agent spawn edip bagimsiz durusstce kontrol taki yuzde yuz alacagana emin "
          "olankadar looptan cikma”</i> – “Now examine everything in the repository and also create the report we "
          "will upload directly to the lecturer; the repository has examples, templates, the brief, everything. It "
          "should be a report that gets 100. Every time you finish the report, spawn another agent to check it "
          "independently and impartially, and do not leave the loop until you are sure it will get 100%.” The "
          "experiments of Section IV-E were run with the prompt “The lecturer will hold a tournament of our agents in "
          "class; as before, improve the agents' performance quickly while following the lecturer's restrictions and "
          "recording what we did in the README; you have 4.5 hours, test everything in parallel.”"))
B.append(("h5", "References"))
B.append(("refs",))


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


def build():
    os.makedirs(WORK, exist_ok=True)
    subprocess.run(["soffice", "--headless", "--convert-to", "docx:MS Word 2007 XML", "--outdir", WORK, TEMPLATE],
                   check=True, capture_output=True)
    doc = docx.Document(os.path.join(WORK, "Paper_Template.docx"))
    order, num = number_citations(B)
    P = doc.paragraphs

    # title
    for r in P[0].runs[1:]:
        r._r.getparent().remove(r._r)
    P[0].runs[0].text = TITLE
    P[0].runs[0].font.name = "Times New Roman"
    P[0].runs[0]._element.rPr.rFonts.set(qn("w:cs"), "Times New Roman")
    P[0].runs[0]._element.rPr.rFonts.set(qn("w:eastAsia"), "Times New Roman")
    # subtitle note and empty author line of section 0
    for i in (1, 2):
        P[i]._p.getparent().remove(P[i]._p)
    # authors: three columns
    a_par = P[5]
    for i in (4, 6):
        P[i]._p.getparent().remove(P[i]._p)
    proto = copy.deepcopy(a_par._p)
    for r in list(a_par._p.findall(qn("w:r"))):
        a_par._p.remove(r)
    pars = [a_par]
    for _ in AUTHORS[1:]:
        e = copy.deepcopy(proto)
        for r in list(e.findall(qn("w:r"))):
            e.remove(r)
        pars[-1]._p.addnext(e)
        pars.append(docx.text.paragraph.Paragraph(e, a_par._parent))
    for k, (par, lines) in enumerate(zip(pars, AUTHORS)):
        if k > 0:
            par.add_run().add_break(docx.enum.text.WD_BREAK.COLUMN)
        for j, line in enumerate(lines):
            r = par.add_run(line)
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
    # first-page footer with the IEEE copyright line
    for par in doc.sections[0].first_page_footer.paragraphs:
        for r in par.runs:
            r.text = ""

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
            new_par(doc, anchor, "Heading 5").add_run(b[1])
        elif kind == "p":
            add_runs(new_par(doc, anchor, "Body Text"), cite(b[1], num))
        elif kind == "pai":
            par = new_par(doc, anchor, "Body Text")
            par.paragraph_format.first_line_indent = Pt(0)
            add_runs(par, cite(b[1], num), size=8)
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
    subprocess.run(["soffice", "--headless", "--convert-to", "pdf", "--outdir", HERE, OUT + ".docx"],
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
