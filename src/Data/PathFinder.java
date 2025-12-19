package Data;

import java.util.ArrayList;
import java.util.PriorityQueue;

class PathFinder {
	double  minCondition    = 0.25;     // unacceptable road conditions beyond this point
	double  streetChangeMod = 1.10;     // penalty for turning onto another street

	PathFinderEntry findPath(Point start, Point end, String pathDelimiter) throws RuntimeException {
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
				String pathStr = entry.getPath() + pathDelimiter + point.getId();

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
		double heuristicScore   = PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), end.getLatitude(), end.getLongitude());

		// penalize changing streets
		// people prefer cruisin' with the big man instead of snaking through the city
		if (!point.getStreetName().equals(prevPoint.getStreetName())) {
			heuristicScore *= streetChangeMod;
		}

		// penalize poor road quality
		double condition = point.getCondition();
		if (condition < minCondition)                       // thar be dragons
			heuristicScore = Double.MAX_VALUE;
		else {
			if (condition < 1.0)
				heuristicScore *= 1+(1 - point.getCondition());   // what am I even paying taxes for? XD
		}

		return heuristicScore;
	}
}
