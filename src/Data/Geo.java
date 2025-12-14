package Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Geo {

	private Geo() {}

	/** Returns bearing in degrees [0,360) */
	static double bearing(Point a, Point b) {
		double lat1 = Math.toRadians(a.getLatitude());
		double lat2 = Math.toRadians(b.getLatitude());
		double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());

		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1) * Math.sin(lat2)
				- Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

		double bearing = Math.toDegrees(Math.atan2(y, x));
		return (bearing + 360.0) % 360.0;
	}

	static Turn turn(double bearing1, double bearing2) {
		double delta = (bearing2 - bearing1 + 360.0) % 360.0;

		if (delta < 30 || delta > 330)
			return Turn.STRAIGHT;
		else if (delta < 180)
			return Turn.RIGHT;
		else
			return Turn.LEFT;
	}

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

	public static List<String> buildInstructions(List<Point> path) {
		List<Instruction> raw = buildRaw(path);
		List<Instruction> merged = mergeStraights(raw);
		return toText(merged);
	}

	static double distanceMeters(Point a, Point b) {
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