"""Draw the benchmark charts from the CSV file."""

import csv
from pathlib import Path

import matplotlib.pyplot as plt

project = Path(__file__).resolve().parents[1]
with (project / "results/tables/benchmark.csv").open(newline="") as file:
    results = list(csv.DictReader(file))

algorithms = ["Kruskal", "Prim", "Kosaraju", "Dijkstra", "Topological_DAG", "Topological_Cyclic"]
plot_folder = project / "results/plots"
plot_folder.mkdir(parents=True, exist_ok=True)

for x_column, x_label, filename in [
    ("vertices", "Number of vertices", "time_vs_vertices.png"),
    ("edges", "Number of edges", "time_vs_edges.png"),
]:
    plt.figure(figsize=(9, 5))
    for algorithm in algorithms:
        rows = [row for row in results if row["algorithm"] == algorithm]
        x = [int(row[x_column]) for row in rows]
        time_ms = [int(row["avg_nanoseconds"]) / 1_000_000 for row in rows]
        plt.plot(x, time_ms, marker="o", label=algorithm.replace("_", " "))

    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel(x_label)
    plt.ylabel("Average time (ms)")
    plt.title("Execution time vs. " + x_label.lower())
    plt.grid(True)
    plt.legend()
    plt.tight_layout()
    plt.savefig(plot_folder / filename, dpi=150)
    plt.close()

print("Saved two plots in results/plots")
