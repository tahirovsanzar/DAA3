package graph;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class GraphFactory {
    private GraphFactory() {}
    private static long key(int a, int b) { return ((long) a << 32) | (b & 0xffffffffL); }

    public static WeightedGraph connectedUndirected(int vertices, int edges, long seed) {
        if (edges < vertices - 1 || (long) edges > (long) vertices * (vertices - 1) / 2)
            throw new IllegalArgumentException("Impossible edge count");
        WeightedGraph graph = new WeightedGraph(vertices, false);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        for (int v = 1; v < vertices; v++) addUndirected(graph, used, v, random.nextInt(v), random);
        while (graph.edges().size() < edges) {
            int a = random.nextInt(vertices), b = random.nextInt(vertices);
            if (a != b && !used.contains(key(Math.min(a, b), Math.max(a, b))))
                addUndirected(graph, used, a, b, random);
        }
        return graph;
    }

    private static void addUndirected(WeightedGraph graph, Set<Long> used, int a, int b, Random random) {
        used.add(key(Math.min(a, b), Math.max(a, b)));
        graph.addEdge(a, b, 1 + random.nextInt(100));
    }

    public static DirectedGraph directed(int vertices, int edges, long seed, boolean dag) {
        long maximum = dag ? (long) vertices * (vertices - 1) / 2 : (long) vertices * (vertices - 1);
        if (edges < (dag ? 0 : 2) || edges > maximum || (!dag && vertices < 2))
            throw new IllegalArgumentException("Impossible edge count");
        DirectedGraph graph = new DirectedGraph(vertices);
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        if (!dag) { graph.addEdge(0, 1); graph.addEdge(1, 0); used.add(key(0, 1)); used.add(key(1, 0)); }
        while (graph.edges() < edges) {
            int a = random.nextInt(vertices), b = random.nextInt(vertices);
            if (a == b) continue;
            if (dag && a > b) { int temp = a; a = b; b = temp; }
            if (used.add(key(a, b))) graph.addEdge(a, b);
        }
        return graph;
    }
}
