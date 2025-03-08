package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import javafx.util.Pair;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import javax.xml.crypto.dsig.TransformService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class GameTreeNode {
    //    Current's node value
    private Board.GameState node;
    private int score;

    //    Children of the current node
    private List<GameTreeNode> childNodes;

    GameTreeNode(Board.GameState node) {
        this.node = node;
        this.score = computeScore();
        this.childNodes = new ArrayList<GameTreeNode>();
    }

    public void computeNextLevel() {

        for (Move move : node.getAvailableMoves()) {
            GameTreeNode childNode = new GameTreeNode(node.advance(move));
            childNodes.add(childNode);
        }
    }

    private int computeScore() {
//        score current node (this.node)
        ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph = node.getSetup().graph;

//        run Dijkstra algorithm for the current position of MrX, then check distances to detectives
        int[] distances = computeDistance(graph, node.getAvailableMoves().stream().findFirst().get().source());

        return 0;
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
                ImmutableSet<ScotlandYard.Transport> transport = graph.edgeValue(vertex, e).get();
                if (distTo[vertex] + 1 < distTo[e]) {
                    distTo[e] = distTo[vertex] + 1;
                    pq.add(new Pair<>(e, distTo[e]));
                }
            }
        }

        return distTo;
    }

    //    Getters
    public int getScore() {
        return score;
    }

    public Board.GameState getCurrentState() {
        return node;
    }

    public List<GameTreeNode> getChildNodes() {
        return childNodes;
    }
}
