# Assignment 3 - Smart City Infrastructure and Route Planning

## Overview

I built a small Java program for planning connections between city districts. Each district is a vertex, and a road is an edge with a cost or distance. Two-way roads go into an undirected weighted graph. One-way connections and service dependencies go into a directed graph. The program can find a cheap road network, check which places are connected, order dependent services, and find routes from one district to another.

## Build and run

I used Java 17. Run these commands from the project folder in PowerShell (or use the `Main`, `Tests`, and `Benchmark` run buttons in IntelliJ). Python 3 is only needed if you want to redraw the plots after running the benchmark.

```powershell
$sources = Get-ChildItem src -Recurse -Filter *.java | ForEach-Object FullName
New-Item -ItemType Directory -Force build | Out-Null
javac -d build $sources
java -cp build Main
java -cp build Tests
java -cp build Benchmark
python scripts/plot_results.py
```

`Main` shows one example city. `Tests` checks the edge cases. `Benchmark` saves its measurements to `results/tables/benchmark.csv`, and the Python script draws the plots from that file. You can also pass a different CSV path as the first argument to `Benchmark`. The times in this README are from my run; your times will probably be different.

## Implementation

| Task | How I implemented it |
|---|---|
| Minimum spanning tree | Kruskal sorts the roads by cost, then uses a disjoint-set union (DSU) to avoid cycles. The DSU uses path compression and union by size. Prim grows a tree using a priority queue. If the graph is disconnected, both algorithms return a forest and set `connected=false`. |
| Topological order | Kahn's algorithm starts with vertices that have no incoming edges. If it cannot process every vertex, there is a cycle, so it returns no order. |
| Strongly connected components | Kosaraju runs two iterative depth-first searches. Between the passes, it reverses all edges. Each output group contains vertices that can reach one another. |
| Shortest paths | Dijkstra starts at a chosen district and keeps the next closest candidate in a priority queue. It returns distances and the actual paths. Unreachable vertices get `INF` and an empty path. |

I used `long` for total costs and distances to avoid ordinary integer overflow. Dijkstra also checks before adding a weight to a distance. The graph rejects negative weights when an edge is added. When several roads have the same weight, there can be more than one correct MST. That is why I compare the **total weight** from Kruskal and Prim, not their exact edge lists.

## Complexity analysis

These bounds assume adjacency lists and Java's binary-heap priority queue. `V` means vertices and `E` means input edges. Each undirected road is stored twice in the adjacency lists, once for each direction.

| Algorithm | Time | Additional space | Effect of graph size |
|---|---|---|---|
| DSU `find`, `union` | Amortized `O(alpha(V))` per operation with path compression and union by size | `O(V)` | The inverse Ackermann factor grows so slowly that repeated connectivity checks are near constant time. Union by size limits tree depth; compression flattens paths on subsequent finds. |
| Kruskal | `O(E log E + E alpha(V))` | `O(V+E)` | Sorting dominates as edge count grows; DSU processes selected candidates until the tree is complete or edges run out. |
| Prim | `O(E log E)` with this lazy edge heap | `O(V+E)` | Every undirected adjacency arc can enter the heap. A denser graph increases heap work. |
| Kosaraju SCC | `O(V+E)` | `O(V+E)` | Each vertex and directed edge is processed in each DFS pass; building the transpose also traverses all edges. |
| Kahn topological ordering | `O(V+E+V log V)` with a priority queue | `O(V)` beyond the input graph | Indegree calculation scans `E` edges; each vertex enters/leaves the heap once. A FIFO queue would give `O(V+E)` without deterministic smallest-ID tie breaking. |
| Dijkstra | `O((V+E) log E)` with lazy duplicate heap entries | `O(V+E)` | Reachable edges are relaxed once; more edges can create more heap entries. With a decrease-key heap the usual bound is `O((V+E) log V)`. |

Dijkstra needs non-negative weights. With a negative road weight, a district that already looks finished could later get a shorter route, so the greedy step would no longer be reliable. I reject negative weights in this project. If they were required, I would use Bellman-Ford instead.

## Experimental setup

| Size | Vertices | Edges |
|---|---:|---:|
| Small | 10 | 30 |
| Medium | 100 | 500 |
| Large | 1,000 | 5,000 |

I used the fixed seed `20261019 + V`, so each size generates the same graph whenever the experiment is repeated. There are exactly `E` different edges and no self-loops. For the road graph, I add a spanning tree first to make sure every district is connected, then add random roads. Road weights are integers from 1 to 100. For the directed DAG, edges always go from a smaller vertex ID to a larger one. For the cyclic case, I add `0 -> 1 -> 0` before the random edges, so that graph definitely contains a cycle. Dijkstra starts at district 0.

For each size, I ran an algorithm three times to warm it up, then timed five more runs with `System.nanoTime()`. The table shows the average of those five runs. I generated the graphs before timing, so the numbers measure the algorithm itself rather than random graph creation. The CSV also records edge examinations and another operation count: sort comparisons plus DSU `find` calls for Kruskal, heap comparisons for Prim, and relaxations plus heap comparisons for Dijkstra. These are different kinds of work, so the counts are best compared **within** an algorithm as the graph grows. On a cyclic graph, topological sorting may stop early and examine fewer edges.

I ran the recorded experiment on Windows with Microsoft OpenJDK 17.0.20.1 and an Intel Core i5-13420H. The smallest timings are only a few hundredths of a millisecond, so background activity and Java warm-up can noticeably change them. Also, both `V` and `E` increase between the three sizes. The plots show how these particular test cases scale, not the separate effect of changing only one variable.

## Results

The table below is from one run. Times are in milliseconds; the unrounded measurements and counts are in [benchmark.csv](results/tables/benchmark.csv).

| Algorithm | V | E | Avg ms | Edges scanned | Other ops | Result |
|---|---:|---:|---:|---:|---:|---|
| Kruskal | 10 | 30 | 0.075 | 24 | 196 | MST weight 156 |
| Prim | 10 | 30 | 0.082 | 60 | 489 | MST weight 156 |
| Topological DAG | 10 | 30 | 0.052 | 60 | 0 | DAG |
| Topological cyclic | 10 | 30 | 0.015 | 30 | 0 | Cycle detected |
| Kosaraju | 10 | 30 | 0.075 | 90 | 0 | 1 SCC |
| Dijkstra | 10 | 30 | 0.056 | 60 | 99 | Source 0 |
| Kruskal | 100 | 500 | 0.391 | 309 | 5,001 | MST weight 1,281 |
| Prim | 100 | 500 | 0.517 | 1,000 | 17,314 | MST weight 1,281 |
| Topological DAG | 100 | 500 | 0.266 | 1,000 | 0 | DAG |
| Topological cyclic | 100 | 500 | 0.052 | 502 | 0 | Cycle detected |
| Kosaraju | 100 | 500 | 0.489 | 1,500 | 0 | 3 SCCs |
| Dijkstra | 100 | 500 | 0.341 | 1,000 | 2,393 | Source 0 |
| Kruskal | 1,000 | 5,000 | 3.054 | 3,252 | 62,794 | MST weight 12,753 |
| Prim | 1,000 | 5,000 | 2.599 | 10,000 | 240,820 | MST weight 12,753 |
| Topological DAG | 1,000 | 5,000 | 0.819 | 10,000 | 0 | DAG |
| Topological cyclic | 1,000 | 5,000 | 0.112 | 5,026 | 0 | Cycle detected |
| Kosaraju | 1,000 | 5,000 | 1.773 | 15,000 | 0 | 8 SCCs |
| Dijkstra | 1,000 | 5,000 | 3.697 | 10,000 | 37,174 | Source 0 |

![Execution time versus vertices](results/plots/time_vs_vertices.svg)

![Execution time versus edges](results/plots/time_vs_edges.svg)

I used logarithmic axes so the small and large cases can be seen on the same plots. Both plots use the same benchmark data, once against vertices and once against edges.

## Comparison and discussion

The first thing I checked was whether Kruskal and Prim agreed. They did: the MST weights were 156, 1,281, and 12,753 for the three graphs. Kruskal was a little faster on the medium graph, while Prim was faster on the large one. Prim looked at both directions of every road, but Kruskal paid for sorting all the roads before it could start selecting them. With only five timed runs, I would not treat the small differences as a general rule that one algorithm is always faster.

Kosaraju's time increased from 0.075 ms on the small graph to 1.773 ms on the large graph. It examined each directed edge three times overall: in the first search, while reversing edges, and in the second search. That matches the expected linear work in `V+E`. The generated graphs had 1, 3, and 8 SCCs; those counts come from this seed and are not fixed properties of the sizes.

Topological sorting produced an order for every generated DAG and rejected every cyclic graph. The cyclic cases were faster because the algorithm stopped before it could process all vertices. Dijkstra examined both directions of each road, and its heap work grew along with the graph. Every district in these benchmark road graphs was reachable; I tested the unreachable case separately.

## Testing

I included a `Tests` class with 22 assertions. It checks a small road graph with equal weights and more than one possible MST, a disconnected graph, a single vertex, repeated DSU unions, valid ordering for a DAG, cycle detection, SCC membership, exact shortest distances and paths, an unreachable vertex, and rejection of a negative weight. The benchmark also checks its generated graphs: both MST methods must agree, the DAG order must respect every edge, the cyclic graph must be rejected, every vertex must appear in one SCC, and all road districts must be reachable from district 0.

## Conclusion

For road construction, either Kruskal or Prim gives the minimum total cost when the city graph is connected. If it is not connected, the result is a forest and the program reports that clearly. Kahn's algorithm fits service dependencies when they do not contain a cycle. Kosaraju is useful for finding groups connected in both directions. Dijkstra is the route-planning choice here because road distances are non-negative. The experiment did not show one MST algorithm winning at every size, which is a good reminder to consider both the graph and the implementation when choosing between them.
