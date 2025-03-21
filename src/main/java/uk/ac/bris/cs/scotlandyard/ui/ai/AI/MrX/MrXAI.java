package uk.ac.bris.cs.scotlandyard.ui.ai.AI.MrX;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.MrXNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.TreeNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.MrXTreeNode;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class MrXAI implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "MrX AI";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        TreeNodeFactory MrXNodeFactory = new MrXNodeFactory();
        GameTreeNode root = MrXNodeFactory.createRoot(board);
        return root.bestMoves(6, 6).get(0);
    }

}
