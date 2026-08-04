package dicechess.bot;

import lv.id.jc.dicechess.runtime.TurnContext;

import java.util.List;
import java.util.function.Function;

/**
 * Common interface for Java bot strategies mapping a TurnContext to move notations.
 */
@FunctionalInterface
public interface Strategy extends Function<TurnContext, List<String>> {

    /**
     * Choose the best list of move notations (micro-moves forming a turn) for the given TurnContext.
     *
     * @param context turn context containing DFEN, remaining time, increment, etc.
     * @return list of move notations (e.g. ["e2e4", "g1f3"])
     */
    List<String> chooseMoves(TurnContext context);

    @Override
    default List<String> apply(TurnContext context) {
        return chooseMoves(context);
    }
}
