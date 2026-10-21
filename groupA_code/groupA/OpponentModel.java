package groupA;

import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;
import games.sushigo.cards.SGCard.SGCardType;

import java.util.ArrayList;
import java.util.List;

/**
 * MODIFICATION Q: online opponent model.
 * <p>
 * The simulations play every player with the card-value policy (B), but real opponents have their own styles (one
 * collects Maki, another Nigiri). Once a hand has been passed on, we know the hand an opponent picks from
 * (SGGameState.isHandKnown) and, at our next decision, the card it played: each such pick is an observation of its
 * choice among known options. For opponent q and card type t a bias b[q][t] (in points) is fitted to its observations
 * by maximum a posteriori estimation with
 * P(q picks card c) proportional to exp((v_q(c) + b[q][type(c)]) / T),   b[q][t] ~ N(0, s^2),
 * where v_q(c) is the marginal value of c for q's tableau in our card-value model (CardValueModel). In the
 * simulations q then values card c at v_q(c) + b[q][type(c)]. The model starts empty (all biases 0) in every game.
 */
final class OpponentModel {
    private static final int TYPES = SGCardType.values().length;
    private static final int KINDS = TYPES * 4;   // kind = type ordinal * 4 + Maki count, as in CardValueModel

    private final int n, me;
    private final double temperature, prior;
    /** bias[q][type ordinal] in points; null for us */
    private final double[][] bias;
    // observations of opponent q: per pick, the options' values, types and copies, and the index of the chosen option
    private final List<double[]>[] obsValues;
    private final List<int[]>[] obsTypes, obsCopies;
    private final List<Integer>[] obsChoice;
    // what we saw at our previous decision
    private int prevRound = -1, prevHandSize = -1;
    private final List<SGCard>[] prevHand;
    private final double[][] prevValues;
    private final int[][] prevPlayed;
    int observations = 0;

    @SuppressWarnings("unchecked")
    OpponentModel(int n, int me, double temperature, double prior) {
        this.n = n;
        this.me = me;
        this.temperature = temperature;
        this.prior = prior;
        bias = new double[n][];
        obsValues = new List[n];
        obsTypes = new List[n];
        obsCopies = new List[n];
        obsChoice = new List[n];
        prevHand = new List[n];
        prevValues = new double[n][];
        prevPlayed = new int[n][];
        for (int q = 0; q < n; q++) {
            if (q == me) continue;
            bias[q] = new double[TYPES];
            obsValues[q] = new ArrayList<>();
            obsTypes[q] = new ArrayList<>();
            obsCopies[q] = new ArrayList<>();
            obsChoice[q] = new ArrayList<>();
        }
    }

    boolean fits(int nPlayers, int playerId) {
        return nPlayers == n && playerId == me;
    }

    /** Bias of opponent q for card type t, in points (0 for us). */
    double bias(int q, SGCardType t) {
        return bias[q] == null ? 0.0 : bias[q][t.ordinal()];
    }

    /** bias(q, .) by type ordinal, or null for us. */
    double[] biasOf(int q) {
        return bias[q];
    }

    /**
     * Called at each of our decisions at the start of a turn: records the picks the opponents made since our previous
     * decision (when we knew their hands then) and remembers the hands we know now.
     */
    void observe(SGGameState s, CardValueModel.Config cfg) {
        int[][] played = playedKinds(s);
        int handSize = s.getPlayerHands().get(me).getSize();
        if (s.getRoundCounter() == prevRound && handSize == prevHandSize - 1) {
            for (int q = 0; q < n; q++) {
                if (q == me || prevHand[q] == null) continue;
                // the card q played: exactly one new card on its table and none gone (a Chopsticks move is skipped)
                int newKind = -1, added = 0;
                boolean gone = false;
                for (int k = 0; k < KINDS; k++) {
                    int d = played[q][k] - prevPlayed[q][k];
                    if (d > 0) {
                        added += d;
                        newKind = k;
                    } else if (d < 0) gone = true;
                }
                if (added == 1 && !gone) record(q, newKind);
            }
        }
        CardValueModel.View view = new CardValueModel.View(s, cfg);
        for (int q = 0; q < n; q++) {
            prevPlayed[q] = played[q];
            if (q != me && s.isHandKnown(me, q)) {
                prevHand[q] = new ArrayList<>(s.getPlayerHands().get(q).getComponents());
                prevValues[q] = CardValueModel.handValues(view, q);
            } else {
                prevHand[q] = null;
                prevValues[q] = null;
            }
        }
        prevRound = s.getRoundCounter();
        prevHandSize = handSize;
    }

    private static int kind(SGCard c) {
        return c.type.ordinal() * 4 + c.count;
    }

    private int[][] playedKinds(SGGameState s) {
        int[][] k = new int[n][KINDS];
        for (int q = 0; q < n; q++)
            for (SGCard c : s.getPlayedCards().get(q).getComponents()) k[q][kind(c)]++;
        return k;
    }

    /** One observation: from its known hand, opponent q played a card of the given kind. */
    private void record(int q, int chosenKind) {
        List<SGCard> hand = prevHand[q];
        int[] slotOf = new int[KINDS];
        java.util.Arrays.fill(slotOf, -1);
        List<Double> values = new ArrayList<>();
        List<Integer> types = new ArrayList<>(), copies = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            int k = kind(hand.get(i));
            if (slotOf[k] < 0) {
                slotOf[k] = values.size();
                values.add(prevValues[q][i]);
                types.add(hand.get(i).type.ordinal());
                copies.add(0);
            }
            copies.set(slotOf[k], copies.get(slotOf[k]) + 1);
        }
        if (slotOf[chosenKind] < 0 || values.size() < 2) return;   // not in the hand we saw, or no choice
        obsValues[q].add(values.stream().mapToDouble(Double::doubleValue).toArray());
        obsTypes[q].add(types.stream().mapToInt(Integer::intValue).toArray());
        obsCopies[q].add(copies.stream().mapToInt(Integer::intValue).toArray());
        obsChoice[q].add(slotOf[chosenKind]);
        observations++;
        refit(q);
    }

    /**
     * MAP estimate of q's biases. The log posterior is concave; each step divides the gradient by a diagonal upper
     * bound of the curvature at the current biases (type probabilities / T^2 + 1 / s^2), at most 2 points per step.
     */
    private void refit(int q) {
        double[] b = bias[q];
        double invT = 1.0 / temperature, invS2 = 1.0 / (prior * prior);
        int m = obsValues[q].size();
        for (int it = 0; it < 30; it++) {
            double[] grad = new double[TYPES], curv = new double[TYPES];
            for (int t = 0; t < TYPES; t++) {
                grad[t] = -b[t] * invS2;
                curv[t] = invS2;
            }
            for (int o = 0; o < m; o++) {
                double[] v = obsValues[q].get(o);
                int[] ty = obsTypes[q].get(o), cp = obsCopies[q].get(o);
                double max = -Double.MAX_VALUE;
                for (int i = 0; i < v.length; i++) max = Math.max(max, (v[i] + b[ty[i]]) * invT);
                double z = 0;
                double[] p = new double[v.length];
                for (int i = 0; i < v.length; i++) {
                    p[i] = cp[i] * Math.exp((v[i] + b[ty[i]]) * invT - max);
                    z += p[i];
                }
                double[] pt = new double[TYPES];   // probability of each type in this pick
                for (int i = 0; i < v.length; i++) pt[ty[i]] += p[i] / z;
                for (int t = 0; t < TYPES; t++) {
                    grad[t] -= pt[t] * invT;
                    curv[t] += pt[t] * invT * invT;
                }
                grad[ty[obsChoice[q].get(o)]] += invT;
            }
            double change = 0;
            for (int t = 0; t < TYPES; t++) {
                double d = Math.max(-2.0, Math.min(2.0, grad[t] / curv[t]));   // at most 2 points per step
                b[t] += d;
                change = Math.max(change, Math.abs(d));
            }
            if (change < 1e-4) break;
        }
    }
}
