package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class WeightedGraph {
    public record Edge(int from, int to, int weight) {}
    private final int vertices;
    private final boolean directed;
    private final List<Edge> edges = new ArrayList<>();
    private final List<List<Edge>> adjacency = new ArrayList<>();

    public WeightedGraph(int vertices, boolean directed) {
        if (vertices < 1) throw new IllegalArgumentException("At least one vertex is required");
        this.vertices = vertices;
        this.directed = directed;
        for (int i = 0; i < vertices; i++) adjacency.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);
        if (weight < 0) throw new IllegalArgumentException("Negative weights are unsupported");
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        if (!directed) adjacency.get(to).add(new Edge(to, from, weight));
    }

    public int vertices() { return vertices; }
    public boolean directed() { return directed; }
    public List<Edge> edges() { return Collections.unmodifiableList(edges); }
    public List<Edge> neighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }
    public void requireUndirected() {
        if (directed) throw new IllegalArgumentException("Undirected graph required");
    }
    public void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertices) throw new IllegalArgumentException("Invalid vertex: " + vertex);
    }
}
