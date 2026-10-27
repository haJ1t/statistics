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
 * Development tool (v5, not part of the submission): search-free duel of epsilon-greedy card-value policies with and
 * without MODIFICATION N (denial). A player is "config@lambda" (config as in GreedyTune).
 * <pre>
 * DenialDuel n1,n2 games eps threads cand1;cand2;... pool1;pool2;...
 * </pre>
 * The candidate sits in seat g % n, the pool fills the other seats (rotated); game g uses deal 9000 + g.
 */
public class DenialDuel {

    record P(CardValueModel.Config cfg, double lambda) {
        static P parse(String s) {
            String[] f = s.split("@");
            return new P(GreedyTune.parse(f[0]), f.length > 1 ? Double.parseDouble(f[1]) : 0.0);
        }
    }

    static AbstractAction choose(SGGameState s, List<AbstractAction> acts, P pl, Random r) {
        int mover = ((ChooseCard) acts.get(0)).playerId;
        CardValueModel.View view = new CardValueModel.View(s, pl.cfg);
        double[] mv = CardValueModel.handValues(view, mover);
        if (pl.lambda > 0) CardValueModel.addDenial(view, mover, mv, pl.lambda);
        AbstractAction choice = null;
        double best = -1e9;
        for (AbstractAction a : acts) {
            ChooseCard cc = (ChooseCard) a;
            double v = mv[cc.cardIdx];
            if (cc.useChopsticks) {
                double other = 0;
                for (int i = 0; i < mv.length; i++) if (i != cc.cardIdx) other = Math.max(other, mv[i]);
                v += other - CardValueModel.CHOPSTICKS_VALUE;
            }
            v += 1e-6 * r.nextDouble();
            if (v > best) { best = v; choice = a; }
        }
        return choice;
    }

    static double[] play(int n, int g, double eps, P cand, List<P> pool) {
        Game game = GameType.SushiGo.createGameInstance(n, 9000 + g);
        List<core.AbstractPlayer> ps = new ArrayList<>();
        for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
        game.reset(ps);
        SGGameState s = (SGGameState) game.getGameState();
        AbstractForwardModel fm = game.getForwardModel();
        int seat = g % n;
        P[] pl = new P[n];
        int oi = 0, shift = g / n;
        for (int p = 0; p < n; p++) pl[p] = p == seat ? cand : pool.get((oi++ + shift) % pool.size());
        Random r = new Random(77L * g + 1);
        while (s.isNotTerminal()) {
            List<AbstractAction> acts = fm.computeAvailableActions(s);
            int mover = ((ChooseCard) acts.get(0)).playerId;
            double u = r.nextDouble();
            int ri = r.nextInt(acts.size());
            AbstractAction choice = u < eps ? acts.get(ri) : choose(s, acts, pl[mover], r);
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

    public static void main(String[] a) throws Exception {
        int[] ns = Arrays.stream(a[0].split(",")).mapToInt(Integer::parseInt).toArray();
        int games = Integer.parseInt(a[1]);
        double eps = Double.parseDouble(a[2]);
        ExecutorService pool = Executors.newFixedThreadPool(Integer.parseInt(a[3]));
        List<P> opp = new ArrayList<>();
        for (String s : a[5].split(";")) opp.add(P.parse(s));
        for (String cs : a[4].split(";")) {
            P c = P.parse(cs);
            StringBuilder sb = new StringBuilder(cs);
            for (int n : ns) {
                List<Future<double[]>> fs = new ArrayList<>();
                int chunks = 64;
                for (int ch = 0; ch < chunks; ch++) {
                    final int c0 = ch;
                    fs.add(pool.submit(() -> {
                        double w = 0, w2 = 0, m = 0;
                        for (int g = c0; g < games; g += chunks) {
                            double[] res = play(n, g, eps, c, opp);
                            w += res[0]; w2 += res[0] * res[0]; m += res[1];
                        }
                        return new double[]{w, w2, m};
                    }));
                }
                double w = 0, w2 = 0, m = 0;
                for (Future<double[]> f : fs) { double[] x = f.get(); w += x[0]; w2 += x[1]; m += x[2]; }
                double p = w / games, se = Math.sqrt(Math.max(0, w2 / games - p * p) / games);
                sb.append(String.format(Locale.ROOT, " | n=%d win %.2f%% +- %.2f margin %+.2f", n, 100 * p, 196 * se, m / games));
            }
            System.out.println(sb);
        }
        pool.shutdown();
    }
}
