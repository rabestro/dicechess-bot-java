package dicechess.bot;

import dicechess.engine.domain.GameState;
import dicechess.engine.jvmapi.JvmApi;

import lv.id.jc.dicechess.runtime.TurnContext;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collections;
import java.util.List;

/**
 * Strategy implementation using an ONNX value model (or fallback engine evaluation) over every legal full turn
 * available from the current position, via the engine's {@link JvmApi} facade.
 *
 * <p>{@link TurnContext#legalMoves()} is not consulted here: scoring a candidate turn needs the resulting board
 * position, not just its UCI tokens, so the engine's own enumeration ({@link JvmApi#legalTurns(GameState)}) has to
 * run regardless of whether the platform's inline tree was present.</p>
 */
public class OnnxStrategy implements Strategy {

    private static final Logger logger = System.getLogger(OnnxStrategy.class.getName());

    private final OnnxEvaluator evaluator;

    /**
     * Constructs a new ONNX strategy instance backed by the given evaluator.
     *
     * @param evaluator position evaluator instance (ONNX-backed or engine fallback)
     */
    public OnnxStrategy(OnnxEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public List<String> chooseMoves(TurnContext context) {
        if (context == null || context.dfen() == null || context.dfen().isBlank()) {
            logger.log(Level.WARNING, "Received empty or null DFEN context");
            return Collections.emptyList();
        }

        GameState initialState;
        try {
            initialState = JvmApi.parseDfen(context.dfen());
        } catch (IllegalArgumentException e) {
            logger.log(Level.ERROR, "Failed to parse DFEN ''{0}'': {1}", context.dfen(), e.getMessage());
            return Collections.emptyList();
        }

        var activeColor = JvmApi.activeColor(initialState);
        var turns = JvmApi.legalTurns(initialState);

        if (turns.isEmpty()) {
            logger.log(Level.INFO, "No legal turn paths available for DFEN: {0}", context.dfen());
            return Collections.emptyList();
        }

        JvmApi.Turn bestTurn = null;
        float bestScore = -Float.MAX_VALUE;

        for (var turn : turns) {
            var score = evaluator.evaluate(turn.finalState(), activeColor);
            if (bestTurn == null || score > bestScore) {
                bestScore = score;
                bestTurn = turn;
            }
        }

        logger.log(Level.DEBUG, "Chosen turn path: {0} with score {1}", bestTurn.uci(), bestScore);
        return bestTurn.uci();
    }
}
