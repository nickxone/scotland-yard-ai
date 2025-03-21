package uk.ac.bris.cs.scotlandyard.ui.ai.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.GameTreeNode;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode.ScoreConstants;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.GraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.RegularGraphHelper;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.WeightedGraphHelper;

import java.util.*;
import java.util.stream.Collectors;

/*-------------------FOR TESTING PURPOSES ONLY-------------------*/
public class PreGeneratedTreeNode {
    private Board.GameState gameState;
    private List<Move> moves;
    private int MrXLocation;
    private int score;

    private List<PreGeneratedTreeNode> childNodes;

    public PreGeneratedTreeNode(Board.GameState gameState, List<Move> moves, int MrXLocation) {
        this.gameState = gameState;
        this.moves = moves;
        this.MrXLocation = MrXLocation;
        this.score = computeScore();
        this.childNodes = new ArrayList<>();
    }

    public PreGeneratedTreeNode bestNode() {
        return minimax(this);
    }

    private PreGeneratedTreeNode minimax(PreGeneratedTreeNode node) { // implements MiniMax algorithm
        if (node.childNodes.isEmpty() || !gameState.getWinner().isEmpty()) return node; // edge case

        PreGeneratedTreeNode bestNode = node.childNodes.get(0);
        if (node.moves == null || node.moves.get(0).commencedBy().isDetective()) { // Maximising (Mr.X move)
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

        if (this.moves == null || this.moves.get(0).commencedBy().isDetective()) { // MrX's turn is next
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
            PreGeneratedTreeNode childNode = new PreGeneratedTreeNode(gameState.advance(move), List.of(move), newMrXLocation);
            possibleChildNodes.add(childNode);
        }
        childNodes = possibleChildNodes.stream()
                .sorted((a, b) -> Integer.compare(b.score, a.score)) // sort in descending order (the higher score, the better)
                .limit(maxNodes)
                .collect(Collectors.toList());
    }

    private void computeDetectivesLevel(int maxNodes) {
        List<Move> possibleMoves = new ArrayList<>(gameState.getAvailableMoves().stream()
                .sorted((a, b) ->
                        Integer.compare(new PreGeneratedTreeNode(gameState.advance(b), moves, MrXLocation).score, new PreGeneratedTreeNode(gameState.advance(a), moves, MrXLocation).score))
                .toList()); // Sort available moves in descending order of computed score (worse moves last)

        List<PreGeneratedTreeNode> newChildNodes = new ArrayList<>();

        while (!possibleMoves.isEmpty() && newChildNodes.size() < maxNodes) {
            // Get and remove the worst-scoring move (last in sorted list)
            Board.GameState newGameState = gameState;
            List<Move> bestMoves = new ArrayList<>(List.of(possibleMoves.remove(possibleMoves.size() - 1)));
            while (bestMoves.get(bestMoves.size() - 1) != null && bestMoves.get(bestMoves.size() - 1).commencedBy().isDetective()) { // while detectives can move
                newGameState = newGameState.advance(bestMoves.get(bestMoves.size() - 1));
                Board.GameState finalNewGameState = newGameState;

                bestMoves.add(finalNewGameState.getAvailableMoves().stream()
                        .min((a, b) ->
                                Integer.compare(new PreGeneratedTreeNode(finalNewGameState.advance(a), moves, MrXLocation).score, new PreGeneratedTreeNode(finalNewGameState.advance(b), moves, MrXLocation).score))
                        .filter(move -> move.commencedBy().isDetective())
                        .orElse(null));
            }

            bestMoves.remove(null);

            newChildNodes.add(new PreGeneratedTreeNode(newGameState, bestMoves, MrXLocation));
        }

        childNodes = newChildNodes;
    }

    private int computeScore() { // score current node (board state, i.e. this.gameState)
        int score = 0;

        if (!this.gameState.getWinner().isEmpty()) // Check if there is a winner
            return this.gameState.getWinner().contains(Piece.MrX.MRX) ? ScoreConstants.WIN_SCORE : ScoreConstants.LOSE_SCORE; // only case for Mr.X, as detectives' gameState doesn't check for a winner

        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = this.gameState.getSetup().graph;
        GraphHelper graphHelper = new RegularGraphHelper();
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


    public List<Move> getMoves() {
        return moves;
    }

}