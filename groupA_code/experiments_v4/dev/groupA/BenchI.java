package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerConstants;
import players.PlayerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/** CPU time per MCTS iteration of a groupA config on fixed states (fixed iteration budget). usage: BenchI cfg.json */
public class BenchI {
    public static void main(String[] args) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        List<AbstractGameState> states = new ArrayList<>();
        List<Integer> movers = new ArrayList<>();
        Random r = new Random(3);
        for (int g = 0; g < 12; g++) {
            Game game = GameType.SushiGo.createGameInstance(3, 500 + g);
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < 3; i++) ps.add(new players.simple.RandomPlayer(new Random(g * 7L + i)));
            game.reset(ps);
            AbstractGameState s = game.getGameState();
            AbstractForwardModel fm = game.getForwardModel();
            int step = 0;
            while (s.isNotTerminal()) {
                if (!s.isActionInProgress() && step % 5 == 0) {
                    states.add(s.copy(s.getCurrentPlayer()));
                    movers.add(s.getCurrentPlayer());
                }
                List<AbstractAction> acts = fm.computeAvailableActions(s);
                fm.next(s, acts.get(r.nextInt(acts.size())));
                step++;
            }
        }
        int iters = Integer.parseInt(args.length > 1 ? args[1] : "1500");
        for (int rep = 0; rep < 4; rep++) {
            long cpu = 0; long totalIt = 0;
            for (int i = 0; i < states.size(); i++) {
                SushiMCTSPlayer pl = (SushiMCTSPlayer) PlayerFactory.createPlayer(args[0]);
                pl.getParameters().budgetType = PlayerConstants.BUDGET_ITERATIONS;
                pl.getParameters().budget = iters;
                Game game = GameType.SushiGo.createGameInstance(3, 1);
                pl.setForwardModel(game.getForwardModel());
                pl.setPlayerID(movers.get(i));
                AbstractGameState s = states.get(i);
                List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(s);
                long t0 = bean.getCurrentThreadCpuTime();
                pl._getAction(s, acts);
                cpu += bean.getCurrentThreadCpuTime() - t0;
                totalIt += pl.lastIterations;
            }
            System.out.printf("rep %d: states=%d iterations=%d  us/iteration=%.2f%n", rep, states.size(), totalIt, cpu / 1000.0 / totalIt);
        }
    }
}
