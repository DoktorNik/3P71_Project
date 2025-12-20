/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Geo;

import Data.Point;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * shows the routes on OSM
 */
public class RouteMapWriter {

	/**
	 * shows the routes as overlay on OpenStreetMap
	 * @param templateFile  the base template map
	 * @param outFile       the file to write the map with overlay to
	 * @param shortest      shortest path (required)
	 * @param recommended   recommended path (if not dangerous)
	 * @param dangerous     dangerous path (if no safe path recommended)
	 * @throws IOException  for file writing errors
	 */
	public static void writeMapHtml(
			Path templateFile,              // map_template.html
			Path outFile,                   // map.html
			ArrayList<Point> shortest,      // required
			ArrayList<Point> recommended,   // optional (nullable/empty)
			ArrayList<Point> dangerous      // optional (nullable/empty)
	) throws IOException {

		List<RouteSpec> specs = new ArrayList<>();

		specs.add(buildSpec("Shortest", "#ef6c00", shortest));

		if (recommended != null && recommended.size() >= 2) {
			specs.add(buildSpec("Recommended", "#2e7d32", recommended));
		}
		if (dangerous != null && dangerous.size() >= 2) {
			specs.add(buildSpec("Dangerous", "#c62828", dangerous));
		}

		// convert strings to JSON
		String routesJson = toRoutesJson(specs);

		// read in template
		String html = Files.readString(templateFile, StandardCharsets.UTF_8)
				.replace("__ROUTES_JSON__", routesJson);

		// write output
		Files.writeString(outFile, html, StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
	}

	/**
	 * route specifications
	 * @param name  name of route
	 * @param color colour of route
	 * @param route array list of points representing the route
	 * @return  route spec
	 */
	private static RouteSpec buildSpec(String name, String color, ArrayList<Point> route) {
		if (route == null || route.size() < 2) {
			throw new IllegalArgumentException(name + " route must have at least 2 points.");
		}

		double minCond = Double.POSITIVE_INFINITY;
		ArrayList<RoutePoint> pts = new ArrayList<>(route.size());

		for (int i = 0; i < route.size(); i++) {
			Point p = route.get(i);
			minCond = Math.min(minCond, p.getCondition());
			pts.add(new RoutePoint(p.getLatitude(), p.getLongitude(), i)); // route-local index
		}

		String conditionStr = BigDecimal.valueOf(minCond)
				.multiply(BigDecimal.valueOf(100))
				.setScale(0, RoundingMode.HALF_UP)
				.toPlainString() + "%";

		return new RouteSpec(name, color, conditionStr, pts);
	}

	/**
	 * route point utility class
	 */
	private static final class RoutePoint {
		final double lat;
		final double lon;
		final int idx; // index within the route list
		RoutePoint(double lat, double lon, int idx) {
			this.lat = lat;
			this.lon = lon;
			this.idx = idx;
		}
	}

	/**
	 * route spec utility class
	 */
	private static final class RouteSpec {
		final String name;
		final String color;
		final String conditionStr;
		final List<RoutePoint> points;
		RouteSpec(String name, String color, String conditionStr, List<RoutePoint> points) {
			this.name = name;
			this.color = color;
			this.conditionStr = conditionStr;
			this.points = points;
		}
	}

	/**
	 * convert list of route specs to JSON
	 * @param specs list of route specs
	 * @return  JSON string
	 */
	private static String toRoutesJson(List<RouteSpec> specs) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < specs.size(); i++) {
			if (i > 0) sb.append(",");
			RouteSpec r = specs.get(i);

			sb.append("{");
			sb.append("\"name\":").append(jsonString(r.name)).append(",");
			sb.append("\"color\":").append(jsonString(r.color)).append(",");
			sb.append("\"conditionStr\":").append(jsonString(r.conditionStr)).append(",");
			sb.append("\"points\":").append(pointsJson(r.points));
			sb.append("}");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * convert route point to JSON
	 * @param points    list of route points
	 * @return          JSON string
	 */
	private static String pointsJson(List<RoutePoint> points) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < points.size(); i++) {
			if (i > 0) sb.append(",");
			RoutePoint p = points.get(i);
			sb.append("{")
					.append("\"lat\":").append(p.lat).append(",")
					.append("\"lon\":").append(p.lon).append(",")
					.append("\"idx\":").append(p.idx)
					.append("}");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * escape for JSONification
	 * @param s input string
	 * @return  escaped string
	 */
	private static String jsonString(String s) {
		String escaped = s.replace("\\", "\\\\").replace("\"", "\\\"");
		return "\"" + escaped + "\"";
	}
}
