package model.NoAlphaBetaPruningAI;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.LogEntry;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.TreeNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.DetectiveTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.MrXTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.NoAlphaBetaPruningMrXTreeNode;

import java.util.List;

public class NoAlphaBetaPruningMrXTreeNodeFactory {

    public NoAlphaBetaPruningMrXTreeNode createRoot(Board board) {
        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();

        AIGameStateFactory gameStateFactory = new AIGameStateFactory();
        Board.GameState gameState = gameStateFactory.build(board, MrXLocation, true);

        return new NoAlphaBetaPruningMrXTreeNode(gameState, null, MrXLocation);
    }
}
