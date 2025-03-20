package uk.ac.bris.cs.scotlandyard.ui.ai.model.AIGameState;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class AIGameState implements Board.GameState {
    private GameSetup setup;
    private ImmutableSet<Piece> remaining;
    private ImmutableList<LogEntry> log;
    private Player mrX;
    private List<Player> detectives;
    private ImmutableSet<Move> moves;
    private ImmutableSet<Piece> winner;

    public AIGameState(
            final GameSetup setup,
            final ImmutableSet<Piece> remaining,
            final ImmutableList<LogEntry> log,
            final Player mrX,
            final List<Player> detectives) {

        this.winner = ImmutableSet.<Piece>builder().build();
        this.setup = setup;
        this.remaining = remaining;
        this.log = log;
        this.mrX = mrX;
        this.detectives = detectives;
        this.moves = initMoves(this.remaining);

        checkValidState();
        checkWinner();
    }

    private boolean detectiveHasTicket(Object ticket) {
        return this.detectives.stream()
                .anyMatch(
                        det -> {
                            Integer ticketNumber = det.tickets().get((ScotlandYard.Ticket) ticket);
                            return ticketNumber != null && ticketNumber != 0;
                        });

    }

    private boolean hasDuplicateDetectives() {
        Set<Player> players = new HashSet<>();
        return this.detectives.stream()
                .anyMatch(player -> !players.add(player));
    }

    private boolean hasOverlap() {
        Set<Integer> locations = new HashSet<>();
        return this.detectives.stream()
                .map(Player::location)
                .anyMatch(location -> !locations.add(location));
    }

    private void checkValidState() throws IllegalArgumentException, NullPointerException {
        if (this.setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty!");
        if (this.setup.moves.isEmpty()) throw new IllegalArgumentException("Moves list is empty!");
        if (this.detectives.isEmpty()) throw new IllegalArgumentException("Detectives list is empty!");
        if (this.mrX == null) throw new NullPointerException("MrX is null!");
        if (detectiveHasTicket(ScotlandYard.Ticket.DOUBLE) || detectiveHasTicket(ScotlandYard.Ticket.SECRET))
            throw new IllegalArgumentException("Detective has wrong ticket!");
        if (hasDuplicateDetectives()) throw new IllegalArgumentException("Game contains duplicate players!");
        if (hasOverlap()) throw new IllegalArgumentException("There is an overlap between locations!");
    }

    private void checkWinner() {
//            Detectives win if:
//            A detective finish a move on the same station as Mr X
        Optional<Player> countWinners = detectives
                .stream()
                .filter(d -> d.location() == mrX.location())
                .findFirst();

        if (countWinners.isPresent()) {
            winner = detectives
                    .stream()
                    .map(Player::piece)
                    .collect(ImmutableSet.toImmutableSet());
        }
        // get tickets count of all detectives
        int count = detectives
                .stream()
                .map(
                        d -> d
                                .tickets()
                                .values()
                                .stream()
                                .mapToInt(i -> i)
                                .sum()
                )
                .mapToInt(i -> i)
                .sum();
        // if the sum of all detectives' tickets is 0 then winner is MrX
        if (count == 0) {
            winner = ImmutableSet.<Piece>builder().add(this.mrX.piece()).build();
        }

        //  There are no unoccupied stations for Mr X to travel to
        if (winner.isEmpty()) {
            if (remaining.contains(this.mrX.piece()) && this.moves.isEmpty()) {
                winner = detectives
                        .stream()
                        .map(Player::piece)
                        .collect(ImmutableSet.toImmutableSet());
            } else if (this.log.size() == setup.moves.size() && remaining.contains(this.mrX.piece())) {
                // game over after all moves used
                winner = ImmutableSet.<Piece>builder().add(this.mrX.piece()).build();
                // if remaining contains detectives, they should do the next move, game is not over
//                if (remaining.contains(this.mrX.piece())) {
//                    // else, remaining contains mr. X, moves should be empty, game is over
//                    this.moves = ImmutableSet.<Move>builder().build();
//                }
            }
        }
//        else {
//            // if the winner is detected, moves should be empty, game is over
//            this.moves = ImmutableSet.<Move>builder().build();
//        }
    }

    @Nonnull
    @Override
    public GameState advance(Move move) {
//             Move.FunctionalVisitor
        if (!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);
        return move.accept(new Move.Visitor<GameState>() {
            // Compute the state of MrX's move that should be added to the log
            private LogEntry hideOrReveal(ScotlandYard.Ticket ticket, int destination, ImmutableList<LogEntry> newLog) {
                return !setup.moves.get(newLog.size()) ?
                        LogEntry.hidden(ticket) :
                        LogEntry.reveal(ticket, destination);
            }

            // Compute the state of MrX's move that should be added to the log
            private ImmutableList<LogEntry> updateLog(LogEntry addToLog, ImmutableList<LogEntry> newLog) {
                return ImmutableList.<LogEntry>builder()
                        .addAll(newLog)
                        .add(addToLog)
                        .build();
            }

            //Adding detectives to the remaining set
            private ImmutableSet<Piece> addDetectivesToRemaining() {
                return detectives.stream()
                        .map(Player::piece)
                        .collect(ImmutableSet.toImmutableSet());
            }

            @Override
            public GameState visit(Move.SingleMove move) {
                ImmutableSet<Piece> newRemaining;
                Player newMrX;
                if (move.commencedBy().isMrX()) {
                    LogEntry hideOrReveal1 = hideOrReveal(move.ticket, move.destination, log);
                    ImmutableList<LogEntry> newLog = updateLog(hideOrReveal1, log);

                    // Take used ticket from the MrX and move them to the new destination
                    newMrX = mrX.use(move.ticket).at(move.destination);

                    newRemaining = addDetectivesToRemaining();

                    return new AIGameState(setup, newRemaining, newLog, newMrX, detectives);
                } else {
                    // Find the detective who made the current move
                    Optional<Player> detective = detectives
                            .stream()
                            .filter(det -> det.piece().equals(move.commencedBy()))
                            .findFirst();
                    newRemaining = remaining.stream()
                            .filter(p -> !p.equals(move.commencedBy()))
                            .collect(ImmutableSet.toImmutableSet());
                    // Update detectives
                    ImmutableList<Player> updatedDetectives = ImmutableList.<Player>builder()
                            .addAll(detectives
                                    .stream()
                                    .filter(det -> !det.equals(detective.get()))
                                    .collect(ImmutableList.toImmutableList())
                            )
                            .add(detective.get().use(move.ticket).at(move.destination))
                            .build();

                    // Take the used ticket from the detective and give it to Mr X
                    newMrX = mrX.give(move.ticket);

                    // If `remaining` is empty, add Mr.X
                    if (initMoves(newRemaining).isEmpty()) {
                        // go to the next round
                        newRemaining = ImmutableSet.<Piece>builder()
                                .add(mrX.piece())
                                .build();
                    }

                    return new AIGameState(setup, newRemaining, log, newMrX, updatedDetectives);
                }
            }

            @Override
            public GameState visit(Move.DoubleMove move) {
                LogEntry hideOrReveal1 = hideOrReveal(move.ticket1, move.destination1, log);
                // Add first move to the log
                ImmutableList<LogEntry> newLog = updateLog(hideOrReveal1, log);

                // Add second move to the log
                LogEntry hideOrReveal2 = hideOrReveal(move.ticket2, move.destination2, newLog);
                newLog = updateLog(hideOrReveal2, newLog);

                Player newMrX = mrX.use(move.ticket1).use(move.ticket2)
                        .at(move.destination1).at(move.destination2)
                        .use(ScotlandYard.Ticket.DOUBLE);

                ImmutableSet<Piece> newRemaining = addDetectivesToRemaining();

                return new AIGameState(setup, newRemaining, newLog, newMrX, detectives);

            }
        });
    }

    @Nonnull
    @Override
    public GameSetup getSetup() {
        return this.setup;
    }

    @Nonnull
    @Override
    public ImmutableSet<Piece> getPlayers() {
        return ImmutableSet.<Piece>builder()
                .addAll(this.detectives
                        .stream()
                        .map(det -> det.piece())
                        .collect(ImmutableSet.toImmutableSet())
                )
                .add(this.mrX.piece())
                .build();
    }

    @Nonnull
    @Override
    public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
        return detectives
                .stream()
                .filter(det -> det.piece().equals(detective))
                .findFirst()
                .map(Player::location); // Optional.map() convert player to location (it is not Collection.map() !!!)
    }

    @Nonnull
    @Override
    public Optional<TicketBoard> getPlayerTickets(Piece piece) {
        try {
            if (!getPlayers().contains(piece)) throw new NullPointerException();
            return Optional.of(new TicketBoard() {

                @Override
                public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
                    Integer ticketNumber = null;
                    if (piece.isMrX()) {
                        ticketNumber = mrX.tickets().get(ticket);
                    } else {
                        ticketNumber = detectives.stream()
                                .filter(det -> det.piece().equals(piece))
                                .findFirst()
                                .map(s -> s.tickets().get(ticket))
                                .get();

                    }
                    if (ticketNumber != null) return ticketNumber;
                    throw new NullPointerException();
                }
            });
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    @Nonnull
    @Override
    public ImmutableList<LogEntry> getMrXTravelLog() {
        return this.log;
    }

    @Nonnull
    @Override
    public ImmutableSet<Piece> getWinner() {
        return this.winner;
    }

    public ImmutableSet<Piece> getRemaining() {
        return remaining;
    }

    private ImmutableSet<Move> initMoves(ImmutableSet<Piece> rem) {
        ImmutableSet.Builder<Move> availableMoves = ImmutableSet.builder();

//            Mr. X moves
        if (rem.contains(this.mrX.piece())) {
            availableMoves.addAll(makeSingleMoves(this.mrX, this.mrX.location()));
            availableMoves.addAll(makeDoubleMoves(this.mrX));
        }
//            Detectives moves
        else if (!rem.isEmpty()) {
            detectives.stream()
                    .filter(s -> rem.contains(s.piece()))
                    .forEach(det -> availableMoves.addAll(makeSingleMoves(det, det.location())));
        }
        return availableMoves.build();
    }

    @Nonnull
    @Override
    public ImmutableSet<Move> getAvailableMoves() {
        return this.moves;
    }

    private Set<Move.SingleMove> makeSingleMoves(Player player, int source) {
        Set<Move.SingleMove> singleMoves = new HashSet<>();

        for (int destination : setup.graph.adjacentNodes(source)) {

//                Check if the destination is occupied
            boolean isOccupied = this.detectives.stream().anyMatch(det -> det.location() == destination);

            if (!isOccupied) {
                for (ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())) {
//                        Check for tickets
                    if (player.hasAtLeast(t.requiredTicket(), 1)) {
                        singleMoves.add(new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination));
                    }
//                        Secret tickets
                    if (player.hasAtLeast(ScotlandYard.Ticket.SECRET, 1)) {
                        singleMoves.add(new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination));
                    }
                }
            }
        }

        return singleMoves;
    }

    private Set<Move.DoubleMove> makeDoubleMoves(Player player) {
        Set<Move.DoubleMove> doubleMoves = new HashSet<>();
        if (mrX.hasAtLeast(ScotlandYard.Ticket.DOUBLE, 1) && this.setup.moves.size() > 1) {
            Set<Move.SingleMove> firstMoves = makeSingleMoves(this.mrX, mrX.location());
            for (Move.SingleMove firstMove : firstMoves) {
                Set<Move.SingleMove> secondMoves = makeSingleMoves(this.mrX, firstMove.destination);
                for (Move.SingleMove secondMove : secondMoves) {
                    if (firstMove.ticket.equals(secondMove.ticket)) {
                        if (player.hasAtLeast(firstMove.ticket, 2))
                            doubleMoves.add(new Move.DoubleMove(player.piece(), firstMove.source(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
                    } else {
                        doubleMoves.add(new Move.DoubleMove(player.piece(), firstMove.source(), firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
                    }
                }
            }
        }

        return doubleMoves;
    }
}
