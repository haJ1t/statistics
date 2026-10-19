package arena;

import core.AbstractPlayer;
import core.Game;
import games.GameType;
import groupA.SushiMCTSPlayer;
import players.PlayerFactory;

import java.util.*;

/**
 * Development probe: average MCTS iterations per decision of a groupA agent (seat 0) in real games.
 * usage: java arena.Probe players=3 games=4 focus=a.json opps=b.json
 */
public class Probe {
    public static void main(String[] args) {
        Map<String, String> a = new HashMap<>();
        for (String s : args) {
            int i = s.indexOf('=');
            if (i > 0) a.put(s.substring(0, i), s.substring(i + 1));
        }
        int n = Integer.parseInt(a.getOrDefault("players", "3"));
        int games = Integer.parseInt(a.getOrDefault("games", "4"));
        String focus = a.get("focus");
        List<String> opps = Arrays.asList(a.get("opps").split(","));
        long sumIt = 0, nDec = 0, sumDet = 0;
        for (int g = 0; g < games; g++) {
            List<AbstractPlayer> players = new ArrayList<>();
            SushiMCTSPlayer me = (SushiMCTSPlayer) PlayerFactory.createPlayer(focus);
            players.add(me);
            for (int s = 1; s < n; s++) players.add(PlayerFactory.createPlayer(opps.get((s - 1) % opps.size())));
            Game game = GameType.SushiGo.createGameInstance(n, 1000 + g);
            game.reset(players);
            while (game.getGameState().isNotTerminal()) {
                int before = me.lastIterations;
                me.lastIterations = -1;
                game.oneAction();
                if (me.lastIterations >= 0) {
                    sumIt += me.lastIterations;
                    sumDet += me.lastDeterminisations;
                    nDec++;
                } else me.lastIterations = before;
            }
        }
        System.out.printf("PROBE %s: decisions=%d  iterations/decision=%.0f  determinisations/decision=%.2f%n",
                focus, nDec, sumIt / (double) nDec, sumDet / (double) nDec);
    }
}
