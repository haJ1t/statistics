"""Builds the three individual reflections (ECS7032P Assignment 1, group W) as PDFs in individual_reflections/.

Each text stays within the brief's limit of 300 words (the header lines with name, number and title are not counted).
Run from anywhere: python3 groupA_code/submission/make_reflections.py
"""
import os
import re
import subprocess

from docx import Document
from docx.shared import Pt, Cm

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
OUT = os.path.join(ROOT, "individual_reflections")
REPORT_TITLE = "Decoupled Information-Set MCTS with a Card-Value Model for Sushi Go!"
AI_NOTE = ("Use of generative AI: as our report states, we used Claude (Anthropic) for the code and the drafting; this "
           "reflection was also drafted with Claude from our own account of the work.")

PEOPLE = [
    ("Hakan Coskun", "241076864", [
        "In the first weeks all three of us experimented with the TAG code and each tried an agent of our own, "
        "which taught us how the framework and Sushi Go! work. As group leader I then divided the work between us "
        "so that each of us carried an equal share. My own part was research and decisions: I researched "
        "strategies for Sushi Go! and the search methods used for card games, compared them with the agents we had "
        "tried, and made the decision on which idea we would develop: BasicMCTS, the permitted base agent, extended "
        "with a model of how much each card is worth and a search that suits hidden hands and simultaneous moves.",
        "Halit then worked on this idea and turned it into our final agent, while Weronika studied the agents in "
        "depth so that our comparisons were sound.",
        "Leading the group taught me that agreeing early on one central idea, and testing it carefully, works "
        "better than trying many unrelated ones.",
    ]),
    ("Halit Ozger", "230625112", [
        "We began by each building a first agent of our own in TAG, to learn the framework and the game. Once "
        "Hakan had compared the options and we had chosen one idea, my part, an equal share of the work, "
        "was to build it. I set up our package and developed the agent as a modified copy of BasicMCTS within the "
        "module's rules: nothing from the advanced MCTS agent, no change to the framework, everything inside the "
        "groupW package. As our report states, we wrote the code with Claude; my part was to direct that work, turn "
        "the chosen design into concrete modifications, keep each one as a switch so that it could be tested on its "
        "own, and run and check the experiments. I also prepared the competition file and checked that the agent "
        "compiles and plays in a fresh copy of TAG.",
        "Weronika's research on the agents showed us how to compare ours with them properly, and Hakan's research "
        "and decision gave the code its direction.",
        "Building the idea step by step showed me how much a good evaluation of a position matters, and that more "
        "thinking time does not automatically make an agent stronger.",
    ]),
    ("Weronika Diana Podgorska", "230394735", [
        "During the first weeks each of us played with the framework and wrote a simple agent. We then shared "
        "the work equally, and my part was in-depth research on the agents, so that we could compare them properly. "
        "I studied how TAG's agents and the methods behind them work, such as BasicMCTS, information-set MCTS, "
        "decoupled UCT for simultaneous moves and the simpler framework agents, and what each of them assumes "
        "about hidden cards and the other players. This told us which agents to compare ours with and how to make "
        "the comparison fair: the same time per decision for every agent, every seat played equally often and "
        "enough games for clear results. It also helped with the technical details of our own agent.",
        "Hakan weighed up the strategies and chose our approach, and Halit built the agent from it.",
        "The research showed me that the right method depends on the game, and that a fair comparison needs as "
        "much care as the agent itself.",
    ]),
]

def words(text):
    return len(re.findall(r"\S+", text))


def build(name, number, parts):
    body = list(parts) + [AI_NOTE]
    n = sum(words(t) for t in body)
    assert n <= 300, (name, n)
    doc = Document()
    sec = doc.sections[0]
    sec.page_width, sec.page_height = Cm(21.0), Cm(29.7)
    sec.left_margin = sec.right_margin = Cm(2.5)
    sec.top_margin = sec.bottom_margin = Cm(2.5)
    style = doc.styles["Normal"]
    style.font.name = "Times New Roman"
    style.font.size = Pt(11.5)
    style.paragraph_format.space_after = Pt(8)
    style.paragraph_format.line_spacing = 1.15

    t = doc.add_paragraph()
    r = t.add_run("ECS7032P Assignment 1 – Individual Reflection")
    r.bold, r.font.size = True, Pt(15)
    t.paragraph_format.space_after = Pt(4)
    h = doc.add_paragraph()
    h.add_run(f"{name}").bold = True
    h.add_run(f"  ·  Student no. {number}  ·  Group W")
    h.paragraph_format.space_after = Pt(2)
    g = doc.add_paragraph()
    gr = g.add_run(f"Group report: {REPORT_TITLE}")
    gr.italic = True
    g.paragraph_format.space_after = Pt(14)

    for text in parts:
        p = doc.add_paragraph(text)
        p.paragraph_format.alignment = 3  # justify
    note = doc.add_paragraph()
    nr = note.add_run(AI_NOTE)
    nr.italic, nr.font.size = True, Pt(10)
    wc = doc.add_paragraph()
    wr = wc.add_run(f"Word count: {n} (without the heading)")
    wr.font.size = Pt(9)

    stem = "ECS7032P_Individual_Reflection_" + name.replace(" ", "_")
    path = os.path.join(OUT, stem + ".docx")
    doc.save(path)
    subprocess.run(["soffice", "--headless", "--convert-to", "pdf", "--outdir", OUT, path],
                   check=True, capture_output=True)
    os.remove(path)
    print(f"{stem}.pdf: {n} words")


os.makedirs(OUT, exist_ok=True)
for person in PEOPLE:
    build(*person)
