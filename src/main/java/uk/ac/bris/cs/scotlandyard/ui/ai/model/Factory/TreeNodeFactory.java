package uk.ac.bris.cs.scotlandyard.ui.ai.model.Factory;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;

abstract public class TreeNodeFactory {
    public abstract GameTreeNode createRoot(Board board);
}
