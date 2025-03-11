package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import javafx.util.Pair;
import org.glassfish.grizzly.asyncqueue.AsyncQueueIO;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.GraphHelper.WeightedGraphHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
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
    }

    public GameTreeNode bestNode() {
//        TODO: implement MinMax algorithm
        GameTreeNode bestNode = childNodes.get(0);
        for (GameTreeNode childNode : childNodes) {
            if (childNode.score > bestNode.score) {
                bestNode = childNode;
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
                GameTreeNode currentBestNode = newGameState.getAvailableMoves().stream()
                        .map(move -> new GameTreeNode(finalNewGameState.advance(move), move, MrXLocation))
                        .min(Comparator.comparingInt(GameTreeNode::computeScore)).get();
                newGameState = newGameState.advance(currentBestNode.move);
            }
            Move detectivesMove = possibleStates.get(0).move; // the move doesn't matter as long as it was made by detectives
            possibleChildNodes.add(new GameTreeNode(newGameState, detectivesMove, MrXLocation));
        }
        childNodes = possibleChildNodes;
    }

    private int computeScore() { // score current node (board state, i.e. this.gameState)
        if (!gameState.getWinner().isEmpty()) return 0; // detectives has won, therefore minimum score

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = gameState.getSetup().graph;
        // check distances to detectives
        GraphHelper graphHelper = new WeightedGraphHelper();
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
