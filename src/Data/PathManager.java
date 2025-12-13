package Data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class PathManager {
	private SQLite              sql;
	private ArrayList<Point>    points          = new ArrayList<>();
	//private final LoadCSV       csvLoader;
	private final PathFinder    pathFinder      = new PathFinder();
	private ArrayList<Point>    path            = new ArrayList<>();
	private String              pathDelimiter   = " -> ";

	public PathManager(boolean useCache) throws IOException, SQLException {
		sql = new SQLite();
		points  = sql.selectAllPoints();

		//csvLoader   = new LoadCSV(fileName);
		// points      = csvLoader.getPoints();
		calculateDistances(useCache);
		//System.err.println("Count: " + points.size());
	}

	public PathManager(String pathDelimiter, boolean useCache) throws IOException, SQLException {
		this(useCache);
		this.pathDelimiter  = pathDelimiter;
	}

	public void printPoints() {
		for (Point point : points) {
			System.out.println(point.toString());
		}
	}

	public void printConnections() {
		for (Point point : points) {
			point.printConnections();
		}
	}

	private void calculateDistances(boolean useCache) {     // for setup
//		Point p1 = points.get(0);
		for (Point p1 : points) {
			for (Point p2 : points) {
			if (p1.equals(p2)) continue;    // don't connect to self
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
				p1.addConnection(p2, distance);
				p2.addConnection(p1, distance);     // undirected graph
			}
		}
	}

	private Point findPoint(String id) {
		for (Point point : points) {
			if (point.getId().equals(id)) {
				return point;
			}
		}
		throw new RuntimeException("Could not find point with id '" + id + "'");
	}

	public void calculatePath(String idFrom, String idTo) throws RuntimeException {
		Point           start       = findPoint(idFrom);
		Point           end         = findPoint(idTo);
		PathFinderEntry pathEntry   = pathFinder.findPath(start, end, pathDelimiter);
		path    = new ArrayList<>();

		String[] pathId = pathEntry.getPath().split(pathDelimiter);
		for (String id : pathId) {
			path.add(findPoint(id));
		}
	}

	public String getPath() {
		if(path.isEmpty())
			throw new RuntimeException("Cannot get empty path. Calculate path first.");

		// build textual representation of path
		Point           start = path.get(0);
		Point           end   = path.get(path.size()-1);
		StringBuilder   ret   = new StringBuilder();

		ret.append("\nPath from ")
				.append(start.getStreetName())
				.append(" to ")
				.append(end.getStreetName())
				.append(":\n");

		for (int i = 0; i < path.size(); i++) {
			ret.append(path.get(i).getStreetName());
			if (i < path.size() - 1)
				ret.append(pathDelimiter);
		}

		return ret.toString();
	}

	public String getPath(String idFrom, String idTo) {
		calculatePath(idFrom, idTo);
		return getPath();
	}

	// simplified function for calculating distance between 2 coordinates
	static double distanceBetween(double lat1, double lon1, double lat2, double lon2) {
		final double R = 6371000.0; // Earth radius (m)
		double lat1r = Math.toRadians(lat1);
		double lat2r = Math.toRadians(lat2);
		double dLat = lat2r - lat1r;
		double dLon = Math.toRadians(lon2 - lon1);
		double x = dLon * Math.cos((lat1r + lat2r) / 2.0);
		double y = dLat;
		return R * Math.hypot(x, y); // meters
	}
}
