/**
 * Nikolas Fraser
 * 5538939
 * Dec 19, 2025
 * COSC 3P71
 */

package Geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import Data.StreetInfo;
import lib.PathManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

/**
 * get street name from gps co-ord via OpenStreetMap API
 */
public final class ReverseGeocoder {

	private final ObjectMapper mapper = new ObjectMapper();
	private final String userAgent;

	/**
	* get street name from gps co-ord via OpenStreetMap API
	*/
	public ReverseGeocoder(String userAgent) {
		this.userAgent = userAgent;
	}

	/**
	 * get the street name and type from the nominatim api
	 * @param lat       point's latitude
	 * @param lon       pint's longitude
	 * @return          info about the street
	 */
	public Optional<StreetInfo> streetInfo(double lat, double lon) {
		try {
			String urlStr = String.format(
					"https://nominatim.openstreetmap.org/reverse?lat=%f&lon=%f&zoom=18&addressdetails=1&format=json",
					lat, lon
			);

			HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", userAgent);

			try (InputStream is = conn.getInputStream()) {
				JsonNode addr = mapper.readTree(is).path("address");

				if (addr.hasNonNull("road"))
					return Optional.of(new StreetInfo(addr.get("road").asText(), PathManager.StreetType.ROAD));

				if (addr.hasNonNull("pedestrian"))
					return Optional.of(new StreetInfo(addr.get("pedestrian").asText(), PathManager.StreetType.PEDESTRIAN));

				if (addr.hasNonNull("footway"))
					return Optional.of(new StreetInfo(addr.get("footway").asText(), PathManager.StreetType.FOOTWAY));

				if (addr.hasNonNull("cycleway"))
					return Optional.of(new StreetInfo(addr.get("cycleway").asText(), PathManager.StreetType.CYCLEWAY));

				if (addr.hasNonNull("service"))
					return Optional.of(new StreetInfo(addr.get("service").asText(), PathManager.StreetType.SERVICE));

				if (addr.hasNonNull("path"))
					return Optional.of(new StreetInfo(addr.get("path").asText(), PathManager.StreetType.PATH));

				return Optional.empty();
			}
		} catch (Exception e) {
			return Optional.empty();
		}
	}
}
