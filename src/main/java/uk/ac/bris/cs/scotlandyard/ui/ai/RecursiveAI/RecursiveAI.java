package uk.ac.bris.cs.scotlandyard.ui.ai.RecursiveAI;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.GameStateFactory;

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
        GameStateFactory gameStateFactory = new GameStateFactory();
        Board.GameState gameState = gameStateFactory.getNewGameState(board);

        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();
        RecursiveTreeNode root = new RecursiveTreeNode(gameState, null, MrXLocation);
        return root.minimax(3, Integer.MIN_VALUE, Integer.MAX_VALUE, true, 3).getMove();
    }

}
