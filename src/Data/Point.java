package Data;

import java.util.ArrayList;

class Point {
	private final String        id;
	private final double        latitude;
	private final double        longitude;
	private final String        streetName;
	private final double        condition;
	private ArrayList<Point>    connections         = new ArrayList<>();
	private double              connectionDistance  = Double.MAX_VALUE;
	private double              bufferArea          = 1086.82;   // average 1086.82 max 2239.73 thanks chat

	Point(String id, double latitude, double longitude, String streetName, double condition) {
		this.id         = id;
		this.latitude   = latitude;
		this.longitude  = longitude;

		// 2do: update this when you get street names
		//this.streetName   = streetName;
		this.streetName     = id;

		this.condition  = condition;
	}

	Point(String id, double latitude, double longitude, String streetName, double condition, double bufferArea) {
		this(id, latitude, longitude, streetName, condition);
		this.bufferArea = bufferArea;
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

	double getCondition() {
		return condition;
	}

	void addConnection(Point point, double distance) {

		// no dups
		if (connections.contains(point))
			return;

		double target = (connectionDistance + bufferArea) / 2;
		//System.err.print("DEBUG: distance = " + distance + " target = " + target + " ");

		// lol, no
		if (distance > target) {
			//System.err.println("BYE!");
			return;
		}
		// ohhh closer connection
		else if (target < connectionDistance) {
			//System.err.print(" UPDATED!");
			connections = new ArrayList<>();
		}
		else {
		//	System.err.print(" APPENDED");
		}

		//System.err.println();
		// add and update
		connections.add(point);
		connectionDistance = distance;
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