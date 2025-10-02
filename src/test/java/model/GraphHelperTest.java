package model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.io.Resources;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.bris.cs.scotlandyard.model.*;
import uk.ac.bris.cs.scotlandyard.model.Player;
import uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper.*;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.STANDARD24MOVES;

import static uk.ac.bris.cs.scotlandyard.model.Piece.Detective.*;
import static uk.ac.bris.cs.scotlandyard.model.Piece.MrX.MRX;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.*;

/**
 * Test GraphHelper
 */

public class GraphHelperTest {
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
    public void checkRegularGraphHelperOnExpectedValues() {
        MyGameStateFactory myGameStateFactory = new MyGameStateFactory();
        var mrX = new Player(MRX, defaultMrXTickets(), 104);
        var red = new Player(RED, defaultDetectiveTickets(), 67);
        var green = new Player(GREEN, defaultDetectiveTickets(), 94);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 141);
        var white = new Player(WHITE, defaultDetectiveTickets(), 155);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 112);

        Board.GameState state = myGameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, blue, red, white, green, yellow);

        GraphHelper graphHelper = new RegularGraphHelper();

        int[] resGraph = graphHelper.computeDistance(state.getSetup().graph, 114);

        assertThat(resGraph[1] == 6).isTrue();
        assertThat(resGraph[2] == 6).isTrue();
        assertThat(resGraph[3] == 5).isTrue();
        assertThat(resGraph[4] == 5).isTrue();
        assertThat(resGraph[5] == 7).isTrue();
        assertThat(resGraph[6] == 6).isTrue();
        assertThat(resGraph[7] == 6).isTrue();
        assertThat(resGraph[10] == 5).isTrue();
        assertThat(resGraph[13] == 4).isTrue();
        assertThat(resGraph[14] == 5).isTrue();

    }
    
    @Test
    public void checkWeightedGraphHelperOnExpectedValues() {
        MyGameStateFactory myGameStateFactory = new MyGameStateFactory();
        var mrX = new Player(MRX, defaultMrXTickets(), 104);
        var red = new Player(RED, defaultDetectiveTickets(), 67);
        var green = new Player(GREEN, defaultDetectiveTickets(), 94);
        var blue = new Player(BLUE, defaultDetectiveTickets(), 141);
        var white = new Player(WHITE, defaultDetectiveTickets(), 155);
        var yellow = new Player(YELLOW, defaultDetectiveTickets(), 112);

        Board.GameState state = myGameStateFactory.build(new GameSetup(defaultGraph, STANDARD24MOVES), mrX, blue, red, white, green, yellow);

        WeightedGraphHelper weightedGraphHelper = new WeightedGraphHelper();

        int[] resWeight = weightedGraphHelper.computeDistance(state.getSetup().graph, 114);

        assertThat(resWeight[1] == 9).isTrue();
        assertThat(resWeight[2] == 8).isTrue();
        assertThat(resWeight[3] == 7).isTrue();
        assertThat(resWeight[4] == 8).isTrue();
        assertThat(resWeight[5] == 9).isTrue();
        assertThat(resWeight[6] == 10).isTrue();
        assertThat(resWeight[7] == 11).isTrue();
        assertThat(resWeight[10] == 7).isTrue();
        assertThat(resWeight[13] == 7).isTrue();
        assertThat(resWeight[14] == 8).isTrue();

    }

}

