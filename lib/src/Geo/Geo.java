/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Geo;

import Data.Point;
import Routing.Turn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * geo class for geographical stuff
 */
public final class Geo {

	public Geo() {}

	/** Returns bearing in degrees [0,360) from one point to another */
	public static double bearing(Point a, Point b) {
		double lat1 = Math.toRadians(a.getLatitude());
		double lat2 = Math.toRadians(b.getLatitude());
		double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2)
				- Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

		double bearing = Math.toDegrees(Math.atan2(y, x));
		return (bearing + 360.0) % 360.0;
	}

	/**
	 * we turning?
	 * @param bearing1  from
	 * @param bearing2  towards
	 * @return  type of turn (or lack thereof)
	 */
	static Turn turn(double bearing1, double bearing2) {
		double delta = (bearing2 - bearing1 + 360.0) % 360.0;

		if (delta < 30 || delta > 330)
			return Turn.STRAIGHT;
		else if (delta < 180)
			return Turn.RIGHT;
		else
			return Turn.LEFT;
	}

	/**
	 * a list of instructions to follow to reach the destination
	 * @param path  a list of Points representing the path to follow
	 * @return list of instructions to reach destination
	 */
	static List<Instruction> buildRaw(List<Point> path) {
		List<Instruction> out = new ArrayList<>();
		if (path.size() < 2) return out;

		out.add(new Instruction(Maneuver.START, path.get(0).getStreetName(), 0));

		for (int i = 1; i < path.size() - 1; i++) {
			Point prev = path.get(i - 1);
			Point curr = path.get(i);
			Point next = path.get(i + 1);

			double b1   = Geo.bearing(prev, curr);
			double b2   = Geo.bearing(curr, next);
			double dist = Geo.distanceMeters(curr, next);

			Maneuver m;
			switch (turn(b1, b2)) {
				case LEFT:
					m = Maneuver.LEFT;
					break;
				case RIGHT:
					m = Maneuver.RIGHT;
					break;
				default:
					m = Maneuver.STRAIGHT;
			}

			out.add(new Instruction(m, next.getStreetName(), dist));
		}

		out.add(new Instruction(Maneuver.ARRIVE, null, 0));
		return out;
	}

	/**
	 * merge repeated "go straight..." directions
	 * @param in the list of instructions
	 * @return the list of instructions with merged "straight" entries
	 */
	static List<Instruction> mergeStraights(List<Instruction> in) {
		List<Instruction> out = new ArrayList<>();

		for (Instruction cur : in) {
			if (out.isEmpty()) {
				out.add(cur);
				continue;
			}

			Instruction last = out.get(out.size() - 1);

			boolean canMerge =
					last.maneuver == Maneuver.STRAIGHT &&
							cur.maneuver == Maneuver.STRAIGHT &&
							Objects.equals(last.street, cur.street);

			if (canMerge) {
				last.meters += cur.meters;
			} else {
				out.add(cur);
			}
		}

		return out;
	}

	/**
	 * @param steps list of navigation instructions
	 * @return  list of strings
	 */
	static List<String> toText(List<Instruction> steps) {
		List<String> out = new ArrayList<>();

		for (Instruction i : steps) {
			int m = (int) Math.round(i.meters);

			switch (i.maneuver) {
				case START:
					out.add("Start on " + i.street);
					break;

				case STRAIGHT:
					out.add("Continue straight on " + i.street + " for " + m + " m");
					break;

				case LEFT:
					out.add("Turn left onto " + i.street + " in " + m + " m");
					break;

				case RIGHT:
					out.add("Turn right onto " + i.street + " in " + m + " m");
					break;

				case ARRIVE:
					out.add("Arrive at destination");
					break;
			}
		}
		return out;
	}

	/**
	 * utility method to call required methods in order to build a list of strings of navigation instructions
	 * @param path  a list of points represtenting the path to navigate
	 * @return       a list of strings of navigation instructions to navigate the path
	 */
	public static List<String> buildInstructions(List<Point> path) {
		List<Instruction> raw = buildRaw(path);
		List<Instruction> merged = mergeStraights(raw);
		return toText(merged);
	}

	/**
	 * @param a     first point
	 * @param b     second point
	 * @return      distance betweeen points in meters
	 */
	public static double distanceMeters(Point a, Point b) {
		final double R = 6371000.0; // Earth radius (m)

		double dLat = Math.toRadians(b.getLatitude() - a.getLatitude());
		double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

		double lat1 = Math.toRadians(a.getLatitude());
		double lat2 = Math.toRadians(b.getLatitude());

		double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(lat1) * Math.cos(lat2)
				* Math.sin(dLon / 2) * Math.sin(dLon / 2);

		return 2 * R * Math.asin(Math.sqrt(h));
	}


	/**
	 *
	 * @param turn      turn type
	 * @param street    street to turn on to
	 * @param meters    how far until turn
	 * @return          user-friendly phrase explaining the direction
	 */
	private static String phrase(Turn turn, String street, double meters) {
		int m = (int) Math.round(meters);

		switch (turn) {
			case LEFT:
				return "Turn left onto " + street + " in " + m + " m";
			case RIGHT:
				return "Turn right onto " + street + " in " + m + " m";
			default:
				return "Continue straight for " + m + " m";
		}
	}

}