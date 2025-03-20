package uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.MrXTreeNode;

public class MrXNodeFactory extends TreeNodeFactory {
    @Override
    public GameTreeNode createRoot(Board board) {
        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();

        AIGameStateFactory gameStateFactory = new AIGameStateFactory();
        Board.GameState gameState = gameStateFactory.build(board, MrXLocation, true);

        return new MrXTreeNode(gameState, null, MrXLocation);
    }
}
