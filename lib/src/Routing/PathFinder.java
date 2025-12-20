/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Routing;

import Data.Point;
import lib.PathManager;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class PathFinder {
	double  minCondition    = 0.25;     // unacceptable road conditions below this point
	double  streetChangeMod = 1.10;     // penalty for turning onto another street

	/**
	 * search algorithm to find a nice path (route)
	 * @param start the point to start at
	 * @param end   the point to end at (destination)
	 * @param pathDelimiter the path delimiter used by the manager
	 * @return  PathFinderEntry node with the path from start to end
	 * @throws RuntimeException if you try pathing to where you already are or no path is available (check graph)
	 */
	public PathFinderEntry findPath(Point start, Point end, String pathDelimiter) throws RuntimeException {
		ArrayList<Point>                searched            = new ArrayList<>();
		PriorityQueue<PathFinderEntry>  frontier            = new PriorityQueue<>();

		if (start.equals(end)) {
			throw new RuntimeException("Cannot find path to current position");
		}

		double distanceToGoal;
		double distanceFromLastPoint;

		// initial node
		distanceToGoal = PathManager.distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());
		PathFinderEntry pfe = new PathFinderEntry(start, start, start.getId(), 0, distanceToGoal);
		frontier.add(pfe);
		searched.add(start);

		// search the frontier
		while (!frontier.isEmpty()) {
			// check for solution
			PathFinderEntry entry   = frontier.remove();
			Point entryPoint        = entry.getPoint();

			if (entryPoint.equals(end))
				return entry;

			// score and add adjacent points to the frontier
			for (Point point : entryPoint.getConnections()) {
				String pathStr = entry.getPath() + pathDelimiter + point.getId();   // the path so far

				// but first, did we find it?
				if (point.equals(end)) {
					distanceFromLastPoint	= PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), entryPoint.getLatitude(), entryPoint.getLongitude());
					return new PathFinderEntry(point, entryPoint, pathStr, distanceFromLastPoint, 0);
				}

				// don't duplicate search
				if (searched.contains(point))   continue;

				// now add to search queue
				searched.add(point);
				double heuristicScore   = generateScore(point, entry.getPrevPoint(), end);
				distanceFromLastPoint	= PathManager.distanceBetween(point.getLatitude(), point.getLongitude(), entryPoint.getLatitude(), entryPoint.getLongitude());

				// create and insert prioritized
				frontier.add(new PathFinderEntry(point, entryPoint, pathStr, distanceFromLastPoint, heuristicScore));
			}
		}
		throw new RuntimeException("Failed to find path from " + start.getStreetName() + " to " + end.getStreetName());
	}

	/**
	 * generate the heuristic score
	 * based on distance to destination
	 * avoid slowing to turn for minor gains
	 * adjust based on condition of road
	 * @param point         the point we're at
	 * @param prevPoint     the point we came from
	 * @param end           the destination point
	 * @return  double heuristic score
	 */
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
