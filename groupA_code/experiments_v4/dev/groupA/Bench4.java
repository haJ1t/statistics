package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerFactory;

import java.util.*;

/**
 * Tree iterations per decision of groupA configs under their own (time) budget, on fixed 4-player states taken from
 * random games at the start of a turn. usage: Bench4 cfg1.json [cfg2.json ...]
 */
public class Bench4 {
    public static void main(String[] args) {
        List<AbstractGameState> states = new ArrayList<>();
        List<Integer> movers = new ArrayList<>();
        Random r = new Random(5);
        for (int g = 0; g < 6; g++) {
            Game game = GameType.SushiGo.createGameInstance(4, 900 + g);
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < 4; i++) ps.add(new players.simple.RandomPlayer(new Random(g * 7L + i)));
            game.reset(ps);
            AbstractGameState s = game.getGameState();
            AbstractForwardModel fm = game.getForwardModel();
            int step = 0;
            while (s.isNotTerminal()) {
                if (!s.isActionInProgress() && step % 7 == 0) {
                    states.add(s.copy(s.getCurrentPlayer()));
                    movers.add(s.getCurrentPlayer());
                }
                List<AbstractAction> acts = fm.computeAvailableActions(s);
                fm.next(s, acts.get(r.nextInt(acts.size())));
                step++;
            }
        }
        for (String cfg : args) {
            long total = 0;
            int n = 0;
            for (int i = 0; i < states.size(); i++) {
                SushiMCTSPlayer pl = (SushiMCTSPlayer) PlayerFactory.createPlayer(cfg);
                Game game = GameType.SushiGo.createGameInstance(4, 1);
                pl.setForwardModel(game.getForwardModel());
                pl.setPlayerID(movers.get(i));
                AbstractGameState s = states.get(i).copy();
                List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(s);
                if (acts.size() < 2) continue;
                pl._getAction(s, acts);
                total += pl.lastIterations;
                n++;
            }
            System.out.printf("%-60s decisions=%d  mean tree iterations=%.0f%n", cfg, n, total / (double) n);
        }
    }
}
