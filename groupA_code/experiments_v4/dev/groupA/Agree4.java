package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerFactory;

import java.util.*;

/**
 * How often two searches choose the same card on the fixed 4-player states of Bench4: configuration A with two
 * different seeds (decision noise), and A against each other configuration B with the same seed. Each search runs
 * under the configuration's own budget. usage: Agree4 cfgA.json [cfgB.json ...]
 */
public class Agree4 {
    public static void main(String[] a) {
        String cfgA = a[0];
        List<String> others = new ArrayList<>(Arrays.asList(a).subList(1, a.length));
        if (others.isEmpty()) others.add(cfgA);
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
        int n = 0, sameA = 0;
        int[] same = new int[others.size()];
        for (int i = 0; i < states.size(); i++) {
            Game game = GameType.SushiGo.createGameInstance(4, 1);
            AbstractGameState s = states.get(i).copy();
            List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(s);
            if (CardValueModel.distinctActions((games.sushigo.SGGameState) s, acts).size() < 2) continue;
            int a1 = decide(cfgA, 11 + i, game, s, movers.get(i));
            int a2 = decide(cfgA, 77 + i, game, s, movers.get(i));
            n++;
            if (a1 == a2) sameA++;
            for (int b = 0; b < others.size(); b++)
                if (decide(others.get(b), 11 + i, game, s, movers.get(i)) == a1) same[b]++;
        }
        System.out.printf(Locale.ROOT, "%s: decisions %d, same card with another seed %.1f%%%n",
                cfgA.substring(cfgA.lastIndexOf('/') + 1), n, 100.0 * sameA / n);
        for (int b = 0; b < others.size(); b++)
            System.out.printf(Locale.ROOT, "  vs %-44s %.1f%% same%n", others.get(b).substring(others.get(b).lastIndexOf('/') + 1), 100.0 * same[b] / n);
    }

    /** the card key the configuration chooses in state s (seeded search) */
    static int decide(String cfg, long seed, Game game, AbstractGameState s, int mover) {
        SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg)).getParameters();
        prm.setRandomSeed(seed);
        SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
        pl.setForwardModel(game.getForwardModel());
        pl.setPlayerID(mover);
        AbstractGameState obs = s.copy(mover);
        List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(obs);
        AbstractAction act = pl._getAction(obs, acts);
        return CardValueModel.actionKey((games.sushigo.SGGameState) obs, (games.sushigo.actions.ChooseCard) act);
    }
}
