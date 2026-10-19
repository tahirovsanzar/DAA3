package shortestpath;

import graph.WeightedGraph;
import graph.WeightedGraph.Edge;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public final class Dijkstra {
    public static final long INF = Long.MAX_VALUE;
    private record Entry(int vertex, long distance) {}
    public record Result(long[] distance, int[] previous, long edgeExaminations,
                         long relaxations, long heapComparisons) {
        public List<Integer> pathTo(int target) {
            if (target < 0 || target >= distance.length) throw new IllegalArgumentException("Invalid target");
            if (distance[target] == INF) return List.of();
            List<Integer> path = new ArrayList<>();
            for (int at = target; at != -1; at = previous[at]) path.add(at);
            Collections.reverse(path);
            return List.copyOf(path);
        }
    }
    private Dijkstra() {}

    public static Result run(WeightedGraph graph, int source) {
        graph.checkVertex(source);
        long[] distance = new long[graph.vertices()];
        int[] previous = new int[graph.vertices()];
        Arrays.fill(distance, INF);
        Arrays.fill(previous, -1);
        distance[source] = 0;
        long[] comparisons = {0};
        PriorityQueue<Entry> queue = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
            return Long.compare(a.distance(), b.distance());
        });
        queue.add(new Entry(source, 0));
        long examined = 0, relaxations = 0;
        while (!queue.isEmpty()) {
            Entry current = queue.remove();
            if (current.distance() != distance[current.vertex()]) continue;
            for (Edge edge : graph.neighbors(current.vertex())) {
                examined++;
                if (distance[current.vertex()] > Long.MAX_VALUE - edge.weight()) continue;
                long candidate = distance[current.vertex()] + edge.weight();
                if (candidate < distance[edge.to()]) {
                    distance[edge.to()] = candidate;
                    previous[edge.to()] = current.vertex();
                    queue.add(new Entry(edge.to(), candidate));
                    relaxations++;
                }
            }
        }
        return new Result(distance, previous, examined, relaxations, comparisons[0]);
    }
}
