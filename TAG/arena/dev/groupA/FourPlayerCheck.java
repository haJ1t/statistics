package groupA;

import core.AbstractPlayer;
import core.Game;
import games.GameType;
import players.PlayerFactory;

import java.lang.reflect.Method;
import java.util.*;

/** Development check: a JSON with a nested fourPlayer block loads, and 4+ player games use it (with the JSON's budget). */
public class FourPlayerCheck {
    public static void main(String[] a) throws Exception {
        SushiMCTSPlayer pl = (SushiMCTSPlayer) PlayerFactory.createPlayer(a[0]);
        Method m = SushiMCTSPlayer.class.getDeclaredMethod("paramsFor", int.class);
        m.setAccessible(true);
        for (int n : new int[]{3, 4, 5}) {
            SushiMCTSParams p = (SushiMCTSParams) m.invoke(pl, n);
            System.out.printf("n=%d K=%.2f fastSim=%b leafRollouts=%d budget=%d wall=%d heuristic=%s fourPlayer=%s%n", n, p.K, p.useFastSim,
                    p.leafRollouts, p.budget, p.wallTimeLimitMS, p.heuristic.getClass().getSimpleName(), p.fourPlayer == null ? "null" : "set");
        }
        // play one quick game with 3 and with 4 players against random players
        for (int n : new int[]{3, 4}) {
            Game game = GameType.SushiGo.createGameInstance(n, 11);
            List<AbstractPlayer> ps = new ArrayList<>();
            SushiMCTSPlayer me = (SushiMCTSPlayer) PlayerFactory.createPlayer(a[0]);
            ps.add(me);
            for (int i = 1; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
            game.reset(ps);
            game.run();
            System.out.printf("n=%d game over, our score %.0f, iterations at last decision %d, getParameters() after game K=%.2f%n", n,
                    game.getGameState().getGameScore(0), me.lastIterations, me.getParameters().K);
        }
    }
}
