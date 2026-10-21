package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;

import java.util.*;

/**
 * Greedy rollout policies against each other (no search): seat 0 uses heuristic A, the others heuristic B.
 * usage: GreedyDuel nPlayers games eps alphaA TA betaA alphaB TB betaB
 */
public class GreedyDuel {
    public static void main(String[] args) {
        int n = Integer.parseInt(args[0]), games = Integer.parseInt(args[1]);
        double eps = Double.parseDouble(args[2]);
        double aA = Double.parseDouble(args[3]), tA = Double.parseDouble(args[4]), betaA = Double.parseDouble(args[5]);
        double aB = Double.parseDouble(args[6]), tB = Double.parseDouble(args[7]), betaB = Double.parseDouble(args[8]);
        SushiGoHeuristic hA = new SushiGoHeuristic(15, 0.7, aA, tA, betaA), hB = new SushiGoHeuristic(15, 0.7, aB, tB, betaB);
        Random r = new Random(5);
        double winsA = 0, scoreA = 0, scoreB = 0, sashA = 0, sashB = 0;
        for (int g = 0; g < games; g++) {
            Game game = GameType.SushiGo.createGameInstance(n, 9000 + g);
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
            game.reset(ps);
            SGGameState s = (SGGameState) game.getGameState();
            AbstractForwardModel fm = game.getForwardModel();
            int seatA = g % n;
            while (s.isNotTerminal()) {
                List<AbstractAction> acts = fm.computeAvailableActions(s);
                int mover = ((ChooseCard) acts.get(0)).playerId;
                AbstractAction choice;
                if (r.nextDouble() < eps) choice = acts.get(r.nextInt(acts.size()));
                else {
                    SushiGoHeuristic h = mover == seatA ? hA : hB;
                    CardValueModel.View view = h.view(s);
                    double best = -1e9; choice = null;
                    for (AbstractAction a : acts) {
                        double v = CardValueModel.actionValue(view, a) + 1e-6 * r.nextDouble();
                        if (v > best) { best = v; choice = a; }
                    }
                }
                fm.next(s, choice);
            }
            double best = -1e9; int nBest = 0;
            for (int p = 0; p < n; p++) best = Math.max(best, s.getGameScore(p));
            for (int p = 0; p < n; p++) if (s.getGameScore(p) == best) nBest++;
            if (s.getGameScore(seatA) == best) winsA += 1.0 / nBest;
            scoreA += s.getGameScore(seatA);
            for (int p = 0; p < n; p++) if (p != seatA) { scoreB += s.getGameScore(p) / (n - 1); sashB += s.getPointsPerCardType()[p].get(SGCard.SGCardType.Sashimi).getValue() / (double) (n - 1); }
            sashA += s.getPointsPerCardType()[seatA].get(SGCard.SGCardType.Sashimi).getValue();
        }
        System.out.printf("n=%d eps=%.2f A(a=%.2f T=%.0f b=%.2f) vs B(a=%.2f T=%.0f b=%.2f): A wins %.1f%% (chance %.1f%%), score A %.2f vs B %.2f, sashimi pts A %.2f B %.2f%n",
                n, eps, aA, tA, betaA, aB, tB, betaB, 100 * winsA / games, 100.0 / n, scoreA / games, scoreB / games, sashA / games, sashB / games);
    }
}
