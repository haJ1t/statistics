package groupA;

import core.actions.AbstractAction;
import games.sushigo.SGGameState;
import games.sushigo.SGParameters;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import games.sushigo.cards.SGCard.SGCardType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CardValueModel - the single piece of Sushi Go domain knowledge used by the agent.
 * <p>
 * It is shared by three components so that they all "agree" about what a good card is:
 * <ul>
 *   <li>SushiGoHeuristic (H): estimated final-round score of every player,</li>
 *   <li>Progressive Bias (A): prior value of an action when a tree node is expanded,</li>
 *   <li>Informed rollouts (B): epsilon-greedy policy for ALL players (opponent model).</li>
 * </ul>
 * All methods are pure arithmetic on public game-state counters: they never call the
 * forward model, so they do not consume FM-call budget (they do consume CPU time, which
 * is why we always report iterations per decision under BUDGET_TIME).
 * <p>
 * IMPORTANT (TAG scoring): Tempura pairs, Sashimi triples, Dumplings and Nigiri are added to
 * the player's score at the moment a card is REVEALED (SGCard.onReveal), so getGameScore()
 * already contains them. Only Maki (end of round) and Pudding (end of game) are not yet banked.
 * The Wasabi counter is decremented when a Nigiri uses it, so it always holds UNUSED Wasabi.
 */
public final class CardValueModel {

    /** alpha: weight of the value of sets that are not complete yet (completion is uncertain). */
    public static final double POTENTIAL_WEIGHT = 0.5;
    /** T: number of remaining picks at which completing a set is considered "likely". */
    public static final double COMPLETION_HORIZON = 4.0;
    /** Average value of a Nigiri, used to value an unused Wasabi. */
    public static final double AVG_NIGIRI = 2.0;
    /** Value of holding Chopsticks with at least two picks left. */
    public static final double CHOPSTICKS_VALUE = 1.0;

    private CardValueModel() {
    }

    /**
     * The two constants of the model, so that a heuristic configured from JSON can change them
     * (DEFAULT reproduces the values above).
     */
    public record Config(double potentialWeight, double completionHorizon) {
        public static final Config DEFAULT = new Config(POTENTIAL_WEIGHT, COMPLETION_HORIZON);
    }

    /** c(r, m): chance-like factor of collecting m more cards of a type with r picks left. */
    static double completion(int picksLeft, int needed, double horizon) {
        if (picksLeft < needed) return 0.0;
        return Math.min(1.0, (picksLeft - needed + 1) / horizon);
    }

    /** Small immutable snapshot of one player's tableau for the current round. */
    private record Tableau(int tempura, int sashimi, int dumpling, int freeWasabi,
                           int maki, int pudding, int chopsticks) {
        static Tableau of(SGGameState s, int p) {
            return new Tableau(
                    s.getPlayedCardTypes(SGCardType.Tempura, p).getValue(),
                    s.getPlayedCardTypes(SGCardType.Sashimi, p).getValue(),
                    s.getPlayedCardTypes(SGCardType.Dumpling, p).getValue(),
                    s.getPlayedCardTypes(SGCardType.Wasabi, p).getValue(),   // already = unused Wasabi
                    s.getPlayedCardTypes(SGCardType.Maki, p).getValue(),     // number of maki ROLLS
                    s.getPlayedCardTypes(SGCardType.Pudding, p).getValue(),  // never reset (kept all game)
                    s.getPlayedCardTypes(SGCardType.Chopsticks, p).getValue());
        }
    }

    /**
     * The counters of ONE game state, read once and then shared by every valuation of that state.
     * Reading a counter is a HashMap look-up in TAG; building the view once per state instead of once
     * per card makes the rollout policy and the heuristic several times cheaper (more MCTS iterations
     * under the same time budget). The numbers it produces are exactly the same.
     */
    public static final class View {
        final SGGameState s;
        final SGParameters par;
        final int[] maki, pudding;          // Maki rolls (this round) and Pudding (all game) of every player
        final double puddingWeight;         // w_t = (round + 1) / nRounds
        final Config cfg;
        private final Tableau[] tableaux;   // built lazily, one per player

        public View(SGGameState s) {
            this(s, Config.DEFAULT);
        }

        public View(SGGameState s, Config cfg) {
            this.s = s;
            this.cfg = cfg;
            this.par = (SGParameters) s.getGameParameters();
            int n = s.getNPlayers();
            maki = new int[n];
            pudding = new int[n];
            for (int i = 0; i < n; i++) {
                maki[i] = s.getPlayedCardTypes(SGCardType.Maki, i).getValue();
                pudding[i] = s.getPlayedCardTypes(SGCardType.Pudding, i).getValue();
            }
            puddingWeight = (s.getRoundCounter() + 1.0) / par.nRounds;
            tableaux = new Tableau[n];
        }

        Tableau tableau(int p) {
            if (tableaux[p] == null) tableaux[p] = Tableau.of(s, p);
            return tableaux[p];
        }
    }

    // ------------------------------------------------------------------------------------
    // 1. Estimated score (used by the heuristic H)
    // ------------------------------------------------------------------------------------

    /**
     * S_hat(p) = banked score + alpha * value of unfinished sets + Maki standing + w_round * Pudding standing.
     */
    public static double estimatedScore(SGGameState s, int p) {
        return estimatedScore(new View(s), p);
    }

    /** Same as estimatedScore(s, p), reusing the counters of a View of the state. */
    public static double estimatedScore(View v, int p) {
        double banked = v.s.getGameScore(p);
        if (!v.s.isNotTerminal()) return banked;          // everything (incl. Pudding) is already scored
        int r = v.s.getPlayerHands().get(p).getSize();   // picks left this round
        Tableau t = v.tableau(p);
        return banked
                + v.cfg.potentialWeight() * potential(t, r, v.par, v.cfg.completionHorizon())
                + makiStanding(v.maki, p, t.maki, v.par)
                + v.puddingWeight * puddingStanding(v.pudding, p, t.pudding, v.par);  // w_t grows towards the final round
    }

    /** Value of sets that are started but not finished (NOT banked yet). */
    static double potential(Tableau t, int r, SGParameters par, double horizon) {
        double v = 0.0;
        if (t.tempura % 2 == 1) v += par.valueTempuraPair * completion(r, 1, horizon);
        int sashimiOpen = t.sashimi % 3;
        if (sashimiOpen == 2) v += par.valueSashimiTriple * completion(r, 1, horizon);
        else if (sashimiOpen == 1) v += par.valueSashimiTriple * completion(r, 2, horizon);
        // k-th unused Wasabi needs k Nigiri: extra value of a Nigiri on Wasabi = (multiplier - 1) * avg Nigiri
        for (int k = 1; k <= t.freeWasabi; k++)
            v += (par.multiplierWasabi - 1) * AVG_NIGIRI * completion(r, k, horizon);
        if (t.chopsticks > 0) v += CHOPSTICKS_VALUE * completion(r, 2, horizon);
        return v;
    }

    /** Maki points player p would get if the round ended now, with myMaki rolls for p (exact TAG rule, incl. ties). */
    static double makiStanding(int[] maki, int p, int myMaki, SGParameters par) {
        int n = maki.length;
        int most = 0;
        for (int i = 0; i < n; i++) most = Math.max(most, i == p ? myMaki : maki[i]);
        if (most == 0 || myMaki == 0) return 0.0;
        int nMost = 0, second = 0;
        for (int i = 0; i < n; i++) {
            int m = i == p ? myMaki : maki[i];
            if (m == most) nMost++;
            else if (m > second) second = m;
        }
        if (myMaki == most) return (double) (par.valueMakiMost / nMost);
        if (nMost > 1 || second == 0 || myMaki != second) return 0.0;  // second place only paid if single leader
        int nSecond = 0;
        for (int i = 0; i < n; i++) if ((i == p ? myMaki : maki[i]) == second) nSecond++;
        return (double) (par.valueMakiSecond / nSecond);
    }

    /** Pudding points player p would get if the game ended now, with myPudding for p (exact TAG rule). */
    static double puddingStanding(int[] pudding, int p, int myPudding, SGParameters par) {
        int n = pudding.length;
        int best = Integer.MIN_VALUE, worst = Integer.MAX_VALUE, nBest = 0, nWorst = 0;
        for (int i = 0; i < n; i++) {
            int x = i == p ? myPudding : pudding[i];
            best = Math.max(best, x);
            worst = Math.min(worst, x);
        }
        if (best == worst) return 0.0;
        for (int i = 0; i < n; i++) {
            int x = i == p ? myPudding : pudding[i];
            if (x == best) nBest++;
            if (x == worst) nWorst++;
        }
        double v = 0.0;
        if (myPudding == best && best > 0) v += (double) (par.valuePuddingMost / nBest);
        if (myPudding == worst && n > 2) v += (double) (par.valuePuddingLeast / nWorst);
        return v;
    }

    // ------------------------------------------------------------------------------------
    // 2. Marginal value of a card (used by Progressive Bias A and rollout policy B)
    // ------------------------------------------------------------------------------------

    /**
     * mv(p, card) = points scored on reveal + change in the value of unfinished sets
     *               + change in Maki standing + change in Pudding standing.
     * Both "before" and "after" use r-1 picks left, because a pick is consumed either way.
     */
    public static double marginalValue(SGGameState s, int p, SGCard card) {
        return marginalValue(new View(s), p, card);
    }

    /** Same as marginalValue(s, p, card), reusing the counters of a View of the state. */
    public static double marginalValue(View v, int p, SGCard card) {
        SGParameters par = v.par;
        int rAfter = Math.max(0, v.s.getPlayerHands().get(p).getSize() - 1);
        Tableau before = v.tableau(p);
        double immediate = 0.0;
        int tempura = before.tempura, sashimi = before.sashimi, dumpling = before.dumpling;
        int wasabi = before.freeWasabi, maki = before.maki, pudding = before.pudding, chop = before.chopsticks;

        switch (card.type) {
            case Tempura -> { if (tempura % 2 == 1) immediate = par.valueTempuraPair; tempura++; }
            case Sashimi -> { if (sashimi % 3 == 2) immediate = par.valueSashimiTriple; sashimi++; }
            case Dumpling -> {
                int idx = Math.min(dumpling + 1, par.valueDumpling.length) - 1;
                immediate = par.valueDumpling[idx];
                dumpling++;
            }
            case SquidNigiri, SalmonNigiri, EggNigiri -> {
                int base = card.type == SGCardType.SquidNigiri ? par.valueSquidNigiri
                        : card.type == SGCardType.SalmonNigiri ? par.valueSalmonNigiri : par.valueEggNigiri;
                if (wasabi > 0) { immediate = base * par.multiplierWasabi; wasabi--; } else immediate = base;
            }
            case Wasabi -> wasabi++;
            case Maki -> maki += card.count;
            case Pudding -> pudding++;
            case Chopsticks -> chop++;
        }
        Tableau after = new Tableau(tempura, sashimi, dumpling, wasabi, maki, pudding, chop);
        double horizon = v.cfg.completionHorizon();
        double dPotential = v.cfg.potentialWeight() * (potential(after, rAfter, par, horizon) - potential(before, rAfter, par, horizon));
        double dMaki = makiStanding(v.maki, p, after.maki, par) - makiStanding(v.maki, p, before.maki, par);
        double dPudding = v.puddingWeight * (puddingStanding(v.pudding, p, after.pudding, par) - puddingStanding(v.pudding, p, before.pudding, par));
        return immediate + dPotential + dMaki + dPudding;
    }

    /**
     * MODIFICATION P (action pruning): keeps the first action for every distinct (card type, Maki icons,
     * chopsticks) combination. Picking either of two identical cards leads to the same game state, so the
     * duplicates only split the visits of one real decision between several branches.
     */
    public static List<AbstractAction> distinctActions(SGGameState s, List<AbstractAction> actions) {
        List<AbstractAction> distinct = new ArrayList<>(actions.size());
        Set<Integer> seen = new HashSet<>();
        for (AbstractAction a : actions) {
            if (a instanceof ChooseCard cc) {
                SGCard card = s.getPlayerHands().get(cc.playerId).get(cc.cardIdx);
                int key = (card.type.ordinal() * 4 + card.count) * 2 + (cc.useChopsticks ? 1 : 0);
                if (!seen.add(key)) continue;
            }
            distinct.add(a);
        }
        return distinct;
    }

    /** Value of a ChooseCard action for the player who takes it (chopsticks add the best other card). */
    public static double actionValue(SGGameState s, AbstractAction a) {
        return actionValue(new View(s), a);
    }

    /** Same as actionValue(s, a), reusing the counters of a View of the state (one View for all actions). */
    public static double actionValue(View view, AbstractAction a) {
        if (!(a instanceof ChooseCard cc)) return 0.0;
        List<SGCard> hand = view.s.getPlayerHands().get(cc.playerId).getComponents();
        double v = marginalValue(view, cc.playerId, hand.get(cc.cardIdx));
        if (cc.useChopsticks) {
            double bestOther = 0.0;
            for (int i = 0; i < hand.size(); i++)
                if (i != cc.cardIdx) bestOther = Math.max(bestOther, marginalValue(view, cc.playerId, hand.get(i)));
            v += bestOther - CHOPSTICKS_VALUE;   // extra card now, but the Chopsticks go back into the hand
        }
        return v;
    }
}
