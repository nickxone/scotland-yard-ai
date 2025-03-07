package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.List;

public class GameTreeNode {
//    Current's node value
    private Board.GameState node;
    private int score;

//    Children of the current node
    private List<GameTreeNode> childNodes;

    GameTreeNode(Board.GameState node) {
        this.node = node;
        this.score = computeScore();
    }

    private void addLevel() {
        for (Move move : node.getAvailableMoves()) {
            GameTreeNode childNode = new GameTreeNode(node.advance(move));
            childNodes.add(childNode);
        }
    }

    private int computeScore() {
//        score current node
        return 0;
    }

//    Getters
    public int getScore() {
        return score;
    }
}
