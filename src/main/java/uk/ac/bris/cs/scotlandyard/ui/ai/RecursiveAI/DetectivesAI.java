package uk.ac.bris.cs.scotlandyard.ui.ai.RecursiveAI;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.GameStateFactory;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class DetectivesAI implements Ai {
    @Nonnull
    @Override
    public String name() {
        return "Detectives AI";
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        int MrXLocation;
        MrXLocation = 114; // Central location of Mr.X

        GameStateFactory gameStateFactory = new GameStateFactory();
        Board.GameState gameState = gameStateFactory.getNewGameState(board, MrXLocation);

        DetectiveTreeNode root = new DetectiveTreeNode(gameState, null, MrXLocation);
        Move bestMove = root.minimax(1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, 5).getMove();
        return bestMove;
    }
}
