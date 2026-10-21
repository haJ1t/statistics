package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerConstants;
import players.PlayerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * TreeStats on the fixed 4-player states of Bench4 (no recorded games needed): per configuration the iterations,
 * CPU microseconds per iteration, root children, visits of the depth-1 nodes, the share of passes through depth-1
 * nodes made while some player there still had an untried card, and the visit-weighted depth.
 * usage: TreeStats4 budget cfg1.json [cfg2.json ...]   (budget: iterations, or tNNN for NNN ms)
 */
public class TreeStats4 {
    public static void main(String[] a) throws Exception {
        Field fVisits = SushiTreeNode.class.getDeclaredField("nVisits");
        Field fSize = SushiTreeNode.class.getDeclaredField("isSize");
        fVisits.setAccessible(true);
        fSize.setAccessible(true);
        TreeStats.fChildren = SushiTreeNode.class.getDeclaredField("isChildren");
        TreeStats.fChildren.setAccessible(true);
        boolean timed = a[0].startsWith("t");
        int budget = Integer.parseInt(timed ? a[0].substring(1) : a[0]);
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
        java.lang.management.ThreadMXBean bean = java.lang.management.ManagementFactory.getThreadMXBean();
        for (int c = 1; c < a.length; c++) {
            String cfg = a[c];
            double sumKids = 0, sumRandom = 0, sumPass = 0, sumDepthW = 0, sumW = 0, sumIt = 0, sumCpu = 0;
            int[] hist = new int[5];   // depth-1 node visits: 1, 2-5, 6-20, 21-100, >100
            int n = 0;
            for (int i = 0; i < states.size(); i++) {
                Game game = GameType.SushiGo.createGameInstance(4, 1);
                AbstractGameState s = states.get(i).copy();
                if (game.getForwardModel().computeAvailableActions(s).size() < 2) continue;
                SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg)).getParameters();
                if (prm.fourPlayer != null) prm = prm.fourPlayer;
                prm.budgetType = timed ? PlayerConstants.BUDGET_TIME : PlayerConstants.BUDGET_ITERATIONS;
                prm.budget = budget;
                prm.setRandomSeed(17 + 7919L * i);
                SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
                pl.setForwardModel(game.getForwardModel());
                pl.setPlayerID(movers.get(i));
                AbstractGameState obs = s.copy(movers.get(i));
                SushiTreeNode root = new SushiTreeNode(pl, null, obs, new Random(3 + i));
                long t0 = bean.getCurrentThreadCpuTime();
                root.mctsSearch(prm.budget, 0, Long.MAX_VALUE);
                sumCpu += (bean.getCurrentThreadCpuTime() - t0) / 1000.0;
                sumIt += root.getIterations();
                n++;
                Map<Long, SushiTreeNode> k1 = TreeStats.kids(root);
                sumKids += k1.size();
                for (SushiTreeNode ch : k1.values()) {
                    int v = fVisits.getInt(ch);
                    hist[v <= 1 ? 0 : v <= 5 ? 1 : v <= 20 ? 2 : v <= 100 ? 3 : 4]++;
                    int[] size = (int[]) fSize.get(ch);
                    int maxSize = 0;
                    if (size != null) for (int z : size) maxSize = Math.max(maxSize, z);
                    int passes = v - 1;   // the first visit created the node and went to the rollout
                    sumPass += passes;
                    sumRandom += Math.min(passes, maxSize);
                }
                Deque<SushiTreeNode> stack = new ArrayDeque<>(List.of(root));
                while (!stack.isEmpty()) {
                    SushiTreeNode x = stack.pop();
                    int v = fVisits.getInt(x);
                    sumDepthW += (double) x.depth * v;
                    sumW += v;
                    stack.addAll(TreeStats.kids(x).values());
                }
            }
            int nk = Math.max(1, Arrays.stream(hist).sum());
            System.out.printf(Locale.ROOT, "%-44s budget %s: states %d, iterations %.0f (%.1f us each), root children %.0f, depth-1 visits 1:%.0f%% 2-5:%.0f%% 6-20:%.0f%% 21-100:%.0f%% >100:%.0f%%, untried passes %.0f%%, mean depth %.2f%n",
                    cfg.substring(cfg.lastIndexOf('/') + 1), a[0], n, sumIt / n, sumCpu / Math.max(1, sumIt), sumKids / n,
                    100.0 * hist[0] / nk, 100.0 * hist[1] / nk, 100.0 * hist[2] / nk, 100.0 * hist[3] / nk, 100.0 * hist[4] / nk,
                    100 * sumRandom / Math.max(1, sumPass), sumDepthW / sumW);
        }
    }
}
