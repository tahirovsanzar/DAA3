import connectivity.ConnectivityAlgorithms;
import graph.DirectedGraph;
import graph.WeightedGraph;
import mst.MSTAlgorithms;
import shortestpath.Dijkstra;
import java.util.Arrays;

public final class Main {
    public static void main(String[] args) {
        String[] districts = {"Central", "North", "East", "South", "West"};
        WeightedGraph roads = new WeightedGraph(districts.length, false);
        roads.addEdge(0, 1, 4);
        roads.addEdge(0, 2, 2);
        roads.addEdge(1, 2, 1);
        roads.addEdge(1, 3, 5);
        roads.addEdge(2, 3, 8);
        roads.addEdge(2, 4, 10);
        roads.addEdge(3, 4, 2);
        var kruskal = MSTAlgorithms.kruskal(roads);
        var prim = MSTAlgorithms.prim(roads);
        System.out.println("Districts: " + Arrays.toString(districts));
        System.out.println("Connected: " + kruskal.connected());
        System.out.println("Kruskal MST cost: " + kruskal.weight());
        System.out.println("Prim MST cost: " + prim.weight());
        System.out.println("MST roads: " + kruskal.tree());

        var routes = Dijkstra.run(roads, 0);
        for (int target = 0; target < districts.length; target++)
            System.out.println("Central to " + districts[target] + ": distance="
                    + routes.distance()[target] + ", path=" + routes.pathTo(target));

        DirectedGraph services = new DirectedGraph(5);
        services.addEdge(0, 1);
        services.addEdge(0, 2);
        services.addEdge(1, 3);
        services.addEdge(2, 3);
        services.addEdge(3, 4);
        System.out.println("Service installation order: " + ConnectivityAlgorithms.topologicalSort(services).order());
        System.out.println("Directed SCCs: " + ConnectivityAlgorithms.kosaraju(services).groups());
    }
}
