package uk.ac.bris.cs.scotlandyard.ui.ai.AI.MrX;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.PreGeneratedTreeNode;

public class PreGeneratedTreeMrXAI implements Ai {

    @Nonnull
    @Override
    public String name() {
        return "(Made for Tests) PreGeneratedTree Mr.X AI";
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

        PreGeneratedTreeNode root = new PreGeneratedTreeNode(gameState, null, MrXLocation);
        root.computeLevels(3, 6);
        root = root.bestNode();
        return root.getMove();
    }

}
