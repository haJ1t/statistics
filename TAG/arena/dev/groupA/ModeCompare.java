package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import players.PlayerConstants;
import players.PlayerFactory;

import java.nio.file.*;
import java.util.*;

/**
 * Development tool: do two configurations make the same decisions? For nStates recorded positions of agent "label",
 * both configurations search with the same iteration budget and several seeds; prints how often two seeds of the
 * same configuration agree, how often the two configurations agree, and the time per iteration of each.
 * usage: ModeCompare rec.txt label cfgA.json cfgB.json nStates seeds iterations
 */
public class ModeCompare {
    public static void main(String[] a) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String label = a[1];
        String[] cfg = {a[2], a[3]};
        int nStates = Integer.parseInt(a[4]), seeds = Integer.parseInt(a[5]), iters = Integer.parseInt(a[6]);
        Map<Integer, String> deals = new HashMap<>();
        Map<Integer, Integer> nPl = new HashMap<>();
        Map<Integer, List<String[]>> byGame = new TreeMap<>();
        for (String l : lines) {
            String[] f = l.trim().split(" ");
            if (f[0].equals("DEAL")) { deals.put(Integer.parseInt(f[1]), f[3]); nPl.put(Integer.parseInt(f[1]), Integer.parseInt(f[2])); continue; }
            if (f[0].equals("END")) continue;
            byGame.computeIfAbsent(Integer.parseInt(f[0]), x -> new ArrayList<>()).add(f);
        }
        List<SGGameState> states = new ArrayList<>();
        List<Integer> movers = new ArrayList<>();
        Random pick = new Random(5);
        for (Map.Entry<Integer, List<String[]>> e : byGame.entrySet()) {
            int g = e.getKey();
            if (!deals.containsKey(g)) continue;
            int n = nPl.get(g);
            Game game = GameType.SushiGo.createGameInstance(n, 1);
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
            game.reset(ps);
            SGGameState s = (SGGameState) game.getGameState();
            Imitate.setDeal(s, deals.get(g));
            AbstractForwardModel fm = game.getForwardModel();
            for (String[] f : e.getValue()) {
                int p = Integer.parseInt(f[2]);
                if (f[3].equals(label) && f[4].equals("0") && s.getPlayerHands().get(p).getSize() > 1 && pick.nextDouble() < 0.25) {
                    states.add((SGGameState) s.copy());
                    movers.add(p);
                }
                fm.next(s, new ChooseCard(p, Integer.parseInt(f[5]), f[6].equals("1")));
            }
        }
        int N = Math.min(nStates, states.size());
        int[][][] choice = new int[2][N][seeds];
        long[] nanos = new long[2], its = new long[2];
        for (int i = 0; i < N; i++) {
            for (int sd = 0; sd < seeds; sd++) {
                for (int m = 0; m < 2; m++) {
                    SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg[m])).getParameters();
                    prm.budgetType = PlayerConstants.BUDGET_ITERATIONS;
                    prm.budget = iters;
                    prm.setRandomSeed(1000L * sd + 17 + 7919L * i);
                    SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
                    Game game = GameType.SushiGo.createGameInstance(states.get(i).getNPlayers(), 1);
                    pl.setForwardModel(game.getForwardModel());
                    pl.setPlayerID(movers.get(i));
                    AbstractGameState obs = states.get(i).copy(movers.get(i));
                    List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(obs);
                    long t0 = System.nanoTime();
                    ChooseCard cc = (ChooseCard) pl._getAction(obs, acts);
                    nanos[m] += System.nanoTime() - t0;
                    its[m] += pl.lastIterations;
                    SGCard card = ((SGGameState) obs).getPlayerHands().get(cc.playerId).get(cc.cardIdx);
                    choice[m][i][sd] = (card.type.ordinal() * 4 + card.count) * 2 + (cc.useChopsticks ? 1 : 0);
                }
            }
        }
        double[] self = new double[2];
        double cross = 0, pairsSelf = 0, pairsCross = 0;
        for (int i = 0; i < N; i++) {
            for (int x = 0; x < seeds; x++) {
                for (int y = 0; y < seeds; y++) {
                    if (x < y) {
                        for (int m = 0; m < 2; m++) if (choice[m][i][x] == choice[m][i][y]) self[m]++;
                        pairsSelf++;
                    }
                    if (choice[0][i][x] == choice[1][i][y]) cross++;
                    pairsCross++;
                }
            }
        }
        System.out.printf(Locale.ROOT, "states %d, %d iterations: A seeds agree %.1f%%, B seeds agree %.1f%%, A vs B %.1f%%; us/iteration A %.1f, B %.1f%n",
                N, iters, 100 * self[0] / pairsSelf, 100 * self[1] / pairsSelf, 100 * cross / pairsCross,
                nanos[0] / 1000.0 / its[0], nanos[1] / 1000.0 / its[1]);
    }
}
