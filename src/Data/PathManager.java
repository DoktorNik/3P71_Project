package Data;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PathManager {
	private SQLite              sql;
	PointEnrichmentService      service;
	//private final LoadCSV     csvLoader;
	private final PathFinder    pathFinder      = new PathFinder();
	private ArrayList<Point>    path            = new ArrayList<>();
	private String              pathDelimiter;
	private double              bufferArea;
	private ArrayList<Point>    points          = new ArrayList<>();
	private static final String DEFAULT_DELIMITER   = " -> ";
	private static final double DEFAULT_BUFFER_AREA = 1086.82;  // average 1086.82 max 2239.73 thanks chat


	public enum StreetType {
		ROAD,
		PEDESTRIAN,
		FOOTWAY,
		CYCLEWAY,
		SERVICE,
		PATH,
		UNKNOWN
	}

	public PathManager(boolean useCache) throws IOException, SQLException {
		this(DEFAULT_DELIMITER, useCache, DEFAULT_BUFFER_AREA);
	}

	public PathManager(String pathDelimiter, boolean useCache) throws IOException, SQLException {
		this(pathDelimiter, useCache, DEFAULT_BUFFER_AREA);
	}

	public PathManager(String pathDelimiter, boolean useCache, double bufferArea) throws IOException, SQLException {
		this.bufferArea     = bufferArea;
		this.pathDelimiter  = pathDelimiter;
		init(useCache);
	}

	private void init(boolean useCache) throws IOException, SQLException {
		sql = new SQLite();
		points  = sql.selectAllPoints();

		//csvLoader   = new LoadCSV(fileName);
		// points      = csvLoader.getPoints();

		//System.err.println("Count: " + points.size());

		ReverseGeocoder geocoder =
				new ReverseGeocoder("3P71Project/1.0 (nik@wrinklyideas.com)");

		service =
				new PointEnrichmentService(geocoder, new PointEnrichmentService.Listener() {
					@Override
					public void onPointUpdated(Point p, StreetInfo info) {
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

		calculateDistances(useCache);
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
				p1.addConnection(p2, distance, bufferArea);
				p2.addConnection(p1, distance, bufferArea);     // undirected graph
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
		Point               start       = findPoint(idFrom);
		Point               end         = findPoint(idTo);
		PathFinderEntry     pathEntry   = pathFinder.findPath(start, end, pathDelimiter);
		ArrayList<Point>    rawPath     = new ArrayList<>();

		String[] pathId = pathEntry.getPath().split(pathDelimiter);
		for (String id : pathId) {
			rawPath.add(findPoint(id));
		}

		path = rawPath;
		//path = clean(rawPath);
	}

	 static ArrayList<Point> clean(ArrayList<Point> raw) {
		 // Tunables — adjust to taste
		 final double STRAIGHT_DEG = 25.0; // <= this = straight
		 final double SHORT_M      = 50.0; // very short hop
		 final double EPSILON_M    = 3.0;  // near-duplicate point

		 if (raw == null || raw.size() <= 2) return raw;

		ArrayList<Point> out = new ArrayList<>();
		out.add(raw.get(0));

		for (int i = 1; i < raw.size() - 1; i++) {
			Point prevKept = out.get(out.size() - 1);
			Point curr     = raw.get(i);
			Point next     = raw.get(i + 1);

			// 0) Drop near-duplicates
			if (Geo.distanceMeters(prevKept, curr) <= EPSILON_M) continue;

			double b1 = Geo.bearing(prevKept, curr);
			double b2 = Geo.bearing(curr, next);
			double deflection = minimalAngle(b1, b2);

			String prevStreet = prevKept.getStreetName();
			String currStreet = curr.getStreetName();
			String nextStreet = next.getStreetName();

			// 1) Single-frame street-name glitch: A, B, A  (with tiny deflection & short hop)
			if (eq(prevStreet, nextStreet) && !eq(prevStreet, currStreet)
					&& deflection <= STRAIGHT_DEG
					&& Geo.distanceMeters(prevKept, curr) <= SHORT_M) {
				// skip 'curr' — it's a blip
				continue;
			}

			// 2) “Straight” but street name changes — likely noise; drop curr
			if (deflection <= STRAIGHT_DEG && !eq(currStreet, prevStreet)) {
				// optionally: if nextStreet equals prevStreet, it’s definitely a blip; skip
				// otherwise you could keep and relabel, but you asked to ignore, so skip
				continue;
			}

			// 3) Otherwise, keep it
			out.add(curr);
		}

		// Always keep the last point
		out.add(raw.get(raw.size() - 1));
		return out;
	}

	private static boolean eq(String a, String b) {
		return Objects.equals(a, b);
	}

	private static double minimalAngle(double a, double b) {
		double d = (b - a + 360.0) % 360.0;
		return d <= 180.0 ? d : 360.0 - d;
	}


	public String getPath() {
		validatePath();

//		// build textual representation of path
		StringBuilder   ret   = new StringBuilder();

		List<String> directions = Geo.buildInstructions(path);
		for (String step: directions) {
			ret.append(step).append("\n");
		}

		// map display
		Path html = Path.of("out", "route.html");
		try {
			RouteHtml.writeLeafletHtml(html, path, true, true);
		} catch (IOException e) {
			throw new RuntimeException("could not write html file " + e.getMessage());
		}

		return ret.toString();
	}

	public void plotDataset() {
		if (points.isEmpty())
			throw new RuntimeException("Cannot plot points for an empty dataset. Check data was loaded correctly.");

		Path html = Path.of("out", "points.html");
		try {
			RouteHtml.plotPoints(html, points, true);
		} catch (IOException e) {
			throw new RuntimeException("could not write html file " + e.getMessage());
		}
	}

	public String getPath(String idFrom, String idTo) {
		calculatePath(idFrom, idTo);
		return getPath();
	}

	private void validatePath() {
		if(path.isEmpty())
			throw new RuntimeException("Cannot get empty path. Calculate path first.");
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

	public boolean isWorking() {
		return !service.isQueueEmpty();
	}
}
