package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;

import java.util.*;

/** Checks CardValueModel.distinctCards against the forward model and handValues against actionValue. */
public class EquivI {
    public static void main(String[] args) {
        long checksA = 0, checksB = 0, chopStates = 0;
        Random r = new Random(7);
        for (int n = 3; n <= 5; n++) {
            for (int g = 0; g < 400; g++) {
                Game game = GameType.SushiGo.createGameInstance(n, 1000L * n + g);
                List<core.AbstractPlayer> ps = new ArrayList<>();
                for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(g * 31L + i)));
                game.reset(ps);
                SGGameState s = (SGGameState) game.getGameState();
                AbstractForwardModel fm = game.getForwardModel();
                SushiGoHeuristic h = new SushiGoHeuristic();
                while (s.isNotTerminal()) {
                    if (!s.isActionInProgress()) {
                        for (int p = 0; p < n; p++) {
                            if (s.getPlayerHands().get(p).getSize() == 0) continue;
                            List<AbstractAction> acts = fm.computeAvailableActions(s, s.getCoreGameParameters().actionSpace, p);
                            List<AbstractAction> dist = CardValueModel.distinctActions(s, acts);
                            int m = 2 * s.getPlayerHands().get(p).getSize();
                            int[] keys = new int[m], idx = new int[m], chop = new int[m];
                            int d = CardValueModel.distinctCards(s, p, keys, idx, chop);
                            if (d != dist.size()) throw new AssertionError("size " + d + " vs " + dist.size());
                            for (int x = 0; x < d; x++) {
                                ChooseCard cc = (ChooseCard) dist.get(x);
                                if (cc.playerId != p || cc.cardIdx != idx[x] || cc.useChopsticks != (chop[x] == 1)
                                        || CardValueModel.actionKey(s, cc) != keys[x])
                                    throw new AssertionError("mismatch at " + x);
                                if (chop[x] == 1) chopStates++;
                            }
                            checksA++;
                        }
                    }
                    // greedy values for the player who moves
                    List<AbstractAction> avail = fm.computeAvailableActions(s);
                    CardValueModel.View view = h.view(s);
                    int mover = ((ChooseCard) avail.get(0)).playerId;
                    double[] mv = CardValueModel.handValues(view, mover);
                    int top = 0;
                    for (int i = 1; i < mv.length; i++) if (mv[i] > mv[top]) top = i;
                    double second = -Double.MAX_VALUE;
                    for (int i = 0; i < mv.length; i++) if (i != top && mv[i] > second) second = mv[i];
                    for (AbstractAction a : avail) {
                        ChooseCard cc = (ChooseCard) a;
                        double vNew = mv[cc.cardIdx];
                        if (cc.useChopsticks) vNew += Math.max(0.0, cc.cardIdx == top ? second : mv[top]) - CardValueModel.CHOPSTICKS_VALUE;
                        double vOld = CardValueModel.actionValue(view, a);
                        if (vNew != vOld) throw new AssertionError("value " + vNew + " vs " + vOld + " " + cc);
                        checksB++;
                    }
                    AbstractAction a = avail.get(r.nextInt(avail.size()));
                    fm.next(s, a);
                }
            }
        }
        System.out.println("OK distinctCards checks=" + checksA + " (chopsticks keys seen " + chopStates + "), value checks=" + checksB);
    }
}
