package groupA;

import core.components.Component;
import core.components.Deck;
import games.sushigo.SGGameState;
import games.sushigo.SGParameters;
import games.sushigo.cards.SGCard;
import games.sushigo.cards.SGCard.SGCardType;

import java.util.Random;

/**
 * MODIFICATION F (fast simulation): one Sushi Go round in small int arrays, so that an iteration of the search can
 * sample the hidden cards, play the tree moves and the rollout and score the round without copying TAG's game state
 * or calling its forward model (together about two thirds of the time of an iteration). The rules are the ones of
 * TAG's SGForwardModel and SGCard: when every player has chosen, the players reveal in player order (with Chopsticks
 * both cards in the order chosen, then the Chopsticks go back into the hand); Tempura, Sashimi, Dumplings and Nigiri
 * (on an unused Wasabi) score on reveal; every hand passes to the player with the next lower id; Maki score at the end
 * of the round and Pudding at the end of the game, with TAG's tie rules; a new round is dealt from the draw pile
 * (SGForwardModel._startRound). SushiSimCheck (experiments) compares it with the forward model move by move.
 * <p>
 * A card is stored as its kind: 0, 1, 2 = Maki with 1, 2, 3 rolls, then Tempura, Sashimi, Dumpling, Squid, Salmon
 * and Egg Nigiri, Wasabi, Chopsticks, Pudding.
 */
final class SushiSim {
    static final int KINDS = 12;
    static final int TEMPURA = 3, SASHIMI = 4, DUMPLING = 5, SQUID = 6, SALMON = 7, EGG = 8, WASABI = 9,
            CHOPSTICKS = 10, PUDDING = 11;
    static final SGCardType[] TYPE = new SGCardType[KINDS];
    static final int[] COUNT = new int[KINDS];

    static {
        for (int k = 0; k < KINDS; k++) {
            TYPE[k] = k < 3 ? SGCardType.Maki : SGCardType.values()[k - 2];
            COUNT[k] = k < 3 ? k + 1 : 1;
        }
    }

    static int kind(SGCard c) {
        return c.type == SGCardType.Maki ? c.count - 1 : c.type.ordinal() + 2;
    }

    /** CardValueModel.actionKey of a card of this kind played without Chopsticks (+1 with Chopsticks). */
    static int key(int kind) {
        return (TYPE[kind].ordinal() * 4 + COUNT[kind]) * 2;
    }

    // per player, after the hands: banked score, Tempura, Sashimi, Dumplings, unused Wasabi, Maki rolls of this round,
    // Pudding (whole game), Chopsticks on the table
    private static final int SCORE = 0, TEMP = 1, SASH = 2, DUMP = 3, WAS = 4, MAKI = 5, PUD = 6, CHOP = 7, FIELDS = 8;

    final int n;
    final SGParameters par;
    /** hand counts a[p * KINDS + kind], then the FIELDS counters of every player */
    final int[] a;
    private final int base;
    int round;          // rounds already scored (TAG's round counter)
    int handSize;       // cards in every hand
    boolean gameOver;

    // hidden cards (root only): players whose hand we cannot see and the cards that may be in those hands
    private int[] unknown = new int[0];
    private int[] pool = new int[0];
    // draw pile: deck[deckPos .. deckEnd - 1]; a sample draws from it at random (the rest of its shuffled pool),
    // an exact copy (of) in TAG's order from the top
    private int[] deck = new int[0];
    private int deckPos, deckEnd;
    private Random drawRnd;

    private SushiSim(int n, SGParameters par) {
        this.n = n;
        this.par = par;
        this.base = n * KINDS;
        this.a = new int[n * (KINDS + FIELDS)];
    }

    /** Empty simulation of the same game, to be filled by sampleFrom. */
    SushiSim emptyCopy() {
        return new SushiSim(n, par);
    }

    /** Exact copy of a game state at the start of a turn (no card chosen yet). */
    static SushiSim of(SGGameState s) {
        int n = s.getNPlayers();
        SushiSim sim = new SushiSim(n, (SGParameters) s.getGameParameters());
        for (int p = 0; p < n; p++) {
            for (SGCard c : s.getPlayerHands().get(p).getComponents())
                sim.a[p * KINDS + kind(c)]++;
            int f = sim.base + p * FIELDS;
            sim.a[f + SCORE] = (int) s.getGameScore(p);
            sim.a[f + TEMP] = s.getPlayedCardTypes(SGCardType.Tempura, p).getValue();
            sim.a[f + SASH] = s.getPlayedCardTypes(SGCardType.Sashimi, p).getValue();
            sim.a[f + DUMP] = s.getPlayedCardTypes(SGCardType.Dumpling, p).getValue();
            sim.a[f + WAS] = s.getPlayedCardTypes(SGCardType.Wasabi, p).getValue();
            sim.a[f + MAKI] = s.getPlayedCardTypes(SGCardType.Maki, p).getValue();
            sim.a[f + PUD] = s.getPlayedCardTypes(SGCardType.Pudding, p).getValue();
            sim.a[f + CHOP] = s.getPlayedCardTypes(SGCardType.Chopsticks, p).getValue();
        }
        sim.round = s.getRoundCounter();
        sim.handSize = s.getPlayerHands().get(0).getSize();
        sim.gameOver = !s.isNotTerminal();
        Deck<SGCard> drawPile = drawPile(s);
        if (drawPile != null) {
            sim.deck = new int[drawPile.getSize()];
            int i = 0;
            for (SGCard c : drawPile.getComponents()) sim.deck[i++] = kind(c);   // index 0 = top of the pile
            sim.deckEnd = i;
        }
        return sim;
    }

    /**
     * The root of a search from player me's observation: like of(s), and the hands that me has not seen are dealt
     * again in every sampleFrom from the cards me cannot see (those hands and the draw pile), as
     * SGGameState.redeterminise does.
     */
    static SushiSim root(SGGameState s, int me) {
        SushiSim sim = of(s);
        if (!s.getCoreGameParameters().partialObservable) return sim;
        int nUnknown = 0;
        int[] unknown = new int[sim.n];
        for (int p = 0; p < sim.n; p++)
            if (p != me && !s.isHandKnown(me, p)) unknown[nUnknown++] = p;
        sim.unknown = java.util.Arrays.copyOf(unknown, nUnknown);
        int size = nUnknown * sim.handSize;
        Deck<SGCard> drawPile = drawPile(s);
        if (drawPile != null) size += drawPile.getSize();
        sim.pool = new int[size];
        int i = 0;
        for (int p : sim.unknown)
            for (SGCard c : s.getPlayerHands().get(p).getComponents()) sim.pool[i++] = kind(c);
        if (drawPile != null)
            for (SGCard c : drawPile.getComponents()) sim.pool[i++] = kind(c);
        return sim;
    }

    @SuppressWarnings("unchecked")
    private static Deck<SGCard> drawPile(SGGameState s) {
        for (Component c : s.getAllTopLevelComponents())
            if (c instanceof Deck<?> d && "Draw pile".equals(d.getComponentName())) return (Deck<SGCard>) d;
        return null;
    }

    /** This simulation becomes a sample of the root: the root's position with the unseen hands dealt at random. */
    void sampleFrom(SushiSim root, Random rnd, int[] scratch) {
        System.arraycopy(root.a, 0, a, 0, a.length);
        round = root.round;
        handSize = root.handSize;
        gameOver = root.gameOver;
        drawRnd = null;
        if (root.pool.length == 0) {   // nothing hidden: the root's own draw pile, in its order
            deck = root.deck;
            deckPos = root.deckPos;
            deckEnd = root.deckEnd;
            return;
        }
        int m = root.pool.length;
        System.arraycopy(root.pool, 0, scratch, 0, m);
        int next = 0;
        for (int p : root.unknown) {
            int h = p * KINDS;
            java.util.Arrays.fill(a, h, h + KINDS, 0);
            for (int c = 0; c < handSize; c++, next++) {   // partial Fisher-Yates shuffle of the pool
                int j = next + rnd.nextInt(m - next);
                int x = scratch[j];
                scratch[j] = scratch[next];
                scratch[next] = x;
                a[h + x]++;
            }
        }
        deck = scratch;   // the rest of the pool is the draw pile; new hands are drawn from it at random
        deckPos = next;
        deckEnd = m;
        drawRnd = rnd;
    }

    /**
     * MODIFICATION L: this simulation becomes a copy of other. The draw pile is shared: a draw only reorders the cards
     * not yet drawn, so every copy made from the same position draws from the same cards.
     */
    void copyFrom(SushiSim other) {
        System.arraycopy(other.a, 0, a, 0, a.length);
        round = other.round;
        handSize = other.handSize;
        gameOver = other.gameOver;
        deck = other.deck;
        deckPos = other.deckPos;
        deckEnd = other.deckEnd;
        drawRnd = other.drawRnd;
    }

    /** Top card of the draw pile (a random one of the rest of the pool in a sample). */
    private int draw() {
        if (drawRnd != null) {
            int j = deckPos + drawRnd.nextInt(deckEnd - deckPos);
            int x = deck[j];
            deck[j] = deck[deckPos];
            deck[deckPos] = x;
        }
        return deck[deckPos++];
    }

    int poolSize() {
        return pool.length;
    }

    int hand(int p, int kind) {
        return a[p * KINDS + kind];
    }

    int score(int p) {
        return a[base + p * FIELDS + SCORE];
    }

    int pudding(int p) {
        return a[base + p * FIELDS + PUD];
    }

    /** Can p use Chopsticks this turn (Chopsticks on the table and two or more cards in hand)? */
    boolean canChop(int p) {
        return a[base + p * FIELDS + CHOP] > 0 && handSize > 1;
    }

    /**
     * One turn: every player p reveals the card of kind first[p] and, if second[p] >= 0, with its Chopsticks a second
     * card of kind second[p]; then the hands are passed on, or the round is scored when the hands are empty.
     */
    void playTurn(int[] first, int[] second) {
        for (int p = 0; p < n; p++) {
            reveal(p, first[p]);
            if (second[p] >= 0) {
                reveal(p, second[p]);
                a[base + p * FIELDS + CHOP]--;   // the Chopsticks go back into the hand
                a[p * KINDS + CHOPSTICKS]++;
            }
        }
        handSize--;
        if (handSize == 0) endRound();   // scores the round and deals the next one (or ends the game)
        else {   // SGForwardModel.rotatePlayerHands: player p gets the hand of player p + 1
            int[] first0 = new int[KINDS];
            System.arraycopy(a, 0, first0, 0, KINDS);
            System.arraycopy(a, KINDS, a, 0, (n - 1) * KINDS);
            System.arraycopy(first0, 0, a, (n - 1) * KINDS, KINDS);
        }
    }

    /** SGCard.onReveal */
    private void reveal(int p, int k) {
        a[p * KINDS + k]--;
        int f = base + p * FIELDS;
        switch (k) {
            case 0, 1, 2 -> a[f + MAKI] += k + 1;
            case TEMPURA -> {
                if (++a[f + TEMP] % 2 == 0) a[f + SCORE] += par.valueTempuraPair;
            }
            case SASHIMI -> {
                if (++a[f + SASH] % 3 == 0) a[f + SCORE] += par.valueSashimiTriple;
            }
            case DUMPLING -> {
                int d = ++a[f + DUMP];
                a[f + SCORE] += par.valueDumpling[Math.min(d, par.valueDumpling.length) - 1];
            }
            case SQUID, SALMON, EGG -> {
                int v = k == SQUID ? par.valueSquidNigiri : k == SALMON ? par.valueSalmonNigiri : par.valueEggNigiri;
                if (a[f + WAS] > 0) {
                    v *= par.multiplierWasabi;
                    a[f + WAS]--;
                }
                a[f + SCORE] += v;
            }
            case WASABI -> a[f + WAS]++;
            case CHOPSTICKS -> a[f + CHOP]++;
            case PUDDING -> a[f + PUD]++;
        }
    }

    /** SGForwardModel._endRound and, after the last round, the end of the game (same order and tie rules). */
    private void endRound() {
        round++;
        // Maki.onRoundEnd
        int most = 0, secondMost = 0;
        boolean[] mostP = new boolean[n], secondP = new boolean[n];
        for (int i = 0; i < n; i++) {
            int m = a[base + i * FIELDS + MAKI];
            if (m > most) {
                secondMost = most;
                System.arraycopy(mostP, 0, secondP, 0, n);
                most = m;
                java.util.Arrays.fill(mostP, false);
                mostP[i] = true;
            } else if (m == most && m != 0) mostP[i] = true;
            else if (m > secondMost) {
                secondMost = m;
                java.util.Arrays.fill(secondP, false);
                secondP[i] = true;
            } else if (m == secondMost && m != 0) secondP[i] = true;
        }
        int nMost = 0, nSecond = 0;
        for (int i = 0; i < n; i++) {
            if (mostP[i]) nMost++;
            if (secondP[i]) nSecond++;
        }
        if (nMost > 0)
            for (int i = 0; i < n; i++) if (mostP[i]) a[base + i * FIELDS + SCORE] += par.valueMakiMost / nMost;
        if (nSecond > 0 && nMost == 1)
            for (int i = 0; i < n; i++) if (secondP[i]) a[base + i * FIELDS + SCORE] += par.valueMakiSecond / nSecond;
        // played cards are discarded, except Pudding
        for (int i = 0; i < n; i++) {
            int f = base + i * FIELDS;
            a[f + TEMP] = a[f + SASH] = a[f + DUMP] = a[f + WAS] = a[f + MAKI] = a[f + CHOP] = 0;
        }
        if (round < par.nRounds) {   // SGForwardModel._startRound: player by player from the top of the draw pile
            handSize = Math.min(par.nCards - n + 2, (deckEnd - deckPos) / n);
            for (int i = 0; i < n; i++)
                for (int j = 0; j < handSize; j++) a[i * KINDS + draw()]++;
            return;
        }
        // Pudding.onGameEnd
        int best = a[base + PUD], worst = best;
        boolean[] bestP = new boolean[n], worstP = new boolean[n];
        for (int i = 0; i < n; i++) {
            int x = a[base + i * FIELDS + PUD];
            if (x > best) {
                best = x;
                java.util.Arrays.fill(bestP, false);
                bestP[i] = true;
            } else if (x == best && x != 0) bestP[i] = true;
            if (x < worst) {
                worst = x;
                java.util.Arrays.fill(worstP, false);
                worstP[i] = true;
            } else if (x == worst) worstP[i] = true;
        }
        if (best > worst) {
            int nBest = 0, nWorst = 0;
            for (int i = 0; i < n; i++) {
                if (bestP[i]) nBest++;
                if (worstP[i]) nWorst++;
            }
            if (nBest > 0)
                for (int i = 0; i < n; i++) if (bestP[i]) a[base + i * FIELDS + SCORE] += par.valuePuddingMost / nBest;
            if (nWorst > 0 && n > 2)
                for (int i = 0; i < n; i++) if (worstP[i]) a[base + i * FIELDS + SCORE] += par.valuePuddingLeast / nWorst;
        }
        gameOver = true;
    }

    // ---------------------------------------------------------------------------------------------------------
    // card values (the same CardValueModel code as for a game state)
    // ---------------------------------------------------------------------------------------------------------

    /** Maki rolls (this round) of every player. */
    int[] makiAll() {
        return makiAll(new int[n]);
    }

    /** makiAll into m (length n), which is returned. */
    int[] makiAll(int[] m) {
        for (int i = 0; i < n; i++) m[i] = a[base + i * FIELDS + MAKI];
        return m;
    }

    /** Pudding of every player. */
    int[] puddingAll() {
        return puddingAll(new int[n]);
    }

    /** puddingAll into m (length n), which is returned. */
    int[] puddingAll(int[] m) {
        for (int i = 0; i < n; i++) m[i] = a[base + i * FIELDS + PUD];
        return m;
    }

    /** Cards of each type (by SGCardType ordinal) in all hands (CardValueModel.View.supply), null when gamma is off. */
    int[] supply(CardValueModel.Config cfg) {
        return supply(cfg, new int[TYPE_COUNT]);
    }

    /** supply into s (length SGCardType.values().length), which is returned (null when gamma is off). */
    int[] supply(CardValueModel.Config cfg, int[] s) {
        if (cfg.supplyScale() <= 0) return null;
        java.util.Arrays.fill(s, 0);
        for (int k = 0; k < KINDS; k++) {
            int c = 0;
            for (int p = 0; p < n; p++) c += a[p * KINDS + k];
            s[TYPE[k].ordinal()] += c;
        }
        return s;
    }

    static final int TYPE_COUNT = SGCardType.values().length;

    /** CardValueModel.marginalValue of every kind of card in p's hand (0 for kinds p does not hold). */
    void handValues(int p, double[] mv, CardValueModel.Config cfg, int[] makiAll, int[] puddingAll, int[] supply) {
        int f = base + p * FIELDS;
        CardValueModel.Tableau t = new CardValueModel.Tableau(a[f + TEMP], a[f + SASH], a[f + DUMP], a[f + WAS],
                a[f + MAKI], a[f + PUD], a[f + CHOP]);
        double w = (round + 1.0) / par.nRounds;
        for (int k = 0; k < KINDS; k++)
            mv[k] = a[p * KINDS + k] > 0
                    ? CardValueModel.marginalValue(t, makiAll, puddingAll, p, handSize, w, supply, TYPE[k], COUNT[k], par, cfg)
                    : 0.0;
    }

    /** Best value of another card in p's hand than one card of kind k (at least 0), as in chopsticksValue. */
    double bestOther(int p, double[] mv, int k) {
        double best = 0.0;
        for (int j = 0; j < KINDS; j++) {
            int c = a[p * KINDS + j] - (j == k ? 1 : 0);
            if (c > 0) best = Math.max(best, mv[j]);
        }
        return best;
    }

    /** The kind of the card at position pos when p's hand is listed kind by kind. */
    int kindAt(int p, int pos) {
        for (int k = 0; k < KINDS; k++) {
            pos -= a[p * KINDS + k];
            if (pos < 0) return k;
        }
        throw new AssertionError("position outside the hand");
    }

    /**
     * Estimated score of every player at the start of a round, i.e. just after a round was scored
     * (CardValueModel.estimatedScore: the sets and Maki are banked, the new hands are worth nothing yet, Pudding
     * counts as its standing with weight w_t), or the final score when the game is over.
     */
    double[] estimates(CardValueModel.Config cfg) {
        double[] est = new double[n];
        int[] pud = gameOver ? null : puddingAll();
        double w = (round + 1.0) / par.nRounds;
        for (int p = 0; p < n; p++) {
            est[p] = score(p);
            if (!gameOver)
                est[p] += cfg.puddingScale() * w * CardValueModel.puddingStanding(pud, p, pud[p], par);
        }
        return est;
    }
}
