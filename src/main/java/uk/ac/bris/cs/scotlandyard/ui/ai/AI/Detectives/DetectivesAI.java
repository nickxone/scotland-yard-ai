package uk.ac.bris.cs.scotlandyard.ui.ai.AI.Detectives;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.LogEntry;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.DetectiveTreeNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DetectivesAI implements Ai {
    private List<Move> moves;
    private int MrXLocation;

    @Nonnull
    @Override
    public String name() {
        return "Detectives AI";
    }

    @Override
    public void onStart() {
        Ai.super.onStart();
        moves = new ArrayList<>();
        MrXLocation = 114; // Central location of Mr.X
    }

    @Nonnull
    @Override
    public Move pickMove(@Nonnull Board board, Pair<Long, TimeUnit> timeoutPair) {
        if (moves.isEmpty()) {
            List<LogEntry> MrXLog = board.getMrXTravelLog().stream().toList();
            if (MrXLog.get(MrXLog.size() - 1).location().isPresent()) {
                MrXLocation = MrXLog.get(MrXLog.size() - 1).location().get();
            }

            AIGameStateFactory aiGameStateFactory = new AIGameStateFactory();
            AIGameState gameState = (AIGameState) aiGameStateFactory.build(board, MrXLocation, false);

            DetectiveTreeNode root = new DetectiveTreeNode(gameState, null, MrXLocation);
            moves = root.minimax(3, Integer.MIN_VALUE, Integer.MAX_VALUE, false, 6).getMoves();
        }

//        for (Move move : moves) {
//            for (Move move1 : moves) {
//                if (!move.equals(move1) && move.commencedBy().webColour().equals(move1.commencedBy().webColour())) {
//                    System.out.println("a;sdf");
//                }
//            }
//        }
        return moves.remove(0);
    }
}
