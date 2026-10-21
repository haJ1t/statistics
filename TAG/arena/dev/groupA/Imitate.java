package groupA;

import core.AbstractForwardModel;
import core.AbstractGameState;
import core.Game;
import core.actions.AbstractAction;
import games.GameType;
import games.sushigo.SGGameState;
import games.sushigo.actions.ChooseCard;
import games.sushigo.cards.SGCard;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Development tool (not part of the submission): how often the greedy card-value policy picks the same card as the
 * recorded agents (Recorder). The recorded games are replayed from their seeds.
 * <pre>
 * eval: Imitate eval rec.txt labels cfg1;cfg2;...           labels: comma list of agent labels to imitate (or "all")
 * tune: Imitate tune rec.txt labels start rounds [only]     coordinate search on the agreement
 * </pre>
 * Agreement of a decision: 1/k if the agent's card (type, Maki count, chopsticks) is among the k cards with the
 * highest value, else 0. Only decisions at the start of a pick (no chopsticks move in progress) are used.
 */
public class Imitate {

    record Decision(SGGameState state, int player, int key, String label, int round) {}

    static List<Decision> load(String file, Set<String> labels) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(file));
        Map<Integer, List<String[]>> byGame = new TreeMap<>();
        Map<Integer, Long> seeds = new HashMap<>();
        Map<Integer, Integer> nPlayers = new HashMap<>();
        Map<Integer, String> deals = new HashMap<>();
        for (String l : lines) {
            String[] f = l.trim().split(" ");
            if (f[0].equals("DEAL")) {
                deals.put(Integer.parseInt(f[1]), f[3]);
                continue;
            }
            if (f[0].equals("END")) {
                nPlayers.put(Integer.parseInt(f[1]), (f.length - 3) / 2);
                continue;
            }
            int g = Integer.parseInt(f[0]);
            seeds.put(g, Long.parseLong(f[1]));
            byGame.computeIfAbsent(g, x -> new ArrayList<>()).add(f);
        }
        List<Decision> out = new ArrayList<>();
        int bad = 0;
        for (Map.Entry<Integer, List<String[]>> e : byGame.entrySet()) {
            int g = e.getKey();
            if (!nPlayers.containsKey(g) || !deals.containsKey(g)) continue;   // unfinished game
            int n = nPlayers.get(g);
            Game game = GameType.SushiGo.createGameInstance(n, seeds.get(g));
            List<core.AbstractPlayer> ps = new ArrayList<>();
            for (int i = 0; i < n; i++) ps.add(new players.simple.RandomPlayer(new Random(i)));
            game.reset(ps);
            SGGameState s = (SGGameState) game.getGameState();
            setDeal(s, deals.get(g));
            AbstractForwardModel fm = game.getForwardModel();
            for (String[] f : e.getValue()) {
                int p = Integer.parseInt(f[2]);
                String label = f[3];
                boolean inProgress = f[4].equals("1");
                int idx = Integer.parseInt(f[5]);
                boolean chop = f[6].equals("1");
                SGCard card = s.getPlayerHands().get(p).get(idx);
                if (s.getCurrentPlayer() != p || !card.type.name().equals(f[7]) || card.count != Integer.parseInt(f[8])) {
                    bad++;
                    break;
                }
                if (!inProgress && (labels.contains("all") || labels.contains(label)))
                    out.add(new Decision((SGGameState) s.copy(), p, (card.type.ordinal() * 4 + card.count) * 2 + (chop ? 1 : 0),
                            label, s.getRoundCounter()));
                fm.next(s, new ChooseCard(p, idx, chop));
            }
        }
        System.err.println("decisions " + out.size() + " (games with replay mismatch: " + bad + ")");
        return out;
    }

    /** puts the recorded hands and draw pile (Recorder.dealString) into a freshly set-up state */
    static void setDeal(SGGameState s, String deal) {
        String[] parts = deal.split("/", -1);
        List<core.components.Deck<SGCard>> decks = new ArrayList<>(s.getPlayerHands());
        decks.add(Recorder.drawPile(s));
        for (int d = 0; d < decks.size(); d++) {
            core.components.Deck<SGCard> deck = decks.get(d);
            deck.clear();
            String[] cards = parts[d].isEmpty() ? new String[0] : parts[d].split(",");
            for (int i = cards.length - 1; i >= 0; i--) {   // add() puts a card on top (index 0)
                if (cards[i].isEmpty()) continue;
                String[] tc = cards[i].split(":");
                deck.add(new SGCard(SGCard.SGCardType.valueOf(tc[0]), Integer.parseInt(tc[1])));
            }
        }
    }

    /** agreement of the greedy policy with config c on decision d */
    static double agree(Decision d, CardValueModel.Config c) {
        SGGameState s = d.state;
        int p = d.player;
        CardValueModel.View view = new CardValueModel.View(s, c);
        double[] mv = CardValueModel.handValues(view, p);
        List<SGCard> hand = s.getPlayerHands().get(p).getComponents();
        boolean chop = s.getPlayedCardTypes(SGCard.SGCardType.Chopsticks, p).getValue() > 0 && hand.size() > 1;
        double best = -Double.MAX_VALUE;
        // value of every distinct key
        Map<Integer, Double> val = new HashMap<>();
        for (int i = 0; i < hand.size(); i++) {
            SGCard card = hand.get(i);
            int base = (card.type.ordinal() * 4 + card.count) * 2;
            for (int ch = 0; ch <= (chop ? 1 : 0); ch++) {
                double v = mv[i];
                if (ch == 1) {
                    double bestOther = 0.0;
                    for (int j = 0; j < mv.length; j++) if (j != i) bestOther = Math.max(bestOther, mv[j]);
                    v += bestOther - CardValueModel.CHOPSTICKS_VALUE;
                }
                val.put(base + ch, v);
                best = Math.max(best, v);
            }
        }
        int k = 0;
        boolean hit = false;
        for (Map.Entry<Integer, Double> e : val.entrySet()) {
            if (e.getValue() >= best - 1e-9) {
                k++;
                if (e.getKey() == d.key) hit = true;
            }
        }
        return hit ? 1.0 / k : 0.0;
    }

    static ExecutorService pool;

    static double score(List<Decision> ds, CardValueModel.Config c) throws Exception {
        int chunks = 16;
        List<Future<Double>> fs = new ArrayList<>();
        for (int x = 0; x < chunks; x++) {
            final int x0 = x;
            fs.add(pool.submit(() -> {
                double s = 0;
                for (int i = x0; i < ds.size(); i += chunks) s += agree(ds.get(i), c);
                return s;
            }));
        }
        double s = 0;
        for (Future<Double> f : fs) s += f.get();
        return s / ds.size();
    }

    public static void main(String[] a) throws Exception {
        pool = Executors.newFixedThreadPool(Integer.parseInt(System.getProperty("threads", "2")));
        Set<String> labels = new HashSet<>(Arrays.asList(a[2].split(",")));
        List<Decision> ds = load(a[1], labels);
        if (a[0].equals("eval")) {
            for (String cs : a[3].split(";")) {
                CardValueModel.Config c = GreedyTune.parse(cs);
                StringBuilder sb = new StringBuilder(GreedyTune.str(c)).append(String.format(Locale.ROOT, " | all %.4f", score(ds, c)));
                for (int r = 0; r < 3; r++) {
                    final int rr = r;
                    List<Decision> sub = ds.stream().filter(d -> d.round == rr).toList();
                    if (!sub.isEmpty()) sb.append(String.format(Locale.ROOT, " | round %d %.4f", r + 1, score(sub, c)));
                }
                Map<String, List<Decision>> byLabel = new TreeMap<>();
                for (Decision d : ds) byLabel.computeIfAbsent(d.label, x -> new ArrayList<>()).add(d);
                for (Map.Entry<String, List<Decision>> e : byLabel.entrySet())
                    sb.append(String.format(Locale.ROOT, " | %s %.4f (%d)", e.getKey(), score(e.getValue(), c), e.getValue().size()));
                System.out.println(sb);
            }
        } else {
            double[][] grid = {
                    {0.15, 0.25, 0.35, 0.5, 0.7, 1.0},             // alpha
                    {2, 3, 4, 5, 6, 8},                             // T
                    {0.3, 0.5, 0.7, 1.0},                           // beta
                    {0, 1, 2, 3, 4, 6, 8},                          // T_m
                    {0, 0.25, 0.5, 0.75, 1.0, 1.5},                 // kappa
                    {0, 0.5, 1.0, 1.5, 2.0},                        // delta
                    {0.5, 0.75, 1.0, 1.25, 1.5, 2.0},               // pi
                    {1.5, 2, 2.5, 3, 4},                            // nu
                    {0, 1, 2, 3, 4}                                 // gamma
            };
            String[] names = {"alpha", "T", "beta", "T_m", "kappa", "delta", "pi", "nu", "gamma"};
            double[] cur = GreedyTune.toArr(GreedyTune.parse(a[3]));
            int rounds = Integer.parseInt(a[4]);
            Set<Integer> only = null;
            if (a.length > 5) {
                only = new HashSet<>();
                for (String x : a[5].split(",")) only.add(Integer.parseInt(x));
            }
            double curScore = score(ds, GreedyTune.fromArr(cur));
            System.out.printf("start %s agreement %.4f%n", GreedyTune.str(GreedyTune.fromArr(cur)), curScore);
            for (int round = 0; round < rounds; round++) {
                boolean improved = false;
                for (int k = 0; k < grid.length; k++) {
                    if (only != null && !only.contains(k)) continue;
                    double bestV = cur[k], bestS = curScore;
                    for (double v : grid[k]) {
                        if (v == cur[k]) continue;
                        double[] c = cur.clone();
                        c[k] = v;
                        double sc = score(ds, GreedyTune.fromArr(c));
                        if (sc > bestS + 1e-9) { bestS = sc; bestV = v; }
                    }
                    if (bestV != cur[k]) { cur[k] = bestV; curScore = bestS; improved = true; }
                    System.out.printf("round %d after %s: %s agreement %.4f%n", round, names[k], GreedyTune.str(GreedyTune.fromArr(cur)), curScore);
                }
                if (!improved) break;
            }
            System.out.println("BEST " + GreedyTune.str(GreedyTune.fromArr(cur)) + " agreement " + curScore);
        }
        pool.shutdown();
    }
}
