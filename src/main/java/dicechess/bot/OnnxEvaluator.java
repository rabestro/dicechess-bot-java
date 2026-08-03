package dicechess.bot;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import dicechess.engine.domain.GameState;
import dicechess.engine.search.Evaluator;
import dicechess.engine.search.OnnxFeatures;
import dicechess.engine.search.RichFeatures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;

/**
 * Evaluates board positions using an ONNX value model or falls back to engine heuristics.
 */
public class OnnxEvaluator implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(OnnxEvaluator.class);

    private final OrtEnvironment env;
    private final OrtSession session;
    private final boolean isLoaded;

    public OnnxEvaluator(String modelPath) {
        OrtEnvironment tempEnv = null;
        OrtSession tempSession = null;
        boolean loaded = false;

        if (modelPath != null && !modelPath.isBlank()) {
            File file = new File(modelPath);
            if (file.exists() && file.isFile()) {
                try {
                    tempEnv = OrtEnvironment.getEnvironment();
                    tempSession = tempEnv.createSession(file.getAbsolutePath(), new OrtSession.SessionOptions());
                    loaded = true;
                    logger.info("Successfully loaded ONNX model from: {}", file.getAbsolutePath());
                } catch (Exception e) {
                    logger.warn("Failed to load ONNX model from {}: {}. Falling back to engine heuristic.",
                            file.getAbsolutePath(), e.getMessage());
                }
            } else {
                logger.info("ONNX model file not found at {}. Using engine heuristic evaluation.", modelPath);
            }
        } else {
            logger.info("No MODEL_PATH specified. Using engine heuristic evaluation.");
        }

        this.env = tempEnv;
        this.session = tempSession;
        this.isLoaded = loaded;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * Evaluates a GameState from the perspective of the specified color.
     *
     * @param state the board state to evaluate
     * @param color active player's color (0 for White, 1 for Black)
     * @return score float (higher is better for color)
     */
    public float evaluate(GameState state, int color) {
        if (!isLoaded || session == null || env == null) {
            // Engine heuristic fallback
            return (float) Evaluator.evaluate(state, color);
        }

        try {
            // Determine feature count expected by ONNX model (9 for RichFeatures, 7 for OnnxFeatures)
            long inputNumFeatures = session.getInputInfo().values().iterator().next().getInfo().toString().contains("9") ? 9 : 7;
            float[] features = (inputNumFeatures == 9) ?
                    RichFeatures.extract(state, color) :
                    OnnxFeatures.extract(state, color);

            long[] shape = new long[]{1, features.length};
            FloatBuffer buffer = FloatBuffer.wrap(features);

            try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, buffer, shape);
                 OrtSession.Result result = session.run(Collections.singletonMap(session.getInputNames().iterator().next(), inputTensor))) {

                Object value = result.get(0).getValue();
                if (value instanceof float[][]) {
                    return ((float[][]) value)[0][0];
                } else if (value instanceof float[]) {
                    return ((float[]) value)[0];
                } else {
                    logger.warn("Unexpected ONNX output shape: {}", value);
                    return 0.0f;
                }
            }
        } catch (OrtException e) {
            logger.warn("Error running ONNX inference: {}. Falling back to engine evaluation.", e.getMessage());
            return (float) Evaluator.evaluate(state, color);
        }
    }

    @Override
    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (OrtException e) {
                logger.error("Error closing OrtSession", e);
            }
        }
        if (env != null) {
            try {
                env.close();
            } catch (Exception e) {
                logger.error("Error closing OrtEnvironment", e);
            }
        }
    }
}
