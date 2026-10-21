package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;
import players.PlayerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Development tool (not part of the submission): plays games and records every decision (to replay the games and
 * compare the card-value policy with the moves of the agents).
 * usage: Recorder n games threads seed out.txt agent1.json,agent2.json,...   (seat s of game g: agent (s + g) mod k)
 * Line per decision: game seed player label actionInProgress cardIdx useChopsticks cardType cardCount handSize round
 */
public class Recorder {
    public static void main(String[] a) throws Exception {
        int n = Integer.parseInt(a[0]), games = Integer.parseInt(a[1]), threads = Integer.parseInt(a[2]);
        long seed = Long.parseLong(a[3]);
        String out = a[4];
        String[] agents = a[5].split(",");
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<List<String>>> fs = new ArrayList<>();
        for (int g = 0; g < games; g++) {
            final int game = g;
            fs.add(pool.submit(() -> play(n, game, seed, agents)));
        }
        try (PrintWriter w = new PrintWriter(new FileWriter(out))) {
            int done = 0;
            for (Future<List<String>> f : fs) {
                for (String line : f.get()) w.println(line);
                w.flush();
                if (++done % 10 == 0) System.err.println("recorded " + done + "/" + games);
            }
        }
        pool.shutdown();
    }

    /** all hands and the draw pile, each card as type:count, in index order (TAG's deck order is JVM dependent) */
    static String dealString(SGGameState s) {
        StringBuilder sb = new StringBuilder();
        for (int p = 0; p < s.getNPlayers(); p++) {
            sb.append(p == 0 ? "" : "/");
            for (SGCard c : s.getPlayerHands().get(p).getComponents()) sb.append(c.type.name()).append(':').append(c.count).append(',');
        }
        sb.append("/");
        for (SGCard c : drawPile(s).getComponents()) sb.append(c.type.name()).append(':').append(c.count).append(',');
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    static core.components.Deck<SGCard> drawPile(SGGameState s) {
        try {
            java.lang.reflect.Field f = SGGameState.class.getDeclaredField("drawPile");
            f.setAccessible(true);
            return (core.components.Deck<SGCard>) f.get(s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String label(String spec) {
        String s = spec.replace('\\', '/');
        s = s.substring(s.lastIndexOf('/') + 1);
        return s.endsWith(".json") ? s.substring(0, s.length() - 5) : s;
    }

    static List<String> play(int n, int g, long seed, String[] agents) {
        List<AbstractPlayer> players = new ArrayList<>();
        String[] labels = new String[n];
        for (int s = 0; s < n; s++) {
            String spec = agents[(s + g) % agents.length];
            players.add(PlayerFactory.createPlayer(spec));
            labels[s] = label(spec);
        }
        Game game = GameType.SushiGo.createGameInstance(n, seed + g);
        game.reset(players);
        AbstractGameState s = game.getGameState();
        AbstractForwardModel fm = game.getForwardModel();
        List<String> lines = new ArrayList<>();
        lines.add("DEAL " + g + " " + n + " " + dealString((SGGameState) s));
        while (s.isNotTerminal()) {
            int p = s.getCurrentPlayer();
            AbstractGameState obs = s.copy(p);
            List<AbstractAction> acts = fm.computeAvailableActions(obs, players.get(p).getParameters().actionSpace);
            AbstractAction act = players.get(p).getAction(obs, acts);
            ChooseCard cc = (ChooseCard) act;
            SGGameState sg = (SGGameState) s;
            SGCard card = sg.getPlayerHands().get(p).get(cc.cardIdx);
            lines.add(String.format(Locale.ROOT, "%d %d %d %s %d %d %d %s %d %d %d", g, seed + g, p, labels[p],
                    sg.isActionInProgress() ? 1 : 0, cc.cardIdx, cc.useChopsticks ? 1 : 0, card.type.name(), card.count,
                    sg.getPlayerHands().get(p).getSize(), sg.getRoundCounter()));
            fm.next(s, act);
        }
        StringBuilder sb = new StringBuilder("END " + g + " " + (seed + g));
        for (int p = 0; p < n; p++) sb.append(' ').append(labels[p]).append(' ').append(s.getGameScore(p));
        lines.add(sb.toString());
        return lines;
    }
}
