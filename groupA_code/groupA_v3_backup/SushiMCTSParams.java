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
