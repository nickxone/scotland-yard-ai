package uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.LogEntry;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameStateFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.DetectiveTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;

import java.util.List;

public class DetectivesNodeFactory extends TreeNodeFactory {
    @Override
    public GameTreeNode createRoot(Board board) {
        int MrXLocation = 114;

        List<LogEntry> MrXLog = board.getMrXTravelLog().stream().toList();
        for (LogEntry entry : MrXLog) {
            if (entry.location().isPresent()) {
                MrXLocation = entry.location().get();
            }
        }

        AIGameStateFactory aiGameStateFactory = new AIGameStateFactory();
        Board.GameState gameState = aiGameStateFactory.build(board, MrXLocation, false);

        return new DetectiveTreeNode(gameState, null, MrXLocation);
    }
}
