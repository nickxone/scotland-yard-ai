package uk.ac.bris.cs.scotlandyard.ui.ai.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState.AIGameState;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.GraphHelper.WeightedGraphHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DetectiveTreeNode {
    private Board.GameState gameState; // Current GameState
    private List<Move> moves; // Moves that were made to obtain current GameState
    private int MrXLocation;
    private int score;

    public DetectiveTreeNode(Board.GameState gameState, List<Move> moves, int MrXLocation) {
        this.gameState = gameState;
        this.moves = moves;
        this.MrXLocation = MrXLocation;
        this.score = computeScore(this.gameState);
    }

    public DetectiveTreeNode minimax(int depth, int alpha, int beta, boolean maximisingPlayer, int maxNodes) {
//        if (depth == 0 || !gameState.getWinner().isEmpty()) return this; // end of the tree, or there is a winner
        if (depth == 0) return this;

        List<DetectiveTreeNode> childNodes = maximisingPlayer ? computeMrXNodes(maxNodes) : computeDetectivesNodes(maxNodes);
        if (childNodes.isEmpty()) return this;

        if (maximisingPlayer) { // Mr. X wants to maximise the score
            int maxEval = Integer.MIN_VALUE;
            DetectiveTreeNode maxNode = childNodes.get(0);
            for (DetectiveTreeNode childNode : childNodes) {
                DetectiveTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, false, maxNodes);
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
            DetectiveTreeNode minNode = childNodes.get(0);
            for (DetectiveTreeNode childNode : childNodes) {
                DetectiveTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, true, maxNodes);
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

    private List<DetectiveTreeNode> computeMrXNodes(int maxNodes) {
        List<DetectiveTreeNode> possibleChildNodes = new ArrayList<>();
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            DetectiveTreeNode childNode = new DetectiveTreeNode((AIGameState) gameState.advance(move), List.of(move), newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        List<DetectiveTreeNode> nodes = possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
        return nodes;
    }

    private List<DetectiveTreeNode> computeDetectivesNodes(int maxNodes) {
        List<Move> possibleMoves = new ArrayList<>(gameState.getAvailableMoves().stream()
                .sorted((a, b) -> Integer.compare(computeScore(gameState.advance(b)), computeScore(gameState.advance(a))))
                .toList()); // Sort available moves in descending order of computed score (worse moves last)

        List<DetectiveTreeNode> childNodes = new ArrayList<>();

        while (!possibleMoves.isEmpty() && childNodes.size() < maxNodes) {
            // Get and remove the worst-scoring move (last in sorted list)
            Board.GameState newGameState = gameState;
            List<Move> bestMoves = new ArrayList<>(List.of(possibleMoves.remove(possibleMoves.size() - 1)));
            while (bestMoves.get(bestMoves.size() - 1) != null && bestMoves.get(bestMoves.size() - 1).commencedBy().isDetective()) { // while detectives can move
                newGameState = newGameState.advance(bestMoves.get(bestMoves.size() - 1));
                Board.GameState finalNewGameState = newGameState;

                bestMoves.add(finalNewGameState.getAvailableMoves().stream()
                        .min((a, b) -> Integer.compare(computeScore(finalNewGameState.advance(a)), computeScore(finalNewGameState.advance(b))))
                        .filter(move -> move.commencedBy().isDetective())
                        .orElse(null));
            }
//            if (bestMoves.contains(null)) bestMoves.remove(bestMoves.size() - 1);
//            bestMoves.remove(bestMoves.size() - 1);
            bestMoves.remove(null);

            childNodes.add(new DetectiveTreeNode(newGameState, bestMoves, MrXLocation));
        }

        return childNodes;
    }

    private int computeScore(Board.GameState currentGameState) { // score current node (board state, i.e. this.gameState)
//        TODO: detectives shouldn't increase score by a lot if they 'think' they won, since they can't guarantee it

//        TODO: consider how many tickets Mr.X has got left after the move (which ones he used), and how many available moves it has after the move
//        TODO: if the current location of Mr.X is visible for detectives, encourage the use of secret ticket
        if (!currentGameState.getWinner().isEmpty()) { // Check if there is a winner
            return currentGameState.getWinner().contains(Piece.MrX.MRX) ? 1000 : -1000;
        }

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = currentGameState.getSetup().graph;
        // Check distances to detectives
        GraphHelper graphHelper = new WeightedGraphHelper();
        int[] distances = graphHelper.computeDistance(graph, MrXLocation);
        ImmutableList<Integer> detectives = currentGameState.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(player -> currentGameState.getDetectiveLocation((Piece.Detective) player).get())
                .collect(ImmutableList.toImmutableList());

        return detectives.stream().map(location -> distances[location]).mapToInt(Integer::intValue).sum();
    }

    public List<Move> getMoves() {
        return moves;
    }
}