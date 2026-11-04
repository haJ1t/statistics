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
        "As group leader I organised the project and divided the work so that each of us carried an equal share. "
        "Early on we agreed to build on BasicMCTS, the permitted base agent, with every modification as a switch, so "
        "that one code base supports the ablations. With the group I set the research questions (which adaptations "
        "make the agent stronger, and how their value changes with the time per decision and the number of players) "
        "and planned the experimental protocol: 500 games per condition, 95% intervals, budgets from 100 to 900 ms, "
        "three and four players, and a final validation on deals never used in development. I also kept the report "
        "one coherent story rather than three separate projects.",
        "Halit turned our plan into working code, ran the experiments and prepared the competition files, while "
        "Weronika's research on hidden information and simultaneous moves gave the agent its technical direction.",
        "The most useful lesson was how much evidence a claim needs: with 500 games a win rate still has an interval "
        "of about ±4.4 percentage points, so several promising changes turned out to be noise. Planning the held-out "
        "validation paid off: our agent won 52.6% of three-player games on unseen deals, within sampling error of our "
        "earlier results. Next time I would fix the deal sets earlier, as our first comparisons used different deals "
        "and could not be paired.",
    ]),
    ("Halit Ozger", "230625112", [
        "We split the work equally three ways, and my part was the code. I set up TAG and our package and built the "
        "agent as a modified copy of BasicMCTS within the rules: nothing from the advanced MCTS agent, no change to "
        "the framework, everything inside the groupW package. As our report states, we wrote the code with Claude; "
        "my part was to direct that work, turn the team's design into concrete modifications (card-value rollouts, "
        "round-end rollouts, max-n backups, decoupled selection over information sets and separate four-player "
        "settings), keep each one as a switch and test it. I oversaw the experiments behind the report, 167 runs, "
        "and checked their results. I also prepared the competition file for the 1-second limit, with a wall-clock "
        "guard, and checked that the agent compiles and plays in a fresh copy of TAG.",
        "For what to build I relied on Weronika's reading of the literature, and for what to test and when, on "
        "Hakan's plan and his decisions as group leader.",
        "Building the agent taught me that the evaluation matters more than the details of the search: without our "
        "heuristic the agent lost 28.3 percentage points, more than any change to the search gained. I also learned "
        "that more thinking time does not automatically help: our lead shrank at 500 ms while TAG's ISMCTS gained. "
        "Next time I would test at the competition's time limit earlier.",
    ]),
    ("Weronika Diana Podgorska", "230394735", [
        "We shared the work equally, and my part was most of the research. I studied how Sushi Go! is modelled in "
        "TAG and what makes it hard for plain MCTS: hidden hands, simultaneous card choices and several players. I "
        "then read the literature on each problem: determinisation and information-set MCTS for hidden information, "
        "decoupled UCT and its alternatives for simultaneous moves, max-n backups for several players, and "
        "simulation balancing for rollout policies. I summarised what each method would mean for our agent, and "
        "these summaries were the basis of our main technical decisions: one tree node per turn in which every "
        "player chooses from its own statistics, hidden cards re-sampled in every iteration instead of a few fixed "
        "samples, and an evaluation based on the lead over the best opponent, since only first place wins.",
        "Hakan kept the project on schedule, shared out the work and made the final calls with us, and Halit turned "
        "the methods I found into code and tested them.",
        "The research showed me that the right method depends on the game. Decoupled selection over information "
        "sets improved our agent by 8.2 percentage points over its sequential, determinised predecessor, while TAG's "
        "own agent of this kind, without our card model, was not detectably better than our previous version. I also "
        "learned to read every result together with its error bars. Next time I would also test the alternatives "
        "to decoupled UCT, regret matching and EXP3, which our report leaves as future work.",
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
