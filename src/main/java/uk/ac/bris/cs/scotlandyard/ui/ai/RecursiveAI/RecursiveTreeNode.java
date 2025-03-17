package uk.ac.bris.cs.scotlandyard.ui.ai.RecursiveAI;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.GameTreeAI.GameTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.GraphHelper.WeightedGraphHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecursiveTreeNode {
    private Board.GameState gameState; // Current GameState
    private Move move; // Move that was made to obtain current GameState
    private int MrXLocation;

    RecursiveTreeNode(Board.GameState gameState, Move move, int MrXLocation) {
        this.gameState = gameState;
        this.move = move;
        this.MrXLocation = MrXLocation;
    }

    public RecursiveTreeNode minimax(RecursiveTreeNode node, int depth, int alpha, int beta, boolean maximisingPlayer, int maxNodes) {
        if (depth == 0 || computeScore(node.gameState) == -1) return node;

        if (maximisingPlayer) { // Mr. X want to maximise the score
            List<RecursiveTreeNode> childNodes = computeMrXNodes(node.gameState, maxNodes);
            RecursiveTreeNode maxNode = childNodes.get(0);
            for (RecursiveTreeNode childNode : childNodes) {
                RecursiveTreeNode evaluatedNode = minimax(childNode, depth - 1, alpha, beta, false, maxNodes);
                alpha = Math.max(alpha, computeScore(evaluatedNode.gameState));
                if (beta <= alpha) break;
            }
            return maxNode;
        } else { // Detectives want to minimise the score
            List<RecursiveTreeNode> childNodes = computeMrXNodes(node.gameState, maxNodes);
            RecursiveTreeNode minNode = childNodes.get(0);
            for (RecursiveTreeNode childNode : childNodes) {
                RecursiveTreeNode evaluatedNode = minimax(childNode, depth - 1, alpha, beta, true, maxNodes);
                beta = Math.min(beta, computeScore(evaluatedNode.gameState));
                if (beta <= alpha) break;
            }
            return minNode;
        }
    }

    private List<RecursiveTreeNode> computeMrXNodes(Board.GameState gameState, int maxNodes) {
        List<RecursiveTreeNode> possibleChildNodes = new ArrayList<>();
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            RecursiveTreeNode childNode = new RecursiveTreeNode(gameState.advance(move), move, newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        return possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(computeScore(b.gameState), computeScore(a.gameState))) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
    }

    private List<RecursiveTreeNode> computeDetectivesNodes(Board.GameState gameState, int maxNodes) {
        List<Move> possibleMoves = new ArrayList<>(gameState.getAvailableMoves().stream()
                .sorted((a, b) -> Integer.compare(computeScore(gameState.advance(a)), computeScore(gameState.advance(b))))
                .toList()); // contains possible moves in ascending order based on the corresponding score

        List<RecursiveTreeNode> childNodes = new ArrayList<>();
        while (childNodes.size() < maxNodes) {
            Board.GameState newGameState = gameState.advance(possibleMoves.get(possibleMoves.size() - 1));
            possibleMoves.remove(possibleMoves.size() - 1);
            for (int i = 0; i < gameState.getPlayers().size() - 2; ++i) { // repeat for the number of detectives times - 1
                Move bestMove = newGameState.getAvailableMoves().stream()
                        .max((a, b) -> Integer.compare(computeScore(gameState.advance(a)), computeScore(gameState.advance(b))))
                        .get();
                newGameState = newGameState.advance(bestMove);
            }
            childNodes.add(new RecursiveTreeNode(newGameState, possibleMoves.get(0), MrXLocation));
        }

        return childNodes;
    }

    private int computeScore(Board.GameState currentState) { // score current node (board state, i.e. this.gameState)
//        TODO: consider how many tickets Mr.X has got left after the move (which ones he used), and how many available moves it has after the move
        if (!currentState.getWinner().isEmpty()) return -1; // detectives has won, therefore minimum score

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = currentState.getSetup().graph;
        // check distances to detectives
        GraphHelper graphHelper = new WeightedGraphHelper();
//        GraphHelper graphHelper = new RegularGraphHelper();
        int[] distances = graphHelper.computeDistance(graph, MrXLocation);
        ImmutableList<Integer> detectives = currentState.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(player -> currentState.getDetectiveLocation((Piece.Detective) player).get())
                .collect(ImmutableList.toImmutableList());

        return detectives.stream().map(location -> distances[location]).mapToInt(Integer::intValue).sum();
    }


    public Move getMove() {
        return move;
    }
}
