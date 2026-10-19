package dsu;

public final class DisjointSetUnion {
    private final int[] parent;
    private final int[] size;
    private int components;
    private long findCalls;
    private long unionCalls;

    public DisjointSetUnion(int n) {
        if (n < 1) throw new IllegalArgumentException("At least one element is required");
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }
    public int find(int x) {
        findCalls++;
        if (x < 0 || x >= parent.length) throw new IllegalArgumentException("Invalid element");
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    public boolean union(int a, int b) {
        unionCalls++;
        int rootA = find(a), rootB = find(b);
        if (rootA == rootB) return false;
        if (size[rootA] < size[rootB]) { int tmp = rootA; rootA = rootB; rootB = tmp; }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        components--;
        return true;
    }
    public int components() { return components; }
    public long findCalls() { return findCalls; }
    public long unionCalls() { return unionCalls; }
}
