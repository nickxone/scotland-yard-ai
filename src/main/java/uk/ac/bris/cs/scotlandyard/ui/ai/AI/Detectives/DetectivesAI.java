package uk.ac.bris.cs.scotlandyard.ui.ai.AI.Detectives;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.LogEntry;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.DetectiveTreeNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetectivesAI implements Ai {
    private List<Move> moves;
    private int MrXLocation;
    private int depth;
    private int maxNodes;

    @Nonnull
    @Override
    public String name() {
        return "Detectives AI";
    }

    @Override
    public void onStart() {
        Ai.super.onStart();

//        Set up the depth and max number of immediate child nodes for a tree
        depth = 3;
        maxNodes = 6;

        moves = new ArrayList<>();
        MrXLocation = 114; // Approximately central location on the board
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        if (moves.isEmpty()) {

            List<LogEntry> MrXLog = board.getMrXTravelLog().stream().toList();
            for (LogEntry entry : MrXLog) {
                if (entry.location().isPresent()) {
                    MrXLocation = entry.location().get();
                }
            }

            AIGameStateFactory aiGameStateFactory = new AIGameStateFactory();
            Board.GameState gameState = aiGameStateFactory.build(board, MrXLocation, false);

            DetectiveTreeNode root = new DetectiveTreeNode(gameState, null, MrXLocation);
            moves = root.bestMoves(depth, maxNodes);
        }

        return moves.remove(0);
    }
}
