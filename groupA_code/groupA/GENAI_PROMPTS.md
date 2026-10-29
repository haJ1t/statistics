# Prompts used with generative AI

As stated at the end of our report, we used Claude (Anthropic), through the Claude Code tool, to write and modify the
agent's code, to run the experiments and to draft the report. These are the prompts.

1. **The report** (original Turkish, verbatim):

   > simdi reponun icindeki butun herseyi incele ve hocaya direk yuklyecegimiz raporuda olusutr repoda ornekler tipler
   > brief hersey var 100 alcagimiz bir rapor olsun her raporu tamaladiginda baska bir agent spawn edip bagimsiz
   > durusstce kontrol taki yuzde yuz alacagana emin olankadar looptan cikma

   English translation: "Now examine everything in the repository and also create the report we will upload directly
   to the lecturer; the repository has examples, tips, the brief, everything. It should be a report that gets 100.
   Every time you finish the report, spawn another agent to check it independently and impartially, and do not leave
   the loop until you are sure it will get 100%."

   Following this prompt, drafts of the report were marked by a second Claude instance, prompted by the first as an
   independent, strict marker for ECS7032P: to mark each draft against the brief and the rubric, to recompute its
   numbers from our result files and to list the changes that would raise the mark. Its written reports were used for
   the revisions and are available from the authors.

2. **The later candidate changes** (tested before the in-class tournament; original Turkish, verbatim):

   > sikinti su hoca ajanlarimizi sinif icinde turnuva yaptircakmis daha oncesinde yaptigimiz gibi ajalnlari cok
   > hizli sekilde hocanin kuydugu yasaklara uyarak ve yaptiklarimiz readme kaydederek ajanlarin performasini hizlica
   > arttirmamiz gerek ama olay su an su ajanlari gelsitirmek icin 4.5 saatin var herseyi ona gore hizli sekilde ayni
   > anda test ederek olabildigince hizli ama perfomasn arttirak gitmemiz gerek bitene kadar looptan cikma

   English translation: "The problem is that the lecturer will hold a tournament of our agents in class; as before, we
   need to improve the agents' performance quickly while following the lecturer's restrictions and recording what we
   did in the README; you have 4.5 hours to develop the agents, so test everything in parallel, as fast as possible
   while improving performance, and do not leave the loop until it is finished."

3. **Earlier development sessions** asked Claude to implement and test the modifications listed in Table I of the
   report and to run the experiments of Section IV; their prompts are not reproduced.

4. **Other messages** asked for the work to be pushed to GitHub, for the authors' names, student numbers and group
   letter to be added, for the submission files to be prepared and checked, and for the report's statement on the use
   of generative AI to be shortened and to leave out the prompts.
