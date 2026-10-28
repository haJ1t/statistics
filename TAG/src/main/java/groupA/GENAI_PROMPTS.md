# Prompts used with generative AI

The agent in this package and the accompanying report were produced with Claude (Anthropic), used through the
Claude Code tool (see the report's statement on the use of generative AI). This file lists the prompts.

## Prompts written by the authors

1. **The report** (original Turkish, verbatim):

   > simdi reponun icindeki butun herseyi incele ve hocaya direk yuklyecegimiz raporuda olusutr repoda ornekler tipler
   > brief hersey var 100 alcagimiz bir rapor olsun her raporu tamaladiginda baska bir agent spawn edip bagimsiz
   > durusstce kontrol taki yuzde yuz alacagana emin olankadar looptan cikma

   English translation: "Now examine everything in the repository and also create the report we will upload directly
   to the lecturer; the repository has examples, tips, the brief, everything. It should be a report that gets 100.
   Every time you finish the report, spawn another agent to check it independently and impartially, and do not leave
   the loop until you are sure it will get 100%."

2. **The later candidate changes** (the five changes tested on seed block 7 before the in-class tournament; English
   translation): "The lecturer will hold a tournament of our agents in class; as before, improve the agents'
   performance quickly while following the lecturer's restrictions and recording what we did in the README; you have
   4.5 hours, test everything in parallel."

3. **Other messages of the report session** only asked for the work to be pushed to GitHub: "github bağladım şimdi
   push et" ("I connected GitHub, now push") and "yaptigin herseyi direk push et githuba repoda bulamadim" ("push
   everything you did straight to GitHub, I could not find it in the repository").

4. **Earlier development sessions** asked Claude to implement and test the modifications listed in Table I of the
   report and in this package's README, and to run the experiments of Section IV; their prompts are not reproduced.

## Prompts written by Claude for its reviewer instances

Following prompt 1, each draft of the report was marked by a separate Claude instance. Its prompt, written by the
generating instance, was the following (the list of pending runs and the remaining space on page 4 changed between
drafts):

> You are an independent, strict marker for the QMUL module ECS7032P (AI in games), Assignment 1. Mark the group
> report and code in the repository as the module's lecturer would, and tell us precisely what stands between it and
> a mark of 90–100%. Read the brief, rubric and guidance, the report (and the scripts that generate it from the
> per-game result files), the agent code and the experiment records; do not read the earlier reviews. (1) Mark every
> rubric item with a band and a mark, with reasons, and give the weighted total. (2) Recompute every number in the
> report from the raw result files with your own script, and flag every mismatch or untraceable number. (3) Check the
> technical description against the code, the literature claims against what the cited papers say, and whether each
> conclusion is supported by the statistics reported next to it. (4) Check compliance with the brief. (5) Check
> presentation. (6) Give a prioritised list of changes with replacement text and their effect on length. (7) End
> with a plain answer: would this get 85% or more, and what stands between it and 90%+?

The reviewers' written reports, one per draft, were the instructions for the revisions; they are kept with our
experiment records.
