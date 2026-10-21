package groupA;

import core.AbstractForwardModel;
import core.Game;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;

import java.nio.file.*;
import java.util.*;

/**
 * Development check of MODIFICATION Q offline: replays recorded games; at every turn-start decision of agent "label"
 * the opponent model observes the state (as in play), and every pick of an opponent whose hand label knows is scored
 * by the softmax policy P(c) ~ exp((v(c) + b) / T) with the learned biases b and with b = 0 (first card only).
 * usage: QPredict rec.txt label T prior [policyT ...]
 */
public class QPredict {
    public static void main(String[] a) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(a[0]));
        String label = a[1];
        double T = Double.parseDouble(a[2]), prior = Double.parseDouble(a[3]);
        double[] pts = a.length > 4 ? Arrays.stream(Arrays.copyOfRange(a, 4, a.length)).mapToDouble(Double::parseDouble).toArray() : new double[]{1.0, 1.5, 2.0};
        Map<Integer, String> deals = new HashMap<>();
        Map<Integer, Integer> nPl = new HashMap<>();
        Map<Integer, List<String[]>> byGame = new TreeMap<>();
        for (String l : lines) {
            String[] f = l.trim().split(" ");
            if (f[0].equals("DEAL")) { deals.put(Integer.parseInt(f[1]), f[3]); nPl.put(Integer.parseInt(f[1]), Integer.parseInt(f[2])); continue; }
            if (f[0].equals("END")) continue;
            byGame.computeIfAbsent(Integer.parseInt(f[0]), x -> new ArrayList<>()).add(f);
        }
        CardValueModel.Config cfg = new CardValueModel.Config(0.5, 6, 0.5, 3, 0.25, 1.5, 1.25, 3, 3);   // X0
        Map<String, double[]> acc = new TreeMap<>();   // opp label -> {n, ll0[T], llQ[T]}
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
            int me = -1;
            for (String[] f : gl) if (f[3].equals(label)) { me = Integer.parseInt(f[2]); break; }
            if (me < 0) continue;
            OpponentModel om = new OpponentModel(n, me, T, prior);
            for (String[] f : gl) {
                int p = Integer.parseInt(f[2]);
                if (p == me && f[4].equals("0")) {
                    SGGameState obs = (SGGameState) s.copy(me);
                    om.observe(obs, cfg);
                    // score this turn's picks of opponents with known hands
                    CardValueModel.View view = new CardValueModel.View(obs, cfg);
                    for (String[] h : gl) {
                        if (!h[4].equals("0") || !h[9].equals(f[9]) || !h[10].equals(f[10]) || h[2].equals(f[2]) || h[6].equals("1")) continue;
                        int q = Integer.parseInt(h[2]);
                        if (!obs.isHandKnown(me, q)) continue;
                        List<SGCard> hand = obs.getPlayerHands().get(q).getComponents();
                        double[] mv = CardValueModel.handValues(view, q);
                        int chosen = -1;
                        for (int c = 0; c < hand.size(); c++)
                            if (hand.get(c).type.name().equals(h[7]) && hand.get(c).count == Integer.parseInt(h[8])) chosen = c;
                        if (chosen < 0 || hand.size() < 2) continue;
                        double[] r = acc.computeIfAbsent(h[3], k -> new double[1 + 2 * pts.length]);
                        r[0]++;
                        for (int ti = 0; ti < pts.length; ti++)
                            for (int withQ = 0; withQ < 2; withQ++) {
                                double[] u = new double[mv.length];
                                double max = -Double.MAX_VALUE;
                                for (int c = 0; c < mv.length; c++) {
                                    u[c] = (mv[c] + (withQ == 1 ? om.bias(q, hand.get(c).type) : 0.0)) / pts[ti];
                                    max = Math.max(max, u[c]);
                                }
                                double z = 0;
                                for (double x : u) z += Math.exp(x - max);
                                r[1 + 2 * ti + withQ] += u[chosen] - max - Math.log(z);
                            }
                    }
                }
                fm.next(s, new ChooseCard(p, Integer.parseInt(f[5]), f[6].equals("1")));
            }
        }
        for (Map.Entry<String, double[]> e : acc.entrySet()) {
            double[] r = e.getValue();
            StringBuilder sb = new StringBuilder(String.format(Locale.ROOT, "%-10s picks %4.0f:", e.getKey(), r[0]));
            for (int ti = 0; ti < pts.length; ti++)
                sb.append(String.format(Locale.ROOT, "  T%.1f no-Q %.3f Q %.3f", pts[ti], r[1 + 2 * ti] / r[0], r[2 + 2 * ti] / r[0]));
            System.out.println(sb);
        }
    }
}
