package uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeAI;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.GameStateFactory;

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

        GameStateFactory gameStateFactory = new GameStateFactory();
        Board.GameState gameState = gameStateFactory.getNewGameState(board, MrXLocation);

        GameTreeNode root = new GameTreeNode(gameState, null, MrXLocation);
        root.computeLevels(4, 6);
        root = root.bestNode();
        return root.getMove();
    }

}
