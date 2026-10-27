package groupA;

import core.AbstractGameState;
import core.interfaces.IStateHeuristic;
import players.PlayerParameters;

import java.util.Arrays;

/**
 * Parameters of the SushiMCTS agent.
 * <p>
 * Copied from players.basicMCTS.BasicMCTSParams (the permitted base agent) and extended with
 * the switches used in the ablation study. Every modification can be turned on/off from JSON,
 * so all variants run the SAME code base.
 */
public class SushiMCTSParams extends PlayerParameters {

    // ---- Original BasicMCTS parameters (unchanged) ----
    public double K = Math.sqrt(2);
    public int rolloutLength = 10;
    public int maxTreeDepth = 100;
    public double epsilon = 1e-6;
    public IStateHeuristic heuristic = AbstractGameState::getHeuristicScore;

    // ---- Ablation switches ----
    public boolean useProgressiveBias = false;      // Modification A
    public boolean useInformedRollout = false;      // Modification B
    public boolean useDeterminisation = false;      // Modification D
    public boolean adaptiveDeterminisation = true;  // D: number of determinisations depends on hidden hands
    public boolean useActionPruning = false;        // Modification P: one branch per distinct card
    public boolean useOpponentModel = false;        // Modification O: opponents follow the card-value policy in the tree
    public boolean useMaxN = false;                 // Modification M: max^n backups, every player maximises its own value
    public boolean rolloutToRoundEnd = false;       // Modification R: rollouts end when the current round is scored
    public boolean useDecoupledUCT = false;         // Modification S: simultaneous moves - decoupled UCT within a turn
    public boolean useInformationSets = false;      // Modification I: one open-loop tree, hidden cards re-sampled every iteration
    public int treeThreshold = 0;                   // I: below this many visits a node plays the rollout policy, not UCB
    public boolean ownCardTree = false;             // I: children keyed by our own card only (not the joint choice)
    public boolean useMAST = false;                 // Modification G: MAST card values (learned in the search) steer the rollouts
    public double mastWeight = 10.0;                // G: weight of the MAST value (points of card value per unit of H)
    public double mastPrior = 5.0;                  // G: pseudo-observations that shrink a card's MAST value to the mean
    public double terminalScale = 0.0;              // Modification V: finished games valued by the gap to the winner (0 = off)
    public boolean useMultiTree = false;            // Modification U (with I): one tree per player, each branching on its own cards
    public double maxChildShare = 0.0;              // I: final card = best mean among cards with >= this share of the top visits (0 = most visits)
    public double sigmaRoundDecay = 0.0;            // leaf values sharper when fewer rounds are left: sigma * (R / 2)^d, R = rounds still to play (0 = off)
    public boolean opponentScoreValue = false;      // max^n opponent model: every other player plays for its own score (our value unchanged)
    public double opponentRivalWeight = 1.0;        // opponentScoreValue: weight of the others' mean score in an opponent's value (0 = own score only)
    public boolean stopAtRoundEnd = false;          // I/U: a round finished inside the tree ends the iteration there (no rollout of the next round)
    public boolean useFastSim = false;              // Modification F (with I, R and SushiGoHeuristic): iterations on a SushiSim instead of TAG's game state
    public double opponentK = -1.0;                 // I/U: UCB exploration constant of the opponents' card statistics (< 0 = K)
    public int leafRollouts = 1;                    // Modification L (with F): rollouts from every leaf, their values averaged
    public int rolloutRounds = 1;                   // R: a rollout ends when this many rounds have been scored (1 = the leaf's round)
    public double rolloutTemperature = 0.0;         // B with F: the informed rollout move is drawn by softmax(value / T) (0 = best card)
    public double opponentRolloutEpsilon = -1.0;    // B with F: rolloutEpsilon of the other players' rollout moves (< 0 = rolloutEpsilon)
    public double opponentRolloutTemperature = -1.0; // B with F: rolloutTemperature of the other players' rollout moves (< 0 = rolloutTemperature)
    public int ensembleTrees = 1;                   // Modification E (with I): the budget is split between this many independent trees, root visits summed
    public int opponentTopK = 0;                    // Modification K (with I): in the tree an opponent only considers its K best cards by the card-value model (0 = all)
    public double opponentWidening = 0.0;           // K, progressive widening: at a node with n visits an opponent considers its ceil((n + 1)^a) best cards (0 = off)
    public boolean useOpponentBias = false;         // Modification Q: per-opponent card-type biases learned during the game steer its simulated picks
    public double opponentBiasTemperature = 2.0;    // Q: T of the choice model P(c) ~ exp((value + bias) / T), in points
    public double opponentBiasPrior = 3.0;          // Q: standard deviation s of the Gaussian prior of every bias, in points
    public double rolloutDenial = 0.0;              // Modification N (v5): weight of denying the next player its best card in the rollout policy (0 = off)
    // Search parameters for games with four or more players (null = these parameters). The budget fields (budgetType,
    // budget, breakMS, wallTimeLimitMS) always come from these parameters, so one JSON keeps one time limit.
    public SushiMCTSParams fourPlayer = null;

    // ---- Hyper-parameters of the modifications ----
    public double biasWeight = 1.0;        // W in the progressive-bias term W * b(a) / (n + 1)
    public double priorScale = 5.0;        // b(a) = tanh(mv(a) / priorScale)
    public double rolloutEpsilon = 0.2;    // probability of a uniformly random rollout move
    public int nDeterminisations = 4;      // M_max
    public double opponentEpsilon = 0.2;   // O: probability that a modelled opponent plays a random card

    // ---- Safety ----
    // Hard wall-clock limit per decision in ms (0 = off). The time budget above is CPU time (ElapsedCpuTimer);
    // on a busy machine a decision can take longer in real time, so the search also stops at this limit.
    public int wallTimeLimitMS = 0;

    public SushiMCTSParams() {
        addTunableParameter("K", Math.sqrt(2), Arrays.asList(0.0, 0.1, 1.0, Math.sqrt(2), 3.0, 10.0));
        addTunableParameter("rolloutLength", 10, Arrays.asList(0, 3, 10, 30, 100));
        addTunableParameter("maxTreeDepth", 100, Arrays.asList(1, 3, 10, 30, 100));
        addTunableParameter("epsilon", 1e-6);
        addTunableParameter("heuristic", (IStateHeuristic) AbstractGameState::getHeuristicScore);

        addTunableParameter("useProgressiveBias", false, Arrays.asList(false, true));
        addTunableParameter("useInformedRollout", false, Arrays.asList(false, true));
        addTunableParameter("useDeterminisation", false, Arrays.asList(false, true));
        addTunableParameter("adaptiveDeterminisation", true, Arrays.asList(false, true));
        addTunableParameter("biasWeight", 1.0, Arrays.asList(0.25, 0.5, 1.0, 2.0, 4.0));
        addTunableParameter("priorScale", 5.0, Arrays.asList(2.5, 5.0, 10.0));
        addTunableParameter("rolloutEpsilon", 0.2, Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.5));
        addTunableParameter("nDeterminisations", 4, Arrays.asList(1, 2, 4, 8));
        addTunableParameter("useActionPruning", false, Arrays.asList(false, true));
        addTunableParameter("useOpponentModel", false, Arrays.asList(false, true));
        addTunableParameter("opponentEpsilon", 0.2, Arrays.asList(0.0, 0.1, 0.2, 0.3, 0.5));
        addTunableParameter("useMaxN", false, Arrays.asList(false, true));
        addTunableParameter("rolloutToRoundEnd", false, Arrays.asList(false, true));
        addTunableParameter("useDecoupledUCT", false, Arrays.asList(false, true));
        addTunableParameter("useInformationSets", false, Arrays.asList(false, true));
        addTunableParameter("treeThreshold", 0, Arrays.asList(0, 2, 4, 8, 16));
        addTunableParameter("ownCardTree", false, Arrays.asList(false, true));
        addTunableParameter("useMAST", false, Arrays.asList(false, true));
        addTunableParameter("mastWeight", 10.0, Arrays.asList(2.5, 5.0, 10.0, 20.0, 40.0));
        addTunableParameter("mastPrior", 5.0, Arrays.asList(1.0, 5.0, 20.0));
        addTunableParameter("terminalScale", 0.0, Arrays.asList(0.0, 1.0, 3.0, 5.0, 15.0));
        addTunableParameter("useMultiTree", false, Arrays.asList(false, true));
        addTunableParameter("maxChildShare", 0.0, Arrays.asList(0.0, 0.1, 0.25, 0.5));
        addTunableParameter("sigmaRoundDecay", 0.0, Arrays.asList(0.0, 0.5, 1.0));
        addTunableParameter("opponentScoreValue", false, Arrays.asList(false, true));
        addTunableParameter("opponentRivalWeight", 1.0, Arrays.asList(0.0, 0.5, 1.0));
        addTunableParameter("stopAtRoundEnd", false, Arrays.asList(false, true));
        addTunableParameter("useFastSim", false, Arrays.asList(false, true));
        addTunableParameter("opponentK", -1.0, Arrays.asList(-1.0, 0.5, 1.0, 2.0, 3.0));
        addTunableParameter("leafRollouts", 1, Arrays.asList(1, 2, 4, 8));
        addTunableParameter("rolloutRounds", 1, Arrays.asList(1, 2, 3));
        addTunableParameter("rolloutTemperature", 0.0, Arrays.asList(0.0, 0.5, 1.0, 2.0, 4.0));
        addTunableParameter("opponentRolloutEpsilon", -1.0, Arrays.asList(-1.0, 0.0, 0.2, 0.35, 0.5));
        addTunableParameter("opponentRolloutTemperature", -1.0, Arrays.asList(-1.0, 0.0, 1.0, 1.5, 2.0));
        addTunableParameter("ensembleTrees", 1, Arrays.asList(1, 2, 3, 5, 8));
        addTunableParameter("opponentTopK", 0, Arrays.asList(0, 2, 3, 4));
        addTunableParameter("opponentWidening", 0.0, Arrays.asList(0.0, 0.2, 0.25, 0.33, 0.5));
        addTunableParameter("useOpponentBias", false, Arrays.asList(false, true));
        addTunableParameter("opponentBiasTemperature", 2.0, Arrays.asList(1.0, 2.0, 4.0));
        addTunableParameter("opponentBiasPrior", 3.0, Arrays.asList(1.0, 3.0, 10.0));
        addTunableParameter("rolloutDenial", 0.0, Arrays.asList(0.0, 0.25, 0.5, 1.0));
        addTunableParameter("fourPlayer", SushiMCTSParams.class);
        addTunableParameter("wallTimeLimitMS", 0);
    }

    @Override
    public void _reset() {
        super._reset();
        K = (double) getParameterValue("K");
        rolloutLength = (int) getParameterValue("rolloutLength");
        maxTreeDepth = (int) getParameterValue("maxTreeDepth");
        epsilon = (double) getParameterValue("epsilon");
        heuristic = (IStateHeuristic) getParameterValue("heuristic");

        useProgressiveBias = (boolean) getParameterValue("useProgressiveBias");
        useInformedRollout = (boolean) getParameterValue("useInformedRollout");
        useDeterminisation = (boolean) getParameterValue("useDeterminisation");
        adaptiveDeterminisation = (boolean) getParameterValue("adaptiveDeterminisation");
        biasWeight = (double) getParameterValue("biasWeight");
        priorScale = (double) getParameterValue("priorScale");
        rolloutEpsilon = (double) getParameterValue("rolloutEpsilon");
        nDeterminisations = (int) getParameterValue("nDeterminisations");
        useActionPruning = (boolean) getParameterValue("useActionPruning");
        useOpponentModel = (boolean) getParameterValue("useOpponentModel");
        opponentEpsilon = (double) getParameterValue("opponentEpsilon");
        useMaxN = (boolean) getParameterValue("useMaxN");
        rolloutToRoundEnd = (boolean) getParameterValue("rolloutToRoundEnd");
        useDecoupledUCT = (boolean) getParameterValue("useDecoupledUCT");
        useInformationSets = (boolean) getParameterValue("useInformationSets");
        treeThreshold = (int) getParameterValue("treeThreshold");
        ownCardTree = (boolean) getParameterValue("ownCardTree");
        useMAST = (boolean) getParameterValue("useMAST");
        mastWeight = (double) getParameterValue("mastWeight");
        mastPrior = (double) getParameterValue("mastPrior");
        terminalScale = (double) getParameterValue("terminalScale");
        useMultiTree = (boolean) getParameterValue("useMultiTree");
        maxChildShare = (double) getParameterValue("maxChildShare");
        sigmaRoundDecay = (double) getParameterValue("sigmaRoundDecay");
        opponentScoreValue = (boolean) getParameterValue("opponentScoreValue");
        opponentRivalWeight = (double) getParameterValue("opponentRivalWeight");
        stopAtRoundEnd = (boolean) getParameterValue("stopAtRoundEnd");
        useFastSim = (boolean) getParameterValue("useFastSim");
        opponentK = (double) getParameterValue("opponentK");
        leafRollouts = (int) getParameterValue("leafRollouts");
        rolloutRounds = (int) getParameterValue("rolloutRounds");
        rolloutTemperature = (double) getParameterValue("rolloutTemperature");
        opponentRolloutEpsilon = (double) getParameterValue("opponentRolloutEpsilon");
        opponentRolloutTemperature = (double) getParameterValue("opponentRolloutTemperature");
        ensembleTrees = (int) getParameterValue("ensembleTrees");
        opponentTopK = (int) getParameterValue("opponentTopK");
        opponentWidening = (double) getParameterValue("opponentWidening");
        useOpponentBias = (boolean) getParameterValue("useOpponentBias");
        opponentBiasTemperature = (double) getParameterValue("opponentBiasTemperature");
        opponentBiasPrior = (double) getParameterValue("opponentBiasPrior");
        rolloutDenial = (double) getParameterValue("rolloutDenial");
        fourPlayer = (SushiMCTSParams) getParameterValue("fourPlayer");
        wallTimeLimitMS = (int) getParameterValue("wallTimeLimitMS");
    }

    @Override
    protected SushiMCTSParams _copy() {
        // As in BasicMCTSParams: TunableParameters.copy() copies every registered parameter value.
        return new SushiMCTSParams();
    }

    @Override
    public IStateHeuristic getStateHeuristic() {
        return heuristic;
    }

    @Override
    public SushiMCTSPlayer instantiate() {
        // copy() so that two agents built from the same JSON never share a parameter object
        return new SushiMCTSPlayer((SushiMCTSParams) this.copy());
    }
}
