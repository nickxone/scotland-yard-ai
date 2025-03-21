package model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Move;
//import uk.ac.bris.cs.scotlandyard.model.ParameterisedModelTestBase;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.AI.MrX.*;
import uk.ac.bris.cs.scotlandyard.ui.ai.AI.Detectives.*;

import static org.assertj.core.api.Assertions.assertThat;

import io.atlassian.fugue.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24MOVES;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

/**
 * Tests related to whether the game state reports game over correctly
 * <br>
 * tests here to work properly!</b>
 */

public class SimulationTest {
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
    public void mrXvsDetectiveFirstSimulation() {
        MrXAI aiRecursive = new MrXAI();
        DetectivesAI detectivesAI = new DetectivesAI();
        detectivesAI.onStart();
        MyGameStateFactory myGameStateFactory = new MyGameStateFactory();
        var mrX = new Player(MRX, defaultMrXTickets(), 78);
        var red = new Player(RED, defaultDetectiveTickets(), 112);
        var green = new Player(GREEN, defaultDetectiveTickets(), 117);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 26);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 94);

        Board.GameState state = myGameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, blue, red, white, green, yellow);

        Pair<Long, TimeUnit> pair = new Pair<>(1500L, TimeUnit.MINUTES);

        while (state.getWinner().isEmpty()) {
            Move mr1AiMove = aiRecursive.pickMove(state, pair);
            state = state.advance(mr1AiMove);
            System.out.println("MrX = move" + mr1AiMove);

            while (state.getWinner().isEmpty() && !state.getAvailableMoves().stream().findFirst().get().commencedBy().isMrX()) {
                Move detectiveAI = detectivesAI.pickMove(state, pair);
                System.out.println("Detective move= " + detectiveAI);
                state = state.advance(detectiveAI);
            }
        }
        System.out.println(state.getWinner());
        assertThat(!state.getWinner().isEmpty()).isTrue(); // check that there is a winner at the end of the game

    }
    @Test
    public void mrXvsDetectiveSecondSimulation() {
        MrXAI aiRecursive = new MrXAI();
        DetectivesAI detectivesAI = new DetectivesAI();
        detectivesAI.onStart();
        MyGameStateFactory myGameStateFactory = new MyGameStateFactory();
        var mrX = new Player(MRX, defaultMrXTickets(), 47);
        var red = new Player(RED, defaultDetectiveTickets(), 123);
        var green = new Player(GREEN, defaultDetectiveTickets(), 155);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 112);
        var white = new Player(WHITE, defaultDetectiveTickets(), 29);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 35);

        Board.GameState state = myGameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, blue, red, white, green, yellow);

        Pair<Long, TimeUnit> pair = new Pair<>(1500L, TimeUnit.MINUTES);

        while (state.getWinner().isEmpty()) {
            Move mr1AiMove = aiRecursive.pickMove(state, pair);
            state = state.advance(mr1AiMove);
            System.out.println("MrX = move" + mr1AiMove);

            while (state.getWinner().isEmpty() && !state.getAvailableMoves().stream().findFirst().get().commencedBy().isMrX()) {
                Move detectiveAI = detectivesAI.pickMove(state, pair);
                System.out.println("Detective move= " + detectiveAI);
                state = state.advance(detectiveAI);
            }
        }
        System.out.println(state.getWinner());
        assertThat(!state.getWinner().isEmpty()).isTrue(); // check that there is a winner at the end of the game

    }

    @Test
    public void mrXvsDetectiveThirdSimulation() {
        MrXAI aiRecursive = new MrXAI();
        DetectivesAI detectivesAI = new DetectivesAI();
        detectivesAI.onStart();
        MyGameStateFactory myGameStateFactory = new MyGameStateFactory();
        var mrX = new Player(MRX, defaultMrXTickets(), 78);
        var red = new Player(RED, defaultDetectiveTickets(), 112);
        var green = new Player(GREEN, defaultDetectiveTickets(), 117);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 26);
        var white = new Player(WHITE, defaultDetectiveTickets(), 50);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 94);

        Board.GameState state = myGameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, blue, red, white, green, yellow);

        Pair<Long, TimeUnit> pair = new Pair<>(1500L, TimeUnit.MINUTES);

        while (state.getWinner().isEmpty()) {
            Move mr1AiMove = aiRecursive.pickMove(state, pair);
            state = state.advance(mr1AiMove);
            System.out.println("MrX = move" + mr1AiMove);

            while (state.getWinner().isEmpty() && !state.getAvailableMoves().stream().findFirst().get().commencedBy().isMrX()) {
                Move detectiveAI = detectivesAI.pickMove(state, pair);
                System.out.println("Detective move= " + detectiveAI);
                state = state.advance(detectiveAI);
            }
        }
        System.out.println(state.getWinner());
        assertThat(!state.getWinner().isEmpty()).isTrue(); // check that there is a winner at the end of the game

    }
}

