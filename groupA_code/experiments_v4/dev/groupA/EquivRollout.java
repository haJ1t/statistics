package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerConstants;

import java.util.*;

/** Deterministic closed-loop searches on fixed states; prints a trace to compare two builds. */
public class EquivRollout {
    public static void main(String[] args) {
        List<AbstractGameState> states = new ArrayList<>();
        Random r = new Random(11);
        for (int n = 3; n <= 4; n++)
            for (int g = 0; g < 10; g++) {
                Game game = GameType.SushiGo.createGameInstance(n, 77 + 10 * n + g);
                List<core.AbstractPlayer> ps = new ArrayList<>();
                for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(g * 13L + i)));
                game.reset(ps);
                AbstractGameState s = game.getGameState();
                AbstractForwardModel fm = game.getForwardModel();
                int step = 0;
                while (s.isNotTerminal()) {
                    if (step % 3 == 0) states.add(s.copy());
                    List<AbstractAction> acts = fm.computeAvailableActions(s);
                    fm.next(s, acts.get(r.nextInt(acts.size())));
                    step++;
                }
            }
        long hash = 17;
        int k = 0;
        for (boolean ductOn : new boolean[]{false, true}) {
            for (AbstractGameState s0 : states) {
                SushiMCTSParams p = new SushiMCTSParams();
                p.K = 0.5; p.heuristic = new SushiGoHeuristic(); p.useInformedRollout = true; p.useActionPruning = true;
                p.useMaxN = true; p.rolloutToRoundEnd = true; p.useDecoupledUCT = ductOn;
                p.budgetType = PlayerConstants.BUDGET_ITERATIONS; p.budget = 150;
                p.setRandomSeed(42);
                SushiMCTSPlayer pl = new SushiMCTSPlayer(p);
                Game game = GameType.SushiGo.createGameInstance(s0.getNPlayers(), 1);
                pl.setForwardModel(game.getForwardModel());
                pl.setPlayerID(s0.getCurrentPlayer());
                AbstractGameState s = s0.copy();
                List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(s);
                AbstractAction a = pl._getAction(s, acts);
                hash = hash * 31 + a.hashCode();
                hash = hash * 31 + pl.lastIterations;
                k++;
            }
        }
        System.out.println("searches=" + k + " states=" + states.size() + " trace-hash=" + hash);
    }
}
