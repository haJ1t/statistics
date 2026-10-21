package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import players.PlayerConstants;
import players.PlayerFactory;

import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;

/**
 * Development tool: shape of the I tree after one search from recorded positions.
 * usage: TreeStats rec.txt label cfg.json nStates iterations
 * Prints per state: iterations, root children, visits of the depth-1 nodes, the share of depth-1 passes made while
 * some player there still had an untried card (= random card), and the visit-weighted depth.
 */
public class TreeStats {
    static Field fChildren, fVisits, fSize, fVal;

    @SuppressWarnings("unchecked")
    static Map<Long, SushiTreeNode> kids(SushiTreeNode x) throws Exception {
        Map<Long, SushiTreeNode> m = (Map<Long, SushiTreeNode>) fChildren.get(x);
        return m == null ? Collections.emptyMap() : m;
    }

    public static void main(String[] a) throws Exception {
        fChildren = SushiTreeNode.class.getDeclaredField("isChildren");
        fVisits = SushiTreeNode.class.getDeclaredField("nVisits");
        fSize = SushiTreeNode.class.getDeclaredField("isSize");
        fVal = SushiTreeNode.class.getDeclaredField("isVal");
        fVal.setAccessible(true);
        for (Field f : new Field[]{fChildren, fVisits, fSize}) f.setAccessible(true);
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String label = a[1], cfg = a[2];
        int nStates = Integer.parseInt(a[3]);
        boolean timed = a[4].startsWith("t");
        int iters = Integer.parseInt(timed ? a[4].substring(1) : a[4]);
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
        double sumKids = 0, sumRandom = 0, sumPass = 0, sumDepthW = 0, sumW = 0, sumIt = 0, shareMe = 0, shareOpp = 0;
        int nOpp = 0;
        int[] hist = new int[5];   // depth-1 node visits: 1, 2-5, 6-20, 21-100, >100
        for (int i = 0; i < N; i++) {
            SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg)).getParameters();
            prm.budgetType = timed ? PlayerConstants.BUDGET_TIME : PlayerConstants.BUDGET_ITERATIONS;
            prm.budget = iters;
            prm.setRandomSeed(17 + 7919L * i);
            SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
            Game game = GameType.SushiGo.createGameInstance(states.get(i).getNPlayers(), 1);
            pl.setForwardModel(game.getForwardModel());
            pl.setPlayerID(movers.get(i));
            AbstractGameState obs = states.get(i).copy(movers.get(i));
            SushiTreeNode root = new SushiTreeNode(pl, null, obs, new Random(3 + i));
            root.mctsSearch(prm.budget, 0, Long.MAX_VALUE);
            sumIt += root.getIterations();
            Map<Long, SushiTreeNode> k1 = kids(root);
            sumKids += k1.size();
            double[][] val = (double[][]) fVal.get(root);
            for (int p = 0; p < val.length; p++) {
                if (val[p] == null) continue;
                double tot = 0, top = 0;
                for (int j = 0; 3 * j < val[p].length; j++) { tot += val[p][3 * j]; top = Math.max(top, val[p][3 * j]); }
                if (tot <= 0) continue;
                if (p == movers.get(i)) shareMe += top / tot; else { shareOpp += top / tot; nOpp++; }
            }
            for (SushiTreeNode c : k1.values()) {
                int v = fVisits.getInt(c);
                hist[v <= 1 ? 0 : v <= 5 ? 1 : v <= 20 ? 2 : v <= 100 ? 3 : 4]++;
                int[] size = (int[]) fSize.get(c);
                int maxSize = 0;
                if (size != null) for (int s : size) maxSize = Math.max(maxSize, s);
                int passes = v - 1;   // the first visit created the node and went to the rollout
                sumPass += passes;
                sumRandom += Math.min(passes, maxSize);
            }
            // visit-weighted depth over the whole tree
            Deque<SushiTreeNode> stack = new ArrayDeque<>(List.of(root));
            while (!stack.isEmpty()) {
                SushiTreeNode x = stack.pop();
                int v = fVisits.getInt(x);
                sumDepthW += (double) x.depth * v;
                sumW += v;
                stack.addAll(kids(x).values());
            }
        }
        int nk = Arrays.stream(hist).sum();
        System.out.printf(Locale.ROOT, "root top-card visit share: ours %.1f%%, opponents %.1f%%%n", 100 * shareMe / N, 100 * shareOpp / Math.max(1, nOpp));
        System.out.printf(Locale.ROOT, "%s iters %d: states %d, iterations %.0f, root children %.0f, depth-1 visits 1:%.0f%% 2-5:%.0f%% 6-20:%.0f%% 21-100:%.0f%% >100:%.0f%%, depth-1 passes with an untried card %.0f%%, mean depth %.2f%n",
                Paths.get(cfg).getFileName(), iters, N, sumIt / N, sumKids / N, 100.0 * hist[0] / nk, 100.0 * hist[1] / nk, 100.0 * hist[2] / nk,
                100.0 * hist[3] / nk, 100.0 * hist[4] / nk, 100 * sumRandom / Math.max(1, sumPass), sumDepthW / sumW);
    }
}
