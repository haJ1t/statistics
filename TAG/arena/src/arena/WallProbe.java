package arena;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import players.PlayerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Development probe (NOT part of the submission): wall-clock time of every decision of the focus agent.
 * The focus agent is wrapped in a pass-through player that times its _getAction (the part TAG's player timer covers).
 * usage: java arena.WallProbe players=3 games=6 focus=a.json opps=b.json,c.json seed=500 limit=1000
 */
public class WallProbe {

    /** Forwards every call to the wrapped agent and records the wall-clock time of each decision. */
    static class Timed extends AbstractPlayer {
        final AbstractPlayer inner;
        final List<Double> ms, cpu;
        static final ThreadMXBean BEAN = ManagementFactory.getThreadMXBean();

        Timed(AbstractPlayer inner, List<Double> ms, List<Double> cpu) {
            super(inner.getParameters(), inner.toString());
            this.inner = inner;
            this.ms = ms;
            this.cpu = cpu;
        }

        @Override
        public void setForwardModel(AbstractForwardModel model) {
            super.setForwardModel(model);
            inner.setForwardModel(model);
        }

        @Override
        public void initializePlayer(AbstractGameState state) {
            inner.setPlayerID(getPlayerID());
            inner.initializePlayer(state);
        }

        @Override
        public void finalizePlayer(AbstractGameState state) {
            inner.finalizePlayer(state);
        }

        @Override
        public void registerUpdatedObservation(AbstractGameState state) {
            inner.registerUpdatedObservation(state);
        }

        @Override
        public AbstractAction _getAction(AbstractGameState state, List<AbstractAction> actions) {
            long t = System.nanoTime(), c = BEAN.getCurrentThreadCpuTime();
            AbstractAction a = inner._getAction(state, actions);
            ms.add((System.nanoTime() - t) / 1e6);
            cpu.add((BEAN.getCurrentThreadCpuTime() - c) / 1e6);
            return a;
        }

        @Override
        public AbstractPlayer copy() {
            return new Timed(inner.copy(), ms, cpu);
        }
    }

    public static void main(String[] args) {
        Map<String, String> a = new HashMap<>();
        for (String s : args) {
            int i = s.indexOf('=');
            if (i > 0) a.put(s.substring(0, i), s.substring(i + 1));
        }
        int n = Integer.parseInt(a.getOrDefault("players", "3"));
        int games = Integer.parseInt(a.getOrDefault("games", "6"));
        long seed = Long.parseLong(a.getOrDefault("seed", "500"));
        double limit = Double.parseDouble(a.getOrDefault("limit", "1000"));
        String focus = a.get("focus");
        List<String> opps = Arrays.asList(a.get("opps").split(","));
        List<Double> ms = new ArrayList<>(), cpu = new ArrayList<>();
        for (int g = 0; g < games; g++) {
            int focusSeat = g % n;
            List<AbstractPlayer> players = new ArrayList<>();
            int oi = 0;
            for (int s = 0; s < n; s++)
                players.add(s == focusSeat ? new Timed(PlayerFactory.createPlayer(focus), ms, cpu)
                        : PlayerFactory.createPlayer(opps.get(oi++ % opps.size())));
            Game game = GameType.SushiGo.createGameInstance(n, seed + g);
            game.reset(players);
            game.run();
            System.err.printf("  game %d done, %d focus decisions so far%n", g + 1, ms.size());
        }
        report("WALL", focus, ms, limit);
        report("CPU ", focus, cpu, limit);
    }

    static void report(String what, String focus, List<Double> values, double limit) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        double sum = 0;
        for (double v : sorted) sum += v;
        long over = sorted.stream().filter(v -> v > limit).count();
        System.out.printf(Locale.ROOT, "PROBE %s %s: decisions=%d mean=%.1f p50=%.1f p95=%.1f p99=%.1f max=%.1f ms, over %.0f ms: %d%n",
                what, focus, sorted.size(), sum / sorted.size(), q(sorted, 0.50), q(sorted, 0.95), q(sorted, 0.99),
                sorted.get(sorted.size() - 1), limit, over);
    }

    static double q(List<Double> sorted, double p) {
        return sorted.get(Math.min(sorted.size() - 1, (int) Math.floor(p * sorted.size())));
    }
}
