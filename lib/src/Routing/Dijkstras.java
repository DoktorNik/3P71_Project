/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * I wrote this at a cafe on a napkin instead of enjoying time with my wife - Dijkstra
 */
public class Dijkstras {
	private double[]    distance;
	private boolean[]   visited;
	private int[]       parent;
	double[][]          graph;
	int                 n;
	int                 start;
	int                 end;
	static final double INF         = Double.MAX_VALUE;

	/**
	 * Dijskstra's algorithm using adjacency matrix representation of the graph
	 * @param graph the adjacency matrix
	 * @param start start point
	 * @param end   end point
	 */
	public Dijkstras(double[][] graph, int start, int end) {
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

	/**
	 * get the shortest path
	 * @return  array list of integer ids of points
	 */
	public ArrayList<Integer> getPath() {
		return (ArrayList<Integer>) pathTo(start, end);
	}

	/**
	 * initialize. don't call this.
	 */
	private void init() {
		Arrays.fill(distance, INF);
		Arrays.fill(visited, false);
		Arrays.fill(parent, -1);
		distance[start] = 0;
	}

	/**
	 * generate the path i.e., the algorithm
	 */
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

	/**
	 * get the path from the start to the end after it has been generated
	 * @param start     integer id of starting point
	 * @param target    integer id of ending point
	 * @return          list of integer ids of points in the path
	 */
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