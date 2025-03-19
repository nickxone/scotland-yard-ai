package uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeAI;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.AIGameState.AIGameStateFactory;

public class GameTreeAI implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "GameTree AI";
    }

    @Override
    public void onStart() {
        Ai.super.onStart();
    }

    @Override
    public void onTerminate() {
        Ai.super.onTerminate();
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();

        AIGameStateFactory gameStateFactory = new AIGameStateFactory();
        Board.GameState gameState = gameStateFactory.build(board, MrXLocation, true);

        GameTreeNode root = new GameTreeNode(gameState, null, MrXLocation);
        root.computeLevels(4, 6);
        root = root.bestNode();
        return root.getMove();
    }

}
