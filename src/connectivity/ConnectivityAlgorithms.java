package connectivity;

import graph.DirectedGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public final class ConnectivityAlgorithms {
    public record Topology(List<Integer> order, boolean dag, long edgeExaminations) {}
    public record Components(List<List<Integer>> groups, long edgeExaminations) {}
    private ConnectivityAlgorithms() {}

    public static Topology topologicalSort(DirectedGraph graph) {
        int n = graph.vertices();
        int[] indegree = new int[n];
        long examined = 0;
        for (int u = 0; u < n; u++) for (int v : graph.neighbors(u)) { indegree[v]++; examined++; }
        PriorityQueue<Integer> ready = new PriorityQueue<>();
        for (int u = 0; u < n; u++) if (indegree[u] == 0) ready.add(u);
        List<Integer> order = new ArrayList<>();
        while (!ready.isEmpty()) {
            int u = ready.remove();
            order.add(u);
            for (int v : graph.neighbors(u)) {
                examined++;
                if (--indegree[v] == 0) ready.add(v);
            }
        }
        return new Topology(order.size() == n ? List.copyOf(order) : List.of(), order.size() == n, examined);
    }

    public static Components kosaraju(DirectedGraph graph) {
        int n = graph.vertices();
        boolean[] seen = new boolean[n];
        List<Integer> finish = new ArrayList<>(n);
        long examined = 0;
        for (int root = 0; root < n; root++) {
            if (seen[root]) continue;
            ArrayDeque<int[]> stack = new ArrayDeque<>();
            seen[root] = true;
            stack.push(new int[] {root, 0});
            while (!stack.isEmpty()) {
                int[] frame = stack.peek();
                List<Integer> neighbors = graph.neighbors(frame[0]);
                if (frame[1] == neighbors.size()) { finish.add(frame[0]); stack.pop(); continue; }
                int v = neighbors.get(frame[1]++);
                examined++;
                if (!seen[v]) { seen[v] = true; stack.push(new int[] {v, 0}); }
            }
        }
        List<List<Integer>> reverse = new ArrayList<>();
        for (int i = 0; i < n; i++) reverse.add(new ArrayList<>());
        for (int u = 0; u < n; u++) for (int v : graph.neighbors(u)) { reverse.get(v).add(u); examined++; }
        Arrays.fill(seen, false);
        List<List<Integer>> groups = new ArrayList<>();
        for (int i = finish.size() - 1; i >= 0; i--) {
            int root = finish.get(i);
            if (seen[root]) continue;
            List<Integer> group = new ArrayList<>();
            ArrayDeque<Integer> stack = new ArrayDeque<>();
            stack.push(root);
            seen[root] = true;
            while (!stack.isEmpty()) {
                int u = stack.pop();
                group.add(u);
                for (int v : reverse.get(u)) {
                    examined++;
                    if (!seen[v]) { seen[v] = true; stack.push(v); }
                }
            }
            group.sort(Integer::compareTo);
            groups.add(List.copyOf(group));
        }
        return new Components(List.copyOf(groups), examined);
    }
}
