package uk.ac.bris.cs.scotlandyard.ui.ai.RecursiveAI;

import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

import java.util.ArrayList;
import java.util.List;

public class RecursiveTreeNode {
    private Board.GameState gameState; // Current GameState
    private Move move; // Move that was made to obtain current GameState

    public RecursiveTreeNode minimax(RecursiveTreeNode node, int depth, int alpha, int beta, boolean maximisingPlayer) {
        if (depth == 0 || computeScore(node.gameState) == -1) return node;

        if (maximisingPlayer) { // Mr. X want to maximise the score
            List<RecursiveTreeNode> childNodes = computeMrXNodes(node.gameState);
            RecursiveTreeNode maxNode = childNodes.get(0);
            for (RecursiveTreeNode childNode : childNodes) {
                RecursiveTreeNode evaluatedNode = minimax(childNode, depth - 1, alpha, beta, false);
                alpha = Math.max(alpha, computeScore(evaluatedNode.gameState));
                if (beta <= alpha) break;
            }
            return maxNode;
        } else { // Detectives want to minimise the score
            List<RecursiveTreeNode> childNodes = computeMrXNodes(node.gameState);
            RecursiveTreeNode minNode = childNodes.get(0);
            for (RecursiveTreeNode childNode : childNodes) {
                RecursiveTreeNode evaluatedNode = minimax(childNode, depth - 1, alpha, beta, true);
                beta = Math.min(beta, computeScore(evaluatedNode.gameState));
                if (beta <= alpha) break;
            }
            return minNode;
        }
    }

    private List<RecursiveTreeNode> computeMrXNodes(Board.GameState gameState) {
        List<RecursiveTreeNode> nodes = new ArrayList<RecursiveTreeNode>();
        return nodes;
    }

    private List<RecursiveTreeNode> computeDetectivesNodes(Board.GameState gameState) {
        List<RecursiveTreeNode> nodes = new ArrayList<RecursiveTreeNode>();
        return nodes;
    }

    private int computeScore(Board.GameState gameState) {
        if (!gameState.getWinner().isEmpty()) return -1;
        return 0;
    }
}
