package arena;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.cards.SGCard;
import players.PlayerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight parallel benchmark for Sushi Go (development tool, NOT part of the groupA submission).
 * <p>
 * Usage: java arena.Arena players=3 games=500 threads=16 seed=1 focus=a.json opps=b.json,c.json out=res.csv
 * <ul>
 *   <li>game g uses game seed (seed + g): identical deals across conditions (paired comparison);</li>
 *   <li>the focus agent sits in seat g % n; opponents fill the other seats, their order rotated every n games;</li>
 *   <li>a shared first place counts 1/k as a win.</li>
 * </ul>
 */
public class Arena {

    record Result(int game, int focusSeat, double focusWin, double focusScore, double bestOppScore,
                  double[] scores, int[] ordinals, String[] names, double msPerDecision, double[][] typePoints,
                  double gameWallMs, int nDecisions) {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> a = new HashMap<>();
        for (String s : args) {
            int i = s.indexOf('=');
            if (i > 0) a.put(s.substring(0, i), s.substring(i + 1));
        }
        int n = Integer.parseInt(a.getOrDefault("players", "3"));
        int nGames = Integer.parseInt(a.getOrDefault("games", "100"));
        int threads = Integer.parseInt(a.getOrDefault("threads", "16"));
        long seed = Long.parseLong(a.getOrDefault("seed", "1"));
        String focus = a.get("focus");
        List<String> opps = Arrays.asList(a.get("opps").split(","));
        String out = a.getOrDefault("out", "");
        String label = a.getOrDefault("label", focus);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Result>> futures = new ArrayList<>();
        AtomicInteger done = new AtomicInteger();
        long t0 = System.currentTimeMillis();
        for (int g = 0; g < nGames; g++) {
            final int game = g;
            futures.add(pool.submit(() -> {
                Result r = play(game, n, seed, focus, opps);
                int d = done.incrementAndGet();
                if (d % 50 == 0)
                    System.err.printf("  [%s] %d/%d games, %.0fs%n", label, d, nGames, (System.currentTimeMillis() - t0) / 1000.0);
                return r;
            }));
        }
        List<Result> results = new ArrayList<>();
        try {
            for (Future<Result> f : futures) results.add(f.get());
        } catch (ExecutionException e) {
            e.getCause().printStackTrace();
            pool.shutdownNow();
            System.exit(1);
        }
        pool.shutdown();

        // ---- summary ----
        double wins = 0, score = 0, diff = 0, diff2 = 0, ms = 0;
        Map<String, double[]> perAgent = new TreeMap<>(); // name -> {wins, seats}
        Map<String, double[]> perAgentTypes = new TreeMap<>(); // name -> summed points per card type
        for (Result r : results) {
            wins += r.focusWin;
            score += r.focusScore;
            double d = r.focusScore - r.bestOppScore;
            diff += d;
            diff2 += d * d;
            ms += r.msPerDecision;
            int k = 0;
            for (int o : r.ordinals) if (o == 1) k++;
            for (int p = 0; p < n; p++) {
                String key = r.names[p] + (p == r.focusSeat ? " [focus]" : "");
                double[] tp = perAgentTypes.computeIfAbsent(key, x -> new double[SGCard.SGCardType.values().length]);
                for (int t = 0; t < tp.length; t++) tp[t] += r.typePoints[p][t];
                double[] v = perAgent.computeIfAbsent(key, x -> new double[2]);
                v[0] += (r.ordinals[p] == 1) ? 1.0 / k : 0.0;
                v[1] += 1;
            }
        }
        int N = results.size();
        double p = wins / N;
        double z = 1.96;
        double centre = (p + z * z / (2 * N)) / (1 + z * z / N);
        double half = z * Math.sqrt(p * (1 - p) / N + z * z / (4.0 * N * N)) / (1 + z * z / N);
        double meanDiff = diff / N;
        double sdDiff = Math.sqrt(Math.max(0, diff2 / N - meanDiff * meanDiff));
        System.out.printf("RESULT %s | n=%d games=%d | win=%.1f%% [%.1f, %.1f] (chance %.1f%%) | score=%.2f | diffBest=%.2f +- %.2f | ms/dec(all)=%.0f | %.0fs%n",
                label, n, N, 100 * p, 100 * (centre - half), 100 * (centre + half), 100.0 / n, score / N,
                meanDiff, 1.96 * sdDiff / Math.sqrt(N), ms / N, (System.currentTimeMillis() - t0) / 1000.0);
        for (Map.Entry<String, double[]> e : perAgent.entrySet()) {
            System.out.printf("    %-45s win %.1f%% over %d seats%n", e.getKey(), 100 * e.getValue()[0] / e.getValue()[1], (int) e.getValue()[1]);
            if (a.containsKey("types")) {
                StringBuilder sb = new StringBuilder("        pts/game:");
                double[] tp = perAgentTypes.get(e.getKey());
                for (SGCard.SGCardType t : SGCard.SGCardType.values())
                    sb.append(String.format(" %s=%.2f", t.name(), tp[t.ordinal()] / e.getValue()[1]));
                System.out.println(sb);
            }
        }

        if (!out.isEmpty()) {
            try (PrintWriter w = new PrintWriter(new FileWriter(out))) {
                StringBuilder header = new StringBuilder("game,focusSeat,focusWin,focusScore,bestOppScore,msPerDecision,gameWallMs,nDecisions");
                for (SGCard.SGCardType t : SGCard.SGCardType.values()) header.append(",pts_").append(t.name());  // focus agent's points per card type
                w.println(header);
                for (Result r : results) {
                    w.printf(Locale.ROOT, "%d,%d,%.4f,%.1f,%.1f,%.1f,%.0f,%d", r.game, r.focusSeat, r.focusWin, r.focusScore, r.bestOppScore, r.msPerDecision, r.gameWallMs, r.nDecisions);
                    for (SGCard.SGCardType t : SGCard.SGCardType.values()) w.printf(Locale.ROOT, ",%.0f", r.typePoints[r.focusSeat][t.ordinal()]);
                    w.println();
                }
            }
        }
    }

    static Result play(int g, int n, long seed, String focus, List<String> opps) {
        int focusSeat = g % n;
        int shift = (g / n) % opps.size();
        List<AbstractPlayer> players = new ArrayList<>();
        String[] names = new String[n];
        int oi = 0;
        for (int s = 0; s < n; s++) {
            String spec = (s == focusSeat) ? focus : opps.get((oi++ + shift) % opps.size());
            AbstractPlayer pl = PlayerFactory.createPlayer(spec);
            players.add(pl);
            names[s] = shortName(spec);
        }
        long gStart = System.nanoTime();
        Game game = GameType.SushiGo.createGameInstance(n, seed + g);
        game.reset(players);
        game.run();
        double gameWallMs = (System.nanoTime() - gStart) / 1e6;
        AbstractGameState gs = game.getGameState();
        double[] scores = new double[n];
        double[][] typePoints = new double[n][SGCard.SGCardType.values().length];
        SGGameState sgs = (SGGameState) gs;
        for (int p = 0; p < n; p++)
            for (SGCard.SGCardType t : SGCard.SGCardType.values())
                typePoints[p][t.ordinal()] = sgs.getPointsPerCardType()[p].get(t).getValue();
        int[] ord = new int[n];
        int k = 0;
        for (int p = 0; p < n; p++) {
            scores[p] = gs.getGameScore(p);
            ord[p] = gs.getOrdinalPosition(p);
            if (ord[p] == 1) k++;
        }
        double best = -1e9;
        for (int p = 0; p < n; p++) if (p != focusSeat) best = Math.max(best, scores[p]);
        double win = ord[focusSeat] == 1 ? 1.0 / k : 0.0;
        double ms = game.getNDecisions() > 0 ? game.getAgentTime() / 1e6 : 0;  // getAgentTime() is already per decision (ns)
        return new Result(g, focusSeat, win, scores[focusSeat], best, scores, ord, names, ms, typePoints, gameWallMs, game.getNDecisions());
    }

    static String shortName(String spec) {
        String s = spec.replace('\\', '/');
        int i = s.lastIndexOf('/');
        return i >= 0 ? s.substring(i + 1) : s;
    }
}
