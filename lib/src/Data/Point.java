/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Data;

import lib.PathManager;

import java.util.ArrayList;

/**
 *  The point class. Represents a point on the map and node on the graph.
 */
public class Point {
	private final int               idInt;
	private final String            id;
	private final double            latitude;
	private final double            longitude;
	private String                  streetName;
	private PathManager.StreetType streetType  = PathManager.StreetType.UNKNOWN;
	private double                  condition;
	private ArrayList<Point>        connections         = new ArrayList<>();
	private double                  connectionDistance  = Double.MAX_VALUE;

	/**
	 * add a new point to the set of known points
	 * @param idInt         the id of the point for use in adjacency matrix
	 * @param id            the id of the point from the api as stored in the database
	 * @param latitude      gps lat
	 * @param longitude     gps long
	 * @param streetName    name of street
	 * @param streetType    type of street, should be PathManager.StreetType.ROAD
	 * @param condition
	 */
	public Point(int idInt, String id, double latitude, double longitude, String streetName, PathManager.StreetType streetType, double condition) {
		this.idInt      = idInt;
		this.id         = id;
		this.latitude   = latitude;
		this.longitude  = longitude;
		this.condition  = condition;
		this.streetType = streetType;
		this.streetName = streetName;
	}

	/**
	 * @return the points this point is connected to
	 */
	public ArrayList<Point> getConnections() {
		return connections;
	}

	public int getIdInt() {
		return idInt;
	}

	public String getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getStreetName() {
		return streetName;
	}

	public PathManager.StreetType getStreetType() {
		return streetType;
	}

	public double getCondition() {
		return condition;
	}

	public void setStreet(String streetName, PathManager.StreetType type) {
		this.streetName = streetName;
		this.streetType = type;
	}

	public void setCondition(double condition) {
		this.condition = condition;
	}

	/**
	 * add a point this point is connected to
	 * @param point         the point connected to
	 * @param distance      distance between points
	 * @param bufferArea    maximum distance to connect
	 */
	public void addConnection(Point point, double distance, double bufferArea) {

		// no dups
		if (connections.contains(point))
			return;

		if (distance <= bufferArea)
			connections.add(point);
	}

	public void printConnections() {
		System.out.print("Connection Distance is " + connectionDistance + "\n");
		for (Point connection : connections) {
			System.out.print(connection.toString() + "\n");
		}
		System.out.println();
	}

	@Override
	public String toString() {
		return "Data.Point{" +
				"id='" + id + '\'' +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", streetName='" + streetName + '\'' +
				", condition='" + condition + '\'' +
				'}';
	}
}