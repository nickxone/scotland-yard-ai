package uk.ac.bris.cs.scotlandyard.ui.ai.AI.Detectives;

import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.DetectivesNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory.TreeNodeFactory;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;

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
            TreeNodeFactory DetectivesNodeFactory = new DetectivesNodeFactory();
            GameTreeNode root = DetectivesNodeFactory.createRoot(board);
            moves = root.bestMoves(3, 6);
        }

        return moves.remove(0);
    }
}
