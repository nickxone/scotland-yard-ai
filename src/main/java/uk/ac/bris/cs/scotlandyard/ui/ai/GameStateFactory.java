package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.bris.cs.scotlandyard.model.*;

public class GameStateFactory {
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

    public Board.GameState getNewGameState(Board board, int location) {
        Piece mrXPiece = board.getPlayers().stream().filter(player -> player.isMrX()).findFirst().get();
//        int location = board.getAvailableMoves().stream().findFirst().get().source();
        ImmutableMap<ScotlandYard.Ticket, Integer> mrXTickets = getTickets(board, mrXPiece);

        ImmutableList<Player> detectives = board.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(piece -> new Player(piece, getTickets(board, piece), board.getDetectiveLocation((Piece.Detective) piece).get()))
                .collect(ImmutableList.toImmutableList());

        Player mrX = new Player(mrXPiece, mrXTickets, location);
        GameSetup setup = board.getSetup();
        return new MyGameStateFactory().build(setup, mrX, detectives);
    }
}
