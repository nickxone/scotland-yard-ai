package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Interner;
import io.atlassian.fugue.Pair;
import org.commonmark.internal.IndentedCodeBlockParser;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.ui.model.Setup;

public class MyAi implements Ai {

    private GameTreeNode root;

    @Nonnull
    @Override
    public String name() {
        return "Mr. <T>";
    }

    @Override
    public void onStart() {
        Ai.super.onStart();
    }

    @Override
    public void onTerminate() {
        Ai.super.onTerminate();
    }

    @Nonnull
    @Override
    public Move pickMove(
            @Nonnull Board board,
            Pair<Long, TimeUnit> timeoutPair) {
        Board.GameState gameState = boardToGameState(board);

        int MrXLocation = board.getAvailableMoves().stream().findFirst().get().source();
        root = new GameTreeNode(gameState, null, MrXLocation);
        root.computeNextLevel(10);
        root = root.bestNode();
        return root.getMove();
    }

    //    Functions related to type conversion from `Board` to `Board.GameState`
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

    private Board.GameState boardToGameState(Board board) {
        Piece mrXPiece = board.getPlayers().stream().filter(player -> player.isMrX()).findFirst().get();
        int location = board.getAvailableMoves().stream().findFirst().get().source();
        Board.TicketBoard ticketBoard = board.getPlayerTickets(mrXPiece).get();
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
