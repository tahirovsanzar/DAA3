import connectivity.ConnectivityAlgorithms;
import dsu.DisjointSetUnion;
import graph.DirectedGraph;
import graph.WeightedGraph;
import mst.MSTAlgorithms;
import shortestpath.Dijkstra;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Tests {
    private static int assertions;
    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }
    private static void mstCases() {
        WeightedGraph graph = new WeightedGraph(4, false);
        graph.addEdge(0, 1, 1); graph.addEdge(1, 2, 1);
        graph.addEdge(2, 3, 2); graph.addEdge(0, 3, 2);
        graph.addEdge(0, 2, 3);
        var k = MSTAlgorithms.kruskal(graph); var p = MSTAlgorithms.prim(graph);
        check(k.connected() && p.connected(), "Connected graph not detected");
        check(k.weight() == 4 && p.weight() == 4, "MST weight with duplicates and multiple optima");
        check(k.tree().size() == 3 && p.tree().size() == 3, "Wrong MST edge count");
        WeightedGraph disconnected = new WeightedGraph(4, false);
        disconnected.addEdge(0, 1, 5); disconnected.addEdge(2, 3, 2);
        check(!MSTAlgorithms.kruskal(disconnected).connected(), "Kruskal missed disconnected graph");
        check(!MSTAlgorithms.prim(disconnected).connected(), "Prim missed disconnected graph");
        WeightedGraph single = new WeightedGraph(1, false);
        check(MSTAlgorithms.kruskal(single).connected() && MSTAlgorithms.prim(single).connected(), "Single vertex MST");
        check(MSTAlgorithms.kruskal(single).weight() == 0, "Single vertex weight");
        DisjointSetUnion dsu = new DisjointSetUnion(3);
        check(dsu.union(0, 1) && !dsu.union(0, 1) && dsu.components() == 2, "DSU union");
    }
    private static void connectivityCases() {
        DirectedGraph dag = new DirectedGraph(4);
        dag.addEdge(0, 1); dag.addEdge(0, 2); dag.addEdge(2, 3); dag.addEdge(1, 3);
        var order = ConnectivityAlgorithms.topologicalSort(dag);
        check(order.dag() && order.order().size() == 4, "DAG topological order missing");
        int[] position = new int[4];
        for (int i = 0; i < 4; i++) position[order.order().get(i)] = i;
        for (int u = 0; u < 4; u++) for (int v : dag.neighbors(u))
            check(position[u] < position[v], "Invalid topological order");
        DirectedGraph cyclic = new DirectedGraph(5);
        cyclic.addEdge(0, 1); cyclic.addEdge(1, 0);
        cyclic.addEdge(1, 2); cyclic.addEdge(2, 3);
        cyclic.addEdge(3, 2); cyclic.addEdge(3, 4);
        check(!ConnectivityAlgorithms.topologicalSort(cyclic).dag(), "Cycle not detected");
        var groups = ConnectivityAlgorithms.kosaraju(cyclic).groups();
        Set<Set<Integer>> actual = new HashSet<>();
        for (List<Integer> group : groups) actual.add(new HashSet<>(group));
        check(actual.equals(Set.of(Set.of(0, 1), Set.of(2, 3), Set.of(4))), "Incorrect SCC groups");
        int[] counts = new int[5];
        for (List<Integer> group : groups) for (int vertex : group) counts[vertex]++;
        check(Arrays.equals(counts, new int[]{1, 1, 1, 1, 1}), "SCC partition invalid");
        check(ConnectivityAlgorithms.kosaraju(new DirectedGraph(1)).groups().size() == 1, "Single vertex SCC");
    }
    private static void shortestPathCases() {
        WeightedGraph graph = new WeightedGraph(5, true);
        graph.addEdge(0, 1, 10); graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2); graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 10);
        var result = Dijkstra.run(graph, 0);
        check(Arrays.equals(result.distance(), new long[]{0, 3, 1, 4, Dijkstra.INF}), "Distances incorrect");
        check(result.pathTo(3).equals(List.of(0, 2, 1, 3)), "Shortest path incorrect");
        check(result.pathTo(4).isEmpty(), "Unreachable path not empty");
        boolean rejected = false;
        try { graph.addEdge(1, 4, -1); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "Negative weight accepted");
        check(Dijkstra.run(new WeightedGraph(1, false), 0).pathTo(0).equals(List.of(0)), "Single vertex path");
    }
    public static void main(String[] args) {
        mstCases(); connectivityCases(); shortestPathCases();
        System.out.println("PASS: " + assertions + " assertions");
    }
}
