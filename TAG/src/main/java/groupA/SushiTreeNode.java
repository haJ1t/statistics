package groupA;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import players.PlayerConstants;
import utilities.ElapsedCpuTimer;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static players.PlayerConstants.*;
import static utilities.Utils.noise;

/**
 * SushiTreeNode - copied from players.basicMCTS.BasicTreeNode (permitted base agent).
 * <p>
 * The structure of BasicTreeNode is kept (closed-loop tree: every node stores its own state,
 * a single scalar value from the root player's point of view, paranoid opponents in UCB).
 * Changes are marked with "MODIFICATION A" / "B" / "P" / "O" / "M" / "R" / "S" / "I" / "G" and are switched
 * on/off from SushiMCTSParams, so that the ablation study runs exactly the same code.
 * <p>
 * A: Progressive Bias (Chaslot et al., 2008). Each child stores a prior b(a) = tanh(mv(a)/scale),
 *    where mv is the marginal value of the card for the player who plays it (CardValueModel).
 *    UCB(child) = +/- Q/n + K*sqrt(ln N / n) + W * b(a) / (n + 1); unexpanded actions are
 *    expanded in order of decreasing prior. The prior is computed from the ACTION, not from the
 *    child state, because in TAG's Sushi Go a chosen card is only revealed after all players
 *    have chosen, so the child states of one node all show the same table.
 * B: Informed epsilon-greedy rollouts. With probability 1 - epsilon every player (us and the
 *    opponents) plays the card with the highest marginal value; otherwise a random card.
 *    This is also our opponent model inside simulations.
 * P: Action pruning. Two copies of the same card in a hand lead to the same next state, so a node
 *    keeps one action per distinct card (CardValueModel.distinctActions).
 * O: Opponent model in the tree. Opponents are not searched: they play the card-value policy of B
 *    (random with probability opponentEpsilon). Tested; not used in the final agent.
 * M: max^n (Sturtevant 2008). Every node also sums the rollout value of EVERY player; at a node where
 *    player j moves, UCB uses j's own average value instead of minus ours (paranoid).
 * R: Rollouts to the end of the current round instead of a fixed number of moves, so Maki and all
 *    sets of the round are scored exactly before the heuristic is applied.
 * S: Simultaneous moves - decoupled UCT (DUCT; Lanctot et al. 2013, Tak et al. 2014). TAG's forward model
 *    asks the players of a Sushi Go turn one after the other, so in the plain tree a later player "sees" the
 *    card an earlier player picked in the same turn. With S the tree has one node per TURN: the node keeps,
 *    for every player, statistics per card; each player picks its card by UCB over its OWN statistics
 *    (decoupled, as in the real game where nobody sees the others' picks), and the child is the state after
 *    the whole joint choice has been revealed. The second card of a chopsticks move is the player's best
 *    card by the card-value model. Requires M (every player maximises its own value).
 * I: Information sets (single-observer ISMCTS; Cowling, Powley and Whitehouse 2012) on top of the turn nodes
 *    of S. Instead of a few fixed samples of the hidden cards (D), every iteration starts from a NEW sample
 *    (state.copy(me) re-deals the unknown hands and the draw pile) and walks one shared tree. The tree is
 *    open loop: a node is a sequence of revealed joint choices and keeps statistics only, no game state,
 *    because its state is different in every sample. Cards are identified by (type, Maki icons, chopsticks),
 *    not by their position in a hand, and a card's exploration term counts the iterations in which the player
 *    actually held it (availability). Requires M. Two variants were tested and are off by default:
 *    treeThreshold (a node visited fewer times plays the rollout policy instead of UCB) and ownCardTree (the
 *    child depends on our own card only, not on the joint choice).
 * G: MAST (Finnsson and Bjoernsson 2008) on top of I. The root keeps, per player and card, the mean value of
 *    the iterations in which that player played that card; in the rollouts this value (times mastWeight, shrunk
 *    towards the player's mean) is added to the card value of B. Tested; off by default.
 */
class SushiTreeNode {
    // Root node of tree
    SushiTreeNode root;
    // Parent of this node
    SushiTreeNode parent;
    // Children of this node
    Map<AbstractAction, SushiTreeNode> children = new HashMap<>();
    // Depth of this node
    final int depth;

    // Total value of this node
    private double totValue;
    // Number of visits
    private int nVisits;
    // Number of FM calls and State copies up until this node
    private int fmCallsCount;
    // Number of MCTS iterations (root only)
    private int iterations;
    // Parameters guiding the search
    private SushiMCTSPlayer player;
    private Random rnd;

    // State in this node (closed loop)
    private AbstractGameState state;

    // MODIFICATION A: prior of the action that leads to this node, and priors of this node's actions
    private double prior = 0.0;
    private Map<AbstractAction, Double> actionPriors = null;
    // MODIFICATION O: cached greedy card of the opponent who moves at this node
    private AbstractAction greedyOpponentAction = null;
    // MODIFICATION M: sum of the backed-up values of EVERY player (max^n), index = player id
    private double[] playerValues;
    // MODIFICATION S (node where a simultaneous turn starts): each player's cards, per player card -> {visits,
    // value sum}, and the children reached by joint choices (one card per player, indexed by player id)
    private List<AbstractAction>[] turnActions = null;
    private Map<AbstractAction, double[]>[] turnStats = null;
    private Map<List<AbstractAction>, SushiTreeNode> jointChildren = null;
    // MODIFICATION S (root only): {turn node, joint choice} of every turn passed in the current iteration
    private List<Object[]> ductPath = null;
    // MODIFICATION I (open-loop turn node): per player p, the card keys seen at this node (isKeys[p][0..isSize[p]-1])
    // and, for the j-th of them, isVal[p][3j] = visits, isVal[p][3j+1] = value sum, isVal[p][3j+2] = availability;
    // the children by joint choice (the players' card keys packed into one long, 8 bits per player)
    private int[][] isKeys = null;
    private double[][] isVal = null;
    private int[] isSize = null;
    // isIndex[p][k] = 1 + the index j of card key k in isKeys[p] (0 = not seen yet), so slot() needs no search
    private byte[][] isIndex = null;
    private static final int KEY_LIMIT = games.sushigo.cards.SGCard.SGCardType.values().length * 8;
    private Map<Long, SushiTreeNode> isChildren = null;
    // MODIFICATION U (root only): the root of every player's own tree (ours is this node)
    private SushiTreeNode[] mtRoots = null;
    // MODIFICATION F (root only): the root position as a SushiSim, the sample of the current iteration, scratch space
    private SushiSim simRoot = null, simSample = null, simLeaf = null;
    private int[] simScratch = null;
    // opponentScoreValue (root only): every player's estimated score at the root, the centre of an own-score value
    private double[] rootEst = null;
    // MODIFICATION G (MAST, Finnsson and Bjoernsson 2008; root of an I search only): per player and card key, the
    // number of iterations in which the player played that card (in the tree or the rollout) and the sum of the
    // player's values; mastMoves holds the (player, key) pairs of the current iteration
    private double[][] mastN = null, mastV = null;
    private double[] mastTotN = null, mastTotV = null;
    private int[] mastMoves = null;
    private int mastLen = 0;

    protected SushiTreeNode(SushiMCTSPlayer player, SushiTreeNode parent, AbstractGameState state, Random rnd) {
        this.player = player;
        this.fmCallsCount = 0;
        this.parent = parent;
        this.root = parent == null ? this : parent.root;
        totValue = 0.0;
        if (player.getParameters().useMaxN)
            playerValues = new double[state.getNPlayers()];
        if (parent == null && player.getParameters().useDecoupledUCT)
            ductPath = new ArrayList<>();
        setState(state);
        if (parent != null) {
            depth = parent.depth + 1;
        } else {
            depth = 0;
        }
        this.rnd = rnd;
        // MODIFICATION I: the root of an information-set search must be the start of a turn (it is not when we
        // pick the second card of a chopsticks move; that small decision uses the closed-loop tree)
        SushiMCTSParams params = player.getParameters();
        if (parent == null && params.useInformationSets && params.useMaxN && isTurnStart(state)) {
            initInformationSets(state.getNPlayers());
            if (params.useMAST) {
                int n = state.getNPlayers();
                mastN = new double[n][128];
                mastV = new double[n][128];
                mastTotN = new double[n];
                mastTotV = new double[n];
                mastMoves = new int[256];
            }
        }
    }

    /** MODIFICATION G: remember that player p played card key k in the current iteration. */
    private void mastRecord(int p, int k) {
        if (mastLen + 2 > mastMoves.length)
            mastMoves = Arrays.copyOf(mastMoves, 2 * mastMoves.length);
        mastMoves[mastLen++] = p;
        mastMoves[mastLen++] = k;
    }

    /**
     * MODIFICATION G: MAST value of card key k for player p - the mean value of p after playing k, shrunk towards
     * p's mean over all cards with mastPrior pseudo-observations (so a rarely played card counts as average).
     */
    private double mastValue(int p, int k) {
        double m0 = player.getParameters().mastPrior;
        double mean = mastTotN[p] > 0 ? mastTotV[p] / mastTotN[p] : 0.0;
        return (mastV[p][k] + m0 * mean) / (mastN[p][k] + m0);
    }

    /**
     * MODIFICATION I: open-loop node. It stores statistics only - its game state is different in every sample.
     */
    private SushiTreeNode(SushiMCTSPlayer player, SushiTreeNode parent, Random rnd) {
        this.player = player;
        this.parent = parent;
        this.root = parent.root;
        this.depth = parent.depth + 1;
        this.rnd = rnd;
    }

    /**
     * Performs full MCTS search, using the given budget (the player may split its budget between
     * several determinisations, so the budget is passed in rather than read from the parameters).
     * wallDeadline: System.nanoTime() at which a time-budgeted search must stop anyway (Long.MAX_VALUE = none).
     */
    void mctsSearch(int budget, int breakMS, long wallDeadline) {

        SushiMCTSParams params = player.getParameters();

        // Variables for tracking time budget
        double avgTimeTaken;
        double acumTimeTaken = 0;
        long remaining;
        ElapsedCpuTimer elapsedTimer = new ElapsedCpuTimer();
        if (params.budgetType == BUDGET_TIME) {
            elapsedTimer.setMaxTimeMillis(budget);
        }

        // Tracking number of iterations for iteration budget
        int numIters = 0;

        boolean stop = false;

        while (!stop) {

            // Selection + expansion: navigate tree until a node not fully expanded is found, add a new node to the tree
            boolean decoupled = jointChildren != null && params.useMaxN && !params.useOpponentModel;
            if (isChildren != null) {
                // MODIFICATION I: sample the hidden cards, select, expand, roll out and back up in one pass
                if (params.useMultiTree && params.useFastSim && fastSimApplies())
                    simMultiTreeIteration();   // MODIFICATIONS U + F
                else if (params.useMultiTree)
                    multiTreeIteration();   // MODIFICATION U: one tree per player
                else if (params.useFastSim && fastSimApplies())
                    simIteration();         // MODIFICATION F: the same iteration on a SushiSim
                else
                    informationSetIteration();
            } else {
                SushiTreeNode selected = decoupled ? treePolicyDecoupled() : treePolicy();
                if (params.useMaxN) {
                    // MODIFICATION M: evaluate the rollout for every player and back up the whole vector
                    double[] values = selected.rollOutAll();
                    selected.backUpAll(values);
                    // MODIFICATION S: update the per-player card statistics of the turns on the path
                    if (decoupled)
                        updateDecoupled(values);
                } else {
                    // Monte carlo rollout: return value of MC rollout from the newly added node
                    double delta = selected.rollOut();
                    // Back up the value of the rollout through the tree
                    selected.backUp(delta);
                }
            }
            // Finished iteration
            numIters++;

            // Check stopping condition
            PlayerConstants budgetType = params.budgetType;
            if (budgetType == BUDGET_TIME) {
                // Time budget
                // BasicMCTS creates a new ElapsedCpuTimer for every iteration (a ThreadMXBean look-up each time); the time
                // of all iterations so far is simply the time on the decision's timer. MODIFICATION F: an iteration on a
                // SushiSim takes a few microseconds, about as long as reading the CPU clock, so the clock is read only
                // every 8th iteration and the search stops 8 iterations earlier.
                int every = params.useFastSim ? 8 : 1;
                if (numIters % every == 0) {
                    long elapsedNanos = elapsedTimer.elapsed();
                    acumTimeTaken = elapsedNanos / 1e6;
                    avgTimeTaken = acumTimeTaken / numIters;
                    remaining = (long) ((budget * 1_000_000L - elapsedNanos) / 1e6);   // = remainingTimeMillis()
                    stop = remaining <= (every + 1) * avgTimeTaken || remaining <= breakMS
                            || (wallDeadline != Long.MAX_VALUE && System.nanoTime() >= wallDeadline);  // safety: real-time limit
                }
            } else if (budgetType == BUDGET_ITERATIONS) {
                // Iteration budget
                stop = numIters >= budget;
            } else if (budgetType == BUDGET_FM_CALLS) {
                // FM calls budget
                stop = fmCallsCount > budget;
            }
        }
        iterations = numIters;
    }

    int getIterations() {
        return iterations;
    }

    /**
     * Selection + expansion steps.
     */
    private SushiTreeNode treePolicy() {

        SushiTreeNode cur = this;
        SushiMCTSParams params = player.getParameters();

        // Keep iterating while the state reached is not terminal and the depth of the tree is not exceeded
        while (cur.state.isNotTerminal() && cur.depth < params.maxTreeDepth) {
            if (params.useOpponentModel && cur.state.getCurrentPlayer() != player.getPlayerID()
                    && cur.state instanceof SGGameState sg) {
                // MODIFICATION O: opponents are not searched (no paranoid minimisation); they play the
                // card-value policy of B, so the tree only branches on our own decisions
                AbstractAction a = cur.opponentAction(sg);
                SushiTreeNode child = cur.children.get(a);
                if (child == null)
                    return cur.expand(a);
                cur = child;
            } else if (!cur.unexpandedActions().isEmpty()) {
                // We have an unexpanded action
                cur = cur.expand();
                return cur;
            } else {
                // Move to next child given by UCT function
                AbstractAction actionChosen = cur.ucb();
                cur = cur.children.get(actionChosen);
            }
        }

        return cur;
    }

    /**
     * MODIFICATION S: selection + expansion over turn nodes. At every node all players pick their card with
     * selectDecoupled (each from its own statistics only); the joint choice leads to the child turn node,
     * which is created (and evaluated by the rollout) if this joint choice has not been seen before.
     */
    private SushiTreeNode treePolicyDecoupled() {
        SushiMCTSParams params = player.getParameters();
        List<Object[]> path = root.ductPath;
        path.clear();
        SushiTreeNode cur = this;
        while (cur.state.isNotTerminal() && cur.depth < params.maxTreeDepth && cur.jointChildren != null) {
            int n = cur.turnActions.length;
            List<AbstractAction> joint = new ArrayList<>(n);
            for (int p = 0; p < n; p++)
                joint.add(cur.selectDecoupled(p, cur.turnActions[p]));
            path.add(new Object[]{cur, joint});
            SushiTreeNode child = cur.jointChildren.get(joint);
            if (child == null)
                return cur.expandJoint(joint);
            cur = child;
        }
        return cur;
    }

    /**
     * MODIFICATION S: card of player `mover` at this turn node - untried cards first, then UCB1 over the
     * player's own value of each card, averaged over everything the other players did in this turn.
     */
    private AbstractAction selectDecoupled(int mover, List<AbstractAction> actions) {
        Map<AbstractAction, double[]> stats = turnStats[mover];
        List<AbstractAction> untried = new ArrayList<>();
        for (AbstractAction a : actions) {
            double[] st = stats.get(a);
            if (st == null || st[0] == 0)
                untried.add(a);
        }
        if (!untried.isEmpty())
            return untried.get(rnd.nextInt(untried.size()));

        SushiMCTSParams params = player.getParameters();
        double logN = Math.log(nVisits + 1);
        AbstractAction best = null;
        double bestValue = -Double.MAX_VALUE;
        for (AbstractAction a : actions) {
            double[] st = stats.get(a);
            double v = st[1] / st[0] + params.K * Math.sqrt(logN / st[0]);
            v = noise(v, params.epsilon, player.getRnd().nextDouble());
            if (v > bestValue) {
                bestValue = v;
                best = a;
            }
        }
        return best;
    }

    /**
     * MODIFICATION S: child turn node of a joint choice. The cards are played in the order in which the forward
     * model asks for them, until the turn has been revealed; a chopsticks player's second card is its best
     * card by the card-value model (so the child of a joint choice is always the same state).
     */
    private SushiTreeNode expandJoint(List<AbstractAction> joint) {
        AbstractGameState next = state.copy();
        do {
            AbstractAction a;
            if (next.isActionInProgress() && next instanceof SGGameState sg)
                a = greedyAction(sg, player.getForwardModel().computeAvailableActions(next, player.getParameters().actionSpace));
            else
                a = joint.get(next.getCurrentPlayer());
            advance(next, a.copy());
        } while (next.isNotTerminal() && !isTurnStart(next));
        SushiTreeNode tn = new SushiTreeNode(player, this, next, rnd);
        jointChildren.put(joint, tn);
        return tn;
    }

    /**
     * MODIFICATION S (called on the root): add this iteration's values to the card statistics of every
     * player at every turn node it passed.
     */
    private void updateDecoupled(double[] values) {
        for (Object[] e : ductPath) {
            SushiTreeNode turnNode = (SushiTreeNode) e[0];
            @SuppressWarnings("unchecked")
            List<AbstractAction> joint = (List<AbstractAction>) e[1];
            for (int p = 0; p < joint.size(); p++) {
                double[] st = turnNode.turnStats[p].computeIfAbsent(joint.get(p), k -> new double[2]);
                st[0] += 1;
                st[1] += values[p];
            }
        }
    }

    private void initInformationSets(int nPlayers) {
        isKeys = new int[nPlayers][];   // a player's arrays are made when it first picks a card here
        isVal = new double[nPlayers][];
        isSize = new int[nPlayers];
        isIndex = new byte[nPlayers][];
        isChildren = new HashMap<>();
    }

    /** MODIFICATION I: index j of card key k in player p's statistics at this node (added if new). */
    private int slot(int p, int k) {
        if (isKeys[p] == null) {
            isKeys[p] = new int[8];
            isVal[p] = new double[24];
            isIndex[p] = new byte[KEY_LIMIT];
        }
        int j = isIndex[p][k] - 1;
        if (j >= 0) return j;
        int[] keys = isKeys[p];
        int n = isSize[p];
        if (n == keys.length) {
            isKeys[p] = keys = Arrays.copyOf(keys, 2 * n);
            isVal[p] = Arrays.copyOf(isVal[p], 6 * n);
        }
        keys[n] = k;
        isSize[p] = n + 1;
        isIndex[p][k] = (byte) (n + 1);
        return n;
    }

    /** MODIFICATION I: mean value of card key k for player p at this node (0 if never chosen). */
    private double meanOf(int p, int k) {
        for (int j = 0; j < isSize[p]; j++)
            if (isKeys[p][j] == k) return isVal[p][3 * j] > 0 ? isVal[p][3 * j + 1] / isVal[p][3 * j] : 0.0;
        return 0.0;
    }

    /** MODIFICATION I: visits of card key k for player p at this node (0 if never chosen). */
    private double visitsOf(int p, int k) {
        for (int j = 0; j < isSize[p]; j++)
            if (isKeys[p][j] == k) return isVal[p][3 * j];
        return 0.0;
    }

    /**
     * MODIFICATION I (called on the root): one iteration of the information-set search.
     * 1. Sample: a copy of the root state in which every card we cannot see is dealt again at random.
     * 2. Selection: at every turn node, each player picks one of the cards it holds IN THIS SAMPLE by UCB over its
     *    own statistics (decoupled, as in S); the joint choice is played on the sample and leads to the child of
     *    that joint choice. 3. Expansion: the first joint choice never seen before adds one node.
     * 4. Rollout from the sample (policy B, until the end of the round with R). 5. Backup: every player's value
     *    is added to its own card at every node on the path (M).
     */
    private void informationSetIteration() {
        SushiMCTSParams params = player.getParameters();
        int me = player.getPlayerID();
        AbstractGameState sample = state.copy(me);
        mastLen = 0;
        List<SushiTreeNode> nodes = new ArrayList<>();
        List<int[]> slots = new ArrayList<>();
        SushiTreeNode cur = this;
        boolean expanded = false;
        int startRound = sample.getRoundCounter();
        while (!expanded && sample.isNotTerminal() && isTurnStart(sample) && cur.depth < params.maxTreeDepth
                && !(params.stopAtRoundEnd && sample.getRoundCounter() != startRound)) {
            SGGameState sg = (SGGameState) sample;
            int n = sg.getNPlayers();
            if (cur.isChildren == null)
                cur.initInformationSets(n);
            int[] slot = new int[n];
            AbstractAction[] chosen = new AbstractAction[n];
            long key = 0;
            // a node visited fewer than treeThreshold times is searched with the rollout policy (B) instead of UCB
            boolean sparse = cur != this && cur.nVisits < params.treeThreshold;
            for (int p = 0; p < n; p++) {
                int k = cur.selectInformationSet(p, sg, chosen, slot, sparse);
                if (mastN != null) mastRecord(p, k);
                // ownCardTree: the child depends on our own card only, so the tree branches on our decisions and the
                // opponents' cards are drawn again from their statistics in every iteration
                if (!params.ownCardTree || p == me)
                    key |= ((long) k) << (8 * p);
            }
            nodes.add(cur);
            slots.add(slot);
            // play the joint choice in the order in which the forward model asks for the cards
            do {
                AbstractAction a = sample.isActionInProgress()
                        ? greedyAction((SGGameState) sample, player.getForwardModel().computeAvailableActions(sample, params.actionSpace))
                        : chosen[sample.getCurrentPlayer()];
                advance(sample, a.copy());
            } while (sample.isNotTerminal() && !isTurnStart(sample));
            SushiTreeNode child = cur.isChildren.get(key);
            if (child == null) {
                child = new SushiTreeNode(player, cur, rnd);
                cur.isChildren.put(key, child);
                expanded = true;
            }
            cur = child;
        }

        // the sample is ours, so the rollout continues on it (stopAtRoundEnd: a round finished in the tree is the leaf)
        boolean roundDone = params.stopAtRoundEnd && sample.getRoundCounter() != startRound;
        double[] values = valuesOf(roundDone ? sample : playOut(sample));
        for (SushiTreeNode x = cur; x != null; x = x.parent) {
            x.nVisits++;
            x.totValue += values[me];
        }
        for (int i = 0; i < nodes.size(); i++) {
            double[][] val = nodes.get(i).isVal;
            int[] slot = slots.get(i);
            for (int p = 0; p < slot.length; p++) {
                val[p][3 * slot[p]] += 1;
                val[p][3 * slot[p] + 1] += values[p];
            }
        }
        if (mastN != null) {   // MODIFICATION G: every card played in this iteration gets the player's value
            for (int j = 0; j < mastLen; j += 2) {
                int p = mastMoves[j], k = mastMoves[j + 1];
                mastN[p][k] += 1;
                mastV[p][k] += values[p];
                mastTotN[p] += 1;
                mastTotV[p] += values[p];
            }
        }
    }

    /**
     * MODIFICATION U (called on the root): one iteration with one information-set tree per player, in the spirit of
     * MO-ISMCTS (Cowling, Powley and Whitehouse 2012) for simultaneous moves. Player p's tree branches on p's own cards
     * only: a node is p's own sequence of cards this round, so its statistics are p's values given what p itself has
     * played, whatever the others did; the others' cards come from their own trees. With one joint tree (I) a child
     * needs the cards of all players, so in 4-player games each child is visited rarely and the tree stays shallow.
     * Each tree adds at most one node per iteration; after that its player plays the rollout policy (B) until every
     * tree has been left, and the rollout continues from the sample. Every player backs up its own value (M).
     */
    private void multiTreeIteration() {
        SushiMCTSParams params = player.getParameters();
        int me = player.getPlayerID();
        AbstractGameState sample = state.copy(me);
        int n = sample.getNPlayers();
        if (mtRoots == null) {
            mtRoots = new SushiTreeNode[n];
            for (int p = 0; p < n; p++)
                mtRoots[p] = p == me ? this : new SushiTreeNode(player, this, rnd);
        }
        SushiTreeNode[] cur = mtRoots.clone();
        List<List<SushiTreeNode>> pathNodes = new ArrayList<>(n);
        List<List<Integer>> pathSlots = new ArrayList<>(n);
        for (int p = 0; p < n; p++) {
            pathNodes.add(new ArrayList<>());
            pathSlots.add(new ArrayList<>());
        }
        int[] slot = new int[n], key = new int[n];
        boolean inTree = true;
        int startRound = sample.getRoundCounter();
        for (int depth = 0; inTree && sample.isNotTerminal() && isTurnStart(sample) && depth < params.maxTreeDepth
                && !(params.stopAtRoundEnd && sample.getRoundCounter() != startRound); depth++) {
            SGGameState sg = (SGGameState) sample;
            AbstractAction[] chosen = new AbstractAction[n];
            for (int p = 0; p < n; p++) {
                if (cur[p] != null) {
                    if (cur[p].isChildren == null)
                        cur[p].initInformationSets(n);
                    key[p] = cur[p].selectInformationSet(p, sg, chosen, slot, false);
                    pathNodes.get(p).add(cur[p]);
                    pathSlots.get(p).add(slot[p]);
                } else {
                    chosen[p] = policyCard(sg, p);
                }
            }
            // play the joint choice in the order in which the forward model asks for the cards
            do {
                AbstractAction a = sample.isActionInProgress()
                        ? greedyAction((SGGameState) sample, player.getForwardModel().computeAvailableActions(sample, params.actionSpace))
                        : chosen[sample.getCurrentPlayer()];
                advance(sample, a.copy());
            } while (sample.isNotTerminal() && !isTurnStart(sample));
            inTree = false;
            for (int p = 0; p < n; p++) {
                if (cur[p] == null) continue;
                SushiTreeNode child = cur[p].isChildren.get((long) key[p]);
                if (child == null) {
                    cur[p].isChildren.put((long) key[p], new SushiTreeNode(player, cur[p], rnd));
                    cur[p] = null;   // expanded: p plays the rollout policy from here
                } else {
                    cur[p] = child;
                    inTree = true;
                }
            }
        }

        // the sample is ours, so the rollout continues on it (stopAtRoundEnd: a round finished in the tree is the leaf)
        boolean roundDone = params.stopAtRoundEnd && sample.getRoundCounter() != startRound;
        double[] values = valuesOf(roundDone ? sample : playOut(sample));
        for (int p = 0; p < n; p++) {
            List<SushiTreeNode> nodes = pathNodes.get(p);
            List<Integer> slots = pathSlots.get(p);
            for (int i = 0; i < nodes.size(); i++) {
                SushiTreeNode x = nodes.get(i);
                int j = slots.get(i);
                x.nVisits++;
                x.isVal[p][3 * j] += 1;
                x.isVal[p][3 * j + 1] += values[p];
            }
        }
    }

    /**
     * MODIFICATION U: the rollout policy's card for player p (who may not be the player the forward model asks next:
     * at the start of a turn every player's choice depends on the same state). Same rule as rolloutCard.
     */
    private AbstractAction policyCard(SGGameState sg, int p) {
        SushiMCTSParams params = player.getParameters();
        int h = sg.getPlayerHands().get(p).getSize();
        boolean chop = sg.getPlayedCardTypes(games.sushigo.cards.SGCard.SGCardType.Chopsticks, p).getValue() > 0 && h > 1;
        if (params.useInformedRollout && rnd.nextDouble() >= params.rolloutEpsilon) {
            double[] mv = policyValues(sg, p);
            int bestIdx = 0;
            boolean bestChop = false;
            double bestValue = -Double.MAX_VALUE;
            for (int i = 0; i < h; i++)
                for (int c = 0; c <= (chop ? 1 : 0); c++) {
                    double v = chopsticksValue(mv, i, c == 1) + 1e-6 * rnd.nextDouble();
                    if (v > bestValue) {
                        bestValue = v;
                        bestIdx = i;
                        bestChop = c == 1;
                    }
                }
            return new ChooseCard(p, bestIdx, bestChop);
        }
        int idx = rnd.nextInt(chop ? 2 * h : h);
        return new ChooseCard(p, chop ? idx / 2 : idx, chop && idx % 2 == 1);
    }

    /** UCB exploration constant of player p in the I / U statistics: K for us, opponentK (if >= 0) for the others. */
    private double explorationOf(int p) {
        SushiMCTSParams params = player.getParameters();
        return params.opponentK >= 0 && p != player.getPlayerID() ? params.opponentK : params.K;
    }

    /** MODIFICATION F applies to a search from the start of a turn, with R, our heuristic and no MAST. */
    private boolean fastSimApplies() {
        SushiMCTSParams params = player.getParameters();
        return state instanceof SGGameState && isTurnStart(state) && params.rolloutToRoundEnd
                && params.heuristic instanceof SushiGoHeuristic && mastN == null;
    }

    /**
     * MODIFICATION F: informationSetIteration on a SushiSim instead of a copy of TAG's game state, with the same
     * sampling, selection, expansion, rollout policy (to the end of the round in which it starts) and backup.
     */
    private void simIteration() {
        SushiMCTSParams params = player.getParameters();
        int me = player.getPlayerID();
        if (simRoot == null) {
            simRoot = SushiSim.root((SGGameState) state, me);
            simSample = simRoot.emptyCopy();
            simScratch = new int[Math.max(1, simRoot.poolSize())];
        }
        SushiSim sim = simSample;
        sim.sampleFrom(simRoot, rnd, simScratch);
        CardValueModel.Config cfg = ((SushiGoHeuristic) params.heuristic).model();
        int n = sim.n, startRound = sim.round;
        List<SushiTreeNode> nodes = new ArrayList<>();
        List<int[]> slots = new ArrayList<>();
        int[] first = new int[n], second = new int[n];
        SimValues mv = new SimValues(sim, cfg, opponentBiases(sim.n));
        SushiTreeNode cur = this;
        boolean expanded = false;
        while (!expanded && !sim.gameOver && cur.depth < params.maxTreeDepth
                && !(params.stopAtRoundEnd && sim.round != startRound)) {
            if (cur.isChildren == null)
                cur.initInformationSets(n);
            int[] slot = new int[n];
            long key = 0;
            boolean sparse = cur != this && cur.nVisits < params.treeThreshold;
            mv.newTurn();
            for (int p = 0; p < n; p++) {
                int k = cur.selectSim(p, sim, mv, first, second, slot, sparse);
                if (!params.ownCardTree || p == me)
                    key |= ((long) k) << (8 * p);
            }
            nodes.add(cur);
            slots.add(slot);
            sim.playTurn(first, second);
            SushiTreeNode child = cur.isChildren.get(key);
            if (child == null) {
                child = new SushiTreeNode(player, cur, rnd);
                cur.isChildren.put(key, child);
                expanded = true;
            }
            cur = child;
        }
        double[] values = simLeafValues(sim, mv, cfg, params, startRound);
        for (SushiTreeNode x = cur; x != null; x = x.parent) {
            x.nVisits++;
            x.totValue += values[me];
        }
        for (int i = 0; i < nodes.size(); i++) {
            double[][] val = nodes.get(i).isVal;
            int[] slot = slots.get(i);
            for (int p = 0; p < slot.length; p++) {
                val[p][3 * slot[p]] += 1;
                val[p][3 * slot[p] + 1] += values[p];
            }
        }
    }

    /** MODIFICATION F: marginal card values of the players in the current turn of a SushiSim, computed on demand. */
    private static final class SimValues {
        final SushiSim sim;
        final CardValueModel.Config cfg;
        final double[][] mv;
        final boolean[] done;
        int[] makiAll, puddingAll, supply;
        boolean counters;

        private final int[] makiBuf, puddingBuf, supplyBuf;
        private final double[][] bias;   // MODIFICATION Q: bias[p][type ordinal] added to p's card values (null = none)

        SimValues(SushiSim sim, CardValueModel.Config cfg, double[][] bias) {
            this.sim = sim;
            this.cfg = cfg;
            this.bias = bias;
            mv = new double[sim.n][SushiSim.KINDS];
            done = new boolean[sim.n];
            makiBuf = new int[sim.n];
            puddingBuf = new int[sim.n];
            supplyBuf = new int[SushiSim.TYPE_COUNT];
        }

        void newTurn() {
            java.util.Arrays.fill(done, false);
            counters = false;
        }

        double[] of(int p) {
            if (!done[p]) {
                if (!counters) {
                    makiAll = sim.makiAll(makiBuf);
                    puddingAll = sim.puddingAll(puddingBuf);
                    supply = sim.supply(cfg, supplyBuf);
                    counters = true;
                }
                sim.handValues(p, mv[p], cfg, makiAll, puddingAll, supply);
                if (bias != null && bias[p] != null)   // MODIFICATION Q: the opponent's learned card-type biases
                    for (int k = 0; k < SushiSim.KINDS; k++)
                        if (sim.hand(p, k) > 0) mv[p][k] += bias[p][SushiSim.TYPE[k].ordinal()];
                done[p] = true;
            }
            return mv[p];
        }
    }

    /**
     * MODIFICATION F: selectInformationSet for a SushiSim. The candidates are the distinct cards of p's hand (and each
     * with Chopsticks when p can use them), with the same keys; the second card of a Chopsticks move is p's best other
     * card, as greedyAction chooses it in informationSetIteration.
     */
    private int selectSim(int p, SushiSim sim, SimValues values, int[] first, int[] second, int[] slotOut, boolean sparse) {
        SushiMCTSParams params = player.getParameters();
        int chopMax = sim.canChop(p) ? 1 : 0;
        int[] kinds = new int[2 * SushiSim.KINDS], chop = new int[2 * SushiSim.KINDS], slots = new int[2 * SushiSim.KINDS],
                keys = new int[2 * SushiSim.KINDS];
        int d = 0, untried = 0;
        for (int k = 0; k < SushiSim.KINDS; k++) {
            if (sim.hand(p, k) == 0) continue;
            for (int c = 0; c <= chopMax; c++) {
                int key = SushiSim.key(k) + c;
                int j = slot(p, key);
                double[] val = isVal[p];
                val[3 * j + 2] += 1;   // available in this iteration
                if (val[3 * j] == 0) untried++;
                kinds[d] = k;
                chop[d] = c;
                slots[d] = j;
                keys[d] = key;
                d++;
            }
        }
        double[] val = isVal[p];
        int pick = 0;
        if (sparse) {
            // rarely visited node: the card the rollout policy would play (best marginal value, random with prob. epsilon)
            if (!params.useInformedRollout || rnd.nextDouble() < params.rolloutEpsilon) {
                pick = rnd.nextInt(d);
            } else {
                double[] mv = values.of(p);
                double bestValue = -Double.MAX_VALUE;
                for (int x = 0; x < d; x++) {
                    double v = mv[kinds[x]] + (chop[x] == 1 ? sim.bestOther(p, mv, kinds[x]) - CardValueModel.CHOPSTICKS_VALUE : 0.0)
                            + 1e-6 * rnd.nextDouble();
                    if (v > bestValue) {
                        bestValue = v;
                        pick = x;
                    }
                }
            }
        } else if (untried > 0) {
            int r = rnd.nextInt(untried);
            for (int x = 0; x < d; x++)
                if (val[3 * slots[x]] == 0 && r-- == 0) {
                    pick = x;
                    break;
                }
        } else {
            double bestValue = -Double.MAX_VALUE;
            for (int x = 0; x < d; x++) {
                int j = 3 * slots[x];
                double v = val[j + 1] / val[j] + explorationOf(p) * Math.sqrt(Math.log(val[j + 2]) / val[j]);
                v = noise(v, params.epsilon, player.getRnd().nextDouble());
                if (v > bestValue) {
                    bestValue = v;
                    pick = x;
                }
            }
        }
        first[p] = kinds[pick];
        second[p] = chop[pick] == 1 ? simSecondCard(p, sim, values.of(p), kinds[pick]) : -1;
        slotOut[p] = slots[pick];
        return keys[pick];
    }

    /**
     * MODIFICATION F: second card of a Chopsticks move, after a first card of kind k: the best other card, as in
     * greedyAction (equal values are shared out by the number of such cards, like its random tie-break per card).
     */
    private int simSecondCard(int p, SushiSim sim, double[] mv, int k) {
        double best = -Double.MAX_VALUE;
        int pick = -1, weight = 0;
        for (int j = 0; j < SushiSim.KINDS; j++) {
            int c = sim.hand(p, j) - (j == k ? 1 : 0);
            if (c <= 0) continue;
            double v = mv[j];
            if (v > best + 1e-9) {
                best = v;
                pick = j;
                weight = c;
            } else if (v > best - 1e-9) {
                weight += c;
                if (rnd.nextInt(weight) < c) pick = j;
            }
        }
        return pick;
    }

    /** MODIFICATION F: a uniformly random card of p's hand other than one card of kind k. */
    private int randomOther(int p, SushiSim sim, int k) {
        int pos = rnd.nextInt(sim.handSize - 1);
        for (int j = 0; j < SushiSim.KINDS; j++) {
            pos -= sim.hand(p, j) - (j == k ? 1 : 0);
            if (pos < 0) return j;
        }
        throw new AssertionError("position outside the hand");
    }

    /**
     * MODIFICATION F: playOut on a SushiSim until the round is scored (rolloutRounds rounds), with the distribution of rolloutCard: with
     * probability 1 - epsilon the card (and Chopsticks use) with the best value, equal values shared out by the number
     * of such cards; otherwise a uniformly random card position (and Chopsticks use). The second card of a Chopsticks
     * move is chosen as in playOut: the best other card with probability 1 - epsilon, otherwise a random other card.
     */
    private void simRollout(SushiSim sim, SimValues values, SushiMCTSParams params) {
        int n = sim.n, startRound = sim.round, rounds = Math.max(1, params.rolloutRounds);
        int[] first = new int[n], second = new int[n];
        while (!sim.gameOver && sim.round - startRound < rounds) {
            values.newTurn();
            for (int p = 0; p < n; p++)
                simPolicyMove(p, sim, values, first, second, params, true);
            sim.playTurn(first, second);
        }
    }

    /**
     * MODIFICATION F: the rollout policy's move of player p on a SushiSim (into first[p] and second[p]). The second
     * card of a Chopsticks move follows the rollout policy too when policySecond is true (as in playOut), otherwise it
     * is p's best other card (as the tree's greedyAction). With rolloutTemperature T > 0 the informed move is not the
     * best card but a card drawn with probability proportional to exp(value / T) (each copy in the hand counts).
     */
    private void simPolicyMove(int p, SushiSim sim, SimValues values, int[] first, int[] second, SushiMCTSParams params,
                               boolean policySecond) {
        boolean chopOk = sim.canChop(p);
        int h = sim.handSize;
        boolean useChop;
        boolean informed = params.useInformedRollout && rnd.nextDouble() >= params.rolloutEpsilon;
        if (informed && params.rolloutTemperature > 0) {
            // softmax over the cards of the hand: card (and Chopsticks use) with value v has weight exp(v / T)
            double[] mv = values.of(p);
            double[] w = new double[2 * SushiSim.KINDS];
            double vmax = -Double.MAX_VALUE;
            for (int k = 0; k < SushiSim.KINDS; k++) {
                if (sim.hand(p, k) == 0) continue;
                for (int ch = 0; ch <= (chopOk ? 1 : 0); ch++) {
                    double v = mv[k] + (ch == 1 ? sim.bestOther(p, mv, k) - CardValueModel.CHOPSTICKS_VALUE : 0.0);
                    w[2 * k + ch] = v;
                    vmax = Math.max(vmax, v);
                }
            }
            double total = 0;
            for (int k = 0; k < SushiSim.KINDS; k++) {
                int c = sim.hand(p, k);
                for (int ch = 0; ch <= 1; ch++) {
                    int x = 2 * k + ch;
                    w[x] = c > 0 && (ch == 0 || chopOk) ? c * Math.exp((w[x] - vmax) / params.rolloutTemperature) : 0.0;
                    total += w[x];
                }
            }
            double r = rnd.nextDouble() * total;
            int pick = -1;
            for (int x = 0; x < w.length; x++) {
                if (w[x] == 0) continue;
                pick = x;
                r -= w[x];
                if (r < 0) break;
            }
            first[p] = pick / 2;
            useChop = pick % 2 == 1;
        } else if (informed) {
            double[] mv = values.of(p);
            double best = -Double.MAX_VALUE;
            int pick = -1, pickChop = 0, weight = 0;
            for (int k = 0; k < SushiSim.KINDS; k++) {
                int c = sim.hand(p, k);
                if (c == 0) continue;
                for (int ch = 0; ch <= (chopOk ? 1 : 0); ch++) {
                    double v = mv[k] + (ch == 1 ? sim.bestOther(p, mv, k) - CardValueModel.CHOPSTICKS_VALUE : 0.0);
                    if (v > best + 1e-9) {
                        best = v;
                        pick = k;
                        pickChop = ch;
                        weight = c;
                    } else if (v > best - 1e-9) {
                        weight += c;
                        if (rnd.nextInt(weight) < c) {
                            pick = k;
                            pickChop = ch;
                        }
                    }
                }
            }
            first[p] = pick;
            useChop = pickChop == 1;
        } else {
            int idx = rnd.nextInt(chopOk ? 2 * h : h);
            first[p] = sim.kindAt(p, chopOk ? idx / 2 : idx);
            useChop = chopOk && idx % 2 == 1;
        }
        if (!useChop) second[p] = -1;
        else if (!policySecond || (params.useInformedRollout && rnd.nextDouble() >= params.rolloutEpsilon))
            second[p] = simSecondCard(p, sim, values.of(p), first[p]);
        else
            second[p] = randomOther(p, sim, first[p]);
    }

    /**
     * MODIFICATIONS U + F: multiTreeIteration on a SushiSim (every player descends its own tree, keyed by its own
     * cards; a player who has left its tree plays the rollout policy).
     */
    private void simMultiTreeIteration() {
        SushiMCTSParams params = player.getParameters();
        int me = player.getPlayerID();
        if (simRoot == null) {
            simRoot = SushiSim.root((SGGameState) state, me);
            simSample = simRoot.emptyCopy();
            simScratch = new int[Math.max(1, simRoot.poolSize())];
        }
        SushiSim sim = simSample;
        sim.sampleFrom(simRoot, rnd, simScratch);
        CardValueModel.Config cfg = ((SushiGoHeuristic) params.heuristic).model();
        int n = sim.n, startRound = sim.round;
        if (mtRoots == null) {
            mtRoots = new SushiTreeNode[n];
            for (int p = 0; p < n; p++)
                mtRoots[p] = p == me ? this : new SushiTreeNode(player, this, rnd);
        }
        SushiTreeNode[] cur = mtRoots.clone();
        List<List<SushiTreeNode>> pathNodes = new ArrayList<>(n);
        List<List<Integer>> pathSlots = new ArrayList<>(n);
        for (int p = 0; p < n; p++) {
            pathNodes.add(new ArrayList<>());
            pathSlots.add(new ArrayList<>());
        }
        int[] slot = new int[n], key = new int[n], first = new int[n], second = new int[n];
        SimValues mv = new SimValues(sim, cfg, opponentBiases(sim.n));
        boolean inTree = true;
        for (int depth = 0; inTree && !sim.gameOver && depth < params.maxTreeDepth
                && !(params.stopAtRoundEnd && sim.round != startRound); depth++) {
            mv.newTurn();
            for (int p = 0; p < n; p++) {
                if (cur[p] != null) {
                    if (cur[p].isChildren == null)
                        cur[p].initInformationSets(n);
                    key[p] = cur[p].selectSim(p, sim, mv, first, second, slot, false);
                    pathNodes.get(p).add(cur[p]);
                    pathSlots.get(p).add(slot[p]);
                } else {
                    simPolicyMove(p, sim, mv, first, second, params, false);
                }
            }
            sim.playTurn(first, second);
            inTree = false;
            for (int p = 0; p < n; p++) {
                if (cur[p] == null) continue;
                SushiTreeNode child = cur[p].isChildren.get((long) key[p]);
                if (child == null) {
                    cur[p].isChildren.put((long) key[p], new SushiTreeNode(player, cur[p], rnd));
                    cur[p] = null;   // expanded: p plays the rollout policy from here
                } else {
                    cur[p] = child;
                    inTree = true;
                }
            }
        }
        double[] values = simLeafValues(sim, mv, cfg, params, startRound);
        for (int p = 0; p < n; p++) {
            List<SushiTreeNode> nodes = pathNodes.get(p);
            List<Integer> slots = pathSlots.get(p);
            for (int i = 0; i < nodes.size(); i++) {
                SushiTreeNode x = nodes.get(i);
                int j = slots.get(i);
                x.nVisits++;
                x.isVal[p][3 * j] += 1;
                x.isVal[p][3 * j + 1] += values[p];
            }
        }
    }

    /**
     * MODIFICATIONS F + L: the players' values of the leaf an iteration has reached: the mean over leafRollouts
     * rollouts from it (simRollout), or the leaf itself when the game (or, with stopAtRoundEnd, the round) is over.
     * More rollouts per leaf make each leaf value less noisy without adding tree iterations.
     */
    private double[] simLeafValues(SushiSim sim, SimValues mv, CardValueModel.Config cfg, SushiMCTSParams params, int startRound) {
        if (sim.gameOver || (params.stopAtRoundEnd && sim.round != startRound))
            return simValues(sim, cfg, params);
        int m = Math.max(1, params.leafRollouts);
        if (m == 1) {
            simRollout(sim, mv, params);
            return simValues(sim, cfg, params);
        }
        if (simLeaf == null) simLeaf = sim.emptyCopy();
        simLeaf.copyFrom(sim);
        double[] values = new double[sim.n];
        for (int r = 0; r < m; r++) {
            if (r > 0) sim.copyFrom(simLeaf);
            simRollout(sim, mv, params);
            double[] v = simValues(sim, cfg, params);
            for (int p = 0; p < values.length; p++) values[p] += v[p] / m;
        }
        return values;
    }

    /** MODIFICATION F: valuesOf for the end of a simulated round (the same values as for the game state). */
    private double[] simValues(SushiSim sim, CardValueModel.Config cfg, SushiMCTSParams params) {
        SushiGoHeuristic sh = (SushiGoHeuristic) params.heuristic;
        double[] est = sim.estimates(cfg);
        double[] values;
        if (params.terminalScale > 0 && sim.gameOver) {
            double[] x = new double[sim.n];
            for (int p = 0; p < sim.n; p++) x[p] = sim.score(p) + 0.01 * sim.pudding(p);
            values = terminalValues(x, params.terminalScale);
        } else {
            values = sh.valuesOf(est, roundScale(sim.par.nRounds, sim.round, params.sigmaRoundDecay));
        }
        if (params.opponentScoreValue) {
            int me = player.getPlayerID();
            double[] ref = rootEstimates(sh);
            for (int i = 0; i < values.length; i++)
                if (i != me) values[i] = sh.scoreValue(est, i, params.opponentRivalWeight, ref[i]);
        }
        return values;
    }

    /** opponentScoreValue: every player's estimated score at the root of the search (computed once per search). */
    private double[] rootEstimates(SushiGoHeuristic sh) {
        if (root.rootEst == null)
            root.rootEst = sh.estimates((SGGameState) root.state);
        return root.rootEst;
    }

    /**
     * MODIFICATION I: card of player p at this node for the current sample. The candidates are the distinct cards
     * p holds in the sample, in the order of the forward model's actions (card i, then card i with chopsticks when p
     * has Chopsticks on the table and two or more cards), and each gets one more availability. Untried cards first,
     * then UCB1 with the card's availability in place of the parent's visit count. The action goes to chosen[p], its
     * statistics index to slotOut[p]; the card key is returned.
     */
    private int selectInformationSet(int p, SGGameState sg, AbstractAction[] chosen, int[] slotOut, boolean sparse) {
        SushiMCTSParams params = player.getParameters();
        int m = 2 * sg.getPlayerHands().get(p).getSize();
        int[] keys = new int[m], cardIdx = new int[m], chop = new int[m], slots = new int[m];
        int d = CardValueModel.distinctCards(sg, p, keys, cardIdx, chop);
        int untried = 0;
        double[] val;
        for (int x = 0; x < d; x++) {
            int j = slot(p, keys[x]);
            val = isVal[p];
            val[3 * j + 2] += 1;   // available in this iteration
            if (val[3 * j] == 0) untried++;
            slots[x] = j;
        }
        val = isVal[p];
        int pick = 0;
        if (sparse) {
            // rarely visited node: the card the rollout policy would play (best marginal value, random with prob. epsilon)
            if (!params.useInformedRollout || rnd.nextDouble() < params.rolloutEpsilon) {
                pick = rnd.nextInt(d);
            } else {
                double[] mv = policyValues(sg, p);
                double bestValue = -Double.MAX_VALUE;
                for (int x = 0; x < d; x++) {
                    double v = chopsticksValue(mv, cardIdx[x], chop[x] == 1) + 1e-6 * rnd.nextDouble();
                    if (v > bestValue) {
                        bestValue = v;
                        pick = x;
                    }
                }
            }
        } else if (untried > 0) {
            int r = rnd.nextInt(untried);
            for (int x = 0; x < d; x++)
                if (val[3 * slots[x]] == 0 && r-- == 0) {
                    pick = x;
                    break;
                }
        } else {
            double bestValue = -Double.MAX_VALUE;
            for (int x = 0; x < d; x++) {
                int j = 3 * slots[x];
                double v = val[j + 1] / val[j] + explorationOf(p) * Math.sqrt(Math.log(val[j + 2]) / val[j]);
                v = noise(v, params.epsilon, player.getRnd().nextDouble());
                if (v > bestValue) {
                    bestValue = v;
                    pick = x;
                }
            }
        }
        chosen[p] = new ChooseCard(p, cardIdx[pick], chop[pick] == 1);
        slotOut[p] = slots[pick];
        return keys[pick];
    }

    private void setState(AbstractGameState newState) {
        state = newState;
        if (newState.isNotTerminal()) {
            List<AbstractAction> actions = player.getForwardModel().computeAvailableActions(state, player.getParameters().actionSpace);
            // MODIFICATION P: two copies of the same card lead to the same next state, so keep one action per distinct card
            if (player.getParameters().useActionPruning && state instanceof SGGameState sg)
                actions = CardValueModel.distinctActions(sg, actions);
            for (AbstractAction action : actions) {
                children.put(action, null); // mark a new node to be expanded
            }
            // MODIFICATION S: a simultaneous turn starts here (no player has chosen a card yet)
            if (player.getParameters().useDecoupledUCT && isTurnStart(newState))
                initTurn((SGGameState) newState);
        }
    }

    @SuppressWarnings("unchecked")
    private void initTurn(SGGameState sg) {
        int n = sg.getNPlayers();
        turnActions = new List[n];
        turnStats = new Map[n];
        for (int p = 0; p < n; p++) {
            List<AbstractAction> actions = player.getForwardModel().computeAvailableActions(sg, player.getParameters().actionSpace, p);
            if (player.getParameters().useActionPruning)
                actions = CardValueModel.distinctActions(sg, actions);
            turnActions[p] = actions;
            turnStats[p] = new HashMap<>();
        }
        jointChildren = new HashMap<>();
    }

    /**
     * MODIFICATION S: true if no player has chosen a card in this Sushi Go turn yet
     * (getCurrentSimultaneousPlayers lists the players whose choice is still missing).
     */
    private static boolean isTurnStart(AbstractGameState s) {
        return s instanceof SGGameState sg && !sg.isActionInProgress()
                && sg.getCurrentSimultaneousPlayers().size() == sg.getNPlayers();
    }


    /**
     * @return A list of the unexpanded Actions from this State
     */
    private List<AbstractAction> unexpandedActions() {
        return children.keySet().stream().filter(a -> children.get(a) == null).collect(toList());
    }

    /**
     * MODIFICATION A: prior b(a) in (-1, 1) for every action of this node, computed once (lazily).
     */
    private Map<AbstractAction, Double> getActionPriors() {
        if (actionPriors == null) {
            actionPriors = new HashMap<>();
            double scale = player.getParameters().priorScale;
            CardValueModel.View view = (state instanceof SGGameState sg) ? cardView(sg) : null;
            for (AbstractAction a : children.keySet()) {
                double mv = (view != null) ? CardValueModel.actionValue(view, a) : 0.0;
                actionPriors.put(a, Math.tanh(mv / scale));
            }
        }
        return actionPriors;
    }

    /**
     * Expands the node by creating a new child node and adding it to the tree.
     */
    private SushiTreeNode expand() {
        List<AbstractAction> notChosen = unexpandedActions();
        AbstractAction chosen;
        if (player.getParameters().useProgressiveBias) {
            // MODIFICATION A: expand the most promising action first (ties broken at random)
            Map<AbstractAction, Double> priors = getActionPriors();
            chosen = null;
            double best = -Double.MAX_VALUE;
            for (AbstractAction a : notChosen) {
                double v = priors.get(a) + 1e-6 * rnd.nextDouble();
                if (v > best) {
                    best = v;
                    chosen = a;
                }
            }
        } else {
            // Original BasicTreeNode behaviour: pick a random unchosen action
            Random r = new Random(player.getParameters().getRandomSeed());
            chosen = notChosen.get(r.nextInt(notChosen.size()));
        }
        return expand(chosen);
    }

    /**
     * Adds the child reached by the given action (also used by MODIFICATION O for the opponent's sampled card).
     */
    private SushiTreeNode expand(AbstractAction chosen) {
        // copy the current state and advance it using the chosen action
        // we first copy the action so that the one stored in the node will not have any state changes
        AbstractGameState nextState = state.copy();
        advance(nextState, chosen.copy());

        // then instantiate a new node
        SushiTreeNode tn = new SushiTreeNode(player, this, nextState, rnd);
        if (player.getParameters().useProgressiveBias)
            tn.prior = getActionPriors().get(chosen);
        children.put(chosen, tn);
        return tn;
    }

    /**
     * Advance the current game state with the given action, count the FM call and compute the next available actions.
     */
    private void advance(AbstractGameState gs, AbstractAction act) {
        player.getForwardModel().next(gs, act);
        root.fmCallsCount++;
    }

    private AbstractAction ucb() {
        // Find child with highest UCB value, maximising for ourselves and minimizing for opponent
        AbstractAction bestAction = null;
        double bestValue = -Double.MAX_VALUE;
        SushiMCTSParams params = player.getParameters();

        for (AbstractAction action : children.keySet()) {
            SushiTreeNode child = children.get(action);
            if (child == null)
                throw new AssertionError("Should not be here");
            else if (bestAction == null)
                bestAction = action;

            // Find child value
            double hvVal = child.totValue;
            double childValue = hvVal / (child.nVisits + params.epsilon);

            // default to standard UCB
            double explorationTerm = params.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + params.epsilon));

            double uctValue;
            if (params.useMaxN) {
                // MODIFICATION M (max^n, Sturtevant 2008): whoever moves here maximises its OWN value
                uctValue = child.playerValues[state.getCurrentPlayer()] / (child.nVisits + params.epsilon);
            } else {
                // If 'we' are taking a turn we use classic UCB
                // If it is an opponent's turn, then we assume they are trying to minimise our score (with exploration)
                boolean iAmMoving = state.getCurrentPlayer() == player.getPlayerID();
                uctValue = iAmMoving ? childValue : -childValue;
            }
            uctValue += explorationTerm;

            // MODIFICATION A: progressive bias, fades as 1/(n+1). The prior is from the point of view
            // of the player who moves here, so for opponents it predicts the cards THEY like.
            if (params.useProgressiveBias) {
                uctValue += params.biasWeight * child.prior / (child.nVisits + 1.0);
            }

            // Apply small noise to break ties randomly
            uctValue = noise(uctValue, params.epsilon, player.getRnd().nextDouble());

            // Assign value
            if (uctValue > bestValue) {
                bestAction = action;
                bestValue = uctValue;
            }
        }

        if (bestAction == null)
            throw new AssertionError("We have a null value in UCT : shouldn't really happen!");

        root.fmCallsCount++;  // log one iteration complete
        return bestAction;
    }

    /**
     * Perform a Monte Carlo rollout from this node.
     */
    private double rollOut() {
        // Evaluate final state and return normalised score
        double value = player.getParameters().getStateHeuristic().evaluateState(playOut(), player.getPlayerID());
        if (Double.isNaN(value))
            throw new AssertionError("Illegal heuristic value - should be a number");
        return value;
    }

    /**
     * MODIFICATION M: same rollout, but the final state is evaluated from the point of view of every player.
     */
    private double[] rollOutAll() {
        return valuesOf(playOut());
    }

    /**
     * MODIFICATION M: value of a (rollout end) state for every player.
     */
    private double[] valuesOf(AbstractGameState end) {
        SushiMCTSParams params = player.getParameters();
        IStateHeuristic h = params.getStateHeuristic();
        if (params.opponentScoreValue && h instanceof SushiGoHeuristic sh && end instanceof SGGameState sg) {
            // opponent model: our value as below, every other player only plays for its own score
            double[] est = sh.estimates(sg);
            double[] values = (params.terminalScale > 0 && !end.isNotTerminal()) ? terminalValues(end, params.terminalScale)
                    : sh.valuesOf(est, roundScale(sg, params.sigmaRoundDecay));
            int me = player.getPlayerID();
            double[] ref = rootEstimates(sh);
            for (int i = 0; i < values.length; i++)
                if (i != me) values[i] = sh.scoreValue(est, i, params.opponentRivalWeight, ref[i]);
            return values;
        }
        if (params.terminalScale > 0 && !end.isNotTerminal())
            return terminalValues(end, params.terminalScale);
        if (h instanceof SushiGoHeuristic sh && end instanceof SGGameState sg)
            return sh.evaluateAll(sg, roundScale(sg, params.sigmaRoundDecay));   // one pass over the players instead of n
        double[] values = new double[end.getNPlayers()];
        for (int p = 0; p < values.length; p++)
            values[p] = h.evaluateState(end, p);
        return values;
    }

    /**
     * Leaf value scale (sigmaRoundDecay = d > 0): the score gap left to make up matters more when fewer rounds are
     * left, so sigma is multiplied by (R / 2)^d, R = rounds still to play at the leaf (a finished game counts as 0.5).
     */
    static double roundScale(SGGameState sg, double d) {
        return roundScale(((games.sushigo.SGParameters) sg.getGameParameters()).nRounds, sg.getRoundCounter(), d);
    }

    static double roundScale(int nRounds, int roundCounter, double d) {
        if (d <= 0) return 1.0;
        double r = Math.max(0.5, nRounds - roundCounter);
        return Math.pow(r / 2.0, d);
    }

    /**
     * MODIFICATION V: value of a finished game for every player, tanh((S_i - max_{j != i} S_j) / sigma_V). Only the
     * winner scores in TAG, so the gap to the best opponent counts, not the gap to the average; ties are broken by
     * Pudding count as in SGGameState.getTiebreak.
     */
    static double[] terminalValues(AbstractGameState end, double scale) {
        int n = end.getNPlayers();
        double[] x = new double[n];
        for (int p = 0; p < n; p++)
            x[p] = end.getGameScore(p) + 0.01 * end.getTiebreak(p, 1);
        return terminalValues(x, scale);
    }

    /** terminalValues from the final scores plus 0.01 * Pudding (the tie-break) of every player. */
    static double[] terminalValues(double[] x, double scale) {
        int n = x.length;
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            double best = Double.NEGATIVE_INFINITY;
            for (int p = 0; p < n; p++) if (p != i) best = Math.max(best, x[p]);
            values[i] = Math.tanh((x[i] - best) / scale);
        }
        return values;
    }

    /**
     * Plays the rollout policy from this node's state and returns the state where the rollout stopped.
     */
    private AbstractGameState playOut() {
        return playOut(state.copy());
    }

    /**
     * Plays the rollout policy on the given state (which is changed) and returns it.
     */
    private AbstractGameState playOut(AbstractGameState rolloutState) {
        int rolloutDepth = 0; // counting from end of tree
        SushiMCTSParams params = player.getParameters();

        int startRound = rolloutState.getRoundCounter();
        if (params.rolloutLength > 0 || params.rolloutToRoundEnd) {
            while (!finishRollout(rolloutState, rolloutDepth, startRound)) {
                AbstractAction next;
                if (rolloutState instanceof SGGameState sg && !sg.isActionInProgress()) {
                    // Same choice as below, made from the hand without building the list of actions
                    next = rolloutCard(sg, params);
                } else {
                    List<AbstractAction> available = player.getForwardModel().computeAvailableActions(rolloutState, params.actionSpace);
                    if (params.useInformedRollout && rnd.nextDouble() >= params.rolloutEpsilon
                            && rolloutState instanceof SGGameState sg) {
                        // MODIFICATION B: greedy w.r.t. the marginal card value of the player who moves
                        next = greedyAction(sg, available);
                    } else {
                        // Original behaviour: uniformly random action
                        next = available.get(rnd.nextInt(available.size()));
                    }
                    if (root.mastN != null && next instanceof ChooseCard cc && rolloutState instanceof SGGameState sg2)
                        root.mastRecord(cc.playerId, CardValueModel.actionKey(sg2, cc));   // MODIFICATION G
                }
                advance(rolloutState, next);
                rolloutDepth++;
            }
        }
        return rolloutState;
    }

    /**
     * Rollout move of the player who picks a card at the start of its pick (no chopsticks move in progress): exactly
     * the choice of the general rollout step - same candidates in the same order as the forward model lists them
     * (card i, then card i with chopsticks when the player has Chopsticks on the table and two or more cards), same
     * random numbers - but without creating an action object for every card in the hand.
     */
    private AbstractAction rolloutCard(SGGameState sg, SushiMCTSParams params) {
        int p = sg.getCurrentPlayer();
        int h = sg.getPlayerHands().get(p).getSize();
        boolean chop = sg.getPlayedCardTypes(games.sushigo.cards.SGCard.SGCardType.Chopsticks, p).getValue() > 0 && h > 1;
        boolean mast = root.mastN != null;   // MODIFICATION G: MAST values also steer the greedy choice
        int[] keys = mast ? CardValueModel.handKeys(sg, p) : null;
        if (params.useInformedRollout && rnd.nextDouble() >= params.rolloutEpsilon) {
            // MODIFICATION B: greedy w.r.t. the marginal card value of the player who moves
            double[] mv = policyValues(sg, p);
            int bestIdx = 0;
            boolean bestChop = false;
            double bestValue = -Double.MAX_VALUE;
            for (int i = 0; i < h; i++) {
                for (int c = 0; c <= (chop ? 1 : 0); c++) {
                    double v = chopsticksValue(mv, i, c == 1);
                    if (mast)
                        v += params.mastWeight * root.mastValue(p, keys[i] + c);
                    v += 1e-6 * rnd.nextDouble();
                    if (v > bestValue) {
                        bestValue = v;
                        bestIdx = i;
                        bestChop = c == 1;
                    }
                }
            }
            if (mast) root.mastRecord(p, keys[bestIdx] + (bestChop ? 1 : 0));
            return new ChooseCard(p, bestIdx, bestChop);
        }
        // Original behaviour: uniformly random action
        int idx = rnd.nextInt(chop ? 2 * h : h);
        int i = chop ? idx / 2 : idx;
        boolean c = chop && idx % 2 == 1;
        if (mast) root.mastRecord(p, keys[i] + (c ? 1 : 0));
        return new ChooseCard(p, i, c);
    }

    /**
     * MODIFICATION O: card chosen by a modelled opponent at this node - the greedy card of the card-value
     * model (computed once per node), or a random card with probability opponentEpsilon.
     */
    private AbstractAction opponentAction(SGGameState sg) {
        List<AbstractAction> available = new ArrayList<>(children.keySet());
        if (rnd.nextDouble() < player.getParameters().opponentEpsilon)
            return available.get(rnd.nextInt(available.size()));
        if (greedyOpponentAction == null)
            greedyOpponentAction = greedyAction(sg, available);
        return greedyOpponentAction;
    }

    /**
     * Card-model view of a state, with the constants of our heuristic when it is a SushiGoHeuristic,
     * so that the rollout policy and the evaluation always agree.
     */
    private CardValueModel.View cardView(SGGameState sg) {
        return player.getParameters().heuristic instanceof SushiGoHeuristic sh ? sh.view(sg) : new CardValueModel.View(sg);
    }

    /**
     * Card values of player p's hand (in hand order) for the rollout policy: the card-value model, plus (MODIFICATION
     * Q) the learned bias of p's card types when p is an opponent.
     */
    private double[] policyValues(SGGameState sg, int p) {
        double[] mv = CardValueModel.handValues(cardView(sg), p);
        OpponentModel om = player.getParameters().useOpponentBias ? player.opponentModel : null;
        if (om != null && p != player.getPlayerID()) {
            List<SGCard> hand = sg.getPlayerHands().get(p).getComponents();
            for (int i = 0; i < mv.length; i++) mv[i] += om.bias(p, hand.get(i).type);
        }
        return mv;
    }

    /** MODIFICATION Q: the learned card-type biases of every player (null for us), or null when Q is off. */
    private double[][] opponentBiases(int n) {
        OpponentModel om = player.getParameters().useOpponentBias ? player.opponentModel : null;
        if (om == null) return null;
        double[][] b = new double[n][];
        for (int q = 0; q < n; q++) b[q] = q == player.getPlayerID() ? null : om.biasOf(q);
        return b;
    }

    /**
     * MODIFICATION B: action with the highest marginal value (random tie-break). No forward-model calls.
     */
    private AbstractAction greedyAction(SGGameState sg, List<AbstractAction> available) {
        AbstractAction best = null;
        double bestValue = -Double.MAX_VALUE;
        CardValueModel.View view = cardView(sg);   // counters read once for all candidate cards
        // marginal value of every card in the mover's hand, computed once: the same numbers as
        // CardValueModel.actionValue, which looks each card up again (a hand is a linked list) for every action
        double[] mv = CardValueModel.handValues(view, ((ChooseCard) available.get(0)).playerId);
        for (AbstractAction a : available) {
            ChooseCard cc = (ChooseCard) a;
            double v = chopsticksValue(mv, cc.cardIdx, cc.useChopsticks) + 1e-6 * rnd.nextDouble();
            if (v > bestValue) {
                bestValue = v;
                best = a;
            }
        }
        return best;
    }

    /**
     * Value of playing card cardIdx (mv = handValues of the mover) = CardValueModel.actionValue: with chopsticks the
     * best other card is added (at least 0) and the Chopsticks go back into the hand.
     */
    private static double chopsticksValue(double[] mv, int cardIdx, boolean useChopsticks) {
        double v = mv[cardIdx];
        if (useChopsticks) {
            double bestOther = 0.0;
            for (int i = 0; i < mv.length; i++)
                if (i != cardIdx) bestOther = Math.max(bestOther, mv[i]);
            v += bestOther - CardValueModel.CHOPSTICKS_VALUE;
        }
        return v;
    }

    /**
     * Checks if rollout is finished. Rollouts end on maximum length, or if game ended.
     */
    private boolean finishRollout(AbstractGameState rollerState, int depth, int startRound) {
        // End of game
        if (!rollerState.isNotTerminal())
            return true;
        // MODIFICATION R: play on until the current round has been scored (Maki and all sets resolved exactly);
        // rolloutRounds > 1 plays the following rounds too
        if (player.getParameters().rolloutToRoundEnd)
            return rollerState.getRoundCounter() - startRound >= Math.max(1, player.getParameters().rolloutRounds);
        return depth >= player.getParameters().rolloutLength;
    }

    /**
     * Back up the value of the child through all parents. Increase number of visits and total value.
     */
    private void backUp(double result) {
        SushiTreeNode n = this;
        while (n != null) {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }

    /**
     * MODIFICATION M: back up one value per player (totValue keeps our own value as before).
     */
    private void backUpAll(double[] results) {
        int me = player.getPlayerID();
        SushiTreeNode n = this;
        while (n != null) {
            n.nVisits++;
            n.totValue += results[me];
            for (int p = 0; p < results.length; p++)
                n.playerValues[p] += results[p];
            n = n.parent;
        }
    }

    /**
     * Calculates the best action from the root according to the most visited node
     */
    AbstractAction bestAction() {

        double bestValue = -Double.MAX_VALUE;
        AbstractAction bestAction = null;

        // MODIFICATION I: our card with the most visits at the root (our own hand is the same in every sample)
        if (isChildren != null) {
            int me = player.getPlayerID();
            SushiMCTSParams params = player.getParameters();
            List<AbstractAction> actions = player.getForwardModel().computeAvailableActions(state, params.actionSpace);
            double maxVisits = 0;
            if (params.maxChildShare > 0)
                for (AbstractAction a : actions)
                    maxVisits = Math.max(maxVisits, visitsOf(me, CardValueModel.actionKey((SGGameState) state, (ChooseCard) a)));
            for (AbstractAction a : actions) {
                int key = CardValueModel.actionKey((SGGameState) state, (ChooseCard) a);
                double visits = visitsOf(me, key);
                double v = visits;
                // maxChildShare > 0: the card with the best mean value among the cards with at least that share of
                // the visits of the most visited card ("max child" instead of "robust child", Chaslot et al. 2008)
                if (params.maxChildShare > 0)
                    v = visits >= params.maxChildShare * maxVisits ? meanOf(me, key) : -1e9 + visits;
                v = noise(v, params.epsilon, player.getRnd().nextDouble());
                if (v > bestValue) {
                    bestValue = v;
                    bestAction = a;
                }
            }
            return bestAction;
        }

        // MODIFICATION S: our card with the most visits in our own statistics at the root
        if (jointChildren != null && !jointChildren.isEmpty()) {
            for (Map.Entry<AbstractAction, double[]> e : turnStats[player.getPlayerID()].entrySet()) {
                double v = noise(e.getValue()[0], player.getParameters().epsilon, player.getRnd().nextDouble());
                if (v > bestValue) {
                    bestValue = v;
                    bestAction = e.getKey();
                }
            }
            return bestAction;
        }

        for (AbstractAction action : children.keySet()) {
            if (children.get(action) != null) {
                SushiTreeNode node = children.get(action);
                double childValue = node.nVisits;

                // Apply small noise to break ties randomly
                childValue = noise(childValue, player.getParameters().epsilon, player.getRnd().nextDouble());

                // Save best value (highest visit count)
                if (childValue > bestValue) {
                    bestValue = childValue;
                    bestAction = action;
                }
            }
        }

        if (bestAction == null) {
            throw new AssertionError("Unexpected - no selection made.");
        }

        return bestAction;
    }

    /**
     * MODIFICATION D (used by SushiMCTSPlayer): add the visit counts of the root's children to a shared tally.
     */
    void addRootVisits(Map<AbstractAction, Double> tally) {
        if (isChildren != null) {   // MODIFICATION I
            int me = player.getPlayerID();
            for (AbstractAction a : player.getForwardModel().computeAvailableActions(state, player.getParameters().actionSpace))
                tally.merge(a, visitsOf(me, CardValueModel.actionKey((SGGameState) state, (ChooseCard) a)), Double::sum);
            return;
        }
        if (jointChildren != null && !jointChildren.isEmpty()) {   // MODIFICATION S
            for (Map.Entry<AbstractAction, double[]> e : turnStats[player.getPlayerID()].entrySet())
                tally.merge(e.getKey(), e.getValue()[0], Double::sum);
            return;
        }
        for (Map.Entry<AbstractAction, SushiTreeNode> e : children.entrySet()) {
            if (e.getValue() != null)
                tally.merge(e.getKey(), (double) e.getValue().nVisits, Double::sum);
        }
    }
}
