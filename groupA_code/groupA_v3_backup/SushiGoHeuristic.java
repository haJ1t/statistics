package groupA;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;
import games.sushigo.SGGameState;

/**
 * SushiGoHeuristic (component H).
 * <p>
 * H(s, i) = tanh( Delta_i / sigma ), with
 * Delta_i = 0.7 * (S_i - max_{j != i} S_j) + 0.3 * (S_i - mean_{j != i} S_j)
 * where S_p is CardValueModel.estimatedScore(s, p).
 * <p>
 * The first term rewards beating the current leader (only 1st place wins in TAG), the second
 * keeps a gradient when we are far behind or far ahead. tanh keeps the value in (-1, 1),
 * the same range as the rewards used by the UCB1 exploration constant K.
 */
public class SushiGoHeuristic implements IStateHeuristic {

    /** sigma: score gap (in points) that maps to tanh(1) = 0.76. */
    public static final double SCORE_SCALE = 15.0;
    public static final double LEADER_WEIGHT = 0.7;

    // Values actually used (the constants above unless the JSON gives "args")
    private final double scoreScale, leaderWeight;
    private final CardValueModel.Config model;

    public SushiGoHeuristic() {
        this(SCORE_SCALE, LEADER_WEIGHT, CardValueModel.POTENTIAL_WEIGHT, CardValueModel.COMPLETION_HORIZON);
    }

    /**
     * JSON: "heuristic": {"class": "groupA.SushiGoHeuristic", "args": [sigma, leaderWeight, alpha, T]}
     */
    public SushiGoHeuristic(double scoreScale, double leaderWeight, double potentialWeight, double completionHorizon) {
        this.scoreScale = scoreScale;
        this.leaderWeight = leaderWeight;
        this.model = new CardValueModel.Config(potentialWeight, completionHorizon);
    }

    /** View of a state with this heuristic's card-model constants (also used by the rollout policy). */
    public CardValueModel.View view(SGGameState s) {
        return new CardValueModel.View(s, model);
    }

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        SGGameState s = (SGGameState) gs;
        int n = s.getNPlayers();
        CardValueModel.View view = view(s);   // counters read once for all players
        double mine = CardValueModel.estimatedScore(view, playerId);
        double maxOpp = Double.NEGATIVE_INFINITY, sumOpp = 0.0;
        for (int p = 0; p < n; p++) {
            if (p == playerId) continue;
            double v = CardValueModel.estimatedScore(view, p);
            maxOpp = Math.max(maxOpp, v);
            sumOpp += v;
        }
        double meanOpp = sumOpp / (n - 1);
        double delta = leaderWeight * (mine - maxOpp) + (1.0 - leaderWeight) * (mine - meanOpp);
        return Math.tanh(delta / scoreScale);
    }

    /**
     * MODIFICATION M: H(s, i) for every player i at once. The estimated scores are computed once and then
     * shared, so this costs about the same as a single evaluateState call.
     */
    public double[] evaluateAll(SGGameState s) {
        int n = s.getNPlayers();
        double[] est = new double[n];
        CardValueModel.View view = view(s);
        for (int p = 0; p < n; p++)
            est[p] = CardValueModel.estimatedScore(view, p);
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            double maxOpp = Double.NEGATIVE_INFINITY, sumOpp = 0.0;
            for (int p = 0; p < n; p++) {
                if (p == i) continue;
                maxOpp = Math.max(maxOpp, est[p]);
                sumOpp += est[p];
            }
            double delta = leaderWeight * (est[i] - maxOpp) + (1.0 - leaderWeight) * (est[i] - sumOpp / (n - 1));
            values[i] = Math.tanh(delta / scoreScale);
        }
        return values;
    }

    @Override
    public double minValue() {
        return -1.0;
    }

    @Override
    public double maxValue() {
        return 1.0;
    }
}
