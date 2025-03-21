package model.NoAlphaBetaPruningAI;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/*-------------FOR TESTING PURPOSES ONLY (please don't mark the structure)-------------*/
public class NoAlphaBetaPruningMrXAI implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "MrX AI";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        NoAlphaBetaPruningMrXTreeNodeFactory MrXNodeFactory = new NoAlphaBetaPruningMrXTreeNodeFactory();
        NoAlphaBetaPruningMrXTreeNode root = MrXNodeFactory.createRoot(board);
        return root.bestMoves(3, 6).get(0);
    }

}
