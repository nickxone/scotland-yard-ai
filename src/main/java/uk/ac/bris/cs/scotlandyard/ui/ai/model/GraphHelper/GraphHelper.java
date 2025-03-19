package uk.ac.bris.cs.scotlandyard.ui.ai.model.GraphHelper;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;

public interface GraphHelper {
    int[] computeDistance(ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph, int source);
}
