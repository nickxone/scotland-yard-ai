package uk.ac.bris.cs.scotlandyard.ui.ai.RecursiveAI;

import com.esotericsoftware.kryo.util.Null;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
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

    public RecursiveTreeNode minimax(int depth, int alpha, int beta, boolean maximisingPlayer, int maxNodes) {
        if (depth == 0 || computeScore(this.gameState) == -1) return this;

        List<RecursiveTreeNode> childNodes = maximisingPlayer ? computeMrXNodes(maxNodes) : computeDetectivesNodes(maxNodes);

        if (maximisingPlayer) { // Mr. X wants to maximise the score
            RecursiveTreeNode maxNode = childNodes.get(0);
            for (RecursiveTreeNode childNode : childNodes) {
                RecursiveTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, false, maxNodes);
                if (computeScore(evaluatedNode.gameState) > computeScore(maxNode.gameState)) {
                    maxNode = childNode;
                }
                alpha = Math.max(alpha, computeScore(evaluatedNode.gameState));
                if (beta <= alpha) break;
            }
            return maxNode;
        } else { // Detectives want to minimise the score
            RecursiveTreeNode minNode = childNodes.get(0);
            for (RecursiveTreeNode childNode : childNodes) {
                RecursiveTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, true, maxNodes);
                if (computeScore(evaluatedNode.gameState) < computeScore(minNode.gameState)) {
                    minNode = childNode;
                }
                beta = Math.min(beta, computeScore(evaluatedNode.gameState));
                if (beta <= alpha) break;
            }
            return minNode;
        }
    }

    private List<RecursiveTreeNode> computeMrXNodes(int maxNodes) {
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

    private List<RecursiveTreeNode> computeDetectivesNodes(int maxNodes) {
        List<Move> possibleMoves = new ArrayList<>(gameState.getAvailableMoves().stream()
                .sorted((a, b) -> Integer.compare(computeScore(gameState.advance(b)), computeScore(gameState.advance(a))))
                .toList()); // Sort available moves in descending order of computed score (worse moves last)

        List<RecursiveTreeNode> childNodes = new ArrayList<>();

        while (!possibleMoves.isEmpty() && childNodes.size() < maxNodes) {
            // Get and remove the worst-scoring move (last in sorted list)
            Board.GameState newGameState = gameState;
            Move bestMove = possibleMoves.remove(possibleMoves.size() - 1);

            while (bestMove != null) { // while detectives can move
                newGameState = newGameState.advance(bestMove);
                Board.GameState finalNewGameState = newGameState;
                bestMove = newGameState.getAvailableMoves().stream()
                        .min((a, b) -> Integer.compare(computeScore(finalNewGameState.advance(a)), computeScore(finalNewGameState.advance(b))))
                        .orElse(null);
            }

            childNodes.add(new RecursiveTreeNode(newGameState, move, MrXLocation));
        }

        return childNodes;
    }


    private int computeScore(Board.GameState currentGameState) { // score current node (board state, i.e. this.gameState)
//        TODO: consider how many tickets Mr.X has got left after the move (which ones he used), and how many available moves it has after the move
//        TODO: if the current location of Mr.X is visible for detectives, encourage the use of secret ticket

        if (!currentGameState.getWinner().isEmpty()) return -1; // detectives has won, therefore minimum score

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


    public Move getMove() {
        return move;
    }
}
