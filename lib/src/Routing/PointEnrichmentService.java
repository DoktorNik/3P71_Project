/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Routing;

import Data.Point;
import Data.StreetInfo;
import Geo.ReverseGeocoder;
import lib.PathManager;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * threading to call the API no more than 1/sec
 */
public final class PointEnrichmentService {

	public interface Listener {
		void onPointUpdated(Point p, StreetInfo info);
		default void onError(Point p, Exception e) {}

		default void onIdle() {} // called when there was no work in a tick
	}

	private final ReverseGeocoder geocoder;
	private final Listener listener;
	private final BlockingQueue<Point> queue = new LinkedBlockingQueue<>();
	private final ScheduledExecutorService scheduler =
			Executors.newSingleThreadScheduledExecutor(r -> {
				Thread t = new Thread(r, "point-enricher");
				t.setDaemon(true);
				return t;
			});

	// cache rounded (lat,lon) -> StreetInfo
	private final Map<String, StreetInfo> cache = new ConcurrentHashMap<>();

	private volatile boolean started = false;

	public PointEnrichmentService(ReverseGeocoder geocoder, Listener listener) {
		this.geocoder = Objects.requireNonNull(geocoder);
		this.listener = (listener != null) ? listener : new Listener() {
			@Override
			public void onPointUpdated(Point p, StreetInfo info) {

			}
		};
	}

	/** Start background worker (idempotent). */
	public synchronized void start() {
		if (started) return;
		started = true;
		// run once per second to respect public Nominatim
		scheduler.scheduleAtFixedRate(this::tick, 0, 1, TimeUnit.SECONDS);
	}

	/** Stop background worker (idempotent). */
	synchronized void stop() {
		if (!started) return;
		scheduler.shutdownNow();
		started = false;
	}

	/** Enqueue a single point for enrichment (non-blocking). */
	public void enqueue(Point p) {
		if (p != null) queue.offer(p);
	}

	/** Enqueue a batch (non-blocking). */
	void enqueueAll(List<Point> points) {
		if (points == null) return;
		for (Point p : points) enqueue(p);
	}

	private void tick() {
		try {
			Point p = queue.poll();
			if (p == null) {
				listener.onIdle();
				return;
			}

			String key = cacheKey(p.getLatitude(), p.getLongitude());
			StreetInfo info = cache.computeIfAbsent(key, k ->
					geocoder.streetInfo(p.getLatitude(), p.getLongitude()).orElse(null)
			);

			if (info != null) {
				p.setStreet(info.getName(), info.getType());
				listener.onPointUpdated(p, info);
			} else {
				// leave point unchanged; still notify if you want
				listener.onPointUpdated(p, new StreetInfo(null, PathManager.StreetType.UNKNOWN));
			}
		} catch (Exception e) {
			// If we know which point failed, the code above would pass it; here we don’t
			listener.onError(null, e);
		}
	}

	public boolean isQueueEmpty() {
		return queue.isEmpty();
	}

	private static String cacheKey(double lat, double lon) {
		// ~1.1 m precision; adjust if you want more/less cache hits
		return String.format(Locale.ROOT, "%.5f,%.5f", lat, lon);
	}
}
