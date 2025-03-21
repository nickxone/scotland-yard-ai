package model.NoAlphaBetaPruningAI;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;

/*-------------FOR TESTING PURPOSES ONLY (please don't mark the structure)-------------*/
public class NoAlphaBetaPruningMrXTreeNodeFactory {

    public NoAlphaBetaPruningMrXTreeNode createRoot(Board board) {
        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();

        AIGameStateFactory gameStateFactory = new AIGameStateFactory();
        Board.GameState gameState = gameStateFactory.build(board, MrXLocation, true);

        return new NoAlphaBetaPruningMrXTreeNode(gameState, null, MrXLocation);
    }
}
