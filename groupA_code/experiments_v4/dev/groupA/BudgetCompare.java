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
 * Development tool: does a longer search change the decisions? For nStates recorded positions of agent "label", the
 * same configuration searches with time budgets msA and msB (several seeds each); prints how often two seeds agree at
 * each budget and how often the two budgets agree (and the cards' share of all choices).
 * usage: BudgetCompare rec.txt label cfg.json nStates seeds msA msB
 */
public class BudgetCompare {
    public static void main(String[] a) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String label = a[1], cfg = a[2];
        int nStates = Integer.parseInt(a[3]), seeds = Integer.parseInt(a[4]);
        int[] ms = {Integer.parseInt(a[5]), Integer.parseInt(a[6])};
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
                if (f[3].equals(label) && f[4].equals("0") && s.getPlayerHands().get(p).getSize() > 2 && pick.nextDouble() < 0.25) {
                    states.add((SGGameState) s.copy());
                    movers.add(p);
                }
                fm.next(s, new ChooseCard(p, Integer.parseInt(f[5]), f[6].equals("1")));
            }
        }
        int N = Math.min(nStates, states.size());
        int[][][] choice = new int[2][N][seeds];
        for (int i = 0; i < N; i++)
            for (int sd = 0; sd < seeds; sd++)
                for (int m = 0; m < 2; m++) {
                    SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg)).getParameters();
                    prm.budgetType = PlayerConstants.BUDGET_TIME;
                    prm.budget = ms[m];
                    prm.setRandomSeed(1000L * sd + 17 + 7919L * i);
                    SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
                    Game game = GameType.SushiGo.createGameInstance(states.get(i).getNPlayers(), 1);
                    pl.setForwardModel(game.getForwardModel());
                    pl.setPlayerID(movers.get(i));
                    AbstractGameState obs = states.get(i).copy(movers.get(i));
                    List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(obs);
                    ChooseCard cc = (ChooseCard) pl._getAction(obs, acts);
                    SGCard card = ((SGGameState) obs).getPlayerHands().get(cc.playerId).get(cc.cardIdx);
                    choice[m][i][sd] = (card.type.ordinal() * 4 + card.count) * 2 + (cc.useChopsticks ? 1 : 0);
                }
        double[] self = new double[2];
        double cross = 0, pairsSelf = 0, pairsCross = 0;
        for (int i = 0; i < N; i++)
            for (int x = 0; x < seeds; x++)
                for (int y = 0; y < seeds; y++) {
                    if (x < y) {
                        for (int m = 0; m < 2; m++) if (choice[m][i][x] == choice[m][i][y]) self[m]++;
                        pairsSelf++;
                    }
                    if (choice[0][i][x] == choice[1][i][y]) cross++;
                    pairsCross++;
                }
        System.out.printf(Locale.ROOT, "states %d, seeds %d: %d ms seeds agree %.1f%%, %d ms seeds agree %.1f%%, %d ms vs %d ms %.1f%%%n",
                N, seeds, ms[0], 100 * self[0] / pairsSelf, ms[1], 100 * self[1] / pairsSelf, ms[0], ms[1], 100 * cross / pairsCross);
    }
}
