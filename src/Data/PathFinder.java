package Data;

import java.util.ArrayList;
import java.util.PriorityQueue;

class PathFinder {
	double                          streetChangeMod = 2.5;

	PathFinderEntry findPath(Point start, Point end, String pathDelimeter) throws RuntimeException {
		ArrayList<Point>                searched            = new ArrayList<>();
		PriorityQueue<PathFinderEntry>  frontier            = new PriorityQueue<>();

		if (start.equals(end)) {
			throw new RuntimeException("Cannot find path to current position");
		}

		double distanceToGoal;
		double distanceFromLastPoint;

		// initial
		distanceToGoal = PathManager.distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
		PathFinderEntry pfe = new PathFinderEntry(start, start, start.getId(), 0, distanceToGoal);
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
					return new PathFinderEntry(point, entryPoint, pathStr, distanceFromLastPoint, 0);
				}

				// don't duplicate search
				if (searched.contains(point))   continue;

				// actually add to search queue
				searched.add(point);
				double heuristicScore   = generateScore(point, entry.getPrevPoint(), end);
				//distanceToGoal          = PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), end.getLatitude(), end.getLongitude());
				distanceFromLastPoint	= PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), entryPoint.getLatitude(), entryPoint.getLongitude());

				// create and insert prioritized
				frontier.add(new PathFinderEntry(point, entryPoint, pathStr, distanceFromLastPoint, heuristicScore));
			}
		}
		throw new RuntimeException("Failed to find path from " + start.getStreetName() + " to " + end.getStreetName());
	}

	private double generateScore(Point point, Point prevPoint, Point end) {
		double distanceToGoal   = PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), end.getLatitude(), end.getLongitude());
		double heuristicScore   = distanceToGoal;

		// penalize changing streets
		if (!point.getStreetName().equals(prevPoint.getStreetName())) {
			heuristicScore *= streetChangeMod;
		}

		return heuristicScore;
	}
}
