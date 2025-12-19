package Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Dijkstras {
	private double[]    distance;
	private boolean[]   visited;
	private int[]       parent;
	double[][]          graph;
	int                 n;
	int                 start;
	int                 end;
	static final double INF         = Double.MAX_VALUE;

	Dijkstras(double[][] graph, int start, int end) {
		this.graph  = graph;
		this.n      = graph.length;
		this.start  = start;
		this.end    = end;

		distance    = new double[n];
		visited     = new boolean[n];
		parent      = new int[n];

		init();
		generatePath();
	}

	double getDistance() {
		return 	distance[end] == INF ? -1.0 : distance[end];
	}

	ArrayList<Integer> getPath() {
		return (ArrayList<Integer>) pathTo(start, end);
	}

	private void init() {
		Arrays.fill(distance, INF);
		Arrays.fill(visited, false);
		Arrays.fill(parent, -1);
		distance[start] = 0;
	}

	private void generatePath() {
		for (int i = 0; i < n; i++) {
			// unvisited node with smallest distance
			int     u       = -1;
			double  best    = INF;

			for (int v = 0; v < n; v++) {
				if (!visited[v] && distance[v] < best) {
					best = distance[v];
					u = v;
				}
			}

			// done?
			if (u == -1 || u == end)
				break;

			visited[u] = true;

			// find the shortest distance and parent
			for (int v = 0; v < n; v++) {
				double w   = graph[u][v];

				if (w < INF && !visited[v]) {
					double nd  = distance[u] + w;
					if (nd < distance[v]) {
						distance[v] = nd;
						parent[v]   = u;
					}
				}
			}
		}
	}

	private List<Integer> pathTo(int start, int target) {
		ArrayList<Integer>   path    = new ArrayList<>();

		if (distance[target] == INF)
			return List.of();

		for (int v = target; v != -1; v = parent[v]) {
			path.add(v);
		}
		Collections.reverse(path);

		if (path.get(0) != start) return List.of(); // uh oh it's broken
		return path;
	}
}
