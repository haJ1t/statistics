# Development tools (not part of the submission)

Checks and diagnostics used while building v4. They are in package `groupA` because they read package-private parts
of the agent (`SushiSim`, `SushiTreeNode` statistics via reflection). Compile and run from the TAG root:

```
javac -cp "target/classes:arena/classes:$(cat cp.txt)" -d arena/dev/out arena/dev/groupA/*.java
java -cp "arena/dev/out:target/classes:arena/classes:$(cat cp.txt)" groupA.SushiSimCheck 400 1
```

| Tool | What it does |
|---|---|
| `SushiSimCheck games seed` | plays random games with TAG's forward model and `SushiSim` side by side and compares hands, scores, Maki, Pudding, values (F must be exact) |
| `ModeCompare rec label cfgA cfgB nStates seeds iterations` | how often two configurations (e.g. with and without F) make the same decision, against two seeds of one configuration |
| `EquivI` | `CardValueModel.distinctCards` against the forward model, and `handValues` against `actionValue` |
| `EquivRollout` | deterministic searches on fixed states; prints a trace to compare two builds |
| `TreeStats rec label cfg nStates budget` | iterations, depth, root children, top-card visit shares of a search on recorded positions |
| `BudgetCompare rec label cfg nStates seeds msA msB` | how often decisions at two budgets agree |
| `Converge rec label cfg nStates seeds iters1,iters2,...` | how the root decision changes with the number of iterations |
| `OpponentPredict rec label cfg nStates budget` | how well the tree's opponent statistics predict the opponents' real picks |
| `QPredict rec label T prior` | offline check of Q: likelihood of the opponents' real picks with and without the learned biases |
| `OpponentModelCheck ours games n opps` | the biases Q learns against given opponents |
| `FourPlayerCheck` | the `fourPlayer` block is used with 4 players only, with the top-level budget |
| `Recorder`, `Imitate` | record arena games (deal + every pick) and replay them |
| `GreedyDuel`, `GreedyTune` | search-free duels of card-value policies, and the tuner that found the X0 constants |
| `BenchI cfg` | CPU time per iteration of a configuration on fixed states |
| `Bench4 cfg ...` | tree iterations per 4-player decision of configurations under their own time budget |
| `EquivF [key=value ...]` | deterministic searches with the final 4-player settings (F + L8) on fixed states; a hash of the decisions to compare two builds (a new switch at its default must give the same hash) |
| `TreeStats4 budget cfg ...` | TreeStats on the fixed 4-player states of Bench4 (no recordings needed): iterations, CPU time per iteration, root children, depth-1 visits, mean depth |
| `Agree4 cfgA cfgB ...` | how often a configuration picks the same card with another seed, and how often other configurations pick its card, on the fixed 4-player states |
