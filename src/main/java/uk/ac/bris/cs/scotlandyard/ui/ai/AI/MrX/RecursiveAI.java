package uk.ac.bris.cs.scotlandyard.ui.ai.AI.MrX;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.MrXTreeNode;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class RecursiveAI  implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "Recursive AI";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();

//        GameStateFactory gameStateFactory = new GameStateFactory();
//        Board.GameState gameState = gameStateFactory.getNewGameState(board, MrXLocation);
        AIGameStateFactory gameStateFactory = new AIGameStateFactory();
        Board.GameState gameState = gameStateFactory.build(board, MrXLocation, false);

        MrXTreeNode root = new MrXTreeNode(gameState, null, MrXLocation);
        return root.minimax(5, Integer.MIN_VALUE, Integer.MAX_VALUE, true, 6).getMove();
    }

}
