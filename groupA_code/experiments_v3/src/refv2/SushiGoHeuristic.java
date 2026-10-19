package refv2;

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

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        SGGameState s = (SGGameState) gs;
        int n = s.getNPlayers();
        double mine = CardValueModel.estimatedScore(s, playerId);
        double maxOpp = Double.NEGATIVE_INFINITY, sumOpp = 0.0;
        for (int p = 0; p < n; p++) {
            if (p == playerId) continue;
            double v = CardValueModel.estimatedScore(s, p);
            maxOpp = Math.max(maxOpp, v);
            sumOpp += v;
        }
        double meanOpp = sumOpp / (n - 1);
        double delta = LEADER_WEIGHT * (mine - maxOpp) + (1.0 - LEADER_WEIGHT) * (mine - meanOpp);
        return Math.tanh(delta / SCORE_SCALE);
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
