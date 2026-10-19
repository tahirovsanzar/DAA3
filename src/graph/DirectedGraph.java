package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DirectedGraph {
    private final List<List<Integer>> adjacency = new ArrayList<>();
    private int edgeCount;

    public DirectedGraph(int vertices) {
        if (vertices < 1) throw new IllegalArgumentException("At least one vertex is required");
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }
    public void addEdge(int from, int to) {
        check(from);
        check(to);
        adjacency.get(from).add(to);
        edgeCount++;
    }
    public int vertices() { return adjacency.size(); }
    public int edges() { return edgeCount; }
    public List<Integer> neighbors(int vertex) {
        check(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }
    private void check(int vertex) {
        if (vertex < 0 || vertex >= vertices()) throw new IllegalArgumentException("Invalid vertex: " + vertex);
    }
}
