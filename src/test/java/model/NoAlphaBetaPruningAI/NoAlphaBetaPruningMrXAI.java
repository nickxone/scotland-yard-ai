package model.NoAlphaBetaPruningAI;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.MrXNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.TreeNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.NoAlphaBetaPruningMrXTreeNode;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

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
