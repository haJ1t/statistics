package groupA;

import core.AbstractForwardModel;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;

import java.util.*;
import java.util.concurrent.*;

/**
 * Development tool (not part of the submission): strength of epsilon-greedy card-value policies, no search.
 * A config is "alpha,T,beta,Tm,kappa,delta,pi,nu" (CardValueModel.Config).
 * <pre>
 * eval: GreedyTune eval n games eps threads cand pool1;pool2;...
 * tune: GreedyTune tune n1,n2 games eps threads start pool1;pool2;...   (coordinate search, pool = opponents)
 * </pre>
 * The candidate sits in seat g % n, the other seats are filled from the pool (rotated); game g uses deal 9000 + g and
 * its own random numbers, so two candidates are compared on the same deals.
 */
public class GreedyTune {

    static CardValueModel.Config parse(String s) {
        String[] f = s.split(",");
        double[] v = new double[9];
        double[] def = {0.5, 4, 1, 0, 0, 0, 1, 2, 0};
        for (int i = 0; i < 9; i++) v[i] = i < f.length ? Double.parseDouble(f[i]) : def[i];
        return fromArr(v);
    }

    static String str(CardValueModel.Config c) {
        return String.format(Locale.ROOT, "%.3g,%.3g,%.3g,%.3g,%.3g,%.3g,%.3g,%.3g,%.3g", c.potentialWeight(), c.completionHorizon(),
                c.sashimiDiscount(), c.makiHorizon(), c.makiIconValue(), c.dumplingWeight(), c.puddingScale(), c.avgNigiri(),
                c.supplyScale());
    }

    /** one game; returns {win share of the candidate, candidate score - mean opponent score} */
    static double[] play(int n, int g, double eps, CardValueModel.Config cand, List<CardValueModel.Config> pool) {
        Game game = GameType.SushiGo.createGameInstance(n, 9000 + g);
        List<core.AbstractPlayer> ps = new ArrayList<>();
        for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
        game.reset(ps);
        SGGameState s = (SGGameState) game.getGameState();
        AbstractForwardModel fm = game.getForwardModel();
        int seat = g % n;
        CardValueModel.Config[] cfg = new CardValueModel.Config[n];
        int oi = 0, shift = g / n;
        for (int p = 0; p < n; p++) cfg[p] = p == seat ? cand : pool.get((oi++ + shift) % pool.size());
        Random r = new Random(77L * g + 1);
        while (s.isNotTerminal()) {
            List<AbstractAction> acts = fm.computeAvailableActions(s);
            int mover = ((ChooseCard) acts.get(0)).playerId;
            AbstractAction choice = null;
            double u = r.nextDouble();
            int ri = r.nextInt(acts.size());
            if (u < eps) choice = acts.get(ri);
            else {
                CardValueModel.View view = new CardValueModel.View(s, cfg[mover]);
                double best = -1e9;
                for (AbstractAction a : acts) {
                    double v = CardValueModel.actionValue(view, a) + 1e-6 * r.nextDouble();
                    if (v > best) { best = v; choice = a; }
                }
            }
            fm.next(s, choice);
        }
        double best = -1e9, sumOpp = 0;
        int nBest = 0;
        for (int p = 0; p < n; p++) best = Math.max(best, s.getGameScore(p));
        for (int p = 0; p < n; p++) {
            if (s.getGameScore(p) == best) nBest++;
            if (p != seat) sumOpp += s.getGameScore(p);
        }
        double win = s.getGameScore(seat) == best ? 1.0 / nBest : 0.0;
        return new double[]{win, s.getGameScore(seat) - sumOpp / (n - 1)};
    }

    static ExecutorService pool;

    /** {win rate, standard error, mean score margin} over games 0..games-1 */
    static double[] eval(int n, int games, double eps, CardValueModel.Config cand, List<CardValueModel.Config> opp) throws Exception {
        int chunks = 64;
        List<Future<double[]>> fs = new ArrayList<>();
        for (int c = 0; c < chunks; c++) {
            final int c0 = c;
            fs.add(pool.submit(() -> {
                double w = 0, w2 = 0, m = 0;
                for (int g = c0; g < games; g += chunks) {
                    double[] res = play(n, g, eps, cand, opp);
                    w += res[0];
                    w2 += res[0] * res[0];
                    m += res[1];
                }
                return new double[]{w, w2, m};
            }));
        }
        double w = 0, w2 = 0, m = 0;
        for (Future<double[]> f : fs) {
            double[] x = f.get();
            w += x[0];
            w2 += x[1];
            m += x[2];
        }
        double p = w / games;
        double se = Math.sqrt(Math.max(0, w2 / games - p * p) / games);
        return new double[]{p, se, m / games};
    }

    public static void main(String[] a) throws Exception {
        String mode = a[0];
        int games = Integer.parseInt(a[2]);
        double eps = Double.parseDouble(a[3]);
        pool = Executors.newFixedThreadPool(Integer.parseInt(a[4]));
        List<CardValueModel.Config> opp = new ArrayList<>();
        for (String s : a[6].split(";")) opp.add(parse(s));
        int[] ns = Arrays.stream(a[1].split(",")).mapToInt(Integer::parseInt).toArray();
        if (mode.equals("eval")) {
            for (String cs : a[5].split(";")) {
                CardValueModel.Config c = parse(cs);
                StringBuilder sb = new StringBuilder(str(c));
                for (int n : ns) {
                    double[] r = eval(n, games, eps, c, opp);
                    sb.append(String.format(Locale.ROOT, " | n=%d win %.2f%% +- %.2f (chance %.1f) margin %+.2f", n, 100 * r[0], 196 * r[1], 100.0 / n, r[2]));
                }
                System.out.println(sb);
            }
        } else {
            // coordinate search: score = mean over n of (win - 1/n) / (1/n)  (relative excess win rate)
            double[][] grid = {
                    {0.15, 0.2, 0.25, 0.3, 0.35, 0.4, 0.5},       // alpha
                    {2, 3, 4, 5, 6, 8},                             // T
                    {0.3, 0.5, 0.6, 0.7, 0.8, 1.0},                 // beta
                    {0, 1, 2, 3, 4, 6, 8},                          // T_m
                    {0, 0.25, 0.5, 0.75, 1.0, 1.5},                 // kappa
                    {0, 0.25, 0.5, 1.0, 1.5, 2.0},                  // delta
                    {0.5, 0.75, 1.0, 1.25, 1.5},                    // pi
                    {1.5, 2, 2.5, 3, 4, 5},                         // nu
                    {0, 1, 2, 3, 4, 5, 6}                           // gamma
            };
            String[] names = {"alpha", "T", "beta", "T_m", "kappa", "delta", "pi", "nu", "gamma"};
            double[] cur = toArr(parse(a[5]));
            double curScore = score(ns, games, eps, fromArr(cur), opp);
            System.out.printf("start %s score %.4f%n", str(fromArr(cur)), curScore);
            int rounds = a.length > 7 ? Integer.parseInt(a[7]) : 2;
            Set<Integer> only = null;
            if (a.length > 8) {
                only = new HashSet<>();
                for (String x : a[8].split(",")) only.add(Integer.parseInt(x));
            }
            for (int round = 0; round < rounds; round++) {
                boolean improved = false;
                for (int k = 0; k < grid.length; k++) {
                    if (only != null && !only.contains(k)) continue;
                    double bestV = cur[k], bestS = curScore;
                    for (double v : grid[k]) {
                        if (v == cur[k]) continue;
                        double[] c = cur.clone();
                        c[k] = v;
                        double sc = score(ns, games, eps, fromArr(c), opp);
                        System.out.printf("  %s=%.3g -> %.4f%n", names[k], v, sc);
                        if (sc > bestS) { bestS = sc; bestV = v; }
                    }
                    if (bestV != cur[k]) {
                        cur[k] = bestV;
                        curScore = bestS;
                        improved = true;
                    }
                    System.out.printf("round %d after %s: %s score %.4f%n", round, names[k], str(fromArr(cur)), curScore);
                }
                if (!improved) break;
            }
            System.out.println("BEST " + str(fromArr(cur)) + " score " + curScore);
        }
        pool.shutdown();
    }

    static double score(int[] ns, int games, double eps, CardValueModel.Config c, List<CardValueModel.Config> opp) throws Exception {
        double s = 0;
        for (int n : ns) s += (eval(n, games, eps, c, opp)[0] * n - 1.0);
        return s / ns.length;
    }

    static double[] toArr(CardValueModel.Config c) {
        return new double[]{c.potentialWeight(), c.completionHorizon(), c.sashimiDiscount(), c.makiHorizon(), c.makiIconValue(),
                c.dumplingWeight(), c.puddingScale(), c.avgNigiri(), c.supplyScale()};
    }

    static CardValueModel.Config fromArr(double[] v) {
        return new CardValueModel.Config(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8]);
    }
}
