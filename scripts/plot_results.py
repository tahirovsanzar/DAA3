"""Generate two dependency-free SVG plots from benchmark.csv."""
import csv
import math
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ROWS = list(csv.DictReader((ROOT / "results/tables/benchmark.csv").open(encoding="utf-8")))
OUT = ROOT / "results/plots"
OUT.mkdir(parents=True, exist_ok=True)
ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Dijkstra", "Topological_DAG", "Topological_Cyclic"]
COLORS = ["#1764ab", "#db6a22", "#238c6e", "#a7356a", "#7551a7", "#9b7930"]


def plot(field, label, filename):
    width, height = 940, 550
    left, top, right, bottom = 95, 60, 225, 75
    plot_w, plot_h = width - left - right, height - top - bottom
    xs = sorted({int(row[field]) for row in ROWS})
    values = [float(row["avg_nanoseconds"]) / 1e6 for row in ROWS]
    low = math.floor(math.log10(min(values)))
    high = math.ceil(math.log10(max(values)))

    def x_pos(x):
        return left + (math.log10(x) - math.log10(xs[0])) / (math.log10(xs[-1]) - math.log10(xs[0])) * plot_w

    def y_pos(ms):
        return top + (high - math.log10(ms)) / (high - low) * plot_h

    lines = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {width} {height}">',
             '<rect width="100%" height="100%" fill="white"/>',
             '<style>text{font-family:Arial,sans-serif;fill:#243448} .title{font-size:22px;font-weight:bold} .axis{font-size:14px} .legend{font-size:14px}</style>',
             f'<text class="title" x="{left}" y="33">Average execution time vs. {label.lower()}</text>']
    for exponent in range(low, high + 1):
        ms = 10 ** exponent
        y = y_pos(ms)
        lines.append(f'<line x1="{left}" y1="{y:.1f}" x2="{left+plot_w}" y2="{y:.1f}" stroke="#dce4ec"/>')
        lines.append(f'<text class="axis" x="{left-12}" y="{y+5:.1f}" text-anchor="end">{ms:g}</text>')
    for x in xs:
        xp = x_pos(x)
        lines.append(f'<line x1="{xp:.1f}" y1="{top}" x2="{xp:.1f}" y2="{top+plot_h}" stroke="#edf1f5"/>')
        lines.append(f'<text class="axis" x="{xp:.1f}" y="{top+plot_h+25}" text-anchor="middle">{x:,}</text>')
    lines.append(f'<line x1="{left}" y1="{top}" x2="{left}" y2="{top+plot_h}" stroke="#243448"/>')
    lines.append(f'<line x1="{left}" y1="{top+plot_h}" x2="{left+plot_w}" y2="{top+plot_h}" stroke="#243448"/>')
    lines.append(f'<text class="axis" x="{left+plot_w/2:.1f}" y="{height-19}" text-anchor="middle">{label} (log scale)</text>')
    lines.append(f'<text class="axis" transform="translate(24 {top+plot_h/2:.1f}) rotate(-90)" text-anchor="middle">Time (ms, log scale)</text>')
    for index, name in enumerate(ALGORITHMS):
        color = COLORS[index]
        selected = sorted((row for row in ROWS if row["algorithm"] == name), key=lambda row: int(row[field]))
        points = [(x_pos(int(row[field])), y_pos(float(row["avg_nanoseconds"]) / 1e6)) for row in selected]
        path = " ".join(f'{x:.1f},{y:.1f}' for x, y in points)
        lines.append(f'<polyline points="{path}" fill="none" stroke="{color}" stroke-width="2.5"/>')
        for x, y in points:
            lines.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="4" fill="{color}"/>')
        legend_y = top + 20 + index * 30
        lines.append(f'<line x1="{width-right+20}" y1="{legend_y}" x2="{width-right+52}" y2="{legend_y}" stroke="{color}" stroke-width="3"/>')
        lines.append(f'<text class="legend" x="{width-right+60}" y="{legend_y+5}">{name.replace("_", " ")}</text>')
    lines.append('</svg>')
    (OUT / filename).write_text("\n".join(lines), encoding="utf-8")


plot("vertices", "Vertices", "time_vs_vertices.svg")
plot("edges", "Edges", "time_vs_edges.svg")
print("Wrote two SVG plots")
