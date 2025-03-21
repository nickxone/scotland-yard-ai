package uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;
        import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.RegularGraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.WeightedGraphHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoAlphaBetaPruningMrXTreeNode {
    protected Board.GameState gameState; // Current GameState
    protected List<Move> moves; // Move that was made to obtain current GameState
    protected int MrXLocation;
    protected int score;

    public NoAlphaBetaPruningMrXTreeNode(Board.GameState gameState, List<Move> moves, int MrXLocation) {
        this.gameState = gameState;
        this.moves = moves;
        this.MrXLocation = MrXLocation;
        this.score = computeScore();
    }

    protected List<NoAlphaBetaPruningMrXTreeNode> computeMrXNodes(int maxNodes) {
        List<NoAlphaBetaPruningMrXTreeNode> possibleChildNodes = new ArrayList<>();
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            NoAlphaBetaPruningMrXTreeNode childNode = new NoAlphaBetaPruningMrXTreeNode(gameState.advance(move), List.of(move), newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        return possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
    }

    protected List<NoAlphaBetaPruningMrXTreeNode> computeDetectivesNodes(int maxNodes) {
        List<Move> possibleMoves = new ArrayList<>(gameState.getAvailableMoves().stream()
                .sorted((a, b) ->
                        Integer.compare(new NoAlphaBetaPruningMrXTreeNode(gameState.advance(b), moves, MrXLocation).score, new NoAlphaBetaPruningMrXTreeNode(gameState.advance(a), moves, MrXLocation).score))
                .toList()); // Sort available moves in descending order of computed score (worse moves last)

        List<NoAlphaBetaPruningMrXTreeNode> childNodes = new ArrayList<>();

        while (!possibleMoves.isEmpty() && childNodes.size() < maxNodes) {
            // Get and remove the worst-scoring move (last in sorted list)
            Board.GameState newGameState = gameState;
            List<Move> bestMoves = new ArrayList<>(List.of(possibleMoves.remove(possibleMoves.size() - 1)));
            while (bestMoves.get(bestMoves.size() - 1) != null && bestMoves.get(bestMoves.size() - 1).commencedBy().isDetective()) { // while detectives can move
                newGameState = newGameState.advance(bestMoves.get(bestMoves.size() - 1));
                Board.GameState finalNewGameState = newGameState;

                bestMoves.add(finalNewGameState.getAvailableMoves().stream()
                        .min((a, b) ->
                                Integer.compare(new NoAlphaBetaPruningMrXTreeNode(finalNewGameState.advance(a), moves, MrXLocation).score, new NoAlphaBetaPruningMrXTreeNode(finalNewGameState.advance(b), moves, MrXLocation).score))
                        .filter(move -> move.commencedBy().isDetective())
                        .orElse(null));
            }

            bestMoves.remove(null);

            childNodes.add(new NoAlphaBetaPruningMrXTreeNode(newGameState, bestMoves, MrXLocation));
        }

        return childNodes;
    }


    private int computeScore() { // score current node (board state, i.e. this.gameState)
        int score = 0;

        if (!this.gameState.getWinner().isEmpty()) // Check if there is a winner
            return this.gameState.getWinner().contains(Piece.MrX.MRX) ? ScoreConstants.WIN_SCORE : ScoreConstants.LOSE_SCORE; // only case for Mr.X, as detectives' gameState doesn't check for a winner

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = this.gameState.getSetup().graph;
        GraphHelper graphHelper = new WeightedGraphHelper();
        int[] distances = graphHelper.computeDistance(graph, this.MrXLocation); // Check distances to detectives
        ImmutableList<Integer> detectives = this.gameState.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(player -> this.gameState.getDetectiveLocation((Piece.Detective) player).get())
                .collect(ImmutableList.toImmutableList());

        // Encourage Mr.X to minimise distance to detectives
        int distance = detectives.stream().map(location -> distances[location]).mapToInt(Integer::intValue).sum();
        score += distance * ScoreConstants.DISTANCE_MULTIPLIER;

        // Encourage Mr.X to use a secret tickets
        if (moves != null) {
            Move move = this.moves.get(0);
            ImmutableList<LogEntry> travelLog = this.gameState.getMrXTravelLog();
            if (move.commencedBy().isMrX() && travelLog.size() > 2) {
                LogEntry entry = travelLog.get(travelLog.size() - 1);
                if (entry.location().isPresent() && entry.ticket().equals(ScotlandYard.Ticket.SECRET))
                    score += ScoreConstants.SECRET_TICKET_BONUS;
                else if (move instanceof Move.DoubleMove) {
                    entry = travelLog.get(travelLog.size() - 2);
                    if (entry.location().isPresent() && entry.ticket().equals(ScotlandYard.Ticket.SECRET))
                        score += ScoreConstants.SECRET_TICKET_BONUS;
                }
            }
        }

        // Discourage Mr.X to use double tickets to early
        if (moves != null && moves.get(0) instanceof Move.DoubleMove) {
            int remainingMoves = this.gameState.getSetup().moves.size() - this.gameState.getMrXTravelLog().size();
            score -= remainingMoves * ScoreConstants.DOUBLE_TICKET_PENALTY_MULTIPLIER;
        }

        // Encourage Mr.X to keep tickets
        Piece mrXPiece = this.gameState.getPlayers().stream().filter(Piece::isMrX).findFirst().get();
        score += scoreTickets(this.gameState, mrXPiece);

        return score;
    }

    private int scoreTickets(Board.GameState gameState, Piece piece) {
        Board.TicketBoard ticketBoard = gameState.getPlayerTickets(piece).get();
        return ticketBoard.getCount(ScotlandYard.Ticket.TAXI) * ScoreConstants.TAXI_TICKET_VALUE +
                ticketBoard.getCount(ScotlandYard.Ticket.BUS) * ScoreConstants.BUS_TICKET_VALUE +
                ticketBoard.getCount(ScotlandYard.Ticket.UNDERGROUND) * ScoreConstants.UNDERGROUND_TICKET_VALUE +
                ticketBoard.getCount(ScotlandYard.Ticket.SECRET) * ScoreConstants.SECRET_TICKET_VALUE +
                ticketBoard.getCount(ScotlandYard.Ticket.DOUBLE) * ScoreConstants.DOUBLE_TICKET_VALUE;
    }


    protected NoAlphaBetaPruningMrXTreeNode minimax(int depth, int alpha, int beta, boolean maximisingPlayer, int maxNodes) {
        if (depth == 0 || !gameState.getWinner().isEmpty()) return this; // end of the tree, or there is a winner

        List<NoAlphaBetaPruningMrXTreeNode> childNodes = maximisingPlayer ? computeMrXNodes(maxNodes) : computeDetectivesNodes(maxNodes);
        if (childNodes.isEmpty()) return this;

        if (maximisingPlayer) { // Mr. X wants to maximise the score
            int maxEval = Integer.MIN_VALUE;
            NoAlphaBetaPruningMrXTreeNode maxNode = childNodes.get(0);
            for (NoAlphaBetaPruningMrXTreeNode childNode : childNodes) {
                NoAlphaBetaPruningMrXTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, false, maxNodes);
                if (evaluatedNode.score > maxEval) {
                    maxEval = evaluatedNode.score;
                    maxNode = childNode;
                    maxNode.score = maxEval;
                }
            }
            return maxNode;
        } else { // Detectives want to minimise the score
            int minEval = Integer.MAX_VALUE;
            NoAlphaBetaPruningMrXTreeNode minNode = childNodes.get(0);
            for (NoAlphaBetaPruningMrXTreeNode childNode : childNodes) {
                NoAlphaBetaPruningMrXTreeNode evaluatedNode = childNode.minimax(depth - 1, alpha, beta, true, maxNodes);
                if (evaluatedNode.score < minEval) {
                    minEval = evaluatedNode.score;
                    minNode = childNode;
                    minNode.score = minEval;
                }
            }
            return minNode;
        }
    }

    public List<Move> bestMoves(int depth, int maxNodes) {
        return minimax(depth, Integer.MIN_VALUE, Integer.MAX_VALUE, true, maxNodes).moves;
    }
}
