/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package lib;

import Routing.Dijkstras;
import Data.SQLite;
import Data.Point;
import Data.StreetInfo;
import Geo.Geo;
import Geo.ReverseGeocoder;
import Geo.RouteMapWriter;
import Routing.PathFinder;
import Routing.PathFinderEntry;
import Routing.PointEnrichmentService;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * path manager class is intended to be the only part of the library externally accessible
 * all interactions with library go through the manager
 */
public class PathManager {
	private SQLite sql;
	PointEnrichmentService service;
	private final PathFinder pathFinder                         = new PathFinder();
	private ArrayList<Point>[]  paths                           = new ArrayList[3];
	private String              pathDelimiter;
	private double              bufferArea;
	private ArrayList<Point>    points                          = new ArrayList<>();
	private static final String DEFAULT_DELIMITER               = " -> ";
	private static final double DEFAULT_BUFFER_AREA             = 1086.82;  // average 1086.82 max 2239.73 thanks chat
	private final double        DANGEROUS_CONDITION_THRESHOLD   = 0.25;
	private double[][]          graph;

	public enum StreetType {
		ROAD,
		PEDESTRIAN,
		FOOTWAY,
		CYCLEWAY,
		SERVICE,
		PATH,
		UNKNOWN
	}

	/**
	 * @param useCache          chat, we're doing it live!  (or not)
	 * @throws IOException      in case of failure writing out mapped paths
	 * @throws SQLException     in case of failure with database
	 */
	public PathManager(boolean useCache) throws IOException, SQLException {
		this(DEFAULT_DELIMITER, useCache, DEFAULT_BUFFER_AREA);
	}

	/**
	 *
	 * @param pathDelimiter     specify your own path delimiter for legacy reasons
	 * @param bufferArea        how far apart points can be and still connect to each other
	 * @param useCache          chat, we're doing it live!  (or not)
	 * @throws IOException      in case of failure writing out mapped paths
	 * @throws SQLException     in case of failure with database
	 */
	public PathManager(String pathDelimiter, boolean useCache, double bufferArea) throws IOException, SQLException {
		this.bufferArea     = bufferArea;
		this.pathDelimiter  = pathDelimiter;

		for (int i = 0; i < paths.length; i++)
			paths[i] = new ArrayList<>();

		init(useCache);
	}

	/**
	 * initialize the manager. don't call this
	 * @param useCache      see constructor
	 * @throws IOException see constructor
	 * @throws SQLException see constructor
	 */
	private void init(boolean useCache) throws IOException, SQLException {
		// get info from database
		sql = new SQLite();
		points  = sql.selectAllPoints();

		// set up reverse geocoder
		ReverseGeocoder geocoder = new ReverseGeocoder("3P71Project/1.0 (nik@wrinklyideas.com)");

		// set up multithread service for utilizing reverse geocoder to update street info in database
		service =
				new PointEnrichmentService(geocoder, new PointEnrichmentService.Listener() {
					@Override public void onPointUpdated(Point p, StreetInfo info) {
						sql.updateStreetInfo(p.getStreetName(), p.getStreetType(), p.getId());

						System.out.printf("Updated %-2s -> %s (%s)%n",
								p.getId(),
								p.getStreetName(),
								p.getStreetType());
					}
					@Override
					public void onIdle() {
						// No work this second; continue doing other things
					}
				});
		service.start();

		// Enqueue without blocking
		for (Point point : points) {
			if (point.getStreetType() == StreetType.UNKNOWN)
				service.enqueue(point);
		}
		//service.enqueueAll(points);

		calculateDistances(useCache);   // between points
		buildAdjacencyMatrix();         // for assignment marks

		// ALPHA TESTING 2do: get actual values
		for (Point point: points) {
			point.setCondition(Math.random());
		}
	}

	/**
	 * private function to get the road info from a point
	 * @param point the point
	 * @return array of strings
	 * [0] street name
	 * [1] street condition
	 * [2] obstacle
	 */
	private String[] getRoadInfo(Point point) {
		String[] roadInfo = new String[3];
		roadInfo[0] = point.getStreetName();
		roadInfo[1] = String.valueOf(point.getCondition());
		roadInfo[2] = "unknown";        // 2do: implement after getting actual info
		return roadInfo;
	}


	/**
	 *  get information about a section of road
	 * @param lat   - latitude of the point
	 * @param lon   - longitude of the point
	 * @return array of strings
	 * [0] street name
	 * [1] street condition
	 * [2] obstacle
	 */
	public String[] getRoadInfo(double lat, double lon) {
		for (Point point: points) {
			if (point.getLatitude() == lat && point.getLongitude() == lon)
				return getRoadInfo(point);
		}

		throw new RuntimeException("No point with latitude '" + lat + "' longitude '" + lon + "' to get street info for!");
	}

	/**
	 * get information about a section of road
	 * @param id    the database id of the road segment
	 * @return  array of strings
	 * [0] street name
	 * [1] street condition
	 * [2] obstacle
	 */
	public String[] getRoadInfo(String id) {
		for (Point point : points) {
			if (point.getId().equals(id)) {
				return getRoadInfo(point);
			}
 		}

		throw new RuntimeException("No point with id '" + id + "' to get street info for!");
	}

	/**
	 * because we need to submit the matrix
	 */
	private void buildAdjacencyMatrix() {
		int size    = points.size();
		graph       = new double[size][size];

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				graph[i][j] = Double.MAX_VALUE;
			}
		}

		for (Point p1 : points) {
			for (Point p2: points) {
				double d = distanceBetween(p1, p2);
				if (d <= bufferArea) {
					graph[p1.getIdInt()][p2.getIdInt()] = d;
					graph[p2.getIdInt()][p1.getIdInt()] = d;
				}
			}
		}
	}

	/**
	 * print all points
	 */
	public void printPoints() {
		for (Point point : points) {
			System.out.println(point.toString());
		}
	}

	/**
	 * print all connections of all points
	 */
	public void printConnections() {
		for (Point point : points) {
			point.printConnections();
		}
	}

	/**
	 * calculate distances between points, update database, and add connections to each point
	 * assuming 2-way streets
	 * @param useCache  doing it live?
	 */
	private void calculateDistances(boolean useCache) {     // for setup
		for (Point p1 : points) {
			for (Point p2 : points) {
				if (p1.equals(p2))
					continue;    // don't connect to self

				double distance = -1;
				if (useCache) {
					try {
						distance = sql.getDistance(p1.getId(), p2.getId());
					} catch (SQLException e) {
						System.err.println("SQL error in retrieving distances from database! Recalculating");
					}
				}
				if (distance < 0) {
					distance = distanceBetween(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
					sql.upsertDistance(p1.getId(), p2.getId(), distance);
					sql.upsertDistance(p2.getId(), p1.getId(), distance);       // undirected graph
				}

				// old, using adjacency matrix now
				p1.addConnection(p2, distance, bufferArea);
				p2.addConnection(p1, distance, bufferArea);     // undirected graph
			}
		}
	}

	/**
	 * find a point by database id
	 * @param id    database id
	 * @return      point with database id
	 * throws run time exception if the point doesn't exist
	 */
	private Point findPoint(String id) {
		for (Point point : points) {
			if (point.getId().equals(id)) {
				return point;
			}
		}
		throw new RuntimeException("Could not find point with id '" + id + "'");
	}

	/**
	 * build a path out of integer ids of points
	 * @param path  ArrayList of Integer IDs of points
	 * @return      ArrayList of points
	 */
	private ArrayList<Point> pathIntToPoint(ArrayList<Integer> path) {
		ArrayList<Point> pathOut = new ArrayList<>();

		for (int pId : path) {
			for (Point point : points) {
				if (point.getIdInt() == pId) {
					pathOut.add(point);
					break;
				}
			}
		}

		return pathOut;
	}

	/**
	 * build a path from a pathfinder entry (A* search node)
	 * @param destinationEntry  destination A* search node
	 * @return                  array list of points on the path
	 */
	private ArrayList<Point> getPointsFromPFE(PathFinderEntry destinationEntry) {
		ArrayList<Point>    path     = new ArrayList<>();
		String[] pathId = destinationEntry.getPath().split(pathDelimiter);

		for (String id : pathId) {
			path.add(findPoint(id));
		}

		return path;
	}

	/**
	 * builds paths using Dijkstra's for shortest path and a* for safer and simpler path
	 * @param idFrom    the database id of the starting point
	 * @param idTo      db id of destination point
	 */
	public void calculatePath(String idFrom, String idTo) {
		Point   start       = findPoint(idFrom);
		Point   end         = findPoint(idTo);

		for (int i = 0; i < 3; i++) {
			paths[i].clear();
		}

		Dijkstras dijk = new Dijkstras(graph, start.getIdInt(), end.getIdInt());
		paths[0] = pathIntToPoint(dijk.getPath());  // first path is the shortest path

		// A* search finds a safer and friendlier path
		PathFinderEntry     pathEntry   = pathFinder.findPath(start, end, pathDelimiter);
		ArrayList<Point>    path        = getPointsFromPFE(pathEntry);

		boolean safe = true;
		for (Point point: path) {
			// but maybe there is no safe path
			if (point.getCondition() <= DANGEROUS_CONDITION_THRESHOLD) {
				paths[2]    = path;
				safe        = false;
				break;
			}
		}

		if (safe)
			paths[1] = path;
	}

	/**
	 * get the path by id
	 * 0 = shorest
	 * 1 = recommended
	 * 2 = dangerous
	 * will return dangerous path if no recommended path is available
	 * be sure to calculate a path first or throws runtime exception
	 * @param pathId    which path to get
	 * @return          string of directions to destination along the path
	 */
	public String getPath(int pathId) {
		ArrayList<Point> path = paths[pathId];

		if (pathId == 1 && !validatePath(paths[pathId])) {
			pathId = 2;
		}

		if (!validatePath(paths[pathId]))
			throw new RuntimeException("Cannot get empty path. Calculate path first.");

//		// build textual representation of path
		StringBuilder   ret   = new StringBuilder();

		List<String> directions = Geo.buildInstructions(path);
		for (String step: directions) {
			ret.append(step).append("\n");
		}

		buildMap();
		return ret.toString();
	}

	/**
	 * map the paths on OpenStreetMap
	 * saves to ./out/route.html
	 */
	public void buildMap() {
		// map display
		Path template   = Path.of("map/","map_template.html");
		Path html       = Path.of("map/", "route.html");
		try {
			ArrayList<Point> pathD = paths[2].isEmpty() ? null : paths[2];
			RouteMapWriter.writeMapHtml(template,  html, paths[0], paths[1], pathD);

		} catch (IOException e) {
			throw new RuntimeException("could not write map file " + e.getMessage());
		}
	}

	/**
	 *  calculate path then return one of them
	 *   get the path by id
	 * 	 0 = shorest
	 * 	 1 = recommended
	 * 	 2 = dangerous
	 * 	 will return dangerous path if no recommended path is available
	 * 	 be sure to calculate a path first or throws runtime exception
	 * @param idFrom    database id of starting Point
	 * @param idTo      db id of ending point
	 * @param pathId    which path to return?
	 * @return          string of directions to destination along the path
	 */
	public String getPath(String idFrom, String idTo, int pathId) {
		calculatePath(idFrom, idTo);
		return getPath(pathId);
	}

	/**
	 * @param path  an array list of points
	 * @return the path is not empty
	 */
	private boolean validatePath(ArrayList<Point> path) {
		return !path.isEmpty();
	}

	/**
	 * function overload for distance between two points
	 * @param p1    the first point
	 * @param p2    the second point
	 * @return      the ditance between them
	 */
	static double distanceBetween(Point p1, Point p2) {
		return distanceBetween(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
	}

	/**
	 *  simplified (Haversine) function for calculating distance between 2 coordinates
	 * @param lat1  latitude of coordinate 1
	 * @param lon1  longitude of coordinate 1
	 * @param lat2  latitude of coordinate 2
	 * @param lon2  longitude of coordinate 2
	 * @return real distance between the points in meters
	 */
	public static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
		final double R = 6371000.0; // Earth radius (m)
		double lat1r = Math.toRadians(lat1);
		double lat2r = Math.toRadians(lat2);
		double dLat = lat2r - lat1r;
		double dLon = Math.toRadians(lon2 - lon1);
		double x = dLon * Math.cos((lat1r + lat2r) / 2.0);
		double y = dLat;
		return R * Math.hypot(x, y); // meters
	}

	/**
	 *  see if there are still points which need street information to be found
	 * @return if there is anything in the service queue
	 */
	public boolean isWorking() {
		return !service.isQueueEmpty();
	}
}
