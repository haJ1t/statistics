package groupA;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import games.sushigo.SGGameState;
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
 * Changes are marked with "MODIFICATION A" / "B" / "P" / "O" / "M" / "R" and are switched on/off from
 * SushiMCTSParams, so that the ablation study runs exactly the same code.
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

    protected SushiTreeNode(SushiMCTSPlayer player, SushiTreeNode parent, AbstractGameState state, Random rnd) {
        this.player = player;
        this.fmCallsCount = 0;
        this.parent = parent;
        this.root = parent == null ? this : parent.root;
        totValue = 0.0;
        if (player.getParameters().useMaxN)
            playerValues = new double[state.getNPlayers()];
        setState(state);
        if (parent != null) {
            depth = parent.depth + 1;
        } else {
            depth = 0;
        }
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
            // New timer for this iteration
            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

            // Selection + expansion: navigate tree until a node not fully expanded is found, add a new node to the tree
            SushiTreeNode selected = treePolicy();
            if (params.useMaxN) {
                // MODIFICATION M: evaluate the rollout for every player and back up the whole vector
                selected.backUpAll(selected.rollOutAll());
            } else {
                // Monte carlo rollout: return value of MC rollout from the newly added node
                double delta = selected.rollOut();
                // Back up the value of the rollout through the tree
                selected.backUp(delta);
            }
            // Finished iteration
            numIters++;

            // Check stopping condition
            PlayerConstants budgetType = params.budgetType;
            if (budgetType == BUDGET_TIME) {
                // Time budget
                acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
                avgTimeTaken = acumTimeTaken / numIters;
                remaining = elapsedTimer.remainingTimeMillis();
                stop = remaining <= 2 * avgTimeTaken || remaining <= breakMS
                        || (wallDeadline != Long.MAX_VALUE && System.nanoTime() >= wallDeadline);  // safety: real-time limit
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
        }
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
        AbstractGameState end = playOut();
        IStateHeuristic h = player.getParameters().getStateHeuristic();
        if (h instanceof SushiGoHeuristic sh && end instanceof SGGameState sg)
            return sh.evaluateAll(sg);   // one pass over the players instead of n
        double[] values = new double[end.getNPlayers()];
        for (int p = 0; p < values.length; p++)
            values[p] = h.evaluateState(end, p);
        return values;
    }

    /**
     * Plays the rollout policy from this node's state and returns the state where the rollout stopped.
     */
    private AbstractGameState playOut() {
        int rolloutDepth = 0; // counting from end of tree
        SushiMCTSParams params = player.getParameters();

        AbstractGameState rolloutState = state.copy();
        int startRound = rolloutState.getRoundCounter();
        if (params.rolloutLength > 0 || params.rolloutToRoundEnd) {
            while (!finishRollout(rolloutState, rolloutDepth, startRound)) {
                List<AbstractAction> available = player.getForwardModel().computeAvailableActions(rolloutState, params.actionSpace);
                AbstractAction next;
                if (params.useInformedRollout && rnd.nextDouble() >= params.rolloutEpsilon
                        && rolloutState instanceof SGGameState sg) {
                    // MODIFICATION B: greedy w.r.t. the marginal card value of the player who moves
                    next = greedyAction(sg, available);
                } else {
                    // Original behaviour: uniformly random action
                    next = available.get(rnd.nextInt(available.size()));
                }
                advance(rolloutState, next);
                rolloutDepth++;
            }
        }
        return rolloutState;
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
     * MODIFICATION B: action with the highest marginal value (random tie-break). No forward-model calls.
     */
    private AbstractAction greedyAction(SGGameState sg, List<AbstractAction> available) {
        AbstractAction best = null;
        double bestValue = -Double.MAX_VALUE;
        CardValueModel.View view = cardView(sg);   // counters read once for all candidate cards
        for (AbstractAction a : available) {
            double v = CardValueModel.actionValue(view, a) + 1e-6 * rnd.nextDouble();
            if (v > bestValue) {
                bestValue = v;
                best = a;
            }
        }
        return best;
    }

    /**
     * Checks if rollout is finished. Rollouts end on maximum length, or if game ended.
     */
    private boolean finishRollout(AbstractGameState rollerState, int depth, int startRound) {
        // End of game
        if (!rollerState.isNotTerminal())
            return true;
        // MODIFICATION R: play on until the current round has been scored (Maki and all sets resolved exactly)
        if (player.getParameters().rolloutToRoundEnd)
            return rollerState.getRoundCounter() != startRound;
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
        for (Map.Entry<AbstractAction, SushiTreeNode> e : children.entrySet()) {
            if (e.getValue() != null)
                tally.merge(e.getKey(), (double) e.getValue().nVisits, Double::sum);
        }
    }
}
