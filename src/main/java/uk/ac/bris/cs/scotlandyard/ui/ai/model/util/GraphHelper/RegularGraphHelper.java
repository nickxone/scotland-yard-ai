package uk.ac.bris.cs.scotlandyard.ui.ai.model.util.GraphHelper;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import javafx.util.Pair;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

import java.util.Comparator;
import java.util.PriorityQueue;

public class RegularGraphHelper implements GraphHelper {
    // Dijkstra's algorithm
    @Override
    public int[] computeDistance(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, int source) {
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
                ImmutableSet<ScotlandYard.Transport> transports = graph.edgeValue(vertex, e).get();

                if (distTo[vertex] + 1 < distTo[e]) {
                    distTo[e] = distTo[vertex] + 1;
                    pq.add(new Pair<>(e, distTo[e]));
                }

            }
        }

        return distTo;
    }
}
