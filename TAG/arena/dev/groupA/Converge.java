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
import java.util.concurrent.*;

/**
 * Development tool: does the root decision of a groupA config change with the number of iterations?
 * usage: Converge rec.txt label cfg.json nStates seeds iters1,iters2,...
 * For nStates decisions of agent "label" in the recorded games (replayed exactly), the agent searches from that
 * player's observation with each iteration budget and each seed; prints how often the decision equals the majority
 * decision of the largest budget, and how often two seeds agree at the same budget.
 */
public class Converge {
    public static void main(String[] a) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String label = a[1], cfg = a[2];
        int nStates = Integer.parseInt(a[3]), seeds = Integer.parseInt(a[4]);
        int[] iters = Arrays.stream(a[5].split(",")).mapToInt(Integer::parseInt).toArray();
        int threads = Integer.parseInt(System.getProperty("threads", "4"));

        // replay and collect (observation, player) for the label's decisions
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
        Collections.shuffle(states, new Random(1));
        Collections.shuffle(movers, new Random(1));   // same permutation (same seed, same size)
        int N = Math.min(nStates, states.size());
        System.err.println("states " + N + " of " + states.size());

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        int[][][] choice = new int[N][iters.length][seeds];
        List<Future<?>> fs = new ArrayList<>();
        for (int i = 0; i < N; i++) for (int b = 0; b < iters.length; b++) for (int sd = 0; sd < seeds; sd++) {
            final int ii = i, bb = b, ss = sd;
            fs.add(pool.submit(() -> {
                SushiMCTSParams prm = ((SushiMCTSPlayer) PlayerFactory.createPlayer(cfg)).getParameters();
                prm.budgetType = PlayerConstants.BUDGET_ITERATIONS;
                prm.budget = iters[bb];
                prm.setRandomSeed(1000L * ss + 17 + 7919L * ii);
                SushiMCTSPlayer pl = new SushiMCTSPlayer(prm);
                Game game = GameType.SushiGo.createGameInstance(states.get(ii).getNPlayers(), 1);
                pl.setForwardModel(game.getForwardModel());
                pl.setPlayerID(movers.get(ii));
                AbstractGameState obs = states.get(ii).copy(movers.get(ii));
                List<AbstractAction> acts = game.getForwardModel().computeAvailableActions(obs);
                ChooseCard cc = (ChooseCard) pl._getAction(obs, acts);
                SGCard card = ((SGGameState) obs).getPlayerHands().get(cc.playerId).get(cc.cardIdx);
                choice[ii][bb][ss] = (card.type.ordinal() * 4 + card.count) * 2 + (cc.useChopsticks ? 1 : 0);
                return null;
            }));
        }
        for (Future<?> f : fs) f.get();
        pool.shutdown();

        int last = iters.length - 1;
        for (int b = 0; b < iters.length; b++) {
            double agreeRef = 0, agreeSelf = 0, pairs = 0;
            for (int i = 0; i < N; i++) {
                Map<Integer, Integer> m = new HashMap<>();
                for (int sd = 0; sd < seeds; sd++) m.merge(choice[i][last][sd], 1, Integer::sum);
                int ref = Collections.max(m.entrySet(), Map.Entry.comparingByValue()).getKey();
                for (int sd = 0; sd < seeds; sd++) if (choice[i][b][sd] == ref) agreeRef++;
                for (int x = 0; x < seeds; x++) for (int y = x + 1; y < seeds; y++) { pairs++; if (choice[i][b][x] == choice[i][b][y]) agreeSelf++; }
            }
            System.out.printf("iterations %7d: same as majority at %d: %.1f%%   two seeds agree: %.1f%%%n", iters[b], iters[last],
                    100 * agreeRef / (N * seeds), 100 * agreeSelf / pairs);
        }
    }
}
