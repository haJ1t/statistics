package groupA;

import core.AbstractPlayer;
import core.Game;
import games.GameType;
import games.sushigo.cards.SGCard.SGCardType;
import players.PlayerFactory;

import java.util.*;

/**
 * Development check of MODIFICATION Q: plays games with our agent (seat 0, opponent model on) against the given
 * opponents and prints, per opponent label, the mean learned bias per card type and the observations per game.
 * usage: OpponentModelCheck ours.json games nPlayers opp1.json,opp2.json
 */
public class OpponentModelCheck {
    public static void main(String[] a) throws Exception {
        String ours = a[0];
        int games = Integer.parseInt(a[1]), n = Integer.parseInt(a[2]);
        String[] opps = a[3].split(",");
        Map<String, double[]> sum = new TreeMap<>();
        Map<String, Integer> cnt = new TreeMap<>();
        long obs = 0;
        for (int g = 0; g < games; g++) {
            Game game = GameType.SushiGo.createGameInstance(n, 100 + g);
            List<AbstractPlayer> ps = new ArrayList<>();
            SushiMCTSPlayer me = (SushiMCTSPlayer) PlayerFactory.createPlayer(ours);
            ps.add(me);
            String[] labels = new String[n];
            for (int i = 1; i < n; i++) {
                String spec = opps[(i - 1 + g) % opps.length];
                ps.add(PlayerFactory.createPlayer(spec));
                labels[i] = spec.substring(spec.lastIndexOf('/') + 1);
            }
            game.reset(ps);
            game.run();
            OpponentModel om = me.opponentModel;
            if (om == null) { System.out.println("no model"); return; }
            obs += om.observations;
            for (int q = 1; q < n; q++) {
                double[] b = om.biasOf(q);
                sum.computeIfAbsent(labels[q], k -> new double[b.length]);
                for (int t = 0; t < b.length; t++) sum.get(labels[q])[t] += b[t];
                cnt.merge(labels[q], 1, Integer::sum);
            }
        }
        System.out.printf("games %d, observations per game %.1f%n", games, obs / (double) games);
        StringBuilder h = new StringBuilder(String.format("%-16s", "opponent"));
        for (SGCardType t : SGCardType.values()) h.append(String.format("%9s", t.name().length() > 8 ? t.name().substring(0, 8) : t.name()));
        System.out.println(h);
        for (String k : sum.keySet()) {
            StringBuilder r = new StringBuilder(String.format("%-16s", k));
            for (double v : sum.get(k)) r.append(String.format(Locale.ROOT, "%9.2f", v / cnt.get(k)));
            System.out.println(r);
        }
    }
}
