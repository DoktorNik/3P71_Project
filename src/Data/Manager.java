package Data;

import java.util.ArrayList;

public class Manager {
	private ArrayList<Point>    points      = new ArrayList<>();
	private final LoadCSV       csvLoader;
	private final PathFinder    pathFinder  = new PathFinder();

	public Manager(String fileName) {
		csvLoader   = new LoadCSV(fileName);
		points      = csvLoader.getPoints();
		calculateDistances();
		//System.err.println("Count: " + points.size());
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

	private void calculateDistances() {     // for setup
//		Point p1 = points.get(0);
		for (Point p1 : points) {
			for (Point p2 : points) {
			if (p1.equals(p2)) continue;    // don't connect to self
				double distance = distanceBetween(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
				p1.addConnection(p2, distance);
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

	public void printPath(String idFrom, String idTo) {
		Point               start;
		Point               end;
		PathFinderEntry     solution;

		try {
			start = findPoint(idFrom);
			end = findPoint(idTo);
		}
		catch(RuntimeException e) {
			System.err.println(e.getMessage());
			return;
		}

		solution = pathFinder.findPath(start, end);
		System.out.print("\nPath from " + idFrom + " to " + idTo + ":\n");
		System.out.println(solution.getPath());
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
