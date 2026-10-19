import connectivity.ConnectivityAlgorithms;
import graph.DirectedGraph;
import graph.GraphFactory;
import graph.WeightedGraph;
import mst.MSTAlgorithms;
import shortestpath.Dijkstra;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.Supplier;

public final class Benchmark {
    private static volatile Object sink;
    private record Measurement(double avgNanoseconds, Object result) {}
    private Benchmark() {}

    private static Measurement measure(Supplier<Object> algorithm) {
        for (int i = 0; i < 3; i++) sink = algorithm.get();
        long total = 0;
        Object result = null;
        for (int i = 0; i < 5; i++) {
            long start = System.nanoTime();
            result = algorithm.get();
            total += System.nanoTime() - start;
            sink = result;
        }
        return new Measurement(total / 5.0, result);
    }

    private static void row(StringBuilder csv, String name, int v, int e, Measurement m,
                            long operation1, long operation2, String detail) {
        csv.append(String.format(Locale.ROOT, "%s,%d,%d,%.0f,%d,%d,%s%n",
                name, v, e, m.avgNanoseconds(), operation1, operation2, detail));
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of(args.length == 0 ? "results/tables/benchmark.csv" : args[0]);
        Files.createDirectories(output.toAbsolutePath().getParent());
        StringBuilder csv = new StringBuilder("algorithm,vertices,edges,avg_nanoseconds,edge_examinations,other_operations,detail\n");
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] size : sizes) {
            int v = size[0], e = size[1];
            WeightedGraph roads = GraphFactory.connectedUndirected(v, e, 20261019L + v);
            DirectedGraph dag = GraphFactory.directed(v, e, 20261019L + v, true);
            DirectedGraph cyclic = GraphFactory.directed(v, e, 20261019L + v, false);

            Measurement kruskal = measure(() -> MSTAlgorithms.kruskal(roads));
            MSTAlgorithms.Result k = (MSTAlgorithms.Result) kruskal.result();
            row(csv, "Kruskal", v, e, kruskal, k.edgeExaminations(), k.comparisons() + k.dsuFindCalls(),
                "weight=" + k.weight() + ";connected=" + k.connected() + ";unions=" + k.dsuUnionCalls());

            Measurement prim = measure(() -> MSTAlgorithms.prim(roads));
            MSTAlgorithms.Result p = (MSTAlgorithms.Result) prim.result();
            if (!k.connected() || !p.connected() || k.weight() != p.weight())
                throw new AssertionError("MST algorithms disagree for V=" + v);
            row(csv, "Prim", v, e, prim, p.edgeExaminations(), p.comparisons(), "weight=" + p.weight());

            Measurement topoDag = measure(() -> ConnectivityAlgorithms.topologicalSort(dag));
            ConnectivityAlgorithms.Topology td = (ConnectivityAlgorithms.Topology) topoDag.result();
            if (!td.dag()) throw new AssertionError("Generated DAG contains a cycle");
            int[] position = new int[v];
            for (int i = 0; i < v; i++) position[td.order().get(i)] = i;
            for (int u = 0; u < v; u++) for (int destination : dag.neighbors(u))
                if (position[u] >= position[destination]) throw new AssertionError("Invalid DAG ordering");
            row(csv, "Topological_DAG", v, e, topoDag, td.edgeExaminations(), 0, "dag=true");

            Measurement topoCycle = measure(() -> ConnectivityAlgorithms.topologicalSort(cyclic));
            ConnectivityAlgorithms.Topology tc = (ConnectivityAlgorithms.Topology) topoCycle.result();
            if (tc.dag()) throw new AssertionError("Generated cyclic graph appears acyclic");
            row(csv, "Topological_Cyclic", v, e, topoCycle, tc.edgeExaminations(), 0, "dag=false");

            Measurement scc = measure(() -> ConnectivityAlgorithms.kosaraju(cyclic));
            ConnectivityAlgorithms.Components c = (ConnectivityAlgorithms.Components) scc.result();
            boolean[] assigned = new boolean[v];
            for (var group : c.groups()) for (int vertex : group) {
                if (assigned[vertex]) throw new AssertionError("Vertex in two SCCs");
                assigned[vertex] = true;
            }
            for (boolean present : assigned) if (!present) throw new AssertionError("Vertex missing from SCCs");
            row(csv, "Kosaraju", v, e, scc, c.edgeExaminations(), 0, "components=" + c.groups().size());

            Measurement dijkstra = measure(() -> Dijkstra.run(roads, 0));
            Dijkstra.Result d = (Dijkstra.Result) dijkstra.result();
            for (long distance : d.distance()) if (distance == Dijkstra.INF)
                throw new AssertionError("Connected road graph has unreachable district");
            row(csv, "Dijkstra", v, e, dijkstra, d.edgeExaminations(), d.relaxations() + d.heapComparisons(),
                "source=0;relaxations=" + d.relaxations());
        }
        Files.writeString(output, csv.toString());
        System.out.print(csv);
    }
}
