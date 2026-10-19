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
 * Only _getAction() is changed, to add Modification D: Information-Aware Determinisation.
 * BasicMCTS searches ONE sampled ("determinised") version of the hidden cards and treats it as
 * the truth. D instead builds M independent trees, each on a fresh sample of the hidden hands
 * (gameState.copy(playerId) re-shuffles everything this player cannot see), splits the budget
 * between them and returns the action with the most visits summed over all trees
 * (Perfect Information Monte Carlo / determinised MCTS).
 * <p>
 * Sushi Go specific part: M is chosen from how many opponent hands are still unknown
 * (SGGameState.isHandKnown). After n-1 passes every hand in the round is known, so M = 1 and
 * the whole budget goes into a single, deeper tree.
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

    @Override
    public AbstractAction _getAction(AbstractGameState gameState, List<AbstractAction> actions) {
        SushiMCTSParams params = getParameters();
        int m = numberOfDeterminisations(gameState);
        lastDeterminisations = m;

        if (m <= 1) {
            // Identical to BasicMCTSPlayer: one tree on the state we were given
            SushiTreeNode root = new SushiTreeNode(this, null, gameState, rnd);
            root.mctsSearch(params.budget, params.breakMS);
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
            root.mctsSearch(share, 0);
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
        return (SushiMCTSParams) parameters;
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
