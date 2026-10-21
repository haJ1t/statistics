package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import players.PlayerConstants;
import players.PlayerFactory;

import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

/**
 * Development tool: how well does the search's model of an opponent predict its real pick? For recorded positions of
 * agent "label" (turn start, hand > 2 cards), runs one search and, for every opponent whose hand is known, compares the
 * opponent's root statistics (visit shares per card) with the card it really played in that turn. Prints top-1
 * accuracy and mean log-likelihood of the real pick (visit share, floored at 0.5%), next to the card-value model's
 * greedy prediction, per opponent label.
 * usage: OpponentPredict rec.txt label cfg.json nStates budget(iterations or tMS)
 */
public class OpponentPredict {
    static final double[] TEMPS = {0.5, 1.0, 1.5, 2.0, 3.0, 5.0};
    public static void main(String[] a) throws Exception {
        Field fVal = SushiTreeNode.class.getDeclaredField("isVal"), fKeys = SushiTreeNode.class.getDeclaredField("isKeys"),
                fSize = SushiTreeNode.class.getDeclaredField("isSize");
        for (Field f : new Field[]{fVal, fKeys, fSize}) f.setAccessible(true);
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String label = a[1], cfg = a[2];
        int nStates = Integer.parseInt(a[3]);
        boolean timed = a[4].startsWith("t");
        int budget = Integer.parseInt(timed ? a[4].substring(1) : a[4]);
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
        List<Map<Integer, String[]>> picks = new ArrayList<>();   // per state: player -> record line of its pick in that turn
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
            List<String[]> gl = e.getValue();
            for (int li = 0; li < gl.size(); li++) {
                String[] f = gl.get(li);
                int p = Integer.parseInt(f[2]);
                if (f[3].equals(label) && f[4].equals("0") && s.getPlayerHands().get(p).getSize() > 2 && pick.nextDouble() < 0.5) {
                    Map<Integer, String[]> pk = new HashMap<>();
                    for (String[] h : gl)
                        if (h[4].equals("0") && h[9].equals(f[9]) && h[10].equals(f[10]) && !h[2].equals(f[2]))
                            pk.put(Integer.parseInt(h[2]), h);
                    states.add((SGGameState) s.copy());
                    movers.add(p);
                    picks.add(pk);
                }
                fm.next(s, new ChooseCard(p, Integer.parseInt(f[5]), f[6].equals("1")));
            }
        }
        int N = Math.min(nStates, states.size());
        Map<String, double[]> acc = new TreeMap<>();   // label -> {n, top1, loglik, greedyTop1}
        for (int i = 0; i < N; i++) {
            SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg)).getParameters();
            prm.budgetType = timed ? PlayerConstants.BUDGET_TIME : PlayerConstants.BUDGET_ITERATIONS;
            prm.budget = budget;
            prm.setRandomSeed(17 + 7919L * i);
            SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
            int me = movers.get(i);
            Game game = GameType.SushiGo.createGameInstance(states.get(i).getNPlayers(), 1);
            pl.setForwardModel(game.getForwardModel());
            pl.setPlayerID(me);
            SGGameState obs = (SGGameState) states.get(i).copy(me);
            SushiTreeNode root = new SushiTreeNode(pl, null, obs, new Random(3 + i));
            root.mctsSearch(prm.budget, 0, Long.MAX_VALUE);
            double[][] val = (double[][]) fVal.get(root);
            int[][] keys = (int[][]) fKeys.get(root);
            int[] size = (int[]) fSize.get(root);
            CardValueModel.View view = prm.heuristic instanceof SushiGoHeuristic sh ? sh.view(obs) : new CardValueModel.View(obs);
            for (Map.Entry<Integer, String[]> e : picks.get(i).entrySet()) {
                int q = e.getKey();
                if (!obs.isHandKnown(me, q) || val[q] == null) continue;
                String[] h = e.getValue();
                SGCard.SGCardType type = SGCard.SGCardType.valueOf(h[7]);
                int key = (type.ordinal() * 4 + Integer.parseInt(h[8])) * 2 + (h[6].equals("1") ? 1 : 0);
                double tot = 0, best = -1, actual = 0;
                int bestKey = -1;
                for (int j = 0; j < size[q]; j++) {
                    double v = val[q][3 * j];
                    tot += v;
                    if (v > best) { best = v; bestKey = keys[q][j]; }
                    if (keys[q][j] == key) actual = v;
                }
                if (tot <= 0) continue;
                // greedy card-value prediction (first card only, no chopsticks)
                double[] mv = CardValueModel.handValues(view, q);
                List<SGCard> hand = obs.getPlayerHands().get(q).getComponents();
                int gi = 0;
                for (int c = 1; c < mv.length; c++) if (mv[c] > mv[gi]) gi = c;
                int greedyKey = (hand.get(gi).type.ordinal() * 4 + hand.get(gi).count) * 2;
                double[] r = acc.computeIfAbsent(h[3], k -> new double[4 + TEMPS.length + 1]);
                r[0]++;
                r[1] += bestKey == key ? 1 : 0;
                r[2] += Math.log(Math.max(0.005, actual / tot));
                r[3] += greedyKey == key ? 1 : 0;
                // policy likelihoods of the real first card (chopsticks ignored): softmax(value / T) and eps-greedy 0.2
                if (!h[6].equals("1")) {
                    int chosen = -1;
                    for (int c = 0; c < hand.size(); c++)
                        if (hand.get(c).type == type && hand.get(c).count == Integer.parseInt(h[8])) chosen = c;
                    if (chosen >= 0) {
                        double vmax = Arrays.stream(mv).max().getAsDouble();
                        for (int ti = 0; ti < TEMPS.length; ti++) {
                            double z = 0;
                            for (double v : mv) z += Math.exp((v - vmax) / TEMPS[ti]);
                            r[4 + ti] += Math.log(Math.exp((mv[chosen] - vmax) / TEMPS[ti]) / z);
                        }
                        int nBest = 0;
                        for (double v : mv) if (v > vmax - 1e-9) nBest++;
                        double pe = 0.2 / mv.length + (mv[chosen] > vmax - 1e-9 ? 0.8 / nBest : 0.0);
                        r[4 + TEMPS.length] += Math.log(pe);
                    }
                }
            }
        }
        for (Map.Entry<String, double[]> e : acc.entrySet()) {
            double[] r = e.getValue();
            System.out.printf(Locale.ROOT, "%s %-10s picks %4.0f: tree top-1 %.1f%%, mean log-lik %.3f, greedy top-1 %.1f%%%n",
                    Paths.get(cfg).getFileName(), e.getKey(), r[0], 100 * r[1] / r[0], r[2] / r[0], 100 * r[3] / r[0]);
            StringBuilder sb = new StringBuilder("    policy log-lik (no-chopsticks picks):");
            for (int ti = 0; ti < TEMPS.length; ti++) sb.append(String.format(Locale.ROOT, " T%.1f %.3f", TEMPS[ti], r[4 + ti] / r[0]));
            sb.append(String.format(Locale.ROOT, " eps0.2 %.3f", r[4 + TEMPS.length] / r[0]));
            System.out.println(sb);
        }
    }
}
