package check;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import players.simple.RandomPlayer;

import java.util.*;

/** Checks that the optimised groupA.CardValueModel gives exactly the numbers of the frozen original (refv2). */
public class Equiv {
    public static void main(String[] args) {
        Random rnd = new Random(42);
        long checks = 0; double maxErr = 0;
        for (int g = 0; g < 400; g++) {
            int n = 2 + rnd.nextInt(4);
            List<AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) ps.add(new RandomPlayer(new Random(rnd.nextLong())));
            Game game = GameType.SushiGo.createGameInstance(n, rnd.nextLong());
            game.reset(ps);
            while (game.getGameState().isNotTerminal()) {
                SGGameState s = (SGGameState) game.getGameState().copy(rnd.nextInt(n));
                groupA.SushiGoHeuristic hNew = new groupA.SushiGoHeuristic();
                refv2.SushiGoHeuristic hOld = new refv2.SushiGoHeuristic();
                double[] all = hNew.evaluateAll(s);
                for (int p = 0; p < n; p++) {
                    double a = groupA.CardValueModel.estimatedScore(s, p), b = refv2.CardValueModel.estimatedScore(s, p);
                    double c = hNew.evaluateState(s, p), d = hOld.evaluateState(s, p);
                    maxErr = Math.max(maxErr, Math.max(Math.abs(a - b), Math.max(Math.abs(c - d), Math.abs(all[p] - d))));
                    checks += 3;
                }
                if (s.isNotTerminal()) {
                    List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(s);
                    for (AbstractAction act : acts) {
                        double a = groupA.CardValueModel.actionValue(s, act), b = refv2.CardValueModel.actionValue(s, act);
                        maxErr = Math.max(maxErr, Math.abs(a - b));
                        checks++;
                    }
                }
                game.oneAction();
            }
        }
        System.out.printf("EQUIV checks=%d maxAbsDiff=%.3e %s%n", checks, maxErr, maxErr < 1e-12 ? "IDENTICAL" : "DIFFERENT!");
    }
}
