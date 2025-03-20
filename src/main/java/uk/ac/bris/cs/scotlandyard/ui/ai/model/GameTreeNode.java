package uk.ac.bris.cs.scotlandyard.ui.ai.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.WeightedGraphHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class GameTreeNode {
    protected Board.GameState gameState; // Current GameState
    protected List<Move> moves; // Move that was made to obtain current GameState
    protected int MrXLocation;
    protected int score;

    protected GameTreeNode(Board.GameState gameState, List<Move> moves, int MrXLocation) {
        this.gameState = gameState;
        this.moves = moves;
        this.MrXLocation = MrXLocation;
        this.score = computeScore(this);
    }

    protected List<GameTreeNode> computeMrXNodes(int maxNodes) {
        List<GameTreeNode> possibleChildNodes = new ArrayList<>();
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            GameTreeNode childNode = createChild(gameState.advance(move), List.of(move), newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        return possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
    }

    protected List<GameTreeNode> computeDetectivesNodes(int maxNodes) {
        List<Move> possibleMoves = new ArrayList<>(gameState.getAvailableMoves().stream()
                .sorted((a, b) ->
                        Integer.compare(createChild(gameState.advance(b), moves, MrXLocation).score, createChild(gameState.advance(a), moves, MrXLocation).score))
                .toList()); // Sort available moves in descending order of computed score (worse moves last)

        List<GameTreeNode> childNodes = new ArrayList<>();

        while (!possibleMoves.isEmpty() && childNodes.size() < maxNodes) {
            // Get and remove the worst-scoring move (last in sorted list)
            Board.GameState newGameState = gameState;
            List<Move> bestMoves = new ArrayList<>(List.of(possibleMoves.remove(possibleMoves.size() - 1)));
            while (bestMoves.get(bestMoves.size() - 1) != null && bestMoves.get(bestMoves.size() - 1).commencedBy().isDetective()) { // while detectives can move
                newGameState = newGameState.advance(bestMoves.get(bestMoves.size() - 1));
                Board.GameState finalNewGameState = newGameState;

                bestMoves.add(finalNewGameState.getAvailableMoves().stream()
                        .min((a, b) ->
                                Integer.compare(createChild(finalNewGameState.advance(a), moves, MrXLocation).score, createChild(finalNewGameState.advance(b), moves, MrXLocation).score))
                        .filter(move -> move.commencedBy().isDetective())
                        .orElse(null));
            }

            bestMoves.remove(null);

            childNodes.add(createChild(newGameState, bestMoves, MrXLocation));
        }

        return childNodes;
    }


    private int computeScore(GameTreeNode node) { // score current node (board state, i.e. this.gameState)
//        TODO: consider how many tickets Mr.X has got left after the move (which ones he used), and how many available moves it has after the move
//        TODO: if the current location of Mr.X is visible for detectives, encourage the use of secret ticket
//        TODO:
        if (!node.gameState.getWinner().isEmpty()) { // Check if there is a winner
            return node.gameState.getWinner().contains(Piece.MrX.MRX) ? 1000 : -1000;
        }

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = node.gameState.getSetup().graph;
        // Check distances to detectives
        GraphHelper graphHelper = new WeightedGraphHelper();
        int[] distances = graphHelper.computeDistance(graph, node.MrXLocation);
        ImmutableList<Integer> detectives = node.gameState.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(player -> node.gameState.getDetectiveLocation((Piece.Detective) player).get())
                .collect(ImmutableList.toImmutableList());

        return detectives.stream().map(location -> distances[location]).mapToInt(Integer::intValue).sum();
    }

    protected abstract GameTreeNode createChild(Board.GameState gameState, List<Move> move, int newLocation);

    protected abstract GameTreeNode minimax(int depth, int alpha, int beta, boolean maximisingPlayer, int maxNodes);

    public abstract List<Move> bestMoves(int depth, int maxNodes);
}
