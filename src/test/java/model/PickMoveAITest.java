package model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import model.NoAlphaBetaPruningAI.NoAlphaBetaPruningMrXAI;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.AI.MrX.*;

import static org.assertj.core.api.Assertions.assertThat;

import io.atlassian.fugue.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24MOVES;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

/**
 * Test that AI picks the right moves
 */

public class PickMoveAITest {
    private static ImmutableValueGraph<Integer, ImmutableSet<Transport>> defaultGraph;

    static ImmutableMap<Ticket, Integer> makeTickets(
            int taxi, int bus, int underground, int x2, int secret) {
        return ImmutableMap.of(
                TAXI, taxi,
                BUS, bus,
                UNDERGROUND, underground,
                Ticket.DOUBLE, x2,
                Ticket.SECRET, secret);
    }

    @BeforeClass
    public static void setUp() {
        try {
            defaultGraph = readGraph(Resources.toString(Resources.getResource(
                            "graph.txt"),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Unable to read game graph", e);
        }
    }

    @Test
    public void testAIsPickMoveShouldBeEqualGameState() {
        NoAlphaBetaPruningMrXAI preGeneratedTreeAI = new NoAlphaBetaPruningMrXAI();
        MrXAI MrXAI = new MrXAI();

        MyGameStateFactory myGameStateFactory = new MyGameStateFactory();
        var mrX = new Player(MRX, defaultMrXTickets(), 45);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 53);
        var red = new Player(RED, defaultDetectiveTickets(), 29);
        var white = new Player(WHITE, defaultDetectiveTickets(), 91);
        var green = new Player(GREEN, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 94);

        Board.GameState state = myGameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, blue, red, white, green, yellow);

        Pair<Long, TimeUnit> pair = new Pair<>(1500L, TimeUnit.MINUTES);
        Move firstAiMove = preGeneratedTreeAI.pickMove(state, pair);
        Move secondAiMove = MrXAI.pickMove(state, pair);
        assertThat(firstAiMove.equals(secondAiMove)).isTrue();
    }

}
