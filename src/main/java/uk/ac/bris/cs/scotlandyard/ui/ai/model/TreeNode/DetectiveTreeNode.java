package uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.List;

public class DetectiveTreeNode extends GameTreeNode {

    public DetectiveTreeNode(Board.GameState gameState, List<Move> moves, int MrXLocation) {
        super(gameState, moves, MrXLocation);
    }

    @Override
    protected GameTreeNode createChild(Board.GameState gameState, List<Move> moves, int newLocation) {
        return new DetectiveTreeNode(gameState, moves, newLocation);
    }

    public GameTreeNode minimax(int depth, int alpha, int beta, boolean maximisingPlayer, int maxNodes) {
        if (depth == 0) return this;

        List<GameTreeNode> childNodes = maximisingPlayer ? computeMrXNodes(maxNodes) : computeDetectivesNodes(maxNodes);
        if (childNodes.isEmpty()) return this;

        if (maximisingPlayer) { // Mr. X wants to maximise the score
            int maxEval = Integer.MIN_VALUE;
            GameTreeNode maxNode = childNodes.get(0);
            for (GameTreeNode childNode : childNodes) {
                GameTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, false, maxNodes);
                if (evaluatedNode.score > maxEval) {
                    maxEval = evaluatedNode.score;
                    maxNode = childNode;
                    maxNode.score = maxEval;
                }
                alpha = Math.max(alpha, evaluatedNode.score);
                if (beta <= alpha) break;
            }
            return maxNode;
        } else { // Detectives want to minimise the score
            int minEval = Integer.MAX_VALUE;
            GameTreeNode minNode = childNodes.get(0);
            for (GameTreeNode childNode : childNodes) {
                GameTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, true, maxNodes);
                if (evaluatedNode.score < minEval) {
                    minEval = evaluatedNode.score;
                    minNode = childNode;
                    minNode.score = minEval;
                }
                beta = Math.min(beta, evaluatedNode.score);
                if (beta <= alpha) break;
            }
            return minNode;
        }
    }

    @Override
    public List<Move> bestMoves(int depth, int maxNodes) {
        return minimax(depth, Integer.MIN_VALUE, Integer.MAX_VALUE, false, maxNodes).moves;
    }

}