package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class GameTreeNode {
    //    Current's node value
    private Board.GameState gameState;
    private Move move;
    private int MrXLocation;
    private int score;

    //    Children of the current node
    private List<GameTreeNode> childNodes;

    GameTreeNode(Board.GameState gameState, Move move, int MrXLocation) {
        this.gameState = gameState;
        this.move = move;
        this.MrXLocation = MrXLocation;
        this.score = computeScore();
        this.childNodes = new ArrayList<GameTreeNode>();
        System.out.println(MrXLocation);
    }

    public Move bestMove() {
//        TODO: implement MinMax algorithm
        GameTreeNode bestNode = childNodes.get(0);
        for (GameTreeNode childNode : childNodes) {
            if (childNode.score > bestNode.score) {
                bestNode = childNode;
            }
        }
        return bestNode.move;
    }

    public void computeNextLevel() {
        for (Move move : gameState.getAvailableMoves()) {
            int newMrXLocation;
            if (move instanceof Move.DoubleMove) newMrXLocation = ((Move.DoubleMove) move).destination2;
            else newMrXLocation = ((Move.SingleMove) move).destination;
            GameTreeNode childNode = new GameTreeNode(gameState.advance(move), move, newMrXLocation);
            childNodes.add(childNode);
        }
    }

    private int computeScore() { // score current node (board state, i.e. this.gameState)
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = gameState.getSetup().graph;

//        run Dijkstra algorithm for the current position of MrX, then check distances to detectives
        int[] distances = computeDistance(graph, this.MrXLocation);
        ImmutableList<Integer> detectives = gameState.getPlayers().stream()
                .filter(player -> !player.isMrX())
                .map(player -> gameState.getDetectiveLocation((Piece.Detective) player).get())
                .collect(ImmutableList.toImmutableList());

        return detectives.stream().map(location -> distances[location]).mapToInt(Integer::intValue).sum();
    }

    private int[] computeDistance(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, int source) {
        int[] distTo = new int[graph.nodes().size() + 1];

        for (int i = 0; i < graph.nodes().size(); i++) {
            distTo[i] = Integer.MAX_VALUE;
        }
        distTo[source] = 0;

        PriorityQueue<Pair<Integer, Integer>> pq = new PriorityQueue<>(Comparator.comparing(Pair::getValue)); // Format: vertex - distance
        pq.add(new Pair<>(source, distTo[source]));
        while (!pq.isEmpty()) {
            int vertex = pq.poll().getKey();
            for (Integer e : graph.adjacentNodes(vertex)) {
                // TODO: possibly calculate distance based on weighting tickets
                ImmutableSet<ScotlandYard.Transport> transports = graph.edgeValue(vertex, e).get();
                for (ScotlandYard.Transport transport : transports) {
                    int distance = 0;
                    switch (transport) {
                        case TAXI -> distance = 1;
                        case BUS -> distance = 2;
                        case UNDERGROUND -> distance = 3;
                        case FERRY -> distance = 5;
                    }
                    if (distTo[vertex] + distance < distTo[e]) {
                        distTo[e] = distTo[vertex] + distance;
                        pq.add(new Pair<>(e, distTo[e]));
                    }
                }
            }
        }

        return distTo;
    }

}
