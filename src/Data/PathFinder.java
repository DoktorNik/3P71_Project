package Data;

import java.util.ArrayList;
import java.util.PriorityQueue;

class PathFinder {

	PathFinderEntry findPath(Point start, Point end, String pathDelimeter) throws RuntimeException {
		ArrayList<Point>                searched    = new ArrayList<>();
		PriorityQueue<PathFinderEntry>  frontier    = new PriorityQueue<>();

		if (start.equals(end)) {
			throw new RuntimeException("Cannot find path to current position");
		}

		double distanceToGoal;
		double distanceFromLastPoint;

		// initial
		distanceToGoal = PathManager.distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
		PathFinderEntry pfe = new PathFinderEntry(start, start.getId(), 0, distanceToGoal);
		frontier.add(pfe);
		searched.add(start);

		// search
		while (!frontier.isEmpty()) {
			// check for solution
			PathFinderEntry entry   = frontier.remove();
			Point entryPoint        = entry.getPoint();

			// yay, found it
			if (entryPoint.equals(end))
				return entry;

			// score and add available paths to frontier
			for (Point point : entryPoint.getConnections()) {
				String pathStr = entry.getPath() + pathDelimeter + point.getId();

				// but first, did we find it?
				if (point.equals(end)) {
					distanceFromLastPoint	= PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), entryPoint.getLatitude(), entryPoint.getLongitude());
					return new PathFinderEntry(point, pathStr, distanceFromLastPoint, 0);
				}

				// don't duplicate search
				if (searched.contains(point))   continue;

				// actually add to search queue
				searched.add(point);
				distanceToGoal          = PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), end.getLatitude(), end.getLongitude());
				distanceFromLastPoint	= PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), entryPoint.getLatitude(), entryPoint.getLongitude());

				// create and insert prioritized
				frontier.add(new PathFinderEntry(point, pathStr, distanceFromLastPoint, distanceToGoal));
			}
		}
		throw new RuntimeException("Failed to find path from " + start.getStreetName() + " to " + end.getStreetName());
	}
}
