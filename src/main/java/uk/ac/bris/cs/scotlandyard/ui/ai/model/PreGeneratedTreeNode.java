package uk.ac.bris.cs.scotlandyard.ui.ai.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.WeightedGraphHelper;

import java.util.*;
import java.util.stream.Collectors;

/*-------------------FOR TESTING PURPOSES ONLY-------------------*/
public class PreGeneratedTreeNode {
    private Board.GameState gameState;
    private Move move;
    private int MrXLocation;
    private int score;

    private List<PreGeneratedTreeNode> childNodes;

    public PreGeneratedTreeNode(Board.GameState gameState, Move move, int MrXLocation) {
        this.gameState = gameState;
        this.move = move;
        this.MrXLocation = MrXLocation;
        this.score = computeScore();
        this.childNodes = new ArrayList<>();
    }

    public PreGeneratedTreeNode bestNode() {
        return minimax(this);
    }

    private PreGeneratedTreeNode minimax(PreGeneratedTreeNode node) { // implements MiniMax algorithm
        if (node.childNodes.isEmpty()) return node; // edge case

        PreGeneratedTreeNode bestNode = node.childNodes.get(0);
        if (node.move == null || node.move.commencedBy().isDetective()) { // Maximising (Mr.X move)
            for (PreGeneratedTreeNode child : node.childNodes) {
                PreGeneratedTreeNode subNode = minimax(child);
                if (subNode.score > bestNode.score) {
                    bestNode = child;
                }
            }
        } else {
            for (PreGeneratedTreeNode child : node.childNodes) { // Minimising (Detectives move)
                PreGeneratedTreeNode subNode = minimax(child);
                if (subNode.score < bestNode.score) {
                    bestNode = child;
                }
            }
        }

        return bestNode;
    }

    public void computeLevels(int levels, int maxNodes) {
        if (levels <= 0) return;

        if (this.move == null || this.move.commencedBy().isDetective()) { // MrX's turn is next
            computeMrXLevel(maxNodes);
        } else { // Detectives' turn is next
            computeDetectivesLevel(maxNodes);
        }

        if (childNodes != null) {
            for (PreGeneratedTreeNode childNode : childNodes) {
                childNode.computeLevels(levels - 1, maxNodes);
            }
        }
    }

    private void computeMrXLevel(int maxNodes) {
        List<PreGeneratedTreeNode> possibleChildNodes = new ArrayList<>();
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            PreGeneratedTreeNode childNode = new PreGeneratedTreeNode(gameState.advance(move), move, newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        childNodes = possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
    }

    private void computeDetectivesLevel(int maxNodes) {
        List<PreGeneratedTreeNode> possibleChildNodes = new ArrayList<>();
        List<PreGeneratedTreeNode> possibleStates = gameState.getAvailableMoves().stream()
                .map(move -> new PreGeneratedTreeNode(gameState.advance(move), move, MrXLocation))
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .collect(Collectors.toList());
        while (possibleChildNodes.size() < maxNodes) {
            // advance the `newGameState` the number of detectives times
            Board.GameState newGameState = gameState;
            newGameState = newGameState.advance(possibleStates.get(possibleStates.size() - 1).move);
            possibleStates.remove(possibleStates.size() - 1);

            for (int i = 0; i < newGameState.getPlayers().size() - 2; i++) {
                Board.GameState finalNewGameState = newGameState;
                Optional<PreGeneratedTreeNode> currentBestNode = newGameState.getAvailableMoves().stream()
                        .map(move -> new PreGeneratedTreeNode(finalNewGameState.advance(move), move, MrXLocation))
                        .min(Comparator.comparingInt(PreGeneratedTreeNode::computeScore));
                if (currentBestNode.isEmpty()) break;
                newGameState = newGameState.advance(currentBestNode.get().move);
            }
            Move detectivesMove = possibleStates.get(0).move; // the move doesn't matter as long as it was made by detectives
            possibleChildNodes.add(new PreGeneratedTreeNode(newGameState, detectivesMove, MrXLocation));
        }
        childNodes = possibleChildNodes;
    }

    private int computeScore() { // score current node (board state, i.e. this.gameState)
//        TODO: consider how many tickets Mr.X has got left after the move (which ones he used), and how many available moves it has after the move
        if (!gameState.getWinner().isEmpty()) return 0; // detectives has won, therefore minimum score

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = gameState.getSetup().graph;
        // check distances to detectives
        GraphHelper graphHelper = new WeightedGraphHelper();
//        GraphHelper graphHelper = new RegularGraphHelper();
        int[] distances = graphHelper.computeDistance(graph, MrXLocation);
        ImmutableList<Integer> detectives = gameState.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(player -> gameState.getDetectiveLocation((Piece.Detective) player).get())
                .collect(ImmutableList.toImmutableList());

        return detectives.stream().map(location -> distances[location]).mapToInt(Integer::intValue).sum();
    }

    public Move getMove() {
        return move;
    }

}