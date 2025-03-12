package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.GraphHelper.WeightedGraphHelper;

import java.util.*;
import java.util.stream.Collectors;

public class GameTreeNode {
    private Board.GameState gameState;
    private Move move;
    private int MrXLocation;
    private int score;

    private List<GameTreeNode> childNodes;

    GameTreeNode(Board.GameState gameState, Move move, int MrXLocation) {
        this.gameState = gameState;
        this.move = move;
        this.MrXLocation = MrXLocation;
        this.score = computeScore();
        this.childNodes = new ArrayList<>();
    }

    public GameTreeNode bestNode() {
        return minimax(this);
    }

    private GameTreeNode minimax(GameTreeNode node) { // implements MiniMax algorithm
        if (node.childNodes.isEmpty()) return node; // edge case

        GameTreeNode bestNode = node.childNodes.get(0);
        if (node.move == null || node.move.commencedBy().isDetective()) { // Maximising (Mr.X move)
            for (GameTreeNode child : node.childNodes) {
                GameTreeNode subNode = minimax(child);
                if (subNode.score > bestNode.score) {
                    bestNode = child;
                }
            }
        } else {
            for (GameTreeNode child : node.childNodes) { // Minimising (Detectives move)
                GameTreeNode subNode = minimax(child);
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
            for (GameTreeNode childNode : childNodes) {
                childNode.computeLevels(levels - 1, maxNodes);
            }
        }
    }

    private void computeMrXLevel(int maxNodes) {
        List<GameTreeNode> possibleChildNodes = new ArrayList<>();
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            GameTreeNode childNode = new GameTreeNode(gameState.advance(move), move, newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        // Select up to `maxNodes` best nodes for efficiency (Alpha–beta pruning)
        childNodes = possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
    }

    private void computeDetectivesLevel(int maxNodes) {
        List<GameTreeNode> possibleChildNodes = new ArrayList<>();
        List<GameTreeNode> possibleStates = gameState.getAvailableMoves().stream()
                .map(move -> new GameTreeNode(gameState.advance(move), move, MrXLocation))
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .collect(Collectors.toList());
        while (possibleChildNodes.size() < maxNodes) {
            // advance the `newGameState` the number of detectives times
            Board.GameState newGameState = gameState;
            newGameState = newGameState.advance(possibleStates.get(possibleStates.size() - 1).move);
            possibleStates.remove(possibleStates.size() - 1);

            for (int i = 0; i < newGameState.getPlayers().size() - 2; i++) {
                Board.GameState finalNewGameState = newGameState;
                Optional<GameTreeNode> currentBestNode = newGameState.getAvailableMoves().stream()
                        .map(move -> new GameTreeNode(finalNewGameState.advance(move), move, MrXLocation))
                        .min(Comparator.comparingInt(GameTreeNode::computeScore));
                if (currentBestNode.isEmpty()) break;
                newGameState = newGameState.advance(currentBestNode.get().move);
            }
            Move detectivesMove = possibleStates.get(0).move; // the move doesn't matter as long as it was made by detectives
            possibleChildNodes.add(new GameTreeNode(newGameState, detectivesMove, MrXLocation));
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
