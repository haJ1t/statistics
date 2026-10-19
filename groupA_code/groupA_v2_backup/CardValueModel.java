package groupA;

import core.actions.AbstractAction;
import games.sushigo.SGGameState;
import games.sushigo.SGParameters;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import games.sushigo.cards.SGCard.SGCardType;

import java.util.List;

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

    /** c(r, m): chance-like factor of collecting m more cards of a type with r picks left. */
    static double completion(int picksLeft, int needed) {
        if (picksLeft < needed) return 0.0;
        return Math.min(1.0, (picksLeft - needed + 1) / COMPLETION_HORIZON);
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

    // ------------------------------------------------------------------------------------
    // 1. Estimated score (used by the heuristic H)
    // ------------------------------------------------------------------------------------

    /**
     * S_hat(p) = banked score + alpha * value of unfinished sets + Maki standing + w_round * Pudding standing.
     */
    public static double estimatedScore(SGGameState s, int p) {
        double banked = s.getGameScore(p);
        if (!s.isNotTerminal()) return banked;          // everything (incl. Pudding) is already scored
        SGParameters par = (SGParameters) s.getGameParameters();
        int r = s.getPlayerHands().get(p).getSize();   // picks left this round
        Tableau t = Tableau.of(s, p);
        double puddingWeight = (s.getRoundCounter() + 1.0) / par.nRounds; // grows towards the final round
        return banked
                + POTENTIAL_WEIGHT * potential(t, r, par)
                + makiStanding(s, p, t.maki, par)
                + puddingWeight * puddingStanding(s, p, t.pudding, par);
    }

    /** Value of sets that are started but not finished (NOT banked yet). */
    static double potential(Tableau t, int r, SGParameters par) {
        double v = 0.0;
        if (t.tempura % 2 == 1) v += par.valueTempuraPair * completion(r, 1);
        int sashimiOpen = t.sashimi % 3;
        if (sashimiOpen == 2) v += par.valueSashimiTriple * completion(r, 1);
        else if (sashimiOpen == 1) v += par.valueSashimiTriple * completion(r, 2);
        // k-th unused Wasabi needs k Nigiri: extra value of a Nigiri on Wasabi = (multiplier - 1) * avg Nigiri
        for (int k = 1; k <= t.freeWasabi; k++)
            v += (par.multiplierWasabi - 1) * AVG_NIGIRI * completion(r, k);
        if (t.chopsticks > 0) v += CHOPSTICKS_VALUE * completion(r, 2);
        return v;
    }

    /** Maki points player p would get if the round ended now (exact TAG rule, incl. ties). */
    static double makiStanding(SGGameState s, int p, int myMaki, SGParameters par) {
        int n = s.getNPlayers();
        int[] maki = new int[n];
        for (int i = 0; i < n; i++) maki[i] = (i == p) ? myMaki : s.getPlayedCardTypes(SGCardType.Maki, i).getValue();
        int most = 0;
        for (int m : maki) most = Math.max(most, m);
        if (most == 0 || myMaki == 0) return 0.0;
        int nMost = 0, second = 0;
        for (int m : maki) {
            if (m == most) nMost++;
            else if (m > second) second = m;
        }
        if (myMaki == most) return (double) (par.valueMakiMost / nMost);
        if (nMost > 1 || second == 0 || myMaki != second) return 0.0;  // second place only paid if single leader
        int nSecond = 0;
        for (int m : maki) if (m == second) nSecond++;
        return (double) (par.valueMakiSecond / nSecond);
    }

    /** Pudding points player p would get if the game ended now (exact TAG rule). */
    static double puddingStanding(SGGameState s, int p, int myPudding, SGParameters par) {
        int n = s.getNPlayers();
        int best = Integer.MIN_VALUE, worst = Integer.MAX_VALUE, nBest = 0, nWorst = 0;
        int[] pud = new int[n];
        for (int i = 0; i < n; i++) {
            pud[i] = (i == p) ? myPudding : s.getPlayedCardTypes(SGCardType.Pudding, i).getValue();
            best = Math.max(best, pud[i]);
            worst = Math.min(worst, pud[i]);
        }
        if (best == worst) return 0.0;
        for (int x : pud) {
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
        SGParameters par = (SGParameters) s.getGameParameters();
        int rAfter = Math.max(0, s.getPlayerHands().get(p).getSize() - 1);
        Tableau before = Tableau.of(s, p);
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
        double dPotential = POTENTIAL_WEIGHT * (potential(after, rAfter, par) - potential(before, rAfter, par));
        double dMaki = makiStanding(s, p, after.maki, par) - makiStanding(s, p, before.maki, par);
        double puddingWeight = (s.getRoundCounter() + 1.0) / par.nRounds;
        double dPudding = puddingWeight * (puddingStanding(s, p, after.pudding, par) - puddingStanding(s, p, before.pudding, par));
        return immediate + dPotential + dMaki + dPudding;
    }

    /** Value of a ChooseCard action for the player who takes it (chopsticks add the best other card). */
    public static double actionValue(SGGameState s, AbstractAction a) {
        if (!(a instanceof ChooseCard cc)) return 0.0;
        List<SGCard> hand = s.getPlayerHands().get(cc.playerId).getComponents();
        double v = marginalValue(s, cc.playerId, hand.get(cc.cardIdx));
        if (cc.useChopsticks) {
            double bestOther = 0.0;
            for (int i = 0; i < hand.size(); i++)
                if (i != cc.cardIdx) bestOther = Math.max(bestOther, marginalValue(s, cc.playerId, hand.get(i)));
            v += bestOther - CHOPSTICKS_VALUE;   // extra card now, but the Chopsticks go back into the hand
        }
        return v;
    }
}
