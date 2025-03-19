package uk.ac.bris.cs.scotlandyard.ui.ai.AIGameState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

public final class AIGameStateFactory {
    private ImmutableMap<ScotlandYard.Ticket, Integer> getTickets(Board board, Piece piece) {
        Board.TicketBoard ticketBoard = board.getPlayerTickets(piece).get();
        return ImmutableMap.of(
                ScotlandYard.Ticket.BUS, ticketBoard.getCount(ScotlandYard.Ticket.BUS),
                ScotlandYard.Ticket.TAXI, ticketBoard.getCount(ScotlandYard.Ticket.TAXI),
                ScotlandYard.Ticket.UNDERGROUND, ticketBoard.getCount(ScotlandYard.Ticket.UNDERGROUND),
                ScotlandYard.Ticket.DOUBLE, ticketBoard.getCount(ScotlandYard.Ticket.DOUBLE),
                ScotlandYard.Ticket.SECRET, ticketBoard.getCount(ScotlandYard.Ticket.SECRET)
        );
    }

    @Nonnull
    public GameState build(
            Board board,
            int location,
            boolean detectivesMove) {
        Piece mrXPiece = board.getPlayers().stream().filter(Piece::isMrX).findFirst().get();
        ImmutableMap<ScotlandYard.Ticket, Integer> mrXTickets = getTickets(board, mrXPiece);

        ImmutableList<Player> detectives = board.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(piece -> new Player(piece, getTickets(board, piece), board.getDetectiveLocation((Piece.Detective) piece).get()))
                .collect(ImmutableList.toImmutableList());
        Player mrX = new Player(mrXPiece, mrXTickets, location);
        GameSetup setup = board.getSetup();

        ImmutableSet<Piece> remaining = detectivesMove ?
                board.getPlayers().stream().filter(player -> !player.isMrX()).collect(ImmutableSet.toImmutableSet()) :
                        ImmutableSet.of(mrXPiece);
        return new AIGameState(setup, remaining, ImmutableList.of(), mrX, detectives);
    }

}