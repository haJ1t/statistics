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
    /**
     * beta: extra factor on the chance of completing a Sashimi triple when two more Sashimi are still needed
     * (1 = off). With beta = 1 and many picks left, one Sashimi already counts the triple as likely, so a second
     * Sashimi adds nothing and a greedy player hardly ever finishes a triple.
     */
    public static final double SASHIMI_DISCOUNT = 1.0;
    /** Value of holding Chopsticks with at least two picks left. */
    public static final double CHOPSTICKS_VALUE = 1.0;
    /**
     * T_m: picks left at which the Maki standing "if the round ended now" gets weight 1/2 (0 = always weight 1).
     * Early in a round that standing is far from final: the first Maki card of the round counts as 6 points.
     */
    public static final double MAKI_HORIZON = 0.0;
    /** kappa: value of one Maki icon, used for the rest of the weight when MAKI_HORIZON > 0. */
    public static final double MAKI_ICON_VALUE = 0.0;
    /** delta: weight of the points of the next Dumpling in the value of unfinished sets (0 = off). */
    public static final double DUMPLING_WEIGHT = 0.0;
    /** pi: factor on the Pudding standing. */
    public static final double PUDDING_SCALE = 1.0;
    /**
     * gamma: a set that needs m more cards of a type is only "likely" when at least gamma * m cards of that type are
     * still in the players' hands this round (0 = off: the cards in the hands are not counted).
     */
    public static final double SUPPLY_SCALE = 0.0;

    private CardValueModel() {
    }

    /**
     * The constants of the model, so that a heuristic configured from JSON can change them
     * (DEFAULT reproduces the values above).
     */
    public record Config(double potentialWeight, double completionHorizon, double sashimiDiscount,
                         double makiHorizon, double makiIconValue, double dumplingWeight, double puddingScale,
                         double avgNigiri, double supplyScale) {
        public static final Config DEFAULT = new Config(POTENTIAL_WEIGHT, COMPLETION_HORIZON, SASHIMI_DISCOUNT);

        public Config(double potentialWeight, double completionHorizon, double sashimiDiscount, double makiHorizon,
                      double makiIconValue, double dumplingWeight, double puddingScale, double avgNigiri) {
            this(potentialWeight, completionHorizon, sashimiDiscount, makiHorizon, makiIconValue, dumplingWeight,
                    puddingScale, avgNigiri, SUPPLY_SCALE);
        }

        public Config(double potentialWeight, double completionHorizon) {
            this(potentialWeight, completionHorizon, SASHIMI_DISCOUNT);
        }

        public Config(double potentialWeight, double completionHorizon, double sashimiDiscount) {
            this(potentialWeight, completionHorizon, sashimiDiscount, MAKI_HORIZON, MAKI_ICON_VALUE, DUMPLING_WEIGHT,
                    PUDDING_SCALE, AVG_NIGIRI, SUPPLY_SCALE);
        }
    }

    /** c(r, m): chance-like factor of collecting m more cards of a type with r picks left. */
    static double completion(int picksLeft, int needed, double horizon) {
        if (picksLeft < needed) return 0.0;
        return Math.min(1.0, (picksLeft - needed + 1) / horizon);
    }

    /** Small immutable snapshot of one player's tableau for the current round. */
    record Tableau(int tempura, int sashimi, int dumpling, int freeWasabi,
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
        private int[] supply;               // cards of each type in all hands (built lazily, only with gamma > 0)

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

        /** Cards of each type (by ordinal) in all players' hands, or null when gamma is off. */
        int[] supply() {
            if (cfg.supplyScale() <= 0) return null;
            if (supply == null) {
                supply = new int[SGCardType.values().length];
                for (int i = 0; i < s.getNPlayers(); i++)
                    for (SGCard c : s.getPlayerHands().get(i).getComponents())
                        supply[c.type.ordinal()]++;
            }
            return supply;
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
                + v.cfg.potentialWeight() * potential(t, r, v.par, v.cfg, v.supply(), -1)
                + makiValue(v.maki, p, t.maki, r, v.par, v.cfg)
                + v.cfg.puddingScale() * v.puddingWeight * puddingStanding(v.pudding, p, t.pudding, v.par);  // w_t grows towards the final round
    }

    /**
     * Value of sets that are started but not finished (NOT banked yet). supply: cards of each type in the hands
     * (null = not counted), without one card of type ordinal "played" (the card being valued, -1 = none).
     */
    static double potential(Tableau t, int r, SGParameters par, Config cfg, int[] supply, int played) {
        double horizon = cfg.completionHorizon();
        double v = 0.0;
        if (t.tempura % 2 == 1)
            v += par.valueTempuraPair * completion(r, 1, horizon) * supplied(supply, played, 1, cfg, SGCardType.Tempura);
        int sashimiOpen = t.sashimi % 3;
        if (sashimiOpen == 2)
            v += par.valueSashimiTriple * completion(r, 1, horizon) * supplied(supply, played, 1, cfg, SGCardType.Sashimi);
        else if (sashimiOpen == 1)
            v += par.valueSashimiTriple * completion(r, 2, horizon) * cfg.sashimiDiscount()
                    * supplied(supply, played, 2, cfg, SGCardType.Sashimi);
        // k-th unused Wasabi needs k Nigiri: extra value of a Nigiri on Wasabi = (multiplier - 1) * avg Nigiri
        for (int k = 1; k <= t.freeWasabi; k++)
            v += (par.multiplierWasabi - 1) * cfg.avgNigiri() * completion(r, k, horizon)
                    * suppliedNigiri(supply, played, k, cfg);
        if (t.chopsticks > 0) v += CHOPSTICKS_VALUE * completion(r, 2, horizon);
        if (cfg.dumplingWeight() != 0 && t.dumpling > 0) {
            // points the next Dumpling would add (TAG: the k-th Dumpling scores valueDumpling[min(k, 5) - 1])
            int next = par.valueDumpling[Math.min(t.dumpling + 1, par.valueDumpling.length) - 1];
            v += cfg.dumplingWeight() * next * completion(r, 1, horizon) * supplied(supply, played, 1, cfg, SGCardType.Dumpling);
        }
        return v;
    }

    /** min(1, cards of the type still in the hands / (gamma * needed)); 1 when the hands are not counted. */
    static double supplied(int[] supply, int played, int needed, Config cfg, SGCardType type) {
        if (supply == null) return 1.0;
        int c = supply[type.ordinal()] - (type.ordinal() == played ? 1 : 0);
        return Math.min(1.0, c / (cfg.supplyScale() * needed));
    }

    /** Same for Nigiri of any kind (what an unused Wasabi needs). */
    static double suppliedNigiri(int[] supply, int played, int needed, Config cfg) {
        if (supply == null) return 1.0;
        int c = supply[SGCardType.SquidNigiri.ordinal()] + supply[SGCardType.SalmonNigiri.ordinal()]
                + supply[SGCardType.EggNigiri.ordinal()];
        if (played == SGCardType.SquidNigiri.ordinal() || played == SGCardType.SalmonNigiri.ordinal()
                || played == SGCardType.EggNigiri.ordinal()) c--;
        return Math.min(1.0, c / (cfg.supplyScale() * needed));
    }

    /**
     * Value of myMaki rolls for player p with r picks left: the standing if the round ended now (makiStanding),
     * mixed with kappa points per roll while many picks are left (weight of the standing 1 / (1 + r / T_m)).
     */
    static double makiValue(int[] maki, int p, int myMaki, int r, SGParameters par, Config cfg) {
        double standing = makiStanding(maki, p, myMaki, par);
        if (cfg.makiHorizon() <= 0) return standing;
        double w = 1.0 / (1.0 + r / cfg.makiHorizon());
        return w * standing + (1.0 - w) * cfg.makiIconValue() * myMaki;
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
        return marginalValue(v.tableau(p), v.maki, v.pudding, p, v.s.getPlayerHands().get(p).getSize(), v.puddingWeight,
                v.supply(), card.type, card.count, v.par, v.cfg);
    }

    /**
     * marginalValue from the counters alone (shared by View and SushiSim): player p with tableau "before", the Maki
     * rolls and Pudding of every player, handSize cards in hand, Pudding weight w_t and the cards in all hands
     * (supply, null = not counted) values a card of the given type and Maki count.
     */
    static double marginalValue(Tableau before, int[] makiAll, int[] puddingAll, int p, int handSize, double puddingWeight,
                                int[] supply, SGCardType type, int count, SGParameters par, Config cfg) {
        int rAfter = Math.max(0, handSize - 1);
        double immediate = 0.0;
        int tempura = before.tempura, sashimi = before.sashimi, dumpling = before.dumpling;
        int wasabi = before.freeWasabi, maki = before.maki, pudding = before.pudding, chop = before.chopsticks;

        switch (type) {
            case Tempura -> { if (tempura % 2 == 1) immediate = par.valueTempuraPair; tempura++; }
            case Sashimi -> { if (sashimi % 3 == 2) immediate = par.valueSashimiTriple; sashimi++; }
            case Dumpling -> {
                int idx = Math.min(dumpling + 1, par.valueDumpling.length) - 1;
                immediate = par.valueDumpling[idx];
                dumpling++;
            }
            case SquidNigiri, SalmonNigiri, EggNigiri -> {
                int base = type == SGCardType.SquidNigiri ? par.valueSquidNigiri
                        : type == SGCardType.SalmonNigiri ? par.valueSalmonNigiri : par.valueEggNigiri;
                if (wasabi > 0) { immediate = base * par.multiplierWasabi; wasabi--; } else immediate = base;
            }
            case Wasabi -> wasabi++;
            case Maki -> maki += count;
            case Pudding -> pudding++;
            case Chopsticks -> chop++;
        }
        Tableau after = new Tableau(tempura, sashimi, dumpling, wasabi, maki, pudding, chop);
        int played = type.ordinal();
        double dPotential = cfg.potentialWeight() * (potential(after, rAfter, par, cfg, supply, played)
                - potential(before, rAfter, par, cfg, supply, played));
        double dMaki = makiValue(makiAll, p, after.maki, rAfter, par, cfg) - makiValue(makiAll, p, before.maki, rAfter, par, cfg);
        double dPudding = cfg.puddingScale() * puddingWeight
                * (puddingStanding(puddingAll, p, after.pudding, par) - puddingStanding(puddingAll, p, before.pudding, par));
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
            if (a instanceof ChooseCard cc && !seen.add(actionKey(s, cc))) continue;
            distinct.add(a);
        }
        return distinct;
    }

    /**
     * MODIFICATION I: the distinct cards player p can play, without creating actions. They come in the order of the
     * forward model's actions (card i, then card i with chopsticks when p has Chopsticks on the table and two or more
     * cards in hand); for the d-th distinct one: keys[d] = actionKey, cardIdx[d] = position in the hand, chop[d] = 1
     * if Chopsticks are used. The arrays need room for 2 * hand size entries. Returns the number of distinct cards.
     */
    public static int distinctCards(SGGameState s, int p, int[] keys, int[] cardIdx, int[] chop) {
        List<SGCard> hand = s.getPlayerHands().get(p).getComponents();
        int chopMax = s.getPlayedCardTypes(SGCardType.Chopsticks, p).getValue() > 0 && hand.size() > 1 ? 1 : 0;
        long seen0 = 0, seen1 = 0;   // keys are below 128
        int d = 0, i = 0;
        for (SGCard card : hand) {
            int base = (card.type.ordinal() * 4 + card.count) * 2;
            for (int c = 0; c <= chopMax; c++) {
                int k = base + c;
                long bit = 1L << (k & 63);
                if (k < 64) {
                    if ((seen0 & bit) != 0) continue;
                    seen0 |= bit;
                } else {
                    if ((seen1 & bit) != 0) continue;
                    seen1 |= bit;
                }
                keys[d] = k;
                cardIdx[d] = i;
                chop[d] = c;
                d++;
            }
            i++;
        }
        return d;
    }

    /** actionKey of playing each card of player p's hand without chopsticks (add 1 for the chopsticks variant). */
    public static int[] handKeys(SGGameState s, int p) {
        List<SGCard> hand = s.getPlayerHands().get(p).getComponents();
        int[] keys = new int[hand.size()];
        int i = 0;
        for (SGCard card : hand)
            keys[i++] = (card.type.ordinal() * 4 + card.count) * 2;
        return keys;
    }

    /**
     * Key of a ChooseCard action that does not depend on where the card sits in the hand:
     * (card type, Maki icons, chopsticks). Always below 128, so one key fits in 7 bits.
     */
    public static int actionKey(SGGameState s, ChooseCard cc) {
        SGCard card = s.getPlayerHands().get(cc.playerId).get(cc.cardIdx);
        return (card.type.ordinal() * 4 + card.count) * 2 + (cc.useChopsticks ? 1 : 0);
    }

    /**
     * Marginal value of every card in player p's hand, in hand order (same numbers as marginalValue). Cards of the
     * same type and Maki count have the same value, so each kind is valued only once.
     */
    public static double[] handValues(View view, int p) {
        List<SGCard> hand = view.s.getPlayerHands().get(p).getComponents();
        double[] mv = new double[hand.size()];
        double[] byKind = new double[64];
        boolean[] known = new boolean[64];
        int i = 0;
        for (SGCard card : hand) {
            int kind = card.type.ordinal() * 4 + card.count;
            if (!known[kind]) {
                byKind[kind] = marginalValue(view, p, card);
                known[kind] = true;
            }
            mv[i++] = byKind[kind];
        }
        return mv;
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
