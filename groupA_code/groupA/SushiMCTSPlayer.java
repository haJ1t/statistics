package groupA;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import core.interfaces.IStateHeuristic;
import games.sushigo.SGGameState;
import players.PlayerConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static utilities.Utils.noise;

/**
 * SushiMCTSPlayer - copied from players.basicMCTS.BasicMCTSPlayer (permitted base agent).
 * <p>
 * _getAction() is changed to add Modification D: Information-Aware Determinisation (v2, v3; the final agent
 * uses I instead, see below).
 * BasicMCTS searches ONE sampled ("determinised") version of the hidden cards and treats it as
 * the truth. D instead builds M independent trees, each on a fresh sample of the hidden hands
 * (gameState.copy(playerId) re-shuffles everything this player cannot see), splits the budget
 * between them and returns the action with the most visits summed over all trees
 * (Perfect Information Monte Carlo / determinised MCTS).
 * <p>
 * Sushi Go specific part: M is chosen from how many opponent hands are still unknown
 * (SGGameState.isHandKnown). After n-1 passes every hand in the round is known, so M = 1 and
 * the whole budget goes into a single, deeper tree.
 * <p>
 * With Modification I (SushiTreeNode) the hidden cards are sampled again in every iteration of a single tree,
 * so D is not used (M = 1). Modification E (ensembleTrees > 1) runs the same split-and-vote loop with M independent
 * I trees instead.
 * <p>
 * Safety (not an AI change): the time budget is CPU time, as in BasicMCTS. If wallTimeLimitMS > 0 the search
 * also stops when that much real time has passed, so a busy machine cannot push a decision over the time limit.
 * <p>
 * One JSON for every table size: if the parameters have a fourPlayer block, decisions in games with four or more
 * players use it (with this JSON's time budget); getParameters() returns the parameters of the current decision.
 */
public class SushiMCTSPlayer extends AbstractPlayer {

    /** Number of determinisations used at the last decision (for logging / analysis). */
    public int lastDeterminisations = 1;
    /** Total MCTS iterations at the last decision (for logging / analysis). */
    public int lastIterations = 0;

    public SushiMCTSPlayer() {
        this(new SushiMCTSParams());
    }

    public SushiMCTSPlayer(SushiMCTSParams params) {
        super(params, "SushiMCTS");
        rnd = new Random(params.getRandomSeed());
    }

    /** Parameters of the decision being made (null between decisions: then getParameters() gives the JSON's). */
    private SushiMCTSParams active = null;
    /** SushiMCTSParams.fourPlayer with the budget fields of the JSON, made at the first 4+ player decision. */
    private SushiMCTSParams fourPlayerParams = null;

    /** MODIFICATION Q: what we learned about the opponents in this game (null when off or before the first decision). */
    OpponentModel opponentModel = null;

    @Override
    public void initializePlayer(AbstractGameState gameState) {
        super.initializePlayer(gameState);
        opponentModel = null;   // a new game: nothing known about the opponents yet
    }

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> actions) {
        active = paramsFor(gameState.getNPlayers());
        try {
            if (active.useOpponentBias && gameState instanceof SGGameState sg && !sg.isActionInProgress()) {
                if (opponentModel == null || !opponentModel.fits(sg.getNPlayers(), getPlayerID()))
                    opponentModel = new OpponentModel(sg.getNPlayers(), getPlayerID(), active.opponentBiasTemperature,
                            active.opponentBiasPrior);
                CardValueModel.Config cfg = active.heuristic instanceof SushiGoHeuristic sh ? sh.model() : CardValueModel.Config.DEFAULT;
                opponentModel.observe(sg, cfg);
            }
            return search(gameState, actions);
        } finally {
            active = null;
        }
    }

    /** The JSON's parameters, or its fourPlayer parameters (with the JSON's budget) in games of four or more players. */
    private SushiMCTSParams paramsFor(int nPlayers) {
        SushiMCTSParams top = (SushiMCTSParams) parameters;
        if (nPlayers < 4 || top.fourPlayer == null) return top;
        if (fourPlayerParams == null) {
            SushiMCTSParams p = (SushiMCTSParams) top.fourPlayer.copy();
            p.budgetType = top.budgetType;
            p.budget = top.budget;
            p.breakMS = top.breakMS;
            p.wallTimeLimitMS = top.wallTimeLimitMS;
            fourPlayerParams = p;
        }
        return fourPlayerParams;
    }

    private AbstractAction search(AbstractGameState gameState, List<AbstractAction> actions) {
        SushiMCTSParams params = getParameters();
        int m = numberOfDeterminisations(gameState);
        lastDeterminisations = m;
        // Safety: real-time deadline of this decision (Long.MAX_VALUE = no wall-clock limit)
        long wallDeadline = (params.budgetType == PlayerConstants.BUDGET_TIME && params.wallTimeLimitMS > 0)
                ? System.nanoTime() + params.wallTimeLimitMS * 1_000_000L : Long.MAX_VALUE;

        if (m <= 1) {
            // Identical to BasicMCTSPlayer: one tree on the state we were given
            SushiTreeNode root = new SushiTreeNode(this, null, gameState, rnd);
            root.mctsSearch(params.budget, params.breakMS, wallDeadline);
            lastIterations = root.getIterations();
            return root.bestAction();
        }

        // Modification D: split the budget between M trees, one per determinisation
        int usable = (params.budgetType == PlayerConstants.BUDGET_TIME) ? params.budget - params.breakMS : params.budget;
        int share = Math.max(1, usable / m);
        Map<AbstractAction, Double> visits = new HashMap<>();
        lastIterations = 0;
        for (int i = 0; i < m; i++) {
            // first tree uses the state we received (already a sample); the others re-sample hidden cards
            AbstractGameState det = (i == 0) ? gameState : gameState.copy(getPlayerID());
            SushiTreeNode root = new SushiTreeNode(this, null, det, rnd);
            // the real time still left is shared equally between the remaining trees
            long now = System.nanoTime();
            long deadline = (wallDeadline == Long.MAX_VALUE) ? Long.MAX_VALUE : now + (wallDeadline - now) / (m - i);
            root.mctsSearch(share, 0, deadline);
            root.addRootVisits(visits);
            lastIterations += root.getIterations();
        }

        // Majority vote: action with the highest total visit count (noise breaks ties)
        AbstractAction best = null;
        double bestValue = -Double.MAX_VALUE;
        for (Map.Entry<AbstractAction, Double> e : visits.entrySet()) {
            double v = noise(e.getValue(), params.epsilon, rnd.nextDouble());
            if (v > bestValue) {
                bestValue = v;
                best = e.getKey();
            }
        }
        return best != null ? best : actions.get(rnd.nextInt(actions.size()));
    }

    /**
     * M = 1 if D is off or every opponent hand is known.
     * Fixed variant: M = M_max while any hand is unknown.
     * Adaptive variant: M = 1 + ceil((M_max - 1) * u / (n - 1)), u = number of unknown opponent hands.
     */
    int numberOfDeterminisations(AbstractGameState gameState) {
        SushiMCTSParams params = getParameters();
        // MODIFICATION I samples the hidden cards again in every iteration of ONE tree (see SushiTreeNode).
        // MODIFICATION E: with ensembleTrees > 1 the budget is split between that many independent such trees and
        // their root visits are summed (root parallelisation, Chaslot, Winands and van den Herik 2008, done one
        // tree after another within the same budget)
        if (params.useInformationSets && params.useMaxN)
            return Math.max(1, params.ensembleTrees);
        if (!params.useDeterminisation || params.nDeterminisations <= 1 || !(gameState instanceof SGGameState sg))
            return 1;
        int n = sg.getNPlayers();
        int unknown = 0;
        for (int p = 0; p < n; p++)
            if (p != getPlayerID() && !sg.isHandKnown(getPlayerID(), p)) unknown++;
        if (unknown == 0) return 1;
        if (!params.adaptiveDeterminisation) return params.nDeterminisations;
        return 1 + (int) Math.ceil((params.nDeterminisations - 1) * unknown / (double) (n - 1));
    }

    @Override
    public SushiMCTSParams getParameters() {
        return active != null ? active : (SushiMCTSParams) parameters;
    }

    public void setStateHeuristic(IStateHeuristic heuristic) {
        getParameters().heuristic = heuristic;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public SushiMCTSPlayer copy() {
        SushiMCTSPlayer retValue = new SushiMCTSPlayer((SushiMCTSParams) parameters.copy());
        retValue.setName(toString());
        return retValue;
    }
}
