package groupA;

import core.AbstractForwardModel;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import games.sushigo.cards.SGCard.SGCardType;

import java.util.*;

/**
 * Development check of MODIFICATION F: random games are played with TAG's forward model and, turn by turn, with
 * SushiSim from a copy of the same position with the same cards; hands, table counters, scores and the end of the
 * round / game must agree exactly. At every turn the marginal card values and, at the end of every round, the
 * estimated scores of SushiSim are compared with CardValueModel on the game state. Also checks the pool of hidden
 * cards of SushiSim.root against TAG's (unknown hands + draw pile).
 * usage: SushiSimCheck games seed
 */
public class SushiSimCheck {
    static long checks = 0, bad = 0;
    static double maxValueDiff = 0;

    static void check(boolean ok, String what) {
        checks++;
        if (!ok) {
            bad++;
            if (bad <= 20) System.out.println("MISMATCH " + what);
        }
    }

    public static void main(String[] a) {
        int games = Integer.parseInt(a[0]);
        long seed = Long.parseLong(a[1]);
        Random rnd = new Random(seed);
        CardValueModel.Config[] cfgs = {CardValueModel.Config.DEFAULT,
                new CardValueModel.Config(0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3)};
        for (int g = 0; g < games; g++) {
            int n = 2 + g % 4;   // 2..5 players
            Game game = GameType.SushiGo.createGameInstance(n, seed + g);
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
            game.reset(ps);
            SGGameState s = (SGGameState) game.getGameState();
            AbstractForwardModel fm = game.getForwardModel();
            CardValueModel.Config cfg = cfgs[g % 2];
            SushiSim cont = SushiSim.of(s);   // played along the whole game, rounds dealt from its own draw pile
            while (s.isNotTerminal()) {
                SushiSim sim = SushiSim.of(s);
                // values
                CardValueModel.View view = new CardValueModel.View(s, cfg);
                int[] maki = sim.makiAll(), pud = sim.puddingAll(), sup = sim.supply(cfg);
                double[] mv = new double[SushiSim.KINDS];
                for (int p = 0; p < n; p++) {
                    double[] ref = CardValueModel.handValues(view, p);
                    sim.handValues(p, mv, cfg, maki, pud, sup);
                    List<SGCard> hand = s.getPlayerHands().get(p).getComponents();
                    for (int i = 0; i < hand.size(); i++) {
                        double d = Math.abs(ref[i] - mv[SushiSim.kind(hand.get(i))]);
                        maxValueDiff = Math.max(maxValueDiff, d);
                        check(d < 1e-12, "value g" + g + " p" + p + " " + hand.get(i) + " " + ref[i] + " vs " + mv[SushiSim.kind(hand.get(i))]);
                    }
                }
                // hidden-card pool of a root for a random player
                int me = rnd.nextInt(n);
                SGGameState obs = (SGGameState) s.copy(me);
                SushiSim root = SushiSim.root(obs, me);
                Map<Integer, Integer> want = new HashMap<>();
                int unknownCards = 0;
                for (int p = 0; p < n; p++)
                    if (p != me && !obs.isHandKnown(me, p))
                        for (SGCard c : obs.getPlayerHands().get(p).getComponents()) { want.merge(SushiSim.kind(c), 1, Integer::sum); unknownCards++; }
                for (core.components.Component c : obs.getAllTopLevelComponents())
                    if (c instanceof core.components.Deck<?> d && "Draw pile".equals(d.getComponentName()))
                        for (Object o : d.getComponents()) { want.merge(SushiSim.kind((SGCard) o), 1, Integer::sum); unknownCards++; }
                check(root.poolSize() == unknownCards, "pool size g" + g + " " + root.poolSize() + " vs " + unknownCards);
                SushiSim smp = root.emptyCopy();
                smp.sampleFrom(root, rnd, new int[Math.max(1, root.poolSize())]);
                for (int p = 0; p < n; p++) {
                    int c = 0;
                    for (int k = 0; k < SushiSim.KINDS; k++) c += smp.hand(p, k);
                    check(c == s.getPlayerHands().get(p).getSize(), "sample hand size g" + g + " p" + p);
                    if (p == me || obs.isHandKnown(me, p))
                        for (int k = 0; k < SushiSim.KINDS; k++) check(smp.hand(p, k) == sim.hand(p, k), "known hand g" + g + " p" + p);
                }
                // one turn with random choices in both
                int[] first = new int[n], second = new int[n];
                Arrays.fill(second, -1);
                int startRound = s.getRoundCounter();
                do {
                    int p = s.getCurrentPlayer();
                    List<AbstractAction> acts = fm.computeAvailableActions(s);
                    ChooseCard cc = (ChooseCard) acts.get(rnd.nextInt(acts.size()));
                    SGCard card = s.getPlayerHands().get(cc.playerId).get(cc.cardIdx);
                    if (s.isActionInProgress()) second[cc.playerId] = SushiSim.kind(card);
                    else first[cc.playerId] = SushiSim.kind(card);
                    fm.next(s, cc);
                } while (s.isNotTerminal() && !(s.getCurrentSimultaneousPlayers().size() == n && !s.isActionInProgress())
                        && s.getRoundCounter() == startRound);
                sim.playTurn(first, second);
                cont.playTurn(first, second);
                for (int p = 0; p < n; p++) {
                    check(cont.score(p) == (int) s.getGameScore(p), "cont score g" + g + " p" + p);
                    check(cont.pudding(p) == s.getPlayedCardTypes(SGCardType.Pudding, p).getValue(), "cont pudding g" + g);
                    if (s.isNotTerminal())
                        for (int k = 0; k < SushiSim.KINDS; k++) {
                            int c = 0;
                            for (SGCard x : s.getPlayerHands().get(p).getComponents()) if (SushiSim.kind(x) == k) c++;
                            check(cont.hand(p, k) == c, "cont hand g" + g + " p" + p + " kind " + k + " round " + s.getRoundCounter());
                        }
                }
                check(cont.round == s.getRoundCounter() && cont.gameOver == !s.isNotTerminal(), "cont round g" + g);
                if (s.isNotTerminal()) check(cont.handSize == s.getPlayerHands().get(0).getSize(), "cont hand size g" + g);
                boolean roundOver = s.getRoundCounter() != startRound || !s.isNotTerminal();
                check(sim.round == s.getRoundCounter(), "round g" + g);
                check(sim.gameOver == !s.isNotTerminal(), "game over g" + g);
                for (int p = 0; p < n; p++) {
                    check(sim.score(p) == (int) s.getGameScore(p), "score g" + g + " p" + p + " " + sim.score(p) + " vs " + s.getGameScore(p) + (roundOver ? " (round end)" : ""));
                    check(sim.pudding(p) == s.getPlayedCardTypes(SGCardType.Pudding, p).getValue(), "pudding g" + g + " p" + p);
                    if (!roundOver) {
                        for (int k = 0; k < SushiSim.KINDS; k++) {
                            int c = 0;
                            for (SGCard x : s.getPlayerHands().get(p).getComponents()) if (SushiSim.kind(x) == k) c++;
                            check(sim.hand(p, k) == c, "hand g" + g + " p" + p + " kind " + k);
                        }
                        int[] mk = sim.makiAll();
                        check(mk[p] == s.getPlayedCardTypes(SGCardType.Maki, p).getValue(), "maki g" + g);
                        check(sim.canChop(p) == (s.getPlayedCardTypes(SGCardType.Chopsticks, p).getValue() > 0 && s.getPlayerHands().get(p).getSize() > 1), "chop g" + g);
                    }
                }
                if (roundOver) {
                    // estimated scores at the end of the round: SushiSim vs CardValueModel on the new state
                    CardValueModel.View v2 = new CardValueModel.View(s, cfg);
                    double[] est = sim.estimates(cfg);
                    for (int p = 0; p < n; p++) {
                        double ref = CardValueModel.estimatedScore(v2, p);
                        check(Math.abs(ref - est[p]) < 1e-9, "estimate g" + g + " p" + p + " " + ref + " vs " + est[p]);
                    }
                }
            }
        }
        System.out.printf("checks %d, mismatches %d, largest value difference %.3g%n", checks, bad, maxValueDiff);
    }
}
