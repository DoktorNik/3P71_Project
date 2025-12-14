package Data;

import java.util.ArrayList;

class Point {
	private final String            id;
	private final double            latitude;
	private final double            longitude;
	private String                  streetName;
	private PathManager.StreetType  streetType  = PathManager.StreetType.UNKNOWN;
	private final double            condition;
	private ArrayList<Point>        connections         = new ArrayList<>();
	private double                  connectionDistance  = Double.MAX_VALUE;

	Point(String id, double latitude, double longitude, String streetName, PathManager.StreetType streetType, double condition) {
		this.id         = id;
		this.latitude   = latitude;
		this.longitude  = longitude;
		this.condition  = condition;
		this.streetType = streetType;
		this.streetName = streetName;
	}

	ArrayList<Point> getConnections() {
		return connections;
	}

	String getId() {
		return id;
	}

	double getLatitude() {
		return latitude;
	}

	double getLongitude() {
		return longitude;
	}

	String getStreetName() {
		return streetName;
	}

	public PathManager.StreetType getStreetType() {
		return streetType;
	}

	double getCondition() {
		return condition;
	}

	void setStreet(String streetName, PathManager.StreetType type) {
		this.streetName = streetName;
		this.streetType = type;
	}

	void addConnection(Point point, double distance, double bufferArea) {

		// no dups
		if (connections.contains(point))
			return;

		//double target = bufferArea;
		//double target = (connectionDistance + bufferArea) / 2;
		//System.err.print("DEBUG: distance = " + distance + " target = " + target + " ");

		if (distance <= bufferArea)
			connections.add(point);


//		// lol, no
//		if (distance > target) {
//			//System.err.println("BYE!");
//			return;
//		}
//		// ohhh closer connection
//		else if (target < target) {
//			//System.err.print(" UPDATED!");
//			connections = new ArrayList<>();
//		}
//		else {
//		//	System.err.print(" APPENDED");
//		}

		//System.err.println();
		// add and update
//		connections.add(point);
//		connectionDistance = distance;
	}

	void printConnections() {
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