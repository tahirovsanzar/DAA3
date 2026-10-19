package mst;

import dsu.DisjointSetUnion;
import graph.WeightedGraph;
import graph.WeightedGraph.Edge;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MSTAlgorithms {
    public record Result(long weight, boolean connected, List<Edge> tree, long edgeExaminations,
                         long comparisons, long dsuFindCalls, long dsuUnionCalls) {}
    private MSTAlgorithms() {}

    public static Result kruskal(WeightedGraph graph) {
        graph.requireUndirected();
        long[] comparisons = {0};
        List<Edge> sorted = new ArrayList<>(graph.edges());
        sorted.sort((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight(), b.weight());
        });
        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertices());
        List<Edge> tree = new ArrayList<>();
        long weight = 0, examined = 0;
        for (Edge edge : sorted) {
            examined++;
            if (dsu.union(edge.from(), edge.to())) {
                tree.add(edge);
                weight += edge.weight();
                if (tree.size() == graph.vertices() - 1) break;
            }
        }
        return new Result(weight, dsu.components() == 1, List.copyOf(tree), examined,
                          comparisons[0], dsu.findCalls(), dsu.unionCalls());
    }

    public static Result prim(WeightedGraph graph) {
        graph.requireUndirected();
        int n = graph.vertices();
        boolean[] seen = new boolean[n];
        long[] comparisons = {0};
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> tree = new ArrayList<>();
        long weight = 0, examined = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) continue;
            components++;
            seen[start] = true;
            for (Edge edge : graph.neighbors(start)) { queue.add(edge); examined++; }
            while (!queue.isEmpty()) {
                Edge edge = queue.remove();
                if (seen[edge.to()]) continue;
                seen[edge.to()] = true;
                tree.add(edge);
                weight += edge.weight();
                for (Edge next : graph.neighbors(edge.to())) { queue.add(next); examined++; }
            }
        }
        return new Result(weight, components == 1, List.copyOf(tree), examined,
                          comparisons[0], 0, 0);
    }
}
