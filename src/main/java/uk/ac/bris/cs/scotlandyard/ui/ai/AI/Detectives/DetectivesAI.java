package uk.ac.bris.cs.scotlandyard.ui.ai.AI.Detectives;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.DetectiveTreeNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetectivesAI implements Ai {
    private List<Move> moves;

    @Nonnull
    @Override
    public String name() {
        return "Detectives AI";
    }

    @Override
    public void onStart() {
        Ai.super.onStart();
        moves = new ArrayList<>();
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        if (moves.isEmpty()) {
            int MrXLocation;
            MrXLocation = 114; // Central location of Mr.X

            AIGameStateFactory aiGameStateFactory = new AIGameStateFactory();
            Board.GameState gameState = aiGameStateFactory.build(board, MrXLocation, true);

            DetectiveTreeNode root = new DetectiveTreeNode(gameState, null, MrXLocation);
            moves = root.minimax(1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, 5).getMoves();
        }
        return moves.remove(moves.size() - 1);
    }
}
