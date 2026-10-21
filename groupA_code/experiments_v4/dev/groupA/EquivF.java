package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerConstants;

import java.util.*;

/**
 * Deterministic open-loop searches with F (fast simulator) on fixed 4-player states, with the final 4-player
 * settings (X0 + W2 + F + L8, K 1.0); prints a hash of the decisions and iteration counts to compare two builds.
 * usage: EquivF [extraParamsJson-like key=value ...]  e.g. opponentRolloutTemperature=1.5
 */
public class EquivF {
    public static void main(String[] args) {
        List<AbstractGameState> states = new ArrayList<>();
        Random r = new Random(11);
        for (int g = 0; g < 12; g++) {
            Game game = GameType.SushiGo.createGameInstance(4, 177 + g);
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < 4; i++) ps.add(new players.simple.RandomPlayer(new Random(g * 13L + i)));
            game.reset(ps);
            try {   // canonical deal (TAG's own deal depends on identity hash codes, see arena.Arena.canonicalDeal)
                java.lang.reflect.Method m = Class.forName("arena.Arena").getDeclaredMethod("canonicalDeal",
                        games.sushigo.SGGameState.class, long.class);
                m.setAccessible(true);
                m.invoke(null, game.getGameState(), 177L + g);
            } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
            AbstractGameState s = game.getGameState();
            AbstractForwardModel fm = game.getForwardModel();
            int step = 0;
            while (s.isNotTerminal()) {
                if (step % 5 == 0) states.add(s.copy());
                List<AbstractAction> acts = fm.computeAvailableActions(s);
                fm.next(s, acts.get(r.nextInt(acts.size())));
                step++;
            }
        }
        long hash = 17;
        int k = 0, trees = 0;
        for (AbstractGameState s0 : states) {
            SushiMCTSParams p = new SushiMCTSParams();
            p.K = 1.0;
            p.heuristic = new SushiGoHeuristic(15.0, 1.0, 0.5, 6.0, 0.5, 3.0, 0.25, 1.5, 1.25, 3.0, 3.0);
            p.useInformedRollout = true; p.useActionPruning = true; p.useMaxN = true; p.rolloutToRoundEnd = true;
            p.useDecoupledUCT = true; p.useInformationSets = true; p.useFastSim = true; p.leafRollouts = 8;
            p.terminalScale = 2.0; p.sigmaRoundDecay = 0.5; p.opponentScoreValue = true;
            for (String a : args) {
                String[] kv = a.split("=");
                try {
                    java.lang.reflect.Field f = SushiMCTSParams.class.getField(kv[0]);
                    if (f.getType() == int.class) f.set(p, Integer.parseInt(kv[1]));
                    else if (f.getType() == boolean.class) f.set(p, Boolean.parseBoolean(kv[1]));
                    else f.set(p, Double.parseDouble(kv[1]));
                } catch (ReflectiveOperationException e) { throw new RuntimeException(e); }
            }
            p.budgetType = PlayerConstants.BUDGET_ITERATIONS; p.budget = 400;
            p.setRandomSeed(42);
            SushiMCTSPlayer pl = new SushiMCTSPlayer(p);
            Game game = GameType.SushiGo.createGameInstance(4, 1);
            pl.setForwardModel(game.getForwardModel());
            pl.setPlayerID(s0.getCurrentPlayer());
            AbstractGameState s = s0.copy();   // full copy: copy(player) would re-shuffle hidden cards with an unseeded RNG
            List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(s);
            AbstractAction a = pl._getAction(s, acts);
            hash = hash * 31 + a.hashCode();
            hash = hash * 31 + pl.lastIterations;
            trees += pl.lastDeterminisations;
            k++;
        }
        System.out.println("searches=" + k + " trees=" + trees + " trace-hash=" + hash);
    }
}
